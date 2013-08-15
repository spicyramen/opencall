package org.mobicents.servlet.sip.tools;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexEngine {
	/**
	 * @param args
	 */

	/**
	 * NUMERIC: Replace 
	 * 
	 * WILDCARD: Replace
	 *  Match 
	 * 	routeString = routeString.replace("X", "\\d");
	 *	routeString = routeString.replace(".", "\\.");
	 *	routeString = routeString.replace("!", "\\d+");
	 *	routeString = routeString.replace("+", "\\+");
	 *	
	 * REGEX: Replace
	 * 
	 * @param args
	 */
	
	@SuppressWarnings({ "static-access", "unused" })
	public static void main(String[] args) {
		

	
		
		//String result = "";
		/**
		 * Matcher transformMatcher = Pattern.compile(vcsRegex).matcher(number);
		transformMatcher.find();
		
		try {
			StringBuffer sb = new StringBuffer();
	        while(transformMatcher.find()){
	        	transformMatcher.appendReplacement(sb,replace);
	        }
	        transformMatcher.appendTail(sb);
	        System.out.println(sb.toString());	
		}
		catch(Exception e) {
			
			System.out.println("No match found");
			e.printStackTrace();
		}
		 * 
		 */
		
		 String str = "xyaahhfhajfahj{adhadh}fsfhgs{sfsf}";
	        String str1 = str.replaceAll("\\{[a-zA-z0-9]*\\}", " ");// to replace string within "{" & "}" with " ".
	        String str2 = str.replaceFirst("\\{[a-zA-z0-9]*\\}", " ");// to replace first string within "{" & "}" with " ".
	        System.out.println(str1);
	        System.out.println(str2);
	        
		/**
		 * 
		 * 
		 * 
		 */
		
		String num1 = "22223333@cisco.com";
		String num2 = "+525557969469";
		String initialRegex = "^(\\d{8})\\@(cisco.com)$";
		String replaceRegex = "$1@yahoo.com";
		String initialWildcard = "^\\+(.*)";
		String replaceWildcard = "011$1";
	
		
		try {
			Pattern srcP = Pattern.compile(initialRegex);
			
			if(srcP.matches(initialRegex, num1)) {
				System.out.println("Rule is matched");
			}
			
			Pattern dstP = Pattern.compile(replaceRegex);
			System.out.println("Initial str: " + num1);
			
			
			if(num1.matches(initialRegex)) {
				num1 = num1.replaceAll(initialRegex, replaceRegex);
				System.out.println("Changed str: " + num1);
			}
		
		} catch (PatternSyntaxException ex) {
	    // Syntax error in the regular expression
		} catch (IllegalArgumentException ex) {
	    // Syntax error in the replacement text (unescaped $ signs?)
		} catch (IndexOutOfBoundsException ex) {
	    // Non-existent backreference used the replacement text
		}
		
		
		try {
			Pattern srcP = Pattern.compile(initialWildcard);
			
			if(srcP.matches(initialWildcard, num2)) {
				System.out.println("Rule is matched");
			}
			
			Pattern dstP = Pattern.compile(replaceWildcard);
			System.out.println("Initial str: " + num2);
			
			
			if(num2.matches(initialWildcard)) {
				num2 = num2.replaceAll(initialWildcard, replaceWildcard);
				System.out.println("Changed str: " + num2);
			}
		
		} catch (PatternSyntaxException ex) {
	    // Syntax error in the regular expression
		} catch (IllegalArgumentException ex) {
	    // Syntax error in the replacement text (unescaped $ signs?)
		} catch (IndexOutOfBoundsException ex) {
	    // Non-existent backreference used the replacement text
		}
		
		
		/**
		 * Matcher m = p.matcher(str);
		 * List<String> matches = new ArrayList<String>();
		while(m.find()){
		    matches.add(m.group());
		}
		
		System.out.println("Size: " + matches.size());
		for(String n: matches)
		{
		    System.out.println(" " + n);
		}
		 */
		
		
		/**
		 * 
		 * 
		 * 
		 * 		try {
			
			String line = "22223333";
	        StringBuilder sb = new StringBuilder();

	        if(number.matches(regexSrc)){ 
	            sb.append(line.replaceAll(regexDst, number) + "\n");
	        }else{
	        	System.out.println("ELSE");
	        	sb.append(line + "\n");
	        
	        }
	        
	        System.out.println(sb.toString());

		}
		catch(Exception e) { 
			
			System.out.println("Error");
		}
		
		 * 
		 */
		

	
        
	}
}
