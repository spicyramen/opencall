package org.mobicents.servlet.sip.model.cc;

import org.apache.log4j.Logger;



public class CcSipCall implements Runnable{
	
	private static Logger logger = Logger.getLogger(CcSipCall.class);
	private int callId;
	private String callingNumber;
	private String calledNumber;
	private String redirectNumber;
	
	public void run() {
		logger.info("New CcSipCall Object Started()");
	}
	
	public CcSipCall(int callId,String callingNumber,String calledNumber,String redirectNumber) {
		this.callId = callId;
		CallInfoReq(callingNumber,calledNumber,redirectNumber);
		logger.info("New CcSipCall Object Created (" + this.callId + ")");
	}
	
	private void CallInfoReq(String callingNumber,String calledNumber,String redirectNumber) {
		this.callingNumber = callingNumber;
		this.calledNumber = calledNumber;
		this.redirectNumber = redirectNumber;
	}

	public int getCallId() {
		return callId;
	}

	public String getCallingNumber() {
		return callingNumber;
	}

	public String getCalledNumber() {
		return calledNumber;
	}

	public String getRedirectNumber() {
		return redirectNumber;
	}
	
	
}
