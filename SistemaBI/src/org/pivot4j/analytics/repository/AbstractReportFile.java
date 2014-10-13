package org.pivot4j.analytics.repository;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class AbstractReportFile
  implements ReportFile
{
  public Serializable getId()
  {
    return getPath();
  }
  
  public List<ReportFile> getAncestors()
    throws IOException
  {
    List<ReportFile> ancestors = new ArrayList();
    
    ReportFile parent = this;
    while ((parent = parent.getParent()) != null) {
      ancestors.add(parent);
    }
    return ancestors;
  }
  
  public boolean isRoot()
  {
    try
    {
      return getParent() == null;
    }
    catch (IOException e)
    {
      throw new UnhandledException(e);
    }
  }
  
  public String getExtension()
  {
    String name = getName();
    
    int index = name.lastIndexOf('.');
    if (index != -1) {
      return name.substring(index + 1);
    }
    return null;
  }
  
  public int hashCode()
  {
    return new HashCodeBuilder().append(getPath()).build().intValue();
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof ReportFile)) {
      return false;
    }
    ReportFile other = (ReportFile)obj;
    
    return new EqualsBuilder().append(getClass(), other.getClass())
      .append(getPath(), other.getPath()).build().booleanValue();
  }
  
  public String toString()
  {
    return getPath();
  }
}
