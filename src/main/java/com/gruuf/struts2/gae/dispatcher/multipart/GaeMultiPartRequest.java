package com.gruuf.struts2.gae.dispatcher.multipart;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.dispatcher.multipart.AbstractMultiPartRequest;
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
public class GaeMultiPartRequest extends AbstractMultiPartRequest {

    private static final Logger LOG = LogManager.getLogger(GaeMultiPartRequest.class);

    // maps parameter name -> List of FileItemStream objects
    private final Map<String, List<FileItemStream>> files = new HashMap<>();

    // maps parameter name -> List of String which contains encoded file contents
    private final Map<String, List<UploadedFile>> fileContents = new HashMap<>();

    // maps parameter name -> List of param values
    private final Map<String, List<String>> params = new HashMap<>();

    /**
     * Creates a new request wrapper to handle multi-part data using methods adapted from Jason Pell's
     * multipart classes (see class description).
     *
     * @param request the request containing the multipart
     * @throws java.io.IOException is thrown if encoding fails.
     */
    public void parse(HttpServletRequest request, String saveDir) throws IOException {
        try {
            setLocale(request);
            processUpload(request);
        } catch (FileUploadException e) {
            LOG.warn("Request exceeded size limit!", e);
            LocalizedMessage errorMessage;
            if(e instanceof FileUploadBase.SizeLimitExceededException) {
                FileUploadBase.SizeLimitExceededException ex = (FileUploadBase.SizeLimitExceededException) e;
                errorMessage = buildErrorMessage(e, new Object[]{ex.getPermittedSize(), ex.getActualSize()});
            } else {
                errorMessage = buildErrorMessage(e, new Object[]{});
            }

            if (!errors.contains(errorMessage)) {
                errors.add(errorMessage);
            }
        } catch (Exception e) {
            LOG.warn("Unable to parse request", e);
            LocalizedMessage errorMessage = buildErrorMessage(e, new Object[]{});
            if (!errors.contains(errorMessage)) {
                errors.add(errorMessage);
            }
        }
    }

    private void processUpload(HttpServletRequest request) throws FileUploadException, IOException {
        FileItemIterator iterator = parseRequest(request);
        while (iterator.hasNext()) {
            FileItemStream itemStream = iterator.next();
            LOG.debug("Found file item: [{}]", itemStream.getFieldName());

            if (itemStream.isFormField()) {
                processNormalFormField(itemStream, request.getCharacterEncoding());
            } else {
                processFileField(itemStream);
            }
        }
    }

    private void processFileField(FileItemStream itemStream) throws IOException {
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

    private void processNormalFormField(FileItemStream itemStream, String charset) throws IOException {
        LOG.debug("Item is a normal form field");

        List<String> values;
        if (params.get(itemStream.getFieldName()) != null) {
            values = params.get(itemStream.getFieldName());
        } else {
            values = new ArrayList<>();
        }

        InputStream in = itemStream.openStream();

        if (charset == null) {
            LOG.debug("Request doesn't specify encoding, using {}", defaultEncoding);
            values.add(IOUtils.toString(in, defaultEncoding));
        } else {
            LOG.debug("Used request's encoding {}", charset);
            values.add(IOUtils.toString(in, charset));
        }

        params.put(itemStream.getFieldName(), values);
    }

    private FileItemIterator parseRequest(HttpServletRequest request) throws FileUploadException, IOException {
        ServletFileUpload upload = new ServletFileUpload();
        upload.setSizeMax(maxSize);

        return upload.getItemIterator(request);
    }

    public Enumeration<String> getFileParameterNames() {
        return Collections.enumeration(files.keySet());
    }

    public String[] getContentType(String fieldName) {
        List<FileItemStream> items = files.get(fieldName);

        if (items == null) {
            return null;
        }

        List<String> contentTypes = new ArrayList<>(items.size());
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

        List<String> fileNames = new ArrayList<>(items.size());
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

    public void cleanUp() {
        // no-op
    }
}
