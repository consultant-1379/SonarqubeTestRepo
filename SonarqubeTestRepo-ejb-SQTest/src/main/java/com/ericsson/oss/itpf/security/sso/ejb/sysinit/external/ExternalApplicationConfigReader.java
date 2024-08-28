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
package com.ericsson.oss.itpf.security.sso.ejb.sysinit.external;

import java.io.*;
import java.util.Properties;

import javax.inject.Inject;

import org.slf4j.Logger;

/**
 *
 * Default implementation for external configuration readers
 *
 * @author ekarpia
 */
public class ExternalApplicationConfigReader implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6920381180411469632L;

    @Inject
    private Logger logger;

    private final String deploymentConfigLocation;

    private final Properties props;

    /**
     * Prevent implementations from creating empty deployment reader
     */
    private ExternalApplicationConfigReader() {
        this.deploymentConfigLocation = null;
        this.props = new Properties();
    }

    /**
     * Load all global properties
     *
     * @throws IOException
     */
    public ExternalApplicationConfigReader(final String deploymentConfigLocation) throws IOException {

        this.deploymentConfigLocation = deploymentConfigLocation;
        this.props = new Properties();
        this.getDeploymentProperties();

    }

    /**
     * @throws IOException
     *
     */
    protected void getDeploymentProperties() throws IOException {

        if (new File(this.deploymentConfigLocation).exists()) {
            try (final BufferedReader reader = getBufferedReader();) {

                this.props.load(reader);
            } catch (final IOException e) {
                logger.error(e.getMessage(), e);
                throw new IOException(e);
            }
        }
    }

    /**
     *
     * @param property
     * @return
     */
    public String getValue(final String property) {

        if (this.props == null) {
            return null;
        }

        if (this.props.containsKey(property)) {
            return this.props.get(property).toString();
        } else {
            return null;
        }

    }

    private BufferedReader getBufferedReader() throws FileNotFoundException {
        return new BufferedReader(new FileReader(this.deploymentConfigLocation));
    }
}
