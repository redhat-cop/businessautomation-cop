package org.redhat.services.security;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.identity.IdentityProvider;

/**
 * This implementation mimics {@link JAASUserGroupCallbackImpl} that is used on JEE application servers.
 * This one is instead based on spring security and provides exact same features.
 *
 */
@Slf4j
public class SpringSecurityUserGroupCallback implements UserGroupCallback {

    private IdentityProvider identityProvider;

    public SpringSecurityUserGroupCallback(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    @Override
    public boolean existsUser(String userId) {
        log.info("existsUser: {}", userId);
        return true;
    }

    @Override
    public boolean existsGroup(String groupId) {
        log.info("existsGroup: {}", groupId);
        return true;
    }

    @Override
    public List<String> getGroupsForUser(String userId) {
        log.info("getGroupsForUser: user - {} role - {}", userId, identityProvider.getRoles());
        return identityProvider.getRoles();
    }

}