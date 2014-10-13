package org.pivot4j.analytics.property;

import java.util.List;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.context.FacesContext;
import org.primefaces.component.selectonemenu.SelectOneMenu;

public class SelectStringPropertyEditor
  extends AbstractPropertyInputEditor
{
  private List<UISelectItem> items;
  
  public SelectStringPropertyEditor() {}
  
  public SelectStringPropertyEditor(List<UISelectItem> items)
  {
    this.items = items;
  }
  
  public List<UISelectItem> getItems()
  {
    return this.items;
  }
  
  public void setItems(List<UISelectItem> items)
  {
    this.items = items;
  }
  
  protected UIInput createInput(PropertyDescriptor descriptor, UIComponent parent, FacesContext context)
  {
    Application application = FacesContext.getCurrentInstance().getApplication();
    
    SelectOneMenu select = (SelectOneMenu)application.createComponent("org.primefaces.component.SelectOneMenu");
    if (this.items != null) {
      for (UISelectItem item : this.items) {
        select.getChildren().add(item);
      }
    }
    return select;
  }
}
