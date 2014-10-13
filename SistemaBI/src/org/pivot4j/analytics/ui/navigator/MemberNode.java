package org.pivot4j.analytics.ui.navigator;

import java.util.ArrayList;
import java.util.List;
import javax.faces.FacesException;
import org.olap4j.OlapException;
import org.olap4j.metadata.Member;
import org.pivot4j.util.OlapUtils;
import org.primefaces.model.TreeNode;

public class MemberNode
  extends MetadataNode<Member>
{
  public MemberNode(Member member)
  {
    super(member);
  }
  
  public String getType()
  {
    return "member";
  }
  
  public boolean isLeaf()
  {
    try
    {
      return ((Member)getObject()).getChildMemberCount() == 0;
    }
    catch (OlapException e)
    {
      throw new FacesException(e);
    }
  }
  
  protected List<TreeNode> createChildren()
  {
    try
    {
      List<? extends Member> members = ((Member)getObject()).getChildMembers();
      List<TreeNode> children = new ArrayList(members.size());
      for (Member member : members) {
        if (OlapUtils.isVisible(member))
        {
          MemberNode node = new MemberNode(member);
          if (configureChildNode(member, node)) {
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
