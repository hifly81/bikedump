package org.hifly.geomapviewer.gui.dialog;

import org.hifly.geomapviewer.domain.Bike;
import org.hifly.geomapviewer.domain.LibrarySetting;
import org.hifly.geomapviewer.domain.ProfileSetting;
import org.hifly.geomapviewer.storage.GeoMapStorage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;


/**
 * @author
 * @date 27/02/14
 */
//TODO should allow to define a list of profiles
public class Setting extends JDialog {
    private Setting currentFrame = this;
    private Frame externalFrame = null;
    private BikeSelection bikeSelectionDialog;
    private JSpinner spinnerWeight, spinnerHeight, spinnerBikeWeight = null;
    private JComboBox bikeBrandsCombo, bikeTypesCombo = null;
    private JTextField bikeNameField, bikeModelField = null;
    private JCheckBox scanFoldersCheck = null;
    private ProfileSetting profileSetting;


    public Setting(Frame frame, final ProfileSetting profileSetting) {
        super(frame, true);

        this.externalFrame = frame;
        this.profileSetting = profileSetting;

        setTitle("Geomapviewer options");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", null, createGeneralSettingPanel(), "General settings");
        tabbedPane.addTab("Conversion", null, createConversionSettingPanel(), "Conversion settings");
        tabbedPane.addTab("Library", null, createLibrarySettingPanel(), "Library settings");
        tabbedPane.addTab("Bike", null, createBikeSettingPanel(), "Bike settings");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                //store bikes list in storage
                GeoMapStorage.savedBikesList = getProfileSetting().getBikes();
            }
        });

    }

    public JPanel createGeneralSettingPanel() {
        JPanel panel = new JPanel();

        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Physical attributes");
        panel1.setBorder(titleBorder);

        SpinnerModel weightSpinnerModel = new SpinnerNumberModel(60.0,
                40.0,
                300.0,
                0.1);
        SpinnerModel heightSpinnerModel = new SpinnerNumberModel(160.0,
                60.0,
                250.0,
                0.1);

        JLabel weightLabel = new JLabel("Weight");
        spinnerWeight = new JSpinner(weightSpinnerModel);
        weightLabel.setLabelFor(spinnerWeight);

        JLabel heightLabel = new JLabel("Height");
        spinnerHeight = new JSpinner(heightSpinnerModel);
        heightLabel.setLabelFor(spinnerHeight);

        panel1.add(weightLabel);
        panel1.add(spinnerWeight);
        panel1.add(heightLabel);
        panel1.add(spinnerHeight);

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
        scanFoldersCheck.setSelected(GeoMapStorage.librarySetting==null?false:GeoMapStorage.librarySetting.isScanFolder());

        panel1.add(scanFoldersCheck);

        panel.add(panel1);

        return panel;
    }

    public JPanel createBikeSettingPanel() {
        JPanel panel = new JPanel();

        JPanel panel0 = new JPanel();
        JLabel bikeList = new JLabel();
        bikeList.setText("<html><a href=\"\">bike list</a></html>");
        bikeList.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bikeList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //define dialogs
                bikeSelectionDialog = new BikeSelection(externalFrame, getProfileSetting());
                bikeSelectionDialog.pack();
                bikeSelectionDialog.setLocationRelativeTo(currentFrame);
                bikeSelectionDialog.setVisible(true);
            }
        });
        panel0.add(bikeList);

        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "New Bike definition");
        panel1.setBorder(titleBorder);

        bikeNameField = new JTextField(10);
        JLabel bikeNameLabel = new JLabel("Name");
        bikeNameLabel.setLabelFor(bikeNameField);

        //TODO load from external source
        String[] bikeBrands = {"Cannondale", "Scott", "Torpado", "Trek", "Wilier"};
        bikeBrandsCombo = new JComboBox(bikeBrands);
        bikeBrandsCombo.setSelectedIndex(0);
        JLabel bikeBrandsLabel = new JLabel("Brand");
        bikeBrandsLabel.setLabelFor(bikeBrandsCombo);

        bikeModelField = new JTextField(10);
        JLabel bikeModelLabel = new JLabel("Model");
        bikeModelLabel.setLabelFor(bikeModelField);

        //TODO load from external source
        String[] bikeTypes = {"BMX", "City", "Cross", "Downhill", "Electric", "Enduro", "MTB", "Road"};
        bikeTypesCombo = new JComboBox(bikeTypes);
        bikeTypesCombo.setSelectedIndex(0);
        JLabel bikeTypesLabel = new JLabel("Type");
        bikeTypesLabel.setLabelFor(bikeTypesCombo);

        SpinnerModel bikeWeightSpinnerModel = new SpinnerNumberModel(10,
                5.0,
                30.0,
                0.1);

        JLabel bikeWeightLabel = new JLabel("Weight");
        spinnerBikeWeight = new JSpinner(bikeWeightSpinnerModel);
        bikeWeightLabel.setLabelFor(spinnerBikeWeight);

        JButton buttonSave = new JButton("Add");
        buttonSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                profileSetting.setWeight((Double) spinnerWeight.getValue());
                profileSetting.setHeight((Double) spinnerHeight.getValue());
                //TODO check if bike already exist
                List<Bike> bikes = profileSetting.getBikes();
                if (bikes == null) {
                    bikes = new ArrayList();
                }
                Bike bike = new Bike();
                bike.setBikeBrand(bikeBrandsCombo.getSelectedItem().toString());
                bike.setBikeModel(bikeModelField.getText());
                bike.setBikeName(bikeNameField.getText());
                bike.setBikeType(bikeTypesCombo.getSelectedItem().toString());
                bike.setBikeWeight((Double) spinnerBikeWeight.getValue());
                bikes.add(bike);
                profileSetting.setBikes(bikes);
            }
        });

        panel1.add(bikeNameLabel);
        panel1.add(bikeNameField);
        panel1.add(bikeBrandsLabel);
        panel1.add(bikeBrandsCombo);
        panel1.add(bikeModelLabel);
        panel1.add(bikeModelField);
        panel1.add(bikeTypesLabel);
        panel1.add(bikeTypesCombo);
        panel1.add(bikeWeightLabel);
        panel1.add(spinnerBikeWeight);
        panel1.add(buttonSave);

        panel.add(panel1);
        panel.add(panel0);

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

    public ProfileSetting getProfileSetting() {
        return profileSetting;
    }

    public void setProfileSetting(ProfileSetting profileSetting) {
        this.profileSetting = profileSetting;
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
                if(GeoMapStorage.librarySetting==null) {
                    GeoMapStorage.librarySetting = new LibrarySetting();
                }
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    GeoMapStorage.librarySetting.setScanFolder(false);
                } else {
                    GeoMapStorage.librarySetting.setScanFolder(true);
                }
            }

        }
    }

}

