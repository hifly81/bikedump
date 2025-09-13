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


    private JCheckBox scanFoldersCheck, elevationCorrection, showTipsAtStartup, useOfflineTiles = null;
    private JTextField offlineTilesPathField = null;
    private JButton browseOfflineTilesButton = null;

    public Settings(Frame frame) {
        super(frame, true);

        setTitle("Options");

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
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Library settings panel
        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Library");
        panel1.setBorder(titleBorder);

        scanFoldersCheck = new JCheckBox("Scan imported folders");
        scanFoldersCheck.addItemListener(new CheckListener());
        scanFoldersCheck.setSelected(GeoMapStorage.librarySetting == null ? false:GeoMapStorage.librarySetting.isScanFolder());

        panel1.add(scanFoldersCheck);

        // Offline tiles settings panel
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
        Border tilesBorder = new TitledBorder(new LineBorder(Color.BLUE), "Offline Map Tiles");
        panel2.setBorder(tilesBorder);

        useOfflineTiles = new JCheckBox("Use offline map tiles");
        useOfflineTiles.addItemListener(new CheckListener());
        useOfflineTiles.setSelected(GeoMapStorage.librarySetting == null ? false : GeoMapStorage.librarySetting.isUseOfflineTiles());

        JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pathPanel.add(new JLabel("Tiles directory:"));
        offlineTilesPathField = new JTextField(30);
        String currentPath = GeoMapStorage.librarySetting != null ? GeoMapStorage.librarySetting.getOfflineTilesPath() : "";
        offlineTilesPathField.setText(currentPath != null ? currentPath : "");
        offlineTilesPathField.addFocusListener(new PathFieldListener());
        
        browseOfflineTilesButton = new JButton("Browse...");
        browseOfflineTilesButton.addActionListener(new BrowseActionListener());
        
        pathPanel.add(offlineTilesPathField);
        pathPanel.add(browseOfflineTilesButton);

        panel2.add(useOfflineTiles);
        panel2.add(pathPanel);

        // Add info text
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel infoLabel = new JLabel("<html><small>Directory should contain tiles in format: {z}/{x}/{y}.png</small></html>");
        infoLabel.setForeground(Color.GRAY);
        infoPanel.add(infoLabel);
        panel2.add(infoPanel);

        panel.add(panel1);
        panel.add(panel2);

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
            } else if (source == useOfflineTiles) {
                if(GeoMapStorage.librarySetting == null)
                    GeoMapStorage.librarySetting = new LibrarySetting();
                if (e.getStateChange() == ItemEvent.DESELECTED)
                    GeoMapStorage.librarySetting.setUseOfflineTiles(false);
                else
                    GeoMapStorage.librarySetting.setUseOfflineTiles(true);
            }

        }
    }

    class PathFieldListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            // Nothing to do
        }

        @Override
        public void focusLost(FocusEvent e) {
            if(GeoMapStorage.librarySetting == null)
                GeoMapStorage.librarySetting = new LibrarySetting();
            GeoMapStorage.librarySetting.setOfflineTilesPath(offlineTilesPathField.getText());
        }
    }

    class BrowseActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("Select Offline Tiles Directory");
            
            String currentPath = offlineTilesPathField.getText();
            if (currentPath != null && !currentPath.isEmpty()) {
                chooser.setCurrentDirectory(new java.io.File(currentPath));
            }
            
            int result = chooser.showOpenDialog(Settings.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                String selectedPath = chooser.getSelectedFile().getAbsolutePath();
                offlineTilesPathField.setText(selectedPath);
                
                if(GeoMapStorage.librarySetting == null)
                    GeoMapStorage.librarySetting = new LibrarySetting();
                GeoMapStorage.librarySetting.setOfflineTilesPath(selectedPath);
            }
        }
    }

}

