package org.mobicents.servlet.sip.clients.twilio;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Call;
import java.util.HashMap;
import java.util.Map;

public class TwilioDriver {
	
		/**
		 * Twilio interface will read Twilio accoun information.
		 * and send request to Twilio via REST API, Twilio will create SIP messages and contact SIP device
		 */
	  // Find your Account Sid and Token at twilio.com/user/account
	  public static final String ACCOUNT_SID = "AC32a3c49700934481addd5ce1659f04d2";
	  public static final String AUTH_TOKEN = "{{ auth_token }}";
	 
	  public static void main(String[] args) throws TwilioRestException {
		  
	    TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
	 
	    // Build a filter for the CallList
	    Map<String, String> params = new HashMap<String, String>();
	    params.put("Url", "http://www.example.com/sipdial.xml");
	    params.put("To", "sip:kate@example.com?hatchkey=4815162342");
	    params.put("From", "Jack");
	     
	    CallFactory callFactory = client.getAccount().getCallFactory();
	    Call call = callFactory.create(params);
	    System.out.println(call.getSid());
	  }
}
