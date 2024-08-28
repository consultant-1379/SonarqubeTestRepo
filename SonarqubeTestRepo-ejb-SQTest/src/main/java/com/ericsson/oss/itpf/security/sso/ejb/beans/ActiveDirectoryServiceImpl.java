/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.itpf.security.sso.ejb.beans;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.forgerock.opendj.ldap.*;
import org.forgerock.opendj.ldap.requests.ModifyRequest;
import org.forgerock.opendj.ldap.requests.Requests;
import org.forgerock.opendj.ldap.responses.Result;
import org.forgerock.opendj.ldap.responses.SearchResultEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.security.sso.ejb.services.external.ActiveDirectoryService;
import com.ericsson.oss.itpf.security.sso.ejb.sysinit.EniqWasPropertiesReader;

/**
 * REST service to update user's account with SSO Token as a password, in Active Directory
 *
 * @author ekarpia
 *
 */

public class ActiveDirectoryServiceImpl implements ActiveDirectoryService {

    private static final Logger logger = LoggerFactory.getLogger(ActiveDirectoryServiceImpl.class);

    private ConnectionFactory ldapAuthFactory;

    private final String baseDN;

    /**
     *
     */
    public ActiveDirectoryServiceImpl(final EniqWasPropertiesReader deploymentProperties) {

        //      should be exposed to JBOSS as attributes
        final String ldapHost = deploymentProperties.getValue("was_ad_hostname").toString(); //e.g. "10.45.192.254";
        final int ldapPort = 636; //default LDAPS Active Directory port

        //service user should be configurable during initial setup and integration on-site
        //between Active Directory and ENM

        //
        baseDN = "CN=Users," + deploymentProperties.getValue("was_ad_ldap_basedn").toString(); //e.g. "CN=Users,DC=athtem,DC=eei,DC=ericsson,DC=se";

        final String ldapUser = String.format("%s,%s", deploymentProperties.getValue("was_ad_ldap_username").toString(), baseDN);
        final String ldapPassword = deploymentProperties.getValue("was_ad_ldap_password").toString();

        LDAPConnectionFactory ldapFactory = null;

        try {

            logger.info("Creating LDAP Factory to connect to Active Directory.");

            ldapFactory = this.getLdapConnectionFactory(ldapHost, ldapPort);

            this.ldapAuthFactory = this.getAuthenticatedConnectionFactory(ldapFactory, ldapUser, ldapPassword);

        } catch (final GeneralSecurityException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ericsson.oss.itpf.security.sso.ejb.services.external.ActiveDirectoryService#sync(com.iplanet.sso.SSOToken, java.lang.String)
     */
    @Override
    public void sync(final String userName, final String ssoToken) throws IOException {

        //enmUserName and enmUserSSOToken
        //are obtained in JBOSS from Security Context
        final String enmUserName = userName;
        final String enmUserSSOToken = ssoToken;

        final String enmUserSSOTokenFormated = '"' + enmUserSSOToken + '"';

        Connection connection = null;

        try {

            connection = ldapAuthFactory.getConnection();

            //first we search, if such user exists in the Active Directory
            //if yes, try to perform the update

            final SearchResultEntry entry = connection.searchSingleEntry(baseDN, SearchScope.WHOLE_SUBTREE, "(CN=" + enmUserName + ")", "cn");

            if (entry == null) {
                logger.info("User is not present in Active Directory.");
                throw new IOException("User is not present in Active Directory.");

            }
            final DN bindDN = entry.getName();
            logger.info("Will update password for {}", bindDN.toString());

            final LinkedAttribute attribute = new LinkedAttribute("unicodePwd", enmUserSSOTokenFormated.getBytes("UTF-16LE"));
            final Modification ldapModification = new Modification(ModificationType.REPLACE, attribute);

            final LinkedAttribute attributeUAC = new LinkedAttribute("userAccountControl", "512");
            final Modification ldapModificationUAC = new Modification(ModificationType.REPLACE, attributeUAC);

            final ModifyRequest request = Requests.newModifyRequest(bindDN);
            request.addModification(ldapModification);
            request.addModification(ldapModificationUAC);

            final Result modificationResult = connection.modify(request);

            logger.info("Is password modified {}", modificationResult.isSuccess());

        } catch (final ErrorResultException | IOException e) {
            logger.error(e.getMessage(), e);
            throw new IOException(e);
        } finally {

            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     *
     * @param ldapHost
     * @param ldapPort
     * @return
     * @throws GeneralSecurityException
     */
    protected final LDAPConnectionFactory getLdapConnectionFactory(final String ldapHost, final int ldapPort) throws GeneralSecurityException {

        logger.info("getLdapConnectionFactory {} {}", ldapHost, ldapPort);
        return new LDAPConnectionFactory(ldapHost, ldapPort, getLDAPOptions());
    }

    /**
     *
     * Make authenticated connection factory from simple factory
     *
     * @param ldapConnectionFactory
     * @param ldapUser
     * @param ldapPassword
     * @return
     */
    protected final ConnectionFactory getAuthenticatedConnectionFactory(final LDAPConnectionFactory ldapConnectionFactory, final String ldapUser, final String ldapPassword) {

        return Connections.newAuthenticatedConnectionFactory(ldapConnectionFactory, Requests.newSimpleBindRequest(ldapUser, ldapPassword.toCharArray()));
    }

    /**
     * Configure TrustStore and TrustManager, disable StartTLS
     *
     * @return
     * @throws GeneralSecurityException
     */
    private LDAPOptions getLDAPOptions() throws GeneralSecurityException {
        final LDAPOptions ldapOptions = new LDAPOptions();

        // Create a trust manager that does not validate certificate chains
        final SSLContext sslContext = new SSLContextBuilder().setTrustManager(TrustManagers.trustAll()).getSSLContext();

        //ldapOptions.setTCPNIOTransport(transport);
        ldapOptions.setSSLContext(sslContext);
        ldapOptions.setUseStartTLS(false);
        ldapOptions.setTimeout(5, TimeUnit.SECONDS);

        return ldapOptions;
    }
}
