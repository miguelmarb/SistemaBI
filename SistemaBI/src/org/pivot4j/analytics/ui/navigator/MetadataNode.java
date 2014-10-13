package org.pivot4j.analytics.ui.navigator;

import org.olap4j.metadata.MetadataElement;
import org.pivot4j.analytics.component.tree.LazyTreeNode;
import org.pivot4j.analytics.component.tree.NodeData;

public abstract class MetadataNode<T extends MetadataElement>
  extends LazyTreeNode<T>
{
  public MetadataNode(T object)
  {
    super(object);
  }
  
  protected NodeData createData(T object)
  {
    return new NodeData(object.getUniqueName(), object.getCaption());
  }
}
