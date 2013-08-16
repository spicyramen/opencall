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
	  
	//private static Logger logger = Logger.getLogger(CcUtils.class);
	
	public RegexEngine() {
		
	}
	
	/**
	 * Knuth-Morris-Pratt Algorithm for Pattern Matching
	 * @param string
	 * @param pattern
	 */
	
	public void KMPMatch(String string, String pattern) {
		
		    this.REGEX = string;
		    this.PATTERN = pattern;
		    FAILURE = new int[pattern.length()];
		    computeFAILURE();
	}
		      
	public void setRegex(String regex) {
		REGEX = regex;
	}

	public void setPattern(String pattern) {
		PATTERN = pattern;
	}
	
	public int getMatchPoint() {
		return MATCHPOINT;
	}
	  
		  
	private boolean match() {
	// Tries to find an occurrence of the pattern in the string
		    
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
	private void computeFAILURE() {

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
	public static Pattern generateRegex(String prototype) {
        return Pattern.compile(generateRegexpEngine(prototype));
    }
	
	/**
	 * Create Regex expression based on input
	 * @param prototype
	 * @return
	 */
	private static String generateRegexpEngine(String prototype) {

		StringBuilder stringBuilder = new StringBuilder();
		
        for (int i = 0; i < prototype.length(); i++) {
        	
            char c = prototype.charAt(i);
            if (Character.isDigit(c)) {
                stringBuilder.append(c);
            } else if (c==EXCLAMATION) {
            	 stringBuilder.append("(.*)");
            } else if (c==STAR) {
           	     stringBuilder.append("\\*");
            } else if (c==POUND) {
              	 stringBuilder.append("\\#");
            } else if (c==PLUS) {
             	 stringBuilder.append("\\+");
            } else if (c==DOT) {
            	 stringBuilder.append("\\.");
            } else if (c==DASH) {
                 stringBuilder.append("\\-");
            } else if (c=='X' || c=='x') {
                 stringBuilder.append("\\d"); 
            } else { 
            	 System.err.println("Unknown character: " +  c);
            	 return null;
                 
            }
        }

        validateRule(stringBuilder.toString());
		return stringBuilder.toString();
	        
    }
	
	/**
	 * Verifies if Regex contains invalid characters (more than one +)
	 * @param regexPrototype
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean validateRule(String regexPrototype) {
		
		
		String plus = "\\+";
		try {
				
			Pattern srcP = Pattern.compile(regexPrototype);
			if(StringUtils.countMatches(regexPrototype, plus)>1) {
				System.err.println("Invalid Regex: " + regexPrototype );
				return false;
			}
		} catch (PatternSyntaxException ex) {
	    	ex.printStackTrace();
	    	System.out.println("Syntax error in the regular expression");
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			System.out.println("Syntax error in the replacement text (unescaped $ signs?)");
		} catch (IndexOutOfBoundsException ex) {
			ex.printStackTrace();
			System.out.println("Non-existent backreference used the replacement text");
		}
				
		System.out.println("validateRule() Valid Regex generated: " + regexPrototype);
		return true;
			
	}
	
	/**
	 * Groups digit common patterns: \d\d\d\d\d\d\d\d\d\d\d = ((\d){11})
	 * @param regexPrototype
	 * @return
	 */
	
	private static String simplifyregexGroup(String regexPrototype) {
		
		String digits = "\\d";
		
		List<Integer> regexGroupElementsList = new ArrayList<Integer>();
		int groups = 0;
		int lastIndex = 0;
    	int count = 0;
    	
    	if(regexPrototype.length()<=0 || StringUtils.countMatches(regexPrototype, digits)==0) {
    		return null;
    	}
    	
    	/**
    	 * Call Knuth-Morris-Pratt algorithm
    	 */
    	
    	RegexEngine matcher = new RegexEngine();
    	matcher.KMPMatch(regexPrototype,digits);
    	
    	if (matcher.match()) {
    		System.out.println("-----------------------------------------------------------------");
    		System.out.println("Knuth-Morris-Pratt match. Index match: " + matcher.getMatchPoint());
    	}
    
		/**
		 * Find index of match digits
		 */
    	
    	if(StringUtils.countMatches(regexPrototype, digits)>0) {
    		
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
		 * 	
		 */
			
		for (int i=0;i<regexGroupElementsList.size();i++) {				
				if(i+1 < regexGroupElementsList.size()) {
						if(regexGroupElementsList.get(i) == regexGroupElementsList.get(i+1) - digits.length()) {	
							if(groups==0) {
								System.out.printf("New Regex Group found index: %d\n",regexGroupElementsList.get(i));
								groups++;
							}
						}
						else {
							System.out.printf("New Regex Group found index: %d\n",regexGroupElementsList.get(i+1));
							groups++;
						}
				}	
				
		}
	
		System.out.println("Pattern found: " + count + " time(s). Regex Groups: " + groups + " Indexes: " + regexGroupElementsList);
		System.out.println("-----------------------------------------------------------------");
    	
		/**
		 * Create new Regex
		 */
		
		//System.out.println("Pattern found: " + count + " time(s). Regex Groups: " + groups);
		//System.out.println("Original Regex: " + regexPrototype + " New Regex: " + regexPrototype);
		
		
    	}
		return "";
	}
	



	
	
	private static void test(String input) {
		
        Pattern pattern = generateRegex(input);
        System.out.println(String.format("Test() String: %s -> Regex: %s", input, pattern));
        
        validateRule(pattern.toString());
    	simplifyregexGroup(pattern.toString());
        
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
	            "9.14082186575",
	            "+5255579469",
	            "+14082185475",
	            "+52-5557969469",
	            "!22223333!",
	            "+19001236575",
	            "XXXXXXXX",
	            "18668643232**XXXXXXXX**XX",
	            "91XXXXXXXXXX",
	            "+!",
	            "011!",
	            "XX**18668643232**XXXXXXXX**X**XX",
	            "XX"
	        };

	        for (String prototype : prototypes) {
	            test(prototype);
	        }
	    }
	 
}
