package org.hifly.geomapviewer.gui.dialog;

import org.hifly.geomapviewer.domain.Bike;
import org.hifly.geomapviewer.domain.Profile;
import org.hifly.geomapviewer.domain.ProfileSetting;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author
 * @date 06/03/14
 */
public class ProfileSelection extends JDialog {

    private ProfileSetting profileSetting;
    private JPanel panelRadio = null;

    public ProfileSelection(Frame frame, final ProfileSetting profileSetting) {
        super(frame, true);

        this.profileSetting = profileSetting;

        setTitle("Profile Selection");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Profile Selection", null, createProfilePanel(),"Profile Selection");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    public JPanel createProfilePanel() {
        JPanel panel = new JPanel();

        panelRadio = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Profile list");
        panelRadio.setBorder(titleBorder);

        RadioListener myListener = new RadioListener();

        ButtonGroup group = new ButtonGroup();

        List<Profile> profiles = profileSetting.getProfiles();
        if(profiles!=null && !profiles.isEmpty()) {
            for(Profile profile:profiles) {
                JRadioButton temp = new JRadioButton(profile.getName());
                temp.setSelected(profile.isSelected());
                temp.addActionListener(myListener);
                group.add(temp);

                JLabel profileDelete = new JLabel();
                profileDelete.setText("<html><a href=\"\">delete</a></html>");
                profileDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
                profileDelete.addMouseListener(new ProfileDeleteListener(profile));

                panelRadio.add(temp);
                panelRadio.add(profileDelete);
            }

        }

        panel.add(panelRadio);

        return panel;
    }

    class RadioListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String profileName = e.getActionCommand();
            List<Profile> profiles = profileSetting.getProfiles();
            for(Profile profile:profiles) {
                if(profile.getName().equalsIgnoreCase(profileName)) {
                    profile.setSelected(true);
                    break;
                }
            }

        }
    }

    class ProfileDeleteListener extends MouseAdapter{
        private Profile profile;

        public ProfileDeleteListener(Profile profile) {
            super();
            this.profile = profile;

        }
        public void mouseClicked(MouseEvent e) {
            List<Profile> profiles = profileSetting.getProfiles();
            int profileIndex = 0;
            boolean foundProfile = false;
            for(Profile temp:profiles) {
                if(temp.getName().equalsIgnoreCase(profile.getName())) {
                    foundProfile = true;
                    break;
                }
                profileIndex++;
            }

            if(foundProfile) {
                profiles.remove(profileIndex);
                panelRadio.remove(profileIndex);
                int bikeIndex2 = profileIndex++;
                panelRadio.remove(bikeIndex2);

                validate();
                repaint();

            }
        }
    }
}
