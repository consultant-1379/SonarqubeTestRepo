package com.ericsson.oss.itpf.security.sso.rest.session;

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

import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Sdk;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import com.ericsson.oss.itpf.security.sso.ejb.services.TimeoutManagementService;
import com.ericsson.oss.itpf.security.sso.rest.TimeoutsJAXB;
import com.ericsson.oss.itpf.security.sso.utils.InvalidInputException;
import com.ericsson.oss.itpf.security.sso.utils.Timeouts;
import org.codehaus.jackson.JsonParseException;
import java.io.*;

/**
 * Created by emapawl on 2016-01-19.
 */

@Path("/")
public class TimeoutManagementRest {

      @Inject
      @Sdk
      private TimeoutManagementService timeoutManagementServiceSdk;

      @GET
      @Path("/config")
      @Produces(MediaType.APPLICATION_JSON)
      public TimeoutsJAXB getTimeouts() {
        Timeouts tmts = timeoutManagementServiceSdk.getTimeouts();
        return new TimeoutsJAXB(tmts);
      }

      @PUT
      @Path("/config")
      @Produces(MediaType.APPLICATION_JSON)
      @Consumes(MediaType.APPLICATION_JSON)
      public TimeoutsJAXB setTimeouts(final TimeoutsJAXB timeoutsJAXB) throws JsonParseException {
          Timeouts tm = timeoutsJAXB.convertToTimeouts();
          long updatedTimestamp;
          try {
              updatedTimestamp = timeoutManagementServiceSdk.setTimeouts(tm.getTimestamp(),tm.getMaxTimeout(),tm.getIdleTimeout());
          } catch (InvalidInputException e) {
             throw new JsonParseException(e.getMessage(), null);
          }
          catch (IOException e) {
             throw new JsonParseException(e.getMessage(), null);
          }
          tm.setTimestamp(updatedTimestamp);
          return new TimeoutsJAXB(tm);
      }

}
