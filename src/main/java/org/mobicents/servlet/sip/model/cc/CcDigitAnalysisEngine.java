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
	private Map<Object, String> systemRoutingRules = new HashMap<Object, String>();
	private Map<Object, String> potentialMatchRules = new HashMap<Object, String>();
	private CcUtils utilObj = new CcUtils();
	private static String DELIMITER = "@";
	private static String SIP_PROTOCOL = "sip:";
	private static String SIP_PORT = "5060";
	private static int SIPURI_LIMIT = 64; // Including sip: + @ + :, hence Total 58 chars										
	

	private String originalURI = null;
	private String userURI = null;
	private String domainURI = null;
	private String portURI = null;
	private String finalURI = null;

	public String getFinalURI() {
		return finalURI;
	}

	/**
	 * Constructor is initialized with Routing rules processed from opencallrules.cfg file or DB
	 * 
	 */

	public CcDigitAnalysisEngine(Map<Object, String> rules) {
		logger.info("CcDigitAnalysisEngine() initializing...");
		systemRoutingRules = rules;
	}

	public boolean isStarted() {
		if (systemRoutingRules != null)
			return true;
		else
			return false;
	}

	/**
	 * Process SIP URI Invite Message from Opencall.java Main application
	 * 
	 */
	
	public boolean CcCallProcessSipMessage(String sipURI) {
		// logger.info("*************************** Parsing SIP URI " + "[" +
		// sipURI + "] " + "***************************");

		String[] resultURI = CcExtractURI(sipURI);
		originalURI = sipURI;

		if (resultURI == null || resultURI[0] == null || resultURI[1] == null) {
			logger.error("CcCallProcessMessage() Invalid SIP URI: " + sipURI);
			return false;
		}

		if (resultURI[2] != null) {
			/*user + domain + port*/
			logger.info("CcCallProcessMessage() URI:\t" + SIP_PROTOCOL
					+ resultURI[0].toString() + "@" + resultURI[1].toString()
					+ ":" + resultURI[2].toString());
			CcDA(resultURI[0], resultURI[1], resultURI[2]);
			return true;
		} else {
			/*user + domain*/
			logger.info("CcCallProcessMessage() URI:\t" + SIP_PROTOCOL
					+ resultURI[0].toString() + "@" + resultURI[1].toString());
			CcDA(resultURI[0], resultURI[1], "");
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

		// logger.info("CcExtractURI Parsing sipURI " + "[" + sipURI + "] ");
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
				// logger.info("CcExtractURI USER:\t" + userURI);
				domainURI = routeType[1].toString();
				resultURI[1] = domainURI;

				try {
					domainPort = domainURI.split(":");
					portURI = domainPort[1].toString();
					if ((Integer.parseInt(portURI) > 0 && Integer
							.parseInt(portURI) <= 65535) && portURI != null) {
						// logger.info("CcExtractURI DOMAIN:\t" +
						// domainPort[0].toString());
						// logger.info("CcExtractURI PORT:\t" + portURI);
						domainURI = domainPort[0].toString();
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
				// logger.info("CcExtractURI URI:\t" + SIP_PROTOCOL + userURI +
				// "@" + domainURI);
			} else {
				// logger.info("CcExtractURI URI:\t" + SIP_PROTOCOL + userURI +
				// "@" + domainURI + ":" + portURI);
			}
			
			return resultURI;
		}
	}

	/**
	 * 
	 * @param userURI
	 * @param domainURI
	 */
	private void CcDA(String userURI, String domainURI, String portURI) {
		
		if (portURI.equals("")) 
		{
			logger.info("CcDA() URI:\t" + SIP_PROTOCOL + userURI + "@"
					+ domainURI);
			CcProcessRules(userURI + DELIMITER + domainURI);
		} 
		else 
		{
			logger.info("CcDA() URI:\t" + SIP_PROTOCOL + userURI + "@"
					+ domainURI + ":" + portURI);
			CcProcessRules(userURI + DELIMITER + domainURI + ":" + portURI);
		}
	}

	/**
	 * 
	 * @param sipURI
	 */

	@SuppressWarnings({ "rawtypes", "unused", })
	
	private void CcProcessRules(String sipURI) {
	
		logger.info("CcProcessRules()  Displaying Potential Route Patterns matches");
		boolean foundRuleMatch = false;
		Set<?> systemRoutingRulesSet = systemRoutingRules.entrySet();
		Iterator<?> systemRoutingRulesit = systemRoutingRulesSet.iterator();
		
		while (systemRoutingRulesit.hasNext()) 
		{
			Map.Entry mapa = (Map.Entry) systemRoutingRulesit.next(); // key=value														
			int key = (Integer) mapa.getKey(); // getKey is used to get key 											
			String value = (String) mapa.getValue(); // getValue is used to get value
														
			if (CcProcessRulesCdcc(utilObj.getRuleValue(0, value), sipURI,value)) {
				foundRuleMatch = true;
			}
		}

		// Add Thread

		if (foundRuleMatch) {
			CcSipInit(sipURI);

		} else {
			logger.info("CcProcessRules() No Route pattern matches found");
		}

		logger.info("CcProcessRules() potentialMatchRules() cache cleaned");
		potentialMatchRules.clear();
	}

	/**
	 * 
	 * @param sipURI
	 */

	@SuppressWarnings({ "rawtypes", "unused" })
	private void CcSipInit(String sipURI) {

		logger.info("CcSipInit() Processing Route Patterns Matches for [" + sipURI + "]");
		Set<?> potentialSet = potentialMatchRules.entrySet();
		Iterator<?> it = potentialSet.iterator();
		
		while (it.hasNext()) {
			
			Map.Entry mapa = (Map.Entry) it.next(); 
			int key = (Integer) mapa.getKey(); // getKey is used to get key of Map											
			String value = (String) mapa.getValue();

		}

		CcFindSipTrunk();
	}

	/**
	 * 
	 */

	private void CcFindSipTrunk() {
		
		CcFindMatchRule finalSipURI = new CcFindMatchRule(CcExtractURI(originalURI), potentialMatchRules);
		logger.info("CcSipInit() Total rules processed: "
				+ finalSipURI.getTotalRules());
		
		int ruleNumber = finalSipURI.CcProcessBestMatchAlgorithm(2);
		CcProcessFinalSipURI(originalURI,
				utilObj.getRuleValue(0, CcExtractRuleParams(ruleNumber)));
	}

	private String CcProcessFinalSipURI(String origSipURI, String[] ruleParams) {

		/**
		 * Return sipURI after parsing rule priority Algorithm: Find userURI,
		 * domainURI and portURI Count Rules Match priority Order by priority If
		 * REGEX and _DNS_ select rule and return unmodified sipURI else if
		 * REGEX and not _DNS_ select rule: parse rule Domain and Port parse SIP
		 * URI Domain and Port replace URI Domain with rule Domain if URI Port
		 * if rule Port not empty replace URI port with rule Port else (rule
		 * Port empty) attach original port to SIP URI port
		 * 
		 */

		// ROUTE=("1","10","NUMERIC","201","192.168.1.10","5060","TLS")

		String rulePort = null;
		logger.info("CcProcessFinalSipURI() Original SIP URI: [" + origSipURI
				+ "]");

		if (ruleParams[6] != null) {
			logger.info("CcProcessFinalSipURI() [1] "
					+ ruleParams[1].toString() + " [2] "
					+ ruleParams[2].toString() + " [3] "
					+ ruleParams[3].toString() + " [4] "
					+ ruleParams[4].toString() + " [5] "
					+ ruleParams[5].toString() + " [6] "
					+ ruleParams[6].toString());
		} else {
			logger.info("CcProcessFinalSipURI() [1] "
					+ ruleParams[1].toString() + " [2] "
					+ ruleParams[2].toString() + " [3] "
					+ ruleParams[3].toString() + " [4] "
					+ ruleParams[4].toString() + " [5] "
					+ ruleParams[5].toString());

		}

		if (ruleParams[3].toString().matches("REGEX")
				&& ruleParams[5].toString().matches("_DNS_")) {
			this.finalURI = origSipURI;
			logger.info("CcProcessFinalSipURI() Final SIP URI: " + finalURI);
			return finalURI;
		} else {

			String ruleDomain = ruleParams[5].toString();
			if (ruleParams[6] != null) {
				rulePort = ruleParams[6].toString();
				portURI = rulePort;
				domainURI = ruleDomain;
			} else {
				domainURI = ruleDomain;
			}
		}

		if (ruleParams[6] != null) {
			logger.info("CcProcessFinalSipURI Building New SIP URI ["
					+ SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ portURI + "]");
			finalURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ portURI;
		} else {
			logger.info("CcProcessFinalSipURI Building New SIP URI ["
					+ SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
					+ SIP_PORT + "]");
			if (CcUtils.isValidHostName(domainURI))
				finalURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI;
			else
				finalURI = SIP_PROTOCOL + userURI + DELIMITER + domainURI + ":"
						+ SIP_PORT;

		}

		logger.info("CcProcessFinalSipURI() Final SIP URI: " + finalURI);
		return finalURI;
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
	
		Set<?> potentialSet = potentialMatchRules.entrySet();
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
					logger.info("CcProcessRulesRegexCdcc()  Digit analysis: potentialMatchRules=potentialMatchRulesExist: "
							+ sipURI + " in rule: " + value);
					potentialMatchRules.put(
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
				} catch (PatternSyntaxException e) {
					exc = e;
					return false;
				}

				if (exc != null) {
					exc.printStackTrace();
					return false;
				} else {
					// logger.info("CcProcessRulesNumericCdcc() Token RULE STRING NUMERIC "
					// + ruleString + " is valid!");
					// We match only userURI portion
					if (userURI.matches(ruleString) && !userURI.isEmpty()) {
						logger.info("CcProcessRulesNumericCdcc()  Digit analysis: potentialMatchRules=potentialMatchRulesExist: "
								+ sipURI + " in rule: " + value);
						potentialMatchRules.put(
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
						logger.info("CcProcessRulesWildCardCdcc()  Digit analysis: potentialMatchRules=potentialMatchRulesExist: "
								+ sipURI + " in rule: " + value);
						potentialMatchRules.put(
								new Integer(Integer.parseInt(ruleNumber)),
								value);
						return true;
					}
				}
			}
		}

		return false;

	}

	/**
	 * 
	 * @param routeString
	 * @return
	 */

}
