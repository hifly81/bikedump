package org.hifly.geomapviewer.gui.panel;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
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


        Font font = new Font("Arial", Font.PLAIN, 10) ;
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; }";
        ((HTMLDocument)getDocument()).getStyleSheet().addRule(bodyRule);

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

    public void addHyperlinkImg(URL url, String text, Color color) {
        try {
            Document doc = this.getDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setUnderline(attrs, true);
            StyleConstants.setForeground(attrs, color);
            attrs.addAttribute(HTML.Attribute.HREF, url.toString());
            attrs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.IMG);
            attrs.addAttribute(HTML.Attribute.SRC, text);
            doc.insertString(doc.getLength(), " ", attrs);
        } catch (BadLocationException e) {
            e.printStackTrace(System.err);
        }
    }

    public void addImg(String text,String imgText) {
        try {
            Document doc = this.getDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.IMG);
            attrs.addAttribute(HTML.Attribute.SRC, text);
            doc.insertString(doc.getLength(), " ", attrs);
            doc.insertString(doc.getLength(), " "+imgText, null);
        } catch (BadLocationException e) {
            e.printStackTrace(System.err);
        }
    }



}
