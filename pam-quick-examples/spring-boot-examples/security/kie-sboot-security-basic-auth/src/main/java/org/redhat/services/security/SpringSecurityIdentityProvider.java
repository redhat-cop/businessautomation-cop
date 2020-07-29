package org.redhat.services.security;


import lombok.extern.slf4j.Slf4j;
import org.kie.internal.identity.IdentityProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class SpringSecurityIdentityProvider implements IdentityProvider {

    public String getName() {

        log.info("Spring Security getName: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        log.info("Spring Security authorities: {}", Arrays.asList(SecurityContextHolder.getContext().getAuthentication().getAuthorities()));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "unknown";
    }

    public List<String> getRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            List<String> roles = new ArrayList<String>();

            for (GrantedAuthority ga : auth.getAuthorities()) {
                String roleName = ga.getAuthority();
                if (roleName.startsWith("ROLE_")) {
                    roleName = roleName.replaceFirst("ROLE_", "");
                }
                roles.add(roleName);
            }

            log.info("Spring Security roles: {}", Arrays.asList(auth.getAuthorities()));
            return roles;
        }

        return Collections.emptyList();
    }

    public boolean hasRole(String role) {
        log.info("Spring Security hasRole: {}", role);
        return false;
    }
}