
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;


public class ConvertChineseLabels {


    public static void main(String[] args) {

        try {

            //create BufferedReader to read csv file
            //BufferedReader br = new BufferedReader(new FileReader("../../qiaoling_excel_sheet/Data_spot_check_P8_with_placeids2.csv"));
            BufferedReader br = new BufferedReader(new FileReader("../../qiaoling_excel_sheet/Data_spot_check_P10_with_placeids2.csv"));

            //PrintWriter out = new PrintWriter(new FileWriter("../../qiaoling_excel_sheet/extractedP8.txt"));
            PrintWriter out = new PrintWriter(new FileWriter("../../qiaoling_excel_sheet/extractedP10.txt"));
            
            //PrintWriter outResultSet = new PrintWriter(new FileWriter("../../qiaoling_excel_sheet/extractedResultSetP8.txt"));
            PrintWriter outResultSet = new PrintWriter(new FileWriter("../../qiaoling_excel_sheet/extractedResultSetP10.txt"));


            String strLine = "";
            StringTokenizer st = null;

            ArrayList<String> noDuplCluster = null;
            ArrayList<ArrayList<String>> dupClusters = null;

            ArrayList<String> curCluster = null;


            String curQueryId = "";
            String curClusterId = "";

            while ((strLine = br.readLine()) != null) {

                strLine = strLine.replaceAll(",,", ",NULL,");
                //System.out.println("debug: "+ strLine);

                //break comma separated line using "\t"
                st = new StringTokenizer(strLine, ",");

                if (st.hasMoreTokens()) {
                    String placeid = st.nextToken().trim();
                    String newClusterId = st.nextToken();
                    String newQueryId = st.nextToken();

                    if (newQueryId.equals(curQueryId)) {
                        outResultSet.print("\t");
                        
                        if (curClusterId.equals(newClusterId)) {
                            System.out.println("same cluster");
                            curCluster.add(placeid);
                        } else if (newClusterId.equals("NULL")) {
                            if (!curClusterId.equals("")) {
                                System.out.println("end cluster " + curClusterId);
                            }
                            System.out.println("no dup");
                            noDuplCluster.add(placeid);
                            curClusterId = "";
                        } else {
                            if (!curClusterId.equals("")) {
                                System.out.println("end cluster " + curClusterId);
                            }
                            System.out.println("new cluster");
                            curClusterId = newClusterId;
                            curCluster = new ArrayList<String>();
                            curCluster.add(placeid);
                            dupClusters.add(curCluster);
                        }
                    } else if (curQueryId.equals("")) {
                        System.out.println("new query " + newQueryId);
                        noDuplCluster = new ArrayList<String>();
                        dupClusters = new ArrayList<ArrayList<String>>();
                        curQueryId = newQueryId;
                        if (!newClusterId.equals("NULL")) {
                            System.out.println("new cluster");
                            curClusterId = newClusterId;
                            curCluster = new ArrayList<String>();
                            curCluster.add(placeid);
                            dupClusters.add(curCluster);
                        } else {
                            System.out.println("no dup");
                            noDuplCluster.add(placeid);
                        }
                    } else {
                        outResultSet.print("\n");

                        if (!curClusterId.equals("")) {
                            System.out.println("end cluster " + curClusterId);
                        }
                        System.out.println("query finished");
                        System.out.println("no dups " + noDuplCluster);
                        System.out.println("dups " + dupClusters);
                        for (ArrayList<String> cluster : dupClusters) {
                            for (int i = 0; i < cluster.size(); i++) {
                                for (int j = i + 1; j < cluster.size(); j++) {
                                    System.out.println("1|qiaoling|" + cluster.get(i) + "|" + cluster.get(j));
                                    out.println("1|qiaoling|" + cluster.get(i) + "|" + cluster.get(j));
                                }
                            }
                        }
                        for (int i = 0; i < noDuplCluster.size(); i++) {
                            for (int j = i + 1; j < noDuplCluster.size(); j++) {
                                System.out.println("-1|qiaoling|" + noDuplCluster.get(i) + "|" + noDuplCluster.get(j));
                                out.println("-1|qiaoling|" + noDuplCluster.get(i) + "|" + noDuplCluster.get(j));
                            }
                        }
                        for (int i = 0; i < noDuplCluster.size(); i++) {
                            for (ArrayList<String> cluster : dupClusters) {
                                for (int j = 0; j < cluster.size(); j++) {
                                    System.out.println("-1|qiaoling|" + noDuplCluster.get(i) + "|" + cluster.get(j));
                                    out.println("-1|qiaoling|" + noDuplCluster.get(i) + "|" + cluster.get(j));
                                }
                            }
                        }

                        for (int i = 0; i < dupClusters.size(); i++) {
                            for (String pid_outer : dupClusters.get(i)) {
                                for (int j = i + 1; j < dupClusters.size(); j++) {
                                    for (String pid_inner : dupClusters.get(j)) {
                                        System.out.println("-1|qiaoling|" + pid_outer + "|" + pid_inner);
                                        out.println("-1|qiaoling|" + pid_outer + "|" + pid_inner);
                                    }
                                }
                            }
                        }


                        System.out.println("new query " + newQueryId);
                        noDuplCluster = new ArrayList<String>();
                        dupClusters = new ArrayList<ArrayList<String>>();
                        curQueryId = newQueryId;

                        if (newClusterId.equals("NULL")) {
                            System.out.println("no dup");
                            noDuplCluster.add(placeid);
                            curClusterId = "";
                        } else {
                            curClusterId = newClusterId;
                            System.out.println("new cluster " + curClusterId);
                            curCluster = new ArrayList<String>();
                            curCluster.add(placeid);
                            dupClusters.add(curCluster);
                        }


                    }
                    System.out.println("placeid: " + placeid + " clusterid : " + newClusterId + " queryid : " + newQueryId);

                    outResultSet.print(placeid);

                }

            }

            if (!curClusterId.equals("")) {
                System.out.println("end cluster " + curClusterId);
            }
            System.out.println("query finished");
            System.out.println("no dups " + noDuplCluster);
            System.out.println("dups " + dupClusters);
            for (ArrayList<String> cluster : dupClusters) {
                for (int i = 0; i < cluster.size(); i++) {
                    for (int j = i + 1; j < cluster.size(); j++) {
                        System.out.println("1|qiaoling|" + cluster.get(i) + "|" + cluster.get(j));
                        out.println("1|qiaoling|" + cluster.get(i) + "|" + cluster.get(j));
                    }
                }
            }
            for (int i = 0; i < noDuplCluster.size(); i++) {
                for (int j = i + 1; j < noDuplCluster.size(); j++) {
                    System.out.println("-1|qiaoling|" + noDuplCluster.get(i) + "|" + noDuplCluster.get(j));
                    out.println("-1|qiaoling|" + noDuplCluster.get(i) + "|" + noDuplCluster.get(j));
                }
            }
            for (int i = 0; i < noDuplCluster.size(); i++) {
                for (ArrayList<String> cluster : dupClusters) {
                    for (int j = 0; j < cluster.size(); j++) {
                        System.out.println("-1|qiaoling|" + noDuplCluster.get(i) + "|" + cluster.get(j));
                        out.println("-1|qiaoling|" + noDuplCluster.get(i) + "|" + cluster.get(j));
                    }
                }
            }

            for (int i = 0; i < dupClusters.size(); i++) {
                for (String pid_outer : dupClusters.get(i)) {
                    for (int j = i + 1; j < dupClusters.size(); j++) {
                        for (String pid_inner : dupClusters.get(j)) {
                            System.out.println("-1|qiaoling|" + pid_outer + "|" + pid_inner);
                            out.println("-1|qiaoling|" + pid_outer + "|" + pid_inner);
                        }
                    }
                }
            }


            out.close();

            br.close();

            outResultSet.close();

        }
        catch (Exception e) {
            System.out.println("###########Exception: " + e);
            e.printStackTrace();
            System.exit(-1);
        }

    }

}
 