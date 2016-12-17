package com.gruuf.struts2.gae.dispatcher;

import ognl.OgnlRuntime;
import org.apache.struts2.StrutsJUnit4TestCase;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletContextEvent;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

public class GaeInitListenerTest extends StrutsJUnit4TestCase<GaeInitListener> {

    @Test
    public void disablingOgnlSecurityManager() throws Exception {
        // given
        GaeInitListener listener = new GaeInitListener();
        ServletContextEvent sce = Mockito.mock(ServletContextEvent.class);
        SecurityManager sm = Mockito.mock(SecurityManager.class);
        OgnlRuntime.setSecurityManager(sm);


        assertThat(OgnlRuntime.getSecurityManager(), is(notNullValue()));

        // when
        listener.contextInitialized(sce);

        // then
        Mockito.verifyZeroInteractions(sce);
        assertThat(OgnlRuntime.getSecurityManager(), is(nullValue()));
    }
}