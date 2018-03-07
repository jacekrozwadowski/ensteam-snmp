package com.ensteam.snmp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.ensteam.snmp.exception.MessageErrorException;

public class EnsteamSnmpClientImpl implements EnsteamSnmpClient {

	private Snmp snmp = null;

	private String address = null;
	private int snmpVersion = SnmpConstants.version1;
	private int timeout = 2000;
	private int retries = 2;

	protected EnsteamSnmpClientImpl(String address, int snmpVersion, int timeout, int retries) {
		this.address = address;
		this.snmpVersion = snmpVersion;
		this.timeout = timeout;
		this.retries = retries;

		if (address == null)
			throw new IllegalArgumentException("Empty target address. Please provide it in format udp:<IP>/<Port>");
	}

	protected EnsteamSnmpClientImpl(String address) {
		this(address, SnmpConstants.version1, 2000, 2);
	}

	@Override
	public int getSnmpVersion() {
		return snmpVersion;
	}

	@Override
	public void setSnmpVersion(int snmpVersion) {
		this.snmpVersion = snmpVersion;
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public int getRetries() {
		return retries;
	}

	@Override
	public void setRetries(int retries) {
		this.retries = retries;
	}

	@Override
	public void writeToFile(File file, OID oid) throws MessageErrorException, IOException {
		writeToFile(file, getAsString(oid));
	}

	@Override
	public TransportMapping<UdpAddress> init() throws IOException {
		TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);
		USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
		SecurityModels.getInstance().addSecurityModel(usm);

		transport.listen();
		return transport;
	}

	@Override
	public String getAsString(OID oid) throws MessageErrorException, IOException {
		PDU pdu = this.get(oid);

		if (pdu != null) {
			if (pdu.size() > 0) {
				if (pdu.getErrorIndex() > 0)
					throw new MessageErrorException(pdu.getErrorStatusText());
				else
					return pdu.get(0).getVariable().toString();
			} else {
				throw new MessageErrorException("Empty Protocol Data Unit");
			}

		} else {
			throw new MessageErrorException("Request Timed Out");
		}
	}

	@Override
	public void getAsync(OID oid, ResponseListener listener) throws IOException {
		getAsync(new OID[] { oid }, listener);
	}

	/* Write content to file with try-with-resources Statement */
	private void writeToFile(File file, String text) throws IOException {
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
			bufferedWriter.write(text);
		} catch (IOException e) {
			throw e;
		}
	}

	/* Return Protocol Data Unit for one given Object Identifier Class(OID) */
	private PDU get(OID oid) throws IOException {
		if (oid == null)
			throw new IllegalArgumentException("Empty OID");

		PDU pdu = get(new OID[] { oid });
		return pdu;
	}

	/*
	 * Return Protocol Data Unit (PDU) in sync way for set of given Object
	 * Identifier Class(OID)
	 */
	private PDU get(OID oids[]) throws IOException {
		PDU pdu = new PDU();
		for (OID oid : oids) {
			pdu.add(new VariableBinding(oid));
		}
		pdu.setType(PDU.GET);

		PDU res = snmp.send(pdu, getTarget(), null).getResponse();

		return res;
	}

	/*
	 * Return Protocol Data Unit (PDU) in async way for set of given Object
	 * Identifier Class(OID)
	 */
	private void getAsync(OID oids[], ResponseListener listener) throws IOException {
		PDU pdu = new PDU();
		for (OID oid : oids) {
			pdu.add(new VariableBinding(oid));
		}
		pdu.setType(PDU.GET);
		snmp.send(pdu, getTarget(), null, listener);
	}

	/* Create Generic Address Target for given address */
	private Target getTarget() {
		Address targetAddress = GenericAddress.parse(address);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setAddress(targetAddress);
		target.setRetries(retries);
		target.setTimeout(timeout);
		target.setVersion(snmpVersion);
		return target;
	}

}
