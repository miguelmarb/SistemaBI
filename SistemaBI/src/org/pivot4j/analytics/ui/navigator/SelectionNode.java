package org.pivot4j.analytics.ui.navigator;

import java.util.ArrayList;
import java.util.List;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.analytics.component.tree.NodeData;
import org.pivot4j.util.MemberSelection;

public class SelectionNode
  extends MetadataNode<Member>
{
  private MemberSelection selection;
  private org.pivot4j.util.TreeNode<Member> node;
  
  public SelectionNode(MemberSelection selection)
  {
    this(selection, selection);
  }
  
  SelectionNode(org.pivot4j.util.TreeNode<Member> node, MemberSelection selection)
  {
    super(node.getReference());
    
    this.node = node;
    this.selection = selection;
    
    boolean selected = selection.isSelected((Member)node.getReference());
    
    setSelectable(true);
    setExpanded(true);
    
    NodeData data = getData();
    if (data != null) {
      data.setSelected(selected);
    }
  }
  
  public String getType()
  {
    return "member";
  }
  
  public int getChildCount()
  {
    return this.node.getChildCount();
  }
  
  public boolean isLeaf()
  {
    return getChildCount() == 0;
  }
  
  public void moveUp(SelectionNode child)
  {
    int index = getChildren().indexOf(child);
    if (index < 0) {
      throw new IllegalArgumentException("The specified node is not a child of this node.");
    }
    SelectionNode other = (SelectionNode)getChildren().get(index - 1);
    
    getChildren().set(index, other);
    getChildren().set(index - 1, child);
  }
  
  public void moveDown(SelectionNode child)
  {
    int index = getChildren().indexOf(child);
    if (index < 0) {
      throw new IllegalArgumentException("The specified node is not a child of this node.");
    }
    SelectionNode other = (SelectionNode)getChildren().get(index + 1);
    
    getChildren().set(index, other);
    getChildren().set(index + 1, child);
  }
  
  protected List<org.primefaces.model.TreeNode> createChildren()
  {
    List<org.pivot4j.util.TreeNode<Member>> nodes = this.node.getChildren();
    

    List<org.primefaces.model.TreeNode> children = new ArrayList(nodes.size());
    for (org.pivot4j.util.TreeNode<Member> memberNode : nodes)
    {
      SelectionNode child = new SelectionNode(memberNode, this.selection);
      child.setParent(this);
      
      children.add(child);
    }
    return children;
  }
}
