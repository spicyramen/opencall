package org.mobicents.servlet.sip.model.cc;

import java.util.Comparator;

class CcRule implements Comparable<CcRule> {

	public int ruleNumber;
	public int rulePriority;
	public String ruleTrunk;
	public int rulePort;
	public String ruleTransport;

	/**
	 * 
	 * @param num
	 * @param pri
	 */
	public CcRule(int num, int pri) {
		ruleNumber = num;
		rulePriority = pri;
	}

	/**
	 * 
	 * @param num
	 * @param pri
	 * @param trunk
	 * @param port
	 * @param transport
	 */

	public CcRule(int num, int pri, String trunk, int port,String transport) {
		ruleNumber = num;
		rulePriority = pri;
		ruleTrunk = trunk;
		rulePort = port;
		ruleTransport = transport;
	}

	public int getRuleNumber() {
		return ruleNumber;
	}

	public int getRulePriority() {
		return rulePriority;
	}

	public String getRuleTrunk() {
		return ruleTrunk;
	}

	public int getRulePort() {
		return rulePort;
	}

	public String getRuleTransport(){
		return ruleTransport;
	}
	
	public int compareTo(CcRule uno) {
		int compareRuleValue = ((CcRule) uno).getRuleNumber();
		// Ascending order
		return this.ruleNumber - compareRuleValue;
	}

	@Override
	public String toString() {
		return "rule id=" + this.ruleNumber + ", rule priority="
				+ this.rulePriority;
	}

	public static Comparator<CcRule> RuleNumberComparator = new Comparator<CcRule>() {

		public int compare(CcRule e1, CcRule e2) {
			return (int) (e1.getRuleNumber() - e2.getRuleNumber());
		}
	};

	public static Comparator<CcRule> RulePriorityComparator = new Comparator<CcRule>() {

		public int compare(CcRule e1, CcRule e2) {
			return (int) (e1.getRulePriority() - e2.getRulePriority());
		}
	};
}
