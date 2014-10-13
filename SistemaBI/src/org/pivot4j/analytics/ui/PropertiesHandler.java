package org.pivot4j.analytics.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.pivot4j.analytics.property.PropertyCategory;
import org.pivot4j.analytics.property.PropertyDescriptor;
import org.pivot4j.analytics.property.PropertyDescriptorFactory;
import org.pivot4j.analytics.property.PropertyEditor;
import org.pivot4j.ui.property.RenderProperty;
import org.pivot4j.ui.property.RenderPropertyList;
import org.pivot4j.ui.property.SimpleRenderProperty;
import org.pivot4j.ui.table.TableRenderer;
import org.primefaces.component.menuitem.UIMenuItem;
import org.primefaces.component.panelmenu.PanelMenu;
import org.primefaces.component.submenu.UISubmenu;
import org.primefaces.extensions.event.CompleteEvent;

@ManagedBean(name="propertiesHandler")
@RequestScoped
public class PropertiesHandler
{
  @ManagedProperty("#{propertyDescriptorFactory}")
  private PropertyDescriptorFactory descriptorFactory;
  @ManagedProperty("#{viewHandler}")
  private ViewHandler viewHandler;
  private ResourceBundle bundle;
  private PropertyDescriptor descriptor;
  private PanelMenu menu;
  private UIComponent editorPanel;
  private Object value;
  private String expression;
  
  @PostConstruct
  protected void initialize()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    this.bundle = context.getApplication().getResourceBundle(context, "msg");
    
    String key = getKey();
    PropertyCategory category = getCategory();
    if ((key != null) && (category != null)) {
      this.descriptor = this.descriptorFactory.getDescriptor(category, key);
    }
  }
  
  protected ResourceBundle getBundle()
  {
    return this.bundle;
  }
  
  public String getName()
  {
    if (this.descriptor == null) {
      return null;
    }
    FacesContext context = FacesContext.getCurrentInstance();
    return this.descriptor.getName(context);
  }
  
  public String getDescription()
  {
    if (this.descriptor == null) {
      return null;
    }
    FacesContext context = FacesContext.getCurrentInstance();
    return this.descriptor.getDescription(context);
  }
  
  public String getCategoryName()
  {
    if (this.descriptor == null) {
      return null;
    }
    return this.bundle.getString("properties.category." + this.descriptor
      .getCategory().name());
  }
  
  public void selectProperty()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
    
    PropertyCategory category = PropertyCategory.valueOf(
      (String)parameters.get("category"));
    String key = (String)parameters.get("key");
    
    setKey(key);
    setCategory(category);
    
    this.descriptor = this.descriptorFactory.getDescriptor(category, key);
    
    RenderPropertyList properties = getProperties(this.descriptor.getCategory());
    

    SimpleRenderProperty property = (SimpleRenderProperty)properties.getRenderProperty(this.descriptor.getKey());
    if (property == null) {
      this.expression = null;
    } else {
      this.expression = property.getValue();
    }
    if ((this.expression != null) && (
      (this.expression.contains("${")) || (this.expression.contains("<#")))) {
      setUseExpression(true);
    } else {
      setUseExpression(false);
    }
    this.editorPanel.getChildren().clear();
    
    PropertyEditor editor = this.descriptor.getEditor();
    if (editor == null)
    {
      this.value = property.getValue();
    }
    else
    {
      Application application = context.getApplication();
      
      ExpressionFactory factory = application.getExpressionFactory();
      
      ValueExpression exp = factory.createValueExpression(context
        .getELContext(), "#{propertiesHandler.value}", Object.class);
      

      MethodExpression listener = factory.createMethodExpression(context
        .getELContext(), "#{propertiesHandler.onPropertyChange}", Void.TYPE, new Class[0]);
      


      editor.createComponent(this.descriptor, this.editorPanel, exp, listener, "button-bar");
      

      this.value = editor.getValue(this.descriptor, properties);
    }
    setDirty(false);
  }
  
  public void onPropertyChange()
  {
    setDirty(true);
  }
  
  public void onEditorModeChange()
  {
    RenderPropertyList properties = getProperties(getCategory());
    
    SimpleRenderProperty property = (SimpleRenderProperty)properties.getRenderProperty(getKey());
    if (getUseExpression())
    {
      if (this.expression == null) {
        if (property == null) {
          this.expression = null;
        } else {
          this.expression = property.getValue();
        }
      }
    }
    else
    {
      PropertyEditor editor = this.descriptor.getEditor();
      if (editor == null) {
        this.value = null;
      } else {
        this.value = editor.getValue(this.descriptor, properties);
      }
    }
  }
  
  protected RenderPropertyList getProperties(PropertyCategory category)
  {
    TableRenderer renderer = this.viewHandler.getRenderer();
    
    RenderPropertyList properties = null;
    
    switch (category) {
    		case Header:
    			properties = renderer.getRenderProperties().get("header");
    			break;
    		case Cell:
    			properties = renderer.getRenderProperties().get("cell");
    			break;
    		default:
    			assert false;
    		}
    
    return properties;
  }
  
  public List<String> complete(CompleteEvent event)
  {
    List<String> suggestions = new LinkedList();
    
    suggestions.add("context: " + event.getContext());
    suggestions.add("token: " + event.getToken());
    
    return suggestions;
  }
  
  public void apply()
  {
    RenderPropertyList properties = getProperties(this.descriptor.getCategory());
    if (getUseExpression())
    {
      if (this.expression == null)
      {
        properties.removeRenderProperty(this.descriptor.getKey());
      }
      else
      {
        SimpleRenderProperty property = new SimpleRenderProperty(this.descriptor.getKey(), this.expression);
        properties.setRenderProperty(property);
      }
    }
    else
    {
      PropertyEditor editor = this.descriptor.getEditor();
      editor.setValue(this.descriptor, properties, this.value);
    }
    setDirty(false);
    
    this.viewHandler.render();
  }
  
  public PanelMenu getMenu()
  {
    return this.menu;
  }
  
  public void setMenu(PanelMenu menu)
  {
    List<UIComponent> children = menu.getChildren();
    
    children.clear();
    children.add(createSubMenu(PropertyCategory.Header));
    children.add(createSubMenu(PropertyCategory.Cell));
    
    this.menu = menu;
  }
  
  protected UISubmenu createSubMenu(PropertyCategory category)
  {
    String postfix = category.name().toLowerCase();
    
    UISubmenu categoryMenu = new UISubmenu();
    categoryMenu.setId("menu-" + postfix);
    categoryMenu.setLabel(this.bundle.getString("properties.category." + category
      .name()));
    
    UISubmenu colorMenu = new UISubmenu();
    colorMenu.setId("menu-color-" + postfix);
    colorMenu.setLabel(this.bundle.getString("properties.category.color"));
    colorMenu.setIcon("ui-icon-image");
    colorMenu.getChildren().add(createMenuItem(category, "fgColor"));
    colorMenu.getChildren().add(createMenuItem(category, "bgColor"));
    
    categoryMenu.getChildren().add(colorMenu);
    
    UISubmenu fontMenu = new UISubmenu();
    fontMenu.setId("menu-font-" + postfix);
    fontMenu.setLabel(this.bundle.getString("properties.category.font"));
    fontMenu.setIcon("ui-icon-pencil");
    fontMenu.getChildren().add(createMenuItem(category, "fontFamily"));
    fontMenu.getChildren().add(createMenuItem(category, "fontSize"));
    fontMenu.getChildren().add(createMenuItem(category, "fontStyle"));
    
    categoryMenu.getChildren().add(fontMenu);
    
    categoryMenu.getChildren().add(createMenuItem(category, "label"));
    categoryMenu.getChildren().add(createMenuItem(category, "link"));
    categoryMenu.getChildren().add(createMenuItem(category, "styleClass"));
    
    return categoryMenu;
  }
  
  protected UIMenuItem createMenuItem(PropertyCategory category, String key)
  {
    PropertyDescriptor property = this.descriptorFactory.getDescriptor(category, key);
    

    FacesContext context = FacesContext.getCurrentInstance();
    
    Application application = context.getApplication();
    ExpressionFactory factory = application.getExpressionFactory();
    
    UIMenuItem item = new UIMenuItem();
    item.setId("mi-" + key.toLowerCase() + "-" + category
      .name().toLowerCase());
    item.setValue(property.getName(context));
    item.setTitle(property.getDescription(context));
    item.setIcon(property.getIcon());
    if ((category.equals(getCategory())) && (key.equals(getKey()))) {
      item.setStyleClass("ui-state-highlight");
    }
    MethodExpression exp = factory.createMethodExpression(context
      .getELContext(), "#{propertiesHandler.selectProperty}", Void.TYPE, new Class[0]);
    
    item.setActionExpression(exp);
    item.setUpdate("content,button-bar,:growl");
    item.setOnclick("jQuery('.ui-menuitem-link').removeClass('ui-state-highlight'); jQuery(this).addClass('ui-state-highlight');");
    
    item.setOncomplete("applyThemeToCMEditor('.properties-config .CodeMirror')");
    
    UIParameter keyParam = new UIParameter();
    keyParam.setName("key");
    keyParam.setValue(key);
    
    item.getChildren().add(keyParam);
    
    UIParameter categoryParam = new UIParameter();
    categoryParam.setName("category");
    categoryParam.setValue(category.name());
    
    item.getChildren().add(categoryParam);
    
    return item;
  }
  
  public String getKey()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    Map<String, Object> attributes = context.getViewRoot().getAttributes();
    
    return (String)attributes.get("propertyKey");
  }
  
  public void setKey(String key)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    Map<String, Object> attributes = context.getViewRoot().getAttributes();
    attributes.put("propertyKey", key);
  }
  
  public PropertyCategory getCategory()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    Map<String, Object> attributes = context.getViewRoot().getAttributes();
    
    return (PropertyCategory)attributes.get("propertyCategory");
  }
  
  public void setCategory(PropertyCategory category)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    Map<String, Object> attributes = context.getViewRoot().getAttributes();
    attributes.put("propertyCategory", category);
  }
  
  public boolean isDirty()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    Map<String, Object> attributes = context.getViewRoot().getAttributes();
    
    Object attribute = attributes.get("propertyChanged");
    
    return (attribute != null) && (((Boolean)attribute).booleanValue());
  }
  
  public void setDirty(boolean dirty)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    Map<String, Object> attributes = context.getViewRoot().getAttributes();
    attributes.put("propertyChanged", Boolean.valueOf(dirty));
  }
  
  public boolean isSet()
  {
    boolean isSet = false;
    if (this.descriptor != null)
    {
      RenderProperty property = getProperties(getCategory()).getRenderProperty(getKey());
      isSet = property != null;
    }
    return isSet;
  }
  
  public boolean getUseExpression()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    Map<String, Object> attributes = context.getViewRoot().getAttributes();
    
    Object attribute = attributes.get("useExpression");
    
    return (attribute != null) && (((Boolean)attribute).booleanValue());
  }
  
  public void setUseExpression(boolean useExpression)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    Map<String, Object> attributes = context.getViewRoot().getAttributes();
    attributes.put("useExpression", Boolean.valueOf(useExpression));
  }
  
  public void reset()
  {
    this.value = null;
    this.expression = null;
    
    setDirty(true);
    setUseExpression(false);
  }
  
  public PropertyDescriptor getDescriptor()
  {
    return this.descriptor;
  }
  
  public PropertyDescriptorFactory getDescriptorFactory()
  {
    return this.descriptorFactory;
  }
  
  public void setDescriptorFactory(PropertyDescriptorFactory descriptorFactory)
  {
    this.descriptorFactory = descriptorFactory;
  }
  
  public ViewHandler getViewHandler()
  {
    return this.viewHandler;
  }
  
  public void setViewHandler(ViewHandler viewHandler)
  {
    this.viewHandler = viewHandler;
  }
  
  public UIComponent getEditorPanel()
  {
    return this.editorPanel;
  }
  
  public void setEditorPanel(UIComponent editorPanel)
  {
    this.editorPanel = editorPanel;
  }
  
  public Object getValue()
  {
    return this.value;
  }
  
  public void setValue(Object value)
  {
    this.value = value;
  }
  
  public String getExpression()
  {
    return this.expression;
  }
  
  public void setExpression(String expression)
  {
    this.expression = expression;
  }
}
