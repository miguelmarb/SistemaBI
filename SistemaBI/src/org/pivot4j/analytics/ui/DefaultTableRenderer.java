package org.pivot4j.analytics.ui;

import java.io.Serializable;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.pivot4j.ui.table.TableRenderer;

public class DefaultTableRenderer
  extends TableRenderer
{
  private boolean visible = true;
  
  public boolean isVisible()
  {
    return this.visible;
  }
  
  public void setVisible(boolean visible)
  {
    this.visible = visible;
  }
  
  public Serializable saveState()
  {
    Serializable[] states = new Serializable[2];
    
    int index = 0;
    
    states[(index++)] = super.saveState();
    states[(index++)] = Boolean.valueOf(this.visible);
    
    return states;
  }
  
  public void restoreState(Serializable state)
  {
    Serializable[] states = (Serializable[])state;
    
    int index = 0;
    
    super.restoreState(states[(index++)]);
    
    this.visible = ((Boolean)states[(index++)]).booleanValue();
  }
  
  public void saveSettings(HierarchicalConfiguration configuration)
  {
    super.saveSettings(configuration);
    
    configuration.addProperty("[@visible]", Boolean.valueOf(this.visible));
  }
  
  public void restoreSettings(HierarchicalConfiguration configuration)
  {
    super.restoreSettings(configuration);
    
    this.visible = configuration.getBoolean("[@visible]", true);
  }
}
