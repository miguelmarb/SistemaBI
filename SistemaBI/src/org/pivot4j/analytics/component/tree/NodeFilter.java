package org.pivot4j.analytics.component.tree;

import org.olap4j.metadata.MetadataElement;

public abstract interface NodeFilter
{
  public abstract <T extends MetadataElement> boolean isSelected(T paramT);
  
  public abstract <T extends MetadataElement> boolean isSelectable(T paramT);
  
  public abstract <T extends MetadataElement> boolean isVisible(T paramT);
  
  public abstract <T extends MetadataElement> boolean isExpanded(T paramT);
  
  public abstract <T extends MetadataElement> boolean isActive(T paramT);
}
