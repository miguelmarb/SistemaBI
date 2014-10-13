package org.pivot4j.analytics.datasource;

import java.util.List;
import org.olap4j.OlapDataSource;

public abstract interface DataSourceManager
{
  public abstract List<CatalogInfo> getCatalogs();
  
  public abstract List<CubeInfo> getCubes(String paramString);
  
  public abstract OlapDataSource getDataSource(ConnectionInfo paramConnectionInfo);
}
