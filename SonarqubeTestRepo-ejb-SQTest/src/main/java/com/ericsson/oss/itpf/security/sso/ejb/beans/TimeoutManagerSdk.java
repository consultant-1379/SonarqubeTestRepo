package com.ericsson.oss.itpf.security.sso.ejb.beans;

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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.itpf.security.sso.utils.InvalidInputException;
import com.iplanet.sso.SSOException;
import com.sun.identity.sm.SMSException;
import com.iplanet.sso.SSOToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.security.accesscontrol.EPredefinedRole;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.annotation.Authorize;
import com.ericsson.oss.itpf.security.sso.ejb.listeners.SsoConfigProvider;
import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Rest;
import com.ericsson.oss.itpf.security.sso.ejb.qualifiers.Sdk;
import com.ericsson.oss.itpf.security.sso.ejb.services.AuthService;
import com.ericsson.oss.itpf.security.sso.ejb.services.TimeoutManagementService;
import com.ericsson.oss.itpf.security.sso.utils.Timeouts;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdRepoException;
import com.ericsson.oss.itpf.security.sso.pib.ConfigurationUpdater;

import java.util.Arrays;
import java.util.HashMap;
import java.io.*;

import javax.inject.Inject;
import javax.ejb.Stateless;

/**
 * Created by emapawl on 2016-01-19.
 */

@Stateless
@Sdk
public class TimeoutManagerSdk implements TimeoutManagementService {

      private static final String REALM = "/";

      private static final String SERVICE_NAME = "iPlanetAMSessionService";

      private static final String MAX_SESSION_TIME = "iplanet-am-session-max-session-time";
      private static final String MAX_IDLE_TIME = "iplanet-am-session-max-idle-time";
      private static final String MAX_TIMEOUT = "session_timeout";
      private static final String IDLE_TIMEOUT = "idle_session_timeout";
      private static final String SESSION_MANAGEMENT = "session_management";
      private static final String READ_ACTION = "read";
      private static final String DELETE_ACTION = "delete";
      private static final String TIMESTAMP_EXCEPTION_MESSAGE_FORMAT = "Stored and send timestamp are different: sent timestamp %d, stored timestamp %d ";
      private static final String TIMEOUT_EXCEPTION_MESSAGE_FORMAT = "Invalid timeout value: allowable range is from %d to %d.";
      private static final int MINIMUM_TIMEOUT_VALUE = 1;
      private static final int MAXIMUM_TIMEOUT_VALUE = 10080;

      private Logger logger = LoggerFactory.getLogger(TimeoutManagerSdk.class);

      @Inject
      @Rest
      private AuthService authService;

      @Inject
      private SsoConfigProvider ssoConfigProvider;

      @Inject
      private ConfigurationUpdater configurationUpdater;

      @Override
      @Authorize(resource=SESSION_MANAGEMENT, action=READ_ACTION, role={EPredefinedRole.SECURITYADMIN})
      public Timeouts getTimeouts() {

          logger.info("Get timeouts");
          Timeouts timeouts = new Timeouts();
          SSOToken ssoToken = null;
          /*Map<String, Set<String>> attrMap = null;
          Set<String> valueSet = null;

          try {
            ssoToken = authService.login();
            AMIdentity ai = getAMIR(ssoToken);
            attrMap = (Map<String, Set<String>>) ai.getServiceAttributes(SERVICE_NAME);
            if (attrMap.containsKey(MAX_SESSION_TIME)) {
              valueSet = attrMap.get(MAX_SESSION_TIME);
              timeouts.setMaxTimeout(Integer.parseInt(valueSet.iterator().next()));
            }
            if (attrMap.containsKey(MAX_IDLE_TIME)) {
              valueSet = attrMap.get(MAX_IDLE_TIME);
              timeouts.setIdleTimeout(Integer.parseInt(valueSet.iterator().next()));
            }
          } catch (SSOException e) {
              e.printStackTrace();
          } catch (SMSException e) {
              e.printStackTrace();
          } catch (IdRepoException e) {
              e.printStackTrace();
          }
          finally {

            timeouts.setTimestamp(200L);
            authService.logout(ssoToken);
            return timeouts;

            } */
          timeouts.setMaxTimeout(ssoConfigProvider.getMaxSessionTimeout());
          timeouts.setIdleTimeout(ssoConfigProvider.getIdleSessionTimeout());
          timeouts.setTimestamp(ssoConfigProvider.getSessionConfigurationTimestamp());
          return timeouts;

      }

      @Override
      @Authorize(resource=SESSION_MANAGEMENT, action=READ_ACTION, role={EPredefinedRole.SECURITYADMIN})
      public long setTimeouts(Long timestamp, Integer maxTimeout, Integer idleTimeout) throws InvalidInputException, IOException {

        logger.info("Set timeouts");
        if(!validateTimeouts(maxTimeout, idleTimeout)){
            throw new InvalidInputException(String.format(TIMEOUT_EXCEPTION_MESSAGE_FORMAT, MINIMUM_TIMEOUT_VALUE, MAXIMUM_TIMEOUT_VALUE));
        }
        Map<String, Set<String>> attrValues = new HashMap<String, Set<String>>();
        attrValues.put(MAX_IDLE_TIME,new HashSet<String>(Arrays.asList(idleTimeout.toString())));
        attrValues.put(MAX_SESSION_TIME,new HashSet<String>(Arrays.asList(maxTimeout.toString())));
        SSOToken ssoToken = null;

        Long storedTimestamp = ssoConfigProvider.getSessionConfigurationTimestamp();
        Integer storedIdleTimeout = ssoConfigProvider.getIdleSessionTimeout();
        Integer storedMaxTimeout = ssoConfigProvider.getMaxSessionTimeout();

        if(! timestamp.equals(storedTimestamp)) {
           throw new InvalidInputException(String.format(TIMESTAMP_EXCEPTION_MESSAGE_FORMAT, timestamp, storedTimestamp));

        }

            long updatedTimestamp = configurationUpdater.updateSessionTimeouts(maxTimeout, idleTimeout);

            try {

               ssoToken = authService.login();
               AMIdentity ai = null;
               ai = getAMIR(ssoToken);
               Map<String, Set<String>> attrMap = (Map<String, Set<String>>) ai.getServiceAttributes(SERVICE_NAME);
               ai.modifyService(SERVICE_NAME, attrValues);
               ai.store();

            } catch (SSOException e) {
                configurationUpdater.updateSessionTimeouts(storedMaxTimeout, storedIdleTimeout);
                e.printStackTrace();
            } catch (SMSException e) {
                configurationUpdater.updateSessionTimeouts(storedMaxTimeout, storedIdleTimeout);
                e.printStackTrace();
            } catch (IdRepoException e) {
                configurationUpdater.updateSessionTimeouts(storedMaxTimeout, storedIdleTimeout);
                e.printStackTrace();
            } finally {
                authService.logout(ssoToken);
            }
            return updatedTimestamp;

      }

      private AMIdentity getAMIR(SSOToken adminSSOToken) throws SMSException, SSOException, IdRepoException {
        AMIdentityRepository repo = null;
        repo = new AMIdentityRepository(adminSSOToken, REALM);
        return repo.getRealmIdentity();

      }

      private boolean validateTimeouts(int maxSessionTimeout, int idleSessionTimeout) {
          return maxSessionTimeout >= MINIMUM_TIMEOUT_VALUE && maxSessionTimeout <= MAXIMUM_TIMEOUT_VALUE
                  && idleSessionTimeout >= MINIMUM_TIMEOUT_VALUE && idleSessionTimeout <= MAXIMUM_TIMEOUT_VALUE;
      }


}
