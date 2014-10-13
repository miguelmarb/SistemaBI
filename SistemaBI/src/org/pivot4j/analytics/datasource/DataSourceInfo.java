package org.pivot4j.analytics.datasource;

import java.io.Serializable;
import org.pivot4j.state.Configurable;

public abstract interface DataSourceInfo
  extends Serializable, Configurable
{
  public abstract String getName();
  
  public abstract String getDescription();
}
