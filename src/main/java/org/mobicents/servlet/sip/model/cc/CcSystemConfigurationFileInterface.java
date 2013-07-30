package org.mobicents.servlet.sip.model.cc;

public interface CcSystemConfigurationFileInterface {
	public boolean CcStartCallRulesEngine(String fileName);
	public boolean CcStartCallTransformsEngine(String fileName);
	public boolean CcStartRouteListEngine(String fileName);
	
}