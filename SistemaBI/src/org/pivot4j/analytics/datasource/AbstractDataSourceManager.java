package org.pivot4j.analytics.datasource;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedProperty;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapDataSource;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.datasource.CloseableDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataSourceManager<T extends DataSourceInfo>
  implements DataSourceManager
{
  private Logger logger = LoggerFactory.getLogger(getClass());
  @ManagedProperty("#{settings}")
  private Settings settings;
  private List<T> definitions = new LinkedList();
  private Map<T, OlapDataSource> dataSources = new HashMap();
  
  @PostConstruct
  protected void initialize()
  {
    if (this.logger.isInfoEnabled()) {
      this.logger.info("Initializing data source manager.");
    }
    registerDefinitions();
  }
  
  @PreDestroy
  protected void destroy()
  {
    if (this.logger.isInfoEnabled()) {
      this.logger.info("Destroying data source manager.");
    }
    List<T> defs = new LinkedList(this.definitions);
    for (T definition : defs) {
      unregisterDefinition(definition);
    }
    this.dataSources.clear();
    this.definitions.clear();
  }
  
  protected Logger getLogger()
  {
    return this.logger;
  }
  
  protected void registerDefinitions()
  {
    List<HierarchicalConfiguration> configurations = this.settings.getConfiguration().configurationsAt("datasources.datasource");
    for (HierarchicalConfiguration configuration : configurations) {
      registerDefinition(configuration);
    }
  }
  
  protected void registerDefinition(HierarchicalConfiguration configuration)
  {
    T definition = createDataSourceDefinition(configuration);
    if (definition != null) {
      registerDefinition(definition);
    }
  }
  
  protected void registerDefinition(T definition)
  {
    if (definition == null) {
      throw new NullArgumentException("definition");
    }
    synchronized (this)
    {
      if (this.definitions.contains(definition)) {
        unregisterDefinition(definition);
      }
      this.definitions.add(definition);
    }
  }
  
  protected void unregisterDefinition(T definition)
  {
    if (definition == null) {
      throw new NullArgumentException("definition");
    }
    synchronized (this)
    {
      if (this.logger.isInfoEnabled()) {
        this.logger.info("Disposing data source : {}", definition);
      }
      OlapDataSource dataSource = (OlapDataSource)this.dataSources.get(definition);
      if (dataSource != null)
      {
        if ((dataSource instanceof CloseableDataSource))
        {
          CloseableDataSource closeable = (CloseableDataSource)dataSource;
          try
          {
            closeable.close();
          }
          catch (SQLException e)
          {
            if (this.logger.isErrorEnabled()) {
              this.logger.error("Failed to close data source : {}", definition, e);
            }
          }
        }
        this.dataSources.remove(definition);
      }
      this.definitions.remove(definition);
    }
  }
  
  protected abstract T createDataSourceDefinition(HierarchicalConfiguration paramHierarchicalConfiguration);
  
  protected abstract OlapDataSource createDataSource(T paramT);
  
  protected OlapDataSource getDataSource(T definition)
  {
    synchronized (this)
    {
      if (!this.dataSources.containsKey(definition))
      {
        if (this.logger.isInfoEnabled()) {
          this.logger.info("Registering data source : {}", definition);
        }
        OlapDataSource dataSource = createDataSource(definition);
        this.dataSources.put(definition, dataSource);
      }
    }
    return (OlapDataSource)this.dataSources.get(definition);
  }
  
  protected T getDefinition(ConnectionInfo connectionInfo)
  {
    if (connectionInfo == null) {
      throw new NullArgumentException("connectionInfo");
    }
    T definition = null;
    if (connectionInfo.getCatalogName() == null)
    {
      if (!this.definitions.isEmpty())
      {
        definition = this.definitions.get(0);
        connectionInfo.setCatalogName(definition.getName());
      }
    }
    else {
      synchronized (this)
      {
        for (T def : this.definitions) {
          if (connectionInfo.getCatalogName().equals(def.getName()))
          {
            definition = def;
            break;
          }
        }
      }
    }
    return definition;
  }
  
  protected T getDefinition(String name)
  {
    if (name == null) {
      throw new NullArgumentException("name");
    }
    T definition = null;
    synchronized (this)
    {
      for (T def : this.definitions) {
        if (name.equals(def.getName()))
        {
          definition = def;
          break;
        }
      }
    }
    return definition;
  }
  
  public OlapDataSource getDataSource(ConnectionInfo connectionInfo)
  {
    OlapDataSource dataSource = null;
    
    T definition = getDefinition(connectionInfo);
    if (definition != null) {
      dataSource = getDataSource(definition);
    }
    return dataSource;
  }
  
  protected List<T> getDefinitions()
  {
    return Collections.unmodifiableList(this.definitions);
  }
  
  public Settings getSettings()
  {
    return this.settings;
  }
  
  public void setSettings(Settings settings)
  {
    this.settings = settings;
  }
}
