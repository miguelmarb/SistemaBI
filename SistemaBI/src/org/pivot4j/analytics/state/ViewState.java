package org.pivot4j.analytics.state;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.repository.ReportFile;
import org.pivot4j.analytics.ui.LayoutRegion;

public class ViewState
{
  private String id;
  private String name;
  private boolean dirty = false;
  private ReportFile file;
  private boolean readOnly = false;
  private Date lastActive = new Date();
  private ConnectionInfo connectionInfo;
  private PivotModel model;
  private Serializable rendererState;
  private Serializable chartState;
  private Map<String, Object> parameters;
  private Map<LayoutRegion, Boolean> layoutRegions;
  
  public ViewState(String id, String name)
  {
    if (id == null) {
      throw new NullArgumentException("id");
    }
    if (name == null) {
      throw new NullArgumentException("name");
    }
    this.id = id;
    this.name = name;
    this.layoutRegions = new HashMap();
  }
  
  public ViewState(String id, String name, ConnectionInfo connectionInfo, PivotModel model, ReportFile file)
  {
    if (id == null) {
      throw new NullArgumentException("id");
    }
    if (name == null) {
      throw new NullArgumentException("name");
    }
    this.id = id;
    this.name = name;
    this.connectionInfo = connectionInfo;
    this.model = model;
    this.file = file;
    this.layoutRegions = new HashMap();
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void setName(String name)
  {
    if (name == null) {
      throw new NullArgumentException("name");
    }
    this.name = name;
  }
  
  public ReportFile getFile()
  {
    return this.file;
  }
  
  public void setFile(ReportFile file)
  {
    this.file = file;
  }
  
  public Date getLastActive()
  {
    return this.lastActive;
  }
  
  public ConnectionInfo getConnectionInfo()
  {
    return this.connectionInfo;
  }
  
  public void setConnectionInfo(ConnectionInfo connectionInfo)
  {
    this.connectionInfo = connectionInfo;
  }
  
  public boolean isReadOnly()
  {
    return this.readOnly;
  }
  
  public void setReadOnly(boolean readOnly)
  {
    this.readOnly = readOnly;
  }
  
  public PivotModel getModel()
  {
    return this.model;
  }
  
  public void setModel(PivotModel model)
  {
    this.model = model;
  }
  
  public Serializable getRendererState()
  {
    return this.rendererState;
  }
  
  public void setRendererState(Serializable rendererState)
  {
    this.rendererState = rendererState;
  }
  
  public Serializable getChartState()
  {
    return this.chartState;
  }
  
  public void setChartState(Serializable chartState)
  {
    this.chartState = chartState;
  }
  
  public Map<String, Object> getParameters()
  {
    return this.parameters;
  }
  
  public void setParameters(Map<String, Object> parameters)
  {
    this.parameters = parameters;
  }
  
  public Map<LayoutRegion, Boolean> getLayoutRegions()
  {
    return this.layoutRegions;
  }
  
  public boolean isRegionVisible(LayoutRegion region)
  {
    if (region == null) {
      throw new NullArgumentException("region");
    }
    return !Boolean.FALSE.equals(this.layoutRegions.get(region));
  }
  
  public void setRegionVisible(LayoutRegion region, boolean visible)
  {
    if (region == null) {
      throw new NullArgumentException("region");
    }
    this.layoutRegions.put(region, Boolean.valueOf(visible));
  }
  
  public boolean isDirty()
  {
    return this.dirty;
  }
  
  public void setDirty(boolean dirty)
  {
    this.dirty = dirty;
  }
  
  public void update()
  {
    this.lastActive = new Date();
  }
  
  public String toString()
  {
    return new ToStringBuilder(this).append("id", this.id).append("name", this.name).append("connectionInfo", this.connectionInfo).toString();
  }
}
