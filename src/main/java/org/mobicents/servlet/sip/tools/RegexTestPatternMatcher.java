package org.mobicents.servlet.sip.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTestPatternMatcher {
	
	
  public static final String TEXT = "+14082186575";
  public static final String REGEX_SRC = "^\\+1408((\\d){7})$";
  public static final String REGEX_DST = "^001408((\\d){7})$";
  

  public static void main(String[] args) {
	 
	Pattern original = Pattern.compile(REGEX_SRC);
    Matcher matcher = original.matcher(TEXT);
    String str1 = "";
    
    // Check all occurrences
    if (matcher.find()) {
      System.out.print("Start index: " + matcher.start(1));
      System.out.print(" End index: " + matcher.end(1) + " ");
      System.out.println("String: " + matcher.group(1));
      str1 =  matcher.group(1);
    }
    
    System.out.println("Matching src string: " + str1);
    // Now create a new pattern and matcher to replace whitespace with tabs
    

    Pattern dstpattern = Pattern.compile(REGEX_DST);    
    Matcher matcher2 = dstpattern.matcher(TEXT);
    String str2 = "";
    
    if (matcher2.find()) { 
    	  System.out.print("Start index: " + matcher2.start(1));
          System.out.print(" End index: " + matcher2.end(1) + " ");
          System.out.println("String: " + matcher2.group(1));
          str2 =  matcher2.group(1);
    }
    
    System.out.println("Matching string: " + str2);
  //  System.out.println(TEXT.replaceAll(dst,"$1")); 
    
    
 

  
  
  }
} 