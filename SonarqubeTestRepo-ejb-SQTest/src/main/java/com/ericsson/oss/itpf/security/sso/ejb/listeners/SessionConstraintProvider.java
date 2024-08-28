package com.ericsson.oss.itpf.security.sso.ejb.listeners;


import com.ericsson.oss.itpf.sdk.config.annotation.ConfigurationChangeNotification;
import com.ericsson.oss.itpf.sdk.config.annotation.Configured;
import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Sdk;
import com.ericsson.oss.itpf.security.sso.ejb.services.OpenAMParamsManager;
import org.slf4j.Logger;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@Startup
@Singleton
public class SessionConstraintProvider {

    public static final String SERVICE = "iPlanetAMSessionService";
    public static final String CONSTRAINT_PARAM = "iplanet-am-session-enable-session-constraint";


    @Inject
    Logger logger;

    @Inject
    @Sdk
    OpenAMParamsManager openAMParamManager;

    @Inject
    @Configured(propertyName = "enableSessionConstraint")
    private Boolean enableSessionConstraint;


    public void listenForEnableSessionConstraintChange(@Observes @ConfigurationChangeNotification(propertyName = "enableSessionConstraint") final Boolean actualEnableSessionConstraint) {
        logger.info("enableSessionConstraint has changed: {}", actualEnableSessionConstraint);
        String value = "OFF";

        if(actualEnableSessionConstraint==true)
            value="ON";

         openAMParamManager.setGlobalServiceAttribute(SERVICE,CONSTRAINT_PARAM,value);
    }


}
