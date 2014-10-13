package org.pivot4j.analytics.ui;

import java.io.Serializable;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.analytics.state.ViewStateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name="pivotStateManager")
@ViewScoped
public class PivotStateManager
  implements Serializable
{
  private static final long serialVersionUID = -146698046524588064L;
  private Logger log = LoggerFactory.getLogger(getClass());
  @ManagedProperty("#{settings}")
  private Settings settings;
  @ManagedProperty("#{viewStateHolder}")
  private ViewStateHolder viewStateHolder;
  private String viewId;
  
  @PostConstruct
  protected void initialize()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    ExternalContext externalContext = context.getExternalContext();
    Flash flash = externalContext.getFlash();
    

    Map<String, String> parameters = externalContext.getRequestParameterMap();
    
    this.viewId = ((String)parameters.get(this.settings.getViewParameterName()));
    if (this.viewId == null) {
      this.viewId = ((String)flash.get("viewId"));
    }
    ViewState state = null;
    if (this.viewId != null) {
      state = this.viewStateHolder.getState(this.viewId);
    }
    if (state == null)
    {
      ProjectStage stage = context.getApplication().getProjectStage();
      if (stage == ProjectStage.UnitTest)
      {
        state = this.viewStateHolder.createNewState();
        this.viewStateHolder.registerState(state);
        
        this.viewId = state.getId();
      }
      else
      {
        throw new FacesException("No view state data is available : " + this.viewId);
      }
    }
    if (this.log.isInfoEnabled()) {
      this.log.info("Using an existing view state : {}", this.viewId);
    }
  }
  
  @PreDestroy
  public void destroy()
  {
    this.viewStateHolder.unregisterState(this.viewId);
  }
  
  public String getViewId()
  {
    return this.viewId;
  }
  
  public ViewState getState()
  {
    return this.viewStateHolder.getState(this.viewId);
  }
  
  public PivotModel getModel()
  {
    ViewState state = getState();
    if (state == null) {
      return null;
    }
    return state.getModel();
  }
  
  public boolean isReadOnly()
  {
    ViewState state = getState();
    if (state == null) {
      return true;
    }
    return state.isReadOnly();
  }
  
  public boolean isDirty()
  {
    ViewState state = getState();
    if (state == null) {
      return false;
    }
    return state.isDirty();
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
  
  public Serializable getRendererState()
  {
    ViewState state = getState();
    if (state == null) {
      return null;
    }
    return state.getRendererState();
  }
  
  public void setRendererState(Serializable rendererState)
  {
    ViewState state = getState();
    if (state == null) {
      return;
    }
    state.setRendererState(rendererState);
  }
  
  public Serializable getChartState()
  {
    ViewState state = getState();
    if (state == null) {
      return null;
    }
    return state.getChartState();
  }
  
  public void setChartState(Serializable chartState)
  {
    ViewState state = getState();
    if (state == null) {
      return;
    }
    state.setChartState(chartState);
  }
  
  public ConnectionInfo getConnectionInfo()
  {
    ViewState state = getState();
    if (state == null) {
      return null;
    }
    return state.getConnectionInfo();
  }
  
  public void keepAlive()
  {
    this.viewStateHolder.keepAlive(this.viewId);
  }
}
