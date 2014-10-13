package org.pivot4j.analytics.converter;

import java.util.ResourceBundle;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.SelectItem;

@FacesConverter("aggregatorConverter")
public class AggregatorConverter
  implements Converter
{
  public Object getAsObject(FacesContext context, UIComponent component, String value)
  {
    if (value == null) {
      return null;
    }
    ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
    

    String key = "label.aggregation.type." + value;
    
    return new SelectItem(value, bundle.getString(key));
  }
  
  public String getAsString(FacesContext context, UIComponent component, Object value)
  {
    if (value == null) {
      return null;
    }
    if ((value instanceof String)) {
      return (String)value;
    }
    SelectItem item = (SelectItem)value;
    return (String)item.getValue();
  }
}
