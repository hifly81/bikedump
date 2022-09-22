package org.hifly.bikedump.gui.dialog;

import org.hifly.bikedump.domain.Bike;
import org.hifly.bikedump.domain.LibrarySetting;
import org.hifly.bikedump.domain.Profile;
import org.hifly.bikedump.domain.ProfileSetting;
import org.hifly.bikedump.storage.GeoMapStorage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class SettingDialog extends JDialog {
 
    private static final long serialVersionUID = 14L;

    private SettingDialog currentFrame = this;
    private Frame externalFrame = null;
    private BikeSelection bikeSelectionDialog;
    private ProfileSelection profileSelectionDialog;
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
        tabbedPane.addTab("Profile", null, createProfileSettingPanel(), "Profile settings");
        tabbedPane.addTab("Conversion", null, createConversionSettingPanel(), "Conversion settings");
        tabbedPane.addTab("Library", null, createLibrarySettingPanel(), "Library settings");
        tabbedPane.addTab("Bike", null, createBikeSettingPanel(), "Bike settings");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                GeoMapStorage.profileSetting = profileSetting;
            }
        });

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

    public JPanel createProfileSettingPanel() {
        JPanel panel = createRootPanel();

        JPanel panel0 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Profile saved");
        panel0.setBorder(titleBorder);
        if(profileSetting.getProfiles()!=null && !profileSetting.getProfiles().isEmpty()) {
            profileSetting.getProfiles().stream().filter(profile -> profile.isSelected()).forEach(profile -> {
                JLabel profileSelected = new JLabel("Profile selected:" + profile.getName());
                panel0.add(profileSelected);
            });

        }

        JLabel profileList = new JLabel();
        profileList.setText("<html><a href=\"\">profile list</a></html>");
        profileList.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (profileSetting.getProfiles() == null || profileSetting.getProfiles().isEmpty()) {
                    JOptionPane.showMessageDialog(currentFrame,
                            "No profiles",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    //define dialogs
                    profileSelectionDialog = new ProfileSelection(externalFrame, getProfileSetting());
                    profileSelectionDialog.pack();
                    profileSelectionDialog.setLocationRelativeTo(currentFrame);
                    profileSelectionDialog.setVisible(true);
                }
            }
        });
        panel0.add(profileList);

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleBorder = new TitledBorder(new LineBorder(Color.RED), "New Profile definition");
        panel1.setBorder(titleBorder);

        final JTextField profileNameField = new JTextField();
        profileNameField.setPreferredSize(new Dimension(100, 24));
        JLabel profileNameLabel = new JLabel("Profile name");
        profileNameLabel.setLabelFor(profileNameField);

        SpinnerModel weightSpinnerModel = new SpinnerNumberModel(60.0,
                40.0,
                300.0,
                0.1);
        SpinnerModel heightSpinnerModel = new SpinnerNumberModel(160.0,
                60.0,
                250.0,
                0.1);
        SpinnerModel hrSpinnerModel = new SpinnerNumberModel(100.0,
                50.0,
                220.0,
                0.1);

        JLabel weightLabel = new JLabel("Weight");
        spinnerWeight = new JSpinner(weightSpinnerModel);
        weightLabel.setLabelFor(spinnerWeight);

        JLabel heightLabel = new JLabel("Height");
        spinnerHeight = new JSpinner(heightSpinnerModel);
        heightLabel.setLabelFor(spinnerHeight);

        JLabel hrLabel = new JLabel("Avg. Heart Rate");
        spinnerHr = new JSpinner(hrSpinnerModel);
        hrLabel.setLabelFor(spinnerHr);

        JButton buttonSave = new JButton("Add");
        buttonSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //TODO check if profiles already exist
                List<Profile> profiles = profileSetting.getProfiles();
                if (profiles == null)
                    profiles = new ArrayList<>();
                Profile profile = new Profile();
                profile.setName(profileNameField.getText());
                profile.setWeight((Double) spinnerWeight.getValue());
                profile.setHeight((Double) spinnerHeight.getValue());
                profile.setLhtr((Double) spinnerHr.getValue());
                profiles.add(profile);
                profileSetting.setProfiles(profiles);
            }
        });

        panel1.add(profileNameLabel);
        panel1.add(profileNameField);
        panel1.add(weightLabel);
        panel1.add(spinnerWeight);
        panel1.add(heightLabel);
        panel1.add(spinnerHeight);
        panel1.add(hrLabel);
        panel1.add(spinnerHr);
        panel1.add(buttonSave);

        panel.add(panel1);
        panel.add(panel0);

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

    public JPanel createBikeSettingPanel() {
        JPanel panel = createRootPanel();

        JPanel panel0 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Bikes saved");
        panel0.setBorder(titleBorder);
        if(profileSetting.getBikes()!=null && !profileSetting.getBikes().isEmpty()) {
            profileSetting.getBikes().stream().filter(bike -> bike.isSelected()).forEach(bike -> {
                JLabel bikeSelected = new JLabel("Bike selected:" + bike.getBikeName());
                panel0.add(bikeSelected);
            });

        } else {
            panel0.setVisible(false);
        }
        JLabel bikeList = new JLabel();
        bikeList.setText("<html><a href=\"\">bike list</a></html>");
        bikeList.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bikeList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(profileSetting.getBikes()==null || profileSetting.getBikes().isEmpty()) {
                    JOptionPane.showMessageDialog(currentFrame,
                            "No bikes",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                else {
                    //define dialogs
                    bikeSelectionDialog = new BikeSelection(externalFrame, getProfileSetting());
                    bikeSelectionDialog.pack();
                    bikeSelectionDialog.setLocationRelativeTo(currentFrame);
                    bikeSelectionDialog.setVisible(true);
                }
            }
        });
        panel0.add(bikeList);

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleBorder = new TitledBorder(new LineBorder(Color.RED), "New Bike definition");
        panel1.setBorder(titleBorder);

        bikeNameField = new JTextField(10);
        JLabel bikeNameLabel = new JLabel("Name");
        bikeNameLabel.setLabelFor(bikeNameField);

        //TODO load from external source
        String[] bikeBrands = {"Bianchi", "Cannondale", "Canyon", "Kross", "Scott", "Specialiezed", "Torpado", "Trek", "Wilier"};
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
                //TODO check if bike already exist
                List<Bike> bikes = profileSetting.getBikes();
                if (bikes == null)
                    bikes = new ArrayList<>();
                Bike bike = new Bike();
                bike.setBikeBrand(bikeBrandsCombo.getSelectedItem().toString());
                bike.setBikeModel(bikeModelField.getText());
                bike.setBikeName(bikeNameField.getText());
                bike.setBikeType(bikeTypesCombo.getSelectedItem().toString());
                bike.setBikeWeight((Double) spinnerBikeWeight.getValue());
                bikes.add(bike);
                profileSetting.setBikes(bikes);
                panel0.setVisible(true);
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

