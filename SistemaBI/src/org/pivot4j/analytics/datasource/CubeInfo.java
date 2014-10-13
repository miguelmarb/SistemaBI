package org.pivot4j.analytics.datasource;

import java.io.Serializable;
import org.apache.commons.lang.NullArgumentException;

public class CubeInfo
  implements Serializable
{
  private static final long serialVersionUID = -2405346074869143874L;
  private String name;
  private String label;
  private String description;
  
  public CubeInfo(String name, String label, String description)
  {
    if (name == null) {
      throw new NullArgumentException("name");
    }
    this.name = name;
    if (label == null) {
      this.label = name;
    } else {
      this.label = label;
    }
    this.description = description;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public String getLabel()
  {
    return this.label;
  }
  
  public String getDescription()
  {
    return this.description;
  }
  
  public void setDescription(String description)
  {
    this.description = description;
  }
}
