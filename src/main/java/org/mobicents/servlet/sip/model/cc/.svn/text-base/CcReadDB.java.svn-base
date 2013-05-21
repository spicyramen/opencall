package org.mobicents.servlet.sip.model.cc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 * 
 * @author gogasca
 * 
 */

public class CcReadDB implements CcSystemConfigurationDBInterface {

	private static Logger logger = Logger.getLogger(CcReadDB.class);
	private String dbUrl;
	private String dbName;
	private String dbHostName;
	private String dbClass;
	private String dbUserName;
	private String dbPassword;
	private int dbPort;
	private ArrayList<String> candidateRoutePatterns = new ArrayList<String>();

	public CcReadDB() {

	}

	/**
 * 
 */

	public void CcInitReadDB() {

		dbHostName = "localhost";
		dbName = "opencall";
		dbPort = 3306;
		dbClass = "com.mysql.jdbc.Driver";
		dbUserName = "root";
		dbPassword = "";
		dbUrl = "jdbc:mysql://" + dbHostName + ":" + dbPort + "/" + dbName;

	}

	public boolean CcCheckDB(String hostName, int port, String Name,
			String userName, String password) {

		if (CcUtils.checkDbParams(hostName, port, Name, userName, password)) {
			if (CcUtils.isValidHostName(hostName) && CcUtils.isValidPort(port)) {
				dbHostName = hostName;
				dbName = Name;
				dbPort = port;
				dbClass = "com.mysql.jdbc.Driver";
				dbUserName = userName;
				dbPassword = password;
				dbUrl = "jdbc:mysql://" + dbHostName + ":" + dbPort + "/"
						+ dbName;
				return true;
			} else {
				logger.error("CcReadDB() Unable to initialize SQL object, check DB Parameters");
				return false;
			}
		}

		return false;

	}

	public boolean CcInitDBConnection() {

		try {
			logger.info("SQL INFO: " + " dbClass: " + dbClass);
			logger.info("SQL PARAMETERS: " + " dbUrl: " + dbUrl);
			/**
			 * try {
			 * 
			 * Class.forName(dbClass).newInstance();
			 * 
			 * } catch (ClassNotFoundException e) {
			 * logger.error("InstantiationException() Exception");
			 * e.printStackTrace(); } catch (IllegalAccessException e) {
			 * logger.error("IllegalAccessException() Exception");
			 * e.printStackTrace(); } catch (InstantiationException e) { // TODO
			 * Auto-generated catch block e.printStackTrace(); }
			 **/
			if (dbPassword.equals(null))
				dbPassword = "";
			Connection dbConnection = DriverManager.getConnection(dbUrl,
					dbUserName, dbPassword);
			dbConnection.close();
			return true;

		} catch (SQLException e) {
			logger.error("CcInitDBConnection() Exception");
			e.printStackTrace();
		}
		return false;

	}

	public boolean CcReadDBInfo() {

		String dbClass = "com.mysql.jdbc.Driver";
		String query = "select * from callrules";
		String RULE, PRIORITY, TYPE, STRING, TARGET, PORT;
		String CANDIDATE;

		try {
			Class.forName(dbClass);
			Connection dbConnection = DriverManager.getConnection(dbUrl,
					dbUserName, dbPassword);
			Statement stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				RULE = rs.getString(1);
				PRIORITY = rs.getString(2);
				TYPE = rs.getString(3);
				STRING = rs.getString(4);
				TARGET = rs.getString(5);
				PORT = rs.getString(6);

				// System.out.printf("%s %s %s %s %s %s \n",RULE,PRIORITY,TYPE,STRING,TARGET,PORT);
				// ROUTE=("1","100","REGEX","(.*)@.*","_DNS_")
				// ROUTE=("12","1","WILDCARD","4XX","110.10.0.220","5060")
				RULE = CcUtils.addQuotes(RULE);
				PRIORITY = CcUtils.addQuotes(PRIORITY);
				TYPE = CcUtils.addQuotes(TYPE);
				STRING = CcUtils.addQuotes(STRING);
				TARGET = CcUtils.addQuotes(TARGET);

				if (PORT != null) {
					PORT = CcUtils.addQuotes(PORT);
					CANDIDATE = "ROUTE=(" + RULE + "," + PRIORITY + "," + TYPE
							+ "," + STRING + "," + TARGET + "," + PORT + ")";
				} else {
					CANDIDATE = "ROUTE=(" + RULE + "," + PRIORITY + "," + TYPE
							+ "," + STRING + "," + TARGET + ")";
				}
				candidateRoutePatterns.add(CANDIDATE);

			} // end while

			dbConnection.close();
			return true;
		} // end try

		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		catch (SQLException e) {
			e.printStackTrace();
		}
		return false;

	}

	public ArrayList<String> getDBRules() {
		return candidateRoutePatterns;

	}

}
