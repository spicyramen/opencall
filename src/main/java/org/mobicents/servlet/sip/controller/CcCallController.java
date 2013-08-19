package org.mobicents.servlet.sip.controller;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.model.cc.CcInitConfigSrv;
import org.mobicents.servlet.sip.model.cc.CcReadSystemConfiguration;



public class CcCallController {

	private String INIT_FILE = "";
	private CcInitConfigSrv processConfigurationRules = new CcInitConfigSrv();
	private boolean isStarted;
	private String[] callInformation;
		
	
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
				logger.info("SipEngine() started...");
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
		
		String[] callInfo = new String[4];
		
		try {
			
			logger.info("newCallProcessor() Processing Call Info: (" + Id + ") " + callingNumber + " " + calledNumber + " " + redirectNumber);
			callInfo = processConfigurationRules.processNewCallInformationCc(Id,callingNumber,calledNumber,redirectNumber);
			callInformation = callInfo;
			
			if (callInfo[1] != null) {
				logger.info("newCallProcessor() Processed Call Info completed");
				return callInfo;
			}
			else {
				logger.fatal("newCallProcessor() Called Number is empty!");
				return null;
			}
			// Return Called number
			
		} catch (Exception e) {
			logger.error("newCallProcessor() Error processing new call (" + Id + ")");
			e.printStackTrace();
			return null;
		}
			
	
	}

	public String[] getCallInformation() {
		return callInformation;
	}

	@Override
	public String toString() {
		return "CcCallController [callInformation="
				+ Arrays.toString(callInformation) + "]";
	}
	

}


