package org.pivot4j.analytics.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.olap4j.metadata.Cube;
import org.pivot4j.PivotModel;
import org.pivot4j.ui.fop.FopExporter;
import org.pivot4j.ui.poi.ExcelExporter;
import org.pivot4j.ui.poi.Format;
import org.pivot4j.ui.table.TableRenderer;

@ManagedBean(name="pivotExportHandler")
@RequestScoped
public class PivotExportHandler
{
  @ManagedProperty("#{pivotStateManager.model}")
  private PivotModel model;
  @ManagedProperty("#{viewHandler}")
  private ViewHandler viewHandler;
  private boolean showHeader;
  private String headerText;
  private boolean showFooter;
  private String footerText;
  private int paperSize;
  private List<SelectItem> paperSizes;
  private Orientation orientation;
  private List<SelectItem> orientations;
  private int fontSize;
  private int headerFontSize;
  private int footerFontSize;
  
  public PivotExportHandler()
  {
    this.showHeader = true;
    


    this.showFooter = true;
    


    this.paperSize = MediaSizeName.ISO_A4.getValue();
    


    this.orientation = Orientation.Portrait;
    


    this.fontSize = 8;
    
    this.headerFontSize = 10;
    
    this.footerFontSize = 10;
  }
  
  public  enum Orientation
  {
   
    Portrait {
     			OrientationRequested getValue() {
    	 				return OrientationRequested.PORTRAIT;
    	 			}
    	 		},
    	 		Landscape {
    				OrientationRequested getValue() {
    	 				return OrientationRequested.LANDSCAPE;
    	 			}
    	 		};
    abstract OrientationRequested getValue();
  }
  
  
  
  public ViewHandler getViewHandler()
  {
    return this.viewHandler;
  }
  
  public void setViewHandler(ViewHandler viewHandler)
  {
    this.viewHandler = viewHandler;
  }
  
  public PivotModel getModel()
  {
    return this.model;
  }
  
  public void setModel(PivotModel model)
  {
    this.model = model;
  }
  
  public boolean getShowHeader()
  {
    return this.showHeader;
  }
  
  public void setShowHeader(boolean showHeader)
  {
    this.showHeader = showHeader;
  }
  
  public String getHeaderText()
  {
    return this.headerText;
  }
  
  public void setHeaderText(String headerText)
  {
    this.headerText = headerText;
  }
  
  public boolean getShowFooter()
  {
    return this.showFooter;
  }
  
  public void setShowFooter(boolean showFooter)
  {
    this.showFooter = showFooter;
  }
  
  public String getFooterText()
  {
    return this.footerText;
  }
  
  public void setFooterText(String footerText)
  {
    this.footerText = footerText;
  }
  
  public int getPaperSize()
  {
    return this.paperSize;
  }
  
  public void setPaperSize(int paperSize)
  {
    this.paperSize = paperSize;
  }
  
  public List<SelectItem> getPaperSizes()
    throws IllegalAccessException
  {
    if (this.paperSizes == null)
    {
      this.paperSizes = new ArrayList();
      
      Field[] fields = MediaSizeName.class.getFields();
      for (Field field : fields)
      {
        String name = field.getName();
        MediaSizeName media = (MediaSizeName)field.get(null);
        this.paperSizes.add(new SelectItem(
          Integer.toString(media.getValue()), name));
      }
    }
    return this.paperSizes;
  }
  
  public Orientation getOrientation()
  {
    return this.orientation;
  }
  
  public void setOrientation(Orientation orientation)
  {
    this.orientation = orientation;
  }
  
  public List<SelectItem> getOrientations()
  {
    if (this.orientations == null)
    {
      FacesContext context = FacesContext.getCurrentInstance();
      
      ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
      

      this.orientations = new ArrayList();
      for (Orientation orient : Orientation.values())
      {
        String label;
        try
        {
          label = bundle.getString("label.pdfExport.page.orientation." + orient
            .name().toLowerCase());
        }
        catch (MissingResourceException e)
        {
          label = orient.name();
        }
        this.orientations.add(new SelectItem(orient, label));
      }
    }
    return this.orientations;
  }
  
  public int getFontSize()
  {
    return this.fontSize;
  }
  
  public void setFontSize(int fontSize)
  {
    this.fontSize = fontSize;
  }
  
  public int getHeaderFontSize()
  {
    return this.headerFontSize;
  }
  
  public void setHeaderFontSize(int headerFontSize)
  {
    this.headerFontSize = headerFontSize;
  }
  
  public int getFooterFontSize()
  {
    return this.footerFontSize;
  }
  
  public void setFooterFontSize(int footerFontSize)
  {
    this.footerFontSize = footerFontSize;
  }
  
  public void exportExcel()
    throws IOException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    ExternalContext externalContext = context.getExternalContext();
    

    Map<String, String> parameters = externalContext.getRequestParameterMap();
    Format format;
    if (parameters.containsKey("format")) {
      format = Format.valueOf((String)parameters.get("format"));
    } else {
      format = Format.HSSF;
    }
    exportExcel(format);
    
    context.responseComplete();
  }
  
  protected void exportExcel(Format format)
    throws IOException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    
    String disposition = String.format("attachment; filename=\"%s.%s\"", new Object[] {this.model
      .getCube().getName(), format.getExtension() });
    
    ExternalContext externalContext = context.getExternalContext();
    externalContext.setResponseHeader("Content-Disposition", disposition);
    
    TableRenderer renderer = this.viewHandler.getRenderer();
    
    boolean renderSlicer = renderer.getRenderSlicer();
    boolean inline = renderer.getShowSlicerMembersInline();
    
    OutputStream out = externalContext.getResponseOutputStream();
    
    ExcelExporter exporter = new ExcelExporter(out);
    exporter.setFormat(format);
    
    externalContext.setResponseContentType(exporter.getContentType());
    try
    {
      renderer.setRenderSlicer(true);
      renderer.setShowSlicerMembersInline(false);
      
      renderer.render(this.model, exporter);
    }
    finally
    {
      renderer.setRenderSlicer(renderSlicer);
      renderer.setShowSlicerMembersInline(inline);
      
      out.flush();
      IOUtils.closeQuietly(out);
    }
  }
  
  public void exportPdf()
    throws IOException, IllegalAccessException
  {
    TableRenderer renderer = this.viewHandler.getRenderer();
    
    FacesContext context = FacesContext.getCurrentInstance();
    
    String disposition = String.format("attachment; filename=\"%s.%s\"", new Object[] {this.model
      .getCube().getName(), "pdf" });
    
    ExternalContext externalContext = context.getExternalContext();
    
    OutputStream out = externalContext.getResponseOutputStream();
    
    FopExporter exporter = new FopExporter(out);
    exporter.setShowHeader(this.showHeader);
    if (StringUtils.isNotBlank(this.headerText)) {
      exporter.setTitleText(this.headerText);
    }
    exporter.setShowFooter(this.showFooter);
    if (StringUtils.isNotBlank(this.footerText)) {
      exporter.setFooterText(this.footerText);
    }
    exporter.setFontSize(this.fontSize + "pt");
    exporter.setTitleFontSize(this.headerFontSize + "pt");
    exporter.setFooterFontSize(this.footerFontSize + "pt");
    exporter.setOrientation(this.orientation.getValue());
    
    MediaSize mediaSize = null;
    
    Field[] fields = MediaSizeName.class.getFields();
    for (Field field : fields)
    {
      MediaSizeName name = (MediaSizeName)field.get(null);
      if (name.getValue() == this.paperSize)
      {
        mediaSize = MediaSize.getMediaSizeForName(name);
        break;
      }
    }
    exporter.setMediaSize(mediaSize);
    
    externalContext.setResponseContentType(exporter.getContentType());
    externalContext.setResponseHeader("Content-Disposition", disposition);
    
    boolean renderSlicer = renderer.getRenderSlicer();
    try
    {
      renderer.setRenderSlicer(true);
      
      renderer.render(this.model, exporter);
    }
    finally
    {
      renderer.setRenderSlicer(renderSlicer);
      
      out.flush();
      IOUtils.closeQuietly(out);
    }
    context.responseComplete();
  }
}
