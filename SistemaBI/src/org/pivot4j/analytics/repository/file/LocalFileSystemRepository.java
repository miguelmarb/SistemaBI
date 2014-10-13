package org.pivot4j.analytics.repository.file;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.FacesException;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.repository.AbstractFileSystemRepository;
import org.pivot4j.analytics.repository.ReportContent;
import org.pivot4j.analytics.repository.ReportFile;
import org.pivot4j.analytics.repository.RepositoryFileComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name="reportRepository")
@ApplicationScoped
public class LocalFileSystemRepository
  extends AbstractFileSystemRepository
{
  @ManagedProperty("#{settings}")
  private Settings settings;
  private Logger log = LoggerFactory.getLogger(getClass());
  private LocalFile root;
  
  @PostConstruct
  protected void initialize()
  {
    String path = getRootPath();
    if (this.log.isInfoEnabled()) {
      this.log.info("Root repository path : {}", path);
    }
    try
    {
      File file = new File(path);
      if ((!file.exists()) && (!file.mkdirs())) {
        throw new IOException("Creating repository direcoty failed with unknown reason : " + path);
      }
      this.root = new LocalFile(file, file);
    }
    catch (IOException e)
    {
      throw new FacesException(e);
    }
  }
  
  protected String getRootPath()
  {
    File home = this.settings.getApplicationHome();
    return home.getPath() + File.separator + "repository";
  }
  
  public ReportFile getRoot()
  {
    return this.root;
  }
  
  public boolean exists(String path)
    throws IOException
  {
    return getSystemFile(path).exists();
  }
  
  public boolean fileWithIdExists(String id)
    throws IOException
  {
    return exists(id);
  }
  
  public ReportFile getFile(String path)
    throws IOException
  {
    File file = getSystemFile(path);
    if (!file.exists()) {
      return null;
    }
    return new LocalFile(file, this.root.getRoot());
  }
  
  public ReportFile getFileById(String id)
    throws IOException
  {
    return getFile(id);
  }
  
  public List<ReportFile> getFiles(ReportFile parent)
    throws IOException
  {
    if (parent == null) {
      throw new NullArgumentException("parent");
    }
    LocalFile directory = getLocalFile(parent);
    
    File[] children = directory.getFile().listFiles();
    if (children == null) {
      return Collections.emptyList();
    }
    List<ReportFile> files = new ArrayList(children.length);
    for (File child : children) {
      files.add(new LocalFile(child, this.root.getRoot()));
    }
    Collections.sort(files, new RepositoryFileComparator());
    
    return files;
  }
  
  public ReportFile createDirectory(ReportFile parent, String name)
    throws IOException
  {
    if (parent == null) {
      throw new NullArgumentException("parent");
    }
    if (name == null) {
      throw new NullArgumentException("name");
    }
    LocalFile directory = getLocalFile(parent);
    
    String path = directory.getFile().getCanonicalPath() + File.separator + name;
    

    File file = new File(path);
    if (!file.mkdir()) {
      throw new IOException("Creating a direcoty failed with unknown reason : " + path);
    }
    return new LocalFile(file, this.root.getFile());
  }
  
  public ReportFile createFile(ReportFile parent, String name, ReportContent content)
    throws IOException, ConfigurationException
  {
    if (parent == null) {
      throw new NullArgumentException("parent");
    }
    if (name == null) {
      throw new NullArgumentException("name");
    }
    if (content == null) {
      throw new NullArgumentException("content");
    }
    LocalFile directory = getLocalFile(parent);
    
    String path = directory.getFile().getCanonicalPath() + File.separator + name;
    

    File file = new File(path);
    
    ReportFile localFile = new LocalFile(file, this.root.getFile());
    
    setReportContent(localFile, content);
    
    return localFile;
  }
  
  public InputStream readContent(ReportFile file)
    throws IOException
  {
    if (file == null) {
      throw new NullArgumentException("file");
    }
    return new FileInputStream(getLocalFile(file).getFile());
  }
  
  public void setReportContent(ReportFile file, ReportContent content)
    throws IOException, ConfigurationException
  {
    if (file == null) {
      throw new NullArgumentException("file");
    }
    if (content == null) {
      throw new NullArgumentException("content");
    }
    LocalFile localFile = getLocalFile(file);
    
    OutputStream out = null;
    try
    {
      out = new BufferedOutputStream(new FileOutputStream(localFile.getFile(), false));
      
      content.write(out);
      
      out.flush();
    }
    finally
    {
      IOUtils.closeQuietly(out);
    }
  }
  
  public ReportFile renameFile(ReportFile file, String newName)
    throws IOException
  {
    if (file == null) {
      throw new NullArgumentException("file");
    }
    if (newName == null) {
      throw new NullArgumentException("newName");
    }
    File localFile = getLocalFile(file).getFile();
    
    File newFile = new File(localFile.getParent() + File.separator + newName);
    if (!localFile.renameTo(newFile)) {
      throw new IOException("Renaming a file failed with unknown reason : " + newFile.getPath());
    }
    return new LocalFile(newFile, this.root.getFile());
  }
  
  public void deleteFile(ReportFile file)
    throws IOException
  {
    if (file == null) {
      throw new NullArgumentException("file");
    }
    File localFile = getLocalFile(file).getFile();
    if (localFile.isDirectory()) {
      FileUtils.deleteDirectory(localFile);
    } else if (!localFile.delete()) {
      throw new IOException("Deleting a file failed with unknown reason : " + localFile.getPath());
    }
  }
  
  protected File getSystemFile(String path)
    throws IOException
  {
    if (path == null) {
      throw new NullArgumentException("file");
    }
    if (path.equals("/")) {
      return this.root.getFile();
    }
    String filePath = this.root.getFile().getCanonicalPath() + StringUtils.replaceChars(path, "/", File.separator);
    

    return new File(filePath);
  }
  
  protected LocalFile getLocalFile(ReportFile file)
    throws IOException
  {
    LocalFile localFile;
    if ((file instanceof LocalFile)) {
      localFile = (LocalFile)file;
    } else {
      localFile = (LocalFile)getFile(file.getPath());
    }
    return localFile;
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
