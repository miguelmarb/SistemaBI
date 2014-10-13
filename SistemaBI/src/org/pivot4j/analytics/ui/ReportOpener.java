package org.pivot4j.analytics.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.configuration.ConfigurationException;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.DataSourceManager;
import org.pivot4j.analytics.repository.DataSourceNotFoundException;
import org.pivot4j.analytics.repository.ReportContent;
import org.pivot4j.analytics.repository.ReportFile;
import org.pivot4j.analytics.repository.ReportRepository;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.analytics.state.ViewStateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name="reportOpener")
@RequestScoped
public class ReportOpener
{
  private Logger log = LoggerFactory.getLogger(getClass());
  @ManagedProperty("#{settings}")
  private Settings settings;
  @ManagedProperty("#{viewStateHolder}")
  private ViewStateHolder viewStateHolder;
  @ManagedProperty("#{dataSourceManager}")
  private DataSourceManager dataSourceManager;
  @ManagedProperty("#{reportRepository}")
  private ReportRepository reportRepository;
  private String fileId;
  private String path;
  private boolean embeded = false;
  
  public void load()
    throws IOException, ClassNotFoundException, ConfigurationException, DataSourceNotFoundException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
    
    ReportFile file = getReportFromRequest(request);
    if (file == null) {
      throw new FacesException("Unable to find requested report file.");
    }
    ViewState state = createViewWithRequest(request, file);
    if (state == null) {
      throw new FacesException("Unable to create a view state.");
    }
    ReportContent content = this.reportRepository.getReportContent(file);
    content.read(state, this.dataSourceManager, this.settings.getConfiguration());
    
    this.viewStateHolder.registerState(state);
    

    NavigationHandler navigationHandler = context.getApplication().getNavigationHandler();
    
    String target = this.embeded ? "embed" : "view";
    
    String path = String.format("%s?faces-redirect=true&%s=%s", new Object[] { target, this.settings
      .getViewParameterName(), state.getId() });
    
    navigationHandler.handleNavigation(context, null, path);
  }
  
  protected ViewState createViewWithRequest(HttpServletRequest request, ReportFile file)
  {
    String viewId = request.getParameter(this.settings.getViewParameterName());
    if (this.log.isInfoEnabled()) {
      this.log.info("Creating a view '{}' with a report: {}", viewId, file);
    }
    ViewState state;
    if (viewId == null)
    {
      state = this.viewStateHolder.createNewState();
      state.setName(file.getName());
    }
    else
    {
      state = new ViewState(viewId, file.getName());
    }
    Map<String, String[]> parameterMap = request.getParameterMap();
    

    Map<String, Object> parameters = new HashMap(parameterMap.size());
    for (String key : parameterMap.keySet())
    {
      String[] values = (String[])parameterMap.get(key);
      if (values != null) {
        if (values.length == 1) {
          parameters.put(key, values[0]);
        } else {
          parameters.put(key, Arrays.asList(values));
        }
      }
    }
    state.setFile(file);
    state.setParameters(parameters);
    state.setReadOnly(this.embeded);
    
    return state;
  }
  
  protected ReportFile getReportFromRequest(HttpServletRequest request)
    throws IOException
  {
    ReportFile file = null;
    if (this.fileId != null)
    {
      if (this.log.isDebugEnabled()) {
        this.log.debug("Opening report file with id: {}", this.fileId);
      }
      file = this.reportRepository.getFileById(this.fileId);
    }
    else if (this.path != null)
    {
      if (this.log.isDebugEnabled()) {
        this.log.debug("Opening report file with path: {}", this.path);
      }
      file = this.reportRepository.getFile(this.path);
    }
    return file;
  }
  
  public String getFileId()
  {
    return this.fileId;
  }
  
  public void setFileId(String fileId)
  {
    this.fileId = fileId;
  }
  
  public String getPath()
  {
    return this.path;
  }
  
  public void setPath(String path)
  {
    this.path = path;
  }
  
  public boolean isEmbeded()
  {
    return this.embeded;
  }
  
  public void setEmbeded(boolean embeded)
  {
    this.embeded = embeded;
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
  
  public DataSourceManager getDataSourceManager()
  {
    return this.dataSourceManager;
  }
  
  public void setDataSourceManager(DataSourceManager dataSourceManager)
  {
    this.dataSourceManager = dataSourceManager;
  }
  
  public ReportRepository getReportRepository()
  {
    return this.reportRepository;
  }
  
  public void setReportRepository(ReportRepository reportRepository)
  {
    this.reportRepository = reportRepository;
  }
}
