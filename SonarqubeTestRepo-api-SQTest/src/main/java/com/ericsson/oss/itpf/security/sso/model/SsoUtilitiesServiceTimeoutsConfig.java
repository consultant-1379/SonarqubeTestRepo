/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
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
import com.ericsson.oss.itpf.modeling.annotation.configparam.*;
import com.ericsson.oss.itpf.modeling.annotation.constraints.NotNull;

@EModel(namespace = "SsoUtilitiesServiceTimeoutsConfig", description = "SSO timeouts Configuration")
@ConfParamDefinitions
public class SsoUtilitiesServiceTimeoutsConfig {

    @NotNull
    @ConfParamDefinition(scope = Scope.GLOBAL, description = "SSO max session timeout")
    @DefaultValue("600")
    public Integer maxSessionTimeout;

    @NotNull
    @ConfParamDefinition(scope = Scope.GLOBAL, description = "SSO idle session timeout")
    @DefaultValue("60")
    public Integer idleSessionTimeout;

    @ConfParamDefinition(scope = Scope.GLOBAL, description = "SSO timeouts timestamp")
    public Long sessionConfigurationTimestamp;

}
