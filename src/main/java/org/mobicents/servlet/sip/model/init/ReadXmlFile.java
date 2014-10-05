package org.mobicents.servlet.sip.model.init;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 

public class ReadXmlFile{

	private static String defaultSystemFileName = "opencall-config.xml";
	
	public ReadXmlFile() {
		
	}
	
	/**
	 * 
	 * @param configurationFileName
	 * @return
	 * @throws IOException
	 */
	private boolean verifyFileAccess(String configurationFileName)
			throws IOException {
		if (configurationFileName != "" || configurationFileName != null) {
			defaultSystemFileName = configurationFileName;
		} else {
			return false;
		}

		File configFile = new File(defaultSystemFileName);
				
		if (configFile.exists() && configFile.canRead()) {
			return true;
		} else {
			//logger.info("verifyFileAccess Inaccessible file " + configurationFileName);
			return false;

		}
	}
	
	/**
	 * 
	 * @param serverSettings
	 * @return
	 */
	public ArrayList<Parameter> readFileParameters(NodeList nodeSettings) {
		ArrayList<Parameter> nodeParameters = new ArrayList<Parameter>();
		
		if (nodeSettings.getLength()!=1) {
         	System.out.println("Mandatory elements missing. Invalid configuration file contents");
         	return null;
         }
         
         for(int s=0; s<nodeSettings.getLength(); s++){

             Node firstServerNode = nodeSettings.item(s);
             String paramName = "";
             String paramValue = "";
             
             if(firstServerNode.getNodeType() == Node.ELEMENT_NODE) {
                 Element serverElements = (Element)firstServerNode;
                 NodeList serverList = serverElements.getElementsByTagName("*");
                 //System.out.println("Node Elements found: " + serverList.getLength());
                 
                 for(int el=0;el< serverList.getLength(); el++) {
                	 Element serverListElement = (Element)serverList.item(el);
                	 NodeList textLNList = serverListElement.getChildNodes();
                	 
                	 try {
                		 paramName = serverList.item(el).getNodeName();
                    	 paramValue = ((Node)textLNList.item(0)).getNodeValue().trim();
                    	 if (paramValue!="" && paramValue!="") {
                    		 System.out.println(paramName + " " + paramValue);
							 nodeParameters.add(new Parameter(paramName,""));
                    		 
                    	 } 
                	 }
                	 catch(Exception NullPointerException) {
                		 if (paramValue!="") {
                			 System.out.println(paramName + " " + " ");
							 nodeParameters.add(new Parameter(paramName,""));
                		 }
                	 }
                	
                	        
                 }
                 //------
             }//end of if clause
         }
         return nodeParameters;
         //end of for loop with s var
	}
	/**
	 * 
	 * @param serverSettings
	 * @return
	 */
	public ArrayList<CallRule> readCallRulesParameters(NodeList callRulesSection) {
		
		ArrayList<CallRule> callRulesProccessedListFromXml = new ArrayList<CallRule>();
		
		if (callRulesSection.getLength()!=1) {
         	System.out.println("Mandatory section missing. Using system default call rule.");
         	return null;
         }
       
		// Read CallRules from CallRule Section
		 Node callRulesNodeExtracted = callRulesSection.item(0);
         // Validate is a valid Node
         if(callRulesNodeExtracted.getNodeType() == Node.ELEMENT_NODE) {
        	 
        	 
             Element callRulesElements = (Element)callRulesNodeExtracted;
             // Read CallRules Rule by Rule
             NodeList callRulesList = callRulesElements.getElementsByTagName("Rule");
             System.out.println("Number of Call Rules found: " + callRulesList.getLength());
                  
             // Read each CallRule Element in List
             for(int node=0;node< callRulesList.getLength(); node++) {
             	
            	 Node callRule = callRulesList.item(node);
            	 
            	 if(callRule.getNodeType() == Node.ELEMENT_NODE) {
            		 
            		 String fieldInProgress = null;
            		 int id = -1;
            		 String type = null;
                	 String pattern = null;
                	 String target = null;
                	 int priority = 0;
                	 int port = 0;
                	 String transport = null;
                	
                	 
            		 try {
            			 Element callRuleElement = (Element)callRule;
            			 System.out.println("--------------------------------------------------------");
            			 id = node + 1;
                    	 
            			 if (callRuleElement.getElementsByTagName("Type").getLength()!=0) { 
            				 fieldInProgress = "Type";
            				 NodeList callRuleType = callRuleElement.getElementsByTagName("Type");
                             Element callRuleElementType = (Element)callRuleType.item(0);
                             NodeList textTypeList = callRuleElementType.getChildNodes();
                             type = (String)textTypeList.item(0).getNodeValue().trim();
                             System.out.println("Type : " + 
                                     ((Node)textTypeList.item(0)).getNodeValue().trim());
            			 }
            			 else {
            				 System.out.println("Error in Rule");
            				 continue;
            			 }
            			 
            			 if (callRuleElement.getElementsByTagName("Pattern").getLength()!=0) { 
            				 fieldInProgress = "Pattern";
            				 NodeList callRulePattern = callRuleElement.getElementsByTagName("Pattern");
                             Element callRuleElementPattern = (Element)callRulePattern.item(0);
                             NodeList textPatternList = callRuleElementPattern.getChildNodes();
                             pattern = (String)textPatternList.item(0).getNodeValue().trim();
                             System.out.println("Pattern : " + 
                                     ((Node)textPatternList.item(0)).getNodeValue().trim());
            			 }
            			 else {
            				 System.out.println("Error in Rule");
            				 continue;
            				 
            			 }
                		
            			 if (callRuleElement.getElementsByTagName("Target").getLength()!=0) { 
            				 fieldInProgress = "Target";
            				 NodeList callRuleTarget = callRuleElement.getElementsByTagName("Target");
                             Element callRuleElementTarget = (Element)callRuleTarget.item(0);
                             NodeList textTargetList = callRuleElementTarget.getChildNodes();
                             target = (String)textTargetList.item(0).getNodeValue().trim();
                             System.out.println("Target : " + 
                                     ((Node)textTargetList.item(0)).getNodeValue().trim());
            			 } else {
            			 
            				 System.out.println("Error in Rule");
            				 continue;
            			 }
                         
                        
            		       // Create New Rule with mandatory parameters
                         CallRule rule = new CallRule(id,type,pattern,target);
                          
                         // Optional Parameters opencall-config.xml
                         // Default Priority = 50
                         
                         
                         if (callRuleElement.getElementsByTagName("Priority").getLength()!=0) {
                        	 fieldInProgress = "Priority";
                        	 NodeList callRulePriority = callRuleElement.getElementsByTagName("Priority");
                        	 Element callRuleElementPriority = (Element)callRulePriority.item(0);      		 
                    		 NodeList textPrList = callRuleElementPriority.getChildNodes();
                    		 System.out.println("Priority : " + 
                                     ((Node)textPrList.item(0)).getNodeValue().trim());
                    		 priority = Integer.parseInt((String)textPrList.item(0).getNodeValue().trim());
                    		 rule.setPriority(priority);
                         }
                         else {
                        	 System.out.println("No priority defined");
                         }
                		  
                		 // Default Port = 5060
                         
                         if (callRuleElement.getElementsByTagName("Port").getLength() != 0) {
                        	 fieldInProgress = "Port";
                        	 NodeList callRulePort = callRuleElement.getElementsByTagName("Port");
                        	 Element callRuleElementPort = (Element)callRulePort.item(0);
                             NodeList textPortList = callRuleElementPort.getChildNodes();
                             System.out.println("Port : " + 
                                     ((Node)textPortList.item(0)).getNodeValue().trim());
                             port = Integer.parseInt((String)textPortList.item(0).getNodeValue().trim());
                             rule.setPort(port);
                         }
                         else {
                        	 System.out.println("No port defined");
                         }
                         
                         
                         // Default Port = UDP
                         if (callRuleElement.getElementsByTagName("Transport").getLength() != 0) {
                        	 fieldInProgress = "Transport";
                        	 NodeList callRuleTransport = callRuleElement.getElementsByTagName("Transport");
                        	 Element callRuleElementTransport = (Element)callRuleTransport.item(0);
                             NodeList textTransportList = callRuleElementTransport.getChildNodes();
                             System.out.println("Transport : " + 
                                     ((Node)textTransportList.item(0)).getNodeValue().trim());
                             transport = (String)textTransportList.item(0).getNodeValue().trim();
                             rule.setTransport(transport);
                         } else {
                        	 System.out.println("No transport defined");
                         }
                                                
                         callRulesProccessedListFromXml.add(rule);
                         System.out.println("Rule added " + rule.getId());
            		 }
            		 catch(Exception e) {
            			 System.out.println("Error in Rule: " + node);
            			 System.out.println("Processing parameter " + fieldInProgress);
                		 System.out.println(e);
                	 }
            		 	
            	 }
		                
             }
             //------
         }//end of if clause

        
         return callRulesProccessedListFromXml;
         //end of for loop with s var
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */
	
	public int readConfigurationFile(String fileName) {
		
	try {
		 
		 //Default configuration file Xml encoding= UTF-8 opencall-config.xml
		 if(!verifyFileAccess(fileName)) {
			 return -1;
		 }
		 
		 DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
         Document doc = docBuilder.parse (new File(fileName));

         // Normalize text representation
         doc.getDocumentElement ().normalize ();
         System.out.println ("Root element of the config file is: " + 
              doc.getDocumentElement().getNodeName());

         
         NodeList serverSettings = doc.getElementsByTagName("Server");
         NodeList databaseSettings = doc.getElementsByTagName("Database");
         NodeList policiesSettings = doc.getElementsByTagName("Policies");
         readFileParameters(serverSettings);
         readFileParameters(databaseSettings);
         readFileParameters(policiesSettings);


     }catch (SAXParseException err) {
     System.out.println ("** Parsing error" + ", line " 
          + err.getLineNumber () + ", uri " + err.getSystemId ());
     System.out.println(" " + err.getMessage ());

     }catch (SAXException e) {
     Exception x = e.getException ();
     ((x == null) ? e : x).printStackTrace ();

     }catch (Throwable t) {
     t.printStackTrace ();
     }
	return 0;
	
	}
	
	public int readCallRulesConfigurationFile(String fileName) {
		
		try {
			 
			 //Default configuration file Xml encoding= UTF-8 opencall-config.xml
			 if(!verifyFileAccess(fileName)) {
				 return -1;
			 }
			 
			 DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	         Document doc = docBuilder.parse (new File(fileName));

	         // Normalize text representation
	         doc.getDocumentElement ().normalize ();
	         System.out.println ("Root element of the config file is: " + 
	              doc.getDocumentElement().getNodeName());

	         
	         NodeList callRulesNodeList = doc.getElementsByTagName("CallRules");
	         int callRulesDefinition = callRulesNodeList.getLength();
	         // Only one definition per config file
	         if (callRulesDefinition==1) {
	        	 readCallRulesParameters(callRulesNodeList);
	         }
	         
	 

	     }catch (SAXParseException err) {
	     System.out.println ("** Parsing error" + ", line " 
	          + err.getLineNumber () + ", uri " + err.getSystemId ());
	     System.out.println(" " + err.getMessage ());

	     }catch (SAXException e) {
	     Exception x = e.getException ();
	     ((x == null) ? e : x).printStackTrace ();

	     }catch (Throwable t) {
	     t.printStackTrace ();
	     }
		return 0;
		
		}
	

    public static void main (String argv []){
    	ReadXmlFile rxml = new ReadXmlFile();
        //rxml.readConfigurationFile("opencall-config.xml");
        rxml.readCallRulesConfigurationFile("callrouting-config.xml");
    	//System.exit (0);

    }//end of main


}