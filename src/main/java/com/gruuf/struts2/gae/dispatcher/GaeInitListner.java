package com.gruuf.struts2.gae.dispatcher;

import ognl.OgnlRuntime;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Disables OGNL Security Manager as this won't work on Google AppEngine
 */
public class GaeInitListner implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        OgnlRuntime.setSecurityManager(null);
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }

}
