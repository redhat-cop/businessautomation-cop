package org.redhat.gss.extension;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.common.rest.RestEasy960Util;

public class RESTUtils {
	// stripped version of:
	// https://github.com/kiegroup/droolsjbpm-integration/blob/7.11.x/kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-common/src/main/java/org/kie/server/remote/rest/common/util/RestUtils.java

	public static String getContentType(HttpHeaders headers) {
		// default to application/xml
		String contentType = MediaType.APPLICATION_XML_TYPE.toString();
		// check variant that is based on accept header important in case of GET as then
		// Content-Type might not be given
		Variant v = RestEasy960Util.getVariant(headers);
		if (v != null) {
			// set the default to selected variant
			contentType = v.getMediaType().toString();
		}
		// now look for actual Content-Type header
		List<String> contentTypeHeader = headers.getRequestHeader(HttpHeaders.CONTENT_TYPE);
		if (contentTypeHeader != null && !contentTypeHeader.isEmpty() && contentTypeHeader.get(0) != null) {
			contentType = contentTypeHeader.get(0);
		}
		List<String> kieContentTypeHeader = headers.getRequestHeader(KieServerConstants.KIE_CONTENT_TYPE_HEADER);
		if (kieContentTypeHeader != null && !kieContentTypeHeader.isEmpty()) {
			contentType = kieContentTypeHeader.get(0);
		}

		return contentType;
	}

	public static Response createResponse(Object responseObj, Variant v, javax.ws.rs.core.Response.Status status) {
		Response.ResponseBuilder responseBuilder = null;
		if (status != null) {
			responseBuilder = Response.status(status).entity(responseObj).variant(v);
		} else {
			responseBuilder = Response.ok(responseObj, v);
		}
		return responseBuilder.build();
	}

	public static Variant getVariant(HttpHeaders headers) {
		Variant v = RestEasy960Util.getVariant(headers);
		if (v == null) {
			v = Variant.mediaTypes(getMediaType(headers)).add().build().get(0);
		}
		return v;
	}

	public static MediaType getMediaType(HttpHeaders httpHeaders) {
		String contentType = getContentType(httpHeaders);
		try {
			return MediaType.valueOf(contentType);
		} catch (IllegalArgumentException e) {
			MarshallingFormat format = MarshallingFormat.fromType(contentType);
			switch (format) {
			case JAXB:
				return MediaType.APPLICATION_XML_TYPE;

			case XSTREAM:
				return MediaType.APPLICATION_XML_TYPE;

			case JSON:
				return MediaType.APPLICATION_JSON_TYPE;

			default:
				return MediaType.APPLICATION_XML_TYPE;
			}
		}
	}
}
