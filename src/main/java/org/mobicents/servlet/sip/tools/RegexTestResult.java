package org.mobicents.servlet.sip.tools;

import java.util.ArrayList;
import java.util.List;

public class RegexTestResult {
	    private String text;

	    
	    private List<Group> groups = new ArrayList<Group>();

	    
	    private String replacedText;

	    
	    private boolean matches;

	    public void setGroups(List<Group> groups) {
	        this.groups = groups;
	    }

	    public List<Group> getGroups() {
	        return groups;
	    }

	    public void addGroup(Group group) {
	        groups.add(group);
	    }

	    public void setReplacedText(String replacedText) {
	        this.replacedText = replacedText;
	    }

	    public String getReplacedText() {
	        return replacedText;
	    }

	    public void setText(String text) {
	        this.text = text;
	    }

	    public String getText() {
	        return text;
	    }

	    public boolean isMatches() {
	        return matches;
	    }

	    public void setMatches(boolean matches) {
	        this.matches = matches;
	    }
	    
}
