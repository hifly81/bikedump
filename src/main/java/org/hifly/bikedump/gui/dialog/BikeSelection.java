package org.hifly.bikedump.gui.dialog;

import org.hifly.bikedump.domain.Bike;
import org.hifly.bikedump.domain.ProfileSetting;

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

public class BikeSelection extends JDialog {

    private ProfileSetting profileSetting;
    private JPanel panelRadio = null;

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

        panelRadio = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Bike list");
        panelRadio.setBorder(titleBorder);

        RadioListener myListener = new RadioListener();

        ButtonGroup group = new ButtonGroup();

        List<Bike> bikes = profileSetting.getBikes();
        if(profileSetting.getBikes()!=null && !profileSetting.getBikes().isEmpty()) {
            for(Bike bike:bikes) {
                JRadioButton temp = new JRadioButton(bike.getBikeName());
                temp.setSelected(bike.isSelected());
                temp.addActionListener(myListener);
                group.add(temp);

                JLabel bikeDelete = new JLabel();
                bikeDelete.setText("<html><a href=\"\">delete</a></html>");
                bikeDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
                bikeDelete.addMouseListener(new BikeDeleteListener(bike));

                panelRadio.add(temp);
                panelRadio.add(bikeDelete);
            }

        }

        panel.add(panelRadio);

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

    class BikeDeleteListener extends MouseAdapter{
        private Bike bike;

        public BikeDeleteListener(Bike bike) {
            super();
            this.bike = bike;

        }

        public void mouseClicked(MouseEvent e) {
            List<Bike> bikes = profileSetting.getBikes();
            int bikeIndex = 0;
            boolean foundBike = false;
            for(Bike temp:bikes) {
                if(temp.getBikeName().equalsIgnoreCase(bike.getBikeName())) {
                    foundBike = true;
                    break;
                }
                bikeIndex++;
            }

            if(foundBike) {
                bikes.remove(bikeIndex);
                panelRadio.remove(bikeIndex);
                int bikeIndex2 = bikeIndex++;
                panelRadio.remove(bikeIndex2);

                validate();
                repaint();

            }
        }
    }
}
