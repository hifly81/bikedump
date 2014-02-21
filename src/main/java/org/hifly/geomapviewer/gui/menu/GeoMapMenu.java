package org.hifly.geomapviewer.gui.menu;

import org.hifly.geomapviewer.domain.Track;
import org.hifly.geomapviewer.gps.GPXDocument;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * @author
 * @date 11/02/14
 */
public class GeoMapMenu extends JMenuBar {

    private JMenuItem itemImportFile,itemImportFolder;

    public GeoMapMenu() {
        super();
        this.add(createFileMenu());
        JMenu help = new JMenu("Help");
        this.add(help);
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
}
