package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;
import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.bar.BarChart;

public class StackedBarChartBuilder
  extends BarChartBuilder
{
  public static String NAME = "StackedBar";
  
  public StackedBarChartBuilder(FacesContext context)
  {
    super(context);
  }
  
  public String getName()
  {
    return NAME;
  }
  
  protected BarChart createChart(ChartRenderContext context)
  {
    BarChart chart = super.createChart(context);
    
    chart.setStacked(true);
    
    return chart;
  }
}
