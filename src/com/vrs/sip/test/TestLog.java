package com.vrs.sip.test;

import com.vrs.sip.FileLog;
import com.vrs.sip.Server;
import com.vrs.sip.Util;

public class TestLog {

	public static void main(String[] args) {
		FileLog log = FileLog.getNewInstance(Server.class, "test_TestLog_" + Util.getSimpleUniqueId(), ".log");
		
		log.info("Hello World");
	}
}
