package org.pivot4j.analytics.component;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesRenderer;
import org.primefaces.component.colorpicker.ColorPicker;
import org.primefaces.component.colorpicker.ColorPickerRenderer;
import org.primefaces.util.WidgetBuilder;

@FacesRenderer(componentFamily="org.pivot4j.component", rendererType="org.pivot4j.component.ColorPickerRenderer")
public class AjaxColorPickerRenderer
  extends ColorPickerRenderer
{
  public static final String RENDERER_TYPE = "org.pivot4j.component.ColorPickerRenderer";
  
  public void decode(FacesContext context, UIComponent component)
  {
    decodeBehaviors(context, component);
    
    super.decode(context, component);
  }
  
  protected void encodeScript(FacesContext context, ColorPicker colorPicker)
    throws IOException
  {
    String clientId = colorPicker.getClientId(context);
    String value = (String)colorPicker.getValue();
    
    WidgetBuilder wb = getWidgetBuilder(context);
    
    wb.init("AjaxColorPicker", colorPicker.resolveWidgetVar(), clientId, "colorpicker")
      .attr("mode", colorPicker.getMode())
      .attr("color", value, null);
    
    encodeClientBehaviors(context, (ClientBehaviorHolder)colorPicker);
    
    wb.finish();
  }
}
