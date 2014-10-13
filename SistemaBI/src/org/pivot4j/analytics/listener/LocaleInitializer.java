package org.pivot4j.analytics.listener;

import java.util.Locale;
import java.util.Map;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpSession;
import org.pivot4j.analytics.config.Settings;

public class LocaleInitializer
  implements PhaseListener
{
  private static final long serialVersionUID = -2477093113131236331L;
  
  public PhaseId getPhaseId()
  {
    return PhaseId.RESTORE_VIEW;
  }
  
  public void beforePhase(PhaseEvent event) {}
  
  public void afterPhase(PhaseEvent event)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext externalContext = context.getExternalContext();
    
    Settings settings = (Settings)externalContext.getApplicationMap().get("settings");
    

    Locale locale = null;
    
    HttpSession session = (HttpSession)externalContext.getSession(false);
    if (session != null)
    {
      String key = settings.getLocaleAttributeName();
      if (key != null)
      {
        Object value = session.getAttribute(key);
        if ((value instanceof Locale))
        {
          locale = (Locale)value;
        }
        else if (value != null)
        {
          String[] args = value.toString().split("_");
          if (args.length == 1) {
            locale = new Locale(args[0]);
          } else if (args.length == 2) {
            locale = new Locale(args[0], args[1]);
          } else if (args.length == 3) {
            locale = new Locale(args[0], args[1], args[2]);
          }
        }
      }
    }
    if ((locale != null) && (context.getViewRoot() != null)) {
      context.getViewRoot().setLocale(locale);
    }
  }
}
