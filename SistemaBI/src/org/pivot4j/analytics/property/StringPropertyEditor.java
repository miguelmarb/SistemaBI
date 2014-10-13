package org.pivot4j.analytics.property;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import org.primefaces.component.inputtext.InputText;

public class StringPropertyEditor
  extends AbstractPropertyInputEditor
{
  private Integer size;
  
  public StringPropertyEditor() {}
  
  public StringPropertyEditor(Integer size)
  {
    this.size = size;
  }
  
  protected UIInput createInput(PropertyDescriptor descriptor, UIComponent parent, FacesContext context)
  {
    Application application = FacesContext.getCurrentInstance().getApplication();
    

    InputText input = (InputText)application.createComponent("org.primefaces.component.InputText");
    if (this.size != null) {
      input.setSize(this.size.intValue());
    }
    return input;
  }
  
  public Integer getSize()
  {
    return this.size;
  }
  
  public void setSize(Integer size)
  {
    this.size = size;
  }
}
