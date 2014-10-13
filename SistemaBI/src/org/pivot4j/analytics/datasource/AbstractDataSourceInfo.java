package org.pivot4j.analytics.datasource;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class AbstractDataSourceInfo
  implements DataSourceInfo
{
  private static final long serialVersionUID = -1308857219571095791L;
  private String name;
  private String description;
  
  public void saveSettings(HierarchicalConfiguration configuration)
  {
    if (configuration == null) {
      throw new NullArgumentException("configuration");
    }
    if (this.name != null) {
      configuration.setProperty("name", this.name);
    }
    if (this.description != null) {
      configuration.setProperty("description", this.description);
    }
  }
  
  public void restoreSettings(HierarchicalConfiguration configuration)
  {
    if (configuration == null) {
      throw new NullArgumentException("configuration");
    }
    this.name = configuration.getString("name");
    this.description = configuration.getString("description");
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void setName(String name)
  {
    this.name = name;
  }
  
  public String getDescription()
  {
    return this.description;
  }
  
  public void setDescription(String description)
  {
    this.description = description;
  }
  
  public int hashCode()
  {
    return new HashCodeBuilder().append(this.name).toHashCode();
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof AbstractDataSourceInfo)) {
      return false;
    }
    AbstractDataSourceInfo other = (AbstractDataSourceInfo)obj;
    
    return new EqualsBuilder().append(this.name, other.name).isEquals();
  }
  
  public String toString()
  {
    return new ToStringBuilder(this).append(this.name).append(this.description).build();
  }
}
