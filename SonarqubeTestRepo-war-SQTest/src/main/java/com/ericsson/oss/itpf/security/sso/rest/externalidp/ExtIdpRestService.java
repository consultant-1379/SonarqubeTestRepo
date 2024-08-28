package com.ericsson.oss.itpf.security.sso.rest.externalidp;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.security.sso.ejb.utils.ExtIdpConfigurationHelper;

/**
 * @author egicass
 */

@Path("/")
public class ExtIdpRestService {

    @Inject
    ExtIdpConfigurationHelper helper;

    private Logger logger = LoggerFactory.getLogger(ExtIdpRestService.class);

    /**
     * gets param from pib and writes it on the apporpriate openam ExtIdp field
     *
     * @param parameter
     */
    @POST
    @Path("/extidp/{param}")
    public Response setParams(@PathParam("param") final String parameter) {

        Response response = null;
        try {
            helper.setParam(parameter);
            response = Response.status(Response.Status.OK).entity("Success in setting parameter " + parameter).build();
        } catch (final Exception e) {
            logger.error("{} ERROR: {}", getClass().getName(), e.getMessage());
            response = Response.status(Response.Status.BAD_REQUEST).entity("Parameter " + parameter + " not found").build();
        }
        return response;
    }

}
