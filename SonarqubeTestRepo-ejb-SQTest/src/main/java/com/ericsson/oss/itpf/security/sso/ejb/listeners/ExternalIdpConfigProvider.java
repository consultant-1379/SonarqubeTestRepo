/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
//
package com.ericsson.oss.itpf.security.sso.ejb.listeners;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.config.annotation.ConfigurationChangeNotification;
import com.ericsson.oss.itpf.sdk.config.annotation.Configured;
import com.ericsson.oss.itpf.security.sso.ejb.constants.ExtIdpConstants;
import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Sdk;
import com.ericsson.oss.itpf.security.sso.ejb.services.OpenAMParamsManager;
import com.ericsson.oss.itpf.security.sso.ejb.utils.PasswordHelper;

@Startup
@Singleton
public class ExternalIdpConfigProvider {

    @Inject
    @Configured(propertyName = "authType")
    private String authType;

    @Inject
    @Configured(propertyName = "remoteAuthProfile")
    private String remoteAuthProfile;

    @Inject
    @Configured(propertyName = "baseDN")
    private String baseDN;

    @Inject
    @Configured(propertyName = "primaryServerAddress")
    private String primaryServerAddress;

    @Inject
    @Configured(propertyName = "secondaryServerAddress")
    private String secondaryServerAddress;

    @Inject
    @Configured(propertyName = "ldapConnectionMode")
    private String ldapConnectionMode;

    @Inject
    @Configured(propertyName = "userBindDNFormat")
    private String userBindDNFormat;

    @Inject
    @Configured(propertyName = "searchFilter")
    private String searchFilter;

    @Inject
    @Configured(propertyName = "searchScope")
    private String searchScope;

    @Inject
    @Configured(propertyName = "searchAttribute")
    private String searchAttribute;

    @Inject
    @Configured(propertyName = "bindDN")
    private String bindDN;

    @Inject
    @Configured(propertyName = "bindPassword")
    private String bindPassword;

    @Inject
    Logger logger;

    @Inject
    @Sdk
    OpenAMParamsManager extIdp;

    @Inject
    PasswordHelper pwdHelper;

    private String userSearchAttribute = "";
    private String userNamingAttribute = "";

    public void listenForAuthTypeChange(@Observes @ConfigurationChangeNotification(propertyName = "authType") final String actualAuthTypeValue) {
        logger.info("authType has changed: {}", actualAuthTypeValue);
        setAuthType(actualAuthTypeValue.trim());
    }

    public void listenForRemoteAuthProfileChange(@Observes @ConfigurationChangeNotification(propertyName = "remoteAuthProfile") final String actualRemoteAuthProfileValue) {
        logger.info("remoteAuthProfile has changed: {}", actualRemoteAuthProfileValue);
        setRemoteAuthProfile(actualRemoteAuthProfileValue.trim());
    }

    public void listenForBaseDNChange(@Observes @ConfigurationChangeNotification(propertyName = "baseDN") final String actualBaseDNValue) {
        logger.info("baseDN has changed: {}", actualBaseDNValue);
        setBaseDN(actualBaseDNValue.trim());
    }

    public void listenForPrimaryServerAddressChange(@Observes @ConfigurationChangeNotification(propertyName = "primaryServerAddress") final String actualPrimaryServerAddressValue) {
        logger.info("primaryServerAddress has changed: {}", actualPrimaryServerAddressValue);
        setPrimaryServerAddress(actualPrimaryServerAddressValue.trim());
    }

    public void listenForSecondaryServerAddressChange(@Observes @ConfigurationChangeNotification(propertyName = "secondaryServerAddress") final String actualSecondaryServerAddressValue) {
        logger.info("secondaryServerAddress has changed: {}", actualSecondaryServerAddressValue);
        setSecondaryServerAddress(actualSecondaryServerAddressValue.trim());
    }

    public void listenForLdapConnectionModeChange(@Observes @ConfigurationChangeNotification(propertyName = "ldapConnectionMode") final String actualLdapConnectionModeValue) {
        logger.info("ldapConnectionMode has changed: {}", actualLdapConnectionModeValue);
        setLdapConnectionMode(actualLdapConnectionModeValue.trim());
    }

    public void listenForUserBindDNFormatChange(@Observes @ConfigurationChangeNotification(propertyName = "userBindDNFormat") final String actualuserBindDNFormatValue) {
        logger.info("userBindDNFormatValue has changed: {}", actualuserBindDNFormatValue);
        setUserBindDNFormat(actualuserBindDNFormatValue.trim());
    }

    public void listenForBindDNChange(@Observes @ConfigurationChangeNotification(propertyName = "bindDN") final String actualBindDN) {
        logger.info("BindDN has changed: {}", actualBindDN);
        setBindDN(actualBindDN.trim());
    }

    public void listenForBindPasswordChange(@Observes @ConfigurationChangeNotification(propertyName = "bindPassword") final String actualBindPassword) {
        logger.info("BindPassword has changed");
        setBindPassword(actualBindPassword.trim());
    }

    public void listenForSearchFilterChange(@Observes @ConfigurationChangeNotification(propertyName = "searchFilter") final String actualSearchFilterValue) {
        logger.info("searchFilter has changed: {}", actualSearchFilterValue);
        setSearchFilter(actualSearchFilterValue.trim());
    }

    public void listenForSearchScopeChange(@Observes @ConfigurationChangeNotification(propertyName = "searchScope") final String actualSearchScopeValue) {
        logger.info("searchScope has changed: {}", actualSearchScopeValue);
        setSearchScope(actualSearchScopeValue.trim());
    }

    public void listenForSearchAttributeChange(@Observes @ConfigurationChangeNotification(propertyName = "searchAttribute") final String actualSearchAttributeValue) {
        logger.info("searchAttribute has changed: {}", actualSearchAttributeValue);
        setSearchAttribute(actualSearchAttributeValue.trim());
    }

    public void setAuthType(final String authType) {
        if (!ExtIdpConstants.LOCAL_AUTHN.equals(authType) && !ExtIdpConstants.REMOTE_AUTHN.equals(authType)) {
            logger.error("{} ERROR: wrong PIB param {}", this.getClass().getName(), authType);
        } else {
            this.authType = authType;
            if (ExtIdpConstants.LOCAL_AUTHN.equals(authType)) {
                extIdp.setOrganizationServiceAttribute(ExtIdpConstants.AM_AUTH_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_ORG_CONFIG_AM_AUTH,
                        ExtIdpConstants.LOCAL_AUTHN_CHAIN);
            } else {
                if (ExtIdpConstants.REMOTE_AUTHN.equals(authType)) {
                    extIdp.setOrganizationServiceAttribute(ExtIdpConstants.AM_AUTH_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_ORG_CONFIG_AM_AUTH,
                            ExtIdpConstants.REMOTE_AUTH_CHAIN);
                }
            }
        }
    }

    public void setRemoteAuthProfile(final String remoteAuthProfile) {
        if (!ExtIdpConstants.REMOTE_AUTH_PROFILE__NOSEARCH.equals(remoteAuthProfile)
                && !ExtIdpConstants.REMOTE_AUTH_PROFILE__STANDARD.equals(remoteAuthProfile)) {
            logger.error("{} ERROR: wrong PIB param {}", this.getClass().getName(), remoteAuthProfile);
        } else {
            this.remoteAuthProfile = remoteAuthProfile;
            extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_CUSTOM_LDAP_REMOTE_AUTH_PROFILE,
                    remoteAuthProfile);
        }
    }

    public void setBaseDN(final String baseDN) {
        this.baseDN = baseDN;
        extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_CUSTOM_LDAP_BASE_DN, baseDN);
    }

    public void setPrimaryServerAddress(final String primaryServerAddress) {
        this.primaryServerAddress = primaryServerAddress;
        extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_CUSTOM_LDAP_SERVER,
                primaryServerAddress);
    }

    public void setSecondaryServerAddress(final String secondaryServerAddress) {
        this.secondaryServerAddress = secondaryServerAddress;
        extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_CUSTOM_LDAP_SERVER2,
                secondaryServerAddress);
    }

    public void setLdapConnectionMode(final String ldapConnectionMode) {
        this.ldapConnectionMode = ldapConnectionMode;
        extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AUTH_CUSTOM_LDAP_CONNECTIONMODE,
                ldapConnectionMode);
    }

    public void setUserBindDNFormat(final String userBindDNFormat) {

        final Pattern pattern = Pattern.compile("^(\\w+)=");
        final Matcher matcher = pattern.matcher(userBindDNFormat);
        if (matcher.find()) {
            final String uid = matcher.group(1);
            setUserNamingAttribute(uid);
            setUserSearchAttribute(uid);
        }
        extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_CUSTOM_LDAP_USER_BIND_DN_FORMAT,
                userBindDNFormat);
    }

    public void setSearchFilter(final String searchFilter) {
        this.searchFilter = searchFilter;
        extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_CUSTOM_LDAP_SEARCH_FILTER, searchFilter);
    }

    public void setSearchScope(final String searchScope) {
        this.searchScope = searchScope;
        extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_CUSTOM_LDAP_SEARCH_SCOPE, searchScope);
    }

    public void setSearchAttribute(final String searchAttribute) {
        this.searchAttribute = searchAttribute;
        //To handle
    }

    public void setBindDN(final String bindDN) {
        this.bindDN = bindDN;
        extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_CUSTOM_LDAP_BIND_DN, bindDN);
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
        String clearPwd = "";

        bindPassword = convertValue(bindPassword);
        if (!bindPassword.equals("")) {
            clearPwd = pwdHelper.decryptDecode(bindPassword);
        }
        extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_CUSTOM_LDAP_BIND_PASSWORD, clearPwd);
    }

    public void setUserSearchAttribute(final String userSearchAttribute) {
        this.userSearchAttribute = userSearchAttribute;
        extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_CUSTOM_LDAP_USER_SEARCH_ATTRIBUTES,
                userSearchAttribute);
    }

    public void setUserNamingAttribute(final String userNamingAttribute) {
        this.userNamingAttribute = userNamingAttribute;
        extIdp.setOrganizationServiceAttribute(ExtIdpConstants.CUSTOM_LDAP_SERVICE_NAME, ExtIdpConstants.IPLANET_AM_AUTH_CUSTOM_LDAP_USER_NAMING_ATTRIBUTE,
                userNamingAttribute);
    }

    //getters
    public String getAuthType() {
        return authType;
    }

    public String getRemoteAuthProfile() {
        return remoteAuthProfile;
    }

    public String getBaseDN() {
        return baseDN;
    }

    public String getPrimaryServerAddress() {
        return primaryServerAddress;
    }

    public String getSecondaryServerAddress() {
        return secondaryServerAddress;
    }

    public String getLdapConnectionMode() {
        return ldapConnectionMode;
    }

    public String getUserBindDNFormat() {
        return userBindDNFormat;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public String getSearchScope() {
        return searchScope;
    }

    public String getSearchAttribute() {
        return searchAttribute;
    }

    public String getBindDN() {
        return bindDN;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    private String convertValue(final String value) {
        return (value != null && !ExtIdpConstants.NULL.equals(value)) ? value : "";
    }
}
