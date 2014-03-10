package org.hifly.geomapviewer.gui.dialog;

import org.hifly.geomapviewer.domain.Bike;
import org.hifly.geomapviewer.domain.ProfileSetting;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author
 * @date 06/03/14
 */
public class BikeSelection extends JDialog {

    private ProfileSetting profileSetting;

    public BikeSelection(Frame frame, final ProfileSetting profileSetting) {
        super(frame, true);

        this.profileSetting = profileSetting;

        setTitle("Bike Selection");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Bike Selection", null, createBikeSelectionPanel(),"Bike Selection");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    public JPanel createBikeSelectionPanel() {
        JPanel panel = new JPanel();

        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Bike list");
        panel1.setBorder(titleBorder);

        RadioListener myListener = new RadioListener();

        ButtonGroup group = new ButtonGroup();

        List<Bike> bikes = profileSetting.getBikes();
        if(profileSetting.getBikes()!=null && !profileSetting.getBikes().isEmpty()) {
            for(Bike bike:bikes) {
                JRadioButton temp = new JRadioButton(bike.getBikeName());
                temp.setSelected(bike.isSelected());
                temp.addActionListener(myListener);
                group.add(temp);
                //TODO delete button
                JLabel bikeDelete = new JLabel();
                bikeDelete.setText("<html><a href=\"\">delete</a></html>");
                bikeDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
                bikeDelete.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                       //TODO delete action
                    }
                });

                panel1.add(temp);
                panel1.add(bikeDelete);
            }

        }

        panel.add(panel1);

        return panel;
    }

    class RadioListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String bikeName = e.getActionCommand();
            List<Bike> bikes = profileSetting.getBikes();
            for(Bike bike:bikes) {
                if(bike.getBikeName().equalsIgnoreCase(bikeName)) {
                    bike.setSelected(true);
                    break;
                }
            }

        }
    }
}
