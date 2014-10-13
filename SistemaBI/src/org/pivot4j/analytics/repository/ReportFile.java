package org.pivot4j.analytics.repository;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public abstract interface ReportFile
{
  public static final String SEPARATOR = "/";
  
  public abstract Serializable getId();
  
  public abstract String getName();
  
  public abstract String getPath();
  
  public abstract String getExtension();
  
  public abstract ReportFile getParent()
    throws IOException;
  
  public abstract List<ReportFile> getAncestors()
    throws IOException;
  
  public abstract boolean isDirectory();
  
  public abstract boolean isRoot();
  
  public abstract Date getLastModifiedDate();
  
  public abstract long getSize();
}
