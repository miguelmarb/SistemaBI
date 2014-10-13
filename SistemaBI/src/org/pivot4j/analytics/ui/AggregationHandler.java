package org.pivot4j.analytics.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import org.olap4j.Axis;
import org.pivot4j.ui.aggregator.AggregatorFactory;
import org.pivot4j.ui.aggregator.AggregatorPosition;
import org.pivot4j.ui.table.TableRenderer;
import org.primefaces.model.DualListModel;

@ManagedBean(name="aggregationHandler")
@RequestScoped
public class AggregationHandler
{
  @ManagedProperty("#{viewHandler}")
  private ViewHandler viewHandler;
  private DualListModel<SelectItem> columnAggregators;
  private DualListModel<SelectItem> columnHierarchyAggregators;
  private DualListModel<SelectItem> columnMemberAggregators;
  private DualListModel<SelectItem> rowAggregators;
  private DualListModel<SelectItem> rowHierarchyAggregators;
  private DualListModel<SelectItem> rowMemberAggregators;
  private ResourceBundle bundle;
  
  @PostConstruct
  protected void initialize()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    this.bundle = context.getApplication().getResourceBundle(context, "msg");
    
    this.columnAggregators = createSelectionModel(Axis.COLUMNS, AggregatorPosition.Grand);
    
    this.columnHierarchyAggregators = createSelectionModel(Axis.COLUMNS, AggregatorPosition.Hierarchy);
    
    this.columnMemberAggregators = createSelectionModel(Axis.COLUMNS, AggregatorPosition.Member);
    

    this.rowAggregators = createSelectionModel(Axis.ROWS, AggregatorPosition.Grand);
    
    this.rowHierarchyAggregators = createSelectionModel(Axis.ROWS, AggregatorPosition.Hierarchy);
    
    this.rowMemberAggregators = createSelectionModel(Axis.ROWS, AggregatorPosition.Member);
  }
  
  protected ResourceBundle getBundle()
  {
    return this.bundle;
  }
  
  protected DualListModel<SelectItem> createSelectionModel(Axis axis, AggregatorPosition position)
  {
    TableRenderer renderer = this.viewHandler.getRenderer();
    
    List<String> selected = renderer.getAggregators(axis, position);
    
    List<String> available = renderer.getAggregatorFactory().getAvailableAggregations();
    

    List<SelectItem> unselectedItems = new ArrayList(available.size());
    
    List<SelectItem> selectedItems = new ArrayList(available.size());
    for (String name : available)
    {
      String key = "label.aggregation.type." + name;
      String label = this.bundle.getString(key);
      
      SelectItem item = new SelectItem(name, label);
      if (selected.contains(name)) {
        selectedItems.add(item);
      } else {
        unselectedItems.add(item);
      }
    }
    return new DualListModel(unselectedItems, selectedItems);
  }
  
  protected void applySelection(DualListModel<SelectItem> selection, Axis axis, AggregatorPosition position)
  {
    List<SelectItem> items = selection.getTarget();
    
    List<String> aggregators = new ArrayList(items.size());
    for (SelectItem item : items) {
      aggregators.add((String)item.getValue());
    }
    TableRenderer renderer = this.viewHandler.getRenderer();
    renderer.setAggregators(axis, position, aggregators);
  }
  
  public void apply()
  {
    applySelection(this.rowAggregators, Axis.ROWS, AggregatorPosition.Grand);
    applySelection(this.rowHierarchyAggregators, Axis.ROWS, AggregatorPosition.Hierarchy);
    
    applySelection(this.rowMemberAggregators, Axis.ROWS, AggregatorPosition.Member);
    

    applySelection(this.columnAggregators, Axis.COLUMNS, AggregatorPosition.Grand);
    
    applySelection(this.columnHierarchyAggregators, Axis.COLUMNS, AggregatorPosition.Hierarchy);
    
    applySelection(this.columnMemberAggregators, Axis.COLUMNS, AggregatorPosition.Member);
    

    this.viewHandler.render();
  }
  
  public ViewHandler getViewHandler()
  {
    return this.viewHandler;
  }
  
  public void setViewHandler(ViewHandler viewHandler)
  {
    this.viewHandler = viewHandler;
  }
  
  public DualListModel<SelectItem> getColumnAggregators()
  {
    return this.columnAggregators;
  }
  
  public void setColumnAggregators(DualListModel<SelectItem> columnAggregators)
  {
    this.columnAggregators = columnAggregators;
  }
  
  public DualListModel<SelectItem> getColumnHierarchyAggregators()
  {
    return this.columnHierarchyAggregators;
  }
  
  public void setColumnHierarchyAggregators(DualListModel<SelectItem> columnHierarchyAggregators)
  {
    this.columnHierarchyAggregators = columnHierarchyAggregators;
  }
  
  public DualListModel<SelectItem> getColumnMemberAggregators()
  {
    return this.columnMemberAggregators;
  }
  
  public void setColumnMemberAggregators(DualListModel<SelectItem> columnMemberAggregators)
  {
    this.columnMemberAggregators = columnMemberAggregators;
  }
  
  public DualListModel<SelectItem> getRowAggregators()
  {
    return this.rowAggregators;
  }
  
  public void setRowAggregators(DualListModel<SelectItem> rowAggregators)
  {
    this.rowAggregators = rowAggregators;
  }
  
  public DualListModel<SelectItem> getRowHierarchyAggregators()
  {
    return this.rowHierarchyAggregators;
  }
  
  public void setRowHierarchyAggregators(DualListModel<SelectItem> rowHierarchyAggregators)
  {
    this.rowHierarchyAggregators = rowHierarchyAggregators;
  }
  
  public DualListModel<SelectItem> getRowMemberAggregators()
  {
    return this.rowMemberAggregators;
  }
  
  public void setRowMemberAggregators(DualListModel<SelectItem> rowMemberAggregators)
  {
    this.rowMemberAggregators = rowMemberAggregators;
  }
}
