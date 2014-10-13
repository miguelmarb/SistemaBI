package org.pivot4j.analytics.logging;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.apache.logging.log4j.core.web.Log4jServletContextListener;

public class Log4jServletContextListenerFallback
  extends Log4jServletContextListener
{
  public static final int FALLBACK_MAJOR_VERSION = 3;
  
  public void contextInitialized(ServletContextEvent event)
  {
    if (event.getServletContext().getMajorVersion() < 3) {
      super.contextInitialized(event);
    }
  }
  
  public void contextDestroyed(ServletContextEvent event)
  {
    if (event.getServletContext().getMajorVersion() < 3) {
      super.contextDestroyed(event);
    }
  }
}
