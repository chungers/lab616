// 2009 lab616.com, All Rights Reserved.

package com.lab616.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.MenuElement;

import com.google.common.collect.Lists;

/**
 * Encapsulation of Java AWT control /widget.
 * Package private scoped so that it's not used outside of the package.  It's
 * used strictly as an impl of the UIControl interface.
 * 
 * @author david
 *
 */
class AWTControl implements UIControl {

  private Container c;
  public AWTControl(final Container c) {
    this.c = c;
  }
  
  
  public UIControl.Window getWindow() {
    if (c instanceof java.awt.Window) {
      return new UIControl.Window() {
        @SuppressWarnings("deprecation")
        
        public void setVisible(Boolean b) {
          ((java.awt.Window)c).hide();
        }
        
        public void resize(int w, int h) {
          ((java.awt.Window)c).setSize(w, h);
        }
        
        public void dispose() {
          ((java.awt.Window)c).dispatchEvent(
              new WindowEvent((java.awt.Window)c, 
                  WindowEvent.WINDOW_CLOSING));
        }
      };
    }
    return null;
  }

  
  public boolean hasMessage(String regex, String... regexs) {
    final JLabel l = JLABEL_FINDER.find(c, regex);
    if (l != null) {
      return true;
    }
    JOptionPane o = JOPTIONPANE_FINDER.find(c, regex, regexs);
    return o != null;
  }
  
  
  public Field getField(final int idx) {
    final JTextField f = JTEXTFIELD_FINDER.find(c, idx);
    if (f != null) {
      return new UIControl.Field() {
        
        public void setValue(String s) {
          f.setText(s);
        }
      };
    } 
    return new UIControl.Field() {
      
      public void setValue(String s) {
        throw new NoSuchControlException(idx);
      }
    };
  }

  
  public Option getOption(final String key) {
    final JRadioButton r = JRADIOBUTTON_FINDER.find(c, key);
    if (r != null) {
      return new UIControl.Option() {
        
        public void select() {
          r.setSelected(true);
        }
      };
    }
    return new UIControl.Option() {
      
      public void select() {
        throw new NoSuchControlException(key);
      }
    };
  }

  
  public Submit getSubmit(final String name, boolean... optionalForceEnable) {
  	boolean forceEnable = (optionalForceEnable.length > 0) ?
  			optionalForceEnable[0] : false;
  			
    final JButton b = JBUTTON_FINDER.find(c, name);
    if (b != null) {
    	if (forceEnable && !b.isEnabled()) {
    		b.setEnabled(true);
    	}
    	return new UIControl.Submit() {
    		@Override
    		public void submit() {
    			b.doClick();
    		}
    	};
    }

    // Try the menu item instead.
    final JMenuItem mi = findMenuItem(name);
    if (mi != null) {
      return new UIControl.Submit() {
      	@Override
        public void submit() {
          mi.doClick();
        }
      };
    }

    return new UIControl.Submit() {
      @Override
      public void submit() {
        throw new NoSuchControlException(name);
      }
    };
  }
  
  // Give a container, recurse up the tree to find the top level frame.
  private JFrame getFrame(Container c) {
    if (c == null) {
      return null;
    }
    if (c instanceof JFrame) {
      return (JFrame)c;
    } else {
      return getFrame(c.getParent());
    }
  }
  
  // Local the menu item specified in the form of menu1>menu2>menu3...
  JMenuItem findMenuItem(String menus) {
    JFrame frame = getFrame(c);
    String[] path = menus.split(">");
    
    if (frame == null) {
      return null;
    }

    JMenuBar menuBar = JMENUBAR_FINDER.find(frame, "");
    if (menuBar == null) {
      return null;
    }

    JMenuItem mi = JMENUITEM_FINDER.find(menuBar, path[0]);
    for (int i = 1; i < path.length; i++) {
      mi = JMENUITEM_FINDER.find(mi, path[i]);
    }
    return mi;
  }

  
  private interface Find<W, C> {
    public W find(C c, String r, String... rs);
    public W find(C c, int idx);
  }

  private static abstract class AbstractFinder<W, C, K> implements Find<W, C> {
    
    Class<W> protoType;
    Class<C> containerType;
    
    AbstractFinder(Class<W> p, Class<C> c) {
      protoType = p;
      containerType = c;
    }
    
    abstract boolean match(W c, String regex, String... regexs);
    abstract K[] getComponents(C c);
    
    public W find(C c, String s, String... r) {
      return find(c, s, -1, r);
    }
    
    public W find(C c, int idx) {
      return find(c, null, idx, new String[] {});
    }
    
    private W find(C container, String match, int idx, String... r) {
      int i = 0;
      for (K component : getComponents(container)) {
        if (protoType.isAssignableFrom(component.getClass())) {
          boolean matched = false;
          if (match == null) {
            matched = (idx > -1 && i++ == idx);
          } else {
            matched = match(protoType.cast(component), match, r);
          }
          if (matched) {
            return protoType.cast(component);
          }
        } else if (containerType.isAssignableFrom(component.getClass())) {
          W w = find(containerType.cast(component), match, idx, r);
          if (w != null) {
            return w;
          }
        }
      }
      return null;
    }
  }
  
  private static abstract class Finder<W> 
    extends AbstractFinder<W, Container, Component> {
    Finder(Class<W> wc) {
      super(wc, Container.class);
    }
    
    Component[] getComponents(Container c) {
      return c.getComponents();
    }

    
    boolean match(W c, String regex, String... regexs) {
      List<String> exps = Lists.asList(regex, regexs);
      String msg = getText(c);
      if (msg != null) {
        for (String r : exps) {
          boolean matched = msg.indexOf(r) > -1 || msg.matches(r);
          if (matched) {
            return true;
          }
        }
      }
      return false;
    }
    
    abstract String getText(W c);
  }

  private static Find<JButton, Container> JBUTTON_FINDER = 
    new Finder<JButton>(JButton.class) {
    String getText(JButton b) {
      return b.getText();
    }
  };
  
  private static Find<JRadioButton, Container> JRADIOBUTTON_FINDER = 
    new Finder<JRadioButton>(JRadioButton.class) {
    String getText(JRadioButton b) {
      return b.getText();
    }
  };
  
  private static Find<JLabel, Container> JLABEL_FINDER = 
    new Finder<JLabel>(JLabel.class) {
    String getText(JLabel l) {
      return l.getText();
    }
  };

  private static Find<JOptionPane, Container> JOPTIONPANE_FINDER = 
    new Finder<JOptionPane>(JOptionPane.class) {
    String getText(JOptionPane o) {
      return o.getMessage().toString();
    }
  };

  private static Find<JTextField, Container> JTEXTFIELD_FINDER = 
    new Finder<JTextField>(JTextField.class) {
    String getText(JTextField t) {
      return t.getText();
    }
  };

  private static Find<JMenuBar, Container> JMENUBAR_FINDER = 
    new Finder<JMenuBar>(JMenuBar.class) {
    
    boolean match(JMenuBar c, String regex, String... s) {
      return true;
    }
    String getText(JMenuBar m) {
      return m.getName();
    }
  };

  private static abstract class MenuFinder<W> 
    extends AbstractFinder<W, MenuElement, MenuElement> {
    MenuFinder(Class<W> wc) {
      super(wc, MenuElement.class);
    }
    
    MenuElement[] getComponents(MenuElement c) {
      return c.getSubElements();
    }
    
    boolean match(W c, String regex, String... regexs) {
      boolean match = false;
      if (regex != null) {
        match = getText(c).matches(regex);
      }
      for (String r : regexs) {
        match |= getText(c).matches(r);
      }
      return match;
    }
    
    abstract String getText(W c);
  }

  private static Find<JMenuItem, MenuElement> JMENUITEM_FINDER = 
    new MenuFinder<JMenuItem>(JMenuItem.class) {
    String getText(JMenuItem m) {
      return m.getText();
    }
  };
  
  
  /**
   * Exception class when no control can be located / matched.
   */
  public static class NoSuchControlException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    NoSuchControlException(int i) {
      super("idx=" + i);
    }
    NoSuchControlException(String s) {
      super(s);
    }
  }
  
  
}

