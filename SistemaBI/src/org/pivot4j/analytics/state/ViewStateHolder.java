package org.pivot4j.analytics.state;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapDataSource;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.CatalogInfo;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.datasource.DataSourceManager;
import org.pivot4j.impl.PivotModelImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name="viewStateHolder")
@SessionScoped
public class ViewStateHolder
  implements Serializable
{
  private static final long serialVersionUID = -7947800606762703855L;
  private static final long MINUTE = 60L;
  private Logger log = LoggerFactory.getLogger(getClass());
  @ManagedProperty("#{settings}")
  private Settings settings;
  @ManagedProperty("#{dataSourceManager}")
  private DataSourceManager dataSourceManager;
  private Map<String, ViewState> states = new LinkedHashMap();
  private List<ViewStateListener> viewStateListeners = new ArrayList();
  private Timer timer;
  private long checkInterval = 60L;
  private long keepAliveInterval = 60L;
  private long expires = 300L;
  private String sessionId;
  
  @PostConstruct
  protected void initialize()
  {
    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
    HttpSession session = (HttpSession)context.getSession(true);
    this.sessionId = session.getId();
    if (this.log.isInfoEnabled())
    {
      this.log.info("Initializing view state holder for session : " + this.sessionId);
      
      this.log.info(String.format("Check interval : %d secs.", new Object[] { Long.valueOf(this.checkInterval) }));
      this.log.info(String.format("Keep alive interval : %d secs.", new Object[] {
        Long.valueOf(this.keepAliveInterval) }));
      this.log.info(String.format("Expires : %d secs.", new Object[] { Long.valueOf(this.expires) }));
    }
    this.timer = new Timer();
    
    this.timer.scheduleAtFixedRate(new TimerTask()
    {
      public void run()
      {
        ViewStateHolder.this.checkAbandonedModels();
      }
    }, 60000L, this.checkInterval * 1000L);
  }
  
  @PreDestroy
  protected void destroy()
  {
    if (this.log.isInfoEnabled()) {
      this.log.info("Destroying view state holder for session : {}", this.sessionId);
    }
    this.timer.cancel();
    this.timer.purge();
    
    this.viewStateListeners.clear();
    
    clearStates();
  }
  
  protected synchronized void checkAbandonedModels()
  {
    if (this.log.isDebugEnabled())
    {
      this.log.debug("Checking for abandoned view states for session : " + this.sessionId);
      
      this.log.debug("Current view state count for session : {}", 
        Integer.valueOf(this.states.size()));
    }
    Set<String> keys = new HashSet(this.states.keySet());
    
    Date now = new Date();
    for (String key : keys)
    {
      ViewState state = (ViewState)this.states.get(key);
      
      long elapsed = now.getTime() - state.getLastActive().getTime();
      if (this.expires * 1000L <= elapsed)
      {
        if (this.log.isInfoEnabled()) {
          this.log.info("Found an abandoned view sate : {}", state);
        }
        unregisterState(state);
      }
    }
  }
  
  public synchronized void keepAlive(String id)
  {
    ViewState state = getState(id);
    if (this.log.isDebugEnabled()) {
      this.log.debug("Received a keep alive request : {}", state);
    }
    if (state != null) {
      state.update();
    }
  }
  
  public ViewState getState(String id)
  {
    return (ViewState)this.states.get(id);
  }
  
  public synchronized List<ViewState> getStates()
  {
    List<ViewState> list = new ArrayList(this.states.size());
    for (String id : this.states.keySet()) {
      list.add(this.states.get(id));
    }
    return list;
  }
  
  public synchronized void registerState(ViewState state)
  {
    if (state == null) {
      throw new IllegalArgumentException("Required argument 'state' is null.");
    }
    ViewState oldState = (ViewState)this.states.get(state.getId());
    if (oldState != null)
    {
      if (oldState == state) {
        return;
      }
      unregisterState(oldState);
    }
    this.states.put(state.getId(), state);
    
    fireViewRegistered(state);
    if (this.log.isInfoEnabled())
    {
      this.log.info("View state is registered : {}", state);
      this.log.info("Current view state count for session : {}", Integer.valueOf(this.states.size()));
    }
  }
  
  public synchronized void unregisterState(String id)
  {
    if (id == null) {
      throw new IllegalArgumentException("Required argument 'id' is null.");
    }
    ViewState state = (ViewState)this.states.get(id);
    if (state != null) {
      unregisterState(state);
    }
  }
  
  protected synchronized void unregisterState(ViewState state)
  {
    PivotModel model = state.getModel();
    if ((model != null) && (model.isInitialized())) {
      model.destroy();
    }
    this.states.remove(state.getId());
    
    fireViewUnregistered(state);
    if (this.log.isInfoEnabled())
    {
      this.log.info("View state is unregistered : {}", state);
      this.log.info("Current view state count for session : {}", Integer.valueOf(this.states.size()));
    }
  }
  
  protected synchronized void clearStates()
  {
    for (ViewState state : this.states.values()) {
      unregisterState(state);
    }
  }
  
  public ViewState createNewState()
  {
    List<CatalogInfo> catalogs = this.dataSourceManager.getCatalogs();
    
    ConnectionInfo connectionInfo = null;
    if (catalogs.size() == 1) {
      connectionInfo = new ConnectionInfo(((CatalogInfo)catalogs.get(0)).getName(), null);
    }
    return createNewState(connectionInfo, null);
  }
  
  public ViewState createNewState(ConnectionInfo connectionInfo, String viewId)
  {
    String id;
    if (viewId == null) {
      id = UUID.randomUUID().toString();
    } else {
      id = viewId;
    }
    FacesContext context = FacesContext.getCurrentInstance();
    
    ResourceBundle messages = context.getApplication().getResourceBundle(context, "msg");
    

    MessageFormat mf = new MessageFormat(messages.getString("label.untitled"));
    
    List<ViewState> stateList = getStates();
    
    Set<String> names = new HashSet(stateList.size());
    for (ViewState state : stateList) {
      names.add(state.getName());
    }
    int count = 1;
    
    String name = null;
    while (name == null)
    {
      name = mf.format(new Object[] { Integer.valueOf(count) });
      if (names.contains(name))
      {
        name = null;
        count++;
      }
    }
    PivotModel model = null;
    if (connectionInfo != null)
    {
      OlapDataSource dataSource = this.dataSourceManager.getDataSource(connectionInfo);
      
      model = new PivotModelImpl(dataSource);
      

      HierarchicalConfiguration configuration = this.settings.getConfiguration();
      try
      {
        model.restoreSettings(configuration.configurationAt("model"));
      }
      catch (IllegalArgumentException e) {}
    }
    return new ViewState(id, name, connectionInfo, model, null);
  }
  
  protected void fireViewRegistered(ViewState state)
  {
    ViewStateEvent e = new ViewStateEvent(this, state);
    
    List<ViewStateListener> copiedListeners = new ArrayList(this.viewStateListeners);
    for (ViewStateListener listener : copiedListeners) {
      listener.viewRegistered(e);
    }
  }
  
  protected void fireViewUnregistered(ViewState state)
  {
    ViewStateEvent e = new ViewStateEvent(this, state);
    
    List<ViewStateListener> copiedListeners = new ArrayList(this.viewStateListeners);
    for (ViewStateListener listener : copiedListeners) {
      listener.viewUnregistered(e);
    }
  }
  
  public void addViewStateListener(ViewStateListener listener)
  {
    if (listener == null) {
      throw new NullArgumentException("listener");
    }
    this.viewStateListeners.add(listener);
  }
  
  public void removeViewStateListener(ViewStateListener listener)
  {
    if (listener == null) {
      throw new NullArgumentException("listener");
    }
    this.viewStateListeners.remove(listener);
  }
  
  public Settings getSettings()
  {
    return this.settings;
  }
  
  public void setSettings(Settings settings)
  {
    this.settings = settings;
  }
  
  public DataSourceManager getDataSourceManager()
  {
    return this.dataSourceManager;
  }
  
  public void setDataSourceManager(DataSourceManager dataSourceManager)
  {
    this.dataSourceManager = dataSourceManager;
  }
  
  public long getCheckInterval()
  {
    return this.checkInterval;
  }
  
  public void setCheckInterval(long checkInterval)
  {
    this.checkInterval = checkInterval;
  }
  
  public long getKeepAliveInterval()
  {
    return this.keepAliveInterval;
  }
  
  public void setKeepAliveInterval(long keepAliveInterval)
  {
    this.keepAliveInterval = keepAliveInterval;
  }
  
  public long getExpires()
  {
    return this.expires;
  }
  
  public void setExpires(long expires)
  {
    this.expires = expires;
  }
}
