package org.pivot4j.analytics.exception;

import java.util.Iterator;
import java.util.ResourceBundle;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pivot4JExceptionHandler
  extends ExceptionHandlerWrapper
{
  private Logger logger = LoggerFactory.getLogger(getClass());
  private ExceptionHandler handler;
  
  public Pivot4JExceptionHandler(ExceptionHandler handler)
  {
    this.handler = handler;
  }
  
  public ExceptionHandler getWrapped()
  {
    return this.handler;
  }
  
  public void handle()
    throws FacesException
  {
    Iterator<ExceptionQueuedEvent> it = getUnhandledExceptionQueuedEvents().iterator();
    while (it.hasNext())
    {
      ExceptionQueuedEvent event = (ExceptionQueuedEvent)it.next();
      
      ExceptionQueuedEventContext context = (ExceptionQueuedEventContext)event.getSource();
      
      FacesContext facesContext = FacesContext.getCurrentInstance();
      

      ResourceBundle bundle = facesContext.getApplication().getResourceBundle(facesContext, "msg");
      
      Throwable t = context.getException();
      Throwable cause = ExceptionUtils.getRootCause(t);
      if (cause == null) {
        cause = t;
      }
      String title = bundle.getString("error.unhandled.title");
      String message = bundle.getString("error.unhandled.message") + cause;
      if (this.logger.isErrorEnabled()) {
        this.logger.error(title, t);
      }
      facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message));
      

      it.remove();
    }
    getWrapped().handle();
  }
}
