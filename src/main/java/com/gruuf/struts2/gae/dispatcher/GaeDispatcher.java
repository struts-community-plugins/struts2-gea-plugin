package com.gruuf.struts2.gae.dispatcher;

import org.apache.struts2.dispatcher.Dispatcher;

import javax.servlet.ServletContext;
import java.util.Map;

public class GaeDispatcher extends Dispatcher {

    GaeDispatcher(ServletContext servletContext, Map<String, String> params) {
        super(servletContext, params);
    }

    @Override
    protected String getSaveDir() {
        // Google AppEngine doesn't allow to define a custom save dir
        return null;
    }
}
