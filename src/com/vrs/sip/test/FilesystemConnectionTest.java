package com.vrs.sip.test;

import java.util.List;

import com.vrs.sip.Factory;
import com.vrs.sip.connection.ConnectionType;
import com.vrs.sip.connection.IConnection;
import com.vrs.sip.connection.IConnectionAttributes;
import com.vrs.sip.connection.IResultSet;
import com.vrs.sip.connection.IStatement;
import com.vrs.sip.connection.Record;
import com.vrs.sip.connection.attributes.FilesystemConnectionAttributes;

public class FilesystemConnectionTest {
	public static void main(String[] args) throws Exception {
		IConnection connection = Factory.getConnection(ConnectionType.FILESYSTEM);
		IConnectionAttributes connectionAttributes = new FilesystemConnectionAttributes();
		IStatement statement;
		IResultSet resultSet;
		List<Record> batchRecords;
		
		connectionAttributes.setDirectory("/users/aosantos/tmp");
		connection.openConnection();
		connection.setConnectionAttributes(connectionAttributes);
		
		statement = connection.createStatement();
		
		resultSet = statement.executeQuery(".*");
		
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
}
