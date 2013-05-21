package org.mobicents.servlet.sip.model.cc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 * Return sipURI after parsing rule priority Algorithm: Find userURI, domainURI
 * and portURI Count Rules Match priority Order by priority If REGEX and _DNS_
 * select rule and return unmodified sipURI else if REGEX and not _DNS_ select
 * rule: parse rule Domain and Port parse SIP URI Domain and Port replace URI
 * Domain with rule Domain if URI Port if rule Port not empty replace URI port
 * with rule Port else (rule Port empty) attach original port to SIP URI port
 * 
 */

@SuppressWarnings("unused")
public class CcFindMatchRule {

	private Map<Object, String> rulesMatched = new HashMap<Object, String>();
	private CcRule[] ruleArray = null;
	private String originalURI[];
	private String resultSipURI;
	private CcUtils utilObj = new CcUtils();
	private static Logger logger = Logger.getLogger(CcFindMatchRule.class);

	/**
	 * Constructor
	 * 
	 * @param sipURI
	 * @param rulesDA
	 */

	public CcFindMatchRule(String[] sipURI, Map<Object, String> rulesDA) {
		logger.info("CcFindMatchRule() Create new object CcFindMatchRuleCdcc");
		if (!rulesDA.isEmpty() && rulesDA.size() >= 1) {
			try {
				rulesMatched = rulesDA;
				originalURI = sipURI;
				ruleArray = new CcRule[rulesMatched.size()];
			} catch (Exception e) {
				rulesMatched = null;
				originalURI = null;
			}

		}
	}

	public int CcProcessBestMatchAlgorithm(int type) {
		CcPopulateRouteCache();
		int resultRule = orderRules(type);
		logger.info("Rule match: [" + resultRule + "]");
		logger.info("Rules list sorted by Priority:\n"
				+ Arrays.toString(ruleArray));
		return resultRule;
	}

	/**
	 * Order By Priority
	 */

	@SuppressWarnings("rawtypes")
	public void CcPopulateRouteCache() {
		logger.info("CcPopulateRouteCache() Ordering Route Patterns");
		int ruleFound = 0;
		String[] ruleValue = null;
		Set<?> potentialSet = rulesMatched.entrySet();
		Iterator<?> it = potentialSet.iterator();

		while (it.hasNext()) {
			Map.Entry mapa = (Map.Entry) it.next();
			int key = (Integer) mapa.getKey(); 
			String value = (String) mapa.getValue();
			ruleValue = utilObj.getRuleValue(0, value);
			ruleArray[ruleFound] = (new CcRule(Integer.parseInt(ruleValue[1]
					.toString()), Integer.parseInt(ruleValue[2].toString())));
			ruleFound++;
		}
	}

	/**
	 * 
	 * @param type
	 */

	public int orderRules(int type) {
		/**
		 * Type 1: Order by rule Number Type 2: Order by rule Priority
		 */
		try {
			if (type == 1) {
				Arrays.sort(ruleArray, CcRule.RuleNumberComparator);
				return (ruleArray[0].ruleNumber);
			} else if (type == 2) {
				Arrays.sort(ruleArray, CcRule.RulePriorityComparator);
				return (ruleArray[0].ruleNumber);
			} else {
				return 0;
			}
		} catch (Exception e) {
			return 0;
		}

	}

	/**
	 * 
	 * @param type
	 */

	public int getTotalRules() {
		return rulesMatched.size();
	}

	/**
	 * 
	 * @param iToken
	 * @param routeValue
	 * @return
	 */

}
