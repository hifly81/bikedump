package org.hifly.bikedump.gui.dialog;

import org.hifly.bikedump.domain.LibrarySetting;
import org.hifly.bikedump.domain.ProfileSetting;
import org.hifly.bikedump.storage.GeoMapStorage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

public class SettingDialog extends JDialog {
 
    private static final long serialVersionUID = 14L;

    private SettingDialog currentFrame = this;
    private Frame externalFrame = null;
    private JSpinner spinnerWeight, spinnerHeight, spinnerBikeWeight, spinnerHr = null;
    private JComboBox bikeBrandsCombo, bikeTypesCombo = null;
    private JTextField bikeNameField, bikeModelField = null;
    private JCheckBox scanFoldersCheck, elevationCorrection, showTipsAtStartup = null;
    private ProfileSetting profileSetting;

    public SettingDialog(Frame frame, final ProfileSetting profileSetting) {
        super(frame, true);

        this.externalFrame = frame;
        this.profileSetting = profileSetting;

        setTitle("BikeDump options");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", null, createGeneralSettingPanel(), "General settings");
        tabbedPane.addTab("Conversion", null, createConversionSettingPanel(), "Conversion settings");
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



    public JPanel createConversionSettingPanel() {
        JPanel panel = new JPanel();

        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Conversion properties");
        panel1.setBorder(titleBorder);

        RadioListener myListener = new RadioListener();

        JRadioButton metricButton = new JRadioButton("Metric");
        metricButton.setSelected(true);
        metricButton.addActionListener(myListener);
        JRadioButton imperialButton = new JRadioButton("Imperial");
        imperialButton.addActionListener(myListener);

        ButtonGroup group = new ButtonGroup();
        group.add(metricButton);
        group.add(imperialButton);

        panel1.add(metricButton);
        panel1.add(imperialButton);

        panel.add(panel1);

        return panel;
    }

    private JPanel createRootPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }

    public ProfileSetting getProfileSetting() {
        return profileSetting;
    }

    class RadioListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            profileSetting.setUnitSystem(e.getActionCommand());

        }
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

