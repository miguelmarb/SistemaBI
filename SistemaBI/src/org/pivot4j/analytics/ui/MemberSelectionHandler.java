package org.pivot4j.analytics.ui;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.olap4j.metadata.NamedList;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.component.tree.DefaultTreeNode;
import org.pivot4j.analytics.component.tree.NodeData;
import org.pivot4j.analytics.component.tree.NodeFilter;
import org.pivot4j.analytics.ui.navigator.MemberNode;
import org.pivot4j.analytics.ui.navigator.SelectionNode;
import org.pivot4j.impl.PivotModelImpl;
import org.pivot4j.transform.PlaceMembersOnAxes;
import org.pivot4j.util.MemberHierarchyCache;
import org.pivot4j.util.MemberSelection;
import org.pivot4j.util.OlapUtils;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;

@ManagedBean(name="memberSelectionHandler")
@ViewScoped
public class MemberSelectionHandler
  implements NodeFilter, Serializable
{
  private static final long serialVersionUID = -2124965576827229229L;
  @ManagedProperty("#{pivotStateManager.model}")
  private PivotModel model;
  private TreeNode sourceNode;
  private TreeNode targetNode;
  private TreeNode[] sourceSelection;
  private TreeNode[] targetSelection;
  private Hierarchy hierarchy;
  private String hierarchyName;
  private CommandButton buttonAdd;
  private CommandButton buttonRemove;
  private CommandButton buttonUp;
  private CommandButton buttonDown;
  private CommandButton buttonApply;
  private CommandButton buttonOk;
  private MemberSelection selection;
  
  @PostConstruct
  protected void initialize() {}
  
  @PreDestroy
  protected void destroy() {}
  
  public PivotModel getModel()
  {
    return this.model;
  }
  
  public void setModel(PivotModel model)
  {
    this.model = model;
  }
  
  public TreeNode getSourceNode()
  {
    if (this.sourceNode == null)
    {
      this.sourceNode = new DefaultTreeNode();
      
      Hierarchy hier = getHierarchy();
      if (hier != null) {
        try
        {
          boolean isMeasure = this.hierarchy.getDimension().getDimensionType() == Dimension.Type.MEASURE;
          
          List<? extends Member> members = hier.getRootMembers();
          for (Member member : members) {
            if ((!isMeasure) || (member.isVisible()))
            {
              MemberNode node = new MemberNode(member);
              node.setNodeFilter(this);
              if (isVisible(member))
              {
                node.setExpanded(isExpanded(member));
                node.setSelectable(isSelectable(member));
                node.setSelected(isSelected(member));
                node.getData().setSelected(isActive(member));
                
                this.sourceNode.getChildren().add(node);
              }
            }
          }
        }
        catch (OlapException e)
        {
          throw new FacesException(e);
        }
      }
    }
    return this.sourceNode;
  }
  
  public void setSourceNode(TreeNode sourceNode)
  {
    this.sourceNode = sourceNode;
  }
  
  public TreeNode getTargetNode()
  {
    if (this.targetNode == null)
    {
      MemberSelection sel = getSelection();
      if (sel != null)
      {
        this.targetNode = new SelectionNode(sel);
        
        this.targetNode.setExpanded(true);
      }
    }
    return this.targetNode;
  }
  
  public void setTargetNode(TreeNode targetNode)
  {
    this.targetNode = targetNode;
  }
  
  public void show()
  {
    reset();
    
    FacesContext context = FacesContext.getCurrentInstance();
    

    Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
    
    this.hierarchyName = ((String)parameters.get("hierarchy"));
  }
  
  public void reset()
  {
    this.buttonAdd.setDisabled(true);
    this.buttonRemove.setDisabled(true);
    this.buttonUp.setDisabled(true);
    this.buttonDown.setDisabled(true);
    this.buttonApply.setDisabled(true);
    this.buttonOk.setDisabled(true);
    
    this.hierarchyName = null;
    this.hierarchy = null;
    this.sourceNode = null;
    this.targetNode = null;
    this.selection = null;
  }
  
  public void apply()
  {
    PlaceMembersOnAxes transform = (PlaceMembersOnAxes)this.model.getTransform(PlaceMembersOnAxes.class);
    transform.placeMembers(getHierarchy(), getSelection().getMembers());
    
    this.buttonApply.setDisabled(true);
    this.buttonOk.setDisabled(true);
  }
  
  public void add()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
    
    String modeName = (String)parameters.get("mode");
    if (modeName == null) {
      modeName = SelectionMode.Single.name();
    }
    add(modeName);
  }
  
  public void add(String modeName)
  {
    SelectionMode mode = null;
    if (modeName != null) {
      mode = SelectionMode.valueOf(modeName);
    }
    MemberSelection sel = getSelection();
    if (mode == null)
    {
      sel.clear();
    }
    else
    {
      boolean empty = true;
      
      List<Member> members = sel.getMembers();
      for (TreeNode node : this.sourceSelection)
      {
        MemberNode memberNode = (MemberNode)node;
        
        Member member = (Member)memberNode.getObject();
        
        List<Member> targetMembers = mode.getTargetMembers(member);
        for (Member target : targetMembers) {
          if (!members.contains(target))
          {
            members.add(target);
            empty = false;
          }
        }
      }
      if (empty)
      {
        FacesContext context = FacesContext.getCurrentInstance();
        

        ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
        
        String title = bundle.getString("warn.noMembers.title");
        
        String message = bundle.getString("warn.noMembers.select.message");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, title, message));
        


        return;
      }
      this.selection = new MemberSelection(members, this.model.getCube());
      if ((this.model instanceof PivotModelImpl))
      {
        MemberHierarchyCache cache = ((PivotModelImpl)this.model).getMemberHierarchyCache();
        this.selection.setMemberHierarchyCache(cache);
      }
    }
    this.sourceNode = null;
    this.targetNode = null;
    
    this.sourceSelection = null;
    this.targetSelection = null;
    
    updateButtonStatus();
    
    this.buttonApply.setDisabled(false);
    this.buttonOk.setDisabled(false);
  }
  
  public void remove()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
    
    String modeName = (String)parameters.get("mode");
    if (modeName == null) {
      modeName = SelectionMode.Single.name();
    }
    remove(modeName);
  }
  
  public void remove(String modeName)
  {
    SelectionMode mode = null;
    if (modeName != null) {
      mode = SelectionMode.valueOf(modeName);
    }
    MemberSelection sel = getSelection();
    if (mode == null)
    {
      sel.clear();
    }
    else
    {
      boolean empty = true;
      
      List<Member> members = sel.getMembers();
      
      OlapUtils utils = new OlapUtils(this.model.getCube());
      if ((this.model instanceof PivotModelImpl)) {
        utils.setMemberHierarchyCache(((PivotModelImpl)this.model)
          .getMemberHierarchyCache());
      }
      for (TreeNode node : this.targetSelection)
      {
        SelectionNode memberNode = (SelectionNode)node;
        
        Member member = (Member)memberNode.getObject();
        
        List<Member> targetMembers = mode.getTargetMembers(member);
        for (Member target : targetMembers)
        {
          Member wrappedMember = utils.wrapRaggedIfNecessary(target);
          if (members.contains(wrappedMember))
          {
            members.remove(wrappedMember);
            empty = false;
          }
        }
      }
      if (empty)
      {
        FacesContext context = FacesContext.getCurrentInstance();
        

        ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
        
        String title = bundle.getString("warn.noMembers.title");
        
        String message = bundle.getString("warn.noMembers.remove.message");
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, title, message));
        


        return;
      }
      this.selection = new MemberSelection(members, this.model.getCube());
      if ((this.model instanceof PivotModelImpl))
      {
        MemberHierarchyCache cache = ((PivotModelImpl)this.model).getMemberHierarchyCache();
        this.selection.setMemberHierarchyCache(cache);
      }
    }
    this.sourceNode = null;
    this.targetNode = null;
    
    this.sourceSelection = null;
    this.targetSelection = null;
    
    updateButtonStatus();
    
    this.buttonApply.setDisabled(false);
    this.buttonOk.setDisabled(false);
  }
  
  public void moveUp()
  {
    SelectionNode node = (SelectionNode)this.targetSelection[0];
    Member member = (Member)node.getObject();
    
    MemberSelection sel = getSelection();
    sel.moveUp(member);
    
    SelectionNode parent = (SelectionNode)node.getParent();
    parent.moveUp(node);
    
    updateButtonStatus();
    
    this.buttonApply.setDisabled(false);
    this.buttonOk.setDisabled(false);
  }
  
  public void moveDown()
  {
    SelectionNode node = (SelectionNode)this.targetSelection[0];
    Member member = (Member)node.getObject();
    
    MemberSelection sel = getSelection();
    sel.moveDown(member);
    
    SelectionNode parent = (SelectionNode)node.getParent();
    parent.moveDown(node);
    
    updateButtonStatus();
    
    this.buttonApply.setDisabled(false);
    this.buttonOk.setDisabled(false);
  }
  
  public Hierarchy getHierarchy()
  {
    if ((this.hierarchy == null) && (this.hierarchyName != null) && (this.model.isInitialized())) {
      this.hierarchy = ((Hierarchy)this.model.getCube().getHierarchies().get(this.hierarchyName));
    }
    return this.hierarchy;
  }
  
  protected MemberSelection getSelection()
  {
    if (this.selection == null)
    {
      Hierarchy hier = getHierarchy();
      if (hier != null)
      {
        PlaceMembersOnAxes transform = (PlaceMembersOnAxes)this.model.getTransform(PlaceMembersOnAxes.class);
        
        List<Member> members = transform.findVisibleMembers(hier);
        this.selection = new MemberSelection(members, this.model.getCube());
        if ((this.model instanceof PivotModelImpl))
        {
          MemberHierarchyCache cache = ((PivotModelImpl)this.model).getMemberHierarchyCache();
          this.selection.setMemberHierarchyCache(cache);
        }
      }
    }
    return this.selection;
  }
  
  public boolean isAddButtonEnabled()
  {
    boolean canAdd;
    if ((this.sourceSelection == null) || (this.sourceSelection.length == 0))
    {
      canAdd = false;
    }
    else
    {
      canAdd = true;
      for (TreeNode node : this.sourceSelection) {
        if (((MemberNode)node).getData().isSelected())
        {
          canAdd = false;
          break;
        }
      }
    }
    return canAdd;
  }
  
  public boolean isRemoveButtonEnabled()
  {
    boolean canRemove;
    if ((this.targetSelection == null) || (this.targetSelection.length == 0))
    {
      canRemove = false;
    }
    else
    {
      canRemove = true;
      for (TreeNode node : this.targetSelection) {
        if (!((SelectionNode)node).getData().isSelected())
        {
          canRemove = false;
          break;
        }
      }
    }
    return canRemove;
  }
  
  public boolean isUpButtonEnabled()
  {
    boolean canMoveUp;
   
    if ((this.targetSelection == null) || (this.targetSelection.length != 1))
    {
      canMoveUp = false;
    }
    else
    {
      SelectionNode node = (SelectionNode)this.targetSelection[0];
      
      Member member = (Member)node.getObject();
      
      MemberSelection sel = getSelection();
      
      canMoveUp = sel.canMoveUp(member);
    }
    return canMoveUp;
  }
  
  public boolean isDownButtonEnabled()
  {
    boolean canMoveDown;
    if ((this.targetSelection == null) || (this.targetSelection.length != 1))
    {
      canMoveDown = false;
    }
    else
    {
      SelectionNode node = (SelectionNode)this.targetSelection[0];
      
      Member member = (Member)node.getObject();
      
      MemberSelection sel = getSelection();
      canMoveDown = sel.canMoveDown(member);
    }
    return canMoveDown;
  }
  
  public void onSourceNodeSelected(NodeSelectEvent e)
  {
    updateButtonStatus();
  }
  
  public void onTargetNodeSelected(NodeSelectEvent e)
  {
    updateButtonStatus();
  }
  
  protected void updateButtonStatus()
  {
    this.buttonAdd.setDisabled(!isAddButtonEnabled());
    this.buttonRemove.setDisabled(!isRemoveButtonEnabled());
    this.buttonUp.setDisabled(!isUpButtonEnabled());
    this.buttonDown.setDisabled(!isDownButtonEnabled());
  }
  
  public String getHierarchyName()
  {
    return this.hierarchyName;
  }
  
  public void setHierarchyName(String hierarchyName)
  {
    this.hierarchyName = hierarchyName;
  }
  
  public TreeNode[] getSourceSelection()
  {
    return this.sourceSelection;
  }
  
  public void setSourceSelection(TreeNode[] newSelection)
  {
    if (newSelection == null) {
      this.sourceSelection = null;
    } else {
      this.sourceSelection = ((TreeNode[])Arrays.copyOf(newSelection, newSelection.length));
    }
  }
  
  public TreeNode[] getTargetSelection()
  {
    return this.targetSelection;
  }
  
  public void setTargetSelection(TreeNode[] newSelection)
  {
    if (newSelection == null) {
      this.targetSelection = null;
    } else {
      this.targetSelection = ((TreeNode[])Arrays.copyOf(newSelection, newSelection.length));
    }
  }
  
  public CommandButton getButtonAdd()
  {
    return this.buttonAdd;
  }
  
  public void setButtonAdd(CommandButton buttonAdd)
  {
    this.buttonAdd = buttonAdd;
  }
  
  public CommandButton getButtonRemove()
  {
    return this.buttonRemove;
  }
  
  public void setButtonRemove(CommandButton buttonRemove)
  {
    this.buttonRemove = buttonRemove;
  }
  
  public CommandButton getButtonUp()
  {
    return this.buttonUp;
  }
  
  public void setButtonUp(CommandButton buttonUp)
  {
    this.buttonUp = buttonUp;
  }
  
  public CommandButton getButtonDown()
  {
    return this.buttonDown;
  }
  
  public void setButtonDown(CommandButton buttonDown)
  {
    this.buttonDown = buttonDown;
  }
  
  public CommandButton getButtonApply()
  {
    return this.buttonApply;
  }
  
  public void setButtonApply(CommandButton buttonApply)
  {
    this.buttonApply = buttonApply;
  }
  
  public CommandButton getButtonOk()
  {
    return this.buttonOk;
  }
  
  public void setButtonOk(CommandButton buttonOk)
  {
    this.buttonOk = buttonOk;
  }
  
  public <T extends MetadataElement> boolean isSelected(T element)
  {
    return false;
  }
  
  public <T extends MetadataElement> boolean isSelectable(T element)
  {
    return true;
  }
  
  public <T extends MetadataElement> boolean isVisible(T element)
  {
    Member member = (Member)element;
    try
    {
      return (!isActive(element)) || (member.getChildMemberCount() > 0);
    }
    catch (OlapException e)
    {
      throw new FacesException(e);
    }
  }
  
  public <T extends MetadataElement> boolean isActive(T element)
  {
    return getSelection().isSelected((Member)element);
  }
  
  public <T extends MetadataElement> boolean isExpanded(T element)
  {
    return getSelection().findChild((Member)element) != null;
  }
}
