package org.pivot4j.analytics.ui;

import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.convert.DoubleConverter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotModel;
import org.pivot4j.el.EvaluationFailedException;
import org.pivot4j.el.ExpressionContext;
import org.pivot4j.ui.AbstractRenderCallback;
import org.pivot4j.ui.command.UICommand;
import org.pivot4j.ui.command.UICommandParameters;
import org.pivot4j.ui.table.TableRenderCallback;
import org.pivot4j.ui.table.TableRenderContext;
import org.pivot4j.ui.table.TableRenderer;
import org.pivot4j.util.CssWriter;
import org.pivot4j.util.RenderPropertyUtils;
import org.primefaces.component.behavior.ajax.AjaxBehavior;
import org.primefaces.component.behavior.ajax.AjaxBehaviorListenerImpl;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.inplace.Inplace;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.panelgrid.PanelGrid;
import org.primefaces.component.row.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PivotComponentBuilder
  extends AbstractRenderCallback<TableRenderContext>
  implements TableRenderCallback
{
  private Logger logger = LoggerFactory.getLogger(getClass());
  private Map<String, String> iconMap;
  private UIComponent gridPanel;
  private UIComponent filterPanel;
  private FacesContext facesContext;
  private ExpressionFactory expressionFactory;
  private PanelGrid grid;
  private HtmlPanelGroup header;
  private Row row;
  private Column column;
  private int commandIndex = 0;
  private boolean scenarioEnabled = false;
  private String updateTarget;
  private static Map<String, StyleClassResolver> styleClassResolvers = new HashMap();
  
  static
  {
    styleClassResolvers.put("value", new ValueStyleClassResolver());
    styleClassResolvers.put("aggValue", new AggregationStyleClassResolver());
    
    StyleClassResolver titleStlyeResolver = new TitleStyleClassResolver();
    
    styleClassResolvers.put("label", titleStlyeResolver);
    styleClassResolvers.put("title", titleStlyeResolver);
    styleClassResolvers.put("fill", titleStlyeResolver);
  }
  
  public PivotComponentBuilder(FacesContext facesContext)
  {
    this.facesContext = facesContext;
    if (facesContext != null)
    {
      Application application = facesContext.getApplication();
      
      this.expressionFactory = application.getExpressionFactory();
    }
    this.iconMap = new HashMap();
    
    this.iconMap.put("expandPosition-position", "ui-icon-plus");
    this.iconMap.put("collapsePosition-position", "ui-icon-minus");
    this.iconMap.put("expandMember-member", "ui-icon-plusthick");
    this.iconMap.put("collapseMember-member", "ui-icon-minusthick");
    this.iconMap.put("drillDown-replace", "ui-icon-arrowthick-1-e");
    this.iconMap.put("drillUp-replace", "ui-icon-arrowthick-1-n");
    this.iconMap.put("sort-basic-natural", "ui-icon-triangle-2-n-s");
    this.iconMap.put("sort-basic-other-up", "ui-icon-triangle-1-n");
    this.iconMap.put("sort-basic-other-down", "ui-icon-triangle-1-s");
    this.iconMap.put("sort-basic-current-up", "ui-icon-circle-triangle-n");
    this.iconMap.put("sort-basic-current-down", "ui-icon-circle-triangle-s");
    this.iconMap.put("drillThrough", "ui-icon-search");
  }
  
  public UIComponent getGridPanel()
  {
    return this.gridPanel;
  }
  
  public void setGridPanel(UIComponent gridPanel)
  {
    this.gridPanel = gridPanel;
  }
  
  public UIComponent getFilterPanel()
  {
    return this.filterPanel;
  }
  
  public void setFilterPanel(UIComponent filterPanel)
  {
    this.filterPanel = filterPanel;
  }
  
  public String getContentType()
  {
    return null;
  }
  
  protected String getUpdateTarget()
  {
    return this.updateTarget;
  }
  
  protected Logger getLogger()
  {
    return this.logger;
  }
  
  public void startRender(TableRenderContext context)
  {
    super.startRender(context);
    

    ResourceBundle resources = this.facesContext.getApplication().getResourceBundle(this.facesContext, "msg");
    context.setResourceBundle(resources);
    
    getRenderPropertyUtils().setSuppressErrors(true);
    
    this.commandIndex = 0;
    
    this.scenarioEnabled = ((context.getModel().isScenarioSupported()) && (context.getModel().getScenario() != null));
    
    this.gridPanel.getFacets().clear();
    this.gridPanel.getChildren().clear();
    
    this.filterPanel.getFacets().clear();
    this.filterPanel.getChildren().clear();
    
    List<String> targets = new LinkedList();
    
    targets.add(":grid-form");
    
    UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
    if (viewRoot.findComponent("editor-form") != null)
    {
      targets.add(":editor-form:mdx-editor");
      targets.add(":editor-form:editor-toolbar");
    }
    if (viewRoot.findComponent("source-tree-form") != null) {
      targets.add(":source-tree-form");
    }
    if (viewRoot.findComponent("target-tree-form") != null) {
      targets.add(":target-tree-form");
    }
    this.updateTarget = StringUtils.join(targets, ",");
  }
  
  public void startTable(TableRenderContext context)
  {
    this.grid = new PanelGrid();
    if (context.getAxis() == Axis.FILTER) {
      this.grid.setStyleClass("filter-grid");
    } else {
      this.grid.setStyleClass("pivot-grid");
    }
  }
  
  public void startHeader(TableRenderContext context)
  {
    this.header = new HtmlPanelGroup();
  }
  
  public void endHeader(TableRenderContext context)
  {
    if (this.header.getChildCount() > 0) {
      this.grid.getFacets().put("header", this.header);
    }
    this.header = null;
  }
  
  public void startRow(TableRenderContext context)
  {
    this.row = new Row();
  }
  
  public void startCell(TableRenderContext context)
  {
    this.column = new Column();
    
    String id = "col-" + this.column.hashCode();
    
    this.column.setId(id);
    this.column.setColspan(context.getColumnSpan());
    this.column.setRowspan(context.getRowSpan());
    
    RenderPropertyUtils propertyUtils = getRenderPropertyUtils();
    
    String propertyCategory = context.getRenderPropertyCategory();
    
    StringWriter writer = new StringWriter();
    CssWriter cssWriter = new CssWriter(writer);
    
    String type = context.getCellType();
    if ((type.equals("label")) && (!context.getRenderer().getShowParentMembers()) && 
      (context.getMember() != null) && 
      (context.getAxis() != Axis.FILTER))
    {
      int padding = context.getMember().getDepth() * 10;
      cssWriter.writeStyle("padding-left", padding + "px");
    }
    String fgColor = propertyUtils.getString("fgColor", propertyCategory, null);
    if (fgColor != null) {
      cssWriter.writeStyle("color", fgColor);
    }
    String bgColor = propertyUtils.getString("bgColor", propertyCategory, null);
    if (bgColor != null)
    {
      cssWriter.writeStyle("background-color", bgColor);
      cssWriter.writeStyle("background-image", "none");
    }
    String fontFamily = propertyUtils.getString("fontFamily", propertyCategory, null);
    if (fontFamily != null) {
      cssWriter.writeStyle("font-family", fontFamily);
    }
    String fontSize = propertyUtils.getString("fontSize", propertyCategory, null);
    if (fontSize != null) {
      cssWriter.writeStyle("font-size", fontSize);
    }
    String fontStyle = propertyUtils.getString("fontStyle", propertyCategory, null);
    if (fontStyle != null)
    {
      if (fontStyle.contains("bold")) {
        cssWriter.writeStyle("font-weight", "bold");
      }
      if (fontStyle.contains("italic")) {
        cssWriter.writeStyle("font-style", "oblique");
      }
    }
    writer.flush();
    
    IOUtils.closeQuietly(writer);
    
    String style = writer.toString();
    if (StringUtils.isNotEmpty(style)) {
      this.column.setStyle(style);
    }
    String styleClass = getStyleClass(context);
    String styleClassProperty = propertyUtils.getString("styleClass", propertyCategory, null);
    if (styleClassProperty != null) {
      if (styleClass == null) {
        styleClass = styleClassProperty;
      } else {
        styleClass = styleClass + " " + styleClassProperty;
      }
    }
    this.column.setStyleClass(styleClass);
  }
  
  protected String getStyleClass(TableRenderContext context)
  {
    String styleClass = null;
    
    String type = context.getCellType();
    StyleClassResolver resolver;
    if (("label".equals(type)) && (context.getAxis() == Axis.FILTER)) {
      resolver = (StyleClassResolver)styleClassResolvers.get("value");
    } else {
      resolver = (StyleClassResolver)styleClassResolvers.get(type);
    }
    if (resolver != null) {
      styleClass = resolver.resolve(context);
    }
    return styleClass;
  }
  
  public void renderCommands(TableRenderContext context, List<UICommand<?>> commands)
  {
    if (this.expressionFactory != null) {
      for (UICommand<?> command : commands)
      {
        UICommandParameters parameters = command.createParameters(context);
        
        CommandButton button = new CommandButton();
        

        button.setId("btn-" + this.commandIndex++);
        
        button.setTitle(command.getDescription());
        
        String icon = null;
        
        String mode = command.getMode(context);
        if (mode == null) {
          icon = (String)this.iconMap.get(command.getName());
        } else {
          icon = (String)this.iconMap.get(command.getName() + "-" + mode);
        }
        button.setIcon(icon);
        

        MethodExpression expression = this.expressionFactory.createMethodExpression(this.facesContext.getELContext(), "#{viewHandler.executeCommand}", Void.class, new Class[0]);
        

        button.setActionExpression(expression);
        button.setUpdate(this.updateTarget);
        button.setOncomplete("onViewChanged()");
        button.setProcess("@this");
        
        UIParameter commandParam = new UIParameter();
        commandParam.setName("command");
        commandParam.setValue(command.getName());
        button.getChildren().add(commandParam);
        
        UIParameter axisParam = new UIParameter();
        axisParam.setName("axis");
        axisParam.setValue(Integer.valueOf(parameters.getAxisOrdinal()));
        button.getChildren().add(axisParam);
        
        UIParameter positionParam = new UIParameter();
        positionParam.setName("position");
        positionParam.setValue(Integer.valueOf(parameters.getPositionOrdinal()));
        button.getChildren().add(positionParam);
        
        UIParameter memberParam = new UIParameter();
        memberParam.setName("member");
        memberParam.setValue(Integer.valueOf(parameters.getMemberOrdinal()));
        button.getChildren().add(memberParam);
        
        UIParameter hierarchyParam = new UIParameter();
        hierarchyParam.setName("hierarchy");
        hierarchyParam.setValue(Integer.valueOf(parameters.getHierarchyOrdinal()));
        button.getChildren().add(hierarchyParam);
        
        UIParameter cellParam = new UIParameter();
        cellParam.setName("cell");
        cellParam.setValue(Integer.valueOf(parameters.getCellOrdinal()));
        button.getChildren().add(cellParam);
        
        this.column.getChildren().add(button);
      }
    }
  }
  
  public void renderContent(TableRenderContext context, String label, Double value)
  {
    ExpressionContext elContext = context.getExpressionContext();
    
    elContext.put("label", label);
    elContext.put("value", value);
    


    RenderPropertyUtils propertyUtils = getRenderPropertyUtils();
    String labelText;
    try
    {
      labelText = StringUtils.defaultIfEmpty(propertyUtils
        .getString("label", context
        .getRenderPropertyCategory(), label), "");
    }
    finally
    {
      elContext.remove("label");
      elContext.remove("value");
    }
    Cell cell = context.getCell();
    if ((this.scenarioEnabled) && (context.getCellType().equals("value")) && (cell != null))
    {
      Inplace inplace = new Inplace();
      inplace.setId("inplace-" + context.getCell().getOrdinal());
      inplace.setLabel(labelText);
      inplace.setEditor(true);
      
      InputText input = new InputText();
      input.setId("input-" + context.getCell().getOrdinal());
      input.setValue(value);
      input.setConverter(new DoubleConverter());
      

      MethodExpression expression = this.expressionFactory.createMethodExpression(this.facesContext.getELContext(), "#{viewHandler.updateCell}", Void.class, new Class[0]);
      


      AjaxBehavior behavior = new AjaxBehavior();
      behavior.addAjaxBehaviorListener(new AjaxBehaviorListenerImpl(expression, expression));
      
      behavior.setProcess("@this");
      behavior.setUpdate("@form");
      
      UIParameter commandParam = new UIParameter();
      commandParam.setName("cell");
      commandParam.setValue(Integer.toString(cell.getOrdinal()));
      
      inplace.addClientBehavior("save", behavior);
      inplace.getChildren().add(commandParam);
      inplace.getChildren().add(input);
      
      this.column.getChildren().add(inplace);
    }
    else
    {
      HtmlOutputText text = new HtmlOutputText();
      String id = "txt-" + text.hashCode();
      
      text.setId(id);
      text.setValue(labelText);
      if (context.getMember() != null) {
        text.setTitle(context.getMember().getUniqueName());
      }
      String link = propertyUtils.getString("link", context
        .getRenderPropertyCategory(), null);
      if (link == null)
      {
        this.column.getChildren().add(text);
      }
      else
      {
        HtmlOutputLink anchor = new HtmlOutputLink();
        anchor.setValue(link);
        anchor.getChildren().add(text);
        
        this.column.getChildren().add(anchor);
      }
    }
  }
  
  public void endCell(TableRenderContext context)
  {
    this.row.getChildren().add(this.column);
    this.column = null;
  }
  
  public void endRow(TableRenderContext context)
  {
    if (this.header == null) {
      this.grid.getChildren().add(this.row);
    } else {
      this.header.getChildren().add(this.row);
    }
    this.row = null;
  }
  
  public void endTable(TableRenderContext context)
  {
    if (context.getAxis() == Axis.FILTER) {
      this.filterPanel.getChildren().add(this.grid);
    } else {
      this.gridPanel.getChildren().add(this.grid);
    }
    this.grid = null;
  }
  
  public void endRender(TableRenderContext context)
  {
    ResourceBundle resources = context.getResourceBundle();
    

    MessageFormat mf = new MessageFormat(resources.getString("error.property.expression.title"));
    for (String category : context.getRenderProperties().keySet())
    {
    Map<String, EvaluationFailedException> errors = getRenderPropertyUtils().getLastErrors(category);
      
      for (String property : errors.keySet())
      {
        String title = mf.format(new String[] {resources
          .getString("properties." + property) });
        
        EvaluationFailedException e = (EvaluationFailedException)errors.get(property);
        
        this.facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, title, e
          .getMessage()));
        if (this.logger.isWarnEnabled()) {
          this.logger.warn(title, e);
        }
      }
    }
    this.commandIndex = 0;
    this.scenarioEnabled = false;
    
    super.endRender(context);
  }
  
  public void startBody(TableRenderContext context) {}
  
  public void endBody(TableRenderContext context) {}
  
  static abstract interface StyleClassResolver
  {
    public abstract String resolve(TableRenderContext paramTableRenderContext);
  }
  
  static class TitleStyleClassResolver
    implements PivotComponentBuilder.StyleClassResolver
  {
    public String resolve(TableRenderContext context)
    {
      String type = context.getCellType();
      String styleClass;
      if (context.getAxis() == Axis.COLUMNS)
      {
        styleClass = "col-hdr-cell";
      }
      else
      {
        if ((type.equals("label")) || (
          (context.getAxis() == Axis.FILTER) && (context.getLevel() != null))) {
          styleClass = "row-hdr-cell ui-widget-header";
        } else {
          styleClass = "ui-widget-header";
        }
      }
      return styleClass;
    }
  }
  
  static class ValueStyleClassResolver
    implements PivotComponentBuilder.StyleClassResolver
  {
    public String resolve(TableRenderContext context)
    {
      String styleClass;
      if (context.getAggregator() == null)
      {
        if (context.getRowIndex() % 2 == 0) {
          styleClass = "value-cell cell-even";
        } else {
          styleClass = "value-cell cell-odd";
        }
      }
      else
      {
        styleClass = "ui-widget-header agg-cell";
        if (context.getAxis() == Axis.COLUMNS) {
          styleClass = styleClass + " col-agg-cell";
        } else if (context.getAxis() == Axis.ROWS) {
          styleClass = styleClass + " row-agg-cell";
        }
      }
      return styleClass;
    }
  }
  
  static class AggregationStyleClassResolver
    implements PivotComponentBuilder.StyleClassResolver
  {
    public String resolve(TableRenderContext context)
    {
      String styleClass;
      if (context.getAxis() == Axis.ROWS) {
        styleClass = "ui-widget-header ";
      } else {
        styleClass = "";
      }
       styleClass = styleClass + "agg-title";
      
      return styleClass;
    }
  }
}
