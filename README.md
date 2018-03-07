# Ensteam Snmp micro library

Micro tool for receive SNMP PDUs

Main features:
- Snmp4j based
- Sync and Async message retrieve way
- Unit and Integration Tests


# How to test and build it

First you have to clone project:
```
git clone -b master https://github.com/jacekrozwadowski/ensteam-snmp.git
cd ensteam-snmp
```

To perform all tests - unit and integration use maven command:
```
mvn clean verify
```

To build project use maven command:
```
mvn clean verify package
```

*Remark!* 

*Integration test class EnsteamSnmpIT is using external properties file src/test/resources/config.properties*
*for store real data. Please modify it in case of some environment setup changes* 



# How to use

To get one PDU variable value from external device in synchronous way:
```
EnsteamSnmpClient client = new EnsteamSnmpClientBuilder("udp:153.19.121.167/161")
				.setSnmpVersion(SnmpConstants.version1)
				.setTimeout(1500)
				.setRetries(2)
				.build();
client.init();
String sysDescr = client.getAsString(new OID("1.3.6.1.2.1.1.1.0"));
System.out.println("sync sys.descr: "+sysDescr);
```

To get one PDU from external device in asynchronous way:
```
EnsteamSnmpClient client = new EnsteamSnmpClientBuilder("udp:153.19.121.167/161")
				.setSnmpVersion(SnmpConstants.version1)
				.setTimeout(1500)
				.setRetries(2)
				.build();
client.init();
ResponseListener listener = new ResponseListener() {
    public void onResponse(ResponseEvent event) {
        ((Snmp)event.getSource()).cancel(event.getRequest(), this);
        System.out.println("sys.descr: "+event.getResponse().get(0).getVariable().toString());
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
```

To write PDU to file: 
```
EnsteamSnmpClient client = new EnsteamSnmpClientBuilder("udp:153.19.121.167/161")
				.setSnmpVersion(SnmpConstants.version1)
				.setTimeout(1500)
				.setRetries(2)
				.build();
client.init();
client.writeToFile(new File("./sys.desc"), new OID("1.3.6.1.2.1.1.1.0"));
```
