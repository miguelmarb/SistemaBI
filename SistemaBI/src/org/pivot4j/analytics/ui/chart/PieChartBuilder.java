package org.pivot4j.analytics.ui.chart;

import javax.faces.context.FacesContext;
import org.pivot4j.ui.chart.ChartRenderContext;
import org.primefaces.component.chart.pie.PieChart;
import org.primefaces.model.chart.PieChartModel;

public class PieChartBuilder
  extends AbstractChartBuilder<PieChart, PieChartModel>
{
  public static String NAME = "Pie";
  
  public PieChartBuilder(FacesContext context)
  {
    super(context);
  }
  
  public String getName()
  {
    return NAME;
  }
  
  protected PieChart createChart(ChartRenderContext context)
  {
    return new PieChart();
  }
  
  protected void configureChart(ChartRenderContext context, PieChart chart)
  {
    super.configureChart(context, chart);
    
    chart.setShowDataLabels(true);
    chart.setDataFormat("value");
    chart.setShadow(true);
  }
  
  protected PieChartModel createModel(ChartRenderContext context)
  {
    return new PieChartModel();
  }
  
  public void renderContent(ChartRenderContext context, String label, Double value)
  {
    ((PieChartModel)getModel()).set(label, value);
  }
}
