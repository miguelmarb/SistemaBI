package org.pivot4j.analytics.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.el.ExpressionFactory;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.olap4j.OlapException;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.olap4j.metadata.NamedList;
import org.pivot4j.ModelChangeEvent;
import org.pivot4j.ModelChangeListener;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.component.tree.DefaultTreeNode;
import org.pivot4j.analytics.component.tree.NodeFilter;
import org.pivot4j.analytics.ui.navigator.HierarchyNode;
import org.pivot4j.analytics.ui.navigator.LevelNode;
import org.pivot4j.analytics.ui.navigator.MemberNode;
import org.pivot4j.impl.PivotModelImpl;
import org.pivot4j.transform.ChangeSlicer;
import org.pivot4j.util.MemberHierarchyCache;
import org.pivot4j.util.MemberSelection;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.commandlink.CommandLink;
import org.primefaces.context.RequestContext;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.TreeNode;

@ManagedBean(name="filterHandler")
@RequestScoped
public class FilterHandler
  implements ModelChangeListener, NodeFilter
{
  @ManagedProperty("#{pivotStateManager.model}")
  private PivotModel model;
  @ManagedProperty("#{navigatorHandler}")
  private NavigatorHandler navigator;
  private TreeNode filterNode;
  private TreeNode[] selection;
  private MemberSelection filterMembers;
  private UIComponent filterPanel;
  private CommandButton buttonApply;
  
  @PostConstruct
  protected void initialize()
  {
    if (this.model != null) {
      this.model.addModelChangeListener(this);
    }
  }
  
  @PreDestroy
  protected void destroy()
  {
    if (this.model != null) {
      this.model.removeModelChangeListener(this);
    }
  }
  
  public PivotModel getModel()
  {
    return this.model;
  }
  
  public void setModel(PivotModel model)
  {
    this.model = model;
  }
  
  public NavigatorHandler getNavigator()
  {
    return this.navigator;
  }
  
  public void setNavigator(NavigatorHandler navigator)
  {
    this.navigator = navigator;
  }
  
  protected MemberSelection getFilteredMembers()
  {
    if (this.filterMembers == null)
    {
      Hierarchy hierarchy = getHierarchy();
      if (hierarchy != null)
      {
        ChangeSlicer transform = (ChangeSlicer)this.model.getTransform(ChangeSlicer.class);
        

        this.filterMembers = new MemberSelection(transform.getSlicer(hierarchy), this.model.getCube());
        if ((this.model instanceof PivotModelImpl))
        {
          MemberHierarchyCache cache = ((PivotModelImpl)this.model).getMemberHierarchyCache();
          this.filterMembers.setMemberHierarchyCache(cache);
        }
      }
    }
    return this.filterMembers;
  }
  
  public TreeNode getFilterNode()
  {
    boolean isMeasure;
    List<Member> members;
    if ((this.model != null) && (this.model.isInitialized()))
    {
      Hierarchy hierarchy = getHierarchy();
      if ((this.filterNode == null) && (hierarchy != null))
      {
        this.filterNode = new DefaultTreeNode();
        try
        {
          members = hierarchy.getRootMembers();
          isMeasure = hierarchy.getDimension().getDimensionType() == Dimension.Type.MEASURE;
        }
        catch (OlapException e)
        {
          throw new FacesException(e);
        }
      
        for (Member member : members) {
          if ((!isMeasure) || (member.isVisible()))
          {
            MemberNode node = new MemberNode(member);
            
            node.setNodeFilter(this);
            node.setExpanded(true);
            node.setSelectable(true);
            node.setSelected(isSelected(member));
            
            this.filterNode.getChildren().add(node);
          }
        }
      }
    }
    else
    {
      this.filterNode = null;
    }
    return this.filterNode;
  }
  
  public void setFilterNode(TreeNode filterNode)
  {
    this.filterNode = filterNode;
  }
  
  public TreeNode[] getSelection()
  {
    return this.selection;
  }
  
  public void setSelection(TreeNode[] newSelection)
  {
    if (newSelection == null) {
      this.selection = null;
    } else {
      this.selection = ((TreeNode[])Arrays.copyOf(newSelection, newSelection.length));
    }
  }
  
  public UIComponent getFilterPanel()
  {
    return this.filterPanel;
  }
  
  public void setFilterPanel(UIComponent filterPanel)
  {
    this.filterPanel = filterPanel;
  }
  
  protected String getHierarchyName()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    UIViewRoot view = context.getViewRoot();
    
    return (String)view.getAttributes().get("hierarchy");
  }
  
  protected void setHierarchyName(String hierarchyName)
  {
    FacesContext context = FacesContext.getCurrentInstance();
    UIViewRoot view = context.getViewRoot();
    if (hierarchyName == null) {
      view.getAttributes().remove("hierarchy");
    } else {
      view.getAttributes().put("hierarchy", hierarchyName);
    }
    this.filterMembers = null;
    this.filterNode = null;
    this.selection = null;
  }
  
  protected Hierarchy getHierarchy()
  {
    String hierarchyName = getHierarchyName();
    if (hierarchyName != null) {
      return (Hierarchy)this.model.getCube().getHierarchies().get(hierarchyName);
    }
    return null;
  }
  
  public CommandButton getButtonApply()
  {
    return this.buttonApply;
  }
  
  public void setButtonApply(CommandButton buttonApply)
  {
    this.buttonApply = buttonApply;
  }
  
  protected List<Integer> getNodePath(String id)
  {
    String[] segments = id.split(":");
    String[] indexSegments = segments[(segments.length - 2)].split("_");
    
    List<Integer> path = new ArrayList(indexSegments.length);
    for (String index : indexSegments) {
      path.add(Integer.valueOf(Integer.parseInt(index)));
    }
    return path;
  }
  
  protected boolean isSourceNode(String id)
  {
    return id.startsWith("source-tree-form:cube-navigator");
  }
  
  public void onNodeSelected(NodeSelectEvent e)
  {
    this.buttonApply.setDisabled(false);
  }
  
  public void onNodeUnselected(NodeUnselectEvent e)
  {
    this.buttonApply.setDisabled(false);
  }
  
  public void onClose()
  {
    ChangeSlicer transform = (ChangeSlicer)this.model.getTransform(ChangeSlicer.class);
    
    Hierarchy hierarchy = getHierarchy();
    if (!transform.getHierarchies().contains(hierarchy)) {
      removeHierarchy(getHierarchyName());
    }
    setHierarchyName(null);
  }
  
  public void onDrop(DragDropEvent e)
  {
    List<Integer> sourcePath = getNodePath(e.getDragId());
    
    Hierarchy hierarchy = null;
    if (isSourceNode(e.getDragId()))
    {
      TreeNode sourceNode = findNodeFromPath(this.navigator.getCubeNode(), sourcePath);
      if ((sourceNode instanceof HierarchyNode))
      {
        HierarchyNode node = (HierarchyNode)sourceNode;
        hierarchy = (Hierarchy)node.getObject();
      }
      else if ((sourceNode instanceof LevelNode))
      {
        LevelNode node = (LevelNode)sourceNode;
        Level level = (Level)node.getObject();
        
        hierarchy = level.getHierarchy();
      }
      if (hierarchy == null) {
        return;
      }
      if (this.navigator.isSelected(hierarchy))
      {
        FacesContext context = FacesContext.getCurrentInstance();
        

        ResourceBundle bundle = context.getApplication().getResourceBundle(context, "msg");
        
        String title = bundle.getString("error.filter.title");
        String message = bundle.getString("error.filter.message");
        
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, title, message));
        
        return;
      }
      UIComponent panel = createFilterItem(hierarchy);
      this.filterPanel.getChildren().add(panel);
      
      show(hierarchy.getName());
      
      RequestContext.getCurrentInstance().execute("filterDialog.show();");
    }
  }
  
  protected void configureFilter()
  {
    if ((this.model != null) && (this.filterPanel != null))
    {
      this.filterPanel.getChildren().clear();
      if (this.model.isInitialized())
      {
        ChangeSlicer transform = (ChangeSlicer)this.model.getTransform(ChangeSlicer.class);
        
        List<Hierarchy> hierarchies = transform.getHierarchies();
        for (Hierarchy hierarchy : hierarchies)
        {
          UIComponent panel = createFilterItem(hierarchy);
          this.filterPanel.getChildren().add(panel);
        }
      }
    }
  }
  
  protected UIComponent createFilterItem(Hierarchy hierarchy)
  {
    String id = "filter-item-" + hierarchy.getUniqueName().hashCode();
    
    HtmlPanelGroup panel = new HtmlPanelGroup();
    panel.setId(id);
    panel.setLayout("block");
    panel.setStyleClass("ui-widget-header filter-item");
    
    CommandLink link = new CommandLink();
    link.setId(id + "-link");
    link.setValue(hierarchy.getCaption());
    link.setTitle(hierarchy.getUniqueName());
    
    FacesContext context = FacesContext.getCurrentInstance();
    
    ExpressionFactory factory = context.getApplication().getExpressionFactory();
    
    link.setActionExpression(factory.createMethodExpression(context
      .getELContext(), "#{filterHandler.show}", Void.class, new Class[0]));
    
    link.setUpdate(":filter-form");
    link.setOncomplete("filterDialog.show();");
    
    UIParameter parameter = new UIParameter();
    parameter.setName("hierarchy");
    parameter.setValue(hierarchy.getName());
    
    link.getChildren().add(parameter);
    
    panel.getChildren().add(link);
    
    CommandButton closeButton = new CommandButton();
    closeButton.setId(id + "-button");
    closeButton.setIcon("ui-icon-close");
    closeButton.setActionExpression(factory.createMethodExpression(context
      .getELContext(), "#{filterHandler.removeHierarchy}", Void.class, new Class[0]));
    
    closeButton
      .setUpdate(":filter-items-form,:source-tree-form,:grid-form,:editor-form:mdx-editor,:editor-form:editor-toolbar");
    closeButton.setOncomplete("onViewChanged()");
    
    UIParameter parameter2 = new UIParameter();
    parameter2.setName("hierarchy");
    parameter2.setValue(hierarchy.getName());
    
    closeButton.getChildren().add(parameter2);
    
    panel.getChildren().add(closeButton);
    
    return panel;
  }
  
  public String getFilterItemId()
  {
    String hierarchyName = getHierarchyName();
    if (hierarchyName == null) {
      return null;
    }
    return ":filter-item-" + hierarchyName.replaceAll("[\\[\\]]", "").replaceAll("[\\s\\.]", "_").toLowerCase();
  }
  
  public void onPreRenderView()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    if (!context.isPostback()) {
      configureFilter();
    }
  }
  
  public void show()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
    
    String hierarchyName = (String)parameters.get("hierarchy");
    show(hierarchyName);
  }
  
  public void show(String hierarchyName)
  {
    setHierarchyName(hierarchyName);
    
    this.buttonApply.setDisabled(true);
  }
  
  public void apply()
  {
    List<Member> members = null;
    if (this.selection != null)
    {
      members = new ArrayList(this.selection.length);
      for (TreeNode node : this.selection)
      {
        MemberNode memberNode = (MemberNode)node;
        members.add(memberNode.getObject());
      }
    }
    ChangeSlicer transform = (ChangeSlicer)this.model.getTransform(ChangeSlicer.class);
    transform.setSlicer(getHierarchy(), members);
    
    configureFilter();
  }
  
  public void removeHierarchy()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    

    Map<String, String> parameters = context.getExternalContext().getRequestParameterMap();
    
    String hierarchyName = (String)parameters.get("hierarchy");
    removeHierarchy(hierarchyName);
  }
  
  public void removeHierarchy(String hierarchyName)
  {
    Hierarchy hierarchy = (Hierarchy)this.model.getCube().getHierarchies().get(hierarchyName);
    
    ChangeSlicer transform = (ChangeSlicer)this.model.getTransform(ChangeSlicer.class);
    transform.setSlicer(hierarchy, null);
    
    configureFilter();
  }
  
  protected TreeNode findNodeFromPath(TreeNode parent, List<Integer> indexes)
  {
    if (indexes.size() > 1) {
      return findNodeFromPath((TreeNode)parent.getChildren().get(((Integer)indexes.get(0)).intValue()), indexes
        .subList(1, indexes.size()));
    }
    return (TreeNode)parent.getChildren().get(((Integer)indexes.get(0)).intValue());
  }
  
  public void modelInitialized(ModelChangeEvent e)
  {
    configureFilter();
  }
  
  public void modelDestroyed(ModelChangeEvent e) {}
  
  public void modelChanged(ModelChangeEvent e) {}
  
  public void structureChanged(ModelChangeEvent e)
  {
    configureFilter();
  }
  
  public <T extends MetadataElement> boolean isSelected(T element)
  {
    return getFilteredMembers().isSelected((Member)element);
  }
  
  public <T extends MetadataElement> boolean isSelectable(T element)
  {
    return true;
  }
  
  public <T extends MetadataElement> boolean isActive(T element)
  {
    return false;
  }
  
  public <T extends MetadataElement> boolean isVisible(T element)
  {
    return true;
  }
  
  public <T extends MetadataElement> boolean isExpanded(T element)
  {
    return getFilteredMembers().findChild((Member)element) != null;
  }
}
