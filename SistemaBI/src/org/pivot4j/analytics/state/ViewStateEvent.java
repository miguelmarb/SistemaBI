package org.pivot4j.analytics.state;

import java.util.EventObject;

public class ViewStateEvent
  extends EventObject
{
  private static final long serialVersionUID = -8755698962396196967L;
  private ViewState state;
  
  public ViewStateEvent(Object source, ViewState state)
  {
    super(source);
    
    this.state = state;
  }
  
  public ViewState getState()
  {
    return this.state;
  }
  
  public void setState(ViewState state)
  {
    this.state = state;
  }
}
