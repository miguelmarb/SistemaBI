package org.pivot4j.analytics.ui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.view.facelets.FaceletException;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Cell;
import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Measure;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.component.tree.NodeFilter;
import org.pivot4j.analytics.ui.navigator.CubeNode;
import org.pivot4j.analytics.ui.navigator.MetadataNode;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.model.TreeNode;

@ManagedBean(name="drillThroughHandler")
@RequestScoped
public class DrillThroughHandler
  implements NodeFilter
{
  @ManagedProperty("#{pivotStateManager}")
  private PivotStateManager stateManager;
  @ManagedProperty("#{drillThroughData}")
  private DrillThroughDataModel data;
  private CubeNode cubeNode;
  private TreeNode[] selection;
  private int maximumRows = 0;
  private DataTable table;
  
  public void update()
  {
    update(this.data.getCell());
  }
  
  public void update(Cell cell)
  {
    if (cell == null) {
      throw new NullArgumentException("cell");
    }
    List<MetadataElement> elements = new LinkedList();
    if (this.selection != null) {
      for (TreeNode node : this.selection)
      {
        MetadataElement elem = (MetadataElement)((MetadataNode)node).getObject();
        elements.add(elem);
      }
    }
    this.data.setRowIndex(-1);
    this.data.initialize(cell, elements, this.maximumRows);
    
    this.table.setFirst(0);
  }
  
  public CubeNode getCubeNode()
  {
    if (this.cubeNode != null) {
      return this.cubeNode;
    }
    PivotModel model = this.stateManager.getModel();
    if ((model != null) && (model.isInitialized()) && (this.data.getCell() != null))
    {
      this.cubeNode = new CubeNode(model.getCube());
      this.cubeNode.setNodeFilter(this);
    }
    return this.cubeNode;
  }
  
  public void setCubeNode(CubeNode cubeNode)
  {
    this.cubeNode = cubeNode;
  }
  
  public DrillThroughDataModel getData()
  {
    return this.data;
  }
  
  public void setData(DrillThroughDataModel data)
  {
    this.data = data;
  }
  
  public TreeNode[] getSelection()
  {
    return this.selection;
  }
  
  public void setSelection(TreeNode[] newSelection)
  {
    if (newSelection == null) {
      this.selection = null;
    } else {
      this.selection = ((TreeNode[])Arrays.copyOf(newSelection, newSelection.length));
    }
  }
  
  public int getMaximumRows()
  {
    return this.maximumRows;
  }
  
  public void setMaximumRows(int maximumRows)
  {
    this.maximumRows = maximumRows;
  }
  
  public <T extends MetadataElement> boolean isSelected(T element)
  {
    return this.data.getSelection().contains(element);
  }
  
  public <T extends MetadataElement> boolean isSelectable(T element)
  {
    return ((element instanceof Level)) || ((element instanceof Measure));
  }
  
  public <T extends MetadataElement> boolean isVisible(T element)
  {
    if ((element instanceof Level))
    {
      Level level = (Level)element;
      if ((level.getLevelType() == Level.Type.ALL) || (level.isCalculated())) {
        return false;
      }
    }
    else if ((element instanceof Measure))
    {
      return !((Measure)element).isCalculated();
    }
    return true;
  }
  
  public <T extends MetadataElement> boolean isExpanded(T element)
  {
    Dimension dimension = null;
    if ((element instanceof Cube)) {
      return true;
    }
    if ((element instanceof Dimension)) {
      dimension = (Dimension)element;
    } else if ((element instanceof Hierarchy)) {
      dimension = ((Hierarchy)element).getDimension();
    }
    if (dimension != null) {
      try
      {
        if (dimension.getDimensionType() == Dimension.Type.MEASURE) {
          return true;
        }
      }
      catch (OlapException e)
      {
        throw new FaceletException(e);
      }
    }
    return false;
  }
  
  public <T extends MetadataElement> boolean isActive(T element)
  {
    return false;
  }
  
  public PivotStateManager getStateManager()
  {
    return this.stateManager;
  }
  
  public void setStateManager(PivotStateManager stateManager)
  {
    this.stateManager = stateManager;
  }
  
  public DataTable getTable()
  {
    return this.table;
  }
  
  public void setTable(DataTable table)
  {
    this.table = table;
  }
}
