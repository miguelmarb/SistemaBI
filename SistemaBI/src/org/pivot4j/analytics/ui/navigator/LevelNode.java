package org.pivot4j.analytics.ui.navigator;

import java.util.Collections;
import java.util.List;
import org.olap4j.metadata.Level;
import org.pivot4j.analytics.component.tree.NodeData;
import org.primefaces.model.TreeNode;

public class LevelNode
  extends MetadataNode<Level>
{
  public LevelNode(Level level)
  {
    super(level);
  }
  
  protected NodeData createData(Level level)
  {
    return new LevelNodeData(level);
  }
  
  public String getType()
  {
    return "level";
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
