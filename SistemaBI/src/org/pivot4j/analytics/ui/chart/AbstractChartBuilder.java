package org.pivot4j.analytics.ui.chart;

import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import org.olap4j.metadata.Member;
import org.pivot4j.ui.AbstractRenderCallback;
import org.pivot4j.ui.chart.ChartRenderContext;
import org.pivot4j.ui.command.UICommand;
import org.primefaces.component.breadcrumb.BreadCrumb;
import org.primefaces.component.chart.UIChart;
import org.primefaces.component.menuitem.UIMenuItem;
import org.primefaces.model.chart.ChartModel;

public abstract class AbstractChartBuilder<C extends UIChart, M extends ChartModel>
  extends AbstractRenderCallback<ChartRenderContext>
  implements ChartBuilder
{
  private FacesContext context;
  private UIComponent component;
  private HtmlPanelGrid pageComponent;
  private C chart;
  private M model;
  
  public AbstractChartBuilder(FacesContext context)
  {
    this.context = context;
  }
  
  protected FacesContext getContext()
  {
    return this.context;
  }
  
  public UIComponent getComponent()
  {
    return this.component;
  }
  
  public void setComponent(UIComponent component)
  {
    this.component = component;
  }
  
  protected HtmlPanelGrid getPageComponent()
  {
    return this.pageComponent;
  }
  
  protected C getChart()
  {
    return this.chart;
  }
  
  protected M getModel()
  {
    return this.model;
  }
  
  public void startRender(ChartRenderContext context)
  {
    this.component.getChildren().clear();
  }
  
  public void startPage(ChartRenderContext context)
  {
    this.pageComponent = createPageComponent(context);
  }
  
  public void startChart(ChartRenderContext context)
  {
    this.chart = createChart(context);
    this.model = createModel(context);
    
    configureChart(context, this.chart);
  }
  
  protected abstract C createChart(ChartRenderContext paramChartRenderContext);
  
  protected abstract M createModel(ChartRenderContext paramChartRenderContext);
  
  protected BreadCrumb createBreadCrumb(ChartRenderContext context)
  {
    BreadCrumb breadCrumb = new BreadCrumb();
    
    UIMenuItem rootItem = new UIMenuItem();
    
    rootItem.setValue("");
    breadCrumb.getChildren().add(rootItem);
    
    List<Member> members = context.getPagePath();
    for (Member member : members)
    {
      UIMenuItem item = new UIMenuItem();
      
      item.setValue(member.getCaption());
      item.setTitle(member.getDescription());
      
      breadCrumb.getChildren().add(item);
    }
    return breadCrumb;
  }
  
  protected HtmlPanelGrid createPageComponent(ChartRenderContext context)
  {
    DefaultChartRenderer renderer = (DefaultChartRenderer)context.getRenderer();
    
    HtmlPanelGrid grid = new HtmlPanelGrid();
    if (renderer.getWidth() <= 0) {
      grid.setStyle("width: 100%;");
    }
    grid.setStyleClass("chart-page");
    grid.setColumns(context.getChartCount());
    
    return grid;
  }
  
  protected void configureChart(ChartRenderContext context, C chart)
  {
    List<Member> path = context.getChartPath();
    if ((path != null) && (path.size() > 0))
    {
      String title = ((Member)path.get(path.size() - 1)).getCaption();
      
      chart.setTitle(title);
    }
    chart.setShadow(true);
    

    DefaultChartRenderer renderer = (DefaultChartRenderer)context.getRenderer();
    if (renderer.getLegendPosition() != null) {
      chart.setLegendPosition(renderer.getLegendPosition().name());
    }
    StringBuilder builder = new StringBuilder();
    builder.append("width: ");
    if (renderer.getWidth() <= 0)
    {
      builder.append("100%; ");
    }
    else
    {
      builder.append(Integer.toString(renderer.getWidth()));
      builder.append("px; ");
    }
    if (renderer.getHeight() > 0)
    {
      builder.append("height: ");
      builder.append(Integer.toString(renderer.getHeight()));
      builder.append("px;");
    }
    chart.setStyle(builder.toString());
  }
  
  public void startSeries(ChartRenderContext context) {}
  
  public void renderCommands(ChartRenderContext context, List<UICommand<?>> commands) {}
  
  public void endSeries(ChartRenderContext context) {}
  
  public void endChart(ChartRenderContext context)
  {
    this.chart.setValue(this.model);
    
    this.pageComponent.getChildren().add(this.chart);
    
    this.model = null;
    this.chart = null;
  }
  
  public void endPage(ChartRenderContext context)
  {
    if (!context.getPagePath().isEmpty()) {
      this.pageComponent.getFacets().put("header", createBreadCrumb(context));
    }
    this.component.getChildren().add(this.pageComponent);
    
    this.pageComponent = null;
  }
}
