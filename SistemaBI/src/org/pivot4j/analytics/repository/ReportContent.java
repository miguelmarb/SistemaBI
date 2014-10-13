package org.pivot4j.analytics.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.MergeCombiner;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapDataSource;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.datasource.DataSourceManager;
import org.pivot4j.analytics.state.ViewState;
import org.pivot4j.analytics.ui.DefaultTableRenderer;
import org.pivot4j.analytics.ui.LayoutRegion;
import org.pivot4j.analytics.ui.chart.DefaultChartRenderer;
import org.pivot4j.el.ExpressionContext;
import org.pivot4j.impl.PivotModelImpl;

public class ReportContent
  implements Serializable
{
  private static final long serialVersionUID = 8261947657917338352L;
  private transient HierarchicalConfiguration configuration;
  
  public ReportContent(ViewState state)
  {
    if (state == null) {
      throw new NullArgumentException("state");
    }
    this.configuration = createConfiguration();
    
    ConnectionInfo connectionInfo = state.getConnectionInfo();
    if (connectionInfo != null)
    {
      this.configuration.addProperty("connection", "");
      connectionInfo.saveSettings(this.configuration.configurationAt("connection", true));
    }
    PivotModel model = state.getModel();
    if (model != null)
    {
      this.configuration.addProperty("model", "");
      model.saveSettings(this.configuration.configurationAt("model", true));
    }
    if (state.getRendererState() != null)
    {
      DefaultTableRenderer renderer = new DefaultTableRenderer();
      
      renderer.restoreState(state.getRendererState());
      
      this.configuration.addProperty("render", "");
      renderer.saveSettings(this.configuration.configurationAt("render"));
    }
    if (state.getChartState() != null)
    {
      DefaultChartRenderer renderer = new DefaultChartRenderer();
      
      renderer.restoreState(state.getChartState());
      
      this.configuration.addProperty("chart", "");
      renderer.saveSettings(this.configuration.configurationAt("chart"));
    }
    Map<LayoutRegion, Boolean> regions = state.getLayoutRegions();
    
    this.configuration.addProperty("views", "");
    
    HierarchicalConfiguration views = this.configuration.configurationAt("views", true);
    

    int index = 0;
    
    MessageFormat mf = new MessageFormat("view({0})[@{1}]");
    for (LayoutRegion region : regions.keySet())
    {
      Boolean visibility = (Boolean)regions.get(region);
      if (visibility == null) {
        visibility = Boolean.valueOf(false);
      }
      views.addProperty(mf
        .format(new String[] {Integer.toString(index), "name" }), region
        .name());
      views.addProperty(mf.format(new String[] { Integer.toString(index), "visible" }), visibility
        .toString());
      
      index++;
    }
  }
  
  public ReportContent(InputStream in)
    throws ConfigurationException
  {
    if (in == null) {
      throw new NullArgumentException("in");
    }
    FileConfiguration config = (FileConfiguration)createConfiguration();
    config.load(in);
    
    this.configuration = ((HierarchicalConfiguration)config);
  }
  
  ReportContent() {}
  
  public void write(OutputStream out)
    throws ConfigurationException
  {
    if (out == null) {
      throw new NullArgumentException("out");
    }
    FileConfiguration config = (FileConfiguration)this.configuration;
    config.save(out);
  }
  
  protected HierarchicalConfiguration createConfiguration()
  {
    XMLConfiguration config = new XMLConfiguration();
    
    config.setRootElementName("report");
    config.setDelimiterParsingDisabled(true);
    
    return config;
  }
  
  public ViewState read(ViewState state, DataSourceManager manager, HierarchicalConfiguration defaultSettings)
    throws ConfigurationException, DataSourceNotFoundException
  {
    if (state == null) {
      throw new NullArgumentException("state");
    }
    if (manager == null) {
      throw new NullArgumentException("manager");
    }
    ConnectionInfo connectionInfo = new ConnectionInfo();
    try
    {
      connectionInfo.restoreSettings(this.configuration
        .configurationAt("connection"));
    }
    catch (IllegalArgumentException e) {}
    state.setConnectionInfo(connectionInfo);
    
    OlapDataSource dataSource = manager.getDataSource(connectionInfo);
    if (dataSource == null) {
      throw new DataSourceNotFoundException(connectionInfo);
    }
    CombinedConfiguration mergedSettings = new CombinedConfiguration();
    mergedSettings.setNodeCombiner(new MergeCombiner());
    if (defaultSettings != null) {
      mergedSettings.addConfiguration(defaultSettings);
    }
    mergedSettings.addConfiguration(this.configuration);
    
    PivotModel model = new PivotModelImpl(dataSource);
    try
    {
      model.restoreSettings(mergedSettings.configurationAt("model"));
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    Map<String, Object> parameters = state.getParameters();
    if (parameters == null) {
      parameters = Collections.emptyMap();
    }
    model.getExpressionContext().put("parameters", parameters);
    
    state.setModel(model);
    try
    {
      DefaultTableRenderer renderer = new DefaultTableRenderer();
      
      renderer.restoreSettings(mergedSettings.configurationAt("render"));
      
      state.setRendererState(renderer.saveState());
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    try
    {
      DefaultChartRenderer renderer = new DefaultChartRenderer();
      renderer.restoreSettings(this.configuration.configurationAt("chart"));
      
      state.setChartState(renderer.saveState());
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    state.getLayoutRegions().clear();
    

    List<HierarchicalConfiguration> views = this.configuration.configurationsAt("views.view");
    for (HierarchicalConfiguration view : views)
    {
      LayoutRegion region = LayoutRegion.valueOf(view
        .getString("[@name]"));
      
      boolean visibility = view.getBoolean("[@visible]", true);
      
      state.setRegionVisible(region, visibility);
    }
    return state;
  }
  
  private void readObject(ObjectInputStream in)
    throws IOException
  {
    this.configuration = createConfiguration();
    
    FileConfiguration fileConfig = (FileConfiguration)this.configuration;
    try
    {
      fileConfig.load(in);
    }
    catch (ConfigurationException e)
    {
      throw new IOException(e);
    }
  }
  
  private void writeObject(ObjectOutputStream out)
    throws IOException
  {
    FileConfiguration fileConfig = (FileConfiguration)this.configuration;
    try
    {
      fileConfig.save(out);
    }
    catch (ConfigurationException e)
    {
      throw new IOException(e);
    }
  }
}
