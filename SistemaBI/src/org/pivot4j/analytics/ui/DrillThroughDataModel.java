package org.pivot4j.analytics.ui;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Cell;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.PivotModel;
import org.pivot4j.transform.DrillThrough;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

@ManagedBean(name="drillThroughData")
@ViewScoped
public class DrillThroughDataModel
  extends LazyDataModel<Map<String, Object>>
{
  private static final long serialVersionUID = 2554173601960871316L;
  private static final String ROW_KEY = "_id";
  public static final int DEFAULT_PAGE_SIZE = 15;
  @ManagedProperty("#{pivotStateManager}")
  private PivotStateManager stateManager;
  private Cell cell;
  private int maximumRows = 0;
  private List<MetadataElement> selection = Collections.emptyList();
  private List<DataColumn> columns = Collections.emptyList();
  
  public DrillThroughDataModel()
  {
    setPageSize(15);
  }
  
  public List<DataColumn> getColumns()
  {
    return this.columns;
  }
  
  public Object getRowKey(Map<String, Object> row)
  {
    return row.get("_id");
  }
  
  public void initialize(Cell cell)
  {
    initialize(cell, null, 0);
  }
  
  public void initialize(Cell cell, List<MetadataElement> selection, int maximumRows)
  {
    if (cell == null) {
      throw new NullArgumentException("cell");
    }
    this.cell = cell;
    if (selection == null) {
      this.selection = Collections.emptyList();
    } else {
      this.selection = Collections.unmodifiableList(selection);
    }
    this.maximumRows = maximumRows;
    
    ResultSet result = null;
    Statement stmt = null;
    try
    {
      result = execute();
      stmt = result.getStatement();
      
      boolean scrollable = result.getStatement().getResultSetType() == 1005;
      
      int rowCount = 0;
      if ((scrollable) && (result.last()))
      {
        rowCount = result.getRow();
        
        result.beforeFirst();
      }
      else
      {
        while (result.next())
        {
          rowCount++;
          if ((maximumRows > 0) && (rowCount >= maximumRows)) {
            break;
          }
        }
      }
      if (maximumRows > 0) {
        rowCount = Math.min(rowCount, maximumRows);
      }
      setRowCount(rowCount);
      
      ResultSetMetaData metadata = result.getMetaData();
      
      int count = metadata.getColumnCount();
      
      this.columns = new LinkedList();
      for (int i = 1; i <= count; i++) {
        this.columns.add(new DataColumn(metadata.getColumnLabel(i), metadata
          .getColumnName(i)));
      }
    }
    catch (SQLException e)
    {
      throw new FacesException(e);
    }
    finally
    {
      DbUtils.closeQuietly(result);
      DbUtils.closeQuietly(stmt);
    }
  }
  
  public void reset()
  {
    this.cell = null;
    this.maximumRows = 0;
    this.columns = Collections.emptyList();
    this.selection = Collections.emptyList();
    
    setRowCount(0);
    setRowIndex(-1);
  }
  
  protected ResultSet execute()
  {
    if (this.cell == null) {
      throw new IllegalStateException("The model has not been initialized.");
    }
    DrillThrough transform = (DrillThrough)this.stateManager.getModel().getTransform(DrillThrough.class);
    if (this.selection.isEmpty()) {
      return transform.drillThrough(this.cell);
    }
    return transform.drillThrough(this.cell, this.selection, this.maximumRows);
  }
  
  public List<Map<String, Object>> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters)
  {
    if (this.columns.isEmpty()) {
      return Collections.emptyList();
    }
    List<Map<String, Object>> data = new ArrayList(pageSize);
    

    ResultSet result = null;
    Statement stmt = null;
    try
    {
      result = execute();
      stmt = result.getStatement();
      
      boolean scrollable = result.getStatement().getResultSetType() == 1005;
      
      int rowIndex = 0;
      if (scrollable)
      {
        rowIndex = first;
        
        result.absolute(rowIndex + 1);
      }
      else
      {
        while (rowIndex < first)
        {
          if (!result.next()) {
            return Collections.emptyList();
          }
          rowIndex++;
        }
      }
      List<DataColumn> columnList = getColumns();
      for (int i = 0; i < pageSize; i++)
      {
        if (!result.next()) {
          break;
        }
        Map<String, Object> row = new HashMap<String, Object>(columnList.size() + 1);
        for (DataColumn column : columnList) {
          if ("_id".equals(column.getName())) {
            row.put("_id", Integer.valueOf(rowIndex + i + 1));
          } else {
            row.put(column.getName(), result
              .getObject(column.getName()));
          }
        }
        data.add(row);
       
        
      }
    }
    catch (SQLException e)
    {
      throw new FacesException(e);
    }
    finally
    {
      DbUtils.closeQuietly(result);
      DbUtils.closeQuietly(stmt);
    }
    return data;
  }
  
  public int getMaximumRows()
  {
    return this.maximumRows;
  }
  
  public Cell getCell()
  {
    return this.cell;
  }
  
  public List<MetadataElement> getSelection()
  {
    return this.selection;
  }
  
  public PivotStateManager getStateManager()
  {
    return this.stateManager;
  }
  
  public void setStateManager(PivotStateManager stateManager)
  {
    this.stateManager = stateManager;
  }
  
  public static class DataColumn
  {
    private String label;
    private String name;
    
    DataColumn(String label, String name)
    {
      this.label = label;
      this.name = name;
    }
    
    public String getLabel()
    {
      return this.label;
    }
    
    public String getName()
    {
      return this.name;
    }
  }
}
