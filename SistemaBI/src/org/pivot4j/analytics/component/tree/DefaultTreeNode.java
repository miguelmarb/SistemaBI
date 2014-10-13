package org.pivot4j.analytics.component.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.primefaces.model.TreeNode;

public class DefaultTreeNode
  extends AbstractTreeNode<Object>
{
  public static final String DEFAULT_TYPE = "default";
  private String type;
  private Object data;
  private List<TreeNode> children;
  
  public DefaultTreeNode()
  {
    this(null);
  }
  
  public DefaultTreeNode(Object data)
  {
    this.type = "default";
    this.children = new LazyTreeNodeChildren(this);
    this.data = data;
  }
  
  public DefaultTreeNode(Object data, TreeNode parent)
  {
    this.type = "default";
    this.data = data;
    this.children = new LazyTreeNodeChildren(this);
    if (parent != null) {
      parent.getChildren().add(this);
    }
  }
  
  public DefaultTreeNode(String type, Object data, TreeNode parent)
  {
    this.type = type;
    this.data = data;
    this.children = new LazyTreeNodeChildren(this);
    if (parent != null) {
      parent.getChildren().add(this);
    }
  }
  
  public String getType()
  {
    return this.type;
  }
  
  public void setType(String type)
  {
    this.type = type;
  }
  
  public Object getData()
  {
    return this.data;
  }
  
  public void setData(Object data)
  {
    this.data = data;
  }
  
  public List<TreeNode> getChildren()
  {
    return this.children;
  }
  
  public void setChildren(List<TreeNode> children)
  {
    this.children = children;
  }
  
  public int getChildCount()
  {
    return this.children.size();
  }
  
  public boolean isLeaf()
  {
    return CollectionUtils.isEmpty(this.children);
  }
  
  public int hashCode()
  {
    return new HashCodeBuilder().append(this.data).toHashCode();
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DefaultTreeNode)) {
      return false;
    }
    DefaultTreeNode other = (DefaultTreeNode)obj;
    
    return ObjectUtils.equals(this.data, other.data);
  }
  
  public String toString()
  {
    if (this.data != null) {
      return this.data.toString();
    }
    return super.toString();
  }
  
  static class LazyTreeNodeChildren
    extends ArrayList<TreeNode>
  {
    private static final long serialVersionUID = 1L;
    private TreeNode parent;
    
    public LazyTreeNodeChildren(TreeNode parent)
    {
      this.parent = parent;
    }
    
    private void eraseParent(TreeNode node)
    {
      TreeNode parentNode = node.getParent();
      if (parentNode != null)
      {
        parentNode.getChildren().remove(node);
        node.setParent(null);
      }
    }
    
    public boolean add(TreeNode node)
    {
      if (node == null) {
        throw new NullArgumentException("node");
      }
      eraseParent(node);
      boolean result = super.add(node);
      node.setParent(this.parent);
      updateRowKeys(this.parent);
      return result;
    }
    
    public void add(int index, TreeNode node)
    {
      if (node == null) {
        throw new NullArgumentException("node");
      }
      if ((index < 0) || (index > size())) {
        throw new IndexOutOfBoundsException();
      }
      eraseParent(node);
      super.add(index, node);
      node.setParent(this.parent);
      updateRowKeys(this.parent);
    }
    
    public boolean addAll(Collection<? extends TreeNode> collection)
    {
      Iterator<TreeNode> elements = new ArrayList(collection).iterator();
      
      boolean changed = false;
      while (elements.hasNext())
      {
        TreeNode node = (TreeNode)elements.next();
        
        eraseParent(node);
        super.add(node);
        node.setParent(this.parent);
        changed = true;
      }
      if (changed) {
        updateRowKeys(this.parent);
      }
      return changed;
    }
    
    public boolean addAll(int index, Collection<? extends TreeNode> collection)
    {
      Iterator<TreeNode> elements = new ArrayList(collection).iterator();
      boolean changed = false;
      while (elements.hasNext())
      {
        TreeNode node = (TreeNode)elements.next();
        if (node == null) {
          throw new NullPointerException();
        }
        eraseParent(node);
        super.add(index++, node);
        node.setParent(this.parent);
        changed = true;
      }
      if (changed) {
        updateRowKeys(this.parent);
      }
      return changed;
    }
    
    public TreeNode set(int index, TreeNode node)
    {
      if (node == null) {
        throw new NullArgumentException("node");
      }
      if ((index < 0) || (index >= size())) {
        throw new IndexOutOfBoundsException();
      }
      eraseParent(node);
      TreeNode previous = (TreeNode)get(index);
      super.set(index, node);
      previous.setParent(null);
      node.setParent(this.parent);
      updateRowKeys(this.parent);
      
      return previous;
    }
    
    public TreeNode remove(int index)
    {
      TreeNode node = (TreeNode)get(index);
      node.setParent(null);
      super.remove(index);
      updateRowKeys(this.parent);
      
      return node;
    }
    
    public boolean remove(Object object)
    {
      TreeNode node = (TreeNode)object;
      if (node == null) {
        throw new NullPointerException();
      }
      if (super.indexOf(node) != -1) {
        node.setParent(null);
      }
      if (super.remove(node))
      {
        updateRowKeys(this.parent);
        return true;
      }
      return false;
    }
    
    private void updateRowKeys(TreeNode node)
    {
      if (!node.isExpanded()) {
        return;
      }
      int count = node.getChildCount();
      
      List<TreeNode> children = node.getChildren();
      for (int i = 0; i < count; i++)
      {
        TreeNode childNode = (TreeNode)children.get(i);
        String rowKey;
        if (node.getParent() == null) {
          rowKey = String.valueOf(i);
        } else {
          rowKey = node.getRowKey() + "_" + i;
        }
        childNode.setRowKey(rowKey);
        if (childNode.isExpanded()) {
          updateRowKeys(childNode);
        }
      }
    }
  }
}
