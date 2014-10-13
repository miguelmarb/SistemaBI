package org.pivot4j.analytics.ui;

import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.state.ViewState;
import org.primefaces.extensions.model.layout.LayoutOptions;

@ManagedBean(name="workbenchHandler")
@RequestScoped
public class WorkbenchHandler
{
  @ManagedProperty("#{settings}")
  private Settings settings;
  @ManagedProperty("#{repositoryHandler}")
  private RepositoryHandler repositoryHandler;
  private LayoutOptions layoutOptions;
  
  public LayoutOptions getLayoutOptions()
  {
    if (this.layoutOptions == null)
    {
      this.layoutOptions = new LayoutOptions();
      this.layoutOptions.addOption("enableCursorHotkey", Boolean.valueOf(false));
      
      LayoutOptions toolbarOptions = new LayoutOptions();
      toolbarOptions.addOption("resizable", Boolean.valueOf(false));
      toolbarOptions.addOption("resizable", Boolean.valueOf(false));
      toolbarOptions.addOption("closable", Boolean.valueOf(false));
      
      this.layoutOptions.setNorthOptions(toolbarOptions);
      
      LayoutOptions navigatorOptions = new LayoutOptions();
      navigatorOptions.addOption("resizable", Boolean.valueOf(true));
      navigatorOptions.addOption("closable", Boolean.valueOf(true));
      navigatorOptions.addOption("slidable", Boolean.valueOf(true));
      navigatorOptions.addOption("size", Integer.valueOf(200));
      
      this.layoutOptions.setWestOptions(navigatorOptions);
      
      LayoutOptions contentOptions = new LayoutOptions();
      contentOptions.addOption("contentSelector", "#tab-panel");
      contentOptions.addOption("maskIframesOnResize", Boolean.valueOf(true));
      
      this.layoutOptions.setCenterOptions(contentOptions);
    }
    return this.layoutOptions;
  }
  
  public String getTheme()
  {
    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    String theme = (String)context.getSessionMap().get("ui-theme");
    if (theme == null) {
      theme = this.settings.getTheme();
    }
    return theme;
  }
  
  public void setTheme(String theme)
  {
    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    if (theme == null) {
      context.getSessionMap().remove("ui-theme");
    } else {
      context.getSessionMap().put("ui-theme", theme);
    }
  }
  
  public boolean isDeleteEnabled()
  {
    ViewState viewState = this.repositoryHandler.getActiveView();
    return (viewState != null) && (viewState.getFile() != null);
  }
  
  public boolean isSaveEnabled()
  {
    ViewState viewState = this.repositoryHandler.getActiveView();
    return (viewState != null) && (viewState.isDirty());
  }
  
  public boolean isViewActive()
  {
    ViewState viewState = this.repositoryHandler.getActiveView();
    return viewState != null;
  }
  
  public boolean isViewValid()
  {
    ViewState viewState = this.repositoryHandler.getActiveView();
    if (viewState == null) {
      return false;
    }
    PivotModel model = viewState.getModel();
    
    return (model != null) && (model.isInitialized());
  }
  
  public Settings getSettings()
  {
    return this.settings;
  }
  
  public void setSettings(Settings settings)
  {
    this.settings = settings;
  }
  
  public RepositoryHandler getRepositoryHandler()
  {
    return this.repositoryHandler;
  }
  
  public void setRepositoryHandler(RepositoryHandler repositoryHandler)
  {
    this.repositoryHandler = repositoryHandler;
  }
}
