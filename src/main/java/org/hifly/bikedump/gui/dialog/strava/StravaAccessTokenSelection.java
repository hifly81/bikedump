package org.hifly.bikedump.gui.dialog.strava;

import org.hifly.bikedump.domain.strava.StravaAthlete;
import org.hifly.bikedump.domain.strava.StravaSetting;

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


public class StravaAccessTokenSelection extends JDialog {

    private static final long serialVersionUID = 14L;

    private StravaSetting stravaSetting;
    private JPanel panelRadio = null;

    public StravaAccessTokenSelection(Frame frame, final StravaSetting stravaSetting) {
        super(frame, true);

        this.stravaSetting = stravaSetting;

        setTitle("Strava Access Token Selection");

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Strava Access Token Selection", null, createPanel(),"Strava Access Token Selection");

        setContentPane(tabbedPane);

        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    public JPanel createPanel() {
        JPanel panel = new JPanel();

        panelRadio = new JPanel();
        Border titleBorder = new TitledBorder(new LineBorder(Color.RED), "Strava Access Token list");
        panelRadio.setBorder(titleBorder);

        RadioListener myListener = new RadioListener();

        ButtonGroup group = new ButtonGroup();

        List<StravaAthlete> tokens = stravaSetting.getStravaAthletes();
        if(tokens != null && !tokens.isEmpty()) {
            for(StravaAthlete token:tokens) {
                JRadioButton temp = new JRadioButton(token.getAccessToken());
                temp.setSelected(token.isSelected());
                temp.addActionListener(myListener);
                group.add(temp);

                JLabel tokenDelete = new JLabel();
                tokenDelete.setText("<html><a href=\"\">delete</a></html>");
                tokenDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
                tokenDelete.addMouseListener(new AccessTokenDeleteListener(token));

                panelRadio.add(temp);
                panelRadio.add(tokenDelete);
            }

        }

        panel.add(panelRadio);

        return panel;
    }

    class RadioListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String tokenName = e.getActionCommand();
            List<StravaAthlete> athletes = stravaSetting.getStravaAthletes();
            for(StravaAthlete athlete:athletes) {
                if(athlete.getAccessToken().equalsIgnoreCase(tokenName)) {
                    athlete.setSelected(true);
                    stravaSetting.setCurrentAthleteSelected(athlete);
                    break;
                }
            }

        }
    }

    class AccessTokenDeleteListener extends MouseAdapter{
        private StravaAthlete token;

        public AccessTokenDeleteListener(StravaAthlete token) {
            super();
            this.token = token;

        }
        public void mouseClicked(MouseEvent e) {
            List<StravaAthlete> tokens = stravaSetting.getStravaAthletes();
            int tokenIndex = 0;
            boolean foundToken = false;
            for(StravaAthlete temp:tokens) {
                if(temp.getAccessToken().equalsIgnoreCase(token.getAccessToken())) {
                    foundToken = true;
                    break;
                }
                tokenIndex++;
            }

            if(foundToken) {
                tokens.remove(tokenIndex);
                panelRadio.remove(tokenIndex);
                int tokenIndex2 = tokenIndex++;
                panelRadio.remove(tokenIndex2);

                validate();
                repaint();

            }
        }
    }

}
