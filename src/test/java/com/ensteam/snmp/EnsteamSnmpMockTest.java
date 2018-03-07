package com.ensteam.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

@RunWith(MockitoJUnitRunner.class)
public class EnsteamSnmpMockTest {

	private static final String TEST_TEXT = "Test string with normal text{}";

	@Mock
	Snmp snmp;

	@InjectMocks
	private EnsteamSnmpClientImpl client = new EnsteamSnmpClientImpl("dummy address");

	@Test
	public void testSyncGetAsString() throws Exception {
		PDU responsePDU = new PDU();
		responsePDU.setType(PDU.RESPONSE);
		responsePDU.setErrorStatus(PDU.noError);
		responsePDU.setErrorIndex(0);
		responsePDU.add(new VariableBinding(new OID(SnmpConstants.sysDescr), new OctetString(TEST_TEXT)));
		ResponseEvent event = new ResponseEvent("", null, null, responsePDU, null);

		when(snmp.send(any(PDU.class), any(Target.class), isNull())).thenReturn(event);
		assertEquals(client.getAsString(new OID(SnmpConstants.sysDescr)), new OctetString(TEST_TEXT).toString());
	}

	@Test
	public void testAsyncGet() throws Exception {
		PDU responsePDU = new PDU();
		responsePDU.setType(PDU.RESPONSE);
		responsePDU.setErrorStatus(PDU.noError);
		responsePDU.setErrorIndex(0);
		responsePDU.add(new VariableBinding(new OID(SnmpConstants.sysDescr), new OctetString(TEST_TEXT)));
		ResponseEvent event = new ResponseEvent("", null, null, responsePDU, null);

		ResponseListener listener = new ResponseListener() {
			public void onResponse(ResponseEvent event) {
				assertEquals((new OctetString(TEST_TEXT)).toString(),
						event.getResponse().get(0).getVariable().toString());
			}
		};

		doAnswer(new Answer<Void>() {

			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments != null) {
					assertTrue(arguments.length == 4);
					ResponseListener lst = (ResponseListener) arguments[3];
					lst.onResponse(event);
				} else {
					assertNotNull(arguments);
				}

				return null;
			}
		}).when(snmp).send(any(PDU.class), any(Target.class), isNull(), any(ResponseListener.class));

		client.getAsync(new OID(SnmpConstants.sysDescr), listener);
		assertTrue(true);
	}

}
