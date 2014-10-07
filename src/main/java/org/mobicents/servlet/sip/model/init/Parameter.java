package org.mobicents.servlet.sip.model.init;


public class Parameter {

	/**
	 * @param args
	 */
	private String parameterName = 	"";
	private String parameterValue =  ""; 
	
	public Parameter(String paramName,String paramValue) {
		parameterName = paramName;
		parameterValue = paramValue;
	}

	public String getParameterName() {
		return parameterName;
	}

	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	public String getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(String parameterValue) {
		this.parameterValue = parameterValue;
	}
	
	@Override
	public String toString() {
		return parameterName + ":" + parameterValue;
		
	}
}
