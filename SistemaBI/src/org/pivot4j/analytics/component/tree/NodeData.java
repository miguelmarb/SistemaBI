package org.pivot4j.analytics.component.tree;

import java.io.Serializable;

public class NodeData
  implements Serializable
{
  private static final long serialVersionUID = 1504395803569600514L;
  private String id;
  private String name;
  private boolean selected;
  
  public NodeData() {}
  
  public NodeData(String id, String name)
  {
    this.id = id;
    this.name = name;
  }
  
  public String getId()
  {
    return this.id;
  }
  
  public void setId(String id)
  {
    this.id = id;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void setName(String name)
  {
    this.name = name;
  }
  
  public boolean isSelected()
  {
    return this.selected;
  }
  
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }
}
