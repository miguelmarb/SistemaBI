package org.pivot4j.analytics.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.pivot4j.el.ExpressionContext;
import org.pivot4j.el.ExpressionEvaluator;
import org.pivot4j.el.ExpressionEvaluatorFactory;
import org.pivot4j.el.freemarker.FreeMarkerExpressionEvaluatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name="settings", eager=true)
@ApplicationScoped
public class Settings
{
  public static final String CONFIG_FILE = "pivot4j.config";
  public static final String APPLICATION_HOME = "pivot4j.home";
  private Logger logger;
  private File applicationHome;
  private HierarchicalConfiguration configuration;
  private String theme;
  private String editorTheme;
  private String resourcePrefix;
  private String viewParameterName;
  private String fileParameterName;
  private String pathParameterName;
  private String localeAttributeName;
  private SortedMap<String, String> availableThemes;
  
  public Settings()
  {
    this.logger = LoggerFactory.getLogger(getClass());
  }
  
  @PostConstruct
  protected void initialize()
  {
    if (this.logger.isInfoEnabled()) {
      this.logger.info("Reading configuration parameters.");
    }
    FacesContext context = FacesContext.getCurrentInstance();
    ExternalContext externalContext = context.getExternalContext();
    
    ProjectStage stage = context.getApplication().getProjectStage();
    
    String path = StringUtils.trimToNull(externalContext
      .getInitParameter("pivot4j.home"));
    if (path == null)
    {
      if (this.logger.isInfoEnabled()) {
        this.logger.info("Parameter 'applicationHome' is not set. Using the default path.");
      }
      path = System.getProperty("user.home") + File.separator + ".pivot4j";
    }
    else if (path.endsWith(File.separator))
    {
      path = path.substring(0, path.length() - File.separator.length());
    }
    if (this.logger.isInfoEnabled()) {
      this.logger.info("Using application home : {}", path);
    }
    this.applicationHome = new File(path);
    if (!this.applicationHome.exists()) {
      this.applicationHome.mkdirs();
    }
    InputStream in = null;
    try
    {
      String configPath = StringUtils.trimToNull(externalContext
        .getInitParameter("pivot4j.config"));
      if ((configPath == null) || (stage == ProjectStage.UnitTest))
      {
        configPath = path + File.separator + "pivot4j-config.xml";
        
        File configFile = new File(configPath);
        if ((!configFile.exists()) || (stage == ProjectStage.UnitTest))
        {
          String defaultConfig = "/WEB-INF/pivot4j-config.xml";
          if (this.logger.isInfoEnabled()) {
            this.logger.info("Config file does not exist. Using default : " + defaultConfig);
          }
          ServletContext servletContext = (ServletContext)externalContext.getContext();
          
          String location = servletContext.getRealPath(defaultConfig);
          if (location != null) {
            configFile = new File(location);
          }
        }
        if (!configFile.exists())
        {
          String msg = "Unable to read the default config : " + configFile;
          
          throw new FacesException(msg);
        }
        in = new FileInputStream(configFile);
      }
      else
      {
        URL url;
        if (configPath.startsWith("classpath:")) {
          url = new URL(null, configPath, new ClasspathStreamHandler());
        } else {
          url = new URL(configPath);
        }
        in = url.openStream();
        if (in == null)
        {
          String msg = "Unable to read config from URL : " + url;
          throw new FacesException(msg);
        }
      }
      this.configuration = readConfiguration(context, in);
    }
    catch (IOException e)
    {
      String msg = "Failed to read application config : " + e;
      throw new FacesException(msg, e);
    }
    catch (ConfigurationException e)
    {
      String msg = "Invalid application config : " + e;
      throw new FacesException(msg, e);
    }
    finally
    {
      IOUtils.closeQuietly(in);
    }
    if (this.logger.isInfoEnabled()) {
      this.logger.info("Pivot4J Analytics has been initialized successfully.");
    }
    this.configuration.addConfigurationListener(new ConfigurationListener()
    {
      public void configurationChanged(ConfigurationEvent event)
      {
        Settings.this.onConfigurationChanged(event);
      }
    });
  }
  
  protected HierarchicalConfiguration readConfiguration(FacesContext context, InputStream in)
    throws ConfigurationException, IOException
  {
    ExpressionEvaluatorFactory factory = new FreeMarkerExpressionEvaluatorFactory();
    ExpressionEvaluator evaluator = factory.createEvaluator();
    
    String source = IOUtils.toString(in);
    source = (String)evaluator.evaluate(source, createELContext(context));
    
    XMLConfiguration config = new XMLConfiguration();
    config.load(new StringReader(source));
    
    return config;
  }
  
  protected ExpressionContext createELContext(FacesContext context)
  {
    ExpressionContext elContext = new ExpressionContext();
    
    elContext.put("FS", File.separator);
    
    elContext.put("userHome", System.getProperty("user.dir"));
    elContext.put("appHome", this.applicationHome.getPath());
    

    ServletContext servletContext = (ServletContext)context.getExternalContext().getContext();
    String webRoot = servletContext.getRealPath("/WEB-INF");
    if (webRoot != null) {
      elContext.put("webRoot", webRoot);
    }
    return elContext;
  }
  
  protected void onConfigurationChanged(ConfigurationEvent event)
  {
    this.editorTheme = null;
    
    this.resourcePrefix = null;
    this.viewParameterName = null;
    this.localeAttributeName = null;
  }
  
  public File getApplicationHome()
  {
    return this.applicationHome;
  }
  
  public HierarchicalConfiguration getConfiguration()
  {
    return this.configuration;
  }
  
  public String getTheme()
  {
    if (this.theme == null) {
      this.theme = this.configuration.getString("appearances.ui-theme.default", "redmond").trim();
    }
    return this.theme;
  }
  
  public String getEditorTheme()
  {
    if (this.editorTheme == null) {
      this.editorTheme = StringUtils.trimToNull(this.configuration
        .getString("appearances.editor-theme"));
    }
    return this.editorTheme;
  }
  
  public SortedMap<String, String> getAvailableThemes()
  {
    synchronized (this)
    {
      if (this.availableThemes == null)
      {
        this.availableThemes = new TreeMap();
        

        List<HierarchicalConfiguration> configurations = this.configuration.configurationsAt("appearances.ui-theme.available-themes.theme");
        for (HierarchicalConfiguration config : configurations)
        {
          String name = config.getString("[@name]");
          this.availableThemes.put(StringUtils.capitalize(name), name);
        }
      }
    }
    return this.availableThemes;
  }
  
  public String getResourcePrefix()
  {
    if (this.resourcePrefix == null) {
      this.resourcePrefix = StringUtils.trimToEmpty(this.configuration
        .getString("web.resource-prefix"));
    }
    return this.resourcePrefix;
  }
  
  public String getViewParameterName()
  {
    if (this.viewParameterName == null) {
      this.viewParameterName = this.configuration.getString("web.view-parameter", "viewId").trim();
    }
    return this.viewParameterName;
  }
  
  public String getFileParameterName()
  {
    if (this.fileParameterName == null) {
      this.fileParameterName = this.configuration.getString("web.file-parameter", "fileId").trim();
    }
    return this.fileParameterName;
  }
  
  public String getPathParameterName()
  {
    if (this.pathParameterName == null) {
      this.pathParameterName = this.configuration.getString("web.path-parameter", "path").trim();
    }
    return this.pathParameterName;
  }
  
  public String getLocaleAttributeName()
  {
    if (this.localeAttributeName == null) {
      this.localeAttributeName = this.configuration.getString("web.locale-attribute", "locale").trim();
    }
    return this.localeAttributeName;
  }
  
  static class ClasspathStreamHandler
    extends URLStreamHandler
  {
    protected URLConnection openConnection(URL u)
      throws IOException
    {
      URL resourceUrl = getClass().getClassLoader().getResource(u
        .getPath());
      if (resourceUrl == null) {
        return null;
      }
      return resourceUrl.openConnection();
    }
  }
}
