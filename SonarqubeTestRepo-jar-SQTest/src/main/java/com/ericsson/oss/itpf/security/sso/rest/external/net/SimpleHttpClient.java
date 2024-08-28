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
package com.ericsson.oss.itpf.security.sso.rest.external.net;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.NewCookie;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

public class SimpleHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHttpClient.class);

    private CookieStore httpCookieStore;
    private CloseableHttpClient httpClient;

    /**
     * To disallow
     */
    private SimpleHttpClient() {

    }

    public SimpleHttpClient(final CookieStore httpCookieStore, final CloseableHttpClient httpClient) {
        super();
        this.httpCookieStore = httpCookieStore;
        this.httpClient = httpClient;
    }

    /**
     * @return the httpCookieStore
     */
    public CookieStore getHttpCookieStore() {
        return httpCookieStore;
    }

    /**
     * @return the httpClient
     */
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Gets instance of Http Client
     *
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static SimpleHttpClient getHttpClient(final boolean isSecureProtocol) throws IOException {

        SimpleHttpClient externalHttpClient = null;

        final CookieStore httpCookieStore = new BasicCookieStore();

        final int maxConnections = 10;

        if (isSecureProtocol) {
            // Create a trust manager that does not validate certificate chains
            SSLContext sslContext;

            try {

                sslContext = SSLContext.getInstance("TLS");

                final TrustManager tm = new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        // TODO Auto-generated method stub
                        return null;
                    }
                };

                sslContext.init(null, new TrustManager[] { tm }, null);

                externalHttpClient = new SimpleHttpClient(httpCookieStore,
                        HttpClients.custom().setDefaultCookieStore(httpCookieStore).setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections)
                                .setSslcontext(sslContext).setHostnameVerifier(new AllowAllHostnameVerifier()).build());

            } catch (final GeneralSecurityException e) {
                throw new IOException(e);
            }

        } else {
            externalHttpClient = new SimpleHttpClient(httpCookieStore, HttpClients.custom().setDefaultCookieStore(httpCookieStore)
                    .setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).build());
        }

        return externalHttpClient;
    }

    /**
     *
     * @param relativePath
     * @return
     * @throws UnsupportedEncodingException
     */
    public HttpPost generatePost(final HttpHost httpHost, final String relativePath, final List<NameValuePair> parameters)
            throws UnsupportedEncodingException {

        final String uri = String.format("%s://%s%s", httpHost.getSchemeName(), httpHost.toHostString(), relativePath);

        logger.info("uri {}", uri);

        final HttpPost httpPost = new HttpPost(uri);

        if (parameters != null && !parameters.isEmpty()) {
            httpPost.setEntity(new UrlEncodedFormEntity(parameters));
        }

        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded; charset=UTF-8");
        httpPost.addHeader(HttpHeaders.HOST, httpHost.getHostName());

        return httpPost;
    }

    /**
     *
     * @param relativePath
     * @return
     * @throws UnsupportedEncodingException
     */
    public HttpGet generateGet(final HttpHost httpHost, final String relativePath, final List<NameValuePair> headers)
            throws UnsupportedEncodingException {

        final String uri = String.format("%s://%s%s", httpHost.getSchemeName(), httpHost.toHostString(), relativePath);

        final HttpGet httpGet = new HttpGet(uri);

        if (headers != null) {
            for (final NameValuePair header : headers) {
                httpGet.addHeader(header.getName(), header.getValue());
            }
        }

        return httpGet;
    }

    /**
     * Converts cookies from Apache HTTP Client format to JAX-RS Cookie format
     * 
     * @param httpCookieStore
     * @return
     */
    public static NewCookie[] convertCookies(final CookieStore httpCookieStore) {
        final List<Cookie> cookiesFromResponse = httpCookieStore.getCookies();

        logger.info("\ncookies size: {}\ncookies list {} ", cookiesFromResponse.size(), cookiesFromResponse);

        final NewCookie[] cookies = new NewCookie[cookiesFromResponse.size()];

        int i = 0;
        for (final Cookie cookie : cookiesFromResponse) {

            // super(name, value, path, domain);
            // TODO -> maxAge we should be reading from original cookie, needs extra calculations

            final Date expiryDate = cookie.getExpiryDate();

            final int maxAge = NewCookie.DEFAULT_MAX_AGE;

            if (expiryDate != null) {

            }

            cookies[i] = new NewCookie(cookie.getName(), cookie.getValue(),

                    cookie.getPath(), cookie.getDomain(), cookie.getVersion(), cookie.getComment(), maxAge, cookie.isSecure());
            i++;
        }

        return cookies;
    }
}
