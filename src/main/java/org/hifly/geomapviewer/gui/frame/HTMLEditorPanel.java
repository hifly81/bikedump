package org.hifly.geomapviewer.gui.frame;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

/**
 * @author
 * @date 25/02/14
 */
public class HTMLEditorPanel extends JTextPane {

    public HTMLEditorPanel () {
        super();
        setContentType("text/html");
    }


    public void append(Color c, String text) {
        Document doc = getDocument();
        Reader r = new StringReader(text);
        EditorKit kit = getEditorKit();
        try {
            kit.read(r, doc, doc.getLength());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void addHyperlink(URL url, String text, Color color) {
        try {
            Document doc = this.getDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setUnderline(attrs, true);
            StyleConstants.setForeground(attrs, color);
            attrs.addAttribute(HTML.Attribute.HREF, url.toString());
            doc.insertString(doc.getLength(), text, attrs);
        } catch (BadLocationException e) {
            e.printStackTrace(System.err);
        }
    }



}
