package com.ericsson.oss.itpf.security.sso.ejb.beans;


import com.ericsson.oss.itpf.sdk.security.accesscontrol.EPredefinedRole;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.annotation.Authorize;
import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Rest;
import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Sdk;
import com.ericsson.oss.itpf.security.sso.ejb.services.AuthService;
import com.ericsson.oss.itpf.security.sso.ejb.services.SessionManagementService;
import com.ericsson.oss.itpf.security.sso.ejb.services.UserManagementService;
import com.ericsson.oss.itpf.security.sso.ejb.sysinit.ServiceBootstrap;
import com.ericsson.oss.itpf.security.sso.ejb.utils.OpenDSConfiguration;
import com.iplanet.sso.SSOToken;
import com.iplanet.sso.SSOTokenManager;
import org.forgerock.opendj.ldap.*;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;
import org.forgerock.opendj.ldif.ConnectionEntryReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by ejakgub on 12/7/15.
 */


@Stateless
@Sdk
public class UserManagerSdk implements UserManagementService {

    private static Logger logger = LoggerFactory.getLogger(UserManagerSdk.class);

    private static final String SESSION_MANAGEMENT = "session_management";
    private static final String READ_ACTION = "read";
    private static final String DELETE_ACTION = "delete";
    private static final String LDAP_SESSION_BASE_DN_PROPERTY = "ou=famrecords,ou=openam-session,ou=tokens,dc=opensso,dc=java,dc=net";
    private static final String LDAP_SESSION_FILTER = "(coreTokenType=SESSION)";
    private static final String LDAP_USER_SUFFIX = ",ou=user,dc=opensso,dc=java,dc=net";

    @Inject
    @Rest
    private AuthService authService;

    @Inject
    private OpenDSConfiguration openDSConfiguration;

    @Override
    @Authorize(resource = SESSION_MANAGEMENT, action = READ_ACTION, role = {EPredefinedRole.SECURITYADMIN})
    public Map<String, ?> getActiveUsers() {

        Map<String, Integer> userIDs = new HashMap<>();
        try {

            logger.info("[{}]: Getting sessions for all users",getClass().getCanonicalName());
            ConnectionEntryReader reader = null;
            Connection connection =null;
             try {

                 connection = openDSConfiguration.getLdapConnection();
                 reader = connection.search(LDAP_SESSION_BASE_DN_PROPERTY, SearchScope.WHOLE_SUBTREE, LDAP_SESSION_FILTER, "coreTokenUserId");


                 while (reader.hasNext()) {
                    if (reader.isEntry()) {
                        SearchResultEntry entry = reader.readEntry();
                        Attribute attr = entry.getAttribute("coreTokenUserId");
                        Pattern pattern = Pattern.compile("id=(\\w+)");
                        Matcher matcher = pattern.matcher(attr.firstValueAsString());
                        if (matcher.find()) {
                            String key = matcher.group(1);
                            if (!userIDs.containsKey(key)) {
                                userIDs.put(key, 1);
                            } else {
                                int val = ((Integer) userIDs.get(key));
                                userIDs.put(key, ++val);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("[{}] ErrorResultException: {}",getClass().getCanonicalName() ,e.getMessage());
            } finally {
                reader.close();
                logger.debug("[{}]: Releasing connection", getClass().getCanonicalName());
                if (connection != null) {
                    connection.close();
                    logger.debug("[{}]: Connection released",getClass().getCanonicalName());
                } else
                    logger.debug("[{}]: Unable to clores connection.Connection is null",getClass().getCanonicalName());
            }

        } catch (Exception e) {
            logger.error("[{}] ERROR getting Active sessions: {}",getClass().getCanonicalName(),e.getMessage());
        }
        return userIDs;
    }

    @Authorize(resource = SESSION_MANAGEMENT, action = DELETE_ACTION, role = {EPredefinedRole.SECURITYADMIN})
    public void deactivateUser(String userId) {

        SSOToken ssoToken = null;
        logger.info("[{}]: Deactivating user: {}",getClass().getCanonicalName(),userId);
        ConnectionEntryReader reader = null;
        StringBuffer ldapFilter = new StringBuffer("(coreTokenUserId=id=");
        ldapFilter.append(userId.toLowerCase());
        ldapFilter.append(LDAP_USER_SUFFIX);
        ldapFilter.append(")");
        Connection connection=null;

        try {
            ssoToken = authService.login();
            connection = openDSConfiguration.getLdapConnection();
            reader = connection.search(LDAP_SESSION_BASE_DN_PROPERTY, SearchScope.WHOLE_SUBTREE, ldapFilter.toString(),"coreTokenString02");


            while (reader.hasNext()) {
                if (reader.isEntry()) {
                    SearchResultEntry entry = reader.readEntry();
                    Attribute tokenId = entry.getAttribute("coreTokenString02");
                    SSOTokenManager ssoTokenManager = SSOTokenManager.getInstance();
                    SSOToken userToken = ssoTokenManager.createSSOToken(tokenId.firstValueAsString());

                    logger.info("Attempting to destroy session {}:", tokenId.firstValueAsString());
                    ssoTokenManager.destroyToken(ssoToken, userToken);

                }
            }
        } catch (Exception e) {
            logger.error("[{}] ErrorResultException: {}",getClass().getCanonicalName(), e.getMessage());
        } finally {
            reader.close();
            logger.debug("[{}]: Releasing connection", getClass().getCanonicalName());
            if (connection != null) {
                connection.close();
                logger.debug("[{}]: Connection released",getClass().getCanonicalName());
            } else
                logger.debug("[{}]: Connection is null",getClass().getCanonicalName());
            authService.logout(ssoToken);

        }

    }



}