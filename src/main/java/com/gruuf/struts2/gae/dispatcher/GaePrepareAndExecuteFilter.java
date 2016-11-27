package com.gruuf.struts2.gae.dispatcher;

import ognl.OgnlRuntime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.dispatcher.InitOperations;
import org.apache.struts2.dispatcher.filter.StrutsPrepareAndExecuteFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * This filter only detects if OGNL was properly configured with ServletListener
 */
public class GaePrepareAndExecuteFilter extends StrutsPrepareAndExecuteFilter {

    private static final Logger LOG = LogManager.getLogger(GaePrepareAndExecuteFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (OgnlRuntime.getSecurityManager() != null) {
            LOG.warn("OgnlRuntime.getSecurityManager() is defined, this can break your application when running on AppEngine!" +
                    "Add the following code to web.xml:\n" +
                    "<listener>\n" +
                    "    <listener-class>com.gruuf.struts2.gae.dispatcher.GaeInitListener</listener-class>\n" +
                    "</listener>\n");
        }

        super.init(filterConfig);
    }

    @Override
    protected InitOperations createInitOperations() {
        return new GaeInitOperations();
    }
}
