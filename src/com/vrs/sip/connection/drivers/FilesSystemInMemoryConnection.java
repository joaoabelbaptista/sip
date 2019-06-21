package com.vrs.sip.connection.drivers;

import com.vrs.sip.connection.ConnectionType;
import com.vrs.sip.connection.IStatement;

public class FilesSystemInMemoryConnection extends FilesystemConnection {

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.FILESYSTEM_IN_MEMORY;
    }

    @Override
    public IStatement createStatement() throws Exception {
        FilesystemStatement statement = null;

        statement = new FileSystemInMemoryStatement();

        statement.setConnection(this);

        return statement;
    }

}
