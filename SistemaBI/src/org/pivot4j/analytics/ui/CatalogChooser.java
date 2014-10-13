package org.pivot4j.analytics.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UISelectItem;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import org.olap4j.OlapDataSource;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.CatalogInfo;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.datasource.CubeInfo;
import org.pivot4j.analytics.datasource.DataSourceManager;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.analytics.state.ViewStateHolder;
import org.pivot4j.impl.PivotModelImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name="catalogChooser")
@ViewScoped
public class CatalogChooser
  implements Serializable
{
  private static final long serialVersionUID = 9032548845357820921L;
  @ManagedProperty("#{settings}")
  private Settings settings;
  @ManagedProperty("#{dataSourceManager}")
  private DataSourceManager dataSourceManager;
  @ManagedProperty("#{viewStateHolder}")
  private ViewStateHolder viewStateHolder;
  private List<UISelectItem> catalogItems;
  private List<UISelectItem> cubeItems;
  private String catalogName;
  private String cubeName;
  private String viewId;
  private boolean editable;
  
  public List<UISelectItem> getCatalogs()
  {
    if (this.catalogItems == null)
    {
      FacesContext context = FacesContext.getCurrentInstance();
      
      ResourceBundle messages = context.getApplication().getResourceBundle(context, "msg");
      try
      {
        List<CatalogInfo> catalogs = this.dataSourceManager.getCatalogs();
        
        this.catalogItems = new ArrayList(catalogs.size());
        
        UISelectItem defaultItem = new UISelectItem();
        defaultItem.setItemLabel(messages
          .getString("message.catalog.chooser.default"));
        defaultItem.setItemValue("");
        
        this.catalogItems.add(defaultItem);
        for (CatalogInfo catalog : catalogs)
        {
          UISelectItem item = new UISelectItem();
          
          item.setItemValue(catalog.getName());
          item.setItemLabel(catalog.getLabel());
          item.setItemDescription(catalog.getDescription());
          
          this.catalogItems.add(item);
        }
      }
      catch (Exception e)
      {
        String title = messages.getString("error.catalogList.title");
        String msg = e.getMessage();
        
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, msg));
        

        Logger log = LoggerFactory.getLogger(getClass());
        if (log.isErrorEnabled()) {
          log.error(msg, e);
        }
      }
    }
    return this.catalogItems;
  }
  
  public List<UISelectItem> getCubes()
  {
    if (this.cubeItems == null)
    {
      FacesContext context = FacesContext.getCurrentInstance();
      
      ResourceBundle messages = context.getApplication().getResourceBundle(context, "msg");
      
      this.cubeItems = new ArrayList();
      
      UISelectItem defaultItem = new UISelectItem();
      defaultItem.setItemLabel(messages
        .getString("message.cubeList.default"));
      defaultItem.setItemValue("");
      
      this.cubeItems.add(defaultItem);
      if (this.catalogName != null) {
        try
        {
          List<CubeInfo> cubes = this.dataSourceManager.getCubes(this.catalogName);
          for (CubeInfo cube : cubes)
          {
            UISelectItem item = new UISelectItem();
            
            item.setItemValue(cube.getName());
            item.setItemLabel(cube.getLabel());
            item.setItemDescription(cube.getDescription());
            
            this.cubeItems.add(item);
          }
        }
        catch (Exception e)
        {
          ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
          
          String title = bundle.getString("error.cubeList.title");
          String msg = e.getMessage();
          
          context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, msg));
          

          Logger log = LoggerFactory.getLogger(getClass());
          if (log.isErrorEnabled()) {
            log.error(msg, e);
          }
        }
      }
    }
    return this.cubeItems;
  }
  
  public void onCatalogChanged()
  {
    this.cubeItems = null;
    if (getCubes().size() > 1) {
      this.cubeName = ((String)((UISelectItem)getCubes().get(1)).getItemValue());
    } else {
      this.cubeName = null;
    }
  }
  
  public boolean isNewReport()
  {
    if (this.viewId == null) {
      return true;
    }
    ViewState state = this.viewStateHolder.getState(this.viewId);
    
    return (state == null) || (state.getConnectionInfo() == null);
  }
  
  public String proceed()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Flash flash = context.getExternalContext().getFlash();
    
    ConnectionInfo connectionInfo = new ConnectionInfo(this.catalogName, this.cubeName);
    

    ViewState state = this.viewStateHolder.getState(this.viewId);
    if (state == null)
    {
      state = this.viewStateHolder.createNewState(connectionInfo, this.viewId);
      this.viewStateHolder.registerState(state);
    }
    else
    {
      OlapDataSource dataSource = this.dataSourceManager.getDataSource(connectionInfo);
      state.setModel(new PivotModelImpl(dataSource));
      state.setConnectionInfo(connectionInfo);
    }
    flash.put("connectionInfo", connectionInfo);
    flash.put("viewId", this.viewId);
    
    StringBuilder builder = new StringBuilder();
    builder.append("view");
    builder.append("?faces-redirect=true");
    builder.append("&");
    builder.append(this.settings.getViewParameterName());
    builder.append("=");
    builder.append(this.viewId);
    
    return builder.toString();
  }
  
  public DataSourceManager getDataSourceManager()
  {
    return this.dataSourceManager;
  }
  
  public void setDataSourceManager(DataSourceManager dataSourceManager)
  {
    this.dataSourceManager = dataSourceManager;
  }
  
  public Settings getSettings()
  {
    return this.settings;
  }
  
  public void setSettings(Settings settings)
  {
    this.settings = settings;
  }
  
  public ViewStateHolder getViewStateHolder()
  {
    return this.viewStateHolder;
  }
  
  public void setViewStateHolder(ViewStateHolder viewStateHolder)
  {
    this.viewStateHolder = viewStateHolder;
  }
  
  public String getCatalogName()
  {
    return this.catalogName;
  }
  
  public void setCatalogName(String catalogName)
  {
    this.catalogName = catalogName;
  }
  
  public String getCubeName()
  {
    return this.cubeName;
  }
  
  public void setCubeName(String cubeName)
  {
    this.cubeName = cubeName;
  }
  
  public String getViewId()
  {
    return this.viewId;
  }
  
  public void setViewId(String viewId)
  {
    this.viewId = viewId;
  }
  
  public boolean isEditable()
  {
    return this.editable;
  }
  
  public void setEditable(boolean editable)
  {
    this.editable = editable;
  }
}
