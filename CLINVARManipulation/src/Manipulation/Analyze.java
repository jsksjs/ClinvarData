package Manipulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Analyze {
    public static void main(String[] args) throws Exception {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        HashSet<String> pathoIdSet = new HashSet<>();
        HashMap<String, Integer> symbolFreq = new HashMap<>();
        // register driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // connect
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/clinvar?" +
                    "user=root&password=jsksjs&verifyServerCertificate=false&useSSL=false");

        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
        // queries
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM VARIANTS WHERE INFO='CLNSIG=Pathogenic'");
            ResultSetMetaData meta = rs.getMetaData();
            int columns = meta.getColumnCount();
            // write patho to file
            FileWriter pathoFW = new FileWriter("patho.csv");
            BufferedWriter pathoBW = new BufferedWriter(pathoFW);
            // write column names
            for (int i = 1; i < columns; i++) {
                pathoBW.write(meta.getColumnLabel(i) + ",");
            }
            pathoBW.write(meta.getColumnLabel(columns));
            pathoBW.write("\n");
            // write row data and add pathogenic genes to set
            while (rs.next()) {
                for (int i = 1; i < columns; i++) {
                    pathoBW.write(rs.getString(i) + ",");
                    if (meta.getColumnLabel(i).equals("ID")) {
                        pathoIdSet.add(rs.getString(i));
                    }
                }
                pathoBW.write(rs.getString(columns) + "\n");
            }
            pathoBW.close();
            pathoFW.close();

            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM VARIANTS WHERE INFO LIKE '%GENEINFO%'");
            // write symbolFreq to file
            FileWriter freqFW = new FileWriter("symbolPathoFreq.csv");
            BufferedWriter freqBW = new BufferedWriter(freqFW);
            // collect frequency of genes
            while (rs.next()) {
                String id = "";
                String symbol = rs.getString(columns).split(":")[0].split("=")[1];
                for (int i = 1; i < columns; i++) {
                    if (meta.getColumnLabel(i).equals("ID")) {
                        id = rs.getString(i);
                        break;
                    }
                }
                if (pathoIdSet.contains(id))
                    symbolFreq.merge(symbol, 1, (x, y) -> x + y);
            }
            freqBW.write("Symbol,Frequency\n");
            for (Map.Entry<String, Integer> entry : symbolFreq.entrySet()) {
                freqBW.write(entry.getKey() + "," + entry.getValue() + "\n");
            }
            freqBW.close();
            freqFW.close();

        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
        }
    }
}
