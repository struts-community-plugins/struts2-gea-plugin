# Apache Struts 2 Google AppEngine plugin

Plugin supporting developing and running Apache Struts 2 based application on Google AppEngine.

[![Build Status](https://travis-ci.org/lukaszlenart/struts2-gea-plugin.svg?branch=master)](https://travis-ci.org/lukaszlenart/struts2-gea-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.gruuf/struts2-gae-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.gruuf/struts2-gae-plugin/)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

## Installation

Just add this plugin as a dependency in `pom.xml`:

```xml
<dependency>
  <groupId>com.gruuf</groupId>
  <artifactId>struts2-gae-plugin</artifactId>
  <version>[VERSION]</version>
</dependency>
```

### File upload support

To provide a proper file upload support on the Google AppEngine you must use the below filter instead of the one provided
by Struts, update `web.xml` as follow:

```xml
<filter>
  <filter-name>struts2</filter-name>
  <filter-class>com.gruuf.struts2.gae.dispatcher.GaePrepareAndExecuteFilter</filter-class>
</filter>
```

This filter detects if OGNL was properly configured, to do so add the following listener to `web,xml`

```xml
<listener>
  <listener-class>com.gruuf.struts2.gae.dispatcher.GaeInitListener</listener-class>
</listener>
```

It also redefine `Dispatcher`'s `getSaveDir` to allow properly upload files on AppEngine.

### reCAPTCHA support

There is a dedicated interceptor with connected interface to allow perform reCAPTCHA validation per action.

First you must define your reCAPTCHA secret using a constant:

```xml
<constant name="struts.gae.reCaptchaSecret" value="${env.GRUUF_RECAPTCHA_SECRET}"/>
```

Now your action can implement `ReCaptchaAware` interface and implement the below methods:

- `setReCaptchaResult` - you will get `true` when reCAPTCHA validation has passed
- `isReCaptchaEnabled` - allows you enabling/disabling reCAPTCHA validation per action, e.g.: when user is logged-in
  you don't want to perform the validation
  
The last thing is to define the reCAPTCHA interceptor and add it to your interceptors stack:

```xml
<interceptor name="reCaptcha" class="com.gruuf.struts2.gae.recaptcha.ReCaptchaInterceptor"/>
```  

```xml
<interceptor-stack name="reCaptchaStack">
    <interceptor-ref name="strutsDefault"/>
  <interceptor-ref name="reCaptcha"/>
</interceptor-stack>
```

And finally you can add the reCAPTCHA code to your page.

### Testimonials

This plugin is based on a code developed here https://code.google.com/archive/p/struts2-gae/

More details can be found here https://squarepusher782.wordpress.com/2010/10/01/file-upload-on-google-app-engine-using-struts2-revisited/

I have also taken some inspiration from here https://github.com/triologygmbh/reCAPTCHA-V2-java
