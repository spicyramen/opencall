/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.servlet.sip.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.apache.log4j.Logger;
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.UAMode;

import org.mobicents.servlet.sip.controller.CcCallController;


/**
 * @author Gonzalo Gasca Meza
 *
 */


public class Opencall extends SipServlet {

	private static final Logger logger = Logger.getLogger(Opencall.class);
	
	private String INIT_FILE = "../standalone/configuration/opencall/opencall.ini";
	private static final long serialVersionUID = 1L;
	private static final String RECEIVED = "Received";
	private static final String UA = "Ramen Labs OpenCall";
	private CcCallController opencallSipEngine =  null;
	private int callID = 1;
	B2buaHelper helper = null;
	

	/** Creates a new instance of opencall */
	public Opencall() {

	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {		
		
		super.init(servletConfig);
		
		Thread initializeMainServices = new Thread(new Runnable() {
			
			public void run() {
				
				logger.info("OpenCall() sip servlet reading init parameters: " + INIT_FILE);
				
				try {
					/**
					 * Read Rules
					 */
					
					opencallSipEngine = new CcCallController(INIT_FILE);
					opencallSipEngine.startService();
					
					if (opencallSipEngine.isStarted()) {
						
						logger.info("OpenCall() Engine started succesfully...");
					} 
					else {
					
						logger.fatal("OpenCall() Engine unable to start...");
					}
					
				} catch (Exception e) {	
					
					logger.fatal("OpenCall() Exception during system initialization...");
					e.printStackTrace();
				}
			}
		});
		
		initializeMainServices.start();
        
        try {
        	initializeMainServices.join();
		} catch (InterruptedException e) {
			logger.fatal("OpenCall() Exception occured during system initialization" + e.getMessage());
			e.printStackTrace();
			
		}
		
	}
	
	@SuppressWarnings("unused")
	@Override
	protected void doInvite(final SipServletRequest request) throws ServletException,IOException {
		
		try {
			
			if (logger.isInfoEnabled()) {
				logger.info("Opencall() New SIP Call Detected: " + request.toString());
			
				if(!request.getFrom().getDisplayName().isEmpty() || request.getFrom().getDisplayName()!=null) {
					logger.info("Opencall() Display Name: " + request.getFrom().getDisplayName().toString());
				}
				logger.info("Opencall() From: " + request.getFrom().getURI().toString());		
				logger.info("Opencall() To: " + request.getTo().getURI().toString());
				logger.info("Opencall() Supported transports:  "
						+ getServletContext().getAttribute(
								"javax.servlet.sip.outboundInterfaces"));
			}	
		}
		catch(Exception e) {
			logger.error("Display name not present: " + e.getMessage());
		}
		

		if (request.isInitial()) {
			
			/**
			 * 	Obtain SIP info:
			 *  Obtain 3 parameters: CALLING, CALLED, REDIRECT NUMBER
			 *  RFC 3261 	8.1.1.3 From field
			 *  			8.1.1.1 Request-URI
			 *  Process ROUTELIST 
			 */
	
			/**
			 *  String[] newCallProcessor(int Id, String callingNumber, String calledNumber, String redirectNumber)
			 */

			
			try {
			
				//String DisplayName = request.getFrom().getDisplayName();
				String[] finalSipCallInfo  = opencallSipEngine.newCallProcessor(callID++,request.getFrom().getURI().toString(),request.getTo().getURI().toString(),"");
				
				logger.info("Opencall() newCallProcessor() Call info processed completed");
				
				if (finalSipCallInfo==null) {
					
					try {
						
						logger.warn("Unable to send SIP INVITE");			
						SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE);
						sipServletResponse.send();
						return;
					}		
					
					catch(Exception e) {					
						logger.error("Error: " + e.getMessage());
						e.printStackTrace();
					}

				}
				
				String finalCalling   = finalSipCallInfo[0];
				String finalCalled    = finalSipCallInfo[1];
				String finalRedirect  = finalSipCallInfo[2];
				String finalTransport = finalSipCallInfo[3];
				String finalReject    = finalSipCallInfo[4];
				
				/**
				 * Reject call is enabled in Rules
				 */
				
				if (finalReject.matches("TRUE")) {				
					try {
						
						logger.warn("Unable to send SIP INVITE: " + finalCalled + " Call is Rejected by Transformation Rules");			
						SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_FORBIDDEN);
						sipServletResponse.send();
						return;
					}		
					
					catch(Exception e) {	
						e.printStackTrace();
						logger.error("Error: " + e.getMessage());
						
					}
					
				}
				
				if (finalSipCallInfo != null && finalCalled.length() > 0) {
					
					helper = request.getB2buaHelper();
					request.getSession().setAttribute("INVITE", RECEIVED);
					request.getApplicationSession().setAttribute("INVITE", RECEIVED);

					SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);

					Map<String, List<String>> headers = new HashMap<String, List<String>>();
					
					List<String> toHeaderSet = new ArrayList<String>();
					List<String> fromHeaderSet = new ArrayList<String>();
					List<String> UAHeaderSet = new ArrayList<String>();
					List<String> OrgHeaderSet = new ArrayList<String>();
									
					toHeaderSet.add(finalCalled);
					fromHeaderSet.add(finalCalling);
					UAHeaderSet.add(UA);
					OrgHeaderSet.add("Ramen Networks");
					
					headers.put("From", fromHeaderSet);
					headers.put("To", toHeaderSet);
					headers.put("Organization", OrgHeaderSet);
					headers.put("User-Agent", UAHeaderSet);
				
					SipServletRequest inviteRequest = helper.createRequest(request,true, headers);
					/**
					 * Process Transport
					 */
					
					String transport = inviteRequest.getTransport();
	                
	                if(logger.isInfoEnabled()) {
	                	logger.info("OpenCall() Original transport for sending request is: '" + transport + "'");
	                	
	                }
	                
					SipURI sipUri = (SipURI) sipFactory.createURI(finalCalled);
					inviteRequest.setRequestURI(sipUri);
			
					
					if (finalTransport!=null) {			
						sipUri.setTransportParam(finalTransport);
						logger.info("OpenCall() Final transport for sending request is: '" + finalTransport + "'");
	            	}
					
					
					if (logger.isInfoEnabled()) {
							logger.info("OpenCall() InviteRequest = " + inviteRequest);
					}
					
					inviteRequest.getSession().setAttribute("originalRequest",request);
					inviteRequest.getSession().setAttribute("INVITE", RECEIVED);
							
	            
					/**
					 * Route List implementation
					 */
					
					try {	
						inviteRequest.send();
					}
					catch (Exception exc) {
						
						exc.printStackTrace();
						logger.error("Unable to send SIP INVITE: " + finalCalled);
						logger.error("Error: " + exc.getMessage());			
						SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE);
						sipServletResponse.send();
					
					}
					
				} 
				else {

					logger.error("INVITE. Not found in rules");
					SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_NOT_FOUND);
					sipServletResponse.send();
				}
			} 
			catch (Exception e) {
				e.printStackTrace();
				logger.error("Opencall() Unable to process call information");
				logger.error("Error: " + e.getMessage());			
				SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_SERVICE_UNAVAILABLE);
				sipServletResponse.send();
			}
					
		} else {
			
			// Deals with Re-Invite request		
			if (logger.isInfoEnabled()) {
				logger.info("SIP RE-INVITE");
			}
			
			B2buaHelper b2buaHelper = request.getB2buaHelper();
			SipSession origSession = b2buaHelper.getLinkedSession(request.getSession());
			origSession.setAttribute("originalRequest", request);
			b2buaHelper.createRequest(origSession, request, null).send();
		}
		
	}
	
	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {
		
		logger.info("Got : " + request.toString());
		
		if (request.getTo().getURI().toString().contains("fwd-ack")) {
			B2buaHelper helper = request.getB2buaHelper();
			SipSession peerSession = helper.getLinkedSession(request
					.getSession());
			List<SipServletMessage> pendingMessages = helper
					.getPendingMessages(peerSession, UAMode.UAC);
			SipServletResponse invitePendingResponse = null;
			logger.info("Pending messages : ");
			for (SipServletMessage pendingMessage : pendingMessages) {
				logger.info("\t Pending message : " + pendingMessage);
				if (((SipServletResponse) pendingMessage).getStatus() == 200) {
					invitePendingResponse = (SipServletResponse) pendingMessage;
					break;
				}
			}
			invitePendingResponse.createAck().send();
		}
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,IOException {
		
		logger.info("Got BYE: " + request.toString());
	
		// We forward the BYE
		B2buaHelper byeHelper = request.getB2buaHelper();
		SipSession linkedSipSession = byeHelper.getLinkedSession(request.getSession());
		String linkedSipSessionInviteAttribute = (String) linkedSipSession.getAttribute("INVITE");
		String sipSessionInviteAttribute = (String) request.getSession().getAttribute("INVITE");
		String sipApplicationSessionInviteAttribute = (String) request.getApplicationSession().getAttribute("INVITE");
		
		
		if (logger.isInfoEnabled()) {
			logger.info("Opencall Servlet: attributes previously set in linked sip session INVITE : "
					+ linkedSipSessionInviteAttribute);
			logger.info("Opencall Servlet: attributes previously set in sip session INVITE : "
					+ sipSessionInviteAttribute);
			logger.info("Opencall Servlet: attributes previously set in sip application session INVITE : "
					+ sipApplicationSessionInviteAttribute);
		}

		// we send the OK directly to the first call leg if the attributes have
		// been correctly replicated
		if (sipSessionInviteAttribute == null
				|| sipApplicationSessionInviteAttribute == null
				|| linkedSipSessionInviteAttribute == null
				|| !RECEIVED.equalsIgnoreCase(sipSessionInviteAttribute)
				|| !RECEIVED.equalsIgnoreCase(linkedSipSessionInviteAttribute)
				|| !RECEIVED.equalsIgnoreCase(sipApplicationSessionInviteAttribute)) {
			
			SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR);
			sipServletResponse.send();
			return;
		}
		
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();

		SipSession session = request.getSession();
		SipSession linkedSession = byeHelper.getLinkedSession(session);
		SipServletRequest byeRequest = linkedSession.createRequest("BYE");
		
		if (logger.isInfoEnabled()) {
			logger.info("forkedRequest = " + byeRequest);
		}
		byeRequest.send();
		helper = byeHelper;
	}

	@Override
	protected void doUpdate(SipServletRequest request) throws ServletException,
			IOException {
		
		logger.info("Got : " + request.toString());
		
		B2buaHelper helper = request.getB2buaHelper();
		SipSession peerSession = helper.getLinkedSession(request.getSession());
		SipServletRequest update = helper.createRequest(peerSession, request,
				null);
		update.send();
	}

	@Override
	protected void doInfo(SipServletRequest request) throws ServletException,
			IOException {
		
		logger.info("Got : " + request.toString());
		
		B2buaHelper helper = request.getB2buaHelper();
		SipSession peerSession = helper.getLinkedSession(request.getSession());
		SipServletRequest info = helper.createRequest(peerSession, request,
				null);
		info.send();
	}

	@Override
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {
		logger.info("Got CANCEL: " + request.toString());
	}

	
	
	@Override
	protected void doSuccessResponse(SipServletResponse sipServletResponse)
			throws ServletException, IOException {
		
		logger.info("Got : " + sipServletResponse.toString());
		
		// SipSession originalSession =
		// helper.getLinkedSession(sipServletResponse.getSession());
		// if this is a response to an INVITE we ack it and forward the OK
		if ("INVITE".equalsIgnoreCase(sipServletResponse.getMethod())) {
			if (!sipServletResponse.getTo().getURI().toString()
					.contains("fwd-ack")) {
				SipServletRequest ackRequest = sipServletResponse.createAck();
				if (logger.isInfoEnabled()) {
					logger.info("Sending " + ackRequest);
				}
				ackRequest.send();
			}
			SipServletResponse responseToOriginalRequest = null;
			// create and sends OK for the first call leg
			if (sipServletResponse.getTo().getURI().toString()
					.contains("linked")) {
				B2buaHelper b2buaHelper = sipServletResponse.getRequest()
						.getB2buaHelper();
				SipServletRequest originalRequest = b2buaHelper
						.getLinkedSipServletRequest(sipServletResponse
								.getRequest());
				responseToOriginalRequest = originalRequest
						.createResponse(sipServletResponse.getStatus());
				if (logger.isInfoEnabled()) {
					logger.info("Sending OK on the 1st call leg with linked "
							+ responseToOriginalRequest.toString());
				}
			} else {
				SipServletRequest originalRequest = (SipServletRequest) sipServletResponse
						.getSession().getAttribute("originalRequest");
				responseToOriginalRequest = originalRequest
						.createResponse(sipServletResponse.getStatus());
				if (logger.isInfoEnabled()) {
					logger.info("Sending OK on 1st call leg"
							+ responseToOriginalRequest);
				}
			}
			responseToOriginalRequest.setContentLength(sipServletResponse
					.getContentLength());
			if (sipServletResponse.getContent() != null
					&& sipServletResponse.getContentType() != null)
				responseToOriginalRequest.setContent(
						sipServletResponse.getContent(),
						sipServletResponse.getContentType());
			responseToOriginalRequest.send();
		}
		if (sipServletResponse.getMethod().indexOf("UPDATE") != -1
				|| sipServletResponse.getMethod().indexOf("INFO") != -1) {
			B2buaHelper helper = sipServletResponse.getRequest()
					.getB2buaHelper();
			SipServletRequest orgReq = helper
					.getLinkedSipServletRequest(sipServletResponse.getRequest());
			SipServletResponse res2 = orgReq.createResponse(sipServletResponse
					.getStatus());
			res2.send();
		}
	}

	@Override
	protected void doErrorResponse(SipServletResponse sipServletResponse) throws ServletException, IOException {
		
	
		logger.warn("Error response received got : " + sipServletResponse.getStatus() + " "
					+ sipServletResponse.getReasonPhrase());
		// create and sends the error response for the first call leg
		SipServletRequest originalRequest = (SipServletRequest) sipServletResponse.getSession().getAttribute("originalRequest");
		SipServletResponse responseToOriginalRequest = originalRequest.createResponse(sipServletResponse.getStatus());
		
		if (logger.isInfoEnabled()) {
			logger.info("Sending on the first call leg " + responseToOriginalRequest.toString());
		}
		responseToOriginalRequest.send();
	}

	@Override
	protected void doProvisionalResponse(SipServletResponse sipServletResponse) throws ServletException, IOException {
		
		logger.info("Got : " + sipServletResponse.toString());
		SipServletResponse responseToOriginalRequest = null;
		
		if (sipServletResponse.getTo().getURI().toString().contains("linked")) {
			B2buaHelper b2buaHelper = sipServletResponse.getRequest()
					.getB2buaHelper();
			SipServletRequest originalRequest = b2buaHelper
					.getLinkedSipServletRequest(sipServletResponse.getRequest());
			responseToOriginalRequest = originalRequest
					.createResponse(sipServletResponse.getStatus());
			if (logger.isInfoEnabled()) {
				logger.info("Sending on the first call leg with linked "
						+ responseToOriginalRequest.toString());
			}

		} else {
			SipServletRequest originalRequest = (SipServletRequest) sipServletResponse
					.getSession().getAttribute("originalRequest");
			responseToOriginalRequest = originalRequest
					.createResponse(sipServletResponse.getStatus());
			if (logger.isInfoEnabled()) {
				logger.info("Sending on the first call leg "
						+ responseToOriginalRequest.toString());
			}
		}
		responseToOriginalRequest.send();
	}
}
