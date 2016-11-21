package com.gruuf.struts2.gae.dispatcher;

import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.HostConfig;
import org.apache.struts2.dispatcher.InitOperations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GaeInitOperations extends InitOperations {

    @Override
    public Dispatcher initDispatcher(HostConfig filterConfig) {
        Dispatcher dispatcher = createDispatcher(filterConfig);
        dispatcher.init();
        return dispatcher;
    }

    private Dispatcher createDispatcher(HostConfig filterConfig) {
        Map<String, String> params = new HashMap<>();
        for (Iterator e = filterConfig.getInitParameterNames(); e.hasNext(); ) {
            String name = (String) e.next();
            String value = filterConfig.getInitParameter(name);
            params.put(name, value);
        }
        return new GaeDispatcher(filterConfig.getServletContext(), params);
    }
}
