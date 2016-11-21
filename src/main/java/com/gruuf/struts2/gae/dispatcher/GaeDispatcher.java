package com.gruuf.struts2.gae.dispatcher;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.gruuf.struts2.gae.dispatcher.multipart.GaeMultiPartRequestWrapper;
import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.StrutsRequestWrapper;

import com.gruuf.struts2.gae.dispatcher.multipart.GaeMultiPartRequest;

/**
 * {@link GaeDispatcher} is an {@link Dispatcher} specific to Google App Engine.
 * This dispatcher overrides wrapRequest method of {@link Dispatcher} so that 
 * multipart request can be handled in google app engine specific way.  
 */
public class GaeDispatcher extends Dispatcher {

	public GaeDispatcher(ServletContext servletContext,
			Map<String, String> initParams) {
		super(servletContext, initParams);
	}

    @Override
    public HttpServletRequest wrapRequest(HttpServletRequest request) throws IOException {
        if (request instanceof StrutsRequestWrapper) {
            return request;
        }

        String contentType = request.getContentType();

        if (contentType != null && contentType.startsWith("multipart/form-data")) {
        	GaeMultiPartRequest multi = getContainer().inject(GaeMultiPartRequest.class);
            request = new GaeMultiPartRequestWrapper(multi, request);
        } else {
            request = new StrutsRequestWrapper(request);
        }

        return request;
	}
}