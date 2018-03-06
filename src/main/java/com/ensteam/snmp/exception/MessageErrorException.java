package com.ensteam.snmp.exception;

public class MessageErrorException extends Exception {

	private static final long serialVersionUID = -1958170128534868182L;

	public MessageErrorException() {
        super();
    }

    public MessageErrorException(String msg) {
        super(msg);
    }
	
}
