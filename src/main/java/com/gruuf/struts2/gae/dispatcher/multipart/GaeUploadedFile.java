package com.gruuf.struts2.gae.dispatcher.multipart;

import org.apache.struts2.dispatcher.multipart.UploadedFile;

public class GaeUploadedFile implements UploadedFile {

    private String name;
    private byte[] content;

    GaeUploadedFile(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    @Override
    public Long length() {
        return (long) content.length;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public String getAbsolutePath() {
        return null;
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "GaeUploadedFile{" +
                "name='" + name + '\'' +
                '}';
    }
}
