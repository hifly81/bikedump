package org.hifly.geomapviewer.gui.dialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @autho
 * @date 07/04/14
 */
public class ScrollableDialog {

    JDialog dialog;
    JTextArea textArea;
    int dialogResult = JOptionPane.CANCEL_OPTION;

    public ScrollableDialog(Frame owner, String text, int w,int h){
        // create a modal dialog that will block until hidden
        dialog = new JDialog(owner, true);
        dialog.setLayout(new BorderLayout());
        textArea = new JTextArea(text);
        textArea.setLineWrap(true);

        JScrollPane scrollpane = new JScrollPane(textArea);
        dialog.add(scrollpane,BorderLayout.CENTER);

        JButton btnOk = new JButton("Ok");
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // This will "close" the modal dialog and allow program
                // execution to continue.
                dialogResult = JOptionPane.OK_OPTION;
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        dialog.add(btnOk,BorderLayout.SOUTH);

        dialog.setSize(w,h);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation((dim.width-dialog.getWidth())/2, (dim.height-dialog.getHeight())/2);
    }

    public int showMessage(){
        // show the dialog - this will block until setVisible(false) occurs
        dialog.setVisible(true);
        // return whatever data is required
        return dialogResult;
    }
}
