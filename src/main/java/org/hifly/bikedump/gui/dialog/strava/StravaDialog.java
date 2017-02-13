package org.hifly.bikedump.gui.dialog.strava;

import com.google.gson.internal.LinkedTreeMap;
import org.hifly.bikedump.controller.StravaController;
import org.hifly.bikedump.domain.strava.StravaActivity;
import org.hifly.bikedump.domain.strava.StravaAthlete;
import org.hifly.bikedump.domain.strava.StravaSetting;
import org.hifly.bikedump.storage.GeoMapStorage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class StravaDialog extends JDialog {
    private StravaDialog currentFrame = this;
    private Frame externalFrame = null;
    private StravaSetting stravaSetting;
    private StravaAccessTokenSelection stravaTokenSelection;
    private StravaActivitySelection stravaActivitySelection;

    private JPanel syncPanel;

    public StravaDialog(Frame frame, final StravaSetting stravaSetting) {
        super(frame, true);

        this.externalFrame = frame;
        this.stravaSetting = stravaSetting;

        setTitle("Geomapviewer - Strava options");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Authentication", null, createAuthSettingPanel(), "Authentication");
        tabbedPane.addTab("Data", null, createSyncSettingPanel(), "Data");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                GeoMapStorage.stravaSetting = stravaSetting;
            }
        });

    }

    public JPanel createAuthSettingPanel() {

        JPanel panel = new JPanel();

        JPanel panel1 = new JPanel();
        TitledBorder titleBorder = new TitledBorder(new LineBorder(Color.RED), "Auth");
        panel1.setBorder(titleBorder);

        final JTextField accessTokenField = new JTextField();
        accessTokenField.setPreferredSize(new Dimension(100, 24));
        JLabel accessTokenLabel = new JLabel("New access token");
        accessTokenLabel.setLabelFor(accessTokenField);
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

        panel1.add(accessTokenLabel);
        panel1.add(accessTokenField);
        panel1.add(buttonSave);

        JPanel panel2 = new JPanel();
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

        syncPanel = new JPanel();
        JLabel sync = new JLabel();
        sync.setText("<html><a href=\"\">sync</a></html>");
        sync.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sync.addMouseListener(new SyncListener());

        syncPanel.add(sync);

        panel.add(panel1);
        panel.add(syncPanel);

        return panel;

    }

    class SyncListener extends MouseAdapter {

        public SyncListener() {
            super();
        }

        public void mouseClicked(MouseEvent e) {
            //find activities selected
            final StravaAthlete athlete = stravaSetting.getCurrentAthleteSelected();

            new SwingWorker<Void, String>() {
                final JLabel label = new JLabel("Loading... ", JLabel.CENTER);
                List<StravaActivity> activities;

                @Override
                protected Void doInBackground() throws Exception {
                    syncPanel.add(label, BorderLayout.CENTER);
                    label.setVisible(true);

                    activities = StravaController.getInstance(athlete.getAccessToken()).getAllActivities();
                    if (activities != null && !activities.isEmpty()) {

                        Component[] components = stravaActivitySelection.getActivityPanel().getComponents();
                        if (components != null) {
                            for (Component component : components)
                                stravaActivitySelection.getActivityPanel().remove(component);
                        }

                        //This map must preserve insertion order
                        Map<String, StravaActivity> mapActivities = new LinkedTreeMap();
                        Map<String, String> mapActivitiesByName = new HashMap(activities.size());
                        for (StravaActivity activity : activities) {
                            JCheckBox temp = new JCheckBox(activity.getName() + "-" + activity.getDate());
                            temp.setSelected(activity.isSelected());
                            temp.addItemListener(new StravaActivityCheckListener(athlete));
                            stravaActivitySelection.getActivityPanel().add(temp);

                            //athlete --> activities
                            mapActivities.put(activity.getId(), activity);
                            //TODO is need????
                            mapActivitiesByName.put(activity.getName() + "-" + activity.getDate(), activity.getId());

                        }
                        athlete.setActivities(mapActivities);
                        athlete.setActivitiesByName(mapActivitiesByName);

                        stravaActivitySelection.getActivityPanel().validate();
                        stravaActivitySelection.getActivityPanel().repaint();
                    }

                    return null;
                }

                @Override
                protected void done() {

                    label.setVisible(false);
                    if (activities == null || activities.isEmpty())
                        JOptionPane.showMessageDialog(currentFrame,
                                "No activities selected",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                }
            }.execute();

        }
    }


}

