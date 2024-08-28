package com.ericsson.oss.itpf.security.sso.ejb.utils;

import javax.ejb.Singleton;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.NoSuchFileException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.forgerock.opendj.ldap.*;
import org.forgerock.opendj.ldap.requests.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.itpf.sdk.resources.Resources;
import com.ericsson.oss.itpf.security.sso.ejb.exceptions.PasswordExpirationException;

/**
 * Created by egicass on 05/10/2017
 */

@Singleton
@ApplicationScoped
public class OpenDSConfiguration {

    private static Logger LOGGER = LoggerFactory.getLogger(OpenDSConfiguration.class);

    private final static String GLOBAL_PROPERTIES_FILE = "/ericsson/tor/data/global.properties";
    private static final String LDAP_PORT_PROPERTY = "ldap_port_property";
    private static final String LDAP_ADMIN_CN_PROPERTY = "ldap_admin_cn_property";
    private static final String LDAP_PASSWORD_PROPERTY = "ldap_password_property";
    private static final String LDAP_INSTANCE_1_ALIAS = "ldap_instance_1_alias";
    private static final String LDAP_INSTANCE_2_ALIAS = "ldap_instance_2_alias";

    private static final String LDAP_CLOUD_SERVICE_NAME = "ldap_cloud_service_name";

    private final static boolean USE_START_TLS = false;
    private static final String LDAP_CONFIGURATION_INIT_EXCEPTION = "Initialization of OpenDSConfiguration failed";
    private static int MAX_SOCKET_SIZE = 100;
    private final static String CLOUD_PROPERTY = "DDC_ON_CLOUD";
    private final static String CONFIGURATION_PROPERTY = "configuration.java.properties";

    private List<String> ldapIpAddresses;
    private int ldapPort;
    private String ldapAdminCn;
    private char[] ldapPassword;
    private Properties global_properties = null;
    private ConnectionFactory factory=null;
    final List<ConnectionFactory> factories = new LinkedList<ConnectionFactory>();


    @PostConstruct
    public void init() {
        try {

            final ResourceBundle params = ResourceBundle.getBundle("com.ericsson.oss.itpf.security.sso.ejb.utils.openDS");
            LOGGER.info("Initializing OpenDSConfiguration");
            loadGlobalProperties();

            if (enmOnCloud()) {
                LOGGER.info("Cloud Environment detected");
                ldapIpAddresses = getAllInetAddresses(params.getString(LDAP_CLOUD_SERVICE_NAME).trim());

            } else {
                LOGGER.info("Physical Environment detected");
                ldapIpAddresses = Arrays.asList(readLdapIpAddress(params.getString(LDAP_INSTANCE_1_ALIAS).trim()),
                        readLdapIpAddress(params.getString(LDAP_INSTANCE_2_ALIAS).trim()));
            }

            ldapPort = Integer.parseInt(params.getString(LDAP_PORT_PROPERTY).trim());
            ldapAdminCn = params.getString(LDAP_ADMIN_CN_PROPERTY).trim();
            ldapPassword = params.getString(LDAP_PASSWORD_PROPERTY).trim().toCharArray();

            initializePool();
            LOGGER.info(getState());

        } catch (final Exception e) {
            LOGGER.error("IOException in method init()", e);
            LOGGER.error("LdapConfiguration: " + getState());
            throw new PasswordExpirationException(LDAP_CONFIGURATION_INIT_EXCEPTION, e);
        }
    }

    protected void initializePool() {

        LDAPConnectionFactory ldapConnectionFactory = null;

        try {

            final LDAPOptions ldapConnectionOptions = getLDAPOptions();
            for (final String ldapIpAddress : getLdapIpAddresses()) {
                try {
                    LOGGER.info("creating connection factory for instance {}",ldapIpAddress);
                    ldapConnectionFactory = new LDAPConnectionFactory(ldapIpAddress, getLdapPort(), ldapConnectionOptions);

                    if (ldapConnectionFactory != null) {
                        LOGGER.info("Succesfully created LDAPconnectionFactory for instance: " + ldapIpAddress + " and port: " + getLdapPort());

                        factories.add(Connections.newFixedConnectionPool(
                                Connections.newAuthenticatedConnectionFactory(Connections.newHeartBeatConnectionFactory(ldapConnectionFactory),
                                        Requests.newSimpleBindRequest(ldapAdminCn, ldapPassword)),
                                MAX_SOCKET_SIZE));

                    }
                } catch (final Exception e) {
                    LOGGER.warn("Unable to create LDAPconnectionFactory for instance: " + ldapIpAddress + " and port: " + getLdapPort(), e.getMessage());
                }
            }

            FailoverLoadBalancingAlgorithm algorithm = new FailoverLoadBalancingAlgorithm(factories);
            factory = Connections.newLoadBalancer(algorithm);

        } catch (final Exception e) {
            LOGGER.error("Unable to create LDAP connection pool: {},{} ", getState(), e.getMessage());
        }

    }

    public boolean enmOnCloud() {

        return ("true".equalsIgnoreCase(global_properties.getProperty(CLOUD_PROPERTY)));
    }

    public static String readLdapIpAddress(final String alias) {

        LOGGER.info("Getting ip address for alias {}",alias);
        String address = null;

        try {
            InetAddress inetaddress = InetAddress.getByName(alias);
            if(inetaddress!=null)
                address = inetaddress.getHostAddress();

        } catch (Exception e) {
            LOGGER.error("ERROR getting IP address for host {}: {}",alias, e.getMessage());
        }
        return address;
    }

    public Connection getLdapConnection() {
        LOGGER.info("Getting LDAP connection");
        Connection connection = null;

        try {
            connection = factory.getConnection();
        } catch (final ErrorResultException e) {
            LOGGER.error("[{}] Error getting connection {}", getClass().getCanonicalName());
        }

        return connection;
    }

    public LDAPOptions getLDAPOptions() throws GeneralSecurityException {
        final LDAPOptions ldapOptions = new LDAPOptions();

        ldapOptions.setUseStartTLS(USE_START_TLS);
        ldapOptions.setTimeout(2, TimeUnit.SECONDS);

        return ldapOptions;
    }

    /*
     * public static String decryptLdapPassword(String encryptedPassword) { return PasswordDecryptor.getPlainText(encryptedPassword, OPENDJ_PASSKEY);
     * }
     */

    public String getState() {
        return "LdapIpAddress=" + ldapIpAddresses + ", LdapPort=" + ldapPort + ", LdapAdminCn=" + ldapAdminCn;

    }

    /**
     * Loads properties from global properties file /ericsson/tor/data/global.properties. This default filename can be change by setting the system
     * property "datastore.java.properties".
     */
    private void loadGlobalProperties() {
        if (global_properties == null) {
            global_properties = new Properties();

            final String fileName = System.getProperty(CONFIGURATION_PROPERTY, GLOBAL_PROPERTIES_FILE);

            try {
                global_properties.load(getConfigurationAsStream(fileName));
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("ldap.configuration.propertiesLoaded: {}", fileName);
                }
            } catch (final Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("ldap.configuration.propertiesLoadError: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Returns configuration data as stream.
     *
     * @param fileName
     * @return
     * @throws NoSuchFileException
     */
    protected InputStream getConfigurationAsStream(final String fileName) throws NoSuchFileException {
        final Resource fileResource;

        fileResource = Resources.getFileSystemResource(fileName);

        if (!fileResource.exists()) {
            throw new NoSuchFileException(fileResource.getName());
        }

        return fileResource.getInputStream();
    }

    /**
     * Returns InetAddress object for given sso.
     *
     * @param serviceName
     * @return
     */
    protected List<String> getAllInetAddresses(final String serviceName) {

        final List<String> result = new ArrayList<String>();

        try {
            final InetAddress[] addrLdapCloud = InetAddress.getAllByName(serviceName);
            for (int i = 0; i < addrLdapCloud.length; i++) {
                result.add(addrLdapCloud[i].getHostAddress());
            }
        } catch (final UnknownHostException e) {
            LOGGER.error("[{}] ERROR:{} ", "getAllInetAddresses", e.getMessage());
            // return null;
        }

        LOGGER.debug("[{}] SSO adresses are {}", "getAllInetAddresses", result.toString());
        return result;
    }

    public List<String> getLdapIpAddresses() {
        return ldapIpAddresses;
    }

    public void setLdapIpAddresses(List<String> ldapIpAddresses) {
        ldapIpAddresses = ldapIpAddresses;
    }

    public int getLdapPort() {
        return ldapPort;
    }

    public void setLdapPort(int ldapPort) {
        ldapPort = ldapPort;
    }

    public String getLdapAdminCn() {
        return ldapAdminCn;
    }

    public void setLdapAdminCn(String ldapAdminCn) {
        ldapAdminCn = ldapAdminCn;
    }

    public char[] getLdapPassword() {
        return ldapPassword;
    }

    public void setLdapPassword(char[] ldapPassword) {
        ldapPassword = ldapPassword;
    }
}
