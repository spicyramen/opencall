package org.mobicents.servlet.sip.tools;

import java.util.ArrayList;
import java.util.List;


public class RegexGroup {
	
 /**
  * group 1: index start: 15 index end: 29 offset: 16 elements: 8
	group 2: index start: 35 index end: 37 offset: 4  elements: 2
  */
	
	private int indexStart = 0;
	private int indexEnd = 0;
	private int offset = 0;
	private int elements = 0;
	private int groupID = 0;
	private List<Integer> regexGroupElementsList = new ArrayList<Integer>();

	
	public int getIndexStart() {
		return indexStart;
	}


	public int getIndexEnd() {
		return indexEnd;
	}


	public int getOffset() {
		return offset;
	}


	public int getElements() {
		return elements;
	}


	public int getGroupID() {
		return groupID;
	}
	
	public void setIndexStart(int indexStart) {
		this.indexStart = indexStart;
	}


	public void setIndexEnd(int indexEnd) {
		this.indexEnd = indexEnd;
	}
	
	
	public void setOffset(int offset) {
		this.offset = offset;
	}


	public void setElements(int elements) {
		this.elements = elements;
	}


	public RegexGroup(int groupId) {
		this.groupID = groupId;	
	}
	
	
	public void processElements(int groupId, List<Integer> regexGroupElements) {
	
		String digits = "\\d";
		int ptrGroup = 1;

		
		if (regexGroupElements.size()==0) {
			return;
		}

		for (int i=0;i<regexGroupElements.size();i++) {		
			if( i+1 < regexGroupElements.size()) {
					if(regexGroupElements.get(i) == regexGroupElements.get(i+1) - digits.length()) {	
						if(ptrGroup == groupId) {
							regexGroupElementsList.add(regexGroupElements.get(i));	
						}
					}
					else {
						if(ptrGroup == groupId) {
							regexGroupElementsList.add(regexGroupElements.get(i));	
						}
						ptrGroup++;
					}
			}	
			else {
				if(ptrGroup == groupId) {
					regexGroupElementsList.add(regexGroupElements.get(i));	
				}
				
			}
			
		}
		
		setIndexStart(regexGroupElementsList.get(0));
		setIndexEnd(regexGroupElementsList.get(regexGroupElementsList.size()-1));
		setOffset((getIndexEnd() - getIndexStart()) + digits.length());
		setElements(regexGroupElementsList.size());
}	
	
}
