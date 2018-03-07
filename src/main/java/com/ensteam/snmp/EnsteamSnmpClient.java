package com.ensteam.snmp;

import java.io.File;
import java.io.IOException;

import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;

import com.ensteam.snmp.exception.MessageErrorException;


/**
 * An interface for Ensteam Snmp Client util class.
 */
public interface EnsteamSnmpClient {

	/**
	  * The code snippet to setup a default SNMP session for UDP transport with SNMP support 
	  * @return TransportMapping<UdpAddress>
	  */
	public TransportMapping<UdpAddress> start() throws IOException;
	
	
	/**
	  * Synchronous way of getting content of PDU
	  * @param  oid Object Identifier Class(OID)
	  * @return String representation of common attributes of SNMP variable
	  */
	public String getAsString(OID oid) throws MessageErrorException, IOException;
	
	/**
	  * Asynchronous way of getting content of PDU
	  * @param  oid Object Identifier Class(OID)
	  * @param  listener ResponseListener code that process SNMP response messages. Code has to implement onResponse method
	  */
	public void getAsync(OID oid, ResponseListener listener) throws IOException;
	
	/**
	  * Synchronous way of getting content of PDU and write it content file
	  * @param  file Destination file  
	  * @param  oid Object Identifier Class(OID)
	  * @return String representation of common attributes of SNMP variable
	  */
	public void writeToFile(File file, OID oid) throws MessageErrorException, IOException;
	
	/**
	 * @return SNMP version (thus the SNMP message processing model) of the target
	 * @see org.snmp4j.mp.SnmpConstants.version1
	 * @see org.snmp4j.mp.SnmpConstants.version2c
	 * @see org.snmp4j.mp.SnmpConstants.version3
	 */
	public int getSnmpVersion();
	
	/**
	 * @param version the message processing model ID.
	 * @see org.snmp4j.mp.SnmpConstants.version1
	 * @see org.snmp4j.mp.SnmpConstants.version2c
	 * @see org.snmp4j.mp.SnmpConstants.version3
	 */
	public void setSnmpVersion(int version);
	
	/**
	 * @return Current timeout value in ms
	 */
	public int getTimeout();
	
	/**
	 * @param timeout set connection timeout value
	 */
	public void setTimeout(int timeout);
	
	/**
	 * @return Current number of connection retries
	 */
	public int getRetries();
	
	/**
	 * @param retries set connection retries value
	 */
	public void setRetries(int retries);
	
}
