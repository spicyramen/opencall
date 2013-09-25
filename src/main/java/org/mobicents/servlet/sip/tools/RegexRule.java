package org.mobicents.servlet.sip.tools;

import java.util.ArrayList;
import java.util.List;

public class RegexRule {

	private String simplifiedRuleValue = "";
	private String RuleValue = "";
	private int groupsNumber = 0;
	public List<String> regexGroupsItems = new ArrayList<String>();
	
	public RegexRule(String rule, String ruleSimplified) {
		System.out.println("RegexRule() New Regex rule object: " + rule + " created");
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
		System.out.println("RegexRule() New Regex rule object created");
			
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
		System.out.println("Rule: [" + RuleValue + "] Simplified: [" + simplifiedRuleValue + "] ");
		 for (int i=0;i<regexGroupsItems.size();i++) {
	        	System.out.println(regexGroupsItems.get(i).toString());
	        }
	}
	
}
