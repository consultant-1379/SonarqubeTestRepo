package com.ericsson.oss.itpf.security.sso.ejb.services;

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

import javax.ejb.Local;
import java.io.*;

import com.ericsson.oss.itpf.security.sso.utils.InvalidInputException;
import com.ericsson.oss.itpf.security.sso.utils.Timeouts;

/**
 * Created by emapawl on 2016-01-19.
 */

@Local
public interface TimeoutManagementService {

      Timeouts getTimeouts();

      long setTimeouts(Long timestamp, Integer maxTimeout, Integer idleTimeout) throws InvalidInputException, IOException ;

}
