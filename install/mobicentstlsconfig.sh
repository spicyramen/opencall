#!/bin/bash
#-----------------------------------------------------------------------------
#  AT&T Labs, Inc.
#
# Purpose: Create java keystore, private key and selftsigned certificate in Mobicents 2.0
#		   TODO: Update mss-properties. 
#		   TODO: Enable port 5061 in iptables
#
# AUTHOR: Gonzalo Gasca (gogasca)
# # This script is hereby put in the public domain.
#
# THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO
# EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
# OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
# OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
# ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#
# HISTORY:
#    06/11/2012 - gogasca - Created script
#	 12/10/2012 - gogasca - Added mobicents support
#-----------------------------------------------------------------------------
shopt -s extglob

#------------------------------------------------------------------
# Defines
#------------------------------------------------------------------
SCRIPT_NAME="tlsconfig.sh" 

# Platform configuration file
THIS_HOSTNAME=$(hostname)

# Security parameters
MOBICENTS_PATH="/usr/local/src/jboss/mss-2.0.0.FINAL-jboss-as-7.1.2.Final"
CERTIFICATE_PATH="$MOBICENTS_PATH/standalone/configuration/tls/certificates"
KEYS_PATH="$MOBICENTS_PATH/standalone/configuration/tls/private/keys"
KEYSTORE="$THIS_HOSTNAME.keystore"
KEYALG="RSA"
VALIDITY="365"
KEYSIZE="2048"
#PASSWORD=$(date +%M | sha256sum | base64 | head -c 12 ; echo)
#KEYPASSWORD=$(date +%M | sha256sum | base64 | head -c 12 ; echo)
PASSWORD='Y2ZkMjJiMjk1'
KEYPASSWORD='Y2ZkMjJiMjk1'

# Certificate X509 Information
X509_ORG="AT&T Labs"
X509_UNIT="ATS"
X509_LOCATION="San Jose"
X509_STATE="CA"
X509_COUNTRY="US"

# Log file
LOG_FILE=../standalone/log/enabletls.log

#------------------------------------------------------------------
# Log level
#------------------------------------------------------------------

function log()
{
#------------------------------------------------------------------
# Log levels 1: DEBUG 2: WARN 3: INFO
#------------------------------------------------------------------

if [ $1 -eq 1 ]; then
LEVEL="DEBUG"
elif [ $1 -eq 2 ]; then
LEVEL="WARN"
elif [ $1 -eq 3 ]; then
LEVEL="ERROR"
else 
LEVEL="UNKNOWN"
fi
# Populate log file
echo "$(date +%c) $LEVEL $2" >> $LOG_FILE
}


#######################################################
# This function does the following:
#  *  Escape special characters , = + \
#
# RETURNS:
#    0 if no errors
#    1 if info is missing or not set correctly
#	
#######################################################

function escape_specialchars()
{
#echo "============================================"
#echo "String Value $1"
log 1 "escape_specialchars() Removing special characters"
XSTRING=`echo -e "$1" | sed 's/,/ \\\,/'`
echo "$XSTRING"
}

#######################################################
# This function does the following:
#  * Obtain X509 certificate
#
# RETURNS:
#    0 if no errors
#    1 if info is missing or not set correctly
#	-dname "cn=$THIS_HOSTNAME,ou=$X509_UNIT,o=$X509_ORG,c=$X509_COUNTRY"
#######################################################

function obtain_x509()
{
echo "============================================"
log 1 "obtain_x509() Init"
echo "obtain_x509() Init"

echo "X509 Parameters:"
echo ""
# Obtain security password encrypted:  
 if [ -z X509_ORG ]; then
        log 1 "obtain_x509()	Using default Organization"
             X509_ORG="ATT Labs"
        return 1
 fi
X509_ORG=$(escape_specialchars "$X509_ORG")
echo "X509 Organization: $X509_ORG "
log 1 "obtain_x509()	X509 Organization: $X509_ORG "

 if [ -z X509_UNIT ]; then
        log 1 "obtain_x509()	Using default Unit"
             X509_UNIT="ATS"
        return 1
 fi

X509_UNIT=$(escape_specialchars "$X509_UNIT")
echo "X509 Unit: $X509_UNIT"
log 1 "obtain_x509()	X509 Unit: $X509_UNIT"

 if [ -z X509_LOCATION ]; then
        log 1 "obtain_x509()	Using default Location"
             X509_LOCATION="San Jose"
        return 1
 fi

X509_LOCATION=$(escape_specialchars "$X509_LOCATION")
echo "X509 Location: $X509_LOCATION"
log 1 "obtain_x509()	X509 Location: $X509_LOCATION"

 if [ -z X509_STATE ]; then
        log 1 "obtain_x509()	Using default State"
             X509_STATE="CA"
        return 1
 fi
 
X509_STATE=$(escape_specialchars "$X509_STATE")
echo "X509 State: $X509_STATE"
log 1 "obtain_x509()	X509 State: $X509_STATE"

 if [ -z X509_COUNTRY ]; then
        log 1 "obtain_x509()	Using default Country"
             X509_COUNTRY="US"
        return 1
 fi

X509_COUNTRY=$(escape_specialchars "$X509_COUNTRY") 
echo "X509 Country: $X509_COUNTRY"
log 1 "obtain_x509()	X509 Country: $X509_COUNTRY"
return 0
}

#######################################################
# This function does the following:
#  * Verify if keystore already exists
#
# RETURNS:
#    0 no keystore exists
#    1 if no errors and keystore exists
#######################################################

function verify_keystore()
{
echo "============================================"
log 1 "verify_keystore() Init"
echo "verify_keystore() Init"
KEYSTOREFILE="$KEYS_PATH/$KEYSTORE"

if [ -f $KEYSTOREFILE ];
then
   echo "verify_keystore() Keystore exists"
   log 1 "verify_keystore()	Keystore exists"
   return 1
else
   echo "verify_keystore() Keystore does not exist"
   log 1 "verify_keystore()	Keystore does not exist"
   return 0
fi

}

#######################################################
# This function does the following:
#  * Verify ../standalone/configuration/mss-sip-stack.properties
#    
#
# RETURNS:
#    0 updated succesfully
#    1 errors during update
#######################################################

function update_configurationfile()
{

#Verify file exists
# If yes -> Look for Param X for each line
#   If param exist, update it, else insert it
#   Close file
# Else -> Generate alarm

#gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS=SSLv3,TLSv1
#javax.net.ssl.keyStore=/usr/local/src/jboss/mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/configuration/tls/private/keys/zeus.keystore
#javax.net.ssl.trustStore=/usr/local/src/jboss/mss-2.0.0.FINAL-jboss-as-7.1.2.Final/standalone/configuration/tls/private/keys/zeus.keystore
#javax.net.ssl.keyStorePassword=Y2ZkMjJiMjk1
#javax.net.ssl.keyStoreType=jks
#gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE=Want

KEYS_PATH="$MOBICENTS_PATH/standalone/configuration/tls/private/keys"
KEYSTORE="$THIS_HOSTNAME.keystore"
PROTOCOLS="SSLv3,TLSv1"

#gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS=$PROTOCOLS
#javax.net.ssl.keyStore=$KEYS_PATH/$KEYSTORE
#javax.net.ssl.trustStore=$KEYS_PATH/$KEYSTORE
#javax.net.ssl.keyStorePassword=$PASSWORD
#javax.net.ssl.keyStoreType=jks
#gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE=Want



}

#######################################################
# This function does the following:
#  * Create certs and private folder
#
# RETURNS:
#    0 if dir does exist
#    1 if dir does not exist
#######################################################

function build_folders()
{
#CERTIFICATE_PATH="$MOBICENTS_PATH/standalone/configuration/tls/certificates"
#KEYS_PATH="$MOBICENTS_PATH/standalone/configuration/tls/private/keys"

echo "============================================"
log 1 "build_folders() Init"
echo "build_folders() Init"

CERTS_DIR="$CERTIFICATE_PATH"
  if [ -d "$CERTS_DIR" ]; then
    echo "Folder: $CERTS_DIR exists, not creating this dir."
     log 1 "build_folders()	Folder: $CERTS_DIR exists, not creating this dir."
  else
    echo "$CERTS_DIR does not exist: creating one"
    mkdir -p $CERTS_DIR;retStat=$?
    if [ ! $retStat = 0 ]; then
      echo "Unable to create directory: $CERTS_DIR"
      log 3 "build_folders()	Unable to create directory"
      exit $retStat
    fi  
  fi  

PRIVATEKEYS_DIR="$KEYS_PATH"
  if [ -d "$PRIVATEKEYS_DIR" ]; then
    echo "$PRIVATEKEYS_DIR exists, not creating this dir."
    log 1 "build_folders()	$PRIVATEKEYS_DIR exists, not creating this dir."
    return 1
  else
    echo "$PRIVATEKEYS_DIR does not exist: creating one"
    log 1 "build_folders() $PRIVATEKEYS_DIR does not exist: creating one"
    mkdir -p $PRIVATEKEYS_DIR;retStat=$?
    if [ ! $retStat = 0 ]; then
      echo "Unable to create directory: $PRIVATEKEYS_DIR"
      log 3 "build_folders()	Unable to create directory"
      exit $retStat
    fi  
  fi

return 0
}

#######################################################
# This function does the following:
#  * Create backup for certs and private folder
#
# RETURNS:
#    0 if dir does not exist
#    1 if dir does exist
#######################################################

function build_backup()
{
echo "============================================"
log 1 "build_backup() Init"
echo "build_backup() Init"


BACKUP_DIR="$MOBICENTS_PATH/standalone/configuration/backup"
  if [ -d "$BACKUP_DIR" ]; then
    echo "$BACKUP_DIR exists, not creating this dir."
    log 1 "build_folders()	$BACKUP_DIR exists, not creating this dir."
    return 1
  else
    echo "$BACKUP_DIR does not exist: creating one"
    log 1 "build_folders() $BACKUP_DIR does not exist: creating one"
    mkdir -p $BACKUP_DIR;retStat=$?
    if [ ! $retStat = 0 ]; then
      echo "Unable to create directory: $BACKUP_DIR"
      log 3 "build_folders()	Unable to create backup directory"
      exit $retStat
    fi  
  fi

return 0
}


#######################################################
# This function does the following:
#  * Delete certs and private folder
#
# RETURNS:
#    0 if dir does exist
#    1 if dir does not exist
#######################################################

function delete_folders()
{
#CERTIFICATE_PATH="$MOBICENTS_PATH/standalone/configuration/tls/certificates"
#KEYS_PATH="$MOBICENTS_PATH/standalone/configuration/tls/private/keys"

echo "============================================"
log 1 "delete_folders() Init"
echo "delete_folders() Init"
DELETE_DIR="$MOBICENTS_PATH/standalone/configuration/tls/"
  if [ -d "$DELETE_DIR" ]; then
    echo  "delete_folders() $DELETE_DIR exists, Folder will be deleted."
    log 1 "delete_folders()	$DELETE_DIR exists, Folder will be deleted."
    rm -rf $DELETE_DIR;retStat=$?
    if [ ! $retStat = 0 ]; then
      echo  "delete_folders()	Unable to delete directory: $DELETE_DIR"
      log 3 "delete_folders()	Unable to delete directory: $DELETE_DIR"
      exit $retStat 
    else
      echo  "delete_folders() Folder deleted succesfully $DELETE_DIR"
      log 1 "delete_folders() Folder deleted succesfully $DELETE_DIR"
    fi
  else
    echo "$DELETE_DIR does not exist"
    log 1 "delete_folders() $DELETE_DIR does not exist"
    return 1
  fi

return 0
}

#######################################################
# This function does the following:
#  * Create certs and keystore
#
# RETURNS:
#    0 if success
#    1 if failure
#######################################################

function create_keystore()
{
echo "============================================"
log 1 "create_keystore() Init"
echo "create_keystore() Init"

echo "Keystore Parameters:"
echo ""
echo "Keystore Location: $KEYS_PATH/$KEYSTORE"
echo "Hostname: $THIS_HOSTNAME"
echo ""
echo "X509 Organization: $X509_ORG "
echo "X509 Unit: $X509_UNIT"
echo "X509 Location: $X509_LOCATION"
echo "X509 State: $X509_STATE"
echo "X509 Country: $X509_COUNTRY"
echo ""
echo "Creating Java Keystore"

#Example:
#$JAVA_HOME/bin/keytool -genkey -alias zeus -keyalg RSA -keystore $KEYS_PATH/server.keystore -validity 360 -keysize 2048 -storepass attlabs -keypass attlabs

keytool -genkey -alias $THIS_HOSTNAME -keyalg $KEYALG -keystore $KEYS_PATH/$KEYSTORE -dname "cn=$THIS_HOSTNAME,ou=$X509_UNIT,o=$X509_ORG,s=$X509_STATE,c=$X509_COUNTRY" -validity $VALIDITY -keysize $KEYSIZE -storepass $PASSWORD -keypass $KEYPASSWORD
retStat=$?
    if [ ! $retStat = 0 ]; then
     echo "create_keystore()	Unable to create keystore"
     log 1 "create_keystore()	Unable to create keystore"
     exit $retStat
    else
     echo "create_keystore()	Keystore created succesfully" 
     echo "create_keystore()	Info storepass: $PASSWORD keypass: $KEYPASSWORD"
     log 1 "create_keystore()	Keystore created succesfully"
     log 1 "create_keystore()	Info storepass: $PASSWORD keypass: $KEYPASSWORD" #Writing password to log file.
     
    fi

echo "Displaying Java Keystore:"
echo "create_keystore()	Info storepass: $PASSWORD"
keytool -v -list -keystore $KEYS_PATH/$KEYSTORE -storepass $PASSWORD
retStat=$?
    if [ ! $retStat = 0 ]; then
     echo "create_keystore()	Unable to display keystore"
     log 1 "create_keystore()	Unable to create keystore"
     exit $retStat
    fi
    
}

#######################################################
# This function does the following:
#  * Create selfsigned certificate
#
# RETURNS:
#    0 if success
#    1 if failure
#######################################################

function create_selfsigned()
{
echo "============================================"
log 1 "create_selfsigned() Init"
echo "create_selfsigned() Init"

#Example:
#keytool -export -alias zeus -keystore $KEYS_PATH/server.keystore -storepass attlabs -file /server.cer

# Create self signed certificate
echo "Creating Certificate"
log 1 "create_selfsigned() Creating Certificate"
keytool -export -alias $THIS_HOSTNAME -keystore $KEYS_PATH/$KEYSTORE -storepass $PASSWORD -file $CERTIFICATE_PATH/$THIS_HOSTNAME.cer
retStat=$?
    if [ ! $retStat = 0 ]; then
     echo "Unable to create self signed certificate"
     log 3 "Unable to create self signed certificate"
     exit $retStat
    else 
     echo "create_selfsigned() Certificate  created succesfully"
     log 1 "create_selfsigned()	Certificate created succesfully"
    fi

# Export self signed certificate into PEM format
echo "Convert the Mobicents Engine server certificate to X509 (PEM) format for importing to other solution components"
log 1 "create_selfsigned() Convert the Mobicents Engine server certificate to X509 (PEM) format for importing to other solution components"
openssl x509 -out $CERTIFICATE_PATH/$THIS_HOSTNAME.pem -outform pem -in $CERTIFICATE_PATH/$THIS_HOSTNAME.cer -inform der
retStat=$?
    if [ ! $retStat = 0 ]; then
     echo "create_selfsigned() Unable to export certificate"
     log 1 "create_selfsigned() Unable to export certificate"
     exit $retStat
    fi
    
# Displaying self signed certificate
echo "Displaying $THIS_HOSTNAME selfsigned certificate"
log 1 "create_selfsigned() Displaying $THIS_HOSTNAME selfsigned certificate"
openssl x509 -noout -text -in $CERTIFICATE_PATH/$THIS_HOSTNAME.pem


}


##########################################################################################################################

function obtain_hostname()
{
SERVER_HOSTNAME=`hostname`
SERVER_DOMAIN_NUMBER=$(grep -c domain /etc/resolv.conf)
echo ""
echo "Total  ${SERVER_DOMAIN} domains defined in /etc/resolv.conf"
	
	if [ $countNameservers -lt 0  ];then
   		echo "No domain defined using hostname"
		return 1
	fi
	SERVER_DOMAIN_NAME=$(grep domain /etc/resolv.conf | awk '{ print $2 }')
	echo "Server domain: " 
	echo ${SERVER_DOMAIN_NAME}
}


##########################################################################################################################

function doPrintUsage()
{
  echo ""
  echo "Usage: $0 <enable|disable|config>"
  echo "       $0 enable  - create keystore and self signed certificate in Mobicents"
  echo "       $0 disable - delete keystore and self signed certificate in Mobicents"
  echo "       $0 config - configure mss-properties file for TLS in Mobicents"
  echo ""
  exit $1
}
 
##########################################################################################################################
 
 
 
function main()
{ 
  if [ $# -ne 1 ]; then
    doPrintUsage 1
  fi
 echo "" 
 echo "SSL Configuration for Mobicents SIP Engine Server."
 log  1 "Main()	Mobicents Path: $MOBICENTS_PATH"
 log  1 "Main()	Script initialized. Modifying SSL parameters"
 option=$1
  if [ "$option" != "enable" ]; then
    if [ "$option" != "disable" ]; then
      doPrintUsage 1
     fi
  fi
  
  if [ "$option" = "enable" ]; then
    
    obtain_x509;retStat=$?
     if [ ! $retStat = 0 ]; then
      echo "Unable to obtain X509 information"
      log 3 "Main()	Unable to obtain X509 information"
      exit $retStat
    fi
    verify_keystore;retStat=$?
     if [ ! $retStat = 0 ]; then
      echo "Keystore already exists"
      log 1 "Main()	Keystore already exists"
      exit $retStat
     else
      echo "Keystore does not exist creating new one"
      log 1 "Main()	Keystore does not exist creating new one"
      build_folders
      create_keystore;retStat=$?
     	if [ ! $retStat = 0 ]; then
	      echo "Unable to create keystore"
    	  log 3 "Main()	Unable to create keystore"
	      exit $retStat
	    fi
	  create_selfsigned;retStat=$?
     	if [ ! $retStat = 0 ]; then
	      echo "Unable to create self signed certificate"
    	  log 3 "Main()	Unable to create self signed certificate"
	      exit $retStat
	    fi
	  update_configurationfile
	  fi  
  fi  
  
  if [ "$option" = "disable" ]; then
    build_backup
    delete_folders
  fi



}
 
main $@

