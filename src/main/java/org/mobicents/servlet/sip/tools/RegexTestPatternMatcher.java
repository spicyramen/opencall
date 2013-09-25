package org.mobicents.servlet.sip.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTestPatternMatcher {
	
	
  public static String TEXT = "+14082186575";
  public static String NEW_TEXT = "";
  public static String REGEX_SRC = "^\\+1408((\\d){7})$";
  public static String REGEX_DST = "^001408((\\d){7})$";
  

  public static void main(String[] args) {
	 
	Pattern original = Pattern.compile(REGEX_SRC);
    Matcher matcher = original.matcher(TEXT);
    
    
    // Check all occurrences
    if (matcher.find()) {
      System.out.println("Input: " + TEXT);	
      System.out.println("Regex: " + REGEX_SRC);
      System.out.print("Start index: " + matcher.start(1));
      System.out.print(" End index: " + matcher.end(1) + " ");
      System.out.println("String Match: " + matcher.group(1));
      NEW_TEXT =  matcher.group(1).toString();
    }
    
    System.out.println("Matching src string: " + NEW_TEXT);
    // Now create a new pattern and matcher to replace whitespace with tabs
    
    REGEX_DST = REGEX_DST.replace("^", "");
    REGEX_DST = REGEX_DST.replace("$", "");
    System.out.println("Regex: " + REGEX_DST);
    
 
   
    
    
 

  
  
  }
} 