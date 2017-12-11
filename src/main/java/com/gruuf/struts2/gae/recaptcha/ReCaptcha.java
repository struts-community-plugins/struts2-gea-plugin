package com.gruuf.struts2.gae.recaptcha;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

class ReCaptcha {

    private static final Logger LOG = LogManager.getLogger(ReCaptcha.class);

    private static final String SITE_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final String UTF_8 = "UTF-8";
    private static final String IS_SUCCESS = "(?s).*\"success\".*:.*true.*";

    private final String secret;

    ReCaptcha(String secret) {
        this.secret = secret;
    }

    boolean isValid(String response, String remoteIP) throws IOException {
        URL url = new URL(SITE_VERIFY_URL);
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("secret", secret);
        params.put("response", response);
        params.put("remoteip", remoteIP);

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), UTF_8));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), UTF_8));
        }
        byte[] postDataBytes = postData.toString().getBytes(UTF_8);

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), UTF_8));

        StringBuilder sb = new StringBuilder();
        for (int c; (c = in.read()) >= 0;) {
            sb.append((char) c);
        }

        String serverResponse = sb.toString();

        LOG.debug("Got response from server: {}", serverResponse);

        return serverResponse.matches(IS_SUCCESS);
    }
}
