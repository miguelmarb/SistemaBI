package org.pivot4j.analytics.state;

import java.util.EventListener;

public abstract interface ViewStateListener
  extends EventListener
{
  public abstract void viewRegistered(ViewStateEvent paramViewStateEvent);
  
  public abstract void viewUnregistered(ViewStateEvent paramViewStateEvent);
}
