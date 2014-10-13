package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;
import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.bar.BarChart;

public class BarChartBuilder
  extends AbstractSeriesChartBuilder<BarChart>
{
  public static String NAME = "Bar";
  
  public BarChartBuilder(FacesContext context)
  {
    super(context);
  }
  
  public String getName()
  {
    return NAME;
  }
  
  protected BarChart createChart(ChartRenderContext context)
  {
    return new BarChart();
  }
  
  protected void configureChart(ChartRenderContext context, BarChart chart)
  {
    super.configureChart(context, chart);
    
    chart.setZoom(true);
  }
}
