package com.gruuf.struts2.gae.recaptcha;

public interface ReCaptchaAware {

    /**
     * Sets result of performed reCAPTCHA verification
     *
     * @param valid true if validation passed
     */
    void setReCaptchaResult(boolean valid);

    /**
     * Allows enable/disable reCaptcha validation per action
     *
     * @return true if reCaptcha validation should be performed
     */
    boolean isReCaptchaEnabled();
}
