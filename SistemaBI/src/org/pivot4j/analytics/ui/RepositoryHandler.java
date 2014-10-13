package org.pivot4j.analytics.ui;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.commons.configuration.ConfigurationException;
import org.pivot4j.analytics.component.tree.DefaultTreeNode;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.datasource.DataSourceManager;
import org.pivot4j.analytics.repository.DataSourceNotFoundException;
import org.pivot4j.analytics.repository.ReportContent;
import org.pivot4j.analytics.repository.ReportFile;
import org.pivot4j.analytics.repository.ReportRepository;
import org.pivot4j.analytics.repository.RepositoryFileComparator;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.analytics.state.ViewStateEvent;
import org.pivot4j.analytics.state.ViewStateHolder;
import org.pivot4j.analytics.state.ViewStateListener;
import org.pivot4j.analytics.ui.navigator.RepositoryNode;
import org.primefaces.context.RequestContext;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name="repositoryHandler")
@SessionScoped
public class RepositoryHandler
  implements ViewStateListener, Serializable
{
  private static final long serialVersionUID = -860723075484210684L;
  private Logger log;
  @ManagedProperty("#{settings}")
  private Settings settings;
  @ManagedProperty("#{dataSourceManager}")
  private DataSourceManager dataSourceManager;
  @ManagedProperty("#{reportRepository}")
  private ReportRepository repository;
  @ManagedProperty("#{viewStateHolder}")
  private ViewStateHolder viewStateHolder;
  private TreeNode rootNode;
  private TreeNode selection;
  private String activeViewId;
  private String reportName;
  private String folderName;
  
  public RepositoryHandler()
  {
    this.log = LoggerFactory.getLogger(getClass());
  }
  
  @PostConstruct
  protected void initialize()
  {
    this.viewStateHolder.addViewStateListener(this);
    
    ViewState state = this.viewStateHolder.createNewState();
    if (state != null)
    {
      this.viewStateHolder.registerState(state);
      
      this.activeViewId = state.getId();
    }
  }
  
  @PreDestroy
  protected void destroy()
  {
    this.viewStateHolder.removeViewStateListener(this);
  }
  
  public ReportRepository getRepository()
  {
    return this.repository;
  }
  
  public void setRepository(ReportRepository repository)
  {
    this.repository = repository;
  }
  
  public void loadReports()
  {
    RequestContext context = RequestContext.getCurrentInstance();
    
    List<ViewState> states = this.viewStateHolder.getStates();
    for (ViewState state : states) {
      context.addCallbackParam(state.getId(), new ViewInfo(state));
    }
  }
  
  public void create()
  {
    ViewState state = this.viewStateHolder.createNewState();
    this.viewStateHolder.registerState(state);
    
    this.activeViewId = state.getId();
    if (this.log.isInfoEnabled()) {
      this.log.info("Created a new view state : {}", state.getId());
    }
    RequestContext requestContext = RequestContext.getCurrentInstance();
    requestContext.addCallbackParam("report", new ViewInfo(state));
  }
  
  public void createDirectory()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
    

    ReportFile parent = getTargetDirectory();
    ReportFile newFile;


    StringBuilder builder = new StringBuilder();
    builder.append(parent.getPath());
    if (!parent.getPath().endsWith("/")) {
      builder.append("/");
    }
    builder.append(this.folderName);
    
    String path = builder.toString();
    if (this.log.isInfoEnabled()) {
      this.log.info("Creating a new folder : {}", path);
    }
    try
    {
      if (this.repository.exists(path))
      {
        this.folderName = null;
        
        String title = bundle.getString("error.create.folder.title");
        String message = bundle.getString("warn.folder.exists");
        
        context.addMessage("new-folder-form:name", new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message));
        

        return;
      }
      newFile = this.repository.createDirectory(parent, this.folderName);
    }
    catch (IOException e)
    {
      String title = bundle.getString("error.create.folder.title");
      String message = bundle.getString("error.create.folder.io") + e;
      
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message));
      if (this.log.isErrorEnabled()) {
        this.log.error(title, e);
      }
      return;
    }
    RepositoryNode parentNode = getRepositoryRootNode().findNode(parent);
    parentNode.setExpanded(true);
    parentNode.setSelected(false);
    parentNode.refresh();
    
    RepositoryNode newFileNode = getRepositoryRootNode().findNode(newFile);
    newFileNode.setSelected(true);
    
    this.selection = newFileNode;
    this.folderName = null;
    
    RequestContext requestContext = RequestContext.getCurrentInstance();
    requestContext.execute("newFolderDialog.hide()");
  }
  
  public void save()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    RequestContext requestContext = RequestContext.getCurrentInstance();
    
    ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
    


    Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
    String param = (String)parameters.get("close");
    
    String viewId = (String)parameters.get("viewId");
    if (viewId == null) {
      viewId = this.activeViewId;
    }
    boolean saveAndClose = "true".equals(param);
    
    ViewState state = this.viewStateHolder.getState(viewId);
    
    ReportFile file = state.getFile();
    if (file == null)
    {
      suggestNewName();
      
      requestContext.update("new-form");
      requestContext.execute("newReportDialog.show()");
      
      return;
    }
    requestContext.update(Arrays.asList(new String[] { "toolbar-form:toolbar", "repository-form:repository-panel", "growl" }));
    try
    {
      this.repository.setReportContent(file, new ReportContent(state));
    }
    catch (ConfigurationException e)
    {
      String title = bundle.getString("error.save.report.title");
      String message = bundle.getString("error.save.report.format") + e;
      
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, title, message));
      if (this.log.isErrorEnabled()) {
        this.log.error(title, e);
      }
      return;
    }
    catch (IOException e)
    {
      String title = bundle.getString("error.save.report.title");
      String message = bundle.getString("error.save.report.io") + e;
      
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message));
      if (this.log.isErrorEnabled()) {
        this.log.error(title, e);
      }
      return;
    }
    if (saveAndClose)
    {
      requestContext.update("close-form");
      
      close(viewId);
    }
    else
    {
      if (this.selection != null) {
        this.selection.setSelected(false);
      }
      RepositoryNode node = getRepositoryRootNode().selectNode(file);
      node.setViewId(state.getId());
      node.setSelected(true);
      
      this.selection = node;
    }
    state.setDirty(false);
    
    String title = bundle.getString("message.save.report.title");
    String message = bundle.getString("message.save.report.message");
    
    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, title, message));
    

    requestContext.execute("enableSave(false);");
  }
  
  public void saveAs()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
    

    ReportFile parent = getTargetDirectory();

    ReportFile file;
    RepositoryNode root = getRepositoryRootNode();
    
    ViewState state = this.viewStateHolder.getState(this.activeViewId);
    if (state.getFile() != null)
    {
      RepositoryNode node = root.findNode(state.getFile());
      if (node != null) {
        node.setViewId(null);
      }
    }
    ReportContent content = new ReportContent(state);
    try
    {
      file = this.repository.createFile(parent, this.reportName, content);
    }
    catch (ConfigurationException e)
    {
      String title = bundle.getString("error.save.report.title");
      String message = bundle.getString("error.save.report.format") + e;
      
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message));
      if (this.log.isErrorEnabled()) {
        this.log.error(title, e);
      }
      return;
    }
    catch (IOException e)
    {
      String title = bundle.getString("error.save.report.title");
      String message = bundle.getString("error.save.report.io") + e;
      
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message));
      if (this.log.isErrorEnabled()) {
        this.log.error(title, e);
      }
      return;
    }
    
    state.setName(this.reportName);
    state.setFile(file);
    state.setDirty(false);
    
    RepositoryNode parentNode = root.findNode(parent);
    parentNode.setSelected(false);
    parentNode.setExpanded(true);
    if (this.selection != null) {
      this.selection.setSelected(false);
    }
    RepositoryNode node = parentNode.selectNode(file);
    if (node == null) {
		node = new RepositoryNode(file, repository);
		node.setParent(parentNode);

		parentNode.getChildren().add(node);

		final RepositoryFileComparator comparator = new RepositoryFileComparator();

		Collections.sort(parentNode.getChildren(),
				new Comparator<TreeNode>() {

					@Override
					public int compare(TreeNode t1, TreeNode t2) {
						RepositoryNode r1 = (RepositoryNode) t1;
						RepositoryNode r2 = (RepositoryNode) t2;

						return comparator.compare(r1.getObject(),
								r2.getObject());
					}
				});
	}
    node.setViewId(this.activeViewId);
    node.setSelected(true);
    
    this.selection = node;
    this.reportName = null;
    
    RequestContext requestContext = RequestContext.getCurrentInstance();
    requestContext.addCallbackParam("name", state.getName());
    requestContext.addCallbackParam("path", file.getPath());
    
    String title = bundle.getString("message.save.report.title");
    
    String message = bundle.getString("message.saveAs.report.message") + file.getPath();
    
    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, title, message));
  }
  
  public void open()
  {
    if (this.selection == null)
    {
      if (this.log.isWarnEnabled()) {
        this.log.warn("Unable to load report from empty or multiple selection.");
      }
      return;
    }
    FacesContext context = FacesContext.getCurrentInstance();
    ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
    

    RepositoryNode node = (RepositoryNode)this.selection;
    ReportFile file = (ReportFile)node.getObject();
    
    String viewId = UUID.randomUUID().toString();
    
    ViewState state = new ViewState(viewId, file.getName());
    state.setFile(file);
    
    String errorMessage = null;
    Exception exception = null;
    try
    {
      ReportContent content = this.repository.getReportContent(file);
      content.read(state, this.dataSourceManager, this.settings.getConfiguration());
    }
    catch (ConfigurationException e)
    {
      exception = e;
      errorMessage = bundle.getString("error.open.report.format") + e;
    }
    catch (DataSourceNotFoundException e)
    {
      exception = e;
      
      errorMessage = bundle.getString("error.open.report.dataSource") + e.getConnectionInfo().getCatalogName();
    }
    catch (IOException e)
    {
      exception = e;
      errorMessage = bundle.getString("error.open.report.io") + e;
    }
    if (exception != null)
    {
      String title = bundle.getString("error.open.report.title");
      
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, errorMessage));
      if (this.log.isErrorEnabled()) {
        this.log.error(title, exception);
      }
      return;
    }
    this.viewStateHolder.registerState(state);
    if (this.log.isInfoEnabled()) {
      this.log.info("Created a new view state : {}", viewId);
    }
    this.activeViewId = viewId;
    
    RequestContext requestContext = RequestContext.getCurrentInstance();
    requestContext.addCallbackParam("report", new ViewInfo(state));
  }
  
  public void refresh()
  {
    RepositoryNode node = (RepositoryNode)this.selection;
    node.refresh();
  }
  
  public void delete()
  {
    ViewState state = getActiveView();
    ReportFile file = state.getFile();
    
    delete(state);
    if (file != null) {
      delete(file);
    }
    FacesContext context = FacesContext.getCurrentInstance();
    ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
    

    String title = bundle.getString("message.delete.report.title");
    String message = bundle.getString("message.delete.report.message");
    
    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, title, message));
  }
  
  public void deleteFile()
  {
    RepositoryNode node = (RepositoryNode)this.selection;
    if (node.getViewId() != null) {
      delete(this.viewStateHolder.getState(node.getViewId()));
    }
    delete((ReportFile)node.getObject());
    
    FacesContext context = FacesContext.getCurrentInstance();
    ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
    

    String title = bundle.getString("message.delete.report.title");
    String message = bundle.getString("message.delete.report.message");
    
    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, title, message));
  }
  
  public void deleteDirectory()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
    

    RepositoryNode node = (RepositoryNode)this.selection;
    ReportFile directory = (ReportFile)node.getObject();
    try
    {
      List<ViewState> states = this.viewStateHolder.getStates();
      for (ViewState state : states) {
        if (state.getFile() != null)
        {
          ReportFile file = state.getFile();
          
          List<ReportFile> ancestors = file.getAncestors();
          if (ancestors.contains(directory))
          {
            String title = bundle.getString("warn.folder.delete.title");
            
            String message = bundle.getString("warn.folder.delete.openReport.message");
            
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, title, message));
            

            return;
          }
        }
      }
      this.repository.deleteFile(directory);
      
      this.selection.getParent().getChildren().remove(this.selection);
      
      this.selection = null;
      
      String title = bundle.getString("message.delete.folder.title");
      String message = bundle.getString("message.delete.folder.message");
      
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, title, message));
    }
    catch (IOException e)
    {
      String title = bundle.getString("error.delete.folder.title");
      String message = bundle.getString("error.delete.folder.message") + e;
      

      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message));
      if (this.log.isErrorEnabled()) {
        this.log.error(title, e);
      }
    }
  }
  
  protected void delete(ViewState state)
  {
    String viewId = state.getId();
    
    this.viewStateHolder.unregisterState(viewId);
    if (viewId.equals(this.activeViewId))
    {
      this.activeViewId = null;
      synchronized (this.viewStateHolder)
      {
        List<ViewState> states = this.viewStateHolder.getStates();
        
        int index = states.indexOf(state);
        if (index >= states.size() - 1) {
          index--;
        } else {
          index++;
        }
        if ((index > -1) && (index < states.size())) {
          this.activeViewId = ((ViewState)states.get(index)).getId();
        }
      }
    }
    RequestContext.getCurrentInstance().execute(
      String.format("closeTab(getTabIndex('%s'))", new Object[] { viewId }));
  }
  
  protected void delete(ReportFile file)
  {
    try
    {
      this.repository.deleteFile(file);
    }
    catch (IOException e)
    {
      FacesContext context = FacesContext.getCurrentInstance();
      ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
      

      String title = bundle.getString("error.delete.report.title");
      String message = bundle.getString("error.delete.report.message") + e;
      

      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message));
      if (this.log.isErrorEnabled()) {
        this.log.error(title, e);
      }
      return;
    }
    if ((this.selection instanceof RepositoryNode))
    {
      RepositoryNode node = (RepositoryNode)this.selection;
      if (((ReportFile)node.getObject()).equals(file))
      {
        this.selection.getParent().getChildren().remove(this.selection);
        
        this.selection = null;
      }
    }
  }
  
  public void close()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
    String viewId = (String)parameters.get("viewId");
    
    close(viewId);
  }
  
  public void close(String viewId)
  {
    String viewToClose;
    if (viewId == null)
    {
      viewToClose = this.activeViewId;
      this.activeViewId = null;
    }
    else
    {
      viewToClose = viewId;
    }
    ViewState view = this.viewStateHolder.getState(viewToClose);
    int index = this.viewStateHolder.getStates().indexOf(view);
    
    this.viewStateHolder.unregisterState(viewToClose);
    
    RequestContext.getCurrentInstance().execute(
      String.format("closeTab(%s)", new Object[] {Integer.valueOf(index) }));
  }
  
  public boolean isOpenEnabled()
  {
    if (this.selection != null)
    {
      RepositoryNode node = (RepositoryNode)this.selection;
      ReportFile file = (ReportFile)node.getObject();
      if (!file.isDirectory())
      {
        List<ViewState> states = this.viewStateHolder.getStates();
        for (ViewState state : states) {
          if (file.equals(state.getFile())) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }
  
  public boolean isDeleteEnabled()
  {
    if (this.selection != null)
    {
      RepositoryNode node = (RepositoryNode)this.selection;
      ReportFile file = (ReportFile)node.getObject();
      
      return !file.isRoot();
    }
    return false;
  }
  
  public void onTabChange()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
    
    select((String)parameters.get("viewId"));
  }
  
  public void onSelectionChange()
  {
    RepositoryNode root = getRepositoryRootNode();
    root.clearSelection();
    if (this.selection != null) {
      this.selection.setSelected(true);
    }
  }
  
  protected void select(String viewId)
  {
    this.activeViewId = viewId;
    
    ViewState state = this.viewStateHolder.getState(this.activeViewId);
    
    RepositoryNode root = getRepositoryRootNode();
    root.clearSelection();
    if ((state != null) && (state.getFile() != null)) {
      this.selection = root.selectNode(state.getFile());
    }
  }
  
  public void onChange()
  {
    ViewState state = getActiveView();
    if (state != null) {
      state.setDirty(true);
    }
  }
  
  public void suggestNewName()
  {
    String name = null;
    
    ViewState state = getActiveView();
    if (state != null) {
      name = state.getName();
    }
    if (name == null)
    {
      this.reportName = null;
      return;
    }
    ReportFile parent = getTargetDirectory();
    Set<String> names;
    try
    {
      List<ReportFile> children = this.repository.getFiles(parent);
      
      names = new HashSet(children.size());
      for (ReportFile child : children) {
        names.add(child.getName());
      }
    }
    catch (IOException e)
    {
      
      throw new FacesException(e);
    }
    Pattern pattern = Pattern.compile("([^\\(]+)\\(([0-9]+)\\)");
    while (names.contains(name))
    {
      Matcher matcher = pattern.matcher(name);
      if (matcher.matches())
      {
        String prefix = matcher.group(1);
        int suffix = Integer.parseInt(matcher.group(2)) + 1;
        
        StringBuilder builder = new StringBuilder();
        builder.append(prefix);
        builder.append("(");
        builder.append(Integer.toString(suffix));
        builder.append(")");
        
        name = builder.toString();
      }
      else
      {
        name = name + "(2)";
      }
    }
    this.reportName = name;
  }
  
  protected ReportFile getTargetDirectory()
  {
    ReportFile parent = null;
    if (this.selection != null)
    {
      RepositoryNode node = (RepositoryNode)this.selection;
      
      ReportFile selectedFile = (ReportFile)node.getObject();
      if (selectedFile.isDirectory()) {
        parent = selectedFile;
      } else {
        try
        {
          parent = selectedFile.getParent();
        }
        catch (IOException e)
        {
          throw new FacesException(e);
        }
      }
    }
    if (parent == null) {
      try
      {
        parent = this.repository.getRoot();
      }
      catch (IOException e)
      {
        throw new FacesException(e);
      }
    }
    return parent;
  }
  
  public TreeNode getRootNode()
  {
	  if (rootNode == null) {
			this.rootNode = new DefaultTreeNode();

			rootNode.setExpanded(true);

			RepositoryNode node;

			try {
				node = new RepositoryNode(repository.getRoot(), repository);
			} catch (IOException e) {
				throw new FacesException(e);
			}

			node.setExpanded(true);

			rootNode.getChildren().add(node);
		}
    return this.rootNode;
  }
  
  protected RepositoryNode getRepositoryRootNode()
  {
    return (RepositoryNode)getRootNode().getChildren().get(0);
  }
  
  public void viewRegistered(ViewStateEvent e)
  {
    String viewId = e.getState().getId();
    ReportFile file = e.getState().getFile();
    if (file == null) {
      return;
    }
    RepositoryNode root = getRepositoryRootNode();
    RepositoryNode node = root.findNode(file);
    if (node != null) {
      node.setViewId(viewId);
    }
  }
  
  public void viewUnregistered(ViewStateEvent e)
  {
    String viewId = e.getState().getId();
    
    RepositoryNode root = getRepositoryRootNode();
    RepositoryNode node = root.findNode(viewId);
    if (node != null) {
      node.setViewId(null);
    }
  }
  
  public ViewState getActiveView()
  {
    if (this.activeViewId == null) {
      return null;
    }
    return this.viewStateHolder.getState(this.activeViewId);
  }
  
  public String getActiveViewId()
  {
    return this.activeViewId;
  }
  
  public void setActiveViewId(String activeViewId)
  {
    this.activeViewId = activeViewId;
  }
  
  public String getReportName()
  {
    return this.reportName;
  }
  
  public void setReportName(String reportName)
  {
    this.reportName = reportName;
  }
  
  public String getFolderName()
  {
    return this.folderName;
  }
  
  public void setFolderName(String folderName)
  {
    this.folderName = folderName;
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
  
  public ViewStateHolder getViewStateHolder()
  {
    return this.viewStateHolder;
  }
  
  public void setViewStateHolder(ViewStateHolder viewStateHolder)
  {
    this.viewStateHolder = viewStateHolder;
  }
  
  public TreeNode getSelection()
  {
    return this.selection;
  }
  
  public void setSelection(TreeNode selection)
  {
    this.selection = selection;
  }
  
  public static class ViewInfo
    implements Serializable
  {
    private static final long serialVersionUID = 862747643432896517L;
    private String id;
    private String name;
    private String path;
    private boolean dirty;
    private boolean initialized;
    
    ViewInfo(ViewState state)
    {
      this.id = state.getId();
      this.name = state.getName();
      this.dirty = state.isDirty();
      this.initialized = (state.getConnectionInfo() != null);
      if (state.getFile() != null) {
        this.path = state.getFile().getPath();
      }
    }
    
    public String getId()
    {
      return this.id;
    }
    
    public String getName()
    {
      return this.name;
    }
    
    public String getPath()
    {
      return this.path;
    }
    
    public boolean isDirty()
    {
      return this.dirty;
    }
    
    public boolean isInitialized()
    {
      return this.initialized;
    }
  }
}
