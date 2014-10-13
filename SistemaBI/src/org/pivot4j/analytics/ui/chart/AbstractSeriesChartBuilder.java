package org.pivot4j.analytics.ui.chart;

import java.util.LinkedList;
import java.util.List;
import javax.faces.context.FacesContext;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.CartesianChart;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

public abstract class AbstractSeriesChartBuilder<C extends CartesianChart>
  extends AbstractChartBuilder<C, CartesianChartModel>
{
  private ChartSeries series;
  
  public AbstractSeriesChartBuilder(FacesContext context)
  {
    super(context);
  }
  
  protected ChartSeries getSeries()
  {
    return this.series;
  }
  
  protected CartesianChartModel createModel(ChartRenderContext context)
  {
    return new CartesianChartModel();
  }
  
  public void startSeries(ChartRenderContext context)
  {
    this.series = new ChartSeries();
    
    List<Member> path = new LinkedList(context.getSeriesPath());
    if ((!path.isEmpty()) && (context.getSeriesCount() > 1))
    {
      int size = path.size();
      String label;
      if (size == 1)
      {
        label = ((Member)path.get(0)).getCaption();
      }
      else
      {
        StringBuilder builder = new StringBuilder();
        
        boolean first = true;
        for (Member member : path)
        {
          if (first) {
            first = false;
          } else {
            builder.append(" / ");
          }
          builder.append(member.getCaption());
        }
        label = builder.toString();
      }
      this.series.setLabel(label);
    }
  }
  
  public void renderContent(ChartRenderContext context, String label, Double value)
  {
    if (this.series.getLabel() == null) {
      this.series.setLabel(context.getMember().getHierarchy().getCaption());
    }
    this.series.set(label, value);
  }
  
  public void endSeries(ChartRenderContext context)
  {
    ((CartesianChartModel)getModel()).addSeries(this.series);
    
    this.series = null;
  }
}
