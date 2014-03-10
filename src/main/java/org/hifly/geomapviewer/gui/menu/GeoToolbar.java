package org.hifly.geomapviewer.gui.menu;

import javax.swing.*;

/**
 * @author
 * @date 20/02/14
 */
public class GeoToolbar extends JToolBar {

    protected JButton graphButton;
    protected JButton reportButton;

    public GeoToolbar() {
        super();

        addButtons();
    }

    protected void addButtons() {
        graphButton = makeNavigationButton("Graph", "Graph","Graph","Graph");
        reportButton = makeNavigationButton("Report", "Report","Report","Report");
        add(graphButton);
        add(reportButton);

    }

    protected JButton makeNavigationButton(String imageName,
                                           String actionCommand,
                                           String toolTipText,
                                           String altText) {
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.setText(altText);

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
}