package org.pivot4j.analytics.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;

public abstract interface ReportRepository
{
  public abstract ReportFile getRoot()
    throws IOException;
  
  public abstract ReportFile getFile(String paramString)
    throws IOException;
  
  public abstract ReportFile getFileById(String paramString)
    throws IOException;
  
  public abstract boolean exists(String paramString)
    throws IOException;
  
  public abstract boolean fileWithIdExists(String paramString)
    throws IOException;
  
  public abstract List<ReportFile> getFiles(ReportFile paramReportFile)
    throws IOException;
  
  public abstract List<ReportFile> getFiles(ReportFile paramReportFile, RepositoryFileFilter paramRepositoryFileFilter)
    throws IOException;
  
  public abstract ReportFile createDirectory(ReportFile paramReportFile, String paramString)
    throws IOException;
  
  public abstract ReportFile createFile(ReportFile paramReportFile, String paramString, ReportContent paramReportContent)
    throws IOException, ConfigurationException;
  
  public abstract ReportFile renameFile(ReportFile paramReportFile, String paramString)
    throws IOException;
  
  public abstract void deleteFile(ReportFile paramReportFile)
    throws IOException;
  
  public abstract ReportContent getReportContent(ReportFile paramReportFile)
    throws IOException, ConfigurationException;
  
  public abstract void setReportContent(ReportFile paramReportFile, ReportContent paramReportContent)
    throws IOException, ConfigurationException;
  
  public abstract InputStream readContent(ReportFile paramReportFile)
    throws IOException;
}
