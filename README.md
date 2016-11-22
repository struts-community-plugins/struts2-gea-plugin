# Apache Struts 2 Google AppEngine plugin

Plugin supporting developing and running Apache Struts 2 based application on Google AppEngine.

[![Build Status](https://travis-ci.org/lukaszlenart/struts2-gea-plugin.svg?branch=master)](https://travis-ci.org/lukaszlenart/struts2-gea-plugin)

### Installation

Just add this plugin as a dependency in `pom.xml`:

```xml
<dependency>
  <groupId>com.gruuf</groupId>
  <artifactId>struts2-gae-plugin</artifactId>
  <version>[VERSION]</version>
</dependency>
```

and then instead of using common `org.apache.struts2.dispatcher.filter.StrutsPrepareAndExecuteFilter` filter in `web.xml`,
use the one that was included in the plugin:

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

### Testimonials

This plugin based on code developed here https://code.google.com/archive/p/struts2-gae/

More details can be found here https://squarepusher782.wordpress.com/2010/10/01/file-upload-on-google-app-engine-using-struts2-revisited/
