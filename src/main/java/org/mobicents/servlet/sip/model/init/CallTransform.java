package org.mobicents.servlet.sip.model.init;

public class CallTransform {

	private String id = null;
	private boolean enabled = false;
	private String originalPattern = null;
	private String finalPattern = null;
	private String applyTo = null;
	private boolean blockCall = false;
	private boolean valid = false;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public String getOriginalPattern() {
		return originalPattern;
	}
	public void setOriginalPattern(String originalPattern) {
		this.originalPattern = originalPattern;
	}
	public String getFinalPattern() {
		return finalPattern;
	}
	public void setFinalPattern(String finalPattern) {
		this.finalPattern = finalPattern;
	}
	public String getApplyTo() {
		return applyTo;
	}
	public void setApplyTo(String applyTo) {
		this.applyTo = applyTo;
	}
	public boolean isBlockCall() {
		return blockCall;
	}
	public void setBlockCall(boolean blockCall) {
		this.blockCall = blockCall;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}


}
