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
package com.ericsson.oss.itpf.security.sso.ejb.sysinit;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.security.sso.ejb.sysinit.external.ExternalApplicationConfigReader;

/**
 * Configuration reader for ENIQ-S WAS (Windows Application Server) Integration
 *
 * @author ekarpia
 *
 */

public class EniqWasPropertiesReader extends ExternalApplicationConfigReader {

    /**
     *
     */
    private static final long serialVersionUID = 554993005255847397L;

    private static final String DEPLOYMENT_CONFIG = "/ericsson/tor/data/eniq_was_integration/deployment.properties";

    @Inject
    private Logger logger;

    /**
     * @param deploymentConfigLocation
     * @throws IOException
     */
    public EniqWasPropertiesReader() throws IOException {
        super(DEPLOYMENT_CONFIG);
    }

    @PostConstruct
    void onServiceStart() {
        logger.info("Starting {}", this.getClass().getName());
    }

    @PreDestroy
    void onServiceStop() {
        logger.info("Stopping {}", this.getClass().getName());
    }

}
