#!/bin/bash

org=$1;

if [ -z "${org}" ]; then
	echo "Usage: $0 <org path>";
	exit -1;
fi

java -classpath ../lib/force-wsc-37.0.3.jar:../lib/ST-4.0.8.jar:../lib/rhino-1.7.7.1.zip com.sforce.ws.tools.wsdlc ./${org}/partner.wsdl ../lib/partner.jar

