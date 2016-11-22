package com.gruuf.struts2.gae.dispatcher.multipart;

import com.opensymphony.xwork2.inject.Inject;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.dispatcher.multipart.MultiPartRequest;
import org.apache.struts2.dispatcher.multipart.UploadedFile;
import org.apache.struts2.dispatcher.LocalizedMessage;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dedicated {@link MultiPartRequest} that doesn't depend on {@link java.io.File} to avoid problems
 * when running in AppEngine container.
 */
public class GaeMultiPartRequest implements MultiPartRequest {

    private static final Logger LOG = LogManager.getLogger(GaeMultiPartRequest.class);

    // maps parameter name -> List of FileItemStream objects
    private final Map<String, List<FileItemStream>> files = new HashMap<>();

    // maps parameter name -> List of String which contains encoded file contents
    private final Map<String, List<UploadedFile>> fileContents = new HashMap<>();

    // maps parameter name -> List of param values
    private final Map<String, List<String>> params = new HashMap<>();

    // any errors while processing this request
    private final List<LocalizedMessage> errors = new ArrayList<>();

    private long maxSize;

    @Inject(StrutsConstants.STRUTS_MULTIPART_MAXSIZE)
    public void setMaxSize(String maxSize) {
        this.maxSize = Long.parseLong(maxSize);
    }

    /**
     * Creates a new request wrapper to handle multi-part data using methods adapted from Jason Pell's
     * multipart classes (see class description).
     *
     * @param request the request containing the multipart
     * @throws java.io.IOException is thrown if encoding fails.
     */
    public void parse(HttpServletRequest request, String saveDir) throws IOException {

        // Parse the request
        try {
            ServletFileUpload upload = new ServletFileUpload();
            upload.setSizeMax(maxSize);

            FileItemIterator iterator = upload.getItemIterator(request);

            while (iterator.hasNext()) {
                FileItemStream itemStream = iterator.next();
                InputStream in = itemStream.openStream();

                LOG.debug("Found item " + itemStream.getFieldName());

                if (itemStream.isFormField()) {
                    LOG.debug("Item is a normal form field");

                    List<String> values;
                    if (params.get(itemStream.getFieldName()) != null) {
                        values = params.get(itemStream.getFieldName());
                    } else {
                        values = new ArrayList<>();
                    }

                    values.add(IOUtils.toString(in, "ISO-8859-1"));
                    params.put(itemStream.getFieldName(), values);
                } else {
                    LOG.debug("Item is a file upload");

                    // Skip file uploads that don't have a file name - meaning that no file was selected.
                    if (itemStream.getName() == null || itemStream.getName().trim().length() < 1) {
                        LOG.debug("No file has been uploaded for the field: " + itemStream.getFieldName());
                        continue;
                    }

                    List<FileItemStream> values;
                    List<UploadedFile> fileValues;
                    if (files.get(itemStream.getFieldName()) != null) {
                        values = files.get(itemStream.getFieldName());
                        fileValues = fileContents.get(itemStream.getFieldName());
                    } else {
                        values = new ArrayList<>();
                        fileValues = new ArrayList<>();
                    }

                    values.add(itemStream);
                    fileValues.add(new GaeUploadedFile(itemStream.getName(), IOUtils.toByteArray(itemStream.openStream())));
                    files.put(itemStream.getFieldName(), values);
                    fileContents.put(itemStream.getFieldName(), fileValues);
                }
            }
        } catch (FileUploadException e) {
            LOG.error("Unable to parse request", e);
            errors.add(buildErrorMessage(e, new Object[]{}));
        }
    }

    protected LocalizedMessage buildErrorMessage(Throwable e, Object[] args) {
        String errorKey = "struts.messages.upload.error." + e.getClass().getSimpleName();
        LOG.debug("Preparing error message for key: [{}]", errorKey);
        return new LocalizedMessage(this.getClass(), errorKey, e.getMessage(), args);
    }

    public Enumeration<String> getFileParameterNames() {
        return Collections.enumeration(files.keySet());
    }

    public String[] getContentType(String fieldName) {
        List<FileItemStream> items = files.get(fieldName);

        if (items == null) {
            return null;
        }

        List<String> contentTypes = new ArrayList<String>(items.size());
        for (FileItemStream fileItem : items) {
            contentTypes.add(fileItem.getContentType());
        }

        return contentTypes.toArray(new String[contentTypes.size()]);
    }

    public UploadedFile[] getFile(String fieldName) {
        List<UploadedFile> uploadedFiles = fileContents.get(fieldName);
        return uploadedFiles.toArray(new UploadedFile[uploadedFiles.size()]);
    }

    public String[] getFileNames(String fieldName) {
        List<FileItemStream> items = files.get(fieldName);

        if (items == null) {
            return null;
        }

        List<String> fileNames = new ArrayList<String>(items.size());
        for (FileItemStream fileItem : items) {
            fileNames.add(getCanonicalName(fileItem.getName()));
        }

        return fileNames.toArray(new String[fileNames.size()]);
    }

    public String[] getFilesystemName(String fieldName) {
        return null;
    }

    public String getParameter(String name) {
        List<String> v = params.get(name);
        if (v != null && v.size() > 0) {
            return v.get(0);
        }

        return null;
    }

    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(params.keySet());
    }

    public String[] getParameterValues(String name) {
        List<String> v = params.get(name);
        if (v != null && v.size() > 0) {
            return v.toArray(new String[v.size()]);
        }

        return null;
    }

    public List<LocalizedMessage> getErrors() {
        return errors;
    }

    /**
     * Returns the canonical name of the given file.
     *
     * @param filename the given file
     * @return the canonical name of the given file
     */
    private String getCanonicalName(String filename) {
        int forwardSlash = filename.lastIndexOf("/");
        int backwardSlash = filename.lastIndexOf("\\");
        if (forwardSlash != -1 && forwardSlash > backwardSlash) {
            filename = filename.substring(forwardSlash + 1, filename.length());
        } else if (backwardSlash != -1 && backwardSlash >= forwardSlash) {
            filename = filename.substring(backwardSlash + 1, filename.length());
        }

        return filename;
    }

    public void cleanUp() {
        // no-op
    }
}
