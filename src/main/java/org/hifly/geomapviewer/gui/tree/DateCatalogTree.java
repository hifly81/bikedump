package org.hifly.geomapviewer.gui.tree;

import org.hifly.geomapviewer.domain.Track;

import javax.swing.tree.DefaultMutableTreeNode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author
 * @date 12/02/14
 */
public class DateCatalogTree {

    public static DefaultMutableTreeNode createNodes(List<Track> tracks) {
        //create the root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Date");
        //Sort by date
        Collections.sort(tracks, new Comparator<Track>() {
            public int compare(Track o1, Track o2) {
                return o1.getStartDate().compareTo(o2.getStartDate());
            }
        });

        HashMap<String,String> dateNodes = new HashMap();

        //Extract date
        for(Track track:tracks) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(track.getStartDate());
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH)+1;
            if(dateNodes.get(month+"/"+year)==null) {
                DefaultMutableTreeNode dateNode = new DefaultMutableTreeNode(month+"/"+year);
                DefaultMutableTreeNode trackNode = new DefaultMutableTreeNode(track.getName());
                dateNode.add(trackNode);
                dateNodes.put(month+"/"+year,month+"/"+year);
                root.add(dateNode);
            }
            else {
                //find the node
                Enumeration<DefaultMutableTreeNode> e = root.depthFirstEnumeration();
                while (e.hasMoreElements()) {
                    DefaultMutableTreeNode node = e.nextElement();
                    if (node.toString().equalsIgnoreCase(month+"/"+year)) {
                        DefaultMutableTreeNode trackNode = new DefaultMutableTreeNode(track.getName());
                        node.add(trackNode);
                        break;
                    }
                }
            }


        }
        return root;
    }
}
