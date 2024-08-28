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
package com.ericsson.oss.itpf.security.sso.ejb.services.external;

import com.ericsson.oss.itpf.security.sso.ejb.sysinit.external.ExternalApplicationConfigReader;

import org.apache.http.HttpHost;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author ekarpia
 *
 */
public class ExternalWebAppProperties {

    /**
     *
     * @author ekarpia
     *
     */
    public enum AppType {
        BO, NETAN, SONOM
    }

    private final String hostname;
    private final String protocol;
    private final int port;

    private final HttpHost appHost;

    private final String url;

    public ExternalWebAppProperties(final AppType appType, final ExternalApplicationConfigReader configReader, final String url) {

        final String remoteHostname = configReader.getValue(appType.name().toLowerCase() + "_hostname");
        final String remoteProtocol = configReader.getValue(appType.name().toLowerCase() + "_protocol");
        final String remotePort = configReader.getValue(appType.name().toLowerCase() + "_port");

        this.hostname = remoteHostname != null ? remoteHostname : "localhost";
        this.protocol = remoteProtocol != null ? remoteProtocol : "http";
        this.port = Integer.valueOf(remotePort != null ? remotePort : "80");
        this.appHost = new HttpHost(this.hostname, this.port, this.protocol);
        this.url = url;
    }

    public URI getURI() throws URISyntaxException {
        return new URI(appHost.toURI() + url);
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the appHost
     */
    public HttpHost getAppHost() {
        return appHost;
    }

    /**
     *
     * @return
     */
    public boolean isSecured() {
        return "https".equals(this.protocol);
    }

}
