package com.ericsson.oss.itpf.security.sso.ejb.constants;

public class ExtIdpConstants {

    //CustomLDAP Service attributes
    public static final String CUSTOM_LDAP_SERVICE_NAME = "iPlanetAMAuthCustomLDAPService";
    public static final String IPLANET_AM_AUTH_CUSTOM_LDAP_BASE_DN = "iplanet-am-auth-customldap-base-dn";
    public static final String IPLANET_AM_AUTH_CUSTOM_LDAP_SERVER = "iplanet-am-auth-customldap-server";
    public static final String IPLANET_AM_AUTH_CUSTOM_LDAP_SERVER2 = "iplanet-am-auth-customldap-server2";
    public static final String IPLANET_AUTH_CUSTOM_LDAP_CONNECTIONMODE = "openam-auth-customldap-connection-mode";
    public static final String IPLANET_AM_AUTH_CUSTOM_LDAP_BIND_DN = "iplanet-am-auth-customldap-bind-dn";
    public static final String IPLANET_AM_AUTH_CUSTOM_LDAP_BIND_PASSWORD = "iplanet-am-auth-customldap-bind-passwd";
    public static final String IPLANET_AM_AUTH_CUSTOM_LDAP_SEARCH_FILTER = "iplanet-am-auth-customldap-search-filter";
    public static final String IPLANET_AM_AUTH_CUSTOM_LDAP_SEARCH_SCOPE = "iplanet-am-auth-customldap-search-scope";
    public static final String IPLANET_AM_AUTH_CUSTOM_LDAP_USER_SEARCH_ATTRIBUTES = "iplanet-am-auth-customldap-user-search-attributes";
    public static final String IPLANET_AM_AUTH_CUSTOM_LDAP_USER_NAMING_ATTRIBUTE = "iplanet-am-auth-customldap-user-naming-attribute";
    public static final String IPLANET_AM_AUTH_CUSTOM_LDAP_USER_BIND_DN_FORMAT = "iplanet-am-auth-customldap-user-bind-dn-format";
    public static final String IPLANET_AM_AUTH_CUSTOM_LDAP_REMOTE_AUTH_PROFILE = "iplanet-am-auth-customldap-remote-auth-profile";

    //AM Auth Service attributes
    public static final String AM_AUTH_SERVICE_NAME = "iPlanetAMAuthService";
    public static final String IPLANET_AM_AUTH_ORG_CONFIG_AM_AUTH = "iplanet-am-auth-org-config";

    //PIB values for Ext Idp settings
    public static final String REMOTE_AUTH_PROFILE__NOSEARCH = "NOSEARCH";
    public static final String REMOTE_AUTH_PROFILE__STANDARD = "STANDARD";

    public static final String LOCAL_AUTHN = "LOCAL";
    public static final String REMOTE_AUTHN = "REMOTEAUTHN";
    public static final String NULL = "NULL";

    //OpenAM chains
    public static final String LOCAL_AUTHN_CHAIN = "ldapService";
    public static final String REMOTE_AUTH_CHAIN = "EnmExtIdp";

    /*public static final String REALM_NAME = "/";
    public static final String SERVICE_VERSION = "1.0";*/
}
