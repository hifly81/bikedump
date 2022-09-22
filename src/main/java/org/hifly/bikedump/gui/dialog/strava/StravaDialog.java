package org.hifly.bikedump.gui.dialog.strava;

import org.hifly.bikedump.domain.strava.StravaAthlete;
import org.hifly.bikedump.domain.strava.StravaSetting;
import org.hifly.bikedump.storage.GeoMapStorage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StravaDialog extends JDialog {
 
    private static final long serialVersionUID = 15L;
    
    private StravaDialog currentFrame = this;
    private Frame externalFrame = null;
    private StravaSetting stravaSetting;
    private StravaAccessTokenSelection stravaTokenSelection;
    private StravaActivitySelection stravaActivitySelection;

    public StravaDialog(Frame frame, final StravaSetting stravaSetting) {
        super(frame, true);

        this.externalFrame = frame;
        this.stravaSetting = stravaSetting;

        setTitle("BikeDump - Strava options");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Activities", null, createSyncSettingPanel(), "Activities");
        tabbedPane.addTab("Authentication", null, createAuthSettingPanel(), "Authentication");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                GeoMapStorage.stravaSetting = stravaSetting;
            }
        });

    }

    public JPanel createAuthSettingPanel() {

        JPanel panel = createRootPanel();
        panel.setPreferredSize(new Dimension(300, 150));

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        TitledBorder titleBorder = new TitledBorder(new LineBorder(Color.RED), "New Access Token");
        panel1.setBorder(titleBorder);

        final JTextField accessTokenField = new JTextField();
        accessTokenField.setPreferredSize(new Dimension(100, 24));
        JLabel accessTokenListLabel = new JLabel();
        accessTokenListLabel.setText("<html><a href=\"\">access token list</a></html>");
        accessTokenListLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        accessTokenListLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (stravaSetting.getStravaAthletes() == null || stravaSetting.getStravaAthletes().isEmpty()) {
                    JOptionPane.showMessageDialog(currentFrame,
                            "No access tokens",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    //define dialogs
                    stravaTokenSelection = new StravaAccessTokenSelection(externalFrame, stravaSetting);
                    stravaTokenSelection.pack();
                    stravaTokenSelection.setLocationRelativeTo(currentFrame);
                    stravaTokenSelection.setVisible(true);
                }
            }
        });

        JButton buttonSave = new JButton("Add");
        buttonSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                StravaAthlete athlete = new StravaAthlete();
                athlete.setAccessToken(accessTokenField.getText());
                stravaSetting.getStravaAthletes().add(athlete);
            }
        });

        panel1.add(accessTokenField);
        panel1.add(buttonSave);

        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        TitledBorder titleBorder2 = new TitledBorder(new LineBorder(Color.RED), "Access Token saved");
        panel2.setBorder(titleBorder2);
        panel2.add(accessTokenListLabel);

        panel.add(panel1);
        panel.add(panel2);

        return panel;
    }

    public JPanel createSyncSettingPanel() {

        JPanel panel = new JPanel();

        JPanel panel1 = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Activities");
        panel1.setBorder(titleBorder);

        JLabel activitiesListLabel = new JLabel();
        activitiesListLabel.setText("<html><a href=\"\">strava activities list</a></html>");
        activitiesListLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        activitiesListLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (stravaSetting.getStravaAthletes() == null || stravaSetting.getStravaAthletes().isEmpty()) {
                    JOptionPane.showMessageDialog(currentFrame,
                            "No access tokens, choose one!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else if (stravaSetting.getCurrentAthleteSelected() == null) {
                    JOptionPane.showMessageDialog(currentFrame,
                            "No athlete selected",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    //define dialogs
                    stravaActivitySelection = new StravaActivitySelection(externalFrame, stravaSetting.getCurrentAthleteSelected());
                    stravaActivitySelection.pack();
                    stravaActivitySelection.setLocationRelativeTo(currentFrame);
                    stravaActivitySelection.setVisible(true);
                }
            }
        });

        panel1.add(activitiesListLabel);
        panel.add(panel1);

        return panel;

    }

    private JPanel createRootPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }


}

