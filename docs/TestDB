package org.mobicents.servlet.sip.model.cc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class CcTestDb {

	private static Logger logger = Logger.getLogger(CcTestDb.class);
	private ArrayList<String> candidateRoutePatterns = new ArrayList<String>();
	private String dbUrl;
	private String dbName;
	private String dbHostName;
	private String dbClass;
	private String dbUserName;
	private String dbPassword;
	private int dbPort;

	public boolean CcInitReadDB(String Url, String Name, String hostName,
			String userName, String password, int port) {

		if (CcUtils.isValidHostName(hostName) && CcUtils.isValidPort(port)) {
			dbHostName = hostName;
			dbName = Name;
			dbPort = port;
			dbClass = "com.mysql.jdbc.Driver";
			dbUserName = userName;
			dbPassword = password;
			dbUrl = "jdbc:mysql://" + dbHostName + ":" + dbPort + "/" + dbName;
			return true;
		} else {
			logger.error("CcReadDB() Unable to initialize SQL object, check Hostname and Port");
			return false;
		}
	}

	public void CcInitReadDB() {
		dbHostName = "localhost";
		dbName = "opencall";
		dbPort = 3306;
		dbClass = "com.mysql.jdbc.Driver";
		dbUserName = "root";
		dbPassword = "";
		dbUrl = "jdbc:mysql://" + dbHostName + ":" + dbPort + "/" + dbName;

	}

	public boolean CcInitDBConnection() throws Exception {

		try {

			Class.forName(dbClass).newInstance();
			System.out.println("SQL PARAMETERS: " + "dbUrl " + dbUrl);
			Connection dbConnection = DriverManager.getConnection(dbUrl,
					dbUserName, dbPassword);
			dbConnection.close();
			return true;
		}

		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		catch (SQLException e) {
			e.printStackTrace();
		}
		return false;

	}

	public boolean CcReadDB() {

		String dbClass = "com.mysql.jdbc.Driver";
		String query = "select * from routingrules";
		String RULE, PRIORITY, TYPE, STRING, TARGET, PORT;
		String CANDIDATE;

		try {
			try {
				Class.forName(dbClass).newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
				System.out.printf("%s,%s,%s,%s,%s,%s \n", RULE, PRIORITY, TYPE,
						STRING, TARGET, PORT);

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

	private boolean CcDisplayCandidateRules() throws IOException {
		int intPatterns = 0;
		for (String r : candidateRoutePatterns) {
			System.out.println(r);
			intPatterns++;
		}
		if (intPatterns == 0) {
			return false;
		} else {
			return true;
		}

	}

	public static void main(String[] args) {

		CcTestDb newDBconnection = new CcTestDb();
		newDBconnection.CcInitReadDB();
		try {
			newDBconnection.CcInitDBConnection();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		newDBconnection.CcReadDB();
		try {
			newDBconnection.CcDisplayCandidateRules();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
