# Ensteam Snmp micro library

Micro tool for receive SNMP PDUs

Main features:
- Snmp4j based
- Sencha GXT is using as UI Framework


# How to build it

First you have to clone project:
```
git clone -b master https://github.com/jacekrozwadowski/ensteam-snmp.git
cd ensteam-snmp
```

To build project use maven command:
```
mvn clean package
```


# How to use

To get one PDU variable value from external device in synchronous way:
```
	EnsteamSnmpClient client = new EnsteamSnmpClientImpl("udp:153.19.121.167/161");
	client.start();
	String sysDescr = client.getAsString(new OID("1.3.6.1.2.1.1.1.0"));
	System.out.println("sys.descr: "+sysDescr);
```

To get one PDU from external device in asynchronous way:
```
	EnsteamSnmpClient client = new EnsteamSnmpClientImpl("udp:153.19.121.167/161"); //"udp:153.19.121.167/161"
	client.start();
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
	EnsteamSnmpClient client = new EnsteamSnmpClientImpl("udp:153.19.121.167/161");
	client.start();
	client.writeToFile(new File("./sys.desc"), new OID("1.3.6.1.2.1.1.1.0"));
```
