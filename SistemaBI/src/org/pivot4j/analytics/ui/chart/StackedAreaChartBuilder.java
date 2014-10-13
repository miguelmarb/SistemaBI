package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;
import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.line.LineChart;

public class StackedAreaChartBuilder
  extends LineChartBuilder
{
  public static String NAME = "StackedArea";
  
  public StackedAreaChartBuilder(FacesContext context)
  {
    super(context);
  }
  
  public String getName()
  {
    return NAME;
  }
  
  protected void configureChart(ChartRenderContext context, LineChart chart)
  {
    super.configureChart(context, chart);
    
    chart.setFill(true);
    chart.setStacked(true);
    chart.setLegendPosition("n");
  }
}
