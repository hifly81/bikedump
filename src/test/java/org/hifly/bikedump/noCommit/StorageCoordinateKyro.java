package org.hifly.bikedump.noCommit;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author
 * @date 01/02/14
 */
public class StorageCoordinateKyro {
    public static void main (String[]args) throws Exception {

        Map<String,Double> mapStorage = new HashMap();

        File root = new File("/home/hifly/Dropbox/Docs/CYCLING_MTB/ROUTE/elevation_stats");
        try {
            boolean recursive = true;

            Collection files = FileUtils.listFiles(root, null, recursive);
            int total = 0;
            int totalFiles = 0;
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                File file = (File) iterator.next();
                if(file.getName().endsWith("storage_coordinates_kyro.db")
                        ||
                        file.getName().endsWith("storage_coordinates.db")
                        ||
                        file.getName().endsWith("storage.log")
                        ||
                        file.getName().endsWith("storage_kyro.log"))
                    continue;

                totalFiles++;

                System.out.println("Parsing file"+file.getAbsolutePath());

                InputStream fis;
                BufferedReader br;
                String         line;

                fis = new FileInputStream(file);
                br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

                while ((line = br.readLine()) != null) {
                    String[] splitter = line.split("-");
                    String s1 = splitter[0];
                    String s2 = splitter[1];
                    int lenghtDec = s1.substring(s1.lastIndexOf('.')).length();
                    int lenghtDec2 = s2.substring(s2.lastIndexOf('.')).length();
                    if(lenghtDec<7) {
                        for(int i=lenghtDec;i<7;i++) {
                            s1 = s1.concat("0");
                        }
                    }
                    if(lenghtDec2<7) {
                        for(int i=lenghtDec2;i<7;i++) {
                            s2 = s2.concat("0");
                        }
                    }
                    mapStorage.put(s1+"-"+s2,Double.valueOf(splitter[2]));
                    total++;
                }

                br.close();



            }
            System.out.println("Parsed files:"+totalFiles);
            System.out.println("Parsed coordinates:"+total);
            System.out.println("Storage size:"+mapStorage.size());

            File file = new File(
                    "/home/hifly/Dropbox/Docs/CYCLING_MTB/ROUTE/elevation_stats/storage_kyro.log");
            BufferedWriter output = new BufferedWriter(new FileWriter(file));
            output.append(new Date()+"\n");
            output.append("Parsed files:"+totalFiles+"\n");
            output.append("Parsed coordinates:" + total + "\n");
            output.append("Storage size:" + mapStorage.size() + "\n");
            output.append("------------------------------------------\n");
            output.close();


            File file2 = new File(
                    "/home/hifly/Dropbox/Docs/CYCLING_MTB/ROUTE/elevation_stats/storage_coordinates_kyro.db");
            file2.delete();

            FileOutputStream fos =
                    new FileOutputStream("/home/hifly/Dropbox/Docs/CYCLING_MTB/ROUTE/elevation_stats/storage_coordinates_kyro.db");


            System.out.println("Start create db");


            Kryo kryo = new Kryo();
            com.esotericsoftware.kryo.serializers.MapSerializer serializer =
                    new com.esotericsoftware.kryo.serializers.MapSerializer();
            kryo.register(HashMap.class, serializer);

            Output outputKyro = new Output(fos);
            kryo.writeObject(outputKyro, mapStorage);
            outputKyro.close();


            /*ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mapStorage);
            oos.close();        */

            System.out.println("End create db");



        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
