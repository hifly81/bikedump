package org.hifly.geomapviewer.gui.menu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * @author
 * @date 11/02/14
 */
//TODO settings menu to insert metric/imperial - weight - bike weight -
public class GeoMapMenu extends JMenuBar {

    private JMenuItem itemImportFile,itemImportFolder, itemOptionsSetting;

    public GeoMapMenu() {
        super();
        this.add(createFileMenu());
        this.add(createSettingMenu());
        this.add(new JMenu("Help"));
    }

    private JMenu createFileMenu() {
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        JMenu importMenu = new JMenu("Import");
        importMenu.setMnemonic(KeyEvent.VK_I);

        JMenuItem importFile = new JMenuItem("Import from file...");
        importFile.setMnemonic(KeyEvent.VK_F);
        importFile.setToolTipText("Import from file...");

        JMenuItem importFolder = new JMenuItem("Import from folder...");

        importMenu.add(importFile);
        importMenu.add(importFolder);

        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic(KeyEvent.VK_C);
        exit.setToolTipText("Exit application");
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,ActionEvent.CTRL_MASK));

        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        file.add(importMenu);
        file.addSeparator();
        file.add(exit);

        this.itemImportFile = importFile;
        this.itemImportFolder = importFolder;

        return file;
    }

    private JMenu createSettingMenu() {
        JMenu setting = new JMenu("Tool");
        setting.setMnemonic(KeyEvent.VK_S);

        JMenuItem optionsItem = new JMenuItem("Options");
        optionsItem.setMnemonic(KeyEvent.VK_P);
        optionsItem.setToolTipText("Setting options");

        setting.add(optionsItem);

        this.itemOptionsSetting = optionsItem;

        return setting;
    }

    public JMenuItem getItemImportFile() {
        return itemImportFile;
    }

    public void setItemImportFile(JMenuItem itemImportFile) {
        this.itemImportFile = itemImportFile;
    }

    public JMenuItem getItemImportFolder() {
        return itemImportFolder;
    }

    public void setItemImportFolder(JMenuItem itemImportFolder) {
        this.itemImportFolder = itemImportFolder;
    }

    public JMenuItem getItemOptionsSetting() {
        return itemOptionsSetting;
    }

    public void setItemOptionsSetting(JMenuItem itemOptionsSetting) {
        this.itemOptionsSetting = itemOptionsSetting;
    }
}
