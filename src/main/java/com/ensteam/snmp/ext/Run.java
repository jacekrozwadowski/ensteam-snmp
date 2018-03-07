package com.ensteam.snmp.ext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;

import com.ensteam.snmp.EnsteamSnmpClient;
import com.ensteam.snmp.EnsteamSnmpClientBuilder;
import com.ensteam.snmp.exception.MessageErrorException;

public class Run {

	public static void main(String[] args) throws IOException, MessageErrorException {
		
		System.out.println("Sync way");
		EnsteamSnmpClient client = new EnsteamSnmpClientBuilder("udp:153.19.121.167/161")
				.setSnmpVersion(SnmpConstants.version1)
				.setTimeout(1500)
				.setRetries(2)
				.build();
		
		client.start();
		String sysDescr = client.getAsString(new OID("1.3.6.1.2.1.1.1.0"));
		System.out.println("sync sys.descr: "+sysDescr);
		
		
		System.out.println("Async way");
		ResponseListener listener = new ResponseListener() {
		    public void onResponse(ResponseEvent event) {
		        ((Snmp)event.getSource()).cancel(event.getRequest(), this);
		        System.out.println("async sys.descr: "+event.getResponse().get(0).getVariable().toString());
		    }
		};
		client.getAsync(new OID("1.3.6.1.2.1.1.1.0"), listener);
		synchronized (listener) {
		    try {
		        listener.wait(10000);
		    } catch (InterruptedException e) {
		        e.printStackTrace();
		    }
		}
		
		System.out.println("Write to file");
		File file = new File("./sys.desc");
		client.writeToFile(file, new OID("1.3.6.1.2.1.1.1.0"));
		String out = FileUtils.readFileToString(file, Charset.defaultCharset());
		System.out.println("file sys.descr: "+out);

	}

}
