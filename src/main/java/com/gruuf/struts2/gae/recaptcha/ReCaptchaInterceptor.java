package com.gruuf.struts2.gae.recaptcha;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Interceptor should be used to validate actions that require reCAPTCHA validation.
 * It works only with POST requests. Please define a constant with a proper secret:
 *
 * <constant name="struts.gae.reCaptchaSecret" value="${env.MY_RECAPTCHA_SECRET}"/>
 *
 * or each action will have to provide it by itself.
 *
 * See also {@link ReCaptchaAware} interface.
 */
public class ReCaptchaInterceptor extends AbstractInterceptor {

    private static final Logger LOG = LogManager.getLogger(ReCaptchaInterceptor.class);

    private String reCaptchaSecret;

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        Object action = invocation.getAction();
        HttpServletRequest request = ServletActionContext.getRequest();

        if (action instanceof ReCaptchaAware && "post".equalsIgnoreCase(request.getMethod())) {
            LOG.debug("A ReCaptchaAware action, performing ReCaptcha validation");
            ReCaptchaAware reCaptchaAction = (ReCaptchaAware) action;

            if (reCaptchaAction.isReCaptchaEnabled()) {
                LOG.debug("ReCaptcha enabled per action");

                String secret = reCaptchaSecret;
                if (secret == null || secret.length() == 0) {
                    LOG.debug("No ReCaptcha secret defined per interceptor, using action {}", action.getClass().getName());
                    secret = reCaptchaAction.getReCaptchaSecret();
                }


                ReCaptcha reCaptcha = new ReCaptcha(secret);
                boolean valid = reCaptcha.isValid(request.getParameter("g-recaptcha-response"), request.getRemoteAddr());

                reCaptchaAction.setReCaptchaResult(valid);
            } else {
                LOG.debug("Ignoring reCaptcha validation per action {}", action.getClass().getName());
            }
        }

        return invocation.invoke();
    }

    @Inject("struts.gae.reCaptchaSecret")
    public void setReCaptchaSecret(String reCaptchaSecret) {
        this.reCaptchaSecret = reCaptchaSecret;
    }
}
