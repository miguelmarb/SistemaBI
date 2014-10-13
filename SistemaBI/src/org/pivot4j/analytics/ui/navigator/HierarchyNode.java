package org.pivot4j.analytics.ui.navigator;

import java.util.ArrayList;
import java.util.List;
import javax.faces.FacesException;
import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.primefaces.model.TreeNode;

public class HierarchyNode
  extends MetadataNode<Hierarchy>
{
  public HierarchyNode(Hierarchy hierarchy)
  {
    super(hierarchy);
  }
  
  public String getType()
  {
    return "hierarchy";
  }
  
  public boolean isLeaf()
  {
    return false;
  }
  
  protected List<TreeNode> createChildren()
  {
    Hierarchy hierarchy = (Hierarchy)getObject();
    try
    {
      if (hierarchy.getDimension().getDimensionType() == Dimension.Type.MEASURE)
      {
        List<? extends Member> members = hierarchy.getRootMembers();
        
        List<TreeNode> children = new ArrayList(members.size());
        for (Member member : members) {
          if (member.isVisible())
          {
            MeasureNode node = new MeasureNode(this, member);
            if (configureChildNode(member, node))
            {
              node.setParent(this);
              
              children.add(node);
            }
          }
        }
        return children;
      }
      List<Level> levels = hierarchy.getLevels();
      List<TreeNode> children = new ArrayList(levels.size());
      for (Level level : levels) {
        if (level.isVisible())
        {
          LevelNode node = new LevelNode(level);
          if (configureChildNode(level, node))
          {
            node.setParent(this);
            
            children.add(node);
          }
        }
      }
      return children;
    }
    catch (OlapException e)
    {
      throw new FacesException(e);
    }
  }
}
