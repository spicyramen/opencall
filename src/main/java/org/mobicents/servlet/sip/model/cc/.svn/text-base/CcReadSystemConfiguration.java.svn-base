package org.mobicents.servlet.sip.model.cc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * This class Reads File from local path and determines connection type
 * 
 * @author gogasca
 * 
 */

public class CcReadSystemConfiguration {

	private static Logger logger = Logger
			.getLogger(CcReadSystemConfiguration.class);
	private static String DELIMITER = "=";
	private String SYSTEM_CONFIGURATION = "";

	private int MODE = 0;
	private ArrayList<String> candidateParameters = new ArrayList<String>();
	private ArrayList<String> systemParameters = new ArrayList<String>();
	private ArrayList<String> initParams = new ArrayList<String>();
	private ArrayList<String> generalParams = new ArrayList<String>();
	private ArrayList<String> fileParams = new ArrayList<String>();
	private ArrayList<String> dbParams = new ArrayList<String>();
	private ArrayList<String> CONNECTION = new ArrayList<String>();

	/**
	 * Constructor we initialize parameters
	 * 
	 * @param fileName
	 */

	public CcReadSystemConfiguration(String fileName) {

		SYSTEM_CONFIGURATION = fileName;
		initParams.add("MODE");
		generalParams.add("RULE_LIMIT");
		fileParams.add("CALLRULES");
		fileParams.add("CALLTRANSFORMS");
		fileParams.add("CALLROUTELIST");
		dbParams.add("DBTYPE");
		dbParams.add("DBHOSTNAME");
		dbParams.add("DBPORT");
		dbParams.add("DBNAME");
		dbParams.add("DBUSERNAME");
		dbParams.add("DBPASSWORD");
	}

	public boolean CcInitSystemConfiguration() {
		logger.info("CcInitSystemConfiguration() CcInitConfigurationFile initializing...");
		try {
			if (CcVerifyConfigurationFileAccess(SYSTEM_CONFIGURATION)) { // Verify File is readable
																			
				if (!CcReadFileParameters(SYSTEM_CONFIGURATION)) { // Read File parameters
																
					return false;
				}
				if (!CcVerifyFileParameters(SYSTEM_CONFIGURATION)) { // Verify File Parameters
																		
					return false;
				}
				return true;

			} else {
				return false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * 
	 * @return
	 */
	public int getSystemMode() {
		return MODE;
	}

	/**
	 * 
	 * @return
	 */

	public ArrayList<String> getConnection() {
		return CONNECTION;
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
			SYSTEM_CONFIGURATION = configurationFileName;
		} else {
			return false;
		}

		File configFile = new File(SYSTEM_CONFIGURATION);
		
		
		if (configFile.exists() && configFile.canRead()) {
			return true;
		} else {
			logger.info("CcVerifyConfigurationFileAccess Inaccessible file " + configurationFileName);
			return false;

		}
	}

	private boolean CcReadFileParameters(String configurationFileName)
			throws IOException {
		try {
			FileInputStream fstream = new FileInputStream(configurationFileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				strLine = strLine.trim();
				if (strLine.startsWith("#") || strLine.isEmpty()) {

				} else {
					candidateParameters.add(strLine);
				}
			}

			in.close();
			if (CcDisplayCandidateParameters()) {
				return true;
			} else {
				return false;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			logger.info("CcDisplayCandidateRules() Candidate Parameters "
					+ e.getMessage());
			return false;
		}

	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */

	private boolean CcDisplayCandidateParameters() throws IOException {
		logger.info("CcDisplayCandidateRules()");
	//	int intPatterns = 0;
		
	/*	for (@SuppressWarnings("unused") String r : candidateParameters) {
			intPatterns++;
		}
   	if (intPatterns == 0) {
		} else {
			return true;
		}

   *
   */
		if (candidateParameters.size() == 0) {
			logger.info("CcDisplayCandidateRules() No rules found");
			return false;	
		}
		else {
			return true;
		}
	
	}

	/**
	 * 
	 * @param configurationFileName
	 * @return
	 * @throws IOException
	 */

	private boolean CcVerifyFileParameters(String configurationFileName) throws IOException {
		
		try {
		
			// Open the file that is the first
		
			FileInputStream fstream = new FileInputStream(configurationFileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {

				strLine = strLine.trim();
				if (strLine.startsWith("#") || strLine.isEmpty()) {
					// Comment found do nothing
				} else {
					systemParameters.add(strLine);
				}
			}

			// Close the input stream
			in.close();

			logger.info("CcVerifyFilesystemParameters() Total System Parameters ("	+ systemParameters.size() + ")");
			String[] paramType;
			int index = 1;
			boolean modeFound = false;
			/* given string will be split by the argument delimiter provided. */

			if (!systemParameters.isEmpty()) {

				for (String param : systemParameters) {
					logger.info("*************************** Parsing System Configuration "
							+ "["
							+ index
							+ "] "
							+ "***************************");
					logger.info("CcVerifyFilesystemParameters() Parsing System Configuration "
							+ "[" + index + "] " + param);
					paramType = param.split(DELIMITER);
					if (paramType.length > 2) {
						logger.info("CcVerifyFilesystemParameters() Error Parsing Rule"
								+ "(" + index + ") " + param);
					} else {
						
						//Check MODE
						if (verifyParameter(paramType[0].toString(), 0)) 
						{
							logger.info("CcVerifyFileParameters Parameter: "
									+ paramType[0].toString() + " index: "
									+ index);
							if (verifyParameter(paramType[0].toString(), 1)
									&& !modeFound) {
								try {
									MODE = Integer.parseInt(paramType[1]
											.toString());
									logger.info("CcVerifyFileParameters Mode: "
											+ MODE);
									modeFound = true;
									if (obtainRules(MODE)) {
										logger.info("obtainRules Mode: " + MODE);
										return true;
									} else {
										logger.info("obtainRules Error Mode: "
												+ MODE);
										return false;
									}
								} catch (NumberFormatException e) {
									logger.info("CcVerifyFilesystemParameters() Error Parsing Rule");
									return false;
								}

							} else {
								logger.info("CcVerifyFileParameters Invalid Parameter: "
										+ paramType[0].toString()
										+ " index: "
										+ index);
								return false;
							}
						} else {
							logger.info("CcVerifyFileParameters Index:" + index);
							logger.info("CcVerifyFilesystemParameters() Error in Parameter Rule"
									+ "(" + index + ") " + param + "\n");
						}
					}
					index++;
				}
				return false;
			}

			else {
				logger.info("CcVerifyFilesystemParameters() No Parameters found!");
				return false;
			}

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			return false;
		}

	}

	/**
	 * 
	 * @param configurationParameter
	 * @return
	 */

	public boolean verifyParameter(String configurationParameter, int checkParam) {
		// Check all
		if (checkParam == 0) {
			logger.info("verifyParameter() checking all params in file");
			if (initParams.contains(configurationParameter)) {
				logger.info("verifyParameter() initParam "
						+ configurationParameter);
			} 
			else if (generalParams.contains(configurationParameter)) {

			} 
			else if (fileParams.contains(configurationParameter)) {
			
			} 
			else if (dbParams.contains(configurationParameter)) {
			
			} 
			else {
				return false;
			}

			return true;
			
		} else if (checkParam == 1) {
			if (initParams.contains(configurationParameter)) {
				logger.info("verifyParameter() initParam "
						+ configurationParameter);
				return true;
			} else {
				return false;
			}
		} else if (checkParam == 2) {
			if (generalParams.contains(configurationParameter)) {
				logger.info("verifyParameter generalParams");
				return true;
			} else {
				return false;
			}
		} else if (checkParam == 3) {
			if (fileParams.contains(configurationParameter)) {
				logger.info("verifyParameter fileParams");
				return true;
			} else {
				return false;
			}
		} else if (checkParam == 4) {
			if (dbParams.contains(configurationParameter)) {
				logger.info("verifyParameter dbParams");
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	private boolean obtainRules(int Mode) {
		int index = 0;
		String[] paramType;
		if (Mode == 1) { // File mode
			for (String param : systemParameters) {
				paramType = param.split(DELIMITER);
				if (fileParams.contains(paramType[0].toString())) {
					logger.info("ObtainRules FILE param found: " + param);
					CONNECTION.add(param);
					index++;
				}
			}
			if (index == 1) {
				logger.info("obtainRules Valid parameters finalized");
				return true;
			}
		} else if (Mode == 2) { // DB mode
			for (String param : systemParameters) {
				paramType = param.split(DELIMITER);
				if (dbParams.contains(paramType[0].toString())) {
					logger.info("ObtainRules DB param found: " + param);
					CONNECTION.add(param);
					index++;
				}

			}
			if (index == 6) {
				logger.info("ObtainRules Valid parameters finalized");
				return true;
			}
		} else if (Mode == 3) {
			return true;
		} else {
			logger.info("Invalid Mode");
			return false;
		}

		return false;
	}

}
