package org.pivot4j.analytics.property;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import org.pivot4j.ui.property.RenderPropertyList;

public abstract interface PropertyEditor
{
  public abstract void createComponent(PropertyDescriptor paramPropertyDescriptor, UIComponent paramUIComponent, ValueExpression paramValueExpression, MethodExpression paramMethodExpression, String paramString);
  
  public abstract Object getValue(PropertyDescriptor paramPropertyDescriptor, RenderPropertyList paramRenderPropertyList);
  
  public abstract void setValue(PropertyDescriptor paramPropertyDescriptor, RenderPropertyList paramRenderPropertyList, Object paramObject);
}
