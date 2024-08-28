package com.ericsson.oss.itpf.security.sso.model;


import com.ericsson.oss.itpf.modeling.annotation.DefaultValue;
import com.ericsson.oss.itpf.modeling.annotation.EModel;
import com.ericsson.oss.itpf.modeling.annotation.configparam.ConfParamDefinition;
import com.ericsson.oss.itpf.modeling.annotation.configparam.ConfParamDefinitions;
import com.ericsson.oss.itpf.modeling.annotation.configparam.Scope;
import com.ericsson.oss.itpf.modeling.annotation.constraints.NotNull;

@EModel(namespace = "identity-management", description = "SSO session limits Configuration")
@ConfParamDefinitions
public class SessionLimitConfiguration {

    @NotNull
    @ConfParamDefinition(scope = Scope.GLOBAL, description = "Enable session contraint")
    @DefaultValue("false")
    public Boolean enableSessionConstraint;


}
