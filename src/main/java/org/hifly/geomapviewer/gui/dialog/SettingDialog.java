package org.hifly.geomapviewer.gui.dialog;

import org.hifly.geomapviewer.domain.ProfileSetting;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;


/**
 * @author
 * @date 27/02/14
 */
//TODO should allow to define a list of profiles
public class SettingDialog extends JDialog  {
     private JSpinner spinnerWeight,spinnerHeight,spinnerBikeWeight = null;
     private JComboBox bikeBrandsCombo,bikeTypesCombo = null;
     private JTextField bikeModelField = null;
     private ProfileSetting profileSetting;



    public SettingDialog(Frame frame, final ProfileSetting profileSetting) {
        super(frame, true);

        this.profileSetting = profileSetting;

        setTitle("Geomapviewer options");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("General", null, createGeneralSettingPanel(),"General settings");
        tabbedPane.addTab("Conversion", null, createConversionSettingPanel(),"Conversion settings");
        tabbedPane.addTab("Bike", null, createBikeSettingPanel(),"Bike settings");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {

                profileSetting.setWeight((Double)spinnerWeight.getValue());
                profileSetting.setHeight((Double) spinnerHeight.getValue());
                profileSetting.setBikeBrand((String)bikeBrandsCombo.getSelectedItem());
                profileSetting.setBikeType((String) bikeTypesCombo.getSelectedItem());
                profileSetting.setBikeModel(bikeModelField.getText());
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

    public JPanel createBikeSettingPanel() {
        JPanel panel = new JPanel();

        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Bike attributes");
        panel1.setBorder(titleBorder);

        //TODO load from external source
        String[] bikeBrands = { "Cannondale", "Scott", "Torpado", "Trek", "Wilier" };
        bikeBrandsCombo = new JComboBox(bikeBrands);
        bikeBrandsCombo.setSelectedIndex(0);
        JLabel bikeBrandsLabel = new JLabel("Brand");
        bikeBrandsLabel.setLabelFor(bikeBrandsCombo);

        bikeModelField = new JTextField(10);
        JLabel bikeModelLabel = new JLabel("Model");
        bikeModelLabel.setLabelFor(bikeModelField);

        //TODO load from external source
        String[] bikeTypes = { "BMX", "City", "Cross", "Downhill", "Electric", "Enduro", "MTB", "Road" };
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

        panel1.add(bikeBrandsLabel);
        panel1.add(bikeBrandsCombo);
        panel1.add(bikeModelLabel);
        panel1.add(bikeModelField);
        panel1.add(bikeTypesLabel);
        panel1.add(bikeTypesCombo);
        panel1.add(bikeWeightLabel);
        panel1.add(spinnerBikeWeight);

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

    class RadioListener implements ActionListener  {
        public void actionPerformed(ActionEvent e) {
            profileSetting.setUnitSystem(e.getActionCommand());

        }
    }

}

