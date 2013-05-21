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
import javax.servlet.sip.B2buaHelper;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.UAMode;
import org.mobicents.servlet.sip.controller.CcProcessor;
import org.apache.log4j.Logger;

/**
 * This Sip Servlet shows how a B2BUA can be used to forward a request on
 * another call leg. Based on the from address if it is in its forwarding list
 * it forwards the INVITE to the corresponding hard coded location.
 * 
 * @author Gonzalo Gasca Meza
 * Oxford University
 * Department of Computer Science, Wolfson Building,  
 * Parks Rd, Oxford OX1, United Kingdom
 * +44 1865 273838
 * gonzalo.gasca.meza@cs.ox.ac.uk
 * 
 * 
 * 
 * Version 1.1  Belador
 *    Route Control
 *    DB, 
 *    File connection, 
 *    API,
 *    Security module
 */
 

public class Opencall extends SipServlet {

	private static Logger logger = Logger.getLogger(Opencall.class);
	private String INIT_FILE = "../standalone/configuration/opencall/opencall.ini";
	private static final long serialVersionUID = 1L;
	private static final String RECEIVED = "Received";
	private static final String VERSION = "1.1 Belador";
	private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
	B2buaHelper helper = null;
	private CcProcessor openCallEngine =  null;

	/** Creates a new instance of CallForwardingB2BUASipServlet */
	public Opencall() {

	}

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {		
		
		logger.info("******************************");
		logger.info("OpenCall Engine is starting...");
		logger.info("OpenCall Engine version " + VERSION);
		logger.info("System Folder: " + CURRENT_DIRECTORY);
		
		super.init(servletConfig);
		Thread initOpenCallService = new Thread(new Runnable() {
			
			public void run() {
				logger.info("OpenCall sip servlet reading init parameters: " + INIT_FILE);
				
				try {
					openCallEngine = new CcProcessor(INIT_FILE);
					openCallEngine.startService();
					if (openCallEngine.isStarted()) {
						logger.info("OpenCall engine started succesfully.");
					} else {
						logger.fatal("OpenCall engine unable to start.");
					}
					
				} catch (Exception e) {
					
					logger.error("OpenCall Exception during system initialization");
					e.printStackTrace();
				}
			}
		});
		
		initOpenCallService.start();
        
        try {
        	initOpenCallService.join();
		} catch (InterruptedException e) {
			logger.error("OpenCall Exception occured during system initialization" + e.getMessage());
			
		}
		
	}

	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Got : " + request.toString());
		}
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
	protected void doInvite(final SipServletRequest request) throws ServletException,IOException {
		
		if (logger.isInfoEnabled()) {
			if (logger.isDebugEnabled())
				logger.info("New SIP Call Detected: " + request.toString());
			if (logger.isDebugEnabled())
				logger.debug(request.getFrom().getURI().toString());
			if (logger.isDebugEnabled())
				logger.debug("OUTBOUND INTERFACES  "
						+ getServletContext().getAttribute(
								"javax.servlet.sip.outboundInterfaces"));
		}

		if (request.isInitial()) {
			
			String finalSipUri = openCallEngine.digitsDialed(request.getTo().getURI().toString());

			if (finalSipUri != null && finalSipUri.length() > 0) {
				helper = request.getB2buaHelper();
				request.getSession().setAttribute("INVITE", RECEIVED);
				request.getApplicationSession().setAttribute("INVITE", RECEIVED);

				SipFactory sipFactory = (SipFactory) getServletContext().getAttribute(SIP_FACTORY);

				Map<String, List<String>> headers = new HashMap<String, List<String>>();
				List<String> toHeaderSet = new ArrayList<String>();
				toHeaderSet.add(finalSipUri);
				headers.put("To", toHeaderSet);

				SipServletRequest inviteRequest = helper.createRequest(request,true, headers);
				SipURI sipUri = (SipURI) sipFactory.createURI(finalSipUri);			
				inviteRequest.setRequestURI(sipUri);
			
				if (logger.isInfoEnabled()) {
					if (logger.isDebugEnabled())
						logger.debug("inviteRequest = " + inviteRequest);
				}
				
				inviteRequest.getSession().setAttribute("originalRequest",request);
				inviteRequest.getSession().setAttribute("INVITE", RECEIVED);
				
				try {
					inviteRequest.send();
				}
				catch (Exception e) {
					logger.error("Unable to send SIP INVITE: " + finalSipUri);
					e.printStackTrace();
				
				}
				
			} 
			else {

				if (logger.isInfoEnabled()) {
					logger.error("INVITE has not been forwarded. Not found in rules");
					SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_NOT_FOUND);
					sipServletResponse.send();
				}
			}
		} else {
			
			// Deals with Re-Invite request
			
			if (logger.isInfoEnabled()) {
				logger.info("SIP Re-INVITE");
			}
			
			B2buaHelper b2buaHelper = request.getB2buaHelper();
			SipSession origSession = b2buaHelper.getLinkedSession(request.getSession());
			origSession.setAttribute("originalRequest", request);
			b2buaHelper.createRequest(origSession, request, null).send();
		}
		
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,IOException {
		
		
		if (logger.isInfoEnabled()) {
			logger.info("Got BYE: " + request.toString());
		}
		
		// We forward the BYE
		B2buaHelper byeHelper = request.getB2buaHelper();
		SipSession linkedSipSession = byeHelper.getLinkedSession(request.getSession());
		String linkedSipSessionInviteAttribute = (String) linkedSipSession.getAttribute("INVITE");
		String sipSessionInviteAttribute = (String) request.getSession().getAttribute("INVITE");
		String sipApplicationSessionInviteAttribute = (String) request.getApplicationSession().getAttribute("INVITE");
		
		
		if (logger.isInfoEnabled()) {
			logger.info("Distributable Simple Servlet: attributes previously set in linked sip session INVITE : "
					+ linkedSipSessionInviteAttribute);
			logger.info("Distributable Simple Servlet: attributes previously set in sip session INVITE : "
					+ sipSessionInviteAttribute);
			logger.info("Distributable Simple Servlet: attributes previously set in sip application session INVITE : "
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
		if (logger.isInfoEnabled()) {
			logger.info("Got : " + request.toString());
		}
		B2buaHelper helper = request.getB2buaHelper();
		SipSession peerSession = helper.getLinkedSession(request.getSession());
		SipServletRequest update = helper.createRequest(peerSession, request,
				null);
		update.send();
	}

	@Override
	protected void doInfo(SipServletRequest request) throws ServletException,
			IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Got : " + request.toString());
		}
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
		if (logger.isInfoEnabled()) {
			logger.info("Got : " + sipServletResponse.toString());
		}

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
		
		if (logger.isInfoEnabled()) {
			logger.info("Got : " + sipServletResponse.getStatus() + " "
					+ sipServletResponse.getReasonPhrase());
		}

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
