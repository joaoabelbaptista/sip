package com.vrs.sip.connection.drivers;

import com.vrs.sip.connection.IConnectionAttributes;
import com.vrs.sip.connection.IResultSet;

public class FileSystemInMemoryStatement extends  FilesystemStatement{

    @Override
    public IResultSet executeQuery(String filePattern) throws Exception {

        IConnectionAttributes connectionAttributes = connection.getConnectionAttributes();
        String directory = connectionAttributes.getDirectory();
        FilesSystemInMemoryResultSet frs;

        if (directory == null || directory.trim().equals("") == true) {
            directory = ".";
        }

        frs = new FilesSystemInMemoryResultSet();

        frs.setStatement(this);
        frs.setFilePattern(filePattern);
        frs.initListFiles();

        return frs;
    }
}
