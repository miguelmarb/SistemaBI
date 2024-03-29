package org.pivot4j.analytics.property;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.ui.property.RenderProperty;
import org.pivot4j.ui.property.RenderPropertyList;
import org.pivot4j.ui.property.SimpleRenderProperty;

public abstract class AbstractPropertyEditor
  implements PropertyEditor
{
  public Object getValue(PropertyDescriptor descriptor, RenderPropertyList properties)
  {
    if (descriptor == null) {
      throw new NullArgumentException("descriptor");
    }
    if (properties == null) {
      throw new NullArgumentException("properties");
    }
    RenderProperty property = properties.getRenderProperty(descriptor.getKey());
    if (property == null) {
      return null;
    }
    Object value = null;
    if ((property instanceof SimpleRenderProperty)) {
      value = getValue((SimpleRenderProperty)property);
    }
    return value;
  }
  
  protected Object getValue(SimpleRenderProperty property)
  {
    return property.getValue();
  }
  
  public void setValue(PropertyDescriptor descriptor, RenderPropertyList properties, Object value)
  {
    if (descriptor == null) {
      throw new NullArgumentException("descriptor");
    }
    if (properties == null) {
      throw new NullArgumentException("properties");
    }
    String stringValue = StringUtils.trimToNull(ObjectUtils.toString(value));
    if (stringValue == null) {
      properties.removeRenderProperty(descriptor.getKey());
    } else {
      properties.setRenderProperty(new SimpleRenderProperty(descriptor.getKey(), stringValue));
    }
  }
}
