package org.hifly.bikedump.gui.panel;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

public class HTMLEditorPanel extends JTextPane {

    private static final long serialVersionUID = 23L;

    public HTMLEditorPanel() {
        super();
        setContentType("text/html");

        // Use Look&Feel defaults instead of hardcoding Arial 12
        Font font = UIManager.getFont("Label.font");
        if (font == null) font = getFont();

        Color fg = UIManager.getColor("Label.foreground");
        if (fg == null) fg = getForeground();

        Color bg = UIManager.getColor("Panel.background");
        if (bg == null) bg = getBackground();

        setFont(font);
        setForeground(fg);
        setBackground(bg);
        setOpaque(true);

        // Apply CSS that follows current LAF colors + font
        String bodyRule = "body {"
                + " font-family: " + font.getFamily() + ";"
                + " font-size: " + font.getSize() + "pt;"
                + " color: " + toCss(fg) + ";"
                + " background-color: " + toCss(bg) + ";"
                + " }"
                + " a { color: " + toCss(UIManager.getColor("Component.linkColor") != null ? UIManager.getColor("Component.linkColor") : fg) + "; }";

        ((HTMLDocument) getDocument()).getStyleSheet().addRule(bodyRule);

        // Optional: remove default margin that sometimes looks "old"
        ((HTMLDocument) getDocument()).getStyleSheet().addRule("body { margin: 6px; }");
    }

    public void append(Color c, String text) {
        Document doc = getDocument();
        Reader r = new StringReader(text);
        EditorKit kit = getEditorKit();
        try {
            kit.read(r, doc, doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addHyperlinkImg(URL url, String text, String alt, Color color) {
        try {
            Document doc = this.getDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            StyleConstants.setUnderline(attrs, true);
            StyleConstants.setForeground(attrs, color);
            attrs.addAttribute(HTML.Attribute.HREF, url.toString());
            attrs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.IMG);
            attrs.addAttribute(HTML.Attribute.SRC, text);
            attrs.addAttribute(HTML.Attribute.ALT, alt);
            doc.insertString(doc.getLength(), " ", attrs);
        } catch (BadLocationException e) {
            e.printStackTrace(System.err);
        }
    }

    public void addImg(String text, String imgText) {
        try {
            Document doc = this.getDocument();
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttribute(StyleConstants.NameAttribute, HTML.Tag.IMG);
            attrs.addAttribute(HTML.Attribute.SRC, text);
            doc.insertString(doc.getLength(), " ", attrs);
            doc.insertString(doc.getLength(), " " + imgText, null);
        } catch (BadLocationException e) {
            e.printStackTrace(System.err);
        }
    }

    private static String toCss(Color c) {
        if (c == null) return "#000000";
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}