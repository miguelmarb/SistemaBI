package org.pivot4j.analytics.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.apache.commons.lang3.StringUtils;
import org.olap4j.Axis;
import org.olap4j.Axis.Factory;
import org.olap4j.Axis.Standard;

@FacesConverter("axisConverter")
public class AxisConverter
  implements Converter
{
  public Object getAsObject(FacesContext context, UIComponent component, String value)
  {
    if (value == null) {
      return null;
    }
    if (StringUtils.isEmpty(value)) {
      return null;
    }
    Axis.Standard axis = Axis.Standard.valueOf(value);
    
    return Axis.Factory.forOrdinal(axis.axisOrdinal());
  }
  
  public String getAsString(FacesContext context, UIComponent component, Object value)
  {
    if (value == null) {
      return null;
    }
    if ((value instanceof String)) {
      return (String)value;
    }
    Axis axis = (Axis)value;
    
    return axis.name();
  }
}
