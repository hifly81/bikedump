package org.hifly.bikedump.gui.dialog;

import org.hifly.bikedump.domain.LibrarySetting;
import org.hifly.bikedump.storage.GeoMapStorage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

public class Settings extends JDialog {
 
    private static final long serialVersionUID = 14L;


    private JCheckBox scanFoldersCheck, elevationCorrection, showTipsAtStartup = null;

    public Settings(Frame frame) {
        super(frame, true);

        setTitle("Bikebump options");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", null, createGeneralSettingPanel(), "General settings");
        tabbedPane.addTab("Library", null, createLibrarySettingPanel(), "Library settings");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
  

    }

    public JPanel createGeneralSettingPanel() {
        JPanel panel = new JPanel();

        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "General");
        panel1.setBorder(titleBorder);

        elevationCorrection = new JCheckBox("Elevation Correction");
        elevationCorrection.addItemListener(new CheckListener());
        //TODO implement save/restore from pref
        elevationCorrection.setSelected(true);
        showTipsAtStartup = new JCheckBox("Show Tips at Startup");
        //TODO implement save/restore from pref
        showTipsAtStartup.setSelected(true);
        panel1.add(elevationCorrection);
        panel1.add(showTipsAtStartup);

        panel.add(panel1);

        return panel;
    }



    public JPanel createLibrarySettingPanel() {
        JPanel panel = new JPanel();

        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Library");
        panel1.setBorder(titleBorder);

        scanFoldersCheck = new JCheckBox("Scan imported folders");
        scanFoldersCheck.addItemListener(new CheckListener());
        scanFoldersCheck.setSelected(GeoMapStorage.librarySetting == null ? false:GeoMapStorage.librarySetting.isScanFolder());

        panel1.add(scanFoldersCheck);

        panel.add(panel1);

        return panel;
    }

    class CheckListener implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            Object source = e.getItemSelectable();

            if (source == scanFoldersCheck) {
                if(GeoMapStorage.librarySetting == null)
                    GeoMapStorage.librarySetting = new LibrarySetting();
                if (e.getStateChange() == ItemEvent.DESELECTED)
                    GeoMapStorage.librarySetting.setScanFolder(false);
                else
                    GeoMapStorage.librarySetting.setScanFolder(true);
            }

        }
    }

}

