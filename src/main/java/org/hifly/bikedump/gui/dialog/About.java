package org.hifly.bikedump.gui.dialog;

import javax.swing.*;
import java.awt.*;

import static org.hifly.bikedump.utility.Constants.PROGRAM_NAME;

public class About extends JDialog {

    private static final long serialVersionUID = 16L;

    public About() {
        initUI();
    }

    public final void initUI() {

        JPanel basic = new JPanel();
        basic.setLayout(new BoxLayout(basic, BoxLayout.Y_AXIS));
        add(basic);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        JTextPane pane = new JTextPane();

        pane.setContentType("text/html");
        //TODO from external changes
        String text = "<p><b>Bikedump v.0.2</b></p><p>- About panel</p><p>- increased font size for track information</p><p>- executable jar available</p>";
        pane.setText(text);
        pane.setEditable(false);
        textPanel.add(pane);

        basic.add(textPanel);


        setTitle(PROGRAM_NAME);
        setSize(new Dimension(450, 350));
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }
}
