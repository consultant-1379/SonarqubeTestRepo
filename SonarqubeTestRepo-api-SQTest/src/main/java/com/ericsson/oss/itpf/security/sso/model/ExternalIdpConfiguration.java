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
package com.ericsson.oss.itpf.security.sso.model;

import com.ericsson.oss.itpf.modeling.annotation.DefaultValue;
import com.ericsson.oss.itpf.modeling.annotation.EModel;
import com.ericsson.oss.itpf.modeling.annotation.configparam.ConfParamDefinition;
import com.ericsson.oss.itpf.modeling.annotation.configparam.ConfParamDefinitions;
import com.ericsson.oss.itpf.modeling.annotation.configparam.Scope;

@EModel(namespace = "identity-management", description = "Configuration for External Idp parameters")
@ConfParamDefinitions
public class ExternalIdpConfiguration {

    @ConfParamDefinition(description = "Authentication Type", scope = Scope.GLOBAL)
    @DefaultValue("LOCAL")
    public String authType;

    @ConfParamDefinition(description = "Remote Authentication Profile", scope = Scope.GLOBAL)
    @DefaultValue("STANDARD")
    public String remoteAuthProfile;

    @ConfParamDefinition(description = "Base DN", scope = Scope.GLOBAL)
    @DefaultValue("")
    public String baseDN;

    @ConfParamDefinition(description = "Primary server address list (server:port | server:port ..)", scope = Scope.GLOBAL)
    @DefaultValue("")
    public String primaryServerAddress;

    @ConfParamDefinition(description = "Secondary server address list (server:port | server:port ..)", scope = Scope.GLOBAL)
    @DefaultValue("")
    public String secondaryServerAddress;

    @ConfParamDefinition(description = "LDAP connection mode", scope = Scope.GLOBAL)
    @DefaultValue("LDAPS")
    public String ldapConnectionMode;

    @ConfParamDefinition(description = "User Bind DN Format", scope = Scope.GLOBAL)
    @DefaultValue("")
    public String userBindDNFormat;

    @ConfParamDefinition(description = "LDAP search filter", scope = Scope.GLOBAL)
    @DefaultValue("")
    public String searchFilter;

    @ConfParamDefinition(description = "Scope of LDAP search", scope = Scope.GLOBAL)
    @DefaultValue("SUBTREE")
    public String searchScope;

    @ConfParamDefinition(description = "Attribute to search for in LDAP search", scope = Scope.GLOBAL)
    @DefaultValue("")
    public String searchAttribute;

    @ConfParamDefinition(description = "Search control", scope = Scope.GLOBAL)
    @DefaultValue("")
    public String searchControls;

    @ConfParamDefinition(description = "LDAP bind mode", scope = Scope.GLOBAL)
    @DefaultValue("PROXY")
    public String bindMode;

    @ConfParamDefinition(description = "LDAP server admin user", scope = Scope.GLOBAL)
    @DefaultValue("")
    public String bindDN;

    @ConfParamDefinition(description = "LDAP server admin password", scope = Scope.GLOBAL)
    @DefaultValue("")
    public String bindPassword;

}
