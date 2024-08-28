package com.ericsson.oss.itpf.security.sso.utils;

/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

 /**
 * Created by emapawl on 2016-01-19.
 */

public class Timeouts {

    private Integer idleTimeout;

    private Integer maxTimeout;

    private Long timestamp;

    public Timeouts() {
        super();
    }

    public Timeouts(Long timestamp, Integer idleTimeout, Integer maxTimeout) {
        super();
        this.idleTimeout = idleTimeout;
        this.maxTimeout = maxTimeout;
        this.timestamp = timestamp;
    }

    public Timeouts(String timestamp, String idleTimeout, String maxTimeout) {
        super();

        if ( timestamp != null ) {
        this.timestamp = Long.parseLong(timestamp);
        }

        if ( idleTimeout != null ) {
        this.idleTimeout = Integer.parseInt(idleTimeout);
        }

        if ( maxTimeout != null ) {
        this.maxTimeout = Integer.parseInt(maxTimeout);
        }

    }

    public Integer getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Integer idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Integer getMaxTimeout() {
        return maxTimeout;
    }

    public void setMaxTimeout(Integer maxTimeout) {
        this.maxTimeout = maxTimeout;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

