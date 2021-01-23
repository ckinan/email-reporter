package com.ckinan.emailreporter.core.pojo;

public class EmailMessage {

    private Long watermark;
    private String body;

    public Long getWatermark() {
        return watermark;
    }

    public void setWatermark(Long watermark) {
        this.watermark = watermark;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
