package org.hifly.geomapviewer.gui.menu;

import javax.swing.*;

/**
 * @author
 * @date 20/02/14
 */
public class GeoToolbar extends JToolBar {

    protected JButton graphButton;

    public GeoToolbar() {
        super();

        addButtons();
    }

    protected void addButtons() {
        makeNavigationButton("Graph", "Graph","Graph","Graph");
        add(graphButton);

    }

    protected void makeNavigationButton(String imageName,
                                           String actionCommand,
                                           String toolTipText,
                                           String altText) {
        graphButton = new JButton();
        graphButton.setActionCommand(actionCommand);
        graphButton.setToolTipText(toolTipText);
        graphButton.setText(altText);

    }

    public JButton getGraphButton() {
        return graphButton;
    }

    public void setGraphButton(JButton graphButton) {
        this.graphButton = graphButton;
    }
}
