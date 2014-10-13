package org.pivot4j.analytics.property;

import java.util.List;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import org.primefaces.component.behavior.ajax.AjaxBehavior;
import org.primefaces.component.behavior.ajax.AjaxBehaviorListenerImpl;

public abstract class AbstractPropertyInputEditor
  extends AbstractPropertyEditor
{
  public void createComponent(PropertyDescriptor descriptor, UIComponent parent, ValueExpression expression, MethodExpression listener, String update)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    UIInput input = createInput(descriptor, parent, context);
    
    input.setValueExpression("value", expression);
    
    String eventName = getEventName();
    if (eventName != null)
    {
      AjaxBehavior behavior = new AjaxBehavior();
      behavior.addAjaxBehaviorListener(new AjaxBehaviorListenerImpl(listener, listener));
      
      behavior.setUpdate(update);
      
      input.addClientBehavior("change", behavior);
    }
    parent.getChildren().add(input);
  }
  
  protected String getEventName()
  {
    return "change";
  }
  
  protected abstract UIInput createInput(PropertyDescriptor paramPropertyDescriptor, UIComponent paramUIComponent, FacesContext paramFacesContext);
}
