package org.hifly.bikedump.gui.menu;

import org.hifly.bikedump.domain.gps.SlopeSegment;
import org.hifly.bikedump.graph.WaypointElevationGraph;
import org.hifly.bikedump.graph.WaypointGraph;
import org.hifly.bikedump.gui.dialog.About;
import org.hifly.bikedump.gui.dialog.GraphViewer;
import org.hifly.bikedump.gui.events.QuitHandler;
import org.hifly.bikedump.gui.theme.ThemePreference;
import org.hifly.bikedump.storage.GeoMapStorage;
import org.hifly.bikedump.utility.SlopeUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TopMenu extends JMenuBar {

    private static final long serialVersionUID = 19L;

    private JMenuItem itemImportFile;
    private JMenuItem itemImportFolder;
    private JMenuItem itemOptionsSetting;
    private JMenuItem trySample;

    // NEW: view/theme actions
    private JRadioButtonMenuItem themeSystem;
    private JRadioButtonMenuItem themeLight;
    private JRadioButtonMenuItem themeDark;

    private JMenu climbs;
    private JFrame currentFrame;

    public TopMenu(JFrame currentFrame) {
        super();
        this.currentFrame = currentFrame;
        this.add(createFileMenu());
        this.add(createSettingMenu());
        this.add(createViewMenu());      // NEW
        this.add(createLibraryMenu());
        this.add(createHelpMenu());
        URL helpImageUrl = getClass().getResource("/img/help.png");
        ImageIcon helpImageIcon = new ImageIcon(helpImageUrl);
        Image img = helpImageIcon.getImage();
        img = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
    }

    private JMenu createFileMenu() {
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);

        URL importImageUrl = getClass().getResource("/img/import.png");
        ImageIcon importImageIcon = new ImageIcon(importImageUrl);
        Image img = importImageIcon.getImage();
        img = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        importImageIcon = new ImageIcon(img);
        JMenu importMenu = new JMenu("Import");
        importMenu.setIcon(importImageIcon);
        importMenu.setMnemonic(KeyEvent.VK_I);

        URL fileImageUrl = getClass().getResource("/img/file.png");
        ImageIcon fileImageIcon = new ImageIcon(fileImageUrl);
        img = fileImageIcon.getImage();
        img = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        fileImageIcon = new ImageIcon(img);
        JMenuItem importFile = new JMenuItem("Import from file...", fileImageIcon);
        importFile.setMnemonic(KeyEvent.VK_F);
        importFile.setToolTipText("Import from file...");

        URL folderImageUrl = getClass().getResource("/img/folder.png");
        ImageIcon folderImageIcon = new ImageIcon(folderImageUrl);
        img = folderImageIcon.getImage();
        img = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        folderImageIcon = new ImageIcon(img);
        JMenuItem importFolder = new JMenuItem("Import from folder...", folderImageIcon);

        JMenuItem importStrava = new JMenuItem("Import from Strava...");
        importStrava.addActionListener(e -> {
            org.hifly.bikedump.gui.dialog.StravaDialog d = new org.hifly.bikedump.gui.dialog.StravaDialog(currentFrame);
            d.setVisible(true);
        });

        importMenu.add(importFile);
        importMenu.add(importFolder);
        importMenu.add(importStrava);

        JMenuItem trySample = new JMenuItem("Try Sample", null);
        trySample.setMnemonic(KeyEvent.VK_S);

        URL exitImageUrl = getClass().getResource("/img/quit.png");
        ImageIcon exitImageIcon = new ImageIcon(exitImageUrl);
        img = exitImageIcon.getImage();
        img = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        exitImageIcon = new ImageIcon(img);
        JMenuItem exit = new JMenuItem("Exit", exitImageIcon);
        exit.setMnemonic(KeyEvent.VK_C);
        exit.setToolTipText("Exit application");
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        exit.addActionListener(new QuitHandler());

        file.add(importMenu);
        file.addSeparator();
        file.add(trySample);
        file.addSeparator();
        file.add(exit);

        this.itemImportFile = importFile;
        this.itemImportFolder = importFolder;
        this.trySample = trySample;

        return file;
    }

    private JMenu createSettingMenu() {
        JMenu setting = new JMenu("Tool");
        setting.setMnemonic(KeyEvent.VK_S);

        URL optionImageUrl = getClass().getResource("/img/option.png");
        ImageIcon optionImageIcon = new ImageIcon(optionImageUrl);
        Image img = optionImageIcon.getImage();
        img = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        optionImageIcon = new ImageIcon(img);
        JMenuItem optionsItem = new JMenuItem("Options", optionImageIcon);
        optionsItem.setMnemonic(KeyEvent.VK_P);
        optionsItem.setToolTipText("Setting options");

        setting.add(optionsItem);

        this.itemOptionsSetting = optionsItem;

        return setting;
    }

    // NEW
    private JMenu createViewMenu() {
        JMenu view = new JMenu("View");
        view.setMnemonic(KeyEvent.VK_V);

        JMenu theme = new JMenu("Theme");

        ButtonGroup group = new ButtonGroup();

        JRadioButtonMenuItem system = new JRadioButtonMenuItem("System");
        JRadioButtonMenuItem light = new JRadioButtonMenuItem("Light");
        JRadioButtonMenuItem dark = new JRadioButtonMenuItem("Dark");

        group.add(system);
        group.add(light);
        group.add(dark);

        theme.add(system);
        theme.add(light);
        theme.add(dark);

        this.themeSystem = system;
        this.themeLight = light;
        this.themeDark = dark;

        view.add(theme);
        return view;
    }

    private JMenu createLibraryMenu() {
        //list of saved climbs
        Map<String, List<SlopeSegment>> savedClimbs = SlopeUtility.organizeSlopesBySteepness(GeoMapStorage.savedClimbsList);

        JMenu library = new JMenu("Library");
        library.setMnemonic(KeyEvent.VK_L);

        climbs = new JMenu("Climbs");
        URL mountainImageUrl = getClass().getResource("/img/mountain.png");
        ImageIcon mountainImageIcon = new ImageIcon(mountainImageUrl);
        Image img = mountainImageIcon.getImage();
        img = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        mountainImageIcon = new ImageIcon(img);
        climbs.setIcon(mountainImageIcon);
        climbs.setMnemonic(KeyEvent.VK_C);
        climbs.setToolTipText("Climbs");

        JMenu climbsItem1 = new JMenu("0% - 4%");
        climbsItem1.setToolTipText("0% - 4%");
        addSavedClimbs(savedClimbs, climbsItem1);

        JMenu climbsItem2 = new JMenu("4% - 8%");
        climbsItem2.setToolTipText("4% - 8%");
        addSavedClimbs(savedClimbs, climbsItem2);

        JMenu climbsItem3 = new JMenu("8% - 10%");
        climbsItem3.setToolTipText("8% - 10%");
        addSavedClimbs(savedClimbs, climbsItem3);

        JMenu climbsItem4 = new JMenu("10% - 15%");
        climbsItem4.setToolTipText("10% - 15%");
        addSavedClimbs(savedClimbs, climbsItem4);

        JMenu climbsItem5 = new JMenu(">15%");
        climbsItem5.setToolTipText(">15%");
        addSavedClimbs(savedClimbs, climbsItem5);

        climbs.add(climbsItem1);
        climbs.add(climbsItem2);
        climbs.add(climbsItem3);
        climbs.add(climbsItem4);
        climbs.add(climbsItem5);

        library.add(climbs);

        return library;
    }

    private JMenu createHelpMenu() {
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutMenuItem = new JMenuItem("About", null);
        aboutMenuItem.setMnemonic(KeyEvent.VK_P);
        aboutMenuItem.setToolTipText("About");
        aboutMenuItem.addActionListener(e -> {
            About about = new About();
            about.setVisible(true);
        });

        help.add(aboutMenuItem);

        return help;
    }

    private void addSavedClimbs(Map<String, List<SlopeSegment>> savedClimbs, JMenu climbsItem) {
        if (savedClimbs != null) {
            List<SlopeSegment> slopes = savedClimbs.get(climbsItem.getText());
            if (slopes != null && !slopes.isEmpty()) {
                for (final SlopeSegment slope : slopes) {
                    String climbName = "No name";
                    if (slope.getName() != null && !slope.getName().equals(""))
                        climbName = slope.getName();
                    JMenuItem temp = new JMenuItem(climbName);
                    temp.setToolTipText(climbName);
                    climbsItem.add(temp);
                    temp.addActionListener(event -> {
                        WaypointGraph waypointElevationGraph =
                                new WaypointElevationGraph(slope.getWaypoints(), true, false, true);
                        new GraphViewer(currentFrame, Arrays.asList(waypointElevationGraph));
                    });
                }
            }
        }
    }

    public JMenuItem getItemImportFile() { return itemImportFile; }
    public JMenuItem getItemImportFolder() { return itemImportFolder; }
    public JMenuItem getItemOptionsSetting() { return itemOptionsSetting; }
    public JMenuItem getItemTrySample() { return trySample; }
    public JMenu getClimbs() { return climbs; }

    // NEW: theme items for wiring from Bikedump
    public JRadioButtonMenuItem getThemeSystemItem() { return themeSystem; }
    public JRadioButtonMenuItem getThemeLightItem() { return themeLight; }
    public JRadioButtonMenuItem getThemeDarkItem() { return themeDark; }

    public void selectThemeRadio(ThemePreference pref) {
        switch (pref) {
            case SYSTEM -> themeSystem.setSelected(true);
            case LIGHT -> themeLight.setSelected(true);
            case DARK -> themeDark.setSelected(true);
        }
    }
}