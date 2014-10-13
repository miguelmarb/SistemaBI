package org.pivot4j.analytics.ui;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.olap4j.AllocationPolicy;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;
import org.olap4j.OlapException;
import org.olap4j.Scenario;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Schema;
import org.pivot4j.ModelChangeEvent;
import org.pivot4j.ModelChangeListener;
import org.pivot4j.PivotModel;
import org.pivot4j.QueryEvent;
import org.pivot4j.QueryListener;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.impl.PivotModelImpl;
import org.pivot4j.transform.NonEmpty;
import org.pivot4j.transform.SwapAxes;
import org.pivot4j.ui.PivotRenderer;
import org.pivot4j.ui.command.BasicDrillThroughCommand;
import org.pivot4j.ui.command.UICommand;
import org.pivot4j.ui.command.UICommandParameters;
import org.pivot4j.ui.table.TableRenderer;
import org.pivot4j.util.OlapUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.extensions.event.CloseEvent;
import org.primefaces.extensions.event.OpenEvent;
import org.primefaces.extensions.model.layout.LayoutOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name="viewHandler")
@RequestScoped
public class ViewHandler
  implements QueryListener, ModelChangeListener
{
  @ManagedProperty("#{pivotStateManager}")
  private PivotStateManager stateManager;
  @ManagedProperty("#{navigatorHandler}")
  private NavigatorHandler navigator;
  @ManagedProperty("#{drillThroughHandler}")
  private DrillThroughHandler drillThroughHandler;
  @ManagedProperty("#{settings}")
  private Settings settings;
  private LayoutOptions layoutOptions;
  private PivotModel model;
  private DefaultTableRenderer renderer;
  private List<UISelectItem> cubeItems;
  private String cubeName;
  private String currentMdx;
  private Long duration;
  private UIComponent component;
  private UIComponent filterComponent;
  private Exception lastError;
  
  @PostConstruct
  protected void initialize()
  {
    this.model = this.stateManager.getModel();
    if (this.model != null)
    {
      this.model.addQueryListener(this);
      this.model.addModelChangeListener(this);
      if (this.model.isInitialized())
      {
        this.cubeName = this.model.getCube().getName();
        
        checkError(this.model);
      }
      else
      {
        ConnectionInfo connectionInfo = this.stateManager.getConnectionInfo();
        if ((connectionInfo != null) && (!this.model.isInitialized()))
        {
          this.cubeName = connectionInfo.getCubeName();
          
          onCubeChange();
        }
      }
    }
    this.renderer = new DefaultTableRenderer();
    
    Serializable state = this.stateManager.getRendererState();
    if (state == null)
    {
      try
      {
        this.renderer.restoreSettings(this.settings.getConfiguration()
          .configurationAt("render"));
      }
      catch (IllegalArgumentException e) {}
      this.renderer.setVisible(true);
      this.renderer.setShowDimensionTitle(true);
      this.renderer.setShowParentMembers(false);
      this.renderer.setHideSpans(false);
      this.renderer.setDrillDownMode("position");
      this.renderer.setEnableDrillThrough(false);
    }
    else
    {
      this.renderer.restoreState(state);
    }
    boolean readOnly = this.stateManager.isReadOnly();
    
    this.renderer.setEnableDrillDown(!readOnly);
    this.renderer.setEnableSort(!readOnly);
    
    this.renderer.addCommand(new DrillThroughCommandImpl(this.renderer));
  }
  
  private void checkError(PivotModel model)
  {
    Exception error = null;
    try
    {
      model.getCellSet();
    }
    catch (Exception e)
    {
      model.destroy();
      
      Logger logger = LoggerFactory.getLogger(getClass());
      logger.error("Failed to get query result.", e);
      
      error = e;
      
      FacesContext context = FacesContext.getCurrentInstance();
      ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
      

      String title = bundle.getString("error.unhandled.title");
      String message = bundle.getString("error.unhandled.message") + e;
      
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message));
    }
    this.lastError = error;
  }
  
  @PreDestroy
  protected void destroy()
  {
    if (this.model != null)
    {
      this.model.removeQueryListener(this);
      this.model.removeModelChangeListener(this);
    }
  }
  
  public Settings getSettings()
  {
    return this.settings;
  }
  
  public void setSettings(Settings settings)
  {
    this.settings = settings;
  }
  
  public TableRenderer getRenderer()
  {
    return this.renderer;
  }
  
  public PivotStateManager getStateManager()
  {
    return this.stateManager;
  }
  
  public void setStateManager(PivotStateManager stateManager)
  {
    this.stateManager = stateManager;
  }
  
  public DrillThroughHandler getDrillThroughHandler()
  {
    return this.drillThroughHandler;
  }
  
  public void setDrillThroughHandler(DrillThroughHandler drillThroughHandler)
  {
    this.drillThroughHandler = drillThroughHandler;
  }
  
  public String getCubeName()
  {
    return this.cubeName;
  }
  
  public void setCubeName(String cubeName)
  {
    this.cubeName = cubeName;
  }
  
  public NavigatorHandler getNavigator()
  {
    return this.navigator;
  }
  
  public void setNavigator(NavigatorHandler navigator)
  {
    this.navigator = navigator;
  }
  
  public UIComponent getComponent()
  {
    return this.component;
  }
  
  public void setComponent(UIComponent component)
  {
    this.component = component;
  }
  
  public UIComponent getFilterComponent()
  {
    return this.filterComponent;
  }
  
  public void setFilterComponent(UIComponent filterComponent)
  {
    this.filterComponent = filterComponent;
  }
  
  public Long getDuration()
  {
    return this.duration;
  }
  
  public List<UISelectItem> getCubes()
    throws SQLException
  {
    if (this.cubeItems == null)
    {
      FacesContext context = FacesContext.getCurrentInstance();
      
      ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
      

      String defaultLabel = bundle.getString("message.cubeList.default");
      
      this.cubeItems = new ArrayList();
      
      UISelectItem defaultItem = new UISelectItem();
      defaultItem.setItemLabel(defaultLabel);
      defaultItem.setItemValue("");
      
      this.cubeItems.add(defaultItem);
      if (this.model != null)
      {
        OlapDataSource dataSource = ((PivotModelImpl)this.model).getDataSource();
        
        Schema schema = dataSource.getConnection().getOlapSchema();
        
        List<Cube> cubes = schema.getCubes();
        for (Cube cube : cubes) {
          if (cube.isVisible())
          {
            UISelectItem item = new UISelectItem();
            item.setItemLabel(cube.getCaption());
            item.setItemValue(cube.getName());
            
            this.cubeItems.add(item);
          }
        }
      }
    }
    return this.cubeItems;
  }
  
  public void onCubeChange()
  {
    if (StringUtils.isEmpty(this.cubeName))
    {
      if (this.model.isInitialized()) {
        this.model.destroy();
      }
    }
    else
    {
      this.model.setMdx(getDefaultMdx());
      if (!this.model.isInitialized()) {
        try
        {
          this.model.initialize();
        }
        catch (Exception e)
        {
          FacesContext context = FacesContext.getCurrentInstance();
          
          ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
          
          String title = bundle.getString("error.unhandled.title");
          
          String message = bundle.getString("error.unhandled.message") + e;
          
          Logger log = LoggerFactory.getLogger(getClass());
          if (log.isErrorEnabled()) {
            log.error(title, e);
          }
          context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, message));
        }
      }
    }
  }
  
  private String getDefaultMdx()
  {
    String mdx;
    if (OlapUtils.isEmptySetSupported(this.model.getMetadata()))
    {
      if (this.model.getDefaultNonEmpty()) {
        mdx = String.format("select non empty {} on columns, non empty {} on rows from [%s]", new Object[] { this.cubeName });
      } else {
        mdx = String.format("select {} on columns, {} on rows from [%s]", new Object[] { this.cubeName });
      }
    }
    else
    {
      mdx = String.format("select from [%s]", new Object[] { this.cubeName });
    }
    return mdx;
  }
  
  public LayoutOptions getLayoutOptions()
  {
    if (this.layoutOptions == null)
    {
      ViewState view = this.stateManager.getState();
      
      this.layoutOptions = new LayoutOptions();
      this.layoutOptions.addOption("enableCursorHotkey", Boolean.valueOf(false));
      
      LayoutOptions toolbarOptions = new LayoutOptions();
      toolbarOptions.addOption("resizable", Boolean.valueOf(false));
      toolbarOptions.addOption("closable", Boolean.valueOf(false));
      
      this.layoutOptions.setNorthOptions(toolbarOptions);
      
      LayoutOptions navigatorOptions = new LayoutOptions();
      navigatorOptions.addOption("resizable", Boolean.valueOf(true));
      navigatorOptions.addOption("closable", Boolean.valueOf(true));
      navigatorOptions.addOption("slidable", Boolean.valueOf(true));
      navigatorOptions.addOption("size", Integer.valueOf(280));
      if (!view.isRegionVisible(LayoutRegion.Navigator)) {
        navigatorOptions.addOption("initClosed", Boolean.valueOf(true));
      }
      this.layoutOptions.setWestOptions(navigatorOptions);
      
      LayoutOptions childWestOptions = new LayoutOptions();
      navigatorOptions.setChildOptions(childWestOptions);
      
      LayoutOptions cubeListOptions = new LayoutOptions();
      cubeListOptions.addOption("resizable", Boolean.valueOf(false));
      cubeListOptions.addOption("closable", Boolean.valueOf(false));
      cubeListOptions.addOption("slidable", Boolean.valueOf(false));
      cubeListOptions.addOption("size", Integer.valueOf(38));
      
      childWestOptions.setNorthOptions(cubeListOptions);
      
      LayoutOptions targetTreeOptions = new LayoutOptions();
      targetTreeOptions.addOption("resizable", Boolean.valueOf(true));
      targetTreeOptions.addOption("closable", Boolean.valueOf(true));
      targetTreeOptions.addOption("slidable", Boolean.valueOf(true));
      targetTreeOptions.addOption("size", Integer.valueOf(300));
      
      childWestOptions.setSouthOptions(targetTreeOptions);
      
      LayoutOptions contentOptions = new LayoutOptions();
      this.layoutOptions.setCenterOptions(contentOptions);
      
      LayoutOptions childCenterOptions = new LayoutOptions();
      childCenterOptions.addOption("onresize_end", "onViewResize");
      contentOptions.setChildOptions(childCenterOptions);
      
      LayoutOptions filterOptions = new LayoutOptions();
      filterOptions.addOption("resizable", Boolean.valueOf(false));
      filterOptions.addOption("closable", Boolean.valueOf(true));
      filterOptions.addOption("slidable", Boolean.valueOf(true));
      filterOptions.addOption("size", Integer.valueOf(38));
      if (!view.isRegionVisible(LayoutRegion.Filter)) {
        filterOptions.addOption("initClosed", Boolean.valueOf(true));
      }
      childCenterOptions.setNorthOptions(filterOptions);
      
      LayoutOptions editorOptions = new LayoutOptions();
      editorOptions.addOption("resizable", Boolean.valueOf(true));
      editorOptions.addOption("closable", Boolean.valueOf(true));
      editorOptions.addOption("slidable", Boolean.valueOf(true));
      editorOptions.addOption("size", Integer.valueOf(180));
      if (!view.isRegionVisible(LayoutRegion.Mdx)) {
        editorOptions.addOption("initClosed", Boolean.valueOf(true));
      }
      childCenterOptions.setSouthOptions(editorOptions);
      
      LayoutOptions editorToolBarOptions = new LayoutOptions();
      editorToolBarOptions.addOption("resizable", Boolean.valueOf(false));
      editorToolBarOptions.addOption("closable", Boolean.valueOf(false));
      editorToolBarOptions.addOption("slidable", Boolean.valueOf(false));
      editorToolBarOptions.addOption("size", Integer.valueOf(38));
      
      editorOptions.setNorthOptions(editorToolBarOptions);
      
      LayoutOptions editorContentOptions = new LayoutOptions();
      editorContentOptions.addOption("resizable", Boolean.valueOf(false));
      editorContentOptions.addOption("closable", Boolean.valueOf(false));
      editorContentOptions.addOption("slidable", Boolean.valueOf(false));
      editorContentOptions.addOption("spacing_open", Integer.valueOf(0));
      editorContentOptions.addOption("spacing_closed", Integer.valueOf(0));
      
      editorOptions.setChildOptions(editorContentOptions);
    }
    return this.layoutOptions;
  }
  
  public void onPreRenderView()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    if (!context.isPostback()) {
      render();
    }
  }
  
  public void onPanelOpened(OpenEvent event)
  {
    LayoutRegion region = getRegionFromId(event.getComponent().getId());
    if (region != null) {
      this.stateManager.getState().setRegionVisible(region, true);
    }
  }
  
  public void onPanelClosed(CloseEvent event)
  {
    LayoutRegion region = getRegionFromId(event.getComponent().getId());
    if (region != null) {
      this.stateManager.getState().setRegionVisible(region, false);
    }
  }
  
  private LayoutRegion getRegionFromId(String id)
  {
    LayoutRegion region = null;
    if ("mdx-editor-pane".equals(id)) {
      region = LayoutRegion.Mdx;
    } else if ("navigator-pane".equals(id)) {
      region = LayoutRegion.Navigator;
    } else if ("grid-header-pane".equals(id)) {
      region = LayoutRegion.Filter;
    }
    return region;
  }
  
  public boolean isValid()
  {
    if ((this.model == null) || (!this.model.isInitialized()) || (this.lastError != null)) {
      return false;
    }
    CellSet cellSet = this.model.getCellSet();
    if (cellSet == null) {
      return false;
    }
    List<CellSetAxis> axes = this.model.getCellSet().getAxes();
    if (axes.size() < 2) {
      return false;
    }
    return (((CellSetAxis)axes.get(0)).getPositionCount() > 0) && (((CellSetAxis)axes.get(1)).getPositionCount() > 0);
  }
  
  public Exception getLastError()
  {
    return this.lastError;
  }
  
  public String getLastErrorMessage()
  {
    if (this.lastError == null) {
      return null;
    }
    return ExceptionUtils.getRootCauseMessage(this.lastError);
  }
  
  public void render()
  {
    if (this.component != null) {
      this.component.getChildren().clear();
    }
    if (this.filterComponent != null) {
      this.filterComponent.getChildren().clear();
    }
    boolean valid = isValid();
    

    boolean renderGrid = (valid) && (this.component != null) && (this.component.isRendered());
    
    boolean renderFilter = (valid) && (this.filterComponent != null) && (this.filterComponent.isRendered());
    if ((renderGrid) || (renderFilter))
    {
      FacesContext context = FacesContext.getCurrentInstance();
      
      PivotComponentBuilder callback = new PivotComponentBuilder(context);
      callback.setGridPanel(this.component);
      callback.setFilterPanel(this.filterComponent);
      
      this.renderer.render(this.model, callback);
    }
    if (this.renderer != null) {
      this.stateManager.setRendererState(this.renderer.saveState());
    }
  }
  
  public void executeCommand()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    Map<String, String> requestParameters = context.getExternalContext().getRequestParameterMap();
    
    UICommandParameters parameters = new UICommandParameters();
    if (requestParameters.containsKey("axis")) {
      parameters.setAxisOrdinal(Integer.parseInt(
        (String)requestParameters.get("axis")));
    }
    if (requestParameters.containsKey("position")) {
      parameters.setPositionOrdinal(Integer.parseInt(
        (String)requestParameters.get("position")));
    }
    if (requestParameters.containsKey("member")) {
      parameters.setMemberOrdinal(Integer.parseInt(
        (String)requestParameters.get("member")));
    }
    if (requestParameters.containsKey("hierarchy")) {
      parameters.setHierarchyOrdinal(Integer.parseInt(
        (String)requestParameters.get("hierarchy")));
    }
    if (requestParameters.containsKey("cell")) {
      parameters.setCellOrdinal(Integer.parseInt(
        (String)requestParameters.get("cell")));
    }
    UICommand<?> command = this.renderer.getCommand(
      (String)requestParameters.get("command"));
    command.execute(this.model, parameters);
    
    render();
  }
  
  public void updateCell()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
    
    int ordinal = Integer.parseInt((String)parameters.get("cell"));
    
    String id = "input-" + ordinal;
    
    UIInput input = (UIInput)this.component.findComponent(id);
    Double value = (Double)input.getValue();
    
    Cell cell = this.model.getCellSet().getCell(ordinal);
    try
    {
      cell.setValue(value, AllocationPolicy.EQUAL_ALLOCATION, new Object[0]);
    }
    catch (OlapException e)
    {
      throw new FacesException(e);
    }
    this.model.refresh();
  }
  
  public void executeMdx()
  {
    try
    {
      this.model.setMdx(this.currentMdx);
      if (!this.model.isInitialized()) {
        this.model.initialize();
      }
      render();
    }
    catch (Exception e)
    {
      this.lastError = e;
      if (this.model.isInitialized()) {
        this.model.destroy();
      }
      FacesContext context = FacesContext.getCurrentInstance();
      
      ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
      

      String title = bundle.getString("error.execute.title");
      
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, e
        .getMessage()));
    }
  }
  
  public String getCurrentMdx()
  {
    if (this.model == null) {
      return null;
    }
    if ((this.lastError != null) && (StringUtils.isBlank(this.model.getCurrentMdx()))) {
      return this.model.getMdx();
    }
    return this.model.getCurrentMdx();
  }
  
  public void setCurrentMdx(String currentMdx)
  {
    this.currentMdx = currentMdx;
  }
  
  public boolean getShowParentMembers()
  {
    return this.renderer.getShowParentMembers();
  }
  
  public void setShowParentMembers(boolean showParentMembers)
  {
    this.renderer.setShowParentMembers(showParentMembers);
  }
  
  public boolean getHideSpans()
  {
    return this.renderer.getHideSpans();
  }
  
  public void setHideSpans(boolean hideSpans)
  {
    this.renderer.setHideSpans(hideSpans);
  }
  
  public boolean getDrillThrough()
  {
    return this.renderer.getEnableDrillThrough();
  }
  
  public void setDrillThrough(boolean drillThrough)
  {
    this.renderer.setEnableDrillThrough(drillThrough);
  }
  
  public String getDrillDownMode()
  {
    return this.renderer.getDrillDownMode();
  }
  
  public void setDrillDownMode(String drillDownMode)
  {
    this.renderer.setDrillDownMode(drillDownMode);
  }
  
  public boolean isVisible()
  {
    return this.renderer.isVisible();
  }
  
  public void setVisible(boolean visible)
  {
    this.renderer.setVisible(visible);
  }
  
  public boolean getSwapAxes()
  {
    if ((this.model == null) || (!this.model.isInitialized())) {
      return false;
    }
    SwapAxes transform = (SwapAxes)this.model.getTransform(SwapAxes.class);
    return transform.isSwapAxes();
  }
  
  public void setSwapAxes(boolean swapAxes)
  {
    SwapAxes transform = (SwapAxes)this.model.getTransform(SwapAxes.class);
    
    boolean current = transform.isSwapAxes();
    transform.setSwapAxes(swapAxes);
    if (current != swapAxes) {
      this.renderer.swapAxes();
    }
  }
  
  public boolean getNonEmpty()
  {
    if ((this.model == null) || (!this.model.isInitialized())) {
      return false;
    }
    NonEmpty transform = (NonEmpty)this.model.getTransform(NonEmpty.class);
    return transform.isNonEmpty();
  }
  
  public void setNonEmpty(boolean nonEmpty)
  {
    NonEmpty transform = (NonEmpty)this.model.getTransform(NonEmpty.class);
    transform.setNonEmpty(nonEmpty);
  }
  
  public boolean isScenarioEnabled()
  {
    return (this.model.isInitialized()) && (this.model.isScenarioSupported()) && (this.model.getScenario() != null);
  }
  
  public void setScenarioEnabled(boolean scenarioEnabled)
  {
    if (scenarioEnabled)
    {
      if (this.model.getScenario() == null)
      {
        Scenario scenario = this.model.createScenario();
        this.model.setScenario(scenario);
      }
    }
    else {
      this.model.setScenario(null);
    }
  }
  
  protected void setDrillThroughRows(Integer drillThroughRows)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    Map<String, Object> session = context.getExternalContext().getSessionMap();
    if (drillThroughRows == null) {
      session.remove("drillThroughRows");
    } else {
      session.put("drillThroughRows", drillThroughRows);
    }
  }
  
  public boolean getRenderSlicer()
  {
    return this.renderer.getRenderSlicer();
  }
  
  public void setRenderSlicer(boolean renderSlicer)
  {
    this.renderer.setRenderSlicer(renderSlicer);
  }
  
  public void queryExecuted(QueryEvent e)
  {
    this.duration = Long.valueOf(e.getDuration());
    if (this.model.getCube() == null) {
      this.cubeName = null;
    } else {
      this.cubeName = this.model.getCube().getName();
    }
    this.lastError = null;
  }
  
  public void modelInitialized(ModelChangeEvent e) {}
  
  public void modelDestroyed(ModelChangeEvent e) {}
  
  public void modelChanged(ModelChangeEvent e) {}
  
  public void structureChanged(ModelChangeEvent e)
  {
    render();
  }
  
  class DrillThroughCommandImpl
    extends BasicDrillThroughCommand
  {
    public DrillThroughCommandImpl(PivotRenderer<?> renderer)
    {
      super(renderer);
    }
    
    public ResultSet execute(PivotModel model, UICommandParameters parameters)
    {
      Cell cell = model.getCellSet().getCell(parameters.getCellOrdinal());
      
      ViewHandler.this.drillThroughHandler.update(cell);
      
      RequestContext context = RequestContext.getCurrentInstance();
      context.update("drillthrough-form");
      context.execute("drillThroughDialog.show()");
      
      return null;
    }
  }
}
