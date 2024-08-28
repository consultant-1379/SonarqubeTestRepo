package com.ericsson.oss.itpf.security.sso.pib;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;


/**
 * Created by epatjuc on 4/13/2016.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationUpdater.class})
public class ConfigurationUpdaterTest {

    @Mock
    private Logger logger;

    @Spy
    @InjectMocks
    private ConfigurationUpdater configurationUpdater = new ConfigurationUpdater();

    @Test
    public void testUpdateSessionTimeouts() throws IOException {
        //Prepare mocks
        Mockito.doNothing().when(configurationUpdater).updateIdleSessionTimeout(Mockito.anyInt());
        Mockito.doNothing().when(configurationUpdater).updateMaxSessionTimeout(Mockito.anyInt());
        Mockito.doNothing().when(configurationUpdater).updateSessionConfigurationTimestamp(Mockito.anyLong());
        //Invoke tested method
        configurationUpdater.updateSessionTimeouts(600, 60);
        //Verify result
        Mockito.verify(configurationUpdater, Mockito.times(1)).updateIdleSessionTimeout(60);
        Mockito.verify(configurationUpdater, Mockito.times(1)).updateMaxSessionTimeout(600);
        Mockito.verify(configurationUpdater, Mockito.times(1)).updateSessionConfigurationTimestamp(Mockito.anyLong());
    }

    @Test
    public void testUpdateIdleSessionTimeout() throws Exception {
        //Prepare mocks
        PowerMockito.doNothing().when(configurationUpdater, "updatePibParameter", Mockito.anyString(), Mockito.anyString());
        //Invoke tested method
        configurationUpdater.updateIdleSessionTimeout(60);
        //Verify result
        PowerMockito.verifyPrivate(configurationUpdater, Mockito.times(1)).invoke("updatePibParameter", "idleSessionTimeout", "60");
    }

    @Test
    public void testUpdateMaxSessionTimeout() throws Exception {
        //Prepare mocks
        PowerMockito.doNothing().when(configurationUpdater, "updatePibParameter", Mockito.anyString(), Mockito.anyString());
        //Invoke tested method
        configurationUpdater.updateMaxSessionTimeout(600);
        //Verify result
        PowerMockito.verifyPrivate(configurationUpdater, Mockito.times(1)).invoke("updatePibParameter", "maxSessionTimeout", "600");
    }

    @Test
    public void testUpdateSessionConfigurationTimestamp() throws Exception {
        //Prepare mocks
        PowerMockito.doNothing().when(configurationUpdater, "updatePibParameter", Mockito.anyString(), Mockito.anyString());
        //Invoke tested method
        configurationUpdater.updateSessionConfigurationTimestamp(1000000l);
        //Verify result
        PowerMockito.verifyPrivate(configurationUpdater, Mockito.times(1)).invoke("updatePibParameter", "sessionConfigurationTimestamp", "1000000");
    }

    @Test
    public void testUpdatePibParameter() throws Exception {
        //Mocks
        Whitebox.setInternalState(ConfigurationUpdater.class, "host", "svc-1-secserv");
        DefaultHttpClient httpClient = Mockito.mock(DefaultHttpClient.class);
        PowerMockito.whenNew(DefaultHttpClient.class).withNoArguments().thenReturn(httpClient);

        //Invoke tested method
        Whitebox.invokeMethod(configurationUpdater, "updatePibParameter", "maxSessionTimeout", "600");

        //Verify result
        ArgumentCaptor<HttpGet> argument = ArgumentCaptor.forClass(HttpGet.class);
        Mockito.verify(httpClient, Mockito.times(1)).execute(argument.capture());
        HttpGet httpGet = argument.getAllValues().get(0);
        Assert.assertEquals("svc-1-secserv", httpGet.getURI().getHost());
        Assert.assertEquals(8080, httpGet.getURI().getPort());
        Assert.assertEquals("/pib/configurationService/updateConfigParameterValue", httpGet.getURI().getPath());
        Assert.assertTrue(httpGet.getURI().getQuery().contains("paramName=maxSessionTimeout"));
        Assert.assertTrue(httpGet.getURI().getQuery().contains("paramValue=600"));
    }

    @Test
    public void testInit() throws Exception {
        //Mocks
        BufferedReader bufferedReader = Mockito.mock(BufferedReader.class);
        Mockito.when(bufferedReader.readLine()).thenReturn("svc-1-secserv");
        PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(bufferedReader);

        //Invoke tested method
        configurationUpdater.init();

        //Verify result
        Assert.assertEquals("svc-1-secserv", Whitebox.getInternalState(ConfigurationUpdater.class, "host"));
    }

}
