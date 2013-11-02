package org.mobicents.servlet.sip.tools;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class RegexRule {

	private String simplifiedRuleValue = "";
	private String RuleValue = "";
	private int groupsNumber = 0;
	public List<String> regexGroupsItems = new ArrayList<String>();
	private static Logger logger = Logger.getLogger(RegexRule.class);
	
	
	public RegexRule(String rule, String ruleSimplified) {
		logger.info("RegexRule() New Regex rule object: " + rule + " created");
		setRuleValue(rule);
		setSimplifiedRuleValue(ruleSimplified);
			
	}
	
	public String getGroup(int index) {
		if(index > regexGroupsItems.size()) {
			return "";
		}
		else {
			return regexGroupsItems.get(index);
		}
	}
	public RegexRule() {
		logger.info("RegexRule() New Regex rule object created");
			
	}

	public String getSimplifiedRuleValue() {
		return simplifiedRuleValue;
	}

	public void setSimplifiedRuleValue(String simplifiedRuleValue) {
		this.simplifiedRuleValue = simplifiedRuleValue;
	}

	public String getRuleValue() {
		return RuleValue;
	}

	public void setRuleValue(String ruleValue) {
		RuleValue = ruleValue;
	}
	
	public String getRuleInfo() {
		return "Rule: [" + RuleValue + "] Simplified: [" + simplifiedRuleValue + "] " + "Groups: " + groupsNumber + " ";
	}

	public void setNumberOfGroups(int groupsNum) {
		groupsNumber = groupsNum;
	}
	
	public int getNumberOfGroups() {
		return groupsNumber;
	}
	
	public void displayGroups() {
		logger.info("Rule: [" + RuleValue + "] Simplified: [" + simplifiedRuleValue + "] ");
		 for (int i=0;i<regexGroupsItems.size();i++) {
	        	logger.info(regexGroupsItems.get(i).toString());
	        }
	}
	
}
