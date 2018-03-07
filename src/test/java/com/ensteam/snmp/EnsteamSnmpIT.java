package com.ensteam.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
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


public class EnsteamSnmpIT {
	
	private static String SYS_DESCR;
	private static String SYS_DESCR_OID;
	private static String ADDRESS;
	private static String UNAVAILABLE_ADDRESS;
	private static String WRONG_OID;
	
	static Properties prop;
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	private EnsteamSnmpClient client;
	private TransportMapping<UdpAddress> transportMapping;
	
	@BeforeClass
	public static void configureRestAssured() {
		try(InputStream input = EnsteamSnmpIT.class.getClassLoader().getResourceAsStream("config.properties")){
			prop = new Properties();
			prop.load(input);
			
			SYS_DESCR = prop.getProperty("SYS_DESCR");
			SYS_DESCR_OID = prop.getProperty("SYS_DESCR_OID");
			ADDRESS = prop.getProperty("ADDRESS");
			UNAVAILABLE_ADDRESS = prop.getProperty("UNAVAILABLE_ADDRESS");
			WRONG_OID = prop.getProperty("WRONG_OID");
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Before
	public void setUp() throws IOException {
		assertNotNull(SYS_DESCR);
		assertNotNull(SYS_DESCR_OID);
		assertNotNull(ADDRESS);
		assertNotNull(UNAVAILABLE_ADDRESS);
		assertNotNull(WRONG_OID);
		
		client = new EnsteamSnmpClientImpl(ADDRESS);
		transportMapping = client.init();
		assertNotNull(client);
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
	
	@Test(expected = MessageErrorException.class)
    public void testWrongOIDRequest() throws MessageErrorException, IOException {
		client.getAsString(new OID(WRONG_OID));
    }
	
	@Test(expected = MessageErrorException.class)
    public void testUnavailableAddressRequest() throws MessageErrorException, IOException {
		client = new EnsteamSnmpClientImpl(UNAVAILABLE_ADDRESS);
		transportMapping = client.init();
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
