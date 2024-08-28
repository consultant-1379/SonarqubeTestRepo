package com.ericsson.oss.itpf.security.sso.rest;


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

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlType;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonProperty;
import com.ericsson.oss.itpf.security.sso.utils.Timeouts;

/**
 * Created by emapawl on 2016-01-19.
 */

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Timeouts")
public class TimeoutsJAXB {

    private static final String MAX_TIMEOUT = "session_timeout";
    private static final String IDLE_TIMEOUT = "idle_session_timeout";
    private static final String TIMESTAMP = "timestamp";

    public TimeoutsJAXB(Timeouts tm) {
        super();
        this.idleTimeout = tm.getIdleTimeout().toString();
        this.maxTimeout = tm.getMaxTimeout().toString();
        this.timestamp = tm.getTimestamp().toString();
    }

    public TimeoutsJAXB()
    {
        super();
    }

    private String idleTimeout;

    private String maxTimeout;

    private String timestamp;

    @JsonProperty(IDLE_TIMEOUT)
    public String getIdleTimeout() {
        return idleTimeout.toString();
    }

    @JsonProperty(IDLE_TIMEOUT)
    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @JsonProperty(MAX_TIMEOUT)
    public String getMaxTimeout() {
        return maxTimeout.toString();
    }

    @JsonProperty(MAX_TIMEOUT)
    public void setMaxTimeout(String maxTimeout) {
        this.maxTimeout = maxTimeout;
    }

    @JsonProperty(TIMESTAMP)
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty(TIMESTAMP)
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Timeouts convertToTimeouts() throws JsonParseException {
        Timeouts tm;
        try {
            tm = new Timeouts(timestamp, idleTimeout, maxTimeout);
        }
        catch (NumberFormatException e)
        {
            throw new JsonParseException(e.getMessage(), null);
        }

        return tm;
    }

    public String toString() {
         return String.format("TargetGroupJAXB{" + TIMESTAMP + " = %s, " + MAX_TIMEOUT + " = %s, " + IDLE_TIMEOUT + " = %s}", timestamp, maxTimeout, idleTimeout);
    }

}