package org.pivot4j.analytics.datasource;

import java.io.Serializable;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.pivot4j.state.Configurable;

public class ConnectionInfo
  implements Configurable, Serializable
{
  private static final long serialVersionUID = 1613489385973603487L;
  private String catalogName;
  private String cubeName;
  
  public ConnectionInfo() {}
  
  public ConnectionInfo(String catalogName, String cubeName)
  {
    this.catalogName = catalogName;
    this.cubeName = cubeName;
  }
  
  public String getCubeName()
  {
    return this.cubeName;
  }
  
  public void setCubeName(String cubeName)
  {
    this.cubeName = cubeName;
  }
  
  public String getCatalogName()
  {
    return this.catalogName;
  }
  
  public void setCatalogName(String catalogName)
  {
    this.catalogName = catalogName;
  }
  
  public void saveSettings(HierarchicalConfiguration configuration)
  {
    if (configuration == null) {
      throw new IllegalArgumentException("Configuration object cannot be null.");
    }
    configuration.addProperty("catalog", this.catalogName);
    configuration.addProperty("cube", this.cubeName);
  }
  
  public void restoreSettings(HierarchicalConfiguration configuration)
  {
    if (configuration == null) {
      throw new IllegalArgumentException("Configuration object cannot be null.");
    }
    this.catalogName = configuration.getString("catalog");
    this.cubeName = configuration.getString("cube");
  }
  
  public String toString()
  {
    return new ToStringBuilder(this).append("cubeName", this.cubeName).append("catalogName", this.catalogName).toString();
  }
}
