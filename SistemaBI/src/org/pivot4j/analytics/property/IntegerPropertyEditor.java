package org.pivot4j.analytics.property;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.ui.property.SimpleRenderProperty;
import org.primefaces.component.spinner.Spinner;

public class IntegerPropertyEditor
  extends AbstractPropertyInputEditor
{
  private Integer minimumValue;
  private Integer maximumValue;
  private Integer size;
  
  public IntegerPropertyEditor() {}
  
  public IntegerPropertyEditor(Integer minimumValue, Integer maximumValue, Integer size)
  {
    this.minimumValue = minimumValue;
    this.maximumValue = maximumValue;
    this.size = size;
  }
  
  protected UIInput createInput(PropertyDescriptor descriptor, UIComponent parent, FacesContext context)
  {
    Application application = FacesContext.getCurrentInstance().getApplication();
    
    Spinner spinner = (Spinner)application.createComponent("org.primefaces.component.Spinner");
    if (this.minimumValue != null) {
      spinner.setMin(this.minimumValue.intValue());
    }
    if (this.maximumValue != null) {
      spinner.setMax(this.maximumValue.intValue());
    }
    if (this.size != null) {
      spinner.setSize(this.size.intValue());
    }
    return spinner;
  }
  
  protected Object getValue(SimpleRenderProperty property)
  {
    String stringValue = StringUtils.trimToNull(
      (String)super.getValue(property));
    
    Object value = null;
    if (NumberUtils.isNumber(stringValue)) {
      value = Integer.valueOf(Integer.parseInt(stringValue));
    } else {
      value = null;
    }
    return value;
  }
  
  public Integer getMinimumValue()
  {
    return this.minimumValue;
  }
  
  public void setMinimumValue(Integer minimumValue)
  {
    this.minimumValue = minimumValue;
  }
  
  public Integer getMaximumValue()
  {
    return this.maximumValue;
  }
  
  public void setMaximumValue(Integer maximumValue)
  {
    this.maximumValue = maximumValue;
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
