package com.bacop.jwt_jaspi;

import java.util.Base64;
import java.util.HashMap;
import java.util.Set;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.sql.DataSource;

import javax.security.auth.message.callback.GroupPrincipalCallback;
import java.util.HashSet;

/**
 *
 */
public class JwtJaspiServerAuthModule implements ServerAuthModule {

    private static final Logger LOG = Logger.getLogger(JwtJaspiServerAuthModule.class.getName());

    private Map<String, String> users;
    private Map<String, Set<String>> roles;
    private ArrayList<String> optionKeys;
    private Map<String, String> optionValues;
    private CallbackHandler handler;

    public static String PART_SEPARATOR = "###";
    public static String ROLES_LIST_SEPARATOR = "[\\s]*,[\\s]*";
    //
    public static Boolean DENY_EMPTY_ROLESET = true;
    //
    private static final String ROLES_HEADER = "roles_header";
    private static final String USER_HEADER = "user_header";
    private static final String TOKEN_HEADER = "token_header";
//  private static final String DATASOURCE = "datasource";
//  private static final String ROLES_QUERY = "rolesQuery";
    private static final String CERTIFICATE_LOCATION = "certificate_location";
    private static final String JWT_ALLOWANCE = "jwt_allowance";
    private static final String AIS_URL = "ais_url";
    private static final String ALLOW_PREFIX = "allow_prefix";
    private static final String ALLOWED_SYSTEM = "allowed_system";

    public JwtJaspiServerAuthModule() {
        users = new HashMap<>();
        roles = new HashMap<>();
        optionValues = new HashMap<>();
        optionKeys = new ArrayList<>();
        optionKeys.add(ROLES_HEADER);
        optionKeys.add(USER_HEADER);
        optionKeys.add(TOKEN_HEADER);
//    optionKeys.add(DATASOURCE);
//    optionKeys.add(ROLES_QUERY);
        optionKeys.add(CERTIFICATE_LOCATION);
//    optionKeys.add(DATASOURCE);
        optionKeys.add(JWT_ALLOWANCE);
        optionKeys.add(AIS_URL);
        optionKeys.add(ALLOW_PREFIX);
        optionKeys.add(ALLOWED_SYSTEM);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, Map options) throws AuthException {

        LOG.log(Level.INFO, "JWT_JASPI Initializing");

        this.handler = handler;
        // loop over options and create the users and roles sets
        if (options != null) {
            for (Map.Entry<Object, Object> e : (Set<Map.Entry<Object, Object>>) options.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    String key = e.getKey().toString();
                    String value = e.getValue().toString();
                    optionValues.put(key, value);
                }
            }
        }
        String configPrefix = this.getClass().getPackage().getName() + ".";
        CustomJwtUtils cju = new CustomJwtUtils();
        optionKeys.stream().filter(key -> (cju.isBlank(optionValues.get(key)))).forEachOrdered(key -> {
            optionValues.put(key, System.getProperty(configPrefix + key));
        });
        LOG.log(Level.INFO, "JWT_JASPI Options read:{0}", options);
        LOG.log(Level.INFO, "JWT_JASPI Options parsed:{0}", optionValues);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Class[] getSupportedMessageTypes() {
        return new Class[]{HttpServletRequest.class, HttpServletResponse.class};
    }

    private String[] getCredentials(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Basic ")) {
            String decoded = new String(Base64.getDecoder().decode(header.substring(6)));
            return decoded.split(":");
        } else {
            return null;
        }
    }

    private void applyCallbacks(String username, Subject clientSubject) throws IOException, UnsupportedCallbackException {
        Set<String> userRoles = roles.get(username);
        Callback[] callbacks = new Callback[userRoles == null ? 1 : 2];
        callbacks[0] = new CallerPrincipalCallback(clientSubject, username);
        if (userRoles != null) {
            callbacks[1] = new GroupPrincipalCallback(clientSubject, userRoles.toArray(new String[0]));
        }
        LOG.log(Level.FINE, "JWT_JASPI Applying callback for user:{0} roles:{1}", new String[]{username, userRoles.toString()});
        handler.handle(callbacks);
    }

    /**
     * @inheritDoc
     */
    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {
        try {

            HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
            HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();

            AuthStatus result = AuthStatus.SEND_FAILURE;

            boolean goon = true;

            boolean isUaserActive = false;

            Integer jwt_allowance = 5 * 60 + 2; // 30 days = 2592000

            CustomJwtUtils cju = new CustomJwtUtils();

//      LOG.log(Level.INFO, "JWT_JASPI {0}:{1}", new String[]{DATASOURCE, optionValues.get(DATASOURCE)});
//      LOG.log(Level.INFO, "JWT_JASPI {0}:{1}", new String[]{ROLES_QUERY, optionValues.get(ROLES_QUERY)});
            LOG.log(Level.INFO, "JWT_JASPI {0}:{1}", new String[]{CERTIFICATE_LOCATION, optionValues.get(CERTIFICATE_LOCATION)});

            final String token = request.getHeader(optionValues.get(TOKEN_HEADER));
            final String xuser = request.getHeader(optionValues.get(USER_HEADER));

            //
            // - sanity checks
            //
            if (!cju.isBlank(optionValues.get(ALLOW_PREFIX))) {
                LOG.log(Level.INFO, "JWT_JASPI ALWAYS ALLOW REQUESTS STARTING WITH:{0}", optionValues.get(ALLOW_PREFIX));
            }
            // - allow early success of matching requests 
            {
                String uri = request.getRequestURI();
                String[] allowList = optionValues.get(ALLOW_PREFIX).split(",");
                LOG.log(Level.INFO, "JWT_JASPI INPUT REQUEST URI: {0}", uri);

                for (String allow : allowList) {

                    LOG.log(Level.FINE, "JWT_JASPI CHEKING URI {0} AGAINST {1}", new String[]{uri, allow});

                    if (request.getRequestURI().startsWith(allow)) {

                        LOG.log(Level.INFO, "JWT_JASPI PREFIX CHECK PASSED");

                        roles = new HashMap<>();
                        Set<String> roleSet = new HashSet<>();
                        roleSet.add("rest-all");
                        roles.put(xuser, roleSet);

                        applyCallbacks(xuser, clientSubject);
                        result = AuthStatus.SUCCESS;

                        return result;

                    }
                }
            }

            // - if prefix check fails, regular checks follow
            if (goon && cju.isBlank(optionValues.get(AIS_URL))) {
                goon = false;
                LOG.log(Level.INFO, "JWT_JASPI ERROR THE URL FOR THE AUTHORITY_ITEM_SERVICE IS MISSING:{0}", optionValues.get(AIS_URL));
            }

            if (goon && cju.isBlank(optionValues.get(USER_HEADER))) {
                goon = false;
                LOG.log(Level.INFO, "JWT_JASPI ERROR USER HEADER CONFIGURATION IS MISSING:{0}", optionValues.get(USER_HEADER));
            }

            if (goon && cju.isBlank(optionValues.get(TOKEN_HEADER))) {
                goon = false;
                LOG.log(Level.INFO, "JWT_JASPI ERROR TOKEN HEADER CONFIGURATION IS MISSING:{0}", optionValues.get(TOKEN_HEADER));
            }

            if (goon && cju.isBlank(optionValues.get(ROLES_HEADER))) {
                goon = false;
                LOG.log(Level.INFO, "JWT_JASPI ERROR ROLES_HEADER CONFIGURATION IS MISSING:{0}", optionValues.get(ROLES_HEADER));
            }

            if (goon && cju.isBlank(optionValues.get(ALLOWED_SYSTEM))) {
                goon = false;
                LOG.log(Level.INFO, "JWT_JASPI ERROR ALLOWED_SYSTEM CONFIGURATION IS MISSING:{0}", optionValues.get(ALLOWED_SYSTEM));
            }

//      if (goon && cju.isBlank(optionValues.get(DATASOURCE))) {
//        goon = false;
//        LOG.log(Level.INFO, "JWT_JASPI ERROR DATASOURCE CONFIGURATION IS MISSING:{0}", optionValues.get(DATASOURCE));
//      }
            if (goon && cju.isBlank(optionValues.get(CERTIFICATE_LOCATION))) {
                goon = false;
                LOG.log(Level.INFO, "JWT_JASPI ERROR CERTIFICATE LOCATION IS EMPTY :{0}", optionValues.get(CERTIFICATE_LOCATION));
            }
            File certFile = new File(optionValues.get(CERTIFICATE_LOCATION));
            if (goon && !Files.isReadable(certFile.toPath())) {
                LOG.log(Level.INFO, "JWT_JASPI ERROR CERTIFICATE CANNOT BE READ :{0}", optionValues.get(CERTIFICATE_LOCATION));
                goon = false;
            }

            LOG.log(Level.INFO, "JWT_JASPI header:{0} value:{1}", new String[]{TOKEN_HEADER, token});
            LOG.log(Level.INFO, "JWT_JASPI header:{0} value:{1}", new String[]{USER_HEADER, xuser});

            if (goon && cju.isBlank(token)) {
                goon = false;
                LOG.log(Level.INFO, "JWT_JASPI ERROR JWT TOKEN CANNOT BE FOUND IN THE HTTP HEADERS OR EMPTY :{0}", token);
            }

            if (goon && cju.isBlank(xuser)) {
                goon = false;
                LOG.log(Level.INFO, "JWT_JASPI ERROR USER CANNOT BE FOUND IN THE HTTP HEADERS OR EMPTY :{0}", xuser);
            }

            if (goon && cju.isBlank(optionValues.get(JWT_ALLOWANCE))) {
                LOG.log(Level.INFO, "JWT_JASPI WARNING JWT_ALLOWANCE IS MISSING SETTING TO {0} seconds", jwt_allowance);
                optionValues.put(JWT_ALLOWANCE, jwt_allowance.toString());
            } else {
                try {
                    jwt_allowance = Integer.parseInt(optionValues.get(JWT_ALLOWANCE));
                } catch (Exception e) {
                    LOG.log(Level.INFO, "JWT_JASPI WARNING JWT_ALLOWANCE IS NOT A NUMBER SETTING TO {0} seconds", jwt_allowance);
                    optionValues.put(JWT_ALLOWANCE, jwt_allowance.toString());
                }
            }

            //
            // - collect information from HTTP request headers
            //
            Map<String, String> headerMap = new HashMap<>();

            if (!cju.isBlank(optionValues.get(ROLES_HEADER))) {
                String[] roles_headers = optionValues.get(ROLES_HEADER).split(ROLES_LIST_SEPARATOR);
                for (String h : roles_headers) {
                    String hv = request.getHeader(h);
                    headerMap.put(h, (cju.isBlank(hv) ? "" : hv));
                    LOG.log(Level.INFO, "JWT_JASPI header:{0} value:{1}", new String[]{h, headerMap.get(h)});
                }
            }

            //
            // - sanity checks passed, proceed with the authorization logic
            //
            if (goon) {

                Jws<Claims> jws;
                try {
                    BufferedInputStream buffin = new BufferedInputStream(new FileInputStream(certFile));

                    CertificateFactory f = CertificateFactory.getInstance("X.509");
                    X509Certificate certificate = (X509Certificate) f.generateCertificate(buffin);
                    PublicKey pk = certificate.getPublicKey();

                    jws = Jwts.parserBuilder().setAllowedClockSkewSeconds(jwt_allowance).setSigningKey(pk).build()
                            .parseClaimsJws(token);

                    if (jws.getBody().containsKey("user_role_list")) {
                        String user_role_list = jws.getBody().get("user_role_list", String.class);
                        Gson gson = new Gson();
                        Map map = gson.fromJson(user_role_list, Map.class);
                        String userActive = (String) map.get("userStatus");
                        isUaserActive = userActive.equalsIgnoreCase("A");
                    }
                    if (isUaserActive) {
                        LOG.log(Level.INFO, "JWT_JASPI USER IS ACTIVE");
                    } else {
                        goon = false;
                        LOG.log(Level.INFO, "JWT_JASPI USER IS NOT ACTIVE");
                    }

                    if (goon) {

                        roles = new HashMap<>();
                        Set<String> roleSet = new HashSet<>();

//            Connection conn = null;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
                        String jsonDBItems = "";
                        // uncomment for pretty-print
//              Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        Gson gson = new Gson();

                        LOG.log(Level.INFO, "JWT_JASPI ABOUT TO INVOKE AUTHORITY_ITEM_SERVICE");
                        Map<String, ArrayList<Map<String, String>>> aisResponseData = new HashMap<>();
//            String aisResponse = this.invokeAuthorityItemService(jsonDBItems, headerMap, token);
                        String aisResponse = this.invokeAuthorityItemService(jsonDBItems, headerMap, token);
                        //
                        // - use the following mock data until we have real data
                        // 
                        // - comment out to use the response from the AuthorityItemService call
                        //
                        aisResponse = "{\n"
                                + "  \"authorizedItemModelList\": [\n"
                                + "    {\n"
                                + "      \"itemId\": \"BPMG-MUSTDAN\",\n"
                                + "      \"itemCompanyId\": \"GAR\",\n"
                                + "      \"itemType\": \"F\",\n"
                                + "      \"authorityInd\": \"Y\",\n"
                                + "      \"readInd\": \"Y\",\n"
                                + "      \"updateInd\": \"\",\n"
                                + "      \"deleteInd\": \"Y\",\n"
                                + "      \"listInd\": \"Y\",\n"
                                + "      \"singleLimitAmount\": 0,\n"
                                + "      \"totalLimitAmount\": 0\n"
                                + "    }\n"
                                + "  ]\n"
                                + "}";

                        aisResponseData = gson.fromJson(aisResponse, aisResponseData.getClass());
                        {
                            Gson gsonTest = new GsonBuilder().setPrettyPrinting().create();
                            String jsonResponse = gsonTest.toJson(aisResponseData);
                            LOG.log(Level.INFO, "JWT_JASPI PARSED AUTHORITY_ITEM_SERVICE RESPONSE IS:{0}", jsonResponse);
                        }

                        ArrayList<Map<String, String>> items = aisResponseData.get("authorizedItemModelList");
                        items.forEach(item -> {
                            // - add the role only if readInd=Y
                            String readInd = item.get("readInd");
                            if (readInd.equalsIgnoreCase("Y")) {
                                LOG.log(Level.INFO, "JWT_JASPI AUTHORITY_ITEM_SERVICE GOT itemId:{0}", item.get("itemId"));
                                roleSet.add(item.get("ItemId"));
                            } else {
                                LOG.log(Level.INFO, "JWT_JASPI AUTHORITY_ITEM_SERVICE readInd_NOT_Y itemId:[{0}] NOT ADDED", item.get("itemId"));
                            }
                        });

                        if (!roleSet.isEmpty()) {
                            LOG.log(Level.INFO, "JWT_JASPI roleSet IS:{0}", roleSet);
                            roles.put(xuser, roleSet);
                        } else {
                            LOG.log(Level.INFO, "JWT_JASPI roleSet IS EMPTY");
                            if (DENY_EMPTY_ROLESET) {
                                LOG.log(Level.INFO, "JWT_JASPI CALL IS REJECTED");
                                goon = false;
                            }
                        }
                    }
                    //
                    // ------------------------------------------------
                    //
                } catch (JwtException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                }

                //
                // - simulate a denied login attempt
                //
                if (xuser.equals("baduser")) {
                    goon = false;
                }

                //
                // -- allow login if all checks passed else deny 
                //
                if (goon) {

                    applyCallbacks(xuser, clientSubject);
                    result = AuthStatus.SUCCESS;

                } else {

                    // error, just request authentication using 401 and basic
                    response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"jaspi realm\"");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);

                }
            }

            return result;

        } catch (IOException | UnsupportedCallbackException e) {
            throw new AuthException(e.getMessage());
        }
    }

    private String invokeAuthorityItemService(String payload, Map<String, String> headerMap, String token) throws MalformedURLException, IOException {
        // -- dummy response for now
        return "dummy-response";
//        URL url = new URL(optionValues.get(AIS_URL));
//        HttpURLConnection con = (HttpURLConnection) url.openConnection();
//        con.setRequestMethod("POST");
//        con.setRequestProperty("Content-Type", "application/json; UTF-8");
//        con.setRequestProperty("Accept", "application/json");
//        con.setRequestProperty("Authorization", token);
//        headerMap.keySet().forEach(header -> {
//            con.setRequestProperty(header, headerMap.get(header));
//        });
//        con.setDoOutput(true);
//        try ( OutputStream os = con.getOutputStream()) {
//            byte[] input = payload.getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
//        String responseLine = "";
//        try ( BufferedReader br = new BufferedReader(
//                new InputStreamReader(con.getInputStream(), "UTF-8"))) {
//            StringBuilder response = new StringBuilder();
//            while ((responseLine = br.readLine()) != null) {
//                response.append(responseLine.trim());
//            }
//            LOG.log(Level.INFO, "JWT_JASPI AUTHORITY_ITEM_SERVICE RESPONSE STRING:{0}", response.toString());
//        }
//        con.disconnect();
//        return responseLine;
    }

    /**
     * @inheritDoc
     */
    @Override
    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
        return AuthStatus.SEND_SUCCESS;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
        if (subject != null) {
            subject.getPrincipals().clear();
            subject.getPrivateCredentials().clear();
            subject.getPublicCredentials().clear();
        }
    }
}
