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
	private CcDigitAnalysisEngine initDigitAnalysisModule = null;
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
					logger.info("MODE " + Type + " ROUTE LIST FILE  FOUND: " + ROUTELIST);
				}
			}

			if (initConfigModule.CcStartCallRulesEngine(CALLRULES)) {
				initDigitAnalysisModule = new CcDigitAnalysisEngine(initConfigModule.CcGetRules());
				if (initDigitAnalysisModule.isStarted()) {
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
				initDigitAnalysisModule = new CcDigitAnalysisEngine(
						initConfigModule.CcGetDbRules());
				if (initDigitAnalysisModule.isStarted()) {
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
	 * @return
	 */

	public String digitsDialed(String sipURI) {
		if (initDigitAnalysisModule.CcCallProcessSipMessage(sipURI)) {
			finalURI = initDigitAnalysisModule.getFinalURI();
			return finalURI;
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @return
	 */

	public boolean isStarted() {
		return isStarted;
	}

}
