package org.pivot4j.analytics.repository.file;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.analytics.repository.AbstractReportFile;
import org.pivot4j.analytics.repository.ReportFile;

public class LocalFile
  extends AbstractReportFile
{
  private File root;
  private File file;
  private String path;
  
  public LocalFile(File file, File root)
    throws IOException
  {
    if (file == null) {
      throw new NullArgumentException("file");
    }
    if (root == null) {
      throw new NullArgumentException("root");
    }
    String rootPath = root.getCanonicalPath();
    String filePath = file.getCanonicalPath();
    if (!filePath.startsWith(rootPath)) {
      throw new IllegalArgumentException("The specified file path does not begin with the root path.");
    }
    this.file = file;
    this.root = root;
    
    this.path = filePath.substring(rootPath.length());
    if (this.path.length() == 0) {
      this.path = "/";
    } else {
      this.path = StringUtils.replaceChars(this.path, File.separator, "/");
    }
  }
  
  protected File getRoot()
  {
    return this.root;
  }
  
  public File getFile()
  {
    return this.file;
  }
  
  public String getName()
  {
    return this.file.getName();
  }
  
  public String getPath()
  {
    return this.path;
  }
  
  public ReportFile getParent()
    throws IOException
  {
    if (isRoot()) {
      return null;
    }
    File parent = this.file.getParentFile();
    if (parent == null) {
      return null;
    }
    return new LocalFile(parent, this.root);
  }
  
  public boolean isDirectory()
  {
    return this.file.isDirectory();
  }
  
  public boolean isRoot()
  {
    return this.root.equals(this.file);
  }
  
  public Date getLastModifiedDate()
  {
    return new Date(this.file.lastModified());
  }
  
  public long getSize()
  {
    return this.file.length();
  }
}
