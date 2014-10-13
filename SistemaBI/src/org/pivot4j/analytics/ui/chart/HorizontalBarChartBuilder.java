package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;
import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.bar.BarChart;

public class HorizontalBarChartBuilder
  extends BarChartBuilder
{
  public static String NAME = "HorizontalBar";
  
  public HorizontalBarChartBuilder(FacesContext context)
  {
    super(context);
  }
  
  public String getName()
  {
    return NAME;
  }
  
  protected void configureChart(ChartRenderContext context, BarChart chart)
  {
    super.configureChart(context, chart);
    
    chart.setLegendPosition("e");
    chart.setOrientation("horizontal");
  }
}
