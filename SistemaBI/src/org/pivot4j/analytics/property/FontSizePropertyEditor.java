package org.pivot4j.analytics.property;

import java.util.List;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.ui.property.RenderPropertyList;
import org.pivot4j.ui.property.SimpleRenderProperty;

public class FontSizePropertyEditor
  extends IntegerPropertyEditor
{
  public FontSizePropertyEditor()
  {
    super(Integer.valueOf(1), null, Integer.valueOf(6));
  }
  
  public void createComponent(PropertyDescriptor descriptor, UIComponent parent, ValueExpression expression, MethodExpression listener, String update)
  {
    super.createComponent(descriptor, parent, expression, listener, update);
    
    HtmlOutputText unitText = new HtmlOutputText();
    unitText.setStyleClass("unit-text");
    unitText.setValue("(pt)");
    
    parent.getChildren().add(unitText);
  }
  
  protected Object getValue(SimpleRenderProperty property)
  {
    String stringValue = StringUtils.trimToNull(property.getValue());
    
    Object value = null;
    if ((stringValue != null) && (stringValue.matches("[0-9]+pt"))) {
      value = Integer.valueOf(Integer.parseInt(stringValue.substring(0, stringValue
        .length() - 2)));
    } else {
      value = null;
    }
    return value;
  }
  
  public void setValue(PropertyDescriptor descriptor, RenderPropertyList properties, Object value)
  {
    String fontSize = StringUtils.trimToNull(ObjectUtils.toString(value));
    if (fontSize != null) {
      fontSize = fontSize + "pt";
    }
    super.setValue(descriptor, properties, fontSize);
  }
}
