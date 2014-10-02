OpenCall 
----------------------

When placing SIP Calls for multiple products you face the challenge to install a SIP server and configure Call routing rules, Trunks, ACL, usernames, passwords. 
OpenCall is an OpenSource SIP B2BUA which uses Mobicents SIP stack which solves this problem.

By installing Mobicents and deploying this application you will be able to place calls in  minutes. 
Opencall will be able to integrate to existing Audio and Video systems  (Cisco Unified Communications Manager, VCS, CTX, CME, Asterisk, Polycom DMA, etc.)

Features
----------------------

-Allows Call Routing Rules configuration
-Local File configuration
-DB connection to MySQL/MongoDb where Rules can be stored
-SIP URI dialing
-SIP Regex support
-E164 dialing
-Transport support: TCP, UDP, WSS, TLS, WS
-Allow WebRTC clients using WS to connect to SIP enviroments (sipml5)


Version 1.1a Features:

-Allows Call Routing Rules transformations
-Route List support
-API
-Twilio 
-Administration Web Page
-Call log analyzer
-CDR generation
-SIP Headers Modification
-SDP Body Modification via LUA script
-Implementation of SIP CLF (SIPCLF)


Installation
----------------------

Mobicents installation

-Download Mobicents 2.0 Jboss
http://sourceforge.net/projects/mobicents/files/Mobicents%20Sip%20Servlets/

-Extract Mobicents file

mss-2.0.0.FINAL-jboss-as-7.1.2.Final-1349104459.zip

-Download configuration file standalone-sip.xml

https://code.google.com/p/ramenOpencall/downloads/detail?name=standalone-sip.xml&can=2&q=

-Move this file to this directory: mss-2.0.0.FINAL-jboss-as-7.1.2.Final/bin and mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/configuration

-Obtain local network information
Obtain local IP address information and define which NIC system will be using ifconfig

Example:
local ipaddress: 192.168.1.69 netmask 0xffffff00 broadcast 192.168.1.255

-Verify iptables or firewall is open for ports 5060,5061 and 5062.
-Start Mobicents server. Go to the following folder in order to start Mobicents server: mss-2.0.0.FINAL-jboss-as-7.1.2.Final/bin

Run the command: ./standalone.sh -b 192.168.1.69 -c standalone-sip.xml &


Opencall Installation
----------------------

-Download Opencall  war file

https://code.google.com/p/ramenOpencall/downloads/detail?name=Opencall-1.0.0-COMENDADOR.war&can=2&q=

-Create Opencall folder

Create Opencall folder in mss-2.0.0.FINAL-jboss-as-7.1.2.Final/bin and mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/configuration

Example:
mkdir mss-2.0.0.FINAL-jboss-as-7.1.2.Final/bin and mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/configuration/opencall


-Download and configure Opencall server init file

https://code.google.com/p/ramenOpencall/downloads/detail?name=Opencall.ini&can=2&q

Configure proper file location inside opencall-config.xml define path for Opencall server routing rules.

<?xml version="1.0" encoding="utf-8"?>
<config version="1.0">
  <Server>
    <Mode>1</Mode>
    <RuleLimit>10000</RuleLimit>
    <CallTransforms>/Users/gogasca/Documents/OpenSource/Development/Java/Mobicents/mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/configuration/opencall/opencalltransforms.cfg</CallTransforms>
    <CallRules>/Users/gogasca/Documents/OpenSource/Development/Java/Mobicents/mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/configuration/opencall/opencallrules.cfg</CallRules>
    <CallRouteLists>/Users/gogasca/Documents/OpenSource/Development/Java/Mobicents/mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/configuration/opencall/opencallroutelists.cfg</CallRouteLists>
  </Server>
  <Database>
    <Type>1</Type>
    <DbHostName>localhost</DbHostName>
    <DbPort>3306</DbPort>
    <DbName>opencall</DbName>
    <DbUserName>root</DbUserName>
    <DbPassword></DbPassword>
  </Database>
  <Policies>
    <BlackList>/Users/gogasca/Documents/OpenSource/Development/Java/Mobicents/mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/configuration/opencall/blacklist.cfg</BlackList>
  </Policies>
</config>


-Download and configure Opencall server call routing rules

https://code.google.com/p/ramenOpencall/downloads/detail?name=Opencallrules.cfg&can=2&q


-Deploy opencall war file

Move Opencall war file to the Mobicents deployment folder: 
mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/deployments

Verify Opencall war is deployed successfully, verify Opencall log and server log file in log folder: the Opencall log file is named: Opencall-engine.log, server log is server.log

Example:

Server 

21:55:33,724 INFO  [org.jboss.as.server] (ServerService Thread Pool -- 29) JBAS018559: Deployed "Opencall-1.0.0-COMENDADOR.war"

Opencall

16:10:32.095 INFO  [Opencall] [Thread-160] | Opencall engine has started succesfully.

Deployment folder

opencall-1.0.0-COMENDADOR.war.deployed

WebSockets Installation
-----------------------

-Download Websockets war file*

http://repo1.maven.org/maven2/org/mobicents/servlet/sip/examples/websockets-sip-servlet/2.0.0.FINAL/websockets-sip-servlet-2.0.0.FINAL.war

	*File may be already included in Mobicents zip file.
 
-Deploy WebSockets war file

Move Opencall war file to the Mobicents deployment folder:  mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/deployments

Verify Websockets war is deployed successfully, verify server log file in log folder: the server log file is named: server.log

	Server

	21:55:33,723 INFO  [org.jboss.as.server] (ServerService Thread Pool -- 29) JBAS018559: Deployed "websockets-sip-servlet-2.1.0-20121108.113957-1.war"

Deployment folder
------------------------

websockets-sip-servlet-2.1.0-20121108.113957-1.war.deployed


Mobicents configuration
------------------------
For Mobicents please make sure you configure configuration file properly.
Configure your Default Application Router Configuration and define the proper Application Name.

INVITE: 	Opencall
REGISTER:	WebSockets
 	 
Opencall configuration
------------------------
Configure opencall.ini and define the initial system configuration settings
Configure opencall.cfg and define the System Routing rules in order to connect Mobicents to your external system.

Opencall System
------------------------
Opencall server will look for opencall.ini located:

mss-2.0.0.FINAL-jboss-as-7.1.2.Final/bin and mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/configuration/opencall

In order to start succesfully

opencall.ini

MODE 
1 - LOCAL FILE
2 - DB (MYSQL DB)
3 - LOCAL RULES (BUILT-IN) (Not implemented)

DB TYPE 
1 - MYSQL
2 - ORACLE DB (Not implemented)	
3 - MICROSOFT SQL SERVER	(Not implemented)	

MODE 1 must have FILEROUTINGRULES
MODE 2 must have DBTYPE,DBHOSTNAME,DBPORT,DBNAME,DBUSERNAME and DBPASSWORD
MODE 3 does not need any parameters (Not implemented)

Opencall Route Configuration
--------------------------------------------

Opencall have the option to read call routing rules from a configuration file or from a MySQL DB.
This document describes how to configure opencallrules.cfg to provide call routing rules in order to connect to your system.

Mandatory parameters:
ROUTE= 
RULE     	= NUMERIC
PRIORITY 	= NUMERIC
TYPE     	= REGEX,  WILDCARD, NUMERIC
STRING   	= VALUE
TARGET   	= IP ADDRESS, HOSTNAME, _DNS_, _TWILIO_

Optional
PORT     = NUMERIC VALUE (DEFAULT 5060)

Rule Entries Order
RULE NUMBER,RULE PRIORITY, RULE TYPE, MATCH STRING,TARGET, PORT TARGET

Example:

ROUTE=("1","100","REGEX","(.*)@.*","_DNS_")
ROUTE=("2","5","REGEX","(.*)@cisco.com","_DNS_")
ROUTE=("3","10","NUMERIC","201","110.10.0.210","5070")
ROUTE=("4","100","WILDCARD","20X","110.10.0.200")
ROUTE=("5","10","WILDCARD","10X","110.10.0.200")
ROUTE=("6","10","WILDCARD","XXXXXXXX","google.com")
ROUTE=("7","10","WILDCARD","XXXXXXXX","video.att.com")
ROUTE=("8","50","WILDCARD","20X","110.10.0.210","5060")
ROUTE=("9","50","WILDCARD","0115255!","110.10.0.200","5060")
ROUTE=("10","50","WILDCARD","0115233!","110.10.0.200","5060")
ROUTE=("11","1","NUMERIC","+525557969469","110.10.0.200","5060")
ROUTE=("12","1","WILDCARD","4XX","110.10.0.220","5060")
ROUTE=("13","10","NUMERIC","+14089449402","110.10.0.200","5060")
ROUTE=("14","10","NUMERIC","+14089449402","_TWILIO_")


-Verify Opencall war file is deployed successfully.

$ pwd
/Users/gogasca/Documents/OpenSource/Development/Java/Mobicents/mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/deployments

$ ls -al
-rw-r--r--   1 gogasca  staff   859547 Apr 13 16:07 Opencall-1.0.0-COMENDADOR.war
-rw-r--r--   1 gogasca  staff       29 Apr 13 16:07 Opencall-1.0.0-COMENDADOR.war.deployed
-rw-rw-r--@  1 gogasca  staff  4081278 Oct  1  2012 sip-servlets-management.war
-rw-r--r--   1 gogasca  staff       27 Oct  1  2012 sip-servlets-management.war.deployed
-rw-r--r--@  1 gogasca  staff  6039635 Nov 18 14:25 websockets-sip-servlet-2.1.0-20121108.113957-1.war
-rw-r--r--   1 gogasca  staff       50 Nov 18 14:25 websockets-sip-servlet-2.1.0-20121108.113957-1.war.deployed
					
