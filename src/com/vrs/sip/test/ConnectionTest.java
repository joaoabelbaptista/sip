/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Connection Test Class.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.test;

import java.util.List;

import com.vrs.sip.Factory;
import com.vrs.sip.connection.ConnectionType;
import com.vrs.sip.connection.IConnection;
import com.vrs.sip.connection.IResultSet;
import com.vrs.sip.connection.IStatement;
import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.credentials.OracleCredentials;

public class ConnectionTest {

	public static void oracleConnectionTest() throws Exception {
		OracleCredentials oracleCredentials;
		IConnection connection = Factory.getConnection(ConnectionType.ORACLE);
		IStatement statement;
		IResultSet resultSet;
		List<Record> batchRecords;
		
		oracleCredentials = new OracleCredentials();
		
		oracleCredentials.setHostname("centos.local");
		oracleCredentials.setPort(1521);
		oracleCredentials.setService("ORCL");
		oracleCredentials.setUsername("testuser");
		oracleCredentials.setPassword("xpto1234");
		
		connection.setCredentials(oracleCredentials);
		
		connection.openConnection();
		
		statement = connection.createStatement();
		
		resultSet = statement.executeQuery("SELECT TO_CHAR(SYSDATE,'YYYY-MM-DD HH24:MI:SS') ts FROM DUAL");
		
		do {
			batchRecords = resultSet.fetchRows();
			
			if (batchRecords != null && batchRecords.isEmpty() == false) {
				for (Record record : batchRecords) {
					System.out.println(record);
				}
			}
		} while (batchRecords != null && batchRecords.isEmpty() == false);
		
		connection.closeConnection();
	}
	
	public static void main(String[] args) throws Exception {
		oracleConnectionTest();
	}

}
