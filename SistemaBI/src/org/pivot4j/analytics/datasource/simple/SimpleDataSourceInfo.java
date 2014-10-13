package org.pivot4j.analytics.datasource.simple;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.pivot4j.analytics.datasource.AbstractDataSourceInfo;

public class SimpleDataSourceInfo
  extends AbstractDataSourceInfo
{
  private static final long serialVersionUID = -513787516897344513L;
  private String url;
  private String userName;
  private String password;
  private String driverClass;
  private Properties properties;
  
  public void saveSettings(HierarchicalConfiguration configuration)
  {
    super.saveSettings(configuration);
    if (this.userName != null) {
      configuration.setProperty("user", this.userName);
    }
    if (this.password != null) {
      configuration.setProperty("password", this.password);
    }
    if (this.url != null) {
      configuration.setProperty("url", this.url);
    }
    if (this.driverClass != null) {
      configuration.setProperty("driverClass", this.url);
    }
    if (this.properties != null)
    {
      int index = 0;
      
      Enumeration<Object> en = this.properties.elements();
      while (en.hasMoreElements())
      {
        String prefix = String.format("properties.property(%s)", new Object[] { Integer.valueOf(index) });
        
        Object key = en.nextElement();
        Object value = this.properties.get(key);
        
        configuration.setProperty(prefix + "[@name]", key);
        configuration.setProperty(prefix, value);
        
        index++;
      }
    }
  }
  
  public void restoreSettings(HierarchicalConfiguration configuration)
  {
    super.restoreSettings(configuration);
    

    SubnodeConfiguration connectionConfig = configuration.configurationAt("connection-info");
    
    this.url = connectionConfig.getString("url");
    this.driverClass = connectionConfig.getString("driverClass");
    this.userName = connectionConfig.getString("user");
    this.password = connectionConfig.getString("password");
    this.properties = new Properties();
    

    List<HierarchicalConfiguration> propertiesConfig = connectionConfig.configurationsAt("properties.property");
    for (HierarchicalConfiguration propertyConfig : propertiesConfig)
    {
      String key = propertyConfig.getString("[@name]");
      String value = propertyConfig.getString("");
      
      this.properties.put(key, value);
    }
  }
  
  public String getUrl()
  {
    return this.url;
  }
  
  public void setUrl(String url)
  {
    this.url = url;
  }
  
  public String getUserName()
  {
    return this.userName;
  }
  
  public void setUserName(String userName)
  {
    this.userName = userName;
  }
  
  public String getPassword()
  {
    return this.password;
  }
  
  public void setPassword(String password)
  {
    this.password = password;
  }
  
  public String getDriverClass()
  {
    return this.driverClass;
  }
  
  public void setDriverClass(String driverClass)
  {
    this.driverClass = driverClass;
  }
  
  public Properties getProperties()
  {
    return this.properties;
  }
  
  public void setProperties(Properties properties)
  {
    this.properties = properties;
  }
  
  public int hashCode()
  {
    return new HashCodeBuilder().append(getName()).append(this.driverClass).append(this.url).toHashCode();
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SimpleDataSourceInfo)) {
      return false;
    }
    SimpleDataSourceInfo other = (SimpleDataSourceInfo)obj;
    


    return new EqualsBuilder().append(getName(), other.getName()).append(this.driverClass, other.driverClass).append(this.url, other.url).isEquals();
  }
}
