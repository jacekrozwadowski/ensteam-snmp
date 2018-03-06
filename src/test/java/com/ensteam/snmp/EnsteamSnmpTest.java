package com.ensteam.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;

import com.ensteam.snmp.exception.MessageErrorException;

public class EnsteamSnmpTest {
	
	private static final String SYS_DESCR = "HP ETHERNET MULTI-ENVIRONMENT";
	private static final String SYS_DESCR_OID = "1.3.6.1.2.1.1.1.0";
	private static final String ADDRESS = "udp:153.19.121.167/161";
	
	private static final String UNAVAILABLE_ADDRESS = "udp:153.191.121.167/161";
	private static final String WRONG_OID = "1.3.6.1.2.1.1.1.9";
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private EnsteamSnmpClient client;
	private TransportMapping<UdpAddress> transportMapping;
	
	@Before
    public void setUp() throws Exception {
		client = new EnsteamSnmpClientImpl(ADDRESS);
		transportMapping = client.start();
	}
	
	@Test
    public void testFileWrite() throws Exception {
		// Create a temporary file.
	    final File file = tempFolder.newFile("sysDescr.txt");
	    
	    // Write something to it.
	    client.writeToFile(file, new OID(SYS_DESCR_OID));
	    
	    // Read it from temp file
	    final String out = FileUtils.readFileToString(file, Charset.defaultCharset());
	    
	    // Verify the content
	    assertEquals(out, SYS_DESCR);
    }
	
	@Test(expected = IOException.class)
    public void testFileWriteException() throws Exception {
		// Create a temporary file.
	    final File file = tempFolder.newFile("nonexistent_catalog/sysDescr.txt");
	    
	    // Write something to it.
	    client.writeToFile(file, new OID(SYS_DESCR_OID));
    }
	
	@Test
    public void testListen() throws Exception {
        assertEquals(transportMapping.isListening(), true);
    }
	
	@Test(expected = IllegalArgumentException.class)
    public void testEmptyConstructor() throws Exception {
		new EnsteamSnmpClientImpl(null);
    }
	
	@Test(expected = IllegalArgumentException.class)
    public void testEmptyOID() throws Exception {
		client.getAsString(null);
    }
	
	@Test(expected = MessageErrorException.class)
    public void testWrongOIDRequest() throws MessageErrorException, IOException {
		client.getAsString(new OID(WRONG_OID));
    }
	
	@Test(expected = MessageErrorException.class)
    public void testUnavailableAddressRequest() throws MessageErrorException, IOException {
		client = new EnsteamSnmpClientImpl(UNAVAILABLE_ADDRESS);
		transportMapping = client.start();
		client.getAsString(new OID(SYS_DESCR_OID));
    }
	
	@Test
    public void testSyncRequest() throws Exception {
        assertEquals(client.getAsString(new OID(SYS_DESCR_OID)), SYS_DESCR);
    }
	
	@Test
    public void testAsyncRequest() throws Exception {
		final AsyncResponseListener listener = new AsyncResponseListener();
		client.getAsync(new OID(SYS_DESCR_OID), listener);
		synchronized (listener) {
            try {
            	listener.wait(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
	
	private class AsyncResponseListener implements ResponseListener {

        public synchronized void onResponse(ResponseEvent event) {
        	((Snmp)event.getSource()).cancel(event.getRequest(), this);
        	assertNotNull(event.getResponse());
            assertNotNull(event.getResponse().get(0));
            assertNotNull(event.getResponse().get(0).getVariable());
            assertEquals(event.getResponse().get(0).getVariable().toString(), SYS_DESCR);
        }
	
	}

}
