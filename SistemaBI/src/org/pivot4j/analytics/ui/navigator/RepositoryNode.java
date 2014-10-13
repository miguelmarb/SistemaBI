package org.pivot4j.analytics.ui.navigator;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.component.StateHolder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.pivot4j.analytics.component.tree.LazyTreeNode;
import org.pivot4j.analytics.component.tree.NodeData;
import org.pivot4j.analytics.repository.ReportFile;
import org.pivot4j.analytics.repository.ReportRepository;
import org.pivot4j.analytics.repository.RepositoryFileFilter;
import org.primefaces.model.TreeNode;

public class RepositoryNode
  extends LazyTreeNode<ReportFile>
  implements StateHolder
{
  private ReportRepository repository;
  private RepositoryFileFilter filter;
  private boolean transientState = false;
  private String viewId;
  
  public RepositoryNode()
  {
    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    
    Map<String, Object> applicationMap = externalContext.getApplicationMap();
    

    this.repository = ((ReportRepository)applicationMap.get("reportRepository"));
  }
  
  public RepositoryNode(ReportFile file, ReportRepository repository)
  {
    super(file);
    if (repository == null) {
      throw new NullArgumentException("repository");
    }
    this.repository = repository;
    
    setSelectable(true);
  }
  
  public ReportRepository getRepository()
  {
    return this.repository;
  }
  
  public String getType()
  {
    String type;
    if (((ReportFile)getObject()).isRoot())
    {
      type = "root";
    }
    else
    {
      if (((ReportFile)getObject()).isDirectory()) {
        type = "directory";
      } else {
        type = "file";
      }
    }
    return type;
  }
  
  public boolean isLeaf()
  {
    return (!((ReportFile)getObject()).isDirectory()) || (getChildCount() == 0);
  }
  
  public String getViewId()
  {
    return this.viewId;
  }
  
  public void setViewId(String viewId)
  {
    this.viewId = viewId;
    
    getData().setSelected(viewId != null);
  }
  
  public RepositoryFileFilter getFilter()
  {
    return this.filter;
  }
  
  public void setFilter(RepositoryFileFilter filter)
  {
    this.filter = filter;
  }
  
  protected NodeData createData(ReportFile object)
  {
    return new NodeData(object.getPath(), object.getName());
  }
  
  protected List<TreeNode> createChildren()
  {
	   List<TreeNode> children;
	   
	  try
    {
      List<ReportFile> files = this.repository.getFiles((ReportFile)getObject());
      children = new ArrayList<TreeNode>(files.size());
      for (ReportFile file : files) {
        if ((this.filter == null) || (this.filter.accept(file)))
        {
          RepositoryNode child = new RepositoryNode(file, this.repository);
          child.setParent(this);
          child.setFilter(this.filter);
          
          children.add(child);
        }
      }
    }
    catch (IOException e)
    {
      throw new FacesException(e);
    }
    return children;
  }
  
  public RepositoryNode selectNode(ReportFile file)
  {
    RepositoryNode node = findNode(file);
    if (node != null)
    {
      node.setSelected(true);
      
      TreeNode parent = node;
      while ((parent = parent.getParent()) != null) {
        parent.setExpanded(true);
      }
    }
    return node;
  }
  
  public RepositoryNode findNode(ReportFile file)
  {
    if (file == null) {
      throw new NullArgumentException("file");
    }
    RepositoryNode selectedNode = null;
    List<ReportFile> ancestors;
    try
    {
      ancestors = file.getAncestors();
    }
    catch (IOException e)
    {

      throw new FacesException(e);
    }
  
    ReportFile thisFile = (ReportFile)getObject();
    if (file.equals(thisFile)) {
      selectedNode = this;
    } else if (ancestors.contains(thisFile)) {
      for (TreeNode node : getChildren())
      {
        RepositoryNode fileNode = (RepositoryNode)node;
        
        selectedNode = fileNode.findNode(file);
        if (selectedNode != null) {
          break;
        }
      }
    }
    return selectedNode;
  }
  
  public RepositoryNode findNode(String viewId)
  {
    if (viewId == null) {
      throw new NullArgumentException("viewId");
    }
    RepositoryNode selectedNode = null;
    if (viewId.equals(this.viewId)) {
      selectedNode = this;
    } else if (isLoaded()) {
      for (TreeNode node : getChildren())
      {
        RepositoryNode fileNode = (RepositoryNode)node;
        
        selectedNode = fileNode.findNode(viewId);
        if (selectedNode != null) {
          break;
        }
      }
    }
    return selectedNode;
  }
  
  public boolean isTransient()
  {
    return this.transientState;
  }
  
  public void setTransient(boolean newTransientValue)
  {
    this.transientState = newTransientValue;
  }
  
  public Object saveState(FacesContext context)
  {
    List<Object> states = new LinkedList();
    
    states.add(Boolean.valueOf(isSelectable()));
    states.add(Boolean.valueOf(isSelected()));
    states.add(Boolean.valueOf(isExpanded()));
    states.add(this.viewId);
    states.add(((ReportFile)getObject()).getPath());
    if ((this.filter instanceof Serializable)) {
      states.add(this.filter);
    }
    return states.toArray(new Object[states.size()]);
  }
  
  public void restoreState(FacesContext context, Object state)
  {
    Object[] states = (Object[])state;
    if (this.repository == null)
    {
      Application application = context.getApplication();
      this.repository = ((ReportRepository)application.evaluateExpressionGet(context, "#{reportRepository}", ReportRepository.class));
    }
    try
    {
      setObject(this.repository.getFile((String)states[4]));
    }
    catch (IOException e)
    {
      throw new FacesException(e);
    }
    setSelectable(((Boolean)states[0]).booleanValue());
    setSelected(((Boolean)states[1]).booleanValue());
    setExpanded(((Boolean)states[2]).booleanValue());
    setViewId((String)states[3]);
    if (states.length > 5) {
      this.filter = ((RepositoryFileFilter)states[5]);
    }
  }
  
  public int hashCode()
  {
    return new HashCodeBuilder().append(getObject()).append(this.repository).toHashCode();
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RepositoryNode other = (RepositoryNode)obj;
    

    return new EqualsBuilder().append(getObject(), other.getObject()).append(this.repository, other.repository).isEquals();
  }
  
  public String toString()
  {
    return ((ReportFile)getObject()).toString();
  }
}
