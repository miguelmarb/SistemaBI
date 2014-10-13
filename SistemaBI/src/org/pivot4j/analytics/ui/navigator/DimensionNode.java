package org.pivot4j.analytics.ui.navigator;

import java.util.ArrayList;
import java.util.List;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.primefaces.model.TreeNode;

public class DimensionNode
  extends MetadataNode<Dimension>
{
  public DimensionNode(Dimension dimension)
  {
    super(dimension);
  }
  
  public String getType()
  {
    return "dimension";
  }
  
  public boolean isLeaf()
  {
    return false;
  }
  
  protected List<TreeNode> createChildren()
  {
    List<Hierarchy> hierarchies = ((Dimension)getObject()).getHierarchies();
    List<TreeNode> children = new ArrayList(hierarchies.size());
    for (Hierarchy hierarchy : hierarchies) {
      if (hierarchy.isVisible())
      {
        HierarchyNode node = new HierarchyNode(hierarchy);
        if (configureChildNode(hierarchy, node))
        {
          node.setParent(this);
          
          children.add(node);
        }
      }
    }
    return children;
  }
}
