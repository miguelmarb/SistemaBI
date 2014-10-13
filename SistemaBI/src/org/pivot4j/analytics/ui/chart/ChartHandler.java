package org.pivot4j.analytics.ui.chart;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.apache.commons.lang3.StringUtils;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.pivot4j.ModelChangeEvent;
import org.pivot4j.ModelChangeListener;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.ui.PivotStateManager;
import org.pivot4j.ui.chart.ChartRenderer;
import org.pivot4j.util.OlapUtils;

@ManagedBean(name="chartHandler")
@ViewScoped
public class ChartHandler
  implements ModelChangeListener, Serializable
{
  private static final long serialVersionUID = 8929886836467291035L;
  @ManagedProperty("#{pivotStateManager}")
  private PivotStateManager stateManager;
  @ManagedProperty("#{chartBuilderFactory}")
  private ChartBuilderFactory chartBuilderFactory;
  private PivotModel model;
  private DefaultChartRenderer renderer;
  private HtmlPanelGroup component;
  private List<SelectItem> charts;
  private Axis pageAxis;
  private Axis chartAxis;
  private Axis seriesAxis;
  private Axis plotAxis;
  private int width;
  private int height;
  private DefaultChartRenderer.Position legendPosition;
  
  @PostConstruct
  protected void initialize()
  {
    this.model = this.stateManager.getModel();
    if (this.model != null) {
      this.model.addModelChangeListener(this);
    }
    this.renderer = new DefaultChartRenderer();
    
    Serializable state = this.stateManager.getChartState();
    if (state != null) {
      this.renderer.restoreState(state);
    }
    this.charts = new LinkedList();
    
    reset();
    
    FacesContext context = FacesContext.getCurrentInstance();
    
    ResourceBundle resources = context.getApplication().getResourceBundle(context, "msg");
    

    String prefix = "label.chart.items.";
    for (String builder : this.chartBuilderFactory.getBuilderNames()) {
      this.charts.add(new SelectItem(builder, resources.getString(prefix + builder)));
    }
  }
  
  @PreDestroy
  protected void destroy()
  {
    if (this.model != null) {
      this.model.removeModelChangeListener(this);
    }
  }
  
  public ChartRenderer getRenderer()
  {
    return this.renderer;
  }
  
  public HtmlPanelGroup getComponent()
  {
    return this.component;
  }
  
  public void setComponent(HtmlPanelGroup component)
  {
    this.component = component;
  }
  
  public PivotStateManager getStateManager()
  {
    return this.stateManager;
  }
  
  public void setStateManager(PivotStateManager stateManager)
  {
    this.stateManager = stateManager;
  }
  
  public ChartBuilderFactory getChartBuilderFactory()
  {
    return this.chartBuilderFactory;
  }
  
  public void setChartBuilderFactory(ChartBuilderFactory chartBuilderFactory)
  {
    this.chartBuilderFactory = chartBuilderFactory;
  }
  
  public List<SelectItem> getCharts()
  {
    return this.charts;
  }
  
  public String getChartName()
  {
    return this.renderer.getChartName();
  }
  
  public void setChartName(String chartName)
  {
    this.renderer.setChartName(chartName);
  }
  
  public Axis getPageAxis()
  {
    return this.pageAxis;
  }
  
  public void setPageAxis(Axis pageAxis)
  {
    this.pageAxis = pageAxis;
  }
  
  public Axis getChartAxis()
  {
    return this.chartAxis;
  }
  
  public void setChartAxis(Axis chartAxis)
  {
    this.chartAxis = chartAxis;
  }
  
  public Axis getSeriesAxis()
  {
    return this.seriesAxis;
  }
  
  public void setSeriesAxis(Axis seriesAxis)
  {
    this.seriesAxis = seriesAxis;
  }
  
  public Axis getPlotAxis()
  {
    return this.plotAxis;
  }
  
  public void setPlotAxis(Axis plotAxis)
  {
    this.plotAxis = plotAxis;
  }
  
  public int getWidth()
  {
    return this.width;
  }
  
  public void setWidth(int width)
  {
    this.width = width;
  }
  
  public int getHeight()
  {
    return this.height;
  }
  
  public void setHeight(int height)
  {
    this.height = height;
  }
  
  public DefaultChartRenderer.Position getLegendPosition()
  {
    return this.legendPosition;
  }
  
  public void setLegendPosition(DefaultChartRenderer.Position legendPosition)
  {
    this.legendPosition = legendPosition;
  }
  
  public boolean isValid()
  {
    if ((this.model == null) || (!this.model.isInitialized())) {
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
  
  public void onPreRenderView()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    if (!context.isPostback()) {
      render();
    }
  }
  
  public void reset()
  {
    this.pageAxis = this.renderer.getPageAxis();
    this.chartAxis = this.renderer.getChartAxis();
    this.seriesAxis = this.renderer.getSeriesAxis();
    this.plotAxis = this.renderer.getPlotAxis();
    
    this.width = this.renderer.getWidth();
    this.height = this.renderer.getHeight();
    this.legendPosition = this.renderer.getLegendPosition();
  }
  
  public void apply()
  {
    boolean valid = false;
    
    valid |= ((this.pageAxis != null) && (!OlapUtils.equals(this.plotAxis, this.pageAxis)));
    valid |= ((this.chartAxis != null) && (!OlapUtils.equals(this.plotAxis, this.chartAxis)));
    valid |= ((this.seriesAxis != null) && (!OlapUtils.equals(this.plotAxis, this.seriesAxis)));
    if (valid)
    {
      this.renderer.setPageAxis(this.pageAxis);
      this.renderer.setChartAxis(this.chartAxis);
      this.renderer.setSeriesAxis(this.seriesAxis);
      this.renderer.setPlotAxis(this.plotAxis);
      
      this.renderer.setWidth(this.width);
      this.renderer.setHeight(this.height);
      
      this.renderer.setLegendPosition(this.legendPosition);
      
      render();
    }
    else
    {
      FacesContext context = FacesContext.getCurrentInstance();
      

      ResourceBundle messages = context.getApplication().getResourceBundle(context, "msg");
      
      String title = messages.getString("warn.chart.axis.unused.title");
      String msg = messages.getString("warn.chart.axis.unused.message");
      
      context.addMessage("axis-plot", new FacesMessage(FacesMessage.SEVERITY_WARN, title, msg));
    }
  }
  
  public void render()
  {
    String chartName = getChartName();
    if ((this.model != null) && (this.model.isInitialized()) && 
      (StringUtils.isNotBlank(chartName)))
    {
      FacesContext context = FacesContext.getCurrentInstance();
      
      ChartBuilder builder = this.chartBuilderFactory.createChartBuilder(chartName, context);
      
      builder.setComponent(this.component);
      
      this.renderer.render(this.model, builder);
    }
    if (this.renderer != null) {
      this.stateManager.setChartState(this.renderer.saveState());
    }
  }
  
  public void modelInitialized(ModelChangeEvent e) {}
  
  public void modelDestroyed(ModelChangeEvent e) {}
  
  public void modelChanged(ModelChangeEvent e) {}
  
  public void structureChanged(ModelChangeEvent e)
  {
    render();
  }
}
