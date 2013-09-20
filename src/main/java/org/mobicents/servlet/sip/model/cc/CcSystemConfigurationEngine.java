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
	private static int RULE_TOKEN_COUNT = 5;
	private static int RULE_TOKEN_MAX   = 7;
	private static int TRANSFORM_TOKEN_COUNT   = 7;
	private static int START_PORT = 1;
	private static int END_PORT = 65535;
	private static int RULE_LIMIT = 100;
	private static int PRIORITY_LOWER = 1;
	private static int PRIORITY_UPPER = 100;
	private CcUtils utilObj = new CcUtils();
	
	private ArrayList<String> configParams = new ArrayList<String>(); // Clean
	private ArrayList<String> mandatoryConfigParamsRules = new ArrayList<String>();
	private ArrayList<String> mandatoryConfigParamsTransforms = new ArrayList<String>();
	private ArrayList<String> mandatoryConfigParamsRouteList = new ArrayList<String>();
	private ArrayList<String> candidateRoutePatterns = new ArrayList<String>();
	private ArrayList<String> routePatterns = new ArrayList<String>();
	private ArrayList<String> candidateTransformPatterns = new ArrayList<String>();
	private ArrayList<String> transformPatterns = new ArrayList<String>();
	private ArrayList<String> candidateRouteLists = new ArrayList<String>();
	private ArrayList<String> routeLists = new ArrayList<String>();
	
	private Map<Object, String> callTransformRules = new HashMap<Object, String>();
	private Map<Object, String> routeListRules = new HashMap<Object, String>();
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

	/**
	 *  Verify File is readable
	 * 
	 */
	
	public boolean CcStartCallTransformsEngine(String fileName) {
		CONFIGURATION_FILE = fileName;
		logger.info("CcSystemConfigurationEngine() CcStartFileEngine initializing...Mode: ["
				+ ACCESS_MODE + "]" + " CALL TRANSFORMRULES:  " + CONFIGURATION_FILE);
		if (CcReadConfigurationFiles(CONFIGURATION_FILE,1))
			return true;
		else
			return false;
	}
	
	public boolean CcStartRouteListEngine(String fileName) {
		CONFIGURATION_FILE = fileName;
		logger.info("CcSystemConfigurationEngine() CcStartFileEngine initializing...Mode: ["
				+ ACCESS_MODE + "]" + " CALL ROUTELISTS:  " + CONFIGURATION_FILE);
		if (CcReadConfigurationFiles(CONFIGURATION_FILE,2))
			return true;
		else
			return false;
	}
	
	public boolean CcStartCallRulesEngine(String fileName) {
		CONFIGURATION_FILE = fileName;
		logger.info("CcSystemConfigurationEngine() CcStartFileEngine initializing...Mode: ["
				+ ACCESS_MODE + "]" + " CALL RULES:  " + CONFIGURATION_FILE);
		if (CcReadConfigurationFiles(CONFIGURATION_FILE,3))
			return true;
		else
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

			/* Initialize system parameters */
			CcInitConfigurationValues(true,4);

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

	private boolean CcReadConfigurationFiles(String fileName,int type) {
		logger.info("CcSystemConfigurationEngine() CcStartReadConfigurationFile CONFIGURATION_FILE: "
				+ fileName);
		
		// Call Transforms
		if (type==1) {
			CcInitConfigurationValues(true,type);
			if (CcInitConfigurationFile(type))
				return true;
			else
				return false;
		}	
		// Call Route Lists
		else if (type == 2) {
			CcInitConfigurationValues(true,type);
			if (CcInitConfigurationFile(type))
				return true;
			else
				return false;
		}	
		
		// Call Rules
		else if (type == 3) {
			CcInitConfigurationValues(true,type);
			if (CcInitConfigurationFile(type))
				return true;
			else
				return false;
		}	
		else 
			return false;
		
		
	}

	/**
	 * 
	 * @return
	 */

	private boolean CcInitConfigurationFile(int type) {
		logger.info("CcSystemConfigurationEngine() CcInitConfigurationFile initializing...");
		try {
			if (CcVerifyConfigurationFileAccess(CONFIGURATION_FILE)) 
			{ 			
				if (type == 1) {
					//TODO CHECK TRANSFORM SYNTAX
					if (!CcReadFileParameters(CONFIGURATION_FILE,type))	{ // Read Call Rules File																
						return false;
					}
					if (!CcValidateFileTransformRules(CONFIGURATION_FILE))	{ // Validate File Call Rule Parameters															
						return false;
					}
					return true;
					
				}	
				else if (type == 2) {
					//TODO CHECK ROUTELIST SYNTAX
						
				}
				else if (type == 3) {
					
					// CHECK ROUTES SYNTAX
					if (!CcReadFileParameters(CONFIGURATION_FILE,type))	{ // Read Call Rules File																
						return false;
					}
					if (!CcValidateFileCallRules(CONFIGURATION_FILE))	{ // Validate File Call Rule Parameters															
						return false;
					}
					return true;
				}
				else {
					
					return false;
				}
				
			
			} 
			else {			
				return false;
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		
		return false;
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

	private void CcInitConfigurationValues(boolean startInit,int type) {
		logger.info("CcSystemConfigurationEngine() CcInitConfigurationValues initializing...");
		
		if (startInit) {
			logger.info("CcSystemConfigurationEngine() CcInitConfigurationValues (Mandatory) initializing...");
			if (type == 1)
				mandatoryConfigParamsTransforms.add("TRANSFORM");
			else if (type == 2)
				mandatoryConfigParamsRouteList.add("ROUTELIST");
			else if (type == 3)
				mandatoryConfigParamsRules.add("ROUTE");
			else if (type == 4) {
				logger.info("CcInitConfigurationValues Database opencall read.");
				mandatoryConfigParamsRules.add("ROUTE");
				mandatoryConfigParamsTransforms.add("TRANSFORM");
				mandatoryConfigParamsRouteList.add("ROUTELIST");
			}	
			else
				return;
		}

	}

	/**
	 * 
	 * @param configurationFileName
	 * @throws IOException
	 */

	private boolean CcReadFileParameters(String configurationFileName,int type)
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
					if (type == 1)
						candidateTransformPatterns.add(strLine);
					else if (type == 2)
						candidateRouteLists.add(strLine);
					else if (type == 3)
						candidateRoutePatterns.add(strLine);
					else
						return false;
				}
			}

			// Close the Input stream
			in.close();
			CcDisplayCandidateRules(type);
			return true;

		} catch (FileNotFoundException e) {
			
			logger.error("CcReadFileParameters() " + e.getMessage());
			e.printStackTrace();
			return false;
		}

	}

	private boolean CcDisplayCandidateRules(int type) throws IOException {
		
		if (type == 1) {
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
		else if (type == 2){
			logger.info("CcDisplayCandidateRules() Candidate Transform patterns");
			
			for (String r : candidateTransformPatterns) {
				logger.info(r);
			}
			return true;
		}
		else if (type == 3){
			logger.info("CcDisplayCandidateRules() Candidate Route Lists");
			for (String r : candidateRouteLists) {
				logger.info(r);
			}
			return true;
		}
		else {
			return false;
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
					if (CcVerifyCallRuleSyntax(index, 0, routeType[0].toString())
							&& CcVerifyCallRuleSyntax(index, 1,
									routeType[1].toString())) {
						if (CcVerifyCallRuleLogic(routeType[0].toString(),
								routeType[1].toString())) {
							try {
								// Insert Valid rules to Digit Analysis Engine
								CcInsertRulesDigitAnalisysEngine(
										routeType[0].toString(),
										routeType[1].toString(),4);
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
	 * ### Add Multithreading
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
			logger.info("Processor() CcVerifyConfiguration Parameters() Parsing Rule "
					+ "[" + id + "] ");
		}
	}

	
	/*
	 * 
	 */
	
	
	private boolean CcValidateFileTransformRules(String configurationFileName)
			throws IOException {
		
		try {
			
			// Open configurationFileName 
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
					transformPatterns.add(strLine);
				}
			}

			// Close the input stream
			in.close();

			logger.info("CcVerifyFileTransformRules() Transform patterns Total ("
					+ transformPatterns.size() + ")");
			String[] transformType;
			int index = 1;

			/* given string will be split by the argument delimiter provided. */

			if (!transformPatterns.isEmpty()) {

				// Start Multithread engine
				
				ExecutorService executor = Executors
						.newFixedThreadPool(transformPatterns.size());

				for (String transform : transformPatterns) {
					executor.submit(new RuleReader(index));
					
					transformType = transform.split(DELIMITER);
					
					if (transformType.length > 2) 
					{
						logger.error("CcVerifyFileTransformRules() Error Parsing Rule"
								+ "(" + index + ") ");
					} 
					else 
					{
						
						if (CcVerifyTransformRuleSyntax(index, 0, transformType[0].toString()) && 
								CcVerifyTransformRuleSyntax(index, 1, transformType[1].toString())) 
						{
							if (CcVerifyTransformRuleLogic(transformType[0].toString(),transformType[1].toString())) 
							{
								try {
									CcInsertRulesDigitAnalisysEngine(
											transformType[0].toString(),
											transformType[1].toString(),1);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									logger.error("CcVerifyFileTransformRules() Unable to initalize engine");
							}
						}
						} 
						else {
							logger.error("CcVerifyFileTransformRules() Error in VerifyRuleSyntax Rule"
									+ "(" + index + ") " + transform + "\n");
						}
					}			
					index++; // Check next rule
				}
				
				executor.shutdown();

			} 
			else 
			{
				logger.error("CcVerifyFileTransformRules() No Transform Patterns found!");
				return true;
			}
			
			return true;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// logConsole.error(e.getMessage());
			return false;
		}
		
	}
	
	
	@SuppressWarnings("unused")
	private boolean CcVerifyFileRouteListRules(String configurationFileName)
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
					routeLists.add(strLine);
				}
			}

			// Close the input stream
			in.close();

			logger.info("CcVerifyFileRouteListRules() Route Lists Total ("
					+ routeLists.size() + ")");
			String[] routeType;
			int index = 1;

			/* given string will be split by the argument delimiter provided. */

			if (!routeLists.isEmpty()) {

				// Start Multi-threaded engine
				ExecutorService executor = Executors
						.newFixedThreadPool(routeLists.size());

				for (String route : routeLists) {
					executor.submit(new RuleReader(index));
					
					routeType = route.split(DELIMITER);
					
					if (routeType.length > 2) 
					{
						logger.error("CcVerifyFileRouteListRules() Error Parsing Rule"
								+ "(" + index + ") ");
					} 
					else 
					{
						// Start thread
						if (CcVerifyCallRuleSyntax(index, 0, routeType[0].toString()) && 
						CcVerifyCallRuleSyntax(index, 1, routeType[1].toString())) 
						{
							if (CcVerifyCallRuleLogic(routeType[0].toString(),routeType[1].toString())) 
							{
								try {
									CcInsertRulesDigitAnalisysEngine(
											routeType[0].toString(),
											routeType[1].toString(),2);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
							}
						}
						} 
						else {
							logger.error("CcVerifyFileRouteListRules() Error in VerifyRuleType Rule"
									+ "(" + index + ") " + route + "\n");
						}
					}			
					index++; // Check next rule
				}
				
				executor.shutdown();

			} 
			else 
			{
				logger.error("CcVerifyFileRouteListRules() No Route Lists found!");
				return false;
			}
			
			return true;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// logConsole.error(e.getMessage());
			return false;
		}
	}
	
	/**
	 * 
	 * 
	 * @param configurationFileName
	 * @return
	 * @throws IOException
	 */
	
	private boolean CcValidateFileCallRules(String configurationFileName)
			throws IOException {

		try {
			
			// Open configurationFileName
			
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

			/* Given string will be split by the argument delimiter provided. */

			if (!routePatterns.isEmpty()) {

				// Start Multithreaded engine
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
						// routeType[0] = ROUTE 
						// routeType[1] = ("8","50","WILDCARD","20X","110.10.0.210","5061","TLS")
						
						if (CcVerifyCallRuleSyntax(index, 0, routeType[0].toString()) && 
						CcVerifyCallRuleSyntax(index, 1, routeType[1].toString())) 
						{
							if (CcVerifyCallRuleLogic(routeType[0].toString(),routeType[1].toString())) 
							{
								try {
									CcInsertRulesDigitAnalisysEngine(
											routeType[0].toString(),
											routeType[1].toString(),3);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
							}
						}
						} 
						else {
							logger.error("CcVerifyFileCallRules() Error in CcVerifyCallRuleSyntax Rule"
									+ "(" + index + ") " + route);
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
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	
	/*
	 * Valid rule initialize Array
	 */

	private void CcInsertRulesDigitAnalisysEngine(String ruleType, String ruleValue,int type)
			throws InterruptedException {

		String ruleNumber = null;
		String ruleValues[] = null;
		String ruleEnabled = null;
		
		if (type == 1) {
			ruleValues  = utilObj.getTransformValue(0, ruleValue);
			ruleNumber  = ruleValues[1];
			ruleEnabled = ruleValues[2];
			
			ruleEnabled = ruleEnabled.toUpperCase();
			
			if (ruleEnabled.matches("TRUE")) {
				logger.info("CcDigitAnalisysEngineInit() Inserting TransformRule into DA engine. Rule Number ["
						+ Integer.parseInt(ruleNumber) + "]");
				
				callTransformRules.put(new Integer(Integer.parseInt(ruleNumber)), ruleValue);
			}
			else {
				logger.info("CcDigitAnalisysEngineInit() Rule Number ["
						+ Integer.parseInt(ruleNumber) + "] is disabled");
				
			}
			
		}
		else if (type == 2) {
			logger.info("CcDigitAnalisysEngineInit() Inserting RouteList into DA engine. Rule Number ["
					+ Integer.parseInt(ruleNumber) + "]");
			routeListRules.put(new Integer(Integer.parseInt(ruleNumber)), ruleValue);
		}
		
		else if (type == 3) {
			ruleValues = utilObj.getRuleValue(0, ruleValue);
			ruleNumber = ruleValues[1];
			logger.info("CcDigitAnalisysEngineInit() Inserting CallRule into DA engine. Rule Number ["
					+ Integer.parseInt(ruleNumber) + "]");
			callRoutingRules.put(new Integer(Integer.parseInt(ruleNumber)), ruleValue);
		}
		else if (type == 4) {
			logger.info("CcDigitAnalisysEngineInit() Inserting DB CallRule into DA engine. Rule Number ["
					+ Integer.parseInt(ruleNumber) + "]");
			
		}
		else {
			logger.error("CcInsertRulesDigitAnalisysEngine() Invalid type");
		}
	}

	/**
	 * 
	 * @return
	 */

	public Map<Object, String> CcGetTransformRules() throws InterruptedException {
		logger.info("CcGetTransformRules() Displaying Valid Transform Patterns");
		

		Set<?> s = callTransformRules.entrySet();
		Iterator<?> it = s.iterator();

		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry mapa = (Map.Entry) it.next(); // getKey is used to get key
			int key = (Integer) mapa.getKey(); // getValue is used to get value
			String value = (String) mapa.getValue();
			logger.info("CcGetTransformRules() [" + key + "]\t Value: " + value); // key=value
																		
		}

		if (callTransformRules.size() == 0) {
			logger.error("CcGetTransformRules() No rules found!");
		}

		return callTransformRules;

	}
	
	
	/**
	 * 
	 * @return
	 */

	public Map<Object, String> CcGetRouteListsRules() throws InterruptedException {
		logger.info("CcGetRouteListsRules() Displaying Valid Route Lists Patterns");
		

		Set<?> s = routeListRules.entrySet();
		Iterator<?> it = s.iterator();

		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry mapa = (Map.Entry) it.next(); // getKey is used to get key
			int key = (Integer) mapa.getKey(); // getValue is used to get value
			String value = (String) mapa.getValue();
		//	totalRules++;
			logger.info("CcGetRouteListsRules() [" + key + "]\t Value: " + value); // key=value
																		
		}

		if (routeListRules.size() == 0) {
			logger.error("CcGetRouteListsRules() No rules found!");
		}

		return routeListRules;

	}
	
	/**
	 * 
	 * @return
	 */

	public Map<Object, String> CcGetCallRules() throws InterruptedException {
		logger.info("CcGetCallRules() Displaying Valid Route Patterns");
		

		Set<?> s = callRoutingRules.entrySet();
		Iterator<?> it = s.iterator();

		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry mapa = (Map.Entry) it.next(); // getKey is used to get key
			int key = (Integer) mapa.getKey(); // getValue is used to get value
			String value = (String) mapa.getValue();
		//	totalRules++;
			logger.info("CcGetCallRules() [" + key + "]\t Value: " + value); // key=value
																		
		}

		if (callRoutingRules.size() == 0) {
			logger.error("CcGetCallRules() No rules found!");
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
	 * @param configurationParameter
	 * @return
	 */

	private boolean CcVerifyCallRuleSyntax(int ruleNumber, int paramNumber,
			String configurationParameter) {
		// Display Rule number
		logger.info("CcVerifyCallRuleSyntax() Rule Number " + "[" + ruleNumber
				+ "] Value: " + configurationParameter);
		
		// Display Valid Parameter number
		if (paramNumber == 0
				&& (configParams.contains(configurationParameter) || mandatoryConfigParamsRules
						.contains(configurationParameter))) {
			logger.info("CcVerifyCallRuleSyntax() Valid Parameter Rule Type: "
					+ configurationParameter);
			return true;
			
		} else if (paramNumber == 1) {
			// Replace initial ( and last ) with " " respectively from Transform Rule: ("8","50","WILDCARD","20X","110.10.0.210","5061","TLS")

			
			configurationParameter = configurationParameter.replaceAll(
					"\\(\"|\"\\)", "\"");
			
			logger.info("CcVerifyCallRuleSyntax() Rule Value: "	+ configurationParameter);
			
			if (!configurationParameter.contains("\\(\"")
					&& !configurationParameter.contains("\"\\)")) {
				
				logger.info("CcVerifyCallRuleSyntax() Processing Tokens: " + configurationParameter);
				
				StringTokenizer st = new StringTokenizer(
						configurationParameter, ",");
				
				// Count number of Tokens
				
				if (st.countTokens() >= RULE_TOKEN_COUNT
						&& st.countTokens() <= RULE_TOKEN_MAX) {
					
					logger.info("CcVerifyCallRuleSyntax() Processing Tokens: ["
							+ st.countTokens() + "]");
					int tokenIndex = 1;
					
					while (st.hasMoreElements()) {
						
						String token = st.nextElement().toString();
						token = token.replaceAll("\"", "");

						if (token.isEmpty() && tokenIndex != RULE_TOKEN_MAX) { // PORT|TRANSPORT CAN BE EMPTY							
							return false;
						}
						
						if (tokenIndex == 1) { // RULE NUMBER
							try {
								Integer.parseInt(token);
								logger.info("Token (" + tokenIndex + "): "
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
							if (CcUtils.isValidIP(token) || CcUtils.isValidHostName(token) || token.matches("_DNS_"))
								logger.info("Token (" + tokenIndex + "): " + token);

						}
						if (tokenIndex == 6) { // PORT or TRANSPORT
							try {
								if (java.util.regex.Pattern.matches("\\d+", token)) {
									if (Integer.parseInt(token) >= START_PORT
											&& Integer.parseInt(token) <= END_PORT) {
										logger.info("Token (" + tokenIndex + "): "
												+ Integer.parseInt(token));
									}	
									else {
										logger.error("Invalid Port Value");
									}	
								}			
								else if (CcUtils.isValidTransport(token)) {
									 logger.info("Token (" + tokenIndex + "): "
												+ token);
								} 
								else {
									return false;
								}
								
							} catch (Exception e) {
								logger.error("Invalid Value");
								return false;
							}
						}
						if (tokenIndex == 7) { // TRANSPORT
							if (CcUtils.isValidTransport(token)) {
								logger.info("Token (" + tokenIndex + "): "
										+ token);
							} else {
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
			logger.error("CcVerifyCallRuleSyntax() Error in Rule Number " + "["
					+ ruleNumber + "] Value: " + configurationParameter);
			return false;
		}
	}

	/**
	 * 
	 * @param routeType
	 * @param routeValue
	 * @return
	 */

	private boolean CcVerifyCallRuleLogic(String routeType, String routeValue) {

		// logger.info("CcVerifyCallRuleLogic() Rule Type: " + routeType +
		// " Value: " + routeValue);
		String ruleNumber = null;
		String rulePriority = null;
		String ruleType = null;
		String ruleString = null;
		String ruleTrunk = null;
		String rulePort = null;
		String ruleTransport = null;

		if (utilObj.getTokenCount(routeValue) < RULE_TOKEN_COUNT
				|| utilObj.getTokenCount(routeValue) > RULE_TOKEN_MAX) {
			return false;
		}

		if (routeType.matches("ROUTE")) {
			logger.info("CcVerifyCallRuleLogic() Rule: " + routeValue + " Tokens: "
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
			if (utilObj.getTokenCount(routeValue) == 7) {
				ruleTransport = ruleValues[7];
				
			}

			if (ruleNumber != null && !ruleNumber.isEmpty()) {
				try {
					Integer.parseInt(ruleNumber);
					if (Integer.parseInt(ruleNumber) <= RULE_LIMIT) {
						// logger.info("CcVerifyCallRuleLogic() Token RULE NUMBER: "
						// + Integer.parseInt(ruleNumber));
					} else {
						logger.error("CcVerifyCallRuleLogic() Invalid Route Number: "
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
						// logger.info("CcVerifyCallRuleLogic() Token RULE PRIORITY: "
						// + Integer.parseInt(rulePriority));
					} else {
						logger.error("CcVerifyCallRuleLogic() Invalid Route Priority: "
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
					// logger.info("CcVerifyCallRuleLogic() Token RULE TYPE: REGEX");
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
							// logger.info("CcVerifyCallRuleLogic() Token RULE STRING REGEX "
							// + ruleString + " is valid!");
						}
					}
				}

				else if (ruleType.equals("NUMERIC")) {
					// logger.info("CcVerifyCallRuleLogic() Token RULE TYPE: NUMERIC");

					if (ruleString.matches("(^(\\+)?[0-9]+([0-9]+)?)+")) {
						ruleString = ruleString.replace("+", "");
						if (ruleString != null && !ruleString.isEmpty()) {
							try {
								Long.parseLong(ruleString);
								// logger.info("CcVerifyCallRuleLogic() Token RULE STRING: "
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
					// logger.info("CcVerifyCallRuleLogic() Token RULE TYPE: WILDCARD");

					if (ruleString != null && !ruleString.isEmpty()) {
						String newRuleString = utilObj.getWildCard(ruleString);
						// logger.info("CcVerifyCallRuleLogic() WildCard Internal Value: "
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
							// logger.info("CcVerifyCallRuleLogic() Token RULE STRING WILDCARD "
							// + ruleString + " is valid!");
						}
					}
				} else {
					logger.error("CcVerifyCallRuleLogic() " + routeValue
							+ " is INVALID");
					return false;
				}
			}

			if (ruleTrunk != null && !ruleTrunk.isEmpty()) {
				if (ruleTrunk.equals("_DNS_")) {
					// logger.info("CcVerifyCallRuleLogic() Token RULE TRUNK is DNS: "
					// + ruleTrunk);
				} 
				else if (ruleTrunk.equals("_TWILIO_")) {
					// logger.info("CcVerifyCallRuleLogic() Token RULE TRUNK is DNS: "
					// + ruleTrunk);
				} 
				else if (CcUtils.isValidIP(ruleTrunk)
						|| CcUtils.isValidHostName(ruleTrunk)) {
					// logger.info("CcVerifyCallRuleLogic() Token RULE TRUNK: " +
					// ruleTrunk);
				} else {
					return false;
				}
			}
			
			// PORT or TRANSPORT		
			if (rulePort != null && !rulePort.isEmpty()) {			
				
				if (ruleTrunk.equals("_DNS_") || ruleTrunk.equals("_TWILIO_")  ) {
						 logger.warn("CcVerifyCallRuleLogic() Token: " +
						  rulePort + " is INVALID when using _DNS_ type");
						return false;
				} 
				//Assign rulePort as Transport
			    if (CcUtils.isValidTransport(rulePort)) {
						logger.info("CcVerifyCallRuleLogic() Token Rule Transport: " +
								rulePort);
				}
				
			}
			
			// TRANSPORT
			if (ruleTransport != null && !ruleTransport.isEmpty()) {		
				// We can't set the transport in a DNS request
				// TODO  DE1 Future release, receive DNS response and filter based on transport
				
				if (CcUtils.isValidTransport(ruleTransport)) {
					 logger.info("CcVerifyCallRuleLogic() Token Rule Transport: " +
							 ruleTransport);
					 
					 if (ruleTrunk.equals("_DNS_")) {
						 logger.warn("CcVerifyCallRuleLogic() Can't use DNS with Token Rule TRANSPORT: " +
								 ruleTransport);
							return false;
					 }
					 
					 if (ruleTrunk.equals("_TWILIO_")) {
						 logger.warn("CcVerifyCallRuleLogic() Can't use Twilio with Token Rule TRANSPORT: " +
								 ruleTransport);
							return false;
					 }
				} else {
					return false;
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

	private boolean CcVerifyTransformRuleSyntax(int ruleNumber, int paramNumber,
			String configurationParameter) {
		// Display Transform number
		logger.info("CcVerifyTransformRuleSyntax() Rule Number " + "[" + ruleNumber
				+ "] Value: " + configurationParameter);
		
		// Display Valid Parameter number
		if (paramNumber == 0
				&& (configParams.contains(configurationParameter) || mandatoryConfigParamsTransforms
						.contains(configurationParameter))) {
			logger.info("CcVerifyTransformRuleSyntax() Valid Parameter Transform Type: "
					+ configurationParameter);
			return true;
			
		} else if (paramNumber == 1) {
			
			// Replace initial ( and last ) with " " respectively from Transform Rule: ("1","WILDCARD","+!","011!","CALLED","FALSE","TRUE")		
			configurationParameter = configurationParameter.replaceAll(
					"\\(\"|\"\\)", "\"");
			
			logger.info("CcVerifyTransformRuleSyntax() Transform Value: "	+ configurationParameter);
			
			if (!configurationParameter.contains("\\(\"")
					&& !configurationParameter.contains("\"\\)")) {
				
				logger.info("CcVerifyTransformRuleSyntax() Processing Tokens: " + configurationParameter);
				
				StringTokenizer st = new StringTokenizer(
						configurationParameter, ",");
				
				// Count number of Tokens
				
				if (st.countTokens() == TRANSFORM_TOKEN_COUNT) {
					
					logger.info("CcVerifyTransformRuleSyntax() Processing Tokens: ["
							+ st.countTokens() + "]");
					int tokenIndex = 1;
					
					while (st.hasMoreElements()) {
						
						String token = st.nextElement().toString();
						token = token.replaceAll("\"", "");
						
						
						if (token.isEmpty() && tokenIndex != TRANSFORM_TOKEN_COUNT) { // All Paramaters must have a value							
							logger.warn("Invalid Transform rule " + configurationParameter );
							return false;
						}
						
						if (tokenIndex == 1) { // RULE NUMBER
							try {
								Integer.parseInt(token);
								logger.info("Token (" + tokenIndex + "): "
										+ Integer.parseInt(token));
							} catch (NumberFormatException e) {
								logger.error("Invalid rule Number Value");
								return false;
							}

						}
						if (tokenIndex == 2) { // TRANSFORM RULE IS ENABLED
							token = token.toUpperCase();
							if (token.matches("FALSE")
									|| token.matches("TRUE"))
								logger.info("Token (" + tokenIndex + "): "
										+ token);
							else {
								logger.error("Invalid Type Value: " + token);
								return false;
							}
						}
						if (tokenIndex == 3) { // TYPE
							token = token.toUpperCase();
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
						if (tokenIndex == 4) { // ORIGINAL VALUE
							logger.info("Token (" + tokenIndex + "): " + token);
						}
						if (tokenIndex == 5) { // MATCHING VALUE
							logger.info("Token (" + tokenIndex + "): " + token);
						}
						
						if (tokenIndex == 6) { // NUMBER TYPE 
							token = token.toUpperCase();
							if (token.matches("CALLED")
									|| token.matches("CALLING")
									|| token.matches("REDIRECT"))
								logger.info("Token (" + tokenIndex + "): "
										+ token);
							else {
								logger.error("Invalid Type Value");
								return false;
							}
						}
						if (tokenIndex == 7) { // BLOCK CALLS
							token = token.toUpperCase();
							if (token.matches("FALSE")
									|| token.matches("TRUE"))
								logger.info("Token (" + tokenIndex + "): "
										+ token);
							else {
								logger.error("Invalid Type Value");
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
			logger.error("CcVerifyTransformRuleSyntax() Error in Rule Number " + "["
					+ ruleNumber + "] Value: " + configurationParameter);
			return false;
		}
	}

	
	/**
	 * 
	 * @param routeType
	 * @param routeValue
	 * @return
	 */

	private boolean CcVerifyTransformRuleLogic(String transformRuleType, String transformValue) {

		// logger.info("CcVerifyTransformRuleLogic() Rule Type: " + routeType +
		// " Value: " + routeValue);
		String transformNumber = null;
		String transformEnabled = null;
		String transformType = null;
		String transformSrcString = null;
		String transformDestString = null;
		String transformApplyto	= null;
		String transformBlock = null;
		

		if (utilObj.getTokenCount(transformValue) != TRANSFORM_TOKEN_COUNT) {
			return false;
		}

		if (transformRuleType.matches("TRANSFORM")) {
			logger.info("CcVerifyTransformRuleLogic() Rule: " + transformValue + " Tokens: "
					+ utilObj.getTokenCount(transformValue));
			
			String ruleValues[] = utilObj.getTransformValue(0, transformValue);
			
			transformNumber = ruleValues[1];
			transformEnabled = ruleValues[2];
			transformType = ruleValues[3];
			transformSrcString = ruleValues[4];
			transformDestString = ruleValues[5];
			transformApplyto = ruleValues[6];
			transformBlock = ruleValues[7];
			

			if (transformNumber != null && !transformNumber.isEmpty()) {
				try {
					Integer.parseInt(transformNumber);
					if (Integer.parseInt(transformNumber) <= RULE_LIMIT) {
						// logger.info("CcVerifyCallRuleLogic() Token RULE NUMBER: "
						// + Integer.parseInt(ruleNumber));
					} else {
						logger.error("CcVerifyTransformRuleLogic() Invalid Route Number: "
								+ Integer.parseInt(transformNumber));
						return false;
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
					logger.error("Invalid rule Number Value");
					return false;
				}
			}
			
			if (transformEnabled != null && !transformEnabled.isEmpty()) {
				transformEnabled = transformEnabled.toUpperCase();
				if (transformEnabled.equals("TRUE") || transformEnabled.equals("FALSE")) {
					return true;
				}
				else {
					return false;
				}
				
			}

			if (transformType != null && !transformType.isEmpty()) {
				if (transformType.equals("REGEX")) {
					// logger.info("CcVerifyTransformRuleLogic() Token RULE TYPE: REGEX");
					if (transformSrcString != null && !transformSrcString.isEmpty()) {
						PatternSyntaxException exc = null;
						try {
							Pattern.compile(transformSrcString);
						} catch (PatternSyntaxException e) {
							exc = e;
						}

						if (exc != null) {
							exc.printStackTrace();
							return false;
						} else {
							// logger.info("CcVerifyTransformRuleLogic() Token RULE STRING REGEX "
							// + ruleString + " is valid!");
						}
					}
					if (transformDestString != null && !transformDestString.isEmpty()) {
						PatternSyntaxException exc = null;
						try {
							Pattern.compile(transformDestString);
						} catch (PatternSyntaxException e) {
							exc = e;
						}

						if (exc != null) {
							exc.printStackTrace();
							return false;
						} else {
							// logger.info("CcVerifyTransformRuleLogic() Token RULE STRING REGEX "
							// + ruleString + " is valid!");
						}
					}
					
				}

				else if (transformType.equals("NUMERIC")) {
					// logger.info("CcVerifyTransformRuleLogic() Token RULE TYPE: NUMERIC");
					
					if (transformSrcString.matches("(^(\\+)?[0-9]+([0-9]+)?)+")) {
						transformSrcString = transformSrcString.replace("+", "");
						if (transformSrcString != null && !transformSrcString.isEmpty()) {
							try {
								Long.parseLong(transformSrcString);
								// logger.info("CcVerifyTransformRuleLogic() Token RULE STRING: "
								// + Long.parseLong(ruleString));
							} catch (NumberFormatException e) {
								logger.error("Invalid rule NUMERIC must contain only numbers: "
										+ transformSrcString);
								return false;
							}
						}
					} else {
						logger.error("Is an invalid CcProcessRulesNumericCdcc rule");
						return false;
					}
					
					if (transformDestString.matches("(^(\\+)?[0-9]+([0-9]+)?)+")) {
						transformDestString = transformDestString.replace("+", "");
						if (transformDestString != null && !transformDestString.isEmpty()) {
							try {
								Long.parseLong(transformDestString);
								// logger.info("CcVerifyTransformRuleLogic() Token RULE STRING: "
								// + Long.parseLong(ruleString));
							} catch (NumberFormatException e) {
								e.printStackTrace();
								logger.error("Invalid rule NUMERIC must contain only numbers: "
										+ transformDestString);
								return false;
							}
						}
					} else {
						logger.error("Is an invalid CcProcessRulesNumericCdcc rule");
						return false;
					}
					

				} else if (transformType.equals("WILDCARD")) {
					// logger.info("CcVerifyTransformRuleLogic() Token RULE TYPE: WILDCARD");

					if (transformSrcString != null && !transformSrcString.isEmpty()) {
						String newRuleString = utilObj.getWildCard(transformSrcString);
						// logger.info("CcVerifyTransformRuleLogic() WildCard Internal Value: "
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
							// logger.info("CcVerifyTransformRuleLogic() Token RULE STRING WILDCARD "
							// + ruleString + " is valid!");
						}
					}
					if (transformDestString != null && !transformDestString.isEmpty()) {
						String newRuleString = utilObj.getWildCard(transformDestString);
						// logger.info("CcVerifyTransformRuleLogic() WildCard Internal Value: "
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
							// logger.info("CcVerifyTransformRuleLogic() Token RULE STRING WILDCARD "
							// + ruleString + " is valid!");
						}
					}
					
					
				} else {
					logger.error("CcVerifyCallRuleLogic() " + transformValue
							+ " is INVALID");
					return false;
				}
			}
			
			if (transformApplyto != null && !transformApplyto.isEmpty()) {
				transformApplyto = transformApplyto.toUpperCase();
				if (transformApplyto.equals("CALLED") || transformApplyto.equals("CALLING") || transformApplyto.equals("REDIRECT")) {
					return true;
				}
				else {
					return false;
				}
				
			}

			if (transformBlock != null && !transformBlock.isEmpty()) {
				transformBlock = transformBlock.toUpperCase();
				if (transformBlock.equals("TRUE") || transformBlock.equals("FALSE")) {
					return true;
				}
				else {
					return false;
				}
				
			}
			

		}
		return true;
	}


	

}
