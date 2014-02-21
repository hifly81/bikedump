package org.hifly.geomapviewer.gui.tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

/**
 * @author
 * @date 12/02/14
 */
public class EmptyCatalogTree {

    public static DefaultMutableTreeNode createNodes(List<String> trackNames) {
        //create the root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Loaded Tracks");
        for(String trackName:trackNames) {
            DefaultMutableTreeNode trackNode = new DefaultMutableTreeNode(trackName);
            root.add(trackNode);
        }
        return root;
    }
}
