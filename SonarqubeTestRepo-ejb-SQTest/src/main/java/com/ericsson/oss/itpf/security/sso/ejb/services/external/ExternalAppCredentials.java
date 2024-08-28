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

/**
 *
 * @author ekarpia
 *
 */
public class ExternalAppCredentials {

    public static final String USERNAME_HEADER_NAME = "Username";
    public static final String TOKEN_HEADER_NAME = "SSO_Token";
    private final String userName;
    private final String ssoToken;

    /**
     *
     * @param userName
     * @param ssoToken
     */
    public ExternalAppCredentials(final String userName, final String ssoToken) {
        this.userName = userName;
        this.ssoToken = ssoToken;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the ssoToken
     */
    public String getSsoToken() {
        return ssoToken;
    }

}
