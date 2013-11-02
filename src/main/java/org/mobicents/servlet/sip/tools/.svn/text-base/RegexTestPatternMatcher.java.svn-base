package org.mobicents.servlet.sip.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTestPatternMatcher {
	
	
  public static String targetText = "22224444@videolab.att.com";
  public static String matchPattern = "(.*)@(.*)";
  public static String replacePattern = "+$1@$2";
  public static String resultText = "";
  

  public RegexTestPatternMatcher() {
	  
  }
  
  
  public static void main(String[] args) {
	 
	Pattern original = Pattern.compile(matchPattern);
    Matcher matcher = original.matcher(targetText);
    
    RegexTestResult result = new RegexTestResult();
    result.setText(targetText);
    result.setMatches(matcher.matches());
    matcher.reset();
    result.setReplacedText(matcher.replaceAll(replacePattern));
    
    System.out.println("Original text: " + result.getText());
    
    /**
     * while (matcher.find()) {  
        String matchText = matcher.group();
        int start = matcher.start();
        int end = matcher.end();
        result.addGroup(new Group(matchText, start, end));
    }
     */
    
    
    System.out.println("Replaced text: " + result.getReplacedText());
    
   
    // Now create a new pattern and matcher to replace whitespace with tabs
    
 
    
 
   
    
    
 

  
  
  }
} 