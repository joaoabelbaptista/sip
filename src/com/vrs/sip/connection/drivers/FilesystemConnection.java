package com.vrs.sip.connection.drivers;

import com.vrs.sip.FileLog;
import com.vrs.sip.connection.AbstractConnection;
import com.vrs.sip.connection.ConnectionType;
import com.vrs.sip.connection.IConnectionAttributes;
import com.vrs.sip.connection.ICredentials;
import com.vrs.sip.connection.IStatement;
import com.vrs.sip.connection.attributes.FilesystemConnectionAttributes;
import com.vrs.sip.connection.credentials.NoCredentials;

public class FilesystemConnection extends AbstractConnection {
	FileLog log;

	private ICredentials credentials;
	private IConnectionAttributes connectionAttributes;

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

	@Override
	public void setCredentials(ICredentials credentials) throws Exception {
	}

	@Override
	public Object getImplConnection() {
		return null;
	}

	@Override
	public ConnectionType getConnectionType() {
		return ConnectionType.FILESYSTEM;
	}

	@Override
	public ICredentials getCredentials() {
		if (credentials == null) {
			credentials = new NoCredentials();
		}
		
		return credentials;
	}

	@Override
	public IConnectionAttributes getConnectionAttributes() {
		if (connectionAttributes == null) {
			connectionAttributes = new FilesystemConnectionAttributes();
		}
		
		return connectionAttributes;
	}

	@Override
	public void openConnection() throws Exception {
	}

	@Override
	public void closeConnection() throws Exception {
	}

	@Override
	public IStatement createStatement() throws Exception {
		FilesystemStatement statement = null;
		
		statement = new FilesystemStatement();
		
		statement.setConnection(this);
		
		return statement;
	}

	@Override
	public void commit() throws Exception {
	}
}
