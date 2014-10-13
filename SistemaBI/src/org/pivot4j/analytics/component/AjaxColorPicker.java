package org.pivot4j.analytics.component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.faces.application.ResourceDependencies;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.FacesEvent;
import org.primefaces.component.colorpicker.ColorPicker;

@FacesComponent("org.pivot4j.component.ColorPicker")
@ResourceDependencies({@javax.faces.application.ResourceDependency(library="pivot4j", name="js/colorpicker.js")})
public class AjaxColorPicker
  extends ColorPicker
  implements ClientBehaviorHolder
{
  public static final String COMPONENT_FAMILY = "org.pivot4j.component";
  public static final String COMPONENT_TYPE = "org.pivot4j.component.ColorPicker";
  public static final String RENDERER_TYPE = "org.pivot4j.component.ColorPickerRenderer";
  
  public AjaxColorPicker()
  {
    setRendererType("org.pivot4j.component.ColorPickerRenderer");
  }
  
  public String getFamily()
  {
    return "org.pivot4j.component";
  }
  
  public Collection<String> getEventNames()
  {
    return Arrays.asList(new String[] { "change" });
  }
  
  public void queueEvent(FacesEvent event)
  {
    FacesContext context = getFacesContext();
    if ((event instanceof AjaxBehaviorEvent))
    {
      AjaxBehaviorEvent behaviorEvent = (AjaxBehaviorEvent)event;
      
      Map<String, String> params = context.getExternalContext().getRequestParameterMap();
      
      String eventName = (String)params.get("javax.faces.behavior.event");
      String clientId = getClientId(context);
      if (eventName.equals("change"))
      {
        String value = (String)params.get(clientId + "_input");
        if (value != null)
        {
          ChangeEvent changeEvent = new ChangeEvent(this, behaviorEvent.getBehavior(), value);
          changeEvent.setPhaseId(behaviorEvent.getPhaseId());
          
          super.queueEvent(changeEvent);
        }
      }
    }
    else
    {
      super.queueEvent(event);
    }
  }
}
