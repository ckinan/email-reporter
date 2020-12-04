package com.ckinan.core;

import com.ckinan.pojo.EmailMessage;

import java.io.IOException;
import java.util.List;

public interface IEmailReader {

    List<String> getPendingMessageIds(String query) throws IOException;
    EmailMessage getEmailMessageById(String id) throws IOException;
    String calculateQuery(String template, Long watermark);

}
