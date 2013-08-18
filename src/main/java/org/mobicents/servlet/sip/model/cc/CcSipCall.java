package org.mobicents.servlet.sip.model.cc;

import org.apache.log4j.Logger;



public class CcSipCall implements Runnable{
	
	private static Logger logger = Logger.getLogger(CcSipCall.class);
	private int callId;
	
	public void run() {
		logger.info("New Call Object Started");
	}
	
	public CcSipCall(int callId) {
		this.callId = callId;
		logger.info("New Call Object Created (" + this.callId + ")");
	}
	
	public void processCallCdcc(CcInitConfigSrv init) {
		
	}
	
}
