package org.pivot4j.analytics.ui.navigator;

import java.util.Collections;
import java.util.List;
import org.olap4j.metadata.Member;
import org.primefaces.model.TreeNode;

public class MeasureNode
  extends MetadataNode<Member>
{
  public MeasureNode(TreeNode parent, Member member)
  {
    super(member);
    setParent(parent);
  }
  
  public String getType()
  {
    return "measure";
  }
  
  public boolean isLeaf()
  {
    return true;
  }
  
  protected List<TreeNode> createChildren()
  {
    return Collections.emptyList();
  }
}
