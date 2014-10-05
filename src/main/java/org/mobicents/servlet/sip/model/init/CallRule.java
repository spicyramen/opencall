package org.mobicents.servlet.sip.model.init;

public class CallRule {

	private int id = -1;
	private String pattern = null;
	private String type = null;
	private String target = null;
	private int priority = 0;
	private int port = 0;
	private String transport = null;
	private boolean dns = false;
	private boolean valid = false;
	
	
	public CallRule(int id,String pattern,String type, String target) {
		this.id = id;
		this.pattern = pattern;
		this.type = type;
		this.target = target;
		
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getTransport() {
		return transport;
	}
	public void setTransport(String transport) {
		this.transport = transport;
	}
	public boolean isDns() {
		return dns;
	}
	public void setDns(boolean dns) {
		this.dns = dns;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	
	
	
}
