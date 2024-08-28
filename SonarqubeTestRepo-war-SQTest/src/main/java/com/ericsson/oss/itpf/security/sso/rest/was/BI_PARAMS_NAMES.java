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
package com.ericsson.oss.itpf.security.sso.rest.was;

/**
 * BI Web Applications Form Param Names
 *
 * @author ekarpia
 *
 */
public enum BI_PARAMS_NAMES {
    FORM_ACTION("form"), CMS_SERVER("_id2:logon:CMS"), CSF_VIEW("com.sun.faces.VIEW"), BTTOKEN("bttoken");

    private final String formElementId;

    /**
     * @param paramIdOrName
     */
    BI_PARAMS_NAMES(final String paramIdOrName) {
        this.formElementId = paramIdOrName;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return formElementId;
    }

}