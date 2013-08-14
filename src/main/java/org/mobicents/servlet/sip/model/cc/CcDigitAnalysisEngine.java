package org.mobicents.servlet.sip.model.cc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.log4j.Logger;

public class CcDigitAnalysisEngine {

	private static Logger logger = Logger
			.getLogger(CcDigitAnalysisEngine.class);
	
	private CcUtils utilObj = new CcUtils();
	
	private Map<Object, String> systemTransformRules = new HashMap<Object, String>();
	private Map<Object, String> systemCallRules = new HashMap<Object, String>();
	private Map<Object, String> potentialMatchTransformRules = new HashMap<Object, String>();
	private Map<Object, String> potentialMatchCallRules = new HashMap<Object, String>();
	
	private static String DELIMITER = "@";
	private static String SIP_PROTOCOL = "sip:";
	private static String SIP_PORT = "5060";
	private static int SIPURI_LIMIT = 64; // Including sip: + @ + :, hence Total 58 chars										
	
	private String transformedSipURI = null;
	private String originalSipURI = null;
	private String originalCallingSipURI = null;
	private String originalRedirectSipURI = null;
	
	private String userURI = null;
	private String domainURI = null;
	private String portURI = null;
	private String transportURI = null;
	private String finalSipURI = null;

	
	public boolean isStarted() {
		
		// CallRules files is mandatory
		if (systemCallRules != null)
			return true;
		else
			return false;
	}

	/**
	 * 
	 * @return finalSipURI
	 */
	
	public String getSipCalledNumberURI() {
		return finalSipURI;
	}

	/**
	 * 
	 * @return
	 */
	
	public String getTransformedSipURI() {
		return transformedSipURI;
	}
	
	/**
	 * 
	 * @return transportURI
	 */
	public String getTransportURI() {
		
		// No RouteLists implemented
		
		if (transportURI!=null) {
			transportURI = transportURI.toLowerCase();
			logger.info("Transport: " + transportURI);
			return transportURI;
		}
		else
			return null;
		
		//TODO return default Transport
	}
	
	/**
	 * 	
	 * Constructor is initialized with call routing rules processed from opencallrules.cfg file or database.
	 * @param transformRules
	 * @param callRules
	 */
	public CcDigitAnalysisEngine(Map<Object, String> transformRules,Map<Object, String> routeListsRules,Map<Object, String> callRules) {
		logger.info("CcDigitAnalysisEngine() initializing...");
		
		systemTransformRules = transformRules;
		systemCallRules = callRules;	
	}
	
	/**
	 * 
	 * @param callingNumber
	 * @param calledNumber
	 * @param redirectNumber
	 */
	
	public void CcDigitAnalysisReq(String callingNumber,String calledNumber,String redirectNumber) {
		logger.info("Digit Analysis: wait_DaReq");
		
		/**
		 * Type: 	
		 * 	1: Calling
		 *  2: Called
		 *  3: Redirect
		 */
		transformedSipURI = calledNumber;
		originalSipURI = calledNumber;
		originalCallingSipURI = callingNumber;
		originalRedirectSipURI = redirectNumber;
		
		if (systemTransformRules.size()!=0) {
			
			CcProcessNumberTransformation(callingNumber,1);
			CcProcessNumberTransformation(calledNumber,2);
			CcProcessNumberTransformation(redirectNumber,3);
			
		} else {
			transformedSipURI = calledNumber;
			return;
		}
			
	}
	
	private boolean CcProcessNumberTransformation(String Uri,int type) {
		
		
		if (type==1) {
			logger.info("*************************** CcProcessNumberTransformation Parsing SIP URI. Calling number: [" +
					Uri + "] " + "***************************");
		}
		else if (type ==2) {
			logger.info("*************************** CcProcessNumberTransformation Parsing SIP URI. Called number: [" +
					Uri + "] " + "***************************");
		}
		else if (type ==3 ) {
			logger.info("*************************** CcProcessNumberTransformation Parsing SIP URI. Redirect number: [" +
					Uri + "] " + "***************************");
		}
		else {
			return false;
		}

		String[] resultURI = CcExtractURI(Uri);	
		
		if (resultURI == null || resultURI[0] == null || resultURI[1] == null) {
			logger.error("CcProcessNumberTransformations() Invalid SIP URI: " + Uri);
			return false;
		}
		
		if (resultURI[2] != null) {
			/*user + domain + port*/
			logger.info("CcProcessNumberTransformations() URI:\t" + SIP_PROTOCOL
					+ resultURI[0].toString() + "@" + resultURI[1].toString()
					+ ":" + resultURI[2].toString());
			CcDigitAnalysisTransformation(resultURI[0], resultURI[1], resultURI[2],type);
			return true;
			
		} else {
			/*user + domain*/
			logger.info("CcProcessNumberTransformations() URI:\t" + SIP_PROTOCOL
					+ resultURI[0].toString() + "@" + resultURI[1].toString());
			CcDigitAnalysisTransformation(resultURI[0], resultURI[1], "",type);
			return true;
		}
		
	}
	
	

	/**
	 * Process SIP URI Invite Message from Opencall.java Main application
	 * Intercept call and process Transform rules.
	 * String callingNumber,String calledNumber,String redirectNumber
	 * 
	 */
	
	public boolean CcCallProcessSipMessage(String calledNumber) {
		
		logger.info("Digit Analysis: getDaRes");
		logger.info("*************************** CcCallProcessSipMessage Parsing SIP URI. Called number: [" +
				calledNumber + "] " + "***************************");

		if(calledNumber==null || calledNumber.isEmpty())
			return false;
		
		String[] resultURI = CcExtractURI(calledNumber);
		originalSipURI = calledNumber;
		

		if (resultURI == null || resultURI[0] == null || resultURI[1] == null) {
			logger.error("CcCallProcessMessage() Invalid SIP URI: " + calledNumber);
			return false;
		}

		if (resultURI[2] != null) {
			/*user + domain + port*/
			logger.info("CcCallProcessMessage() URI:\t" + SIP_PROTOCOL
					+ resultURI[0].toString() + "@" + resultURI[1].toString()
					+ ":" + resultURI[2].toString());
			CcDigitAnalysis(resultURI[0], resultURI[1], resultURI[2]);
			return true;
		} else {
			/*user + domain*/
			logger.info("CcCallProcessMessage() URI:\t" + SIP_PROTOCOL
					+ resultURI[0].toString() + "@" + resultURI[1].toString());
			CcDigitAnalysis(resultURI[0], resultURI[1], "");
			return true;
		}

	}

	/**
	 * 
	 * @param userURI
	 * @param domainURI
	 */

	private String[] CcExtractURI(String sipURI) {

		String[] routeType;
		String[] domainPort;
		String[] resultURI = new String[3];
		String[] sipProtocolURI;
		userURI = null;
		domainURI = null;
		portURI = null;

		//logger.info("CcExtractURI Parsing sipURI " + "[" + sipURI + "] ");
		if (sipURI.length() > SIPURI_LIMIT) {
			logger.error("CcExtractURI() Error Parsing sipURI" + "[" + sipURI
					+ "] Exceeded size: " + SIPURI_LIMIT);
			return null;
		}

		if (!sipURI.isEmpty()) {
			routeType = sipURI.split(DELIMITER);

			if (routeType.length != 2 || routeType[0].toString() == null
					|| routeType[1].toString() == null) {
				logger.error("CcExtractURI() Error Parsing sipURI" + "["
						+ sipURI + "] ");
				return null;
			}

			else {
				sipProtocolURI = routeType[0].split(SIP_PROTOCOL);
				if (sipProtocolURI.length != 2
						|| sipProtocolURI[0].toString() == null
						|| sipProtocolURI[1].toString() == null) {
					logger.error("CcExtractURI() Error Parsing sipURI USER SIDE"
							+ "[" + sipURI + "] ");
					return null;
				}

				userURI = sipProtocolURI[1].toString();
				resultURI[0] = userURI;
				//logger.info("CcExtractURI USER:\t" + userURI);
				domainURI = routeType[1].toString();
				resultURI[1] = domainURI;

				try {
					domainPort = domainURI.split(":");
					portURI = domainPort[1].toString();
					if ((Integer.parseInt(portURI) > 0 && Integer
							.parseInt(portURI) <= 65535) && portURI != null) {
						 //logger.info("CcExtractURI DOMAIN:\t" +
						 //domainPort[0].toString());
						 //logger.info("CcExtractURI PORT:\t" + portURI);
						//domainURI = domainPort[0].toString();
						resultURI[1] = domainURI;
						resultURI[2] = portURI;
					} else {
						 logger.error("CcExtractURI Invalid Port: " + portURI);
					}
				}

				catch (Exception e) {
					// logger.info("CcExtractURI DOMAIN:\t" + domainURI);
					// logger.info("CcExtractURI PORT:\tNo SIP Port defined in URI");
					resultURI[2] = null;
				}
			}
		} else {
			logger.error("CcExtractURI Empty SIP URI!");
			return null;
		}

		// Verify user & domain length as well as validate domain portion
		if (userURI.isEmpty()
				|| domainURI.isEmpty()
				|| (!CcUtils.isValidHostName(domainURI) && !CcUtils
						.isValidIP(domainURI)))
			return null;
		else {
			if (portURI == null) {
				//logger.info("CcExtractURI URI:\t" + SIP_PROTOCOL + userURI +
				// "@" + domainURI);
			} else {
				//logger.info("CcExtractURI URI:\t" + SIP_PROTOCOL + userURI +
				// "@" + domainURI + ":" + portURI);
			}
			
			return resultURI;
		}
	}

	/**
	 * Process incoming SIP URI and match configure call rules
	 * @param userURI
	 * @param domainURI
	 * @param portURI
	 */
	private void CcDigitAnalysis(String userURI, String domainURI, String portURI) {
		
		// No Port defined in SIP URI
		if (portURI.equals("")) 
		{
			logger.info("CcDigitAnalysis() URI:\t" + SIP_PROTOCOL + userURI + "@"
					+ domainURI);
			CcProcessCallRules(userURI + DELIMITER + domainURI);
		} 
		else 
		{
			logger.info("CcDigitAnalysis() URI:\t" + SIP_PROTOCOL + userURI + "@"
					+ domainURI + ":" + portURI);
			CcProcessCallRules(userURI + DELIMITER + domainURI + ":" + portURI);
		}
	}

	private void CcDigitAnalysisTransformation(String userURI, String domainURI, String portURI,int type) {
		
		// No Port defined in SIP URI
		if (portURI.equals("")) 
		{
			logger.info("CcTransformationDigitAnalysis() URI:\t" + SIP_PROTOCOL + userURI + "@"
					+ domainURI);
			CcProcessTransformRules(userURI + DELIMITER + domainURI,type);
		} 
		else 
		{
			logger.info("CcTransformationDigitAnalysis() URI:\t" + SIP_PROTOCOL + userURI + "@"
					+ domainURI + ":" + portURI);
			CcProcessTransformRules(userURI + DELIMITER + domainURI + ":" + portURI,type);
		}
	}

	
	/**
	 * Process Potential Call Rules
	 * @param sipURI
	 */

	@SuppressWarnings({ "rawtypes"})
	private void CcProcessCallRules(String sipURI) {
	
		logger.info("CcProcessCallRules()  Displaying Potential Route Patterns matches:");
		boolean foundRuleMatch = false;
		Set<?> systemRoutingRulesSet = systemCallRules.entrySet();
		Iterator<?> systemRoutingRulesIt = systemRoutingRulesSet.iterator();
		
		while (systemRoutingRulesIt.hasNext()) 
		{
			Map.Entry mapa = (Map.Entry) systemRoutingRulesIt.next(); 	// key=value														
			String value = (String) mapa.getValue(); 					// getValue is used to get value
														
			if (CcProcessRulesCdcc(utilObj.getRuleValue(0, value), sipURI, value)) {
				foundRuleMatch = true;
			}
		}

		
		/**
		 * Rule is valid and potential match
		 * TODO: Add MultiThreading
		 */
		if (foundRuleMatch) {
			
			CcDeviceManagerInit(sipURI);

		} else {
			logger.info("CcProcessCallRules() No Route pattern matches found");
		}

		logger.info("CcProcessCallRules() potentialMatchCallRules() cache cleaned");
		potentialMatchCallRules.clear();
	}

	/**
	 * Process Potential Call Rules
	 * @param sipURI
	 */

	@SuppressWarnings({ "rawtypes"})
	private void CcProcessTransformRules(String sipURI,int type) {
		
		logger.info("CcProcessTransformRules()  Displaying Potential Transform Patterns matches:");
		
		boolean foundRuleMatch = false;
		Set<?> systemTransformRulesSet = systemTransformRules.entrySet();
		Iterator<?> systemTransformRulesIt = systemTransformRulesSet.iterator();
		
		while (systemTransformRulesIt.hasNext()) 
		{
			Map.Entry mapa = (Map.Entry) systemTransformRulesIt.next(); 	// key=value														
			String value = (String) mapa.getValue(); 						// getValue is used to get value
														
			if (CcProcessTransformRulesCdcc(utilObj.getTransformValue(0, value), sipURI, value)) {
				foundRuleMatch = true;
			}
		}

		/**
		 * Rule is valid and potential match
		 * TODO: Add MultiThreading
		 */
		if (foundRuleMatch) {
			CcTransformInit(sipURI);

		} else {
			if(type==1)
				logger.warn("CcProcessTransformRules() No Transform patterns matches found for type: CALLING " + originalCallingSipURI);
			if(type==2)
				logger.warn("CcProcessTransformRules() No Transform patterns matches found for type: CALLED " + originalSipURI );
			if(type==3)
				logger.warn("CcProcessTransformRules() No Transform patterns matches found for type: REDIRECT " + originalRedirectSipURI);		
		}

		logger.info("CcProcessTransformRules() potentialMatchCallRules() cache cleaned");
		potentialMatchTransformRules.clear();
	}
	
	/**
	 * 
	 * @param sipURI
	 */

	private void CcTransformInit(String sipURI) {

		logger.info("CcTransformInit() Processing Transform Patterns Matches for [" + sipURI + "]");
		CcFindTransformMatch(sipURI);
		
	}
	
	/**
	 * Initiate Digit transformation
	 */
	
	private void CcFindTransformMatch(String sipURI) {
		
		CcFindMatchTransformationRule finalSipURI = new CcFindMatchTransformationRule(CcExtractURI("sip:" + sipURI), potentialMatchTransformRules);
		
		logger.info("CcTransformInit() Total rules processed: "
				+ finalSipURI.getTotalRules());
		/**
		 * Obtain matching rules by rule number
		 */
		int ruleNumber = finalSipURI.CcProcessBestMatchAlgorithm(1);
		
		/**
		 * 		originalSipURI = calledNumber;
		 *		originalCallingSipURI = callingNumber;
		 *		originalRedirectSipURI = redirectNumber;
		 */
		
		//CcProcessFinalTransformSipURI(originalSipURI,utilObj.getTransformValue(0, CcExtractRuleParams(ruleNumber)));
		logger.info("Rule found: " + ruleNumber);
		
	}

	/**
	 * 
	 * @param sipURI
	 */

	private void CcDeviceManagerInit(String sipURI) {

		logger.info("CcDeviceManagerInit() Processing Route Patterns Matches for [" + sipURI + "]");
		
		/**		
		Set<?> potentialSet = potentialMatchCallRules.entrySet();
		Iterator<?> it = potentialSet.iterator();
		
		while (it.hasNext()) {
			
			Map.Entry mapa = (Map.Entry) it.next(); 
			int key = (Integer) mapa.getKey(); // getKey is used to get key of Map											
			String value = (String) mapa.getValue();

		}
		 */
		CcFindCallMatch();
	}
	/**
	 * 
	 */

	private void CcFindCallMatch() {
		
		CcFindMatchRule finalSipURI = new CcFindMatchRule(CcExtractURI(originalSipURI), potentialMatchCallRules);
		
		logger.info("CcFindCallMatch() Total rules processed: "
				+ finalSipURI.getTotalRules());
		/**
		 * Obtain matching rules by rule number
		 */
		int ruleNumber = finalSipURI.CcProcessBestMatchAlgorithm(2);
		
		CcProcessFinalSipURI(originalSipURI,
				utilObj.getRuleValue(0, CcExtractRuleParams(ruleNumber)));
	}

	
	/**
	 * Process SIP URI  based on Call Rules	
	 * @param origSipURI
	 * @param ruleParams
	 * @return
	 */
	@SuppressWarnings("unused")
	private String CcProcessFinalTransformSipURI(String origSipURI, String[] ruleParams) {

		/**
		 * Return sipURI after parsing rule priority Algorithm: 
		 * 1. Find userURI,domainURI and portURI 
		 * 2. Count CallRules Match priority 
		 * 3. Order CallRules by priority 
		 * If REGEX and _DNS_ select rule and return unmodified sipURI 
		 * else if REGEX and not _DNS_ select rule: 
		 * Parse rule Domain and Port. 
		 * Parse SIP URI Domain and Port, replace URI Domain with rule Domain 
		 * if URI Port
		 * if rule Port not empty replace URI port with rule Port else (rule Port empty) attach original port to SIP URI port
		 * 
		 */

		// TRANSFORM=("2","FALSE","WILDCARD","XXXXXXXX","18668643232**XXXXXXXX","CALLED","FALSE")

		String rulePort = null;
		logger.info("DeviceManager::star_DmPidReq");
		logger.info("CcProcessTransformSipURI() Original SIP URI: [" + origSipURI
				+ "]");

		if (ruleParams[7] != null) {
			logger.info("CcProcessTransformSipURI() [1] "
					+ ruleParams[1].toString() + " [2] "
					+ ruleParams[2].toString() + " [3] "
					+ ruleParams[3].toString() + " [4] "
					+ ruleParams[4].toString() + " [5] "
					+ ruleParams[5].toString() + " [6] "
					+ ruleParams[6].toString() + " [7] "
					+ ruleParams[7].toString());
			
		} 	
		else {
			logger.error("CcProcessTransformSipURI() Invalid rule");
			return null;

		}

		if (ruleParams[3].toString().matches("REGEX")
				&& ruleParams[5].toString().matches("_DNS_")) {
			// TODO: DE1 Call Routing Rules transport support DNS trunk type should allow Transport definition
			this.finalSipURI = origSipURI;
			logger.info("CcProcessTransformSipURI() Final SIP URI: " + finalSipURI);
			return finalSipURI;
			
		} 
		else {

			String ruleDomain = ruleParams[5].toString();
			
			/**
			 * If Port is defined:
			 */
			
			if (ruleParams[6] != null && ruleParams[7] == null) {
				
				/**
				 * Port is defined
				 */
				if(utilObj.isPortOrTransport(ruleParams[6])==1) {
					rulePort = ruleParams[6].toString();
					portURI = rulePort;
					domainURI = ruleDomain;
				}
				
				/**
				 *  Transport is defined
				 */
				else if(utilObj.isPortOrTransport(ruleParams[6])==2) {
					transportURI = ruleParams[6].toString();
					domainURI = ruleDomain;
				}
				else {
					logger.error("Invalid parameter");
				}
				
			}
			
			/**
			 * Transport is defined
			 */
			else if (ruleParams[7] != null) {
				
				if(utilObj.isPortOrTransport(ruleParams[7])==2) {
					portURI = ruleParams[6].toString();
					transportURI = ruleParams[7].toString();
					domainURI = ruleDomain;
				}
				else {
					logger.error("Invalid parameter");
				}
			}
			
			else {
				domainURI = ruleDomain;
			}
		}

		/**
		 * Create Final SIP URI
		 */
		logger.info("SMDMSharedData::findLocalDevice:");
		if (ruleParams[6] != null) {
			
			logger.info("CcProcessTransformSipURI Building New SIP URI ["
					+ SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ portURI + "]");
			
			// ValidPort is defined
			if (portURI!=null)
			finalSipURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ portURI;
			// Transport is defined
			else {
				logger.info("CcProcessTransformSipURI Building New SIP URI ["
						+ SIP_PROTOCOL + userURI + DELIMITER + domainURI  + "] Transport defined: " + transportURI);
				finalSipURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI;
			}
					
		
		} 
		else if (ruleParams[7] != null) {
			logger.info("CcProcessTransformSipURI Building New SIP URI ["
					+ SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ portURI + "] Transport defined: " + transportURI);
			if (portURI!=null)
			finalSipURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ portURI;
		}
		else {
			logger.info("CcProcessTransformSipURI Building New SIP URI ["
					+ SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ SIP_PORT + "]");
			if (CcUtils.isValidHostName(domainURI))
				finalSipURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI;
			else
				finalSipURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
						+ SIP_PORT;

		}

		
		logger.info("CcProcessTransformSipURI() Transformed SIP URI: " + finalSipURI);
		return finalSipURI;
	}
	
	/**
	 * Process SIP URI  based on Call Rules	
	 * @param origSipURI
	 * @param ruleParams
	 * @return
	 */
	private String CcProcessFinalSipURI(String origSipURI, String[] ruleParams) {

		/**
		 * Return sipURI after parsing rule priority Algorithm: 
		 * 1. Find userURI,domainURI and portURI 
		 * 2. Count CallRules Match priority 
		 * 3. Order CallRules by priority 
		 * If REGEX and _DNS_ select rule and return unmodified sipURI 
		 * else if REGEX and not _DNS_ select rule: 
		 * Parse rule Domain and Port. 
		 * Parse SIP URI Domain and Port, replace URI Domain with rule Domain 
		 * if URI Port
		 * if rule Port not empty replace URI port with rule Port else (rule Port empty) attach original port to SIP URI port
		 * 
		 */

		// ROUTE=("1","10","NUMERIC","201","192.168.1.10","5060","TLS")

		String rulePort = null;
		logger.info("DeviceManager::star_DmPidReq");
		logger.info("CcProcessFinalSipURI() Original SIP URI: [" + origSipURI
				+ "]");

		if (ruleParams[7] != null) {
			logger.info("CcProcessFinalSipURI() [1] "
					+ ruleParams[1].toString() + " [2] "
					+ ruleParams[2].toString() + " [3] "
					+ ruleParams[3].toString() + " [4] "
					+ ruleParams[4].toString() + " [5] "
					+ ruleParams[5].toString() + " [6] "
					+ ruleParams[6].toString() + " [7] "
					+ ruleParams[7].toString());
			
		} else if (ruleParams[6] != null) {
			logger.info("CcProcessFinalSipURI() [1] "
					+ ruleParams[1].toString() + " [2] "
					+ ruleParams[2].toString() + " [3] "
					+ ruleParams[3].toString() + " [4] "
					+ ruleParams[4].toString() + " [5] "
					+ ruleParams[5].toString() + " [6] "
					+ ruleParams[6].toString());
		}	
		else {
			logger.info("CcProcessFinalSipURI() [1] "
					+ ruleParams[1].toString() + " [2] "
					+ ruleParams[2].toString() + " [3] "
					+ ruleParams[3].toString() + " [4] "
					+ ruleParams[4].toString() + " [5] "
					+ ruleParams[5].toString());

		}

		if (ruleParams[3].toString().matches("REGEX")
				&& ruleParams[5].toString().matches("_DNS_")) {
			// TODO: DE1 Call Routing Rules transport support DNS trunk type should allow Transport definition
			this.finalSipURI = origSipURI;
			logger.info("CcProcessFinalSipURI() Final SIP URI: " + finalSipURI);
			return finalSipURI;
			
		} 
		
		// TODO: DE3 Call Routing Rules using Domain name in SIP URI and type: REGEX should allow send call directly to IP
		
		else {

			String ruleDomain = ruleParams[5].toString();
			
			/**
			 * If Port is defined:
			 */
			
			if (ruleParams[6] != null && ruleParams[7] == null) {
				
				/**
				 * Port is defined
				 */
				if(utilObj.isPortOrTransport(ruleParams[6])==1) {
					rulePort = ruleParams[6].toString();
					portURI = rulePort;
					domainURI = ruleDomain;
				}
				
				/**
				 *  Transport is defined
				 */
				else if(utilObj.isPortOrTransport(ruleParams[6])==2) {
					transportURI = ruleParams[6].toString();
					domainURI = ruleDomain;
				}
				else {
					logger.error("Invalid parameter");
				}
				
			}
			
			/**
			 * Transport is defined
			 */
			else if (ruleParams[7] != null) {
				
				if(utilObj.isPortOrTransport(ruleParams[7])==2) {
					portURI = ruleParams[6].toString();
					transportURI = ruleParams[7].toString();
					domainURI = ruleDomain;
				}
				else {
					logger.error("Invalid parameter");
				}
			}
			
			else {
				domainURI = ruleDomain;
			}
		}

		/**
		 * Create Final SIP URI
		 */
		logger.info("SMDMSharedData::findLocalDevice:");
		if (ruleParams[6] != null) {
			
			logger.info("CcProcessFinalSipURI Building New SIP URI ["
					+ SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ portURI + "]");
			
			// ValidPort is defined
			if (portURI!=null)
			finalSipURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ portURI;
			// Transport is defined
			else {
				logger.info("CcProcessFinalSipURI Building New SIP URI ["
						+ SIP_PROTOCOL + userURI + DELIMITER + domainURI  + "] Transport defined: " + transportURI);
				finalSipURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI;
			}
					
		
		} 
		else if (ruleParams[7] != null) {
			logger.info("CcProcessFinalSipURI Building New SIP URI ["
					+ SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ portURI + "] Transport defined: " + transportURI);
			if (portURI!=null)
			finalSipURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ portURI;
		}
		else {
			logger.info("CcProcessFinalSipURI Building New SIP URI ["
					+ SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ SIP_PORT + "]");
			if (CcUtils.isValidHostName(domainURI))
				finalSipURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI;
			else
				finalSipURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
						+ SIP_PORT;

		}

		
		logger.info("CcProcessFinalSipURI() Final SIP URI: " + finalSipURI);
		return finalSipURI;
	}

	/**
	 * 
	 * @param ruleValue
	 * @param sipURI
	 * @param value
	 */

	/**
	 * 
	 * @param rule
	 */
	@SuppressWarnings("rawtypes")
	private String CcExtractRuleParams(int rule) {
	
		Set<?> potentialSet = potentialMatchCallRules.entrySet();
		Iterator<?> it = potentialSet.iterator();
		
		while (it.hasNext()) {	
			Map.Entry mapa = (Map.Entry) it.next(); 	// key=value separator this										
			int key = (Integer) mapa.getKey(); 			// getKey 												
			String value = (String) mapa.getValue(); 	// getValue 
													
			if (rule == key) {
				logger.info("CcExtractRule() Rule:\t" + key + "  Value:\t"
						+ value);
				return value;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param ruleValue
	 * @param sipURI
	 * @param value
	 * @return
	 */
	private boolean CcProcessRulesCdcc(String[] ruleValue, String sipURI,
			String value) {

		// logger.info("CcProcessRulesCdcc()  Searching Rules for SIP URI: " +
		// "sip:" + sipURI);
		boolean foundRuleMatch = false;

		if (ruleValue[3].equals("REGEX")) {
			if (CcProcessRulesRegexCdcc(ruleValue, sipURI, value)) {
				foundRuleMatch = true;
			}
		} else if (ruleValue[3].equals("NUMERIC")) {
			if (CcProcessRulesNumericCdcc(ruleValue, sipURI, value)) {
				foundRuleMatch = true;
			}
		} else if (ruleValue[3].equals("WILDCARD")) {
			if (CcProcessRulesWildCardCdcc(ruleValue, sipURI, value)) {
				foundRuleMatch = true;
			}
		}

		return foundRuleMatch;

	}
	
	/**
	 * 
	 * @param ruleValue
	 * @param sipURI
	 * @param value
	 * @return
	 */
	private boolean CcProcessTransformRulesCdcc(String[] ruleValue, String sipURI,
			String value) {

		logger.info("CcProcessTransformRulesCdcc()  Searching Transform Rules for SIP URI: " +  "sip:" + sipURI);
		boolean foundRuleMatch = false;

		if (ruleValue[3].equals("REGEX")) {
			if (CcProcessTransformRulesRegexCdcc(ruleValue, sipURI, value)) {
				foundRuleMatch = true;
			}
		} else if (ruleValue[3].equals("NUMERIC")) {
			if (CcProcessTransformRulesNumericCdcc(ruleValue, sipURI, value)) {
				foundRuleMatch = true;
			}
		} else if (ruleValue[3].equals("WILDCARD")) {
			if (CcProcessTransformRulesWildCardCdcc(ruleValue, sipURI, value)) {
				foundRuleMatch = true;
			}
		}

		return foundRuleMatch;

	}
	

	private boolean CcProcessTransformRulesRegexCdcc(String[] Tokens, String sipURI,String value) {
		
		//logger.info("CcProcessTransformRulesRegexCdcc()  |New Instance| SIP URI: " +  "sip:" + sipURI);
		
		
		if (Tokens == null) {
			return false;
		}

		String ruleNumber = null;
		String ruleType = null;
		String ruleSrcString = null;
		@SuppressWarnings("unused")
		String ruleDstString = null;
		
		ruleNumber = Tokens[1];
		ruleType = Tokens[3];
		ruleSrcString = Tokens[4];
		ruleDstString = Tokens[5];

		//logger.info("Tokens[] Rule Number: " + ruleNumber + " Type:" + ruleType + " Rule Source: " + ruleSrcString + " Rule Destination: " + ruleDstString);
		
		if (ruleType.equals("REGEX")) {
			if (ruleSrcString != null && !ruleSrcString.isEmpty()) {
				if (sipURI.matches(ruleSrcString)) {
					logger.info("CcProcessRulesRegexCdcc()  Digit analysis: potentialMatchCallRules=potentialMatchRulesExist: "
							+ sipURI + " in rule: " + value);
					potentialMatchTransformRules.put(
							new Integer(Integer.parseInt(ruleNumber)), value);
					return true;
				}
			}
		} else {
			
			return false;
		
		}
		return false;
	}

	private boolean CcProcessRulesRegexCdcc(String[] Tokens, String sipURI,String value) {
		
		if (Tokens == null) {
			return false;
		}

		String ruleNumber = null;
		String ruleType = null;
		String ruleString = null;
		ruleNumber = Tokens[1];
		ruleType = Tokens[3];
		ruleString = Tokens[4];

		if (ruleType.equals("REGEX")) {
			if (ruleString != null && !ruleString.isEmpty()) {
				if (sipURI.matches(ruleString)) {
					logger.info("CcProcessRulesRegexCdcc()  Digit analysis: potentialMatchCallRules=potentialMatchRulesExist: "
							+ sipURI + " in rule: " + value);
					potentialMatchCallRules.put(
							new Integer(Integer.parseInt(ruleNumber)), value);
					return true;
				}
			}
		} else {
			
			return false;
		
		}
		return false;
	}

	
	@SuppressWarnings("unused")
	private boolean CcProcessTransformRulesNumericCdcc(String[] Tokens, String sipURI,
			String value) {

		if (Tokens == null) {
			return false;
		}

		//logger.info("CcProcessTransformRulesNumericCdcc()  |New Instance| SIP URI: " +  "sip:" + sipURI);
		
		String ruleNumber = null;
		String ruleType = null;
		String ruleSrcString = null;
		String ruleDstString = null;
		
		ruleNumber = Tokens[1];
		ruleType = Tokens[3];
		ruleSrcString = Tokens[4];
		ruleDstString = Tokens[5];
		
		//logger.info("Tokens[] Rule Number: " + ruleNumber + " Type:" + ruleType + " Rule Source: " + ruleSrcString + " Rule Destination: " + ruleDstString);
		
		
		String resultURI[] = CcExtractURI("sip:" + sipURI);
		
		String userURI = resultURI[0].toString();
		if (userURI != null && !ruleSrcString.isEmpty()) {
			// logger.info("CcProcessRulesNumericCdcc()  Rule Numeric Value: " +
			// ruleString);
		} else
			return false;

		if (ruleType.equals("NUMERIC")) {
			if (ruleSrcString != null && !ruleSrcString.isEmpty()) {
				if (ruleSrcString.matches("(^(\\+)?[0-9]+([0-9]+)?)+")) {
					// logger.info("Is a valid CcProcessRulesNumericCdcc rule");
				} else {
					logger.error("Is an invalid CcProcessRulesNumericCdcc rule");
					return false;
				}

				
				ruleSrcString = ruleSrcString.replace("+", "\\+");
				PatternSyntaxException exc = null;
				try {
					Pattern.compile(ruleSrcString);
				} 
				catch (PatternSyntaxException e) {
					e.printStackTrace();
					exc = e;
					return false;
				}

				if (exc != null) {
					logger.error("Exception found: " + exc.getMessage());;
					return false;
				} 
				else {
					// logger.info("CcProcessRulesNumericCdcc() Token RULE STRING NUMERIC "
					// + ruleString + " is valid!");
					// We match only userURI portion
					if (userURI.matches(ruleSrcString) && !userURI.isEmpty()) {
						logger.info("CcProcessRulesNumericCdcc()  Digit analysis: potentialMatchCallRules=potentialMatchRulesExist: "
								+ sipURI + " in rule: " + value);
						potentialMatchTransformRules.put(
								new Integer(Integer.parseInt(ruleNumber)),
								value);
						return true;
					}
				}
			}

		} else {
			return false;
		}

		return false;

	}
	
	@SuppressWarnings("unused")
	private boolean CcProcessRulesNumericCdcc(String[] Tokens, String sipURI,
			String value) {

		if (Tokens == null) {
			return false;
		}

		String ruleNumber = null;
		String ruleType = null;
		String ruleString = null;
		ruleNumber = Tokens[1];
		ruleType = Tokens[3];
		ruleString = Tokens[4];
		String resultURI[] = CcExtractURI("sip:" + sipURI);
		String userURI = resultURI[0].toString();
		if (userURI != null && !ruleString.isEmpty()) {
			// logger.info("CcProcessRulesNumericCdcc()  Rule Numeric Value: " +
			// ruleString);
		} else
			return false;

		if (ruleType.equals("NUMERIC")) {
			if (ruleString != null && !ruleString.isEmpty()) {
				if (ruleString.matches("(^(\\+)?[0-9]+([0-9]+)?)+")) {
					// logger.info("Is a valid CcProcessRulesNumericCdcc rule");
				} else {
					logger.error("Is an invalid CcProcessRulesNumericCdcc rule");
					return false;
				}

				// logger.info("CcProcessRulesNumericCdcc() Internal Value: " +
				// ruleString);
				ruleString = ruleString.replace("+", "\\+");
				PatternSyntaxException exc = null;
				try {
					Pattern.compile(ruleString);
				} 
				catch (PatternSyntaxException e) {
					exc = e;
					return false;
				}

				if (exc != null) {
					logger.error("Exception found: " + exc.getMessage());
					exc.printStackTrace();
					return false;
				} 
				else {
					// logger.info("CcProcessRulesNumericCdcc() Token RULE STRING NUMERIC "
					// + ruleString + " is valid!");
					// We match only userURI portion
					if (userURI.matches(ruleString) && !userURI.isEmpty()) {
						logger.info("CcProcessRulesNumericCdcc()  Digit analysis: potentialMatchCallRules=potentialMatchRulesExist: "
								+ sipURI + " in rule: " + value);
						potentialMatchCallRules.put(
								new Integer(Integer.parseInt(ruleNumber)),
								value);
						return true;
					}
				}
			}

		} else {
			return false;
		}

		return false;

	}

	@SuppressWarnings("unused")
	private boolean CcProcessTransformRulesWildCardCdcc(String[] Tokens, String sipURI,
			String value) {
		if (Tokens == null) {
			return false;
		}
		
		//logger.info("CcProcessTransformRulesWildCardCdcc()  |New Instance| SIP URI: " +  "sip:" + sipURI);
		
		// logger.info("CcProcessTransformRulesWildCardCdcc() Token RULE TYPE: WILDCARD");
		String ruleNumber = null;
		String ruleType = null;
		String ruleSrcString = null;
		String ruleDstString = null;
		
		ruleNumber = Tokens[1];
		ruleType = Tokens[3];
		ruleSrcString = Tokens[4];
		ruleDstString = Tokens[5];
		
		//logger.info("Tokens[] Rule Number: " + ruleNumber + " Type:" + ruleType + " Rule Source: " + ruleSrcString + " Rule Destination: " + ruleDstString);
		
		
		String resultURI[] = CcExtractURI("sip:" + sipURI);
		String userURI = resultURI[0].toString();
		if (userURI != null && !ruleSrcString.isEmpty()) {
			// logger.info("CcProcessTransformRulesWildCardCdcc()  Rule WildCard Value: "
			// + ruleString);
		} else
			return false;

		if (ruleType.equals("WILDCARD")) {
			if (ruleSrcString != null && !ruleSrcString.isEmpty()) {
				String newRuleString = utilObj.getWildCard(ruleSrcString);
				// logger.info("CcProcessTransformRulesWildCardCdcc() WildCard Internal Value: "
				// + newRuleString);
				PatternSyntaxException exc = null;
				try {
					Pattern.compile(newRuleString);
				} catch (PatternSyntaxException e) {
					logger.error("Invalid Rule: " + ruleSrcString);
					e.printStackTrace();
					exc = e;
					return false;
				}

				if (exc != null) {
					exc.printStackTrace();
					return false;
				} 
				else {
					// logger.info("CcProcessTransformRulesWildCardCdcc() Token RULE STRING WILDCARD "
					// + ruleString + " is valid!");
					// We match only userURI portion
					if (userURI.matches(newRuleString) && !userURI.isEmpty()) {
						logger.info("CcProcessTransformRulesWildCardCdcc()  Digit analysis: potentialMatchCallRules=potentialMatchRulesExist: "
								+ sipURI + " in rule: " + value);
						potentialMatchTransformRules.put(
								new Integer(Integer.parseInt(ruleNumber)),
								value);
						return true;
					}
				}
			}
		}

		return false;

	}

	
	@SuppressWarnings("unused")
	private boolean CcProcessRulesWildCardCdcc(String[] Tokens, String sipURI,
			String value) {
		if (Tokens == null) {
			return false;
		}
		// logger.info("CcProcessRulesWildCardCdcc() Token RULE TYPE: WILDCARD");
		String ruleNumber = null;
		String ruleType = null;
		String ruleString = null;
		ruleNumber = Tokens[1];
		ruleType = Tokens[3];
		ruleString = Tokens[4];
		String resultURI[] = CcExtractURI("sip:" + sipURI);
		String userURI = resultURI[0].toString();
		if (userURI != null && !ruleString.isEmpty()) {
			// logger.info("CcProcessRulesWildCardCdcc()  Rule WildCard Value: "
			// + ruleString);
		} else
			return false;

		if (ruleType.equals("WILDCARD")) {
			if (ruleString != null && !ruleString.isEmpty()) {
				String newRuleString = utilObj.getWildCard(ruleString);
				// logger.info("CcProcessRulesWildCardCdcc() WildCard Internal Value: "
				// + newRuleString);
				PatternSyntaxException exc = null;
				try {
					Pattern.compile(newRuleString);
				} catch (PatternSyntaxException e) {
					logger.error("Invalid Rule: " + ruleString);
					exc = e;
					return false;
				}

				if (exc != null) {
					exc.printStackTrace();
					return false;
				} 
				else {
					// logger.info("CcProcessRulesWildCardCdcc() Token RULE STRING WILDCARD "
					// + ruleString + " is valid!");
					// We match only userURI portion
					if (userURI.matches(newRuleString) && !userURI.isEmpty()) {
						logger.info("CcProcessRulesWildCardCdcc()  Digit analysis: potentialMatchCallRules=potentialMatchRulesExist: "
								+ sipURI + " in rule: " + value);
						potentialMatchCallRules.put(
								new Integer(Integer.parseInt(ruleNumber)),
								value);
						return true;
					}
				}
			}
		}

		return false;

	}

}
