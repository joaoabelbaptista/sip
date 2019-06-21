/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Oracle Implementation of a Connection.
 * History: aosantos, 2016-06-26, Initial Release.
 * 
 * 
 */
package com.vrs.sip.connection.drivers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.vrs.sip.FileLog;
import com.vrs.sip.connection.AbstractConnection;
import com.vrs.sip.connection.ConnectionType;
import com.vrs.sip.connection.IConnectionAttributes;
import com.vrs.sip.connection.ICredentials;
import com.vrs.sip.connection.IStatement;
import com.vrs.sip.connection.attributes.OracleConnectionAttributes;
import com.vrs.sip.connection.credentials.OracleCredentials;

import oracle.jdbc.pool.OracleDataSource;

public class OracleConnection extends AbstractConnection {
	FileLog log;
	
	private Connection connection;

	@Override
	public void setLog(FileLog log) {
		this.log = log;
	}
	
	@Override
	public FileLog getLog() {
		if (log == null) {
			log = new FileLog();
		}
		
		return log;
	}
	
	/** Private Method **/
	private OracleDataSource getOracleDataSource() throws SQLException {
		OracleDataSource oracleDS = new OracleDataSource();
		IConnectionAttributes oracleAttributes;
		
		oracleAttributes = getConnectionAttributes();
		
		oracleDS.setUser(getCredentials().getUsername());
		oracleDS.setPassword(getCredentials().getPassword());
		oracleDS.setURL("jdbc:oracle:thin:@" + getCredentials().getHostname() + ":" + getCredentials().getPort() + "/" + getCredentials().getService());
		
		if (oracleAttributes != null) {
			if (oracleAttributes.getLoginTimeout() != null) {
				oracleDS.setLoginTimeout(oracleAttributes.getLoginTimeout());
			}
		}
		
		return oracleDS;
	}
	
	private void connectivityTest() throws Exception {
		PreparedStatement ps;
		ResultSet rs;
		
		getLog().debug("connectivityTest START");
		
		ps = connection.prepareStatement("SELECT SYSDATE FROM DUAL");
		rs = ps.executeQuery();
		
		if (rs != null) {
			while (rs.next()) {
				Date sysdate = rs.getDate(1);
				
				getLog().debug("connectivityTest: sysdate="  + sysdate );
			}
			
			rs.close();
		}
	}

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.ORACLE;
	}

	@Override
	public ICredentials getCredentials() {
		if (credentials == null) {
			credentials = new OracleCredentials(null);
		}
		
		return credentials;
	}
	
	@Override
	public void setCredentials(ICredentials credentials) {
		this.credentials = credentials;
	}

	@Override
	public IConnectionAttributes getConnectionAttributes() {
		if (connectionAttributes == null) {
			connectionAttributes = new OracleConnectionAttributes();
		}
		
		return connectionAttributes;
	}
	
	@Override
	public void openConnection() throws Exception {
		IConnectionAttributes connectionAttributes = getConnectionAttributes();
		
		getLog().debug("openConnection START");

		if (connection == null) {
			OracleDataSource oracleDataSource = getOracleDataSource();
			
			connection = oracleDataSource.getConnection();
			
			if (connection != null) {
				if (connectionAttributes != null) {
					if (connectionAttributes.getAutoCommit() != null) {
						connection.setAutoCommit(getConnectionAttributes().getAutoCommit());
					}
				}
				
				connectivityTest();
			}
		}
	}

	@Override
	public void closeConnection() throws Exception {
		getLog().debug("closeConnection START");
		
		if (connection != null) {
			connection.close();
			
			connection = null;
		}
	}

	@Override
	public IStatement createStatement() throws Exception {
		OracleStatement statement = null;
		
		getLog().debug("createStatement");
		
		if (connection != null) {
			statement = new OracleStatement();
			statement.setConnection(this);
		}
		
		return statement;
	}

	@Override
	public void commit() throws Exception {
		getLog().debug("commit");
		
		if (connection != null) {
			connection.commit();
		}
	}
	
	public Connection getOracleJdbcConnection() {
		return connection;
	}

	@Override
	public Object getImplConnection() {
		return connection;
	}
}

