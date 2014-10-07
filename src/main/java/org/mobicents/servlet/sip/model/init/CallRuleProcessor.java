package org.mobicents.servlet.sip.model.init;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CallRuleProcessor {
	
	int callRulesLength = 0;
	int callTransformRulesLength = 0;
	int callRouteListsLength = 0;
	private ArrayList<CallRule> callRulesProccessedListFromXml = new ArrayList<CallRule>();
	
	
	private Object lock = new Object();
	
	private void incrementRule() {
		synchronized(lock) {
			callRulesLength++;
		}
	}
	
	public CallRuleProcessor() {
		
	}
	
	/**
	 * 
	 * @param serverSettings
	 * @return
	 */
	public ArrayList<CallRule> readCallRulesParameters(NodeList callRulesSection) {
		
		
		
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
                         incrementRule();
                         
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

	public void validateCallRules() {
		
		
	}
	
}
