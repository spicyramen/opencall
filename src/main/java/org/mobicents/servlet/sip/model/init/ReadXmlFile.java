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
	
    public static void main (String argv []){
    	ReadXmlFile rxml = new ReadXmlFile();
        rxml.readConfigurationFile("opencall-config.xml");
        //System.exit (0);

    }//end of main


}