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
package com.ericsson.oss.itpf.security.sso.ejb.listeners;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.config.annotation.ConfigurationChangeNotification;
import com.ericsson.oss.itpf.sdk.config.annotation.Configured;

@ApplicationScoped
public class SsoConfigProvider {

    @Inject
    @Configured(propertyName = "maxSessionTimeout")
    private int maxSessionTimeout;

    @Inject
    @Configured(propertyName = "idleSessionTimeout")
    private int idleSessionTimeout;

    @Inject
    @Configured(propertyName = "sessionConfigurationTimestamp")
    private long sessionConfigurationTimestamp;

    void listenForMaxSessionTimeoutChanges(
            @Observes @ConfigurationChangeNotification(propertyName = "maxSessionTimeout") int actualMaxSessionTimeoutValue) {
        this.maxSessionTimeout = actualMaxSessionTimeoutValue;
    }

    void listenForIdleSessionTimeoutChanges(
            @Observes @ConfigurationChangeNotification(propertyName = "idleSessionTimeout") int actualIdleSessionTimeoutValue) {
        this.idleSessionTimeout = actualIdleSessionTimeoutValue;
    }

    void listenForSessionConfigurationTimestampChanges(
            @Observes @ConfigurationChangeNotification(propertyName = "sessionConfigurationTimestamp") long actualSessionConfigurationTimestampValue) {
        this.sessionConfigurationTimestamp = actualSessionConfigurationTimestampValue;
    }

    public int getMaxSessionTimeout() {
        return maxSessionTimeout;
    }

    public int getIdleSessionTimeout() {
        return idleSessionTimeout;
    }

    public long getSessionConfigurationTimestamp() {
        return sessionConfigurationTimestamp;
    }

}
