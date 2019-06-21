package com.vrs.sip.connection.drivers;

import com.vrs.sip.Util;
import com.vrs.sip.connection.Field;
import com.vrs.sip.connection.FieldType;
import com.vrs.sip.connection.Record;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilesSystemInMemoryResultSet extends FilesystemResultSet {

    @Override
    public List<Record> fetchRows() throws Exception {
        List<Record> result = new Vector<Record>();
        byte[] fileData = null;

        log.debug("fetchRows START OLA");

        log.debug("Pattern = " + filePattern);

        Pattern pattern = Pattern.compile(filePattern);

        if (fileList != null && fileList.isEmpty() == false) {
            for (String filename : fileList) {
                Matcher matcher = pattern.matcher(filename);
                BasicFileAttributes fileAttributes = null;

                if (matcher.matches()) {
                    FileTime createdFileTime = null;
                    FileTime lastModifiedFileTime = null;
                    Date createdDate = null;
                    Date lastModifiedDate = null;

                    List<Field> recordFieldList = new Vector<Field>();

                    try {
                        File file = new File(directory + "/" + filename);
                        LinkOption options = LinkOption.NOFOLLOW_LINKS;

                        fileAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class, options);

                        if (fileAttributes != null) {
                            createdFileTime = fileAttributes.creationTime();
                            lastModifiedFileTime = fileAttributes.lastModifiedTime();

                            if (createdFileTime != null) {
                                createdDate = new Date(createdFileTime.toMillis());
                                lastModifiedDate = new Date(lastModifiedFileTime.toMillis());
                            }
                        }


                        Path path = Paths.get(directory + "/" + filename);
                        fileData = Files.readAllBytes(path);

                    } catch (IOException e) {
                        log.info("directory=" + directory + ", filename=" + filename + ": " + Util.getStackTraceString(e));
                    }


                    recordFieldList.add(new Field("directory", FieldType.T_STRING, directory));
                    recordFieldList.add(new Field("filename", FieldType.T_STRING, filename));
                    recordFieldList.add(new Field("createdDate", FieldType.T_DATE, createdDate));
                    recordFieldList.add(new Field("lastModifiedDate", FieldType.T_DATE, lastModifiedDate));
                    recordFieldList.add(new Field("contentBytes", FieldType.T_FILE, fileData));

                    Record record = new Record(recordFieldList);

                    result.add(record);
                }

            }
        }

        fileList = null;

        log.debug("Returning " + (result != null ? result.size() : 0) + " files");

        log.debug("fetchRows END");

        return result;
    }

}
