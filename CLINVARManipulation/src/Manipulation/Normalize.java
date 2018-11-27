package Manipulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class Normalize {
    public static void main(String[] args) {
        try {
            File f = new File("clinvar_20180401.vcf");
            FileInputStream fs = new FileInputStream(f);
            byte[] bText = new byte[(int) f.length()];
            fs.read(bText);
            fs.close();
            String text = new String(bText, "UTF-8");
            ArrayList<String> lines = new ArrayList<>(Arrays.asList(text.split("\n")));
            int i = 0;
            while (i < lines.size()) {
                if (lines.get(i).startsWith("##")) {
                    lines.remove(i);
                } else if (lines.get(i).startsWith("#")) {
                    lines.set(i, lines.get(i).replace("#", ""));
                    String[] split = lines.get(i).split("\t");
                    StringBuilder comma = new StringBuilder();
                    for (int j = 0; j < split.length; j++) {
                        if (j != split.length - 1) {
                            comma.append(split[j]).append(",");
                        } else {
                            comma.append(split[j]);
                        }
                    }
                    lines.set(i, comma.toString());
                    i++;
                } else {
                    String[] split = lines.get(i).split("\t");
                    String fullInfo = split[split.length-1];
                    String[] splitInfo = fullInfo.split(";");
                    boolean found = false;
                    for (String part : splitInfo) {
                        if (part.contains("CLNVC=") || part.contains("CLNSIG=")
                                || part.contains("RS=") || part.contains("GENEINFO=")) {
                            if (found) {
                                String[] temp = split.clone();
                                temp[7] = part;
                                StringBuilder sb = new StringBuilder();
                                if (part.contains("GENEINFO=") && part.split("\\|").length > 1) {
                                    String[] genes = part.split("\\|");
                                    for (int j = 0; j < temp.length - 1; j++) {
                                        sb.append(temp[j]).append(",");
                                    }
                                    StringBuilder sbSec = new StringBuilder(sb.toString());
                                    sb.append(genes[0]);
                                    sbSec.append("GENEINFO=").append(genes[1]);
                                    lines.add(i, sb.toString());
                                    lines.add(i, sbSec.toString());
                                    i++;
                                    i++;
                                } else {
                                    for (int j = 0; j < temp.length; j++) {
                                        if (j != temp.length - 1) {
                                            sb.append(temp[j]).append(",");
                                        } else {
                                            sb.append(temp[j]);
                                        }
                                    }
                                    lines.add(i, sb.toString());
                                    i++;
                                }
                            } else {
                                String[] temp = split.clone();
                                temp[7] = part;
                                StringBuilder sb = new StringBuilder();
                                for (int j = 0; j < temp.length; j++) {
                                    if (j != temp.length - 1) {
                                        sb.append(temp[j]).append(",");
                                    } else {
                                        sb.append(temp[j]);
                                    }
                                }
                                lines.set(i, sb.toString());
                                found = true;
                            }
                        }
                    }
                    i++;
                }
            }
            FileWriter fw = new FileWriter("data.csv");
            BufferedWriter bw = new BufferedWriter(fw);
            for (int j = 0; j < lines.size(); j++) {
                bw.write(lines.get(j) + "\n");
            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
