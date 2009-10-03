package com.jbooktrader.platform.preferences;

import com.jbooktrader.platform.startup.*;
import org.jvnet.substance.*;
import org.jvnet.substance.api.renderers.*;
import org.jvnet.substance.skin.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SubstanceSkinComboSelector extends JComboBox {
    public SubstanceSkinComboSelector() {
        // populate the combobox
        super(new ArrayList<SkinInfo>(SubstanceLookAndFeel.getAllSkins().values()).toArray());

        // set custom renderer to show the skin display name
        setRenderer(new SubstanceDefaultComboBoxRenderer(this) {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                return super.getListCellRendererComponent(list, ((SkinInfo) value).getDisplayName(), index, isSelected, cellHasFocus);
            }
        });

        // add an action listener to change skin based on user selection
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (isEnabled()) {
                            String skinName = ((SkinInfo) getSelectedItem()).getDisplayName();
                            LookAndFeelManager.setSubstanceSkin(skinName);
                        }
                    }
                });
            }
        });
    }
}