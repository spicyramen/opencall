package org.mobicents.servlet.sip.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang.StringUtils;


public class RegexEngine {
	
	static private char EXCLAMATION = '!';
	static private char STAR =  '*';
	static private char POUND = '#';
	static private char PLUS =  '+';
	static private char DOT =  '.';
	static private char DASH =  '-';
	
	private String REGEX;
	private String PATTERN;
	private int[] FAILURE;
	private int MATCHPOINT;
	private static ArrayList<RegexRule> regexRules = new ArrayList<RegexRule>(); 
	
	//private static Logger logger = Logger.getLogger(CcUtils.class);
	
	public RegexEngine() {
		
	}
	
	/**
	 * Knuth-Morris-Pratt Algorithm for Pattern Matching
	 * @param string
	 * @param pattern
	 */
	
	public void KMPInit(String string, String pattern) {
		
		    this.REGEX = string;
		    this.PATTERN = pattern;
		    FAILURE = new int[pattern.length()];
		    KMPcomputeFailure();
	}
		      
	public void setRegex(String regex) {
		REGEX = regex;
	}

	public void setPattern(String pattern) {
		PATTERN = pattern;
	}
	
	public int KMPgetMatchPoint() {
		return MATCHPOINT;
	}
	  
	
	/**
	 *  Tries to find an occurrence of the pattern in the string
	 * @return
	 */
	
	private boolean KMPmatch() {
	
		    
		    int j = 0;
		    if (REGEX.length() == 0) return false;
		    
		    for (int i = 0; i < REGEX.length(); i++) {
		      while (j > 0 && PATTERN.charAt(j) != REGEX.charAt(i)) {
		        j = FAILURE[j - 1];
		      }
		      if (PATTERN.charAt(j) == REGEX.charAt(i)) { j++; }
		      if (j == PATTERN.length()) {
		        MATCHPOINT = i - PATTERN.length() + 1;
		        return true;
		      }
		    }
		    return false;
	}
		  
		  
	/** 
	  * Computes the FAILURE function using a boot-strapping process,
      * where the pattern is matched against itself.
	  */
	private void KMPcomputeFailure() {

		    int j = 0;
		    for (int i = 1; i < PATTERN.length(); i++) {
		      while (j > 0 && PATTERN.charAt(j) != PATTERN.charAt(i)) { j = FAILURE[j - 1]; }
		      	if (PATTERN.charAt(j) == PATTERN.charAt(i)) { j++; }
		      	FAILURE[i] = j;
		    }
	}
		  
	/**
	 * 
	 * @param prototype
	 * @return
	 */
	
	public static Pattern generateRegexHelper(String prototype) {
		
        return Pattern.compile(generateRegexpEngine(prototype));
    
	}
	
	/**
	 * Create Regex expression based on input
	 * @param prototype
	 * @return
	 */
	
	private static String generateRegexpEngine(String prototype) {

		StringBuilder regexPrototype = new StringBuilder();
		
        for (int i = 0; i < prototype.length(); i++) {
        	
            char c = prototype.charAt(i);
            if (Character.isDigit(c)) {
            	regexPrototype.append(c);
            } else if (c==EXCLAMATION) {
            	regexPrototype.append("(.*)");
            } else if (c==STAR) {
            	regexPrototype.append("\\*");
            } else if (c==POUND) {
            	regexPrototype.append("\\#");
            } else if (c==PLUS) {
            	regexPrototype.append("\\+");
            } else if (c==DOT) {
            	regexPrototype.append("\\.");
            } else if (c==DASH) {
            	regexPrototype.append("\\-");
            } else if (c=='X' || c=='x') {
            	regexPrototype.append("\\d"); 
            } else { 
            	 System.err.println("Unknown character: " +  c);
            	 return null;
                 
            }
        }

        if(validateRule(regexPrototype.toString()))
        	return regexPrototype.toString();
        else
        	return null;
	        
    }
	
	/**
	 * Verifies if Regex contains invalid characters (more than one +)
	 * @param regexPrototype
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean validateRule(String regexPrototype) {
		
		String plus = "\\+";
		String all = "(.*)";
		try {
				
			Pattern srcP = Pattern.compile(regexPrototype);
			if(StringUtils.countMatches(regexPrototype, plus)>1) {
				System.err.println("Invalid Regex: " + regexPrototype );
				return false;
			}
			else if(StringUtils.countMatches(regexPrototype, all)>1) {
				System.err.println("Invalid Regex: " + regexPrototype );
				return false;
			}
			else {
				return true;
			}
			
		} catch (PatternSyntaxException ex) {
	    	ex.printStackTrace();
	    	System.out.println("Syntax error in the regular expression");
	    	return false;
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			System.out.println("Syntax error in the replacement text (unescaped $ signs?)");
			return false;
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
			System.out.println("Non-existent backreference used the replacement text");
			return false;
		}
				
			
	}
	
	/**
	 * Groups digit common patterns: \d\d\d\d\d\d\d\d\d\d\d = ((\d){11})
	 * @param regexPrototype
	 * @return
	 */
	
	private static String generateSimpleRegexGroup(String regexPrototype) {
		
		String digits = "\\d";
		String all    = "(.*)";
		StringBuilder simplifiedRegex = new StringBuilder();
	    List<RegexGroup> regexGroupObjectList = new ArrayList<RegexGroup>();
		List<Integer> regexGroupElementsList = new ArrayList<Integer>();
		
		int lastIndex = 0;
    	int count = 0;
    	int groupId = 0;
    	boolean containsAll = false;
    	RegexRule newRule = new RegexRule();
		
		
    	if(!validateRule(regexPrototype) || regexPrototype.length() <= 0) {
    		return null;
    	}
    	
    	/**
    	 * Call Knuth-Morris-Pratt algorithm
    	 */
    	
    	if(StringUtils.countMatches(regexPrototype, all) > 0) {
    		
    		RegexEngine matcherAll = new RegexEngine();
        	matcherAll.KMPInit(regexPrototype,all);
        	
        	if (matcherAll.KMPmatch()) {
        		System.out.println("RegexEngine() Knuth-Morris-Pratt() Match! String: " + regexPrototype + " Index match(ALL): " + matcherAll.KMPgetMatchPoint());
        	}
        	
        	containsAll = true;
        	
        	if(StringUtils.countMatches(regexPrototype, digits) == 0) {
    			newRule.setRuleValue(regexPrototype);
    			newRule.setSimplifiedRuleValue(regexPrototype);
        		newRule.setNumberOfGroups(1);
        		newRule.regexGroupsItems.add("(.*)");
        		regexRules.add(newRule);
        		
    		}
    		     	
    	}
    
    	
    	if(StringUtils.countMatches(regexPrototype, digits)>0) {
    		
    		
    		
    		/**
    		 * Find index of match digits and stores in Main Array
    		 */
    		
				while (lastIndex != -1) {			
					lastIndex = regexPrototype.indexOf(digits, lastIndex);
					if (lastIndex != -1) {
						regexGroupElementsList.add(lastIndex);
						lastIndex += digits.length();
						count++;	
					}
				}
		
				/**
				 * Find number of groups
				 * Create Object
				 * Process String
				 * 	
				 */
				for (int i=0;i<regexGroupElementsList.size();i++) {		
					
					if(i+1 < regexGroupElementsList.size()) {
						if(regexGroupElementsList.get(i) == regexGroupElementsList.get(i+1) - digits.length()) {	
							if(groupId==0) {
								//System.out.printf("New Regex Group found index: %d\n",regexGroupElementsList.get(i));
								regexGroupObjectList.add(new RegexGroup(1));
								regexGroupObjectList.get(0).processElements(1, regexGroupElementsList);	
								regexGroupObjectList.get(0).setIndexStart(regexGroupElementsList.get(i));
								groupId++;
							}
						}
						else {
							//System.out.printf("New Regex Group found index: %d\n",regexGroupElementsList.get(i+1));
							regexGroupObjectList.add(new RegexGroup(groupId+1));
							regexGroupObjectList.get(groupId).processElements(groupId+1, regexGroupElementsList);
							regexGroupObjectList.get(groupId).setIndexStart(regexGroupElementsList.get(i+1));
							groupId++;
						}
					} 
					else {			
						// Only 1 element
						if(regexGroupObjectList.size()==0) {
							regexGroupObjectList.add(new RegexGroup(1));
							regexGroupObjectList.get(0).processElements(1, regexGroupElementsList);	
							regexGroupObjectList.get(0).setIndexStart(regexGroupElementsList.get(i));
						}	
					}
				}
	
						System.out.println("RegexEngine() Pattern found: " + count + " time(s). Regex Groups: " + regexGroupObjectList.size() + " " + " All elements: " + regexGroupElementsList); 	
				   
					
						/**
						 * Convert Original string to New String
						 */
						int obj = 0;
						int ptr = 0;
		
						while(ptr < regexPrototype.length()) {
						
							if(ptr == regexGroupObjectList.get(obj).getIndexStart()) {
								simplifiedRegex.append("((\\d){" + regexGroupObjectList.get(obj).getElements() + "})");
								newRule.regexGroupsItems.add("((\\d){" + regexGroupObjectList.get(obj).getElements() + "})");
								ptr += regexGroupObjectList.get(obj).getOffset();
								
								if(regexGroupObjectList.size()>1 && obj+1 < regexGroupObjectList.size())
									obj++;
								
							}
							else {
								simplifiedRegex.append(regexPrototype.charAt(ptr));
								ptr++;
							}
						}
			
						simplifiedRegex.insert(0,"^");
						simplifiedRegex.insert(simplifiedRegex.length(),"$");
    	} 
    	else 
    	{
    		return null;
    	}
    	
    	
    	if(validateRule(simplifiedRegex.toString())) {
    		System.out.println("RegexEngine() Original Regex: " + regexPrototype + " New Simplified Regex: " + simplifiedRegex);
    		System.out.println("-----------------------------------------------------------------");
    	
    		if (containsAll) {		
    			newRule.setRuleValue(regexPrototype);
    			newRule.setSimplifiedRuleValue(simplifiedRegex.toString());
        		newRule.setNumberOfGroups(regexGroupObjectList.size()+1);
        		newRule.regexGroupsItems.add("(.*)");
        		regexRules.add(newRule);
    		}
    		else {
    			newRule.setRuleValue(regexPrototype);
    			newRule.setSimplifiedRuleValue(simplifiedRegex.toString());
        		newRule.setNumberOfGroups(regexGroupObjectList.size());
        		regexRules.add(newRule);
    		}
    		
    		return simplifiedRegex.toString();
    	}	
    	else
    		return null;
	
	   	
	}
	
	
	
	private static void testRegexRule(String input) {
		
		try {
			Pattern pattern = generateRegexHelper(input);
			System.out.println("-----------------------------------------------------------------");
			System.out.println(String.format("testRegexRule() String: %s --> Regex value: %s", input, pattern));
			generateSimpleRegexGroup(pattern.toString());
		}
		catch(Exception e) {	
			e.printStackTrace();
			System.out.println("testRegexRule() Invalid input for Wildcard: " + input);
		}
        
    }
	
	 public static void main(String[] args) {
		 
	/**
	 * Example: TRANSFORM=("2","TRUE","WILDCARD","XXXXXXXX","18668643232**XXXXXXXX","CALLED","FALSE")
	 * 			22223333 Match RULE XXXXXXXX
	 * 			XXXXXXXX is converted to \d\d\d\d\d\d\d\d
	 * 			\d\d\d\d\d\d\d\d is simplified to ((\d){8})
	 * 			(\d){8} is matched against WILDCARD RULE:
	 * 			18668643232**XXXXXXXX
	 * 			18668643232**XXXXXXXX is converted to 18668643232\*\*\d\d\d\d\d\d\d\d
	 * 			
	 */
		
	        String[] prototypes = {
	            "22223333",
	            "18668643232**22223333",
	            "+!",
	            "9.14082186575",
	            "+1408XXXXXXX",
	            "+1!",
	            "+5255579469",
	            "+14082185475",
	            "+52-5557969469",
	            "22223333!",
	            "+19001236575",
	            "XXXXXXXX",
	            "18668643232**XXXXXXXX",
	            "91XXXXXXXXXX",
	            "+!",
	            "011!",
	            "18668643232**XXXXXXXX**XXXXXXXX**XX!",
	            "X11",
	            "9XX!",
	            "11XX",
	            "XXXXX.XX",
	            "001408XXXXXXX"
	        };

	        for (String prototype : prototypes) {
	        	testRegexRule(prototype);
	        }
	        
	        System.out.println("");
	        for (int i=0;i<regexRules.size();i++) {
	        	System.out.println(regexRules.get(i).getRulesValues());
	        	regexRules.get(i).displayGroups();
	        }
	    }
	 
}
