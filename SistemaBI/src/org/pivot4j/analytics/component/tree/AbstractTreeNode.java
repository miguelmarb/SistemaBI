package org.pivot4j.analytics.component.tree;

import java.util.List;
import org.primefaces.model.TreeNode;

public abstract class AbstractTreeNode<T>
  implements TreeNode
{
  private static final String ROOT_ROW_KEY = "root";
  private TreeNode parent;
  private boolean expanded = false;
  private boolean selectable = false;
  private boolean selected = false;
  private boolean partialSelected = false;
  
  public TreeNode getParent()
  {
    return this.parent;
  }
  
  public void setParent(TreeNode parent)
  {
    this.parent = parent;
  }
  
  public boolean isExpanded()
  {
    return this.expanded;
  }
  
  public void setExpanded(boolean expanded)
  {
    this.expanded = expanded;
  }
  
  public boolean isSelectable()
  {
    return this.selectable;
  }
  
  public void setSelectable(boolean selectable)
  {
    this.selectable = selectable;
  }
  
  public boolean isSelected()
  {
    return this.selected;
  }
  
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }
  
  public String getRowKey()
  {
    if (getParent() == null) {
      return "root";
    }
    int index = getParent().getChildren().indexOf(this);
    
    String parentKey = getParent().getRowKey();
    if ((parentKey == null) || (parentKey.equals("root"))) {
      return Integer.toString(index);
    }
    return parentKey + "_" + index;
  }
  
  public void setRowKey(String rowKey) {}
  
  public boolean isPartialSelected()
  {
    return this.partialSelected;
  }
  
  public void setPartialSelected(boolean partialSelected)
  {
    this.partialSelected = partialSelected;
  }
}
