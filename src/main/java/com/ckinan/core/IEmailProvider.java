package com.ckinan.core;

import java.io.IOException;
import java.util.List;

public interface IEmailProvider {

    Long getWatermark() throws IOException;
    List<String> getPendingMessageIds(String query) throws IOException;
    List<List<Object>> calculateNewValues(List<String> messageIds, Long watermark) throws IOException;
    String calculateQuery(Long watermark);
    List<List<Object>> generateReport() throws IOException;

}