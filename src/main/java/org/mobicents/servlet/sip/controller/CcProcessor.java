package org.mobicents.servlet.sip.controller;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.model.cc.CcInitConfigSrv;
import org.mobicents.servlet.sip.model.cc.CcReadSystemConfiguration;



public class CcProcessor {

	private String INIT_FILE = "";
	private CcInitConfigSrv readConfigRules = new CcInitConfigSrv();
	private boolean isStarted;
		
	
	public boolean isStarted() {
		return isStarted;
	}

	

	private static Logger logger = Logger
			.getLogger(CcProcessor.class);
	
	
	public CcProcessor( String initFile) {
		logger.info("SipEngine() initializing...");
		INIT_FILE = initFile;
	}
	
	public boolean startService() {
		
		logger.info("SipEngine() starting...");
		
		// Initialize object to read for rules from: File, DB,Internal, or other.
		CcReadSystemConfiguration readInitParameters = new CcReadSystemConfiguration(INIT_FILE);
		
		// Reads opencall.ini and determined Connection Mode
		
		readInitParameters.CcInitSystemConfiguration(); 				
		
		try {
			readConfigRules.initializeConfiguration(readInitParameters.getSystemMode(),readInitParameters.getConnection());
			if (readConfigRules.isStarted()) {
				isStarted=true;
			} else {
				isStarted=false;
			}
			
		} catch (Exception e) {
		
			logger.error("SipEngine() Exception during system initialization");
			e.printStackTrace();
		}
		
		return true;
		
	}
	
	public String processDigitsDialed (String sipUri) {
			return readConfigRules.digitsDialed(sipUri);
	
	}
	
	public String getRuleTransport () {
		return readConfigRules.getRuleTransport();
	}

}


