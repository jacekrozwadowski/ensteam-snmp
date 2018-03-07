package com.ensteam.snmp;

import org.snmp4j.mp.SnmpConstants;

public class EnsteamSnmpClientBuilder {

	private String address = null;
	private int snmpVersion = SnmpConstants.version1;
	private int timeout = 2000;
	private int retries = 2;
	
	public EnsteamSnmpClientBuilder(String address, int snmpVersion, int timeout, int retries) {
		super();
		this.address = address;
		this.snmpVersion = snmpVersion;
		this.timeout = timeout;
		this.retries = retries;
	}
	
	public EnsteamSnmpClientBuilder(String address) {
		super();
		this.address = address;
	}

	public EnsteamSnmpClientBuilder setSnmpVersion(int snmpVersion) {
		this.snmpVersion = snmpVersion;
		return this;
	}

	public EnsteamSnmpClientBuilder setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	public EnsteamSnmpClientBuilder setRetries(int retries) {
		this.retries = retries;
		return this;
	}
	
	public EnsteamSnmpClient build() {
		if(address==null)
			throw new IllegalArgumentException("Empty target address. Please provide it in format udp:<IP>/<Port>");
		
		if(timeout<=0)
			throw new IllegalArgumentException("Incorrect timeout value. It should be non-zero value");
		
		if(retries<=0)
			throw new IllegalArgumentException("Incorrect retries value. It should be non-zero value");
		
		if(snmpVersion!=SnmpConstants.version1 && 
		   snmpVersion!=SnmpConstants.version2c &&
		   snmpVersion!=SnmpConstants.version3)
			throw new IllegalArgumentException("Incorrect snmpVersion version. Please see into SnmpConstants class");
		
		
        return new EnsteamSnmpClientImpl(address, snmpVersion, timeout, retries);
    }
	
}
