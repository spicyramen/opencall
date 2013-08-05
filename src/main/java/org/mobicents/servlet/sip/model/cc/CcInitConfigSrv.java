package org.mobicents.servlet.sip.model.cc;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * 
 * @author gogasca
 * 
 */

public class CcInitConfigSrv {

	private static Logger logger = Logger.getLogger(CcInitConfigSrv.class);
	private boolean isStarted = false;
	private static String DELIMITER = "=";
	private CcDigitAnalysisEngine DigitAnalysisModule = null;
	private String finalURI = null;

	/**
	 * Constructor Type 0 = Undefined 1 = Local File 2 = DB 3 = Local String 4 =
	 * Other
	 */

	public CcInitConfigSrv() {

	}

	/**
	 * Initialize configuration based on type, read routing rules.
	 * 
	 * @param Type
	 *            FILE, DB, INTERNAL, OTHER
	 * @param initConfigFileName
	 * @throws Exception
	 */

	public void initializeConfiguration(int Type, ArrayList<String> connectionInfo) throws Exception {

		// We create a new Object to Read information.
		// CcSystemConfigurationEngine will parse the Rules
		
		CcSystemConfigurationEngine initConfigModule = new CcSystemConfigurationEngine(Type, connectionInfo);

		if (Type == 1) {
			String CALLRULES = "";
			String CALLTRANSFORMS = "";
			String ROUTELIST = "";
			
			String[] paramType;
			for (String fileParam : connectionInfo) {
				paramType = fileParam.split(DELIMITER);
				if (paramType[0].toString().equals("CALLRULES")) {
					CALLRULES = paramType[1].toString();
					logger.info("MODE " + Type + " CALL RULES FILE FOUND: " + CALLRULES);
				}
				else if (paramType[0].toString().equals("CALLTRANSFORMS")) {
					CALLTRANSFORMS = paramType[1].toString();
					logger.info("MODE " + Type + " CALL TRANSFORMS FILE  FOUND: " + CALLTRANSFORMS);
				}
				else if (paramType[0].toString().equals("ROUTELIST")) {
					ROUTELIST = paramType[1].toString();
					logger.info("MODE " + Type + " ROUTELIST FILE  FOUND: " + ROUTELIST);
				}
			}

			if (initConfigModule.CcStartCallRulesEngine(CALLRULES)) {
			
				// Obtain valid rules from File or DB
				DigitAnalysisModule = new CcDigitAnalysisEngine(initConfigModule.CcGetRules());
				if (DigitAnalysisModule.isStarted()) {
					isStarted = true;
				} else {
					isStarted = false;
					logger.error("Error InitializeConfiguration.CcStartFileEngine() failed to start.");
				}
			} else {
				logger.error("Error InitializeConfiguration.CcStartFileEngine() failed to initialize");
				isStarted = false;
			}
		}

		else if (Type == 2) {
			/**
			 * DBTYPE=1 0=INVALID 1=MYSQL, 2=SQL SERVER 3=SQL PROGRESS
			 * DBHOSTNAME=localhost DBPORT=3306 DBNAME=opencall DBUSERNAME=root
			 * DBPASSWORD=
			 */
			String[] paramType;
			String DBTYPE, DBHOSTNAME, DBPORT, DBNAME, DBUSERNAME, DBPASSWORD;
			DBTYPE = null;
			DBHOSTNAME = null;
			DBPORT = null;
			DBNAME = null;
			DBUSERNAME = null;
			DBPASSWORD = null;
			logger.info("MODE: [" + Type + "] Processing DB Parameters");
			for (String dbParam : connectionInfo) {
				paramType = dbParam.split(DELIMITER);
				if (paramType[0].toString().equals("DBTYPE")) {
					DBTYPE = paramType[1].toString();
				} else if (paramType[0].toString().equals("DBHOSTNAME")) {
					logger.info("DB Param: " + paramType[0].toString());
					try {
						DBHOSTNAME = paramType[1].toString();
					} catch (ArrayIndexOutOfBoundsException e) {
						DBHOSTNAME = "";
					}
				} else if (paramType[0].toString().equals("DBPORT")) {
					logger.info("DB Param: " + paramType[0].toString());
					try {
						DBPORT = paramType[1].toString();
					} catch (ArrayIndexOutOfBoundsException e) {
						DBPORT = "";
					}
				} else if (paramType[0].toString().equals("DBNAME")) {
					logger.info("DB Param: " + paramType[0].toString());
					try {
						DBNAME = paramType[1].toString();
					} catch (ArrayIndexOutOfBoundsException e) {
						DBNAME = "";
					}
				} else if (paramType[0].toString().equals("DBUSERNAME")) {
					logger.info("DB Param: " + paramType[0].toString());
					try {
						DBUSERNAME = paramType[1].toString();
					} catch (ArrayIndexOutOfBoundsException e) {
						DBUSERNAME = "";
					}
				} else if (paramType[0].toString().equals("DBPASSWORD")) {
					logger.info("DB Param: " + paramType[0].toString());
					try {
						DBPASSWORD = paramType[1].toString();
					} catch (ArrayIndexOutOfBoundsException e) {
						DBPASSWORD = "";
					}
				}

			}

			if (initConfigModule.CcStartDbEngine(DBTYPE, DBHOSTNAME, DBPORT,
					DBNAME, DBUSERNAME, DBPASSWORD)) {
				DigitAnalysisModule = new CcDigitAnalysisEngine(
						initConfigModule.CcGetDbRules());
				if (DigitAnalysisModule.isStarted()) {
					isStarted = true;
				} else {
					isStarted = false;
					logger.error("Error InitializeConfiguration.CcStartDbEngine() failed to start.");
				}
			} else {
				logger.error("Error InitializeConfiguration.CcStartDbEngine() failed to initialize");
				isStarted = false;
			}

		} else if (Type == 3) {
			// TODO in a future
		} else {
			logger.error("Error InitializeConfiguration.CcStartFileEngine() failed to initialize");
			isStarted = false;
		}

	}

	/**
	 * 
	 * @param sipURI
	 * Process incoming SIP Message and return converted string.
	 * @return
	 */

	public String digitsDialed(String sipURI) {
		if (DigitAnalysisModule.CcCallProcessSipMessage(sipURI)) {
			finalURI = DigitAnalysisModule.getFinalURI();
			return finalURI;
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @return Transport defined in Call Rules
	 */
	public String getTransport() {
		return DigitAnalysisModule.getTransportURI();
	}
	
	/**
	 * 
	 * @return
	 */

	public boolean isStarted() {
		return isStarted;
	}

}
