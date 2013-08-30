package org.mobicents.servlet.sip.tools;

import java.util.ArrayList;
import java.util.List;

public class RegexRule {

	private String simplifiedRuleValue = "";
	private String RuleValue = "";
	private int groupNumber = 0;
	public List<String> regexGroupsItems = new ArrayList<String>();
	
	public RegexRule(String rule, String ruleSimplified) {
		System.out.println("RegexRule() New Regex rule object: " + rule + " created");
		setRuleValue(rule);
		setSimplifiedRuleValue(ruleSimplified);
			
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
	
	public String getRulesValues() {
		return "Rule: [" + RuleValue + "] Simplified: [" + simplifiedRuleValue + "] " + "Groups: " + groupNumber + " ";
	}

	public void setNumberOfGroups(int groupsNum) {
		groupNumber = groupsNum;
	}
	
	public int getGroupNumber() {
		return groupNumber;
	}
	
}
