package org.pivot4j.analytics.ui.chart;

import javax.faces.component.UIComponent;
import org.pivot4j.ui.chart.ChartRenderCallback;

public abstract interface ChartBuilder
  extends ChartRenderCallback
{
  public abstract String getName();
  
  public abstract UIComponent getComponent();
  
  public abstract void setComponent(UIComponent paramUIComponent);
}
