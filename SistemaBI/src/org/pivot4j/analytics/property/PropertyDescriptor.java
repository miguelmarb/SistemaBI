package org.pivot4j.analytics.property;

import java.util.ResourceBundle;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import org.apache.commons.lang.NullArgumentException;

public class PropertyDescriptor
{
  private String key;
  private PropertyCategory category;
  private String icon;
  private PropertyEditor editor;
  
  public PropertyDescriptor(PropertyCategory category, String key, String icon, PropertyEditor editor)
  {
    if (category == null) {
      throw new NullArgumentException("category");
    }
    if (key == null) {
      throw new NullArgumentException("key");
    }
    if (editor == null) {
      editor = new StringPropertyEditor();
    }
    this.category = category;
    this.key = key;
    this.icon = icon;
    this.editor = editor;
  }
  
  public String getKey()
  {
    return this.key;
  }
  
  public PropertyCategory getCategory()
  {
    return this.category;
  }
  
  public PropertyEditor getEditor()
  {
    return this.editor;
  }
  
  public String getIcon()
  {
    return this.icon;
  }
  
  public void setIcon(String icon)
  {
    this.icon = icon;
  }
  
  public String getName(FacesContext context)
  {
    if (context == null) {
      throw new NullArgumentException("context");
    }
    ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
    
    return bundle.getString("properties." + this.key);
  }
  
  public String getDescription(FacesContext context)
  {
    if (context == null) {
      throw new NullArgumentException("context");
    }
    ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
    
    return bundle.getString("properties." + this.key + ".description");
  }
}
