package org.pivot4j.analytics.component.tree;

import java.util.List;
import org.olap4j.metadata.MetadataElement;
import org.primefaces.model.TreeNode;

public abstract class LazyTreeNode<T>
  extends AbstractTreeNode<T>
{
  private T object;
  private NodeData data;
  private List<TreeNode> children;
  private NodeFilter nodeFilter;
  
  public LazyTreeNode() {}
  
  public LazyTreeNode(T object)
  {
    setObject(object);
  }
  
  protected abstract NodeData createData(T paramT);
  
  public T getObject()
  {
    return this.object;
  }
  
  public void setObject(T object)
  {
    this.object = object;
    if (object == null) {
      this.data = null;
    } else {
      this.data = createData(object);
    }
  }
  
  public NodeData getData()
  {
    return this.data;
  }
  
  public NodeFilter getNodeFilter()
  {
    return this.nodeFilter;
  }
  
  public void setNodeFilter(NodeFilter nodeFilter)
  {
    this.nodeFilter = nodeFilter;
  }
  
  public int getChildCount()
  {
    return getChildren().size();
  }
  
  public List<TreeNode> getChildren()
  {
    synchronized (this)
    {
      if (!isLoaded()) {
        this.children = createChildren();
      }
    }
    return this.children;
  }
  
  public void refresh()
  {
    this.children = null;
  }
  
  protected boolean isLoaded()
  {
    return this.children != null;
  }
  
  public void clearSelection()
  {
    setSelected(false);
    if (this.children != null) {
      for (TreeNode child : this.children) {
        if ((child instanceof LazyTreeNode)) {
          ((LazyTreeNode)child).clearSelection();
        }
      }
    }
  }
  
  protected <C extends MetadataElement> boolean configureChildNode(C element, LazyTreeNode<?> child)
  {
    child.setParent(this);
    if (this.nodeFilter != null) {
      if (this.nodeFilter.isVisible(element))
      {
        child.setNodeFilter(this.nodeFilter);
        child.setExpanded(this.nodeFilter.isExpanded(element));
        child.setSelectable(this.nodeFilter.isSelectable(element));
        child.setSelected(this.nodeFilter.isSelected(element));
        
        NodeData nodeData = child.getData();
        nodeData.setSelected(this.nodeFilter.isActive(element));
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  
  protected abstract List<TreeNode> createChildren();
}
