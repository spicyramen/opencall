package org.mobicents.servlet.sip.model.cc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

public class CcUtils {

	private static int RULE_TOKEN_COUNT = 5;
	private static int RULE_TOKEN_MAX = 7;
	private static int TRANSFORM_TOKEN_COUNT   = 7;
	private static int START_PORT = 1;
	private static int END_PORT = 65535;
	private static int PRIORITY_LOWER = 1;
	private static int PRIORITY_UPPER = 100;
	private static Logger logger = Logger.getLogger(CcUtils.class);
	

	public CcUtils() {
		// logger.info("CcUtils() New Instance created");
	}

	public String[] getRuleValue(int iToken, String routeValue) {
		// 0 returns all values from Rule

		if (routeValue.isEmpty()) {
			logger.warn("getRuleValue() Processing Tokens: " + routeValue + " is empty!");
			return null;
		}
			
		if (iToken < 0 || iToken > RULE_TOKEN_MAX) {
			logger.warn("getRuleValue() Processing Tokens: " + iToken + " invalid index!");
			return null;
		}
			

		routeValue = routeValue.replaceAll("\\(\"|\"\\)", "\"");
		String[] Tokens = new String[RULE_TOKEN_MAX + 1];

		//logger.info("getRuleValue() Find Token: [" + iToken + "] Rule Content: " + routeValue);
		if (!routeValue.contains("\\(\"") && !routeValue.contains("\"\\)")) {
			// logger.info("getRuleValue() Processing Tokens: " + routeValue);
			StringTokenizer st = new StringTokenizer(routeValue, ",");
			if (st.countTokens() >= RULE_TOKEN_COUNT
					&& st.countTokens() <= RULE_TOKEN_MAX) {
				// logger.info("getRuleValue() Processing Tokens: (" +
				// st.countTokens() + ")");
				int tokenIndex = 1;
				while (st.hasMoreElements()) {
					String token = st.nextElement().toString();
					token = token.replaceAll("\"", "");
					if (token.isEmpty() && tokenIndex != 6) { // PORT CAN BE
																// EMPTY
						return null;
					}

					if ((tokenIndex == 1 && tokenIndex == iToken)
							|| (tokenIndex == 1 && iToken == 0)) { // RULE
																	// NUMBER
						try {
							Integer.parseInt(token);
							// logger.info("getRuleValue() Token [" + tokenIndex
							// + "]: " + Integer.parseInt(token));
							Tokens[tokenIndex] = token;

						} catch (NumberFormatException e) {
							e.printStackTrace();
							logger.info("getRuleValue() Invalid rule Number Value");
							return null;
						}
					}
					if ((tokenIndex == 2 && tokenIndex == iToken)
							|| (tokenIndex == 2 && iToken == 0)) { // PRIORITY

						try {
							if (Integer.parseInt(token) >= PRIORITY_LOWER
									&& Integer.parseInt(token) <= PRIORITY_UPPER
									&& (!token.isEmpty())) {
								// logger.info("getRuleValue() Token [" +
								// tokenIndex + "]: " +
								// Integer.parseInt(token));
								Tokens[tokenIndex] = token;
							} else
								logger.error("getRuleValue() Invalid Priority Value");
						} catch (NumberFormatException e) {
							e.printStackTrace();
							logger.error("getRuleValue() Invalid Priority Value");
							return null;
						}
					}
					if ((tokenIndex == 3 && tokenIndex == iToken)
							|| (tokenIndex == 3 && iToken == 0)) { // TYPE

						if (token.matches("REGEX") || token.matches("NUMERIC")
								|| token.matches("WILDCARD")) {
							// logger.info("getRuleValue() Token [" + tokenIndex
							// + "]: " + token);
							Tokens[tokenIndex] = token;
						} else {
							logger.error("getRuleValue() Invalid Type Value");
							return null;
						}
					}
					
					if ((tokenIndex == 4 && tokenIndex == iToken)
							|| (tokenIndex == 4 && iToken == 0)) { // STRING
						// logger.info("getRuleValue() Token [" + tokenIndex +
						// "]: " + token);
						Tokens[tokenIndex] = token;
					}
					
					if ((tokenIndex == 5 && tokenIndex == iToken)
							|| (tokenIndex == 5 && iToken == 0)) { // TRUNK
						if (isValidIP(token) || isValidHostName(token)
								|| token.matches("_DNS_")) {
							// logger.info("getRuleValue() Token [" + tokenIndex
							// + "]: " + token);
							Tokens[tokenIndex] = token;
						} else
							return null;
					}
					
					if ((tokenIndex == 6 && tokenIndex == iToken)
							|| (tokenIndex == 6 && iToken == 0)) { // PORT or TRANSPORT
						
						try {
							if (java.util.regex.Pattern.matches("\\d+", token)) {
								if (Integer.parseInt(token) >= START_PORT
										&& Integer.parseInt(token) <= END_PORT) {
									Tokens[tokenIndex] = token;
								}	
								else {
									logger.error("getRuleValue() Invalid Port Value");
								}	
							}			
							else if (CcUtils.isValidTransport(token)) {
								 Tokens[tokenIndex] = token;
							} 
							else {
								return null;
							}
							
						} catch (Exception e) {
							logger.error("getRuleValue() Invalid Value");
							return null;
						}
						
					}
					if ((tokenIndex == 7 && tokenIndex == iToken) 
							|| (tokenIndex == 7 && iToken == 0)) { // TRANSPORT
						try {
							if (token.matches("TCP") || token.matches("UDP")
								|| token.matches("TLS") || token.matches("WS") || token.matches("WSS")) {
								Tokens[tokenIndex] = token;
							} else {
								logger.error("getRuleValue() Invalid Transport Value");
								return null;
							}
						} 
						catch (Exception e) {
								return null;
						}
					}
					tokenIndex++;
				}

			} else {
				logger.error("getRuleValue() Invalid Rule");
				return null;
			}
		}

		return Tokens;
	}

	public String[] getTransformValue(int iToken, String transformValue) {
		
		// 0 returns all values from Rule

		if (transformValue.isEmpty()) {
			logger.warn("getTransformValue() Processing Tokens: " + transformValue + " is empty!");
			return null;
		}
			
		if (iToken < 0 || iToken > TRANSFORM_TOKEN_COUNT) {
			logger.warn("getTransformValue() Processing Tokens: " + iToken + " invalid index!");
			return null;
		}
		
		// Replace ( and ) with "
		transformValue = transformValue.replaceAll("\\(\"|\"\\)", "\"");
		String[] Tokens = new String[TRANSFORM_TOKEN_COUNT + 1];

		//logger.info("getTransformValue() Find Token: [" + iToken + "] Rule Content: " + routeValue);
		if (!transformValue.contains("\\(\"") && !transformValue.contains("\"\\)")) {
			// logger.info("getTransformValue() Processing Tokens: " + routeValue);
			StringTokenizer st = new StringTokenizer(transformValue, ",");
			
			if (st.countTokens() == TRANSFORM_TOKEN_COUNT) {
				
				// logger.info("getTransformValue() Processing Tokens: (" +
				// st.countTokens() + ")");
				
				int tokenIndex = 1;
				
				while (st.hasMoreElements()) {
					String token = st.nextElement().toString();
					token = token.replaceAll("\"", "");
					
					if ((tokenIndex == 1 && tokenIndex == iToken)
							|| (tokenIndex == 1 && iToken == 0)) { // RULE NUMBER
																	
						try {
							Integer.parseInt(token);
							// logger.info("getRuleValue() Token [" + tokenIndex
							// + "]: " + Integer.parseInt(token));
							Tokens[tokenIndex] = token;

						} catch (NumberFormatException e) {
							logger.error("getTransformValue() Invalid rule Number Value");
							return null;
						}
					}
					if ((tokenIndex == 2 && tokenIndex == iToken)
							|| (tokenIndex == 2 && iToken == 0)) { // ENABLED

						if (token.matches("TRUE") || token.matches("FALSE")) 
						{
							// logger.info("getRuleValue() Token [" + tokenIndex
							// + "]: " + token);
							Tokens[tokenIndex] = token;
						} else {
							logger.error("getTransformValue() Invalid Type Value");
							return null;
						}
					}
					if ((tokenIndex == 3 && tokenIndex == iToken)
							|| (tokenIndex == 3 && iToken == 0)) { // TYPE

						if (token.matches("REGEX") || token.matches("NUMERIC")
								|| token.matches("WILDCARD")) {
							// logger.info("getRuleValue() Token [" + tokenIndex
							// + "]: " + token);
							Tokens[tokenIndex] = token;
						} else {
							logger.error("getTransformValue() Invalid Type Value");
							return null;
						}
					}
					
					if ((tokenIndex == 4 && tokenIndex == iToken)
							|| (tokenIndex == 4 && iToken == 0)) { // STRING
						// logger.info("getRuleValue() Token [" + tokenIndex +
						// "]: " + token);
						Tokens[tokenIndex] = token;
					}
					
					if ((tokenIndex == 5 && tokenIndex == iToken)
							|| (tokenIndex == 5 && iToken == 0)) { // STRING
						// logger.info("getRuleValue() Token [" + tokenIndex +
						// "]: " + token);
						Tokens[tokenIndex] = token;
					}
					
					if ((tokenIndex == 6 && tokenIndex == iToken)
							|| (tokenIndex == 6 && iToken == 0)) { // PORT or TRANSPORT
						
						if (token.matches("CALLED") || token.matches("CALLING") || token.matches("REDIRECT")) 
						{
							// logger.info("getRuleValue() Token [" + tokenIndex
							// + "]: " + token);
							Tokens[tokenIndex] = token;
						} else {
							logger.error("getTransformValue() Invalid Type Value");
							return null;
						}
						
					}
					if ((tokenIndex == 7 && tokenIndex == iToken)
							|| (tokenIndex == 7 && iToken == 0)) { // ENABLED

						if (token.matches("TRUE") || token.matches("FALSE")) 
						{
							// logger.info("getRuleValue() Token [" + tokenIndex
							// + "]: " + token);
							Tokens[tokenIndex] = token;
						} else {
							logger.error("getTransformValue() Invalid Type Value");
							return null;
						}
					}
					
					tokenIndex++;
				}

			} else {
				logger.error("getTransformValue() Invalid Rule");
				return null;
			}
		}

		return Tokens;
	}
	/**
	 * 
	 * @param routeValue
	 * @return
	 */
	public int getTokenCount(String routeValue) {
		StringTokenizer st = new StringTokenizer(routeValue, ",");
		return st.countTokens();
	}

	/**
	 * 
	 * @param routeString
	 * @return
	 */
	public String getWildCard(String routeString) {
		// logger.info("getWildCard() Incoming rule: " + routeString);
		routeString = routeString.replace("X", "\\d");
		// logger.info("getWildCard() " + routeString);
		routeString = routeString.replace(".", "\\.");
		// logger.info("getWildCard() " + routeString);
		routeString = routeString.replace("!", "\\d+");
		// logger.info("getWildCard() " + routeString);
		// Add + Support for E164 dialing
		routeString = routeString.replace("+", "\\+");
		// logger.info("getWildCard() Return: " + routeString);
		routeString = routeString.replace("-", "\\-");
		// logger.info("getWildCard() Return: " + routeString);
		return routeString;

	}

	public static boolean checkDbParams(String hostName, int port, String Name,
			String userName, String password) {
		// (String hostName,int port, String Name, String userName, String
		// password)
		if (hostName != "" && (port >= 0 && port <= 65535) && Name != ""
				&& userName != "") {
			if (password.equals("")) {
				logger.warn("checkDbParams() DB password is empty");
				return true;

			}
		} else
			return false;
		return false;
	}

	public static String addQuotes(String word) {
		word = "\"" + word + "\"";
		return word;
	}

	/**
	 * 
	 * @param ipAddr
	 * @return
	 */
	public static boolean isValidIP(String ipAddr) {
		return ipAddr
				.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
	}

	/**
	 * 
	 * @param hostName
	 * @return
	 */
	public static boolean isValidHostName(String hostName) {
		
		if(hostName.matches("_DNS_")) {
			return true;
		}
		
		try {
			InetAddress in = InetAddress.getByName(hostName);

			if (in != null)
				return true;
			else
				return false;
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
	}

	public int isPortOrTransport(String param) {
		
		/*
		 * Port: 			1
		 * Transport: 		2
		 * Other:			-1
		 */
		if (java.util.regex.Pattern.matches("\\d+", param)) {
			if (Integer.parseInt(param) >= START_PORT
					&& Integer.parseInt(param) <= END_PORT) {
				return 1;
			}	
			else {
				logger.error("Invalid Port Value");
				return -1;
			}	
		}			
		else if (CcUtils.isValidTransport(param)) {
			return 2; 
		} 
		else {
			return -1;
		}
		
	}
	
	public static boolean isValidPort(int port) {
		
		try {
			if (port < 1024 || port > 65535)
				return false;
			else
				return true;
		} catch (Exception e) {
			logger.warn(e);
			return false;
		}
	}
	
	public static boolean isValidTransport(String transport) {
		
		if (transport!=null && !transport.isEmpty())  {
			
			ArrayList<String> validTransport = new ArrayList<String>();
			validTransport.add("TCP");
			validTransport.add("UDP");
			validTransport.add("TLS");
			validTransport.add("WS");
			validTransport.add("WSS");
			validTransport.add("TWILIO");
			validTransport.add("GTALK");
			
			try {
				transport = transport.toUpperCase();
				if(validTransport.contains(transport))
					return true;
				else
					return false;
			} catch (NumberFormatException e) {
				return false;
			}
			catch (Exception e) {
				logger.warn(e + " Invalid transport ");
				return false;
			}
		}
			
		return false;
		
	}
	
}
