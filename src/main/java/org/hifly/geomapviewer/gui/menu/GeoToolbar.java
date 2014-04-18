package org.hifly.geomapviewer.gui.menu;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * @author
 * @date 20/02/14
 */
public class GeoToolbar extends JToolBar {

    protected JButton backButton;
    protected JButton graphButton;
    protected JButton reportButton;


    public GeoToolbar() {
        super();

        addButtons();
    }

    protected void addButtons() {
        URL backImageUrl = getClass().getResource("/img/back.png");
        ImageIcon backImageIcon = new ImageIcon(backImageUrl);
        Image img = backImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        backImageIcon = new ImageIcon(img);
        backButton = makeNavigationButton("Back","Back","Back",backImageIcon);
        URL graphImageUrl = getClass().getResource("/img/bar-chart-icon.png");
        ImageIcon graphImageIcon = new ImageIcon(graphImageUrl);
        img = graphImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        graphImageIcon = new ImageIcon(img);
        graphButton = makeNavigationButton("Graph","Graph","Graph",graphImageIcon);
        URL reportImageUrl = getClass().getResource("/img/report.png");
        ImageIcon reportImageIcon = new ImageIcon(reportImageUrl);
        img = reportImageIcon.getImage();
        img = img.getScaledInstance(16, 16,  java.awt.Image.SCALE_SMOOTH);
        reportImageIcon = new ImageIcon(img);
        reportButton = makeNavigationButton("Report","Report","Report",reportImageIcon);
        add(backButton);
        add(graphButton);
        add(reportButton);

    }

    protected JButton makeNavigationButton(
                                           String actionCommand,
                                           String toolTipText,
                                           String altText,
                                           ImageIcon icon) {
        JButton button = null;
        if(icon!=null) {
            button = new JButton(icon);
        }
        else {
            button = new JButton();
            button.setText(altText);
        }
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);


        return button;

    }

    public JButton getGraphButton() {
        return graphButton;
    }

    public void setGraphButton(JButton graphButton) {
        this.graphButton = graphButton;
    }

    public JButton getReportButton() {
        return reportButton;
    }

    public void setReportButton(JButton reportButton) {
        this.reportButton = reportButton;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public void setBackButton(JButton backButton) {
        this.backButton = backButton;
    }
}
