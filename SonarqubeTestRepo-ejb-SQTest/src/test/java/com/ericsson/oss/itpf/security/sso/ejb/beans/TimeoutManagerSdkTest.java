package com.ericsson.oss.itpf.security.sso.ejb.beans;

import com.ericsson.oss.itpf.security.sso.ejb.listeners.SsoConfigProvider;
import com.ericsson.oss.itpf.security.sso.ejb.services.AuthService;
import com.ericsson.oss.itpf.security.sso.pib.ConfigurationUpdater;
import com.ericsson.oss.itpf.security.sso.utils.InvalidInputException;
import com.ericsson.oss.itpf.security.sso.utils.Timeouts;
import com.iplanet.sso.SSOException;
import com.iplanet.sso.SSOToken;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.sm.SMSException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import java.io.IOException;
import java.util.*;

/**
 * Created by epatjuc on 4/12/2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AMIdentity.class, TimeoutManagerSdk.class})
public class TimeoutManagerSdkTest {

    @Mock
    private AMIdentity amIdentity;

    @Mock
    private AuthService authService;

    @Mock
    private ConfigurationUpdater configurationUpdater;

    @Mock
    private SsoConfigProvider ssoConfigProvider;

    @Spy
    @InjectMocks
    private TimeoutManagerSdk timeoutManagerSdk = new TimeoutManagerSdk();

    @Before
    public void setUp() throws IOException {

        //Mock current configuration
        Mockito.when(ssoConfigProvider.getSessionConfigurationTimestamp()).thenReturn(1000000l);
        Mockito.when(ssoConfigProvider.getIdleSessionTimeout()).thenReturn(60);
        Mockito.when(ssoConfigProvider.getMaxSessionTimeout()).thenReturn(600);

        //Mock updating session timeouts
        Mockito.when(configurationUpdater.updateSessionTimeouts(Mockito.anyInt(), Mockito.anyInt())).thenReturn(9000000l);
    }

    @Test
    public void testGetTimeouts() {
        //invoke tested method
        Timeouts response = timeoutManagerSdk.getTimeouts();

        //Verify result
        Assert.assertEquals(1000000l, response.getTimestamp().longValue());
        Assert.assertEquals(60, response.getIdleTimeout().intValue());
        Assert.assertEquals(600, response.getMaxTimeout().intValue());
        Mockito.verify(ssoConfigProvider, Mockito.times(1)).getSessionConfigurationTimestamp();
        Mockito.verify(ssoConfigProvider, Mockito.times(1)).getIdleSessionTimeout();
        Mockito.verify(ssoConfigProvider, Mockito.times(1)).getMaxSessionTimeout();
    }

    @Test
    public void testSetTimeouts() throws Exception {

        //Mocks
        Mockito.doNothing().when(amIdentity).store();
        Mockito.doNothing().when(amIdentity).modifyService(Mockito.anyString(), Mockito.<Map>any());
        Mockito.when(amIdentity.getServiceAttributes(Mockito.anyString())).thenReturn(new HashMap());
        PowerMockito.doReturn(amIdentity).when(timeoutManagerSdk, "getAMIR", Mockito.<SSOToken>any());

        //invoke tested method
        long updatedTimestamp = timeoutManagerSdk.setTimeouts(1000000l, 500, 50);

        //Verify result
        Assert.assertEquals(9000000l, updatedTimestamp);
        Mockito.verify(ssoConfigProvider, Mockito.times(1)).getSessionConfigurationTimestamp();
        Mockito.verify(ssoConfigProvider, Mockito.times(1)).getIdleSessionTimeout();
        Mockito.verify(ssoConfigProvider, Mockito.times(1)).getMaxSessionTimeout();

        //Verify that parameters were updated in PIB
        Mockito.verify(configurationUpdater, Mockito.times(1)).updateSessionTimeouts(500, 50);

        //Verify that parameters were updated on SSO
        Map<String, Set<String>> attrValues = new HashMap<String, Set<String>>();
        attrValues.put("iplanet-am-session-max-idle-time", new HashSet<String>(Arrays.asList("50")));
        attrValues.put("iplanet-am-session-max-session-time", new HashSet<String>(Arrays.asList("500")));
        Mockito.verify(amIdentity, Mockito.times(1)).modifyService(Mockito.anyString(), Mockito.eq(attrValues));
        Mockito.verify(amIdentity, Mockito.times(1)).store();
    }

    /**
     The testcase verifies that method setTimeouts(Long, int, int) of TimeoutManagementService throws InvalidInputException when timestamp parameter is invalid.
     */
    @Test(expected=InvalidInputException.class)
    public void testSetTimeoutInvalidInputException() throws IOException, InvalidInputException {
        //Invoke tested method
        timeoutManagerSdk.setTimeouts(1l, 500, 50);
    }

    /**
     The testcase verifies that method setTimeouts(Long, int, int) of TimeoutManagementService throws InvalidInputException when timeout parameter is invalid.
     */
    @Test(expected=InvalidInputException.class)
    public void testSetTimeoutInvalidInputException1() throws IOException, InvalidInputException {
        //Invoke tested method
        timeoutManagerSdk.setTimeouts(1000000l, 10081, 50);
    }

    /**
     The testcase verifies that previous configuration is restored in PIB when updating on SSO failed with SSOException.
     */
    @Test
    public void testSetTimeoutSSOException() throws Exception {
        //Mocks
        PowerMockito.doThrow(new SSOException("SSO exception test")).when(timeoutManagerSdk, "getAMIR", Mockito.<SSOToken>any());

        //invoke tested method
        timeoutManagerSdk.setTimeouts(1000000l, 500, 50);

        //Verify results
        assertThatPibConfigurationWasRestored();
    }

    /**
     The testcase verifies that previous configuration is restored in PIB when updating on SSO failed with SMSException.
     */
    @Test
    public void testSetTimeoutSMSException() throws Exception {
        //Mocks
        PowerMockito.doThrow(new SMSException("SMSException test")).when(timeoutManagerSdk, "getAMIR", Mockito.<SSOToken>any());

        //invoke tested method
        timeoutManagerSdk.setTimeouts(1000000l, 500, 50);

        //Verify results
        assertThatPibConfigurationWasRestored();
    }

    /**
     The testcase verifies that previous configuration is restored in PIB when updating on SSO failed with IdRepoException.
     */
    @Test
    public void testSetTimeoutIdRepoException() throws Exception {
        //Mocks
        PowerMockito.doThrow(new IdRepoException("SSO exception test")).when(timeoutManagerSdk, "getAMIR", Mockito.<SSOToken>any());

        //invoke tested method
        timeoutManagerSdk.setTimeouts(1000000l, 500, 50);

        //Verify results
        assertThatPibConfigurationWasRestored();
    }

    private void assertThatPibConfigurationWasRestored() throws IOException {
        ArgumentCaptor<Integer> maxTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> idleTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(configurationUpdater, Mockito.times(2)).updateSessionTimeouts(maxTimeoutCaptor.capture(), idleTimeoutCaptor.capture());
        List<Integer> maxTimeouts = maxTimeoutCaptor.getAllValues();
        List<Integer> idleTimeouts = idleTimeoutCaptor.getAllValues();
        Assert.assertEquals(500, maxTimeouts.get(0).intValue());
        Assert.assertEquals(600, maxTimeouts.get(1).intValue());
        Assert.assertEquals(50, idleTimeouts.get(0).intValue());
        Assert.assertEquals(60, idleTimeouts.get(1).intValue());
    }

}
