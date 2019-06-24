package com.vrs.sip.exchange;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public class EmailFileAttachment {

    String name;
    ByteArrayOutputStream outputStream;
    byte[] emailMimeContent;
    byte[] fileBytes;

    public EmailFileAttachment(String name, ByteArrayOutputStream outputStream, byte[] emailMimeContent) {
        this.name = name;
        this.outputStream = outputStream;
        this.emailMimeContent = emailMimeContent;
    }

   /*
    public EmailFileAttachment(String name,  byte[] emailMimeContent) {
        this.name = name;
        this.outputStream = outputStream;
        this.emailMimeContent = emailMimeContent;
    }
    */
   public EmailFileAttachment() {

   }

    public EmailFileAttachment(String name, ByteArrayOutputStream outputStream) {
        this.name = name;
        this.outputStream = outputStream;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public byte[] toByteArray()
    {
        if (outputStream != null)
          return  outputStream.toByteArray();
        else
          return null;
    }


    public byte[] getEmailMimeContent() {
        return emailMimeContent;
    }

    public void setEmailMimeContent(byte[] emailMimeContent) {
        this.emailMimeContent = emailMimeContent;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }


}
