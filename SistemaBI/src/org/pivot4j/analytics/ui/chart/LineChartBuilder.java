package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;
import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.line.LineChart;

public class LineChartBuilder
  extends AbstractSeriesChartBuilder<LineChart>
{
  public static String NAME = "Line";
  
  public LineChartBuilder(FacesContext context)
  {
    super(context);
  }
  
  public String getName()
  {
    return NAME;
  }
  
  protected LineChart createChart(ChartRenderContext context)
  {
    return new LineChart();
  }
  
  protected void configureChart(ChartRenderContext context, LineChart chart)
  {
    super.configureChart(context, chart);
    
    chart.setZoom(true);
  }
}
