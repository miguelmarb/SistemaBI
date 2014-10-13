package org.pivot4j.analytics.logging;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.logging.log4j.core.web.Log4jServletFilter;

public class Log4jServletFilterFallback
  extends Log4jServletFilter
{
  public static final int FALLBACK_MAJOR_VERSION = 3;
  private ServletContext servletContext;
  
  public void init(FilterConfig filterConfig)
    throws ServletException
  {
    this.servletContext = filterConfig.getServletContext();
    if (this.servletContext.getMajorVersion() < 3) {
      super.init(filterConfig);
    }
  }
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    if (this.servletContext.getMajorVersion() < 3) {
      super.doFilter(request, response, chain);
    } else {
      chain.doFilter(request, response);
    }
  }
  
  public void destroy()
  {
    if (this.servletContext.getMajorVersion() < 3) {
      super.destroy();
    }
    this.servletContext = null;
  }
}
