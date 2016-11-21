package com.gruuf.struts2.gae.dispatcher.multipart;

public class GaeUploadedFile {

    private byte[] bytes;

    public GaeUploadedFile(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public Long getLength() {
        return bytes != null ? bytes.length : 0L;
    }
}
