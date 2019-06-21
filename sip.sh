#!/bin/bash

arguments=$*;

umask 0007
export TZ="GMT+0";

java -jar /home/sip/emailgrabber/jar/sip.jar ${arguments};
