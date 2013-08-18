package org.mobicents.servlet.sip.controller;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.model.cc.CcInitConfigSrv;
import org.mobicents.servlet.sip.model.cc.CcReadSystemConfiguration;
import org.mobicents.servlet.sip.model.cc.CcSipCall;



public class CcCallController {

	private String INIT_FILE = "";
	private CcInitConfigSrv processConfigurationRules = new CcInitConfigSrv();
	private boolean isStarted;
		
	
	public boolean isStarted() {
		return isStarted;
	}

	

	private static Logger logger = Logger
			.getLogger(CcCallController.class);
	
	
	public CcCallController(String initFile) {
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
			processConfigurationRules.initializeConfiguration(readInitParameters.getSystemMode(),readInitParameters.getConnection());
			if (processConfigurationRules.isStarted()) {
				logger.info("SipEngine() started...Initialization completed.");
				isStarted=true;
			} else {
				isStarted=false;
			}
			
		} catch (Exception e) {
		
			logger.error("SipEngine() Exception during system initialization.");
			e.printStackTrace();
		}
		
		return true;
		
	}
	
	public String[] newCallProcessor(int Id, String callingNumber, String calledNumber, String redirectNumber) {
					
			/**
			 	callInfo[0] = finalCallingSipURI;
				callInfo[1] = finalCalledSipURI;
				callInfo[2] = finalRedirectSipURI;
				callInfo[3] = finalTransport;
			 */
		
		
			logger.info("CcCallController() New Call(" + Id + ")" );	
			String[] callInfo = new String[4];
			Thread newThreadedCall = new Thread(new CcSipCall(Id));
			newThreadedCall.start();
			
			callInfo = processConfigurationRules.processNewCallInformationCc(callingNumber,calledNumber,redirectNumber);
			
			if (callInfo[1]!=null) {
				return callInfo;
			}
			else {
				logger.fatal("newCallProcessor() Called Number is empty!");
				return null;
			}
			// Return Called number
			
	
	}
	

}


