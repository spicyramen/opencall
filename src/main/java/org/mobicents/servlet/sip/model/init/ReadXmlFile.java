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

	
	
			
	public ReadXmlFile() {
		
	}
	
	
	
	/**
	 * 
	 * @param configurationFileName
	 * @return
	 * @throws IOException
	 */
	private boolean verifyFileAccess(String configurationFileName,int fileType)
			throws IOException {
		
		if (configurationFileName != "" || configurationFileName != null) {
			if (fileType ==0)
				System.out.println("System file found");
			else if(fileType==1) {
				System.out.println("Call Routing file found");
			}
			else {
				return false;
			}
				
		} else {
			return false;
		}

		try {
			File configFile = new File(configurationFileName);
			if (configFile.exists() && configFile.canRead()) {
					return true;
				} else {
					//logger.info("verifyFileAccess Inaccessible file " + configurationFileName);
					return false;
				}
		}
		catch (Exception e) {
			//logger.info("verifyFileAccess Inaccessible file " + e);
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
	
	
	public NodeList parseCallRoutingConfigurationFile(String fileName) {
		
		try {
			 
			 //Default configuration file Xml encoding= UTF-8 opencall-config.xml
			 if(!verifyFileAccess(fileName,1)) {
				 return null;
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
	        	 return callRulesNodeList;
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
		
		return null;
		
		}
	
	/**
	 * 
	 * @param fileName
	 * @return
	 */
	
	public int readFileConfigurationFile(String fileName) {
		
	try {
		 
		 //Default configuration file Xml encoding= UTF-8 opencall-config.xml
		 if(!verifyFileAccess(fileName,0)) {
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
         NodeList systemSettings = doc.getElementsByTagName("System");
         readFileParameters(serverSettings);
         readFileParameters(databaseSettings);
         readFileParameters(policiesSettings);
         readFileParameters(systemSettings);

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
	
	/**
	 * 
	 * @param argv
	 */
    public static void main (String argv []){
    	
    	ReadXmlFile rxml = new ReadXmlFile();
    	CallRuleProcessor callRuleCc = new CallRuleProcessor();
    	NodeList callRuleInformation;
    	
    	// Read main configuration file
    	rxml.readFileConfigurationFile("opencall-config.xml");
    	rxml.validateConfigurationRules();
    	// Read main call-routing configuration file
    	callRuleInformation = rxml.parseCallRoutingConfigurationFile("callrouting-config.xml");
        callRuleCc.readCallRulesParameters(callRuleInformation);
    	callRuleCc.validateCallRules();

    }//end of main



	private void validateConfigurationRules() {
		// TODO Auto-generated method stub
		
	}


}