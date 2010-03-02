// 2009-2010 lab616.com, All Rights Reserved.
package com.lab616.omnibus.http;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import com.lab616.common.Pair;
import com.lab616.common.scripting.ScriptObject.Parameter;
import com.lab616.common.scripting.ScriptObject.ScriptModule;
import com.lab616.common.scripting.ScriptObjects.Descriptor;

/**
 * Given a module and its set of ScriptObject, generate R script such
 * that R functions are created by sourcing the output of this servlet.
 * 
 * @author david
 *
 */
public class RScriptServlet extends BasicServlet {

  private static final long serialVersionUID = 1L;
  
  private final ScriptModule moduleAnnot;
  private final ServletScript moduleServletAnnot;
  private final List<Pair<String, Descriptor>> scriptObjectSpecs;
  
  public RScriptServlet(ScriptModule module, ServletScript servletAnnotation,
      List<Pair<String, Descriptor>> scriptObjects) {
    this.moduleAnnot = module;
    this.moduleServletAnnot = servletAnnotation;
    this.scriptObjectSpecs = scriptObjects;
  }
  
  @Override
  protected void processRequest(Map<String, String> params, ResponseBuilder b) {
    b.println("require(RCurl)");
    b.println(getServerFunction());
    for (Pair<String, Descriptor> s : scriptObjectSpecs) {
      b.println(getScriptBlock(s.first, s.second));
    }
    b.build();
  }

  String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
     // Nothing. 
    }
    return "localhost";
  }
  
  String getModulePath() {
    return moduleServletAnnot.path();
  }
  
  String getServerFunction() {
    return new ScriptBuilder()
      .l("%s.server <- function(host='%s', port=%s) {", 
          moduleAnnot.name(), getHostName(), HttpServerModule.HTTP_PORT)
      .l(" paste('http://', host, ':', port, sep='') }")
      .build();
  }

  String getScriptBlock(String path, Descriptor desc) {
    return new ScriptBuilder()
      .l("%s <- %s", 
          getFunctionName(desc), getFunction(path, desc))
          .build();
  }
  
  String getFunctionName(Descriptor desc) {
    return moduleAnnot.name() + "." + desc.annotation.name();
  }
  
  String getFunction(String path, Descriptor desc) {
    return new ScriptBuilder()
      .l("function(%s){ gsub('\\n', '', as.character(%s)) }",
          getFunctionParamList(desc), 
          getForm(path, desc))
          .build();
  }
  
  /**
   * Generates the getForm(url, p=v, p2=v2,...) form.
   * @param desc
   * @return
   */
  String getForm(String path, Descriptor desc) {
    return new ScriptBuilder()
      .l("getForm(paste(ifelse(is.null(server),%s.server(),server), '%s', sep=''), %s)", 
          moduleAnnot.name(), path, getGetFormParamList(desc))
      .toString();
  }
  
  String getFunctionParamList(Descriptor desc) {
    ScriptBuilder b = new ScriptBuilder();
    int i = 0;
    for (Pair<Parameter, Type> p : desc.params) {
      if (p.first.defaultValue().length() > 0) {
        String def = (p.second.equals(String.class)) ?
            "'" + p.first.defaultValue() + "'" : p.first.defaultValue();
        b.l("%s=%s", p.first.name(), def);
      } else {
        b.l("%s", p.first.name());
      }
      if (++i < desc.params.size()) {
        b.l(", ");
      }
    }
    if (!desc.params.isEmpty()) {
      b.l(", server=NULL");
    } else {
      b.l("server=NULL");
    }
    return b.toString();
  }

  String getGetFormParamList(Descriptor desc) {
    ScriptBuilder b = new ScriptBuilder();
    int i = 0;
    for (Pair<Parameter, Type> p : desc.params) {
      b.l("%s=%s", p.first.name(), p.first.name());
      if (++i < desc.params.size()) {
        b.l(", ");
      }
    }
    return b.toString();
  }
  
  static class ScriptBuilder {
    StringBuffer buff = new StringBuffer();
    
    public ScriptBuilder l(String format, Object... args) {
      buff.append(String.format(format, args));
      return this;
    }
    
    public String toString() {
      return buff.toString();
    }
    
    public String build() {
      buff.append("\n");
      return buff.toString();
    }
  }
  
}
