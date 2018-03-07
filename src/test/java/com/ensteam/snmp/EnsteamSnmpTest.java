package com.ensteam.snmp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class EnsteamSnmpTest {

	private static String DUMMY_ADDRESS = "dummy address";

	private EnsteamSnmpClient client;

	@Before
	public void setUp() throws IOException {
		client = new EnsteamSnmpClientImpl(DUMMY_ADDRESS);
		assertNotNull(client);
	}

	@Test
	public void dummy() {
		assertTrue(true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyConstructor() throws Exception {
		new EnsteamSnmpClientImpl(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyOID() throws Exception {
		client.getAsString(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuilderNoAddress() throws Exception {
		new EnsteamSnmpClientBuilder(null).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuilderWrongSnmpVersion() throws Exception {
		new EnsteamSnmpClientBuilder(DUMMY_ADDRESS).setSnmpVersion(100).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuilderWrongTimeout() throws Exception {
		new EnsteamSnmpClientBuilder(DUMMY_ADDRESS).setTimeout(0).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuilderWrongRetries() throws Exception {
		new EnsteamSnmpClientBuilder(DUMMY_ADDRESS).setRetries(0).build();
	}

}
