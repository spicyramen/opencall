package org.mobicents.servlet.sip.model.cc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.log4j.Logger;

public class CcSystemConfigurationEngine implements
		CcSystemConfigurationFileInterface {

	/**
	 * Default Values declaration RULE = NUMERIC PRIORITY = NUMERIC TYPE =
	 * REGEX, WILDCARD, NUMERIC STRING = VALUE TARGET = IP ADDRESS, HOSTNAME,
	 * _DNS_
	 * 
	 * RULES ENTRIES
	 * 
	 * RULE,PRIORITY,TYPE,STRING,TARGET
	 * 
	 * ROUTE=("1","10","REGEX","(.*)@.*","_DNS_")
	 * ROUTE=("2","50","REGEX","(.*)@videolab.att.com","_DNS_")
	 * ROUTE=("3","50","NUMERIC","201","110.10.0.210")
	 * ROUTE=("4","50","WILDCARD","10X","110.10.0.200")
	 * ROUTE=("5","10","WILDCARD","10X","110.10.0.200")
	 * ROUTE=("6","10","WILDCARD","XXXXXXXX","videoalpha.att.com")
	 * ROUTE=("7","10","WILDCARD","XXXXXXXX","videolab.att.com")
	 * ROUTE=("8","50","NUMERIC","210","110.10.0.210","5070")
	 */

	private static Logger logger = Logger
			.getLogger(CcSystemConfigurationEngine.class);
	private static String DELIMITER = "=";
	private static int TOKEN_COUNT = 5;
	private static int START_PORT = 1;
	private static int END_PORT = 65535;
	private static int RULE_LIMIT = 100;
	private static int PRIORITY_LOWER = 1;
	private static int PRIORITY_UPPER = 100;
	private CcUtils utilObj = new CcUtils();
	private ArrayList<String> configParams = new ArrayList<String>();
	private ArrayList<String> mandatoryConfigParamsRules = new ArrayList<String>();
	private ArrayList<String> mandatoryConfigParamsTransforms = new ArrayList<String>();
	private ArrayList<String> mandatoryConfigParamsRouteList = new ArrayList<String>();
	private ArrayList<String> candidateRoutePatterns = new ArrayList<String>();
	private ArrayList<String> routePatterns = new ArrayList<String>();
	private Map<Object, String> callRoutingRules = new HashMap<Object, String>();

	private String CONFIGURATION_FILE = "../standalone/configuration/opencall/opencallrules.cfg";
	private int ACCESS_MODE = 3;

	/**
	 * Constructor
	 * 
	 */
	public CcSystemConfigurationEngine() {
		logger.info("CcSystemConfigurationEngine() DefaultSystemConfiguration initializing...");
	}

	public CcSystemConfigurationEngine(int accessMode,
			ArrayList<String> connection) {
		logger.info("CcSystemConfigurationEngine() DefaultSystemConfiguration initializing...Mode: ["
				+ accessMode + "]");
		if (connection.size() != 0 && accessMode == 1) {
			ACCESS_MODE = 1;
			logger.info("CcSystemConfigurationEngine() DefaultSystemConfiguration(Local File) initializing...");

		} else if (connection.size() != 0 && accessMode == 2) {
			ACCESS_MODE = 2;
			logger.info("CcSystemConfigurationEngine() DefaultSystemConfiguration(DB) initializing...");
		} else if (connection.size() != 0 && accessMode == 3) {
			ACCESS_MODE = 3;
			logger.info("CcSystemConfigurationEngine() DefaultSystemConfiguration(Built-in rules) initializing...");
		} else if (connection.size() != 0 && accessMode == 0) {
			//TODO
			ACCESS_MODE = 0;
			logger.info("CcSystemConfigurationEngine() DefaultSystemConfiguration(Undefined)");

		} else {
			logger.error("CcSystemConfigurationEngine() DefaultSystemConfiguration(Error)");
		}

	}

	public boolean CcStartCallRulesEngine(String fileName) {
		CONFIGURATION_FILE = fileName;
		logger.info("CcSystemConfigurationEngine() CcStartFileEngine initializing...Mode: ["
				+ ACCESS_MODE + "]" + " CALL RULES:  " + CONFIGURATION_FILE);
		if (CcReadConfigurationFile())
			return true;
		else
			return false;
	}

	public boolean CcStartCallRulesEngine() {
		logger.info("CcSystemConfigurationEngine() CcStartFileEngine initializing...Mode: ["
				+ ACCESS_MODE + "]");
		if (CcReadConfigurationFile())
			return true;
		else
			return false;
	}

	public boolean CcStartCallTransformsEngine() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean CcStartRouteListEngine() {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * ReadConfigurationFile
	 * 
	 */
	public boolean CcStartDbEngine(String DBTYPE, String DBHOSTNAME,
			String DBPORT, String DBNAME, String DBUSERNAME, String DBPASSWORD) {

		logger.info("CcSystemConfigurationEngine() CcStartDbEngine initializing...Mode: ["
				+ ACCESS_MODE + "]");
		CcReadDB MySqlConnection = new CcReadDB();

		logger.info("CcSystemConfigurationEngine Verifying DB Parameters: "
				+ "DBTYPE: " + DBTYPE + " DBHOSTNAME: " + DBHOSTNAME
				+ " DBPORT: " + DBPORT + " DBNAME: " + DBNAME + " DBUSERNAME: "
				+ DBUSERNAME + " DBPASSWORD: " + DBPASSWORD);
		MySqlConnection.CcCheckDB(DBHOSTNAME, Integer.parseInt(DBPORT), DBNAME,
				DBUSERNAME, DBPASSWORD);

		if (MySqlConnection.CcInitDBConnection()) {
			logger.info("CcStartDbEngine() Attempting to read from DB");

			/* Init system parameters */
			CcInitConfigurationValues(true);

			if (MySqlConnection.CcReadDBInfo()) {
				logger.info("CcStartDbEngine()Verifying DB route patterns");
				if (CcVerifyDbRoutePatterns(MySqlConnection.getDBRules()))
					return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * 
	 * @return
	 */

	private boolean CcReadConfigurationFile() {
		logger.info("CcSystemConfigurationEngine() CcStartReadConfigurationFile CONFIGURATION_FILE: "
				+ CONFIGURATION_FILE);
		CcInitConfigurationValues(true);
		if (CcInitConfigurationFile())
			return true;
		else
			return false;
	}

	/**
	 * 
	 * @return
	 */

	private boolean CcInitConfigurationFile() {
		logger.info("CcSystemConfigurationEngine() CcInitConfigurationFile initializing...");
		try {
			if (CcVerifyConfigurationFileAccess(CONFIGURATION_FILE)) 
			{ // Verify File
																	
				if (!CcReadCallRulesParameters(CONFIGURATION_FILE)) 
				{ // Read Call rules File
																	
					return false;
				}
				if (!CcVerifyFileCallRules(CONFIGURATION_FILE))
				{ // Verify File Parameters
																
					return false;
				}
				return true;
			
			} 
			else 
			{
				
				return false;
			
			}
		} 
		catch (IOException e) 
		{
		
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param configurationFileName
	 * @return
	 * @throws IOException
	 */

	private boolean CcVerifyConfigurationFileAccess(String configurationFileName)
			throws IOException {
		if (configurationFileName != "") {
			CONFIGURATION_FILE = configurationFileName;
		}
		File configFile = new File(CONFIGURATION_FILE);
		if (configFile.exists() && configFile.canRead()) {
			return true;
		} else {
			logger.error("CcVerifyConfigurationFileAccess Inaccessible file " + configurationFileName);
			return false;

		}
	}

	/**
	 * Initialize Configuration parameters
	 * 
	 * @param startInit
	 */

	private void CcInitConfigurationValues(boolean startInit) {
		logger.info("CcSystemConfigurationEngine() CcInitConfigurationValues initializing...");
		if (startInit) {
			logger.info("CcSystemConfigurationEngine() CcInitConfigurationValues (Mandatory) initializing...");
			mandatoryConfigParamsRules.add("ROUTE");
		}

	}

	/**
	 * 
	 * @param configurationFileName
	 * @throws IOException
	 */

	private boolean CcReadCallRulesParameters(String configurationFileName)
			throws IOException {

		try {
			FileInputStream fstream = new FileInputStream(configurationFileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// logger.info (strLine);
				strLine = strLine.trim();
				if (strLine.startsWith("#") || strLine.isEmpty()) {
					// logger.info ("Comment Found");
				} else {
					candidateRoutePatterns.add(strLine);
				}
			}

			// Close the input stream
			in.close();
			CcDisplayCandidateRules();
			return true;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.error("CcReadFileParameters() " + e.getMessage());
			return false;
		}

	}

	private boolean CcDisplayCandidateRules() throws IOException {
		logger.info("CcDisplayCandidateRules() Candidate Route patterns");
		int intPatterns = 0;
		for (String r : candidateRoutePatterns) {
			logger.info(r);
			intPatterns++;
		}
		if (intPatterns == 0) {
			return false;
		} else {
			return true;
		}

	}

	private boolean CcVerifyDbRoutePatterns(ArrayList<String> rules) {
		routePatterns = rules;
		logger.info("CcVerifyDbRoutePatterns() Route patterns Total ("
				+ routePatterns.size() + ")");
		String[] routeType;
		int index = 1;
		/* given string will be split by the argument delimiter provided. */

		if (!routePatterns.isEmpty()) {

			for (String route : routePatterns) {
				logger.info("*************************** Parsing Rule " + "["
						+ index + "] " + "***************************");
				logger.info("CcVerifyDbRoutePatterns() Parsing Rule " + "["
						+ index + "] " + route);

				routeType = route.split(DELIMITER);

				if (routeType.length > 2) {
					logger.error("CcVerifyDbRoutePatterns() Error Parsing Rule"
							+ "(" + index + ") " + route);
				} else {
					logger.info("Rule Type: " + routeType[0].toString()
							+ " Value: " + routeType[1].toString());
					if (CcVerifyRuleSyntax(index, 0, routeType[0].toString())
							&& CcVerifyRuleSyntax(index, 1,
									routeType[1].toString())) {
						if (CcVerifyRuleLogic(routeType[0].toString(),
								routeType[1].toString())) {
							try {
								CcDigitAnalisysEngineInit(
										routeType[0].toString(),
										routeType[1].toString());
							} catch (InterruptedException e) {

								e.printStackTrace();
							}
						} else {
							logger.error("CcVerifyDbRoutePatterns() Error in VerifyRuleLogic"
									+ "(" + index + ") " + route + "\n");

						}
					} else {
						logger.error("CcVerifyDbRoutePatterns() Error in VerifyRuleType"
								+ "(" + index + ") " + route + "\n");
					}
				}
				index++;
			}
		} else {
			logger.error("CcVerifyDbRoutePatterns() No Route Patterns found in DB!");
			return false;
		}
		return true;
	}

	/**
	 * ### Add multithreading
	 * 
	 * @param configurationFileName
	 * @return
	 * @throws IOException
	 */

	class RuleReader implements Runnable {

		private int id;

		public RuleReader(int id) {
			this.id = id;

		}

		public void run() {
			logger.info("Processor() CcVerifyCallRulesPatterns() Parsing Rule "
					+ "[" + id + "] ");
		}
	}

	private boolean CcVerifyFileCallRules(String configurationFileName)
			throws IOException {

		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(configurationFileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				strLine = strLine.trim();
				if (strLine.startsWith("#") || strLine.isEmpty()) {
					// logger.info ("Comment Found");
				} else {
					routePatterns.add(strLine);
				}
			}

			// Close the input stream
			in.close();

			logger.info("CcVerifyFileCallRules() Route patterns Total ("
					+ routePatterns.size() + ")");
			String[] routeType;
			int index = 1;

			/* given string will be split by the argument delimiter provided. */

			if (!routePatterns.isEmpty()) {

				// Start Multi-threaded engine
				ExecutorService executor = Executors
						.newFixedThreadPool(routePatterns.size());

				for (String route : routePatterns) {
					executor.submit(new RuleReader(index));
					
					routeType = route.split(DELIMITER);
					
					if (routeType.length > 2) 
					{
						logger.error("CcVerifyFileCallRules() Error Parsing Rule"
								+ "(" + index + ") ");
					} 
					else 
					{
						// Start thread
						if (CcVerifyRuleSyntax(index, 0, routeType[0].toString()) && 
						CcVerifyRuleSyntax(index, 1, routeType[1].toString())) 
						{
							if (CcVerifyRuleLogic(routeType[0].toString(),routeType[1].toString())) 
							{
								try {
									CcDigitAnalisysEngineInit(
											routeType[0].toString(),
											routeType[1].toString());
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
							}
						}
						} 
						else {
							logger.error("CcVerifyFileCallRules() Error in VerifyRuleType Rule"
									+ "(" + index + ") " + route + "\n");
						}
					}
				
					index++; // Check next rule
				
				}
				
				executor.shutdown();

			} 
			else 
			{
				logger.error("CcVerifyFileCallRules() No Route Patterns found!");
				return false;
			}
			
			return true;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// logConsole.error(e.getMessage());
			return false;
		}
	}

	/*
	 * Valid rule initialize Array
	 */

	private void CcDigitAnalisysEngineInit(String ruleType, String ruleValue)
			throws InterruptedException {

		String ruleNumber = null;
		String ruleValues[] = utilObj.getRuleValue(0, ruleValue);
		ruleNumber = ruleValues[1];
		logger.info("CcDigitAnalisysEngineInit() Inserting Route Pattern into DA engine: Rule Number ["
				+ Integer.parseInt(ruleNumber) + "]");
		callRoutingRules.put(new Integer(Integer.parseInt(ruleNumber)), ruleValue);

	}

	/**
	 * 
	 * @return
	 */

	public Map<Object, String> CcGetRules() throws InterruptedException {
		logger.info("CcGetRules() Displaying Valid Route Patterns");
	//	int totalRules = 0;

		Set<?> s = callRoutingRules.entrySet();
		Iterator<?> it = s.iterator();

		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry mapa = (Map.Entry) it.next(); // getKey is used to get key
			int key = (Integer) mapa.getKey(); // getValue is used to get value
			String value = (String) mapa.getValue();
		//	totalRules++;
			logger.info("CcGetRules() [" + key + "]\t Value: " + value); // key=value
																		
		}

		if (callRoutingRules.size() == 0) {
			logger.error("CcGetRules() No rules found!");
		}

		return callRoutingRules;

	}

	/**
	 * 
	 * @return
	 */

	public Map<Object, String> CcGetDbRules() {
		logger.info("CcGetDbRules() Displaying Valid Route Patterns");
		int totalRules = 0;

		Set<?> s = callRoutingRules.entrySet();
		Iterator<?> it = s.iterator();
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry mapa = (Map.Entry) it.next(); // getKey is used to get key
													// of Map
			int key = (Integer) mapa.getKey(); // getValue is used to get value
												// of key in Map
			String value = (String) mapa.getValue();
			totalRules++;
			logger.info("CcGetDbRules() [" + key + "]\t Value: " + value); // key=value
																			// separator
																			// this
																			// by
																			// Map.Entry
																			// to
																			// get
																			// key
																			// and
																			// value
		}
		if (totalRules == 0) {
			logger.error("CcGetDbRules() No rules found!");
		}

		return callRoutingRules;
	}

	/**
	 * 
	 * @param routeType
	 * @param routeValue
	 * @return
	 */

	private boolean CcVerifyRuleLogic(String routeType, String routeValue) {

		// logger.info("CcVerifyRuleLogic() Rule Type: " + routeType +
		// " Value: " + routeValue);
		String ruleNumber = null;
		String rulePriority = null;
		String ruleType = null;
		String ruleString = null;
		String ruleTrunk = null;
		String rulePort = null;

		if (utilObj.getTokenCount(routeValue) < TOKEN_COUNT
				&& utilObj.getTokenCount(routeValue) > TOKEN_COUNT + 1) {
			return false;
		}

		if (routeType.matches("ROUTE")) {
			logger.info("CcVerifyRuleLogic() Rule: " + routeValue + " Tokens: "
					+ utilObj.getTokenCount(routeValue));
			String ruleValues[] = utilObj.getRuleValue(0, routeValue);
			ruleNumber = ruleValues[1];
			rulePriority = ruleValues[2];
			ruleType = ruleValues[3];
			ruleString = ruleValues[4];
			ruleTrunk = ruleValues[5];
			if (utilObj.getTokenCount(routeValue) == 6) {
				rulePort = ruleValues[6];
			}

			if (ruleNumber != null && !ruleNumber.isEmpty()) {
				try {
					Integer.parseInt(ruleNumber);
					if (Integer.parseInt(ruleNumber) <= RULE_LIMIT) {
						// logger.info("CcVerifyRuleLogic() Token RULE NUMBER: "
						// + Integer.parseInt(ruleNumber));
					} else {
						logger.error("CcVerifyRuleLogic() Invalid Route Number: "
								+ Integer.parseInt(ruleNumber));
						return false;
					}
				} catch (NumberFormatException e) {
					logger.error("Invalid rule Number Value");
					return false;
				}
			}
			if (rulePriority != null && !rulePriority.isEmpty()) {
				try {
					Integer.parseInt(rulePriority);
					if (Integer.parseInt(rulePriority) >= PRIORITY_LOWER
							&& Integer.parseInt(rulePriority) <= PRIORITY_UPPER) {
						// logger.info("CcVerifyRuleLogic() Token RULE PRIORITY: "
						// + Integer.parseInt(rulePriority));
					} else {
						logger.error("CcVerifyRuleLogic() Invalid Route Priority: "
								+ Integer.parseInt(rulePriority));
						return false;
					}
				} catch (NumberFormatException e) {
					logger.error("Invalid rule Number Value");
					return false;
				}
			}

			if (ruleType != null && !ruleType.isEmpty()) {
				if (ruleType.equals("REGEX")) {
					// logger.info("CcVerifyRuleLogic() Token RULE TYPE: REGEX");
					if (ruleString != null && !ruleString.isEmpty()) {
						PatternSyntaxException exc = null;
						try {
							Pattern.compile(ruleString);
						} catch (PatternSyntaxException e) {
							exc = e;
						}

						if (exc != null) {
							exc.printStackTrace();
							return false;
						} else {
							// logger.info("CcVerifyRuleLogic() Token RULE STRING REGEX "
							// + ruleString + " is valid!");
						}
					}
				}

				else if (ruleType.equals("NUMERIC")) {
					// logger.info("CcVerifyRuleLogic() Token RULE TYPE: NUMERIC");

					if (ruleString.matches("(^(\\+)?[0-9]+([0-9]+)?)+")) {
						ruleString = ruleString.replace("+", "");
						if (ruleString != null && !ruleString.isEmpty()) {
							try {
								Long.parseLong(ruleString);
								// logger.info("CcVerifyRuleLogic() Token RULE STRING: "
								// + Long.parseLong(ruleString));
							} catch (NumberFormatException e) {
								logger.error("Invalid rule NUMERIC must contain only numbers: "
										+ ruleString);
								return false;
							}
						}
					} else {
						logger.error("Is an invalid CcProcessRulesNumericCdcc rule");
						return false;
					}

				} else if (ruleType.equals("WILDCARD")) {
					// logger.info("CcVerifyRuleLogic() Token RULE TYPE: WILDCARD");

					if (ruleString != null && !ruleString.isEmpty()) {
						String newRuleString = utilObj.getWildCard(ruleString);
						// logger.info("CcVerifyRuleLogic() WildCard Internal Value: "
						// + newRuleString);
						PatternSyntaxException exc = null;
						try {
							Pattern.compile(newRuleString);
						} catch (PatternSyntaxException e) {
							exc = e;
						}

						if (exc != null) {
							exc.printStackTrace();
							return false;
						} else {
							// logger.info("CcVerifyRuleLogic() Token RULE STRING WILDCARD "
							// + ruleString + " is valid!");
						}
					}
				} else {
					logger.error("CcVerifyRuleLogic() " + routeValue
							+ " is INVALID");
					return false;
				}
			}

			if (ruleTrunk != null && !ruleTrunk.isEmpty()) {
				if (ruleTrunk.equals("_DNS_")) {
					// logger.info("CcVerifyRuleLogic() Token RULE TRUNK is DNS: "
					// + ruleTrunk);
				} else if (CcUtils.isValidIP(ruleTrunk)
						|| CcUtils.isValidHostName(ruleTrunk)) {
					// logger.info("CcVerifyRuleLogic() Token RULE TRUNK: " +
					// ruleTrunk);
				} else {
					return false;
				}
			}

			if (utilObj.getTokenCount(routeValue) == 6) {

				if (rulePort != null && !rulePort.isEmpty()) {
					if (ruleType.equals("_DNS_")) {
						// logger.info("CcVerifyRuleLogic() Token RULE PORT " +
						// rulePort + "INVALID when using _DNS_ type");
						return false;
					} else {
						// logger.info("CcVerifyRuleLogic() Token RULE PORT: " +
						// rulePort);
					}
				}
			}

		}
		return true;
	}

	/**
	 * 
	 * @param configurationParameter
	 * @return
	 */

	private boolean CcVerifyRuleSyntax(int ruleNumber, int paramNumber,
			String configurationParameter) {

		logger.info("CcVerifyRuleSyntax() Rule Number " + "[" + ruleNumber
				+ "] Value: " + configurationParameter);
		if (paramNumber == 0
				&& (configParams.contains(configurationParameter) || mandatoryConfigParamsRules
						.contains(configurationParameter))) {
			logger.info("CcVerifyRuleSyntax() Valid Parameter Rule Type: "
					+ configurationParameter);
			return true;
		} else if (paramNumber == 1) {
			// logger.info("Before: " + configurationParameter);
			// Remove () from Rule
			configurationParameter = configurationParameter.replaceAll(
					"\\(\"|\"\\)", "\"");
			logger.info("CcVerifyRuleSyntax() Rule Value: "
					+ configurationParameter);
			if (!configurationParameter.contains("\\(\"")
					&& !configurationParameter.contains("\"\\)")) {
				logger.info("Processing Tokens: " + configurationParameter);
				StringTokenizer st = new StringTokenizer(
						configurationParameter, ",");
				if (st.countTokens() == TOKEN_COUNT
						|| st.countTokens() == TOKEN_COUNT + 1) {
					logger.info("CcVerifyRuleSyntax() Processing Tokens: ["
							+ st.countTokens() + "]");
					int tokenIndex = 1;
					while (st.hasMoreElements()) {
						String token = st.nextElement().toString();
						token = token.replaceAll("\"", "");

						if (token.isEmpty() && tokenIndex != 6) { // PORT CAN BE
																	// EMPTY
							return false;
						}
						if (tokenIndex == 1) { // RULE NUMBER
							try {
								Integer.parseInt(token);
								logger.info("Token [" + tokenIndex + "]: "
										+ Integer.parseInt(token));
							} catch (NumberFormatException e) {
								logger.error("Invalid rule Number Value");
								return false;
							}

						}
						if (tokenIndex == 2) { // PRIORITY

							try {
								if (Integer.parseInt(token) >= 0
										&& Integer.parseInt(token) <= 100
										&& (!token.isEmpty()))
									logger.info("Token (" + tokenIndex + "): "
											+ Integer.parseInt(token));
								else {
									logger.error("Invalid Priority Value RULE "
											+ ruleNumber);
									return false;
								}
							} catch (NumberFormatException e) {
								logger.error("Invalid Priority Value RULE "
										+ ruleNumber);
								return false;
							}

						}
						if (tokenIndex == 3) { // TYPE

							if (token.matches("REGEX")
									|| token.matches("NUMERIC")
									|| token.matches("WILDCARD"))
								logger.info("Token (" + tokenIndex + "): "
										+ token);
							else {
								logger.error("Invalid Type Value");
								return false;
							}
						}
						if (tokenIndex == 4) { // STRING
							logger.info("Token (" + tokenIndex + "): " + token);
						}
						if (tokenIndex == 5) { // TRUNK
							if (CcUtils.isValidIP(token)
									|| CcUtils.isValidHostName(token)
									|| token.matches("_DNS_"))
								logger.info("Token (" + tokenIndex + "): "
										+ token);

						}
						if (tokenIndex == 6) { // PORT
							try {
								if (Integer.parseInt(token) >= START_PORT
										&& Integer.parseInt(token) <= END_PORT)
									logger.info("Token (" + tokenIndex + "): "
											+ Integer.parseInt(token));
								else
									logger.error("Invalid Port Value");
							} catch (NumberFormatException e) {
								logger.error("Invalid Port Value");
								return false;
							}
						}
						tokenIndex++;
					}
					return true;
				} else {

					return false;
				}
			} else {
				return false;
			}
		} else {
			logger.error("CcVerifyRuleSyntax() Error in Rule Number " + "["
					+ ruleNumber + "] Value: " + configurationParameter);
			return false;
		}
	}

	

}
