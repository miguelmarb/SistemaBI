package org.pivot4j.analytics.ui.navigator;

import java.util.ArrayList;
import java.util.List;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.MetadataElement;
import org.olap4j.metadata.NamedList;
import org.primefaces.model.TreeNode;

public class CubeNode
  extends MetadataNode<Cube>
{
  public CubeNode(Cube cube)
  {
    super(cube);
  }
  
  public String getType()
  {
    return "cube";
  }
  
  public boolean isLeaf()
  {
    return false;
  }
  
  protected List<TreeNode> createChildren()
  {
    List<Dimension> dimensions = ((Cube)getObject()).getDimensions();
    List<TreeNode> children = new ArrayList<TreeNode>(dimensions.size());
    for (Dimension dimension : dimensions) {
      if (dimension.isVisible())
      {
        Hierarchy defaultHierarchy = dimension.getDefaultHierarchy();
        MetadataNode<?> node;
        MetadataElement element;
        if ((dimension.getHierarchies().size() == 1) && 
          (defaultHierarchy.isVisible()))
        {
           element = defaultHierarchy;
          
          node = new HierarchyNode((Hierarchy)element);
        }
        else
        {
          element = dimension;
          
          node = new DimensionNode(dimension);
        }
        if (configureChildNode(element, node))
        {
          node.setParent(this);
          
          children.add(node);
        }
      }
    }
    return children;
  }
}
