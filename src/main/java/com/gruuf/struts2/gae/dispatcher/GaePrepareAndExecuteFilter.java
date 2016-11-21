package com.gruuf.struts2.gae.dispatcher;

import org.apache.struts2.dispatcher.Dispatcher;
import org.apache.struts2.dispatcher.ExecuteOperations;
import org.apache.struts2.dispatcher.InitOperations;
import org.apache.struts2.dispatcher.PrepareOperations;
import org.apache.struts2.dispatcher.filter.FilterHostConfig;
import org.apache.struts2.dispatcher.filter.StrutsPrepareAndExecuteFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * GaeFilterDispatcher is an {@link FilterDispatcher} which is specific to Google App Engine.
 * Google App Engine imposes lots of restriction such as you cannot write to the FileSystem. To
 * overcome these restrictions you need to use GaeFilterDispatcher.
 * It overrides createDispatcher method to provide Google App Engine Specific Dispatcher {@link GaeDispatcher}.
 * To use this you need to configure this in your web.xml file instead of {@link FilterDispatcher}.
 *
 * @author whyjava7@gmail.com
 * @version 0.1
 */
@SuppressWarnings("deprecation")
public class GaePrepareAndExecuteFilter extends StrutsPrepareAndExecuteFilter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        InitOperations init = new GaeInitOperations();
        try {
            FilterHostConfig config = new FilterHostConfig(filterConfig);
            init.initLogging(config);
            Dispatcher dispatcher = init.initDispatcher(config);

            init.initStaticContentLoader(config, dispatcher);

            prepare = new PrepareOperations(dispatcher);
            execute = new ExecuteOperations(dispatcher);
            this.excludedPatterns = init.buildExcludedPatternsList(dispatcher);

            postInit(dispatcher, filterConfig);
        } finally {
            init.cleanup();
        }
    }

}
