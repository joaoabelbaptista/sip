package com.vrs.sip.exchange;

import java.util.ArrayList;

public class Email {
    private  String from;
    private  String to;
    private  String cc;
    private  String bcc;
    private  String subject;
    private  String body;
    private  ArrayList<EmailFileAttachment> emailFileAttachments;

    public Email(String from, String to, String cc, String bcc, String subject, String body, ArrayList<EmailFileAttachment> emailFileAttachments) {
        this.setFrom(from);
        this.setTo(to);
        this.setCc(cc);
        this.setBcc(bcc);
        this.setSubject(subject);
        this.setBody(body);
        this.setEmailFileAttachments(emailFileAttachments);
    }

    public Email(){}

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getCc() {
        return cc;
    }

    public String getBcc() {
        return bcc;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public ArrayList<EmailFileAttachment> getEmailFileAttachments() {
        return emailFileAttachments;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setEmailFileAttachments(ArrayList<EmailFileAttachment> emailFileAttachments) {
        this.emailFileAttachments = emailFileAttachments;
    }
}
