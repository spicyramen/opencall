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
 * Return sipURI after parsing rule priority Algorithm: 
 * Find userURI, domainURI and portURI
 * Count Rules Match priority Order by priority 
 * If REGEX and _DNS_ select rule and return unmodified sipURI 
 * else if REGEX and not _DNS_ select rule: 
 * 
 * Parse rule Domain and Port parse SIP URI Domain and Port replace URI Domain with rule Domain 
 * if URI Port and if rule Port not empty replace URI port with rule Port 
 * else (rule Port empty) attach original port to SIP URI port
 * 
 */

@SuppressWarnings("unused")
public class CcFindMatchTransformationRule {

	private static Logger logger = Logger.getLogger(CcFindMatchTransformationRule.class);
	private Map<Object, String> rulesMatched = new HashMap<Object, String>();
	private CcTransformationRule[] ruleArray = null;
	private String originalURI[];
	private String resultSipURI;
	private CcUtils utilObj = new CcUtils();


	/**
	 * Constructor
	 * 
	 * @param sipURI
	 * @param rulesDA
	 */

	public CcFindMatchTransformationRule(String[] sipURI, Map<Object, String> rulesDA) {
		logger.info("CcFindMatchRule() Create new object CcFindMatchRuleCdcc");
		if (!rulesDA.isEmpty() && rulesDA.size() >= 1) {
			try {
				rulesMatched = rulesDA;
				originalURI = sipURI;
				ruleArray = new CcTransformationRule[rulesMatched.size()];
			} catch (Exception e) {
				e.printStackTrace();
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
			ruleValue = utilObj.getTransformValue(0, value);
			//CcTransformationRule(int num,boolean enabled,String type, String srcNumber, String dstNumber,String apply, boolean block) {
			
			ruleArray[ruleFound] = (new CcTransformationRule(Integer.parseInt(ruleValue[1]
					.toString())));
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
				Arrays.sort(ruleArray, CcTransformationRule.RuleNumberComparator);
				return (ruleArray[0].transformNumber);
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
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

}
