package org.pivot4j.analytics.ui.chart;

import java.io.Serializable;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.pivot4j.ui.chart.ChartRenderer;

public class DefaultChartRenderer
  extends ChartRenderer
{
  private String chartName;
  private int width;
  private int height;
  private Position legendPosition;
  
  public DefaultChartRenderer()
  {
    this.width = 0;
    
    this.height = 300;
    
    this.legendPosition = Position.w;
  }
  
  public String getChartName()
  {
    return this.chartName;
  }
  
  public void setChartName(String chartName)
  {
    this.chartName = chartName;
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
  
  public Position getLegendPosition()
  {
    return this.legendPosition;
  }
  
  public void setLegendPosition(Position legendPosition)
  {
    this.legendPosition = legendPosition;
  }
  
  public Serializable saveState()
  {
    Serializable[] states = new Serializable[5];
    
    int index = 0;
    
    states[(index++)] = super.saveState();
    states[(index++)] = this.chartName;
    states[(index++)] = Integer.valueOf(this.width);
    states[(index++)] = Integer.valueOf(this.height);
    
    String position = null;
    if (this.legendPosition != null) {
      position = this.legendPosition.name();
    }
    states[(index++)] = position;
    
    return states;
  }
  
  public void restoreState(Serializable state)
  {
    Serializable[] states = (Serializable[])state;
    
    int index = 0;
    
    super.restoreState(states[(index++)]);
    
    this.chartName = ((String)states[(index++)]);
    this.width = ((Integer)states[(index++)]).intValue();
    this.height = ((Integer)states[(index++)]).intValue();
    
    String position = (String)states[(index++)];
    if (position == null) {
      this.legendPosition = null;
    } else {
      this.legendPosition = Position.valueOf(position);
    }
  }
  
  public void saveSettings(HierarchicalConfiguration configuration)
  {
    super.saveSettings(configuration);
    
    configuration.addProperty("[@type]", this.chartName);
    configuration.addProperty("dimension[@width]", Integer.valueOf(this.width));
    configuration.addProperty("dimension[@height]", Integer.valueOf(this.height));
    if (this.legendPosition != null) {
      configuration.addProperty("legend.position", this.legendPosition.name());
    }
  }
  
  public void restoreSettings(HierarchicalConfiguration configuration)
  {
    super.restoreSettings(configuration);
    
    this.chartName = configuration.getString("[@type]");
    this.width = configuration.getInt("dimension[@width]", 0);
    this.height = configuration.getInt("dimension[@height]", 0);
    
    String position = configuration.getString("legend.position", Position.w
      .name());
    
    this.legendPosition = Position.valueOf(position);
  }
  
  public static enum Position
  {
    n,  w,  s,  e;
    
    private Position() {}
  }
}
