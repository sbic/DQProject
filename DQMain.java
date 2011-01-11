
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.regex.Pattern;


/*
todo:

- middle insertion
- translations lernen, anz tokens gleich, mind. ein match dann differenz als feature, berlin dom vs. berlin cathedral oder cafe ritz vs. bar ritz
- cross-validation balancieren
- poly kernel, quadratic features

*/



public class DQMain {

    /*

    java -Xmx2500m DQMain 8 deu ../opr_deu_utf8_sort_all.csv_filtered ../model_file_deu ../where_sets_random_deu_s60_24808_shuffled.txt 1350
    java -Xmx2500m DQMain 9 deu ../opr_deu_utf8_sort_all.csv_filtered ../model_file_deu ../where_sets_random_deu_s60_24808_shuffled.txt 1350
    java -Xmx2500m DQMain 14 deu ../opr_deu_utf8_sort_all.csv_filtered ../model_file_deu ../where_sets_random_deu_s60_24808_shuffled.txt 1350
    java -Xmx2500m DQMain 16 deu ../opr_deu_utf8_sort_all.csv ../model_file_deu ../where_sets_random_deu_s60_24808_shuffled.txt 1350



    java -Xmx2500m DQMain 7 gbr ../opr_gbr_utf8_sort.csv ../model_file_gbr ../extracted_where_log_data_gbr_shuffled_head5000.csv | less
    java -Xmx2500m DQMain 8 gbr ../opr_gbr_utf8_sort.csv ../model_file_gbr ../extracted_where_log_data_gbr_shuffled_head5000.csv
    java -Xmx2500m DQMain 14 gbr ../opr_gbr_utf8_sort.csv ../model_file_gbr ../extracted_where_log_data_gbr_shuffled_head5000.csv  | less
    java -Xmx2500m DQMain 9 gbr ../opr_gbr_utf8_sort.csv ../model_file_gbr ../extracted_where_log_data_gbr_shuffled_head5000.csv | less

    java -Xmx2500m DQMain 8 deu ../opr_deu_utf8_sort_filtered.csv ../model_file_gbr ../where_sets_random_deu_s60_24808_shuffled.txt 1350
    java -Xmx2500m DQMain 8 deu ../opr_deu_utf8_sort_filtered.csv ../model_file_deu ../where_sets_random_deu_s60_24808_shuffled.txt 1350

    java -Xmx2500m DQMain 9 deu ../opr_deu_utf8_sort_filtered.csv ../model_file_gbr ../where_sets_random_deu_s60_24808_shuffled.txt 1350 | less
    java -Xmx2500m DQMain 9 deu ../opr_deu_utf8_sort_filtered.csv ../model_file_deu ../where_sets_random_deu_s60_24808_shuffled.txt 1350 | less
            
    java -Xmx2500m DQMain 8 deu ../opr_deu_utf8_sort_head50000.csv ../model_file_gbr ../where_sets_random_deu_s60_24808_shuffled.txt 1350



    java -Xmx2500m DQMain 13 deu ../opr_deu_utf8_sort.csv ../model_file_gbr ../where_sets_random_deu_s60_24808_shuffled.txt 40000
    java -Xmx2500m DQMain 13 deu ../opr_deu_utf8_sort_all.csv ../model_file_gbr ../where_sets_random_deu_s60_24808_shuffled.txt 40000
                           

    java -Xmx2500m DQMain 10 gbr ../opr_gbr_utf8_sort.csv ../model_file_gbr | less


    java -Xmx2000m DQMain 15 gbr ../opr_gbr_sort_en_en.csv ../model_file_gbr | less
    java -Xmx2000m DQMain 8 gbr ../opr_gbr_sort_en_en.csv ../model_file_gbr | less


    java -Xmx2000m DQMain 10 gbr ../opr_gbr_sort.csv ../model_file_gbr | less

    java -Xmx2000m DQMain 15 gbrx ../opr_gbr_sort_en_en.csv ../model_file_gbr | less

    java -Xmx2000m DQMain 15 deu ../opr_deu_sort100000.csv ../model_file_deu | less

     */


    // 0: write out automatically extracted placeIds of training pairs
    // 1: write out svm training data
    // 2: load model, validate training errors
    // 3: show wrong merges
    // 5: actively collect training data
    // 6: load raw where log pairs with counts
    // 7: write out svm training data from where logs only manually labeled data (param 5 is where log data file)
    // 8: load model, ask user for labels of data from where logs close to decision boundary (param 5 is where log data file)
    // 9: write out svm training data for manually and predicted data, first predict labels of unlabeled examples (param 5 is where log data file)
    // 10: load active evaluation set and draw samples and compute active evaluation measure
    // 11: label where logs with aggressive labeling definition

    // 12: load extracted where data and sample subset (faster if data is sorted descending by count)
    // 13: load where full result set data and filter to only places that are in where data and write out opr data
    // 14: load model, show pairs close to decision boundary (training error)
    // 15: cross-validation on complete result sets
    // 16: create and save category-token dictionaries


    static int mode = 1;
    static String taskName;
    static String freeCommandLineParam1;

    static String modelFileName = "../model_file_gbr";
    static String svmTrainDataFileName = "../train_gbr_400000.svm";

    static boolean isWhereLogFullResultSetFormat;


    static String svmTrainDataWhereFileName;


    //csv file containing data
//            String rawOPRDataFileName = "./opr_test4_sort.csv";
//            String rawOPRDataFileName = "./opr_berlin.csv";
    //          String rawOPRDataFileName = "./opr_london.csv";
//           String rawOPRDataFileName = "./opr_deu_sort.csv";
//           String rawOPRDataFileName = "./opr_deu_sort100000.csv";
//           String rawOPRDataFileName = "./opr_gbr_sort.csv";
    static String rawOPRDataFileName = "../opr_gbr_sort_en_en.csv";
    //static String rawWhereLogFileName = "../raw_where_pairs_test.csv";
    static String rawWhereLogFileName = "../raw_where_pairs.csv";

    static String extractedWhereLogDataFileName = "../extracted_where_log_data.csv";
    static String whereLogDataFileName;

    static String extractedWhereLogDataSampleActiveEvalFileName = "../extracted_where_log_data_shuffled_subset5001_100000.csv";


    static String extractedWhereLogDataSubsetFileName = "../extracted_where_log_subset_data.csv";

    static String extractedManualAndAutomaticLabelsOPRFormatFilename = "../extractedManualAndAutomaticLabelsOPRFormat";

    static String manuallyLabeledDataFileName = "../manual_labels.txt";
    static String manuallyLabeledDataWhereFileName;

    static String manuallyLabeledDataWhereAggrFileName = "../manual_labels_where_aggr.txt";

    static String manuallyLabeledDataActiveEvalFileName = "../manual_labels_active_eval.txt";

    static String autoLabeledDataFileName = "../automatic_labels.txt";

    static String dictionariesFileName = "../dictionaries";
    static HashMap<String, HashMap<String, HashMap<String, Integer>>> languageCategoryDictionaries = null;


    static boolean debugOutput = false;
    //  static boolean debugOutput = true;


    static FeatureProcessor featureProcessor = null;

    static Model model = null;
    static GeoHash gh = null;
    static HashMap<ByteArrayWrapper, Place> placeMap = null;

    static HashMap<ByteArrayWrapper, Integer> trainDataManualMap = null;
    static HashMap<ByteArrayWrapper, Integer> trainDataWhereManualMap = null;
    static HashMap<ByteArrayWrapper, Integer> trainDataWhereAggrManualMap = null;
    static HashMap<ByteArrayWrapper, Integer> extractedUnlabeledDataWhereMap = null;

    static HashMap<ByteArrayWrapper, Integer> trainDataAutoMap = null;

    static int numberOfCrossValidationSplits = 10;


    static final Charset FILE_CHARSET = Charset.forName("ISO-8859-1");

    static double probabilityThreshold = 0.5;

    public static void main(String[] args) {
         /*
         //MatchTokenizer mt = new MatchTokenizer("abcxxewe", "eweabfabcwer");
        //  MatchTokenizer mt = new MatchTokenizer("zababcd", "abcdxab");
        // MatchTokenizer mt = new MatchTokenizer("strandperle övelgönne", "strandperle medien services e k");
        MatchTokenizer mt = new MatchTokenizer("ndperle övel", "strandperle medien services e k");

        System.out.println(" match tokenizer " + mt);

        System.exit(-1);
           */
        /*
System.out.println(java.util.Arrays.toString("a s".split(" ")));
System.out.println(java.util.Arrays.toString("a  s".split(" ")));
System.out.println(java.util.Arrays.toString("a   s".split(" ")));

System.out.println("aa   sss".split(" ")[1].length());

System.exit(-1);
        */
        //  System.out.println(" levenst. " + getLevenshteinDistance("bp", "b", 1000, null));


        //  String s = "55";
        //  System.out.println("out: " + s.matches(".*\\d+.*"));
        //  System.exit(-1);


        // read command line arguments
        if (args.length > 0) {
            try {
                mode = Integer.parseInt(args[0]);
                System.out.println("mode: " + mode);
            }
            catch (NumberFormatException nfe) {
                System.out.println("NumberFormatException: " + nfe.getMessage());
            }
        }


        if (args.length > 1) {
            taskName = args[1];
        }
        isWhereLogFullResultSetFormat = !taskName.equals("gbr");

        if (args.length > 2) {
            rawOPRDataFileName = args[2];
        }

        if (args.length > 3) {
            modelFileName = args[3];
        }

        if (args.length > 4) {
            whereLogDataFileName = args[4];
        }

        if (args.length > 5) {
            freeCommandLineParam1 = args[5];
        }


        svmTrainDataWhereFileName = "../train_where_" + taskName + ".svm";
        System.out.println("svm training data file name: " + svmTrainDataWhereFileName);
        manuallyLabeledDataWhereFileName = "../manual_labels_where_" + taskName + ".txt";
        System.out.println("manual labels file name: " + manuallyLabeledDataWhereFileName);
        extractedManualAndAutomaticLabelsOPRFormatFilename = extractedManualAndAutomaticLabelsOPRFormatFilename + "_" + taskName + ".csv";
        dictionariesFileName = dictionariesFileName + "_" + taskName + ".csv";

        featureProcessor = new FeatureProcessor(debugOutput, dictionariesFileName);


        if (mode == 2 || mode == 3 || mode == 5 || mode == 6 || mode == 8 || mode == 9 || mode == 10 || mode == 14 || mode == 15) {
            try {
                model = loadModel(new File(modelFileName));
                System.out.println(model);
            }
            catch (Exception e) {
                System.out.println("###########Exception: " + e);
                e.printStackTrace();
                System.exit(-1);
            }
        }

        if (mode != 13) {
            if (mode == 16) {
                readRawOPRDataMultiLanguage(rawOPRDataFileName, false, true);
                featureProcessor.writeDictionaries();
                System.exit(1);
            } else {
                readRawOPRDataMultiLanguage(rawOPRDataFileName, true, false);
                //featureProcessor.loadDictionaries();
            }
        }


        if (mode == 1 || mode == 2 || mode == 3 || mode == 5 || mode == 6) {
            trainDataAutoMap = new HashMap<ByteArrayWrapper, Integer>(100);
            trainDataManualMap = new HashMap<ByteArrayWrapper, Integer>(100);
            readLabels(autoLabeledDataFileName, trainDataAutoMap);
            readLabels(manuallyLabeledDataFileName, trainDataManualMap);
        }

/*
        // somewhere in berlin
        double lat1 = 52.5004;
        double lat2 = 52.50035;
        double lon1 = 13.4006;
        double lon2 = 13.40055 ;

        double lat3 = -33.94025;
        double lon3 = 151.22942;


        // nearby search
        double search_radius = 0.5; // 1 km
        ArrayList<Place> search_results;
        search_results = gh.nearBySearch(lat3, lon3, search_radius, 1);

        System.out.println("found nearby: " + search_results.size());
  //      for(int i=0;i<search_results.size();i++) {
  //              System.out.println(search_results.get(i));
  //      }
*/


        if (mode == 0) {
            PrintWriter outAutoLabeledTrainData = null;
            try {
                outAutoLabeledTrainData = new PrintWriter(new FileWriter(autoLabeledDataFileName));
                // output merged as positive examples
                int countMerged = 0;
                Collection places = placeMap.values();
                Iterator places_it = places.iterator();
                while (places_it.hasNext()) {
                    Place refPlace = ((Place) places_it.next());
                    if (refPlace.mergedWith != null) {
                        Place mergedWithPlace = placeMap.get(refPlace.mergedWith);
                        if (mergedWithPlace != null) {
                            countMerged++;
                            debugOutputln("countMerged: " + countMerged);
                            storeAutoLabel(outAutoLabeledTrainData, +1, refPlace, mergedWithPlace, "merged");

                        }
                    }
                }
                System.out.println("extracted positives: " + countMerged);


                // output random pairs as negative examples
                Place[] placesArray = (Place[]) places.toArray(new Place[0]);
                //###############################################
                //countMerged=1000;

                int countNeg = 0;
                Random randomGenerator = new Random(1234561);
                while (countNeg < countMerged) {
                    int randomRefId = randomGenerator.nextInt(placesArray.length);
                    Place refPlace = placesArray[randomRefId];
                    if (!refPlace.merged) {
                        double search_radius = 1.0;
                        ArrayList<Place> searchResults;
                        searchResults = gh.nearBySearch(refPlace.latitude, refPlace.longitude, search_radius, 1);
                        if (!searchResults.isEmpty()) {
                            Place[] searchResultsArray = (Place[]) searchResults.toArray(new Place[0]);
                            int randomCandId = randomGenerator.nextInt(searchResultsArray.length);
                            Place candPlace = searchResultsArray[randomCandId];
                            if (!candPlace.merged && !refPlace.placeId.equals(candPlace.placeId)) {
                                //if(refPlace.distance(candPlace)>0.5) {
                                //    System.out.println("extracted negative distance: " + refPlace.distance(candPlace));
                                countNeg++;
                                debugOutputln("countNeg: " + countNeg);
                                storeAutoLabel(outAutoLabeledTrainData, -1, refPlace, candPlace, "merged");
                                //}
                            }
                        }
                    }
                }
                System.out.println("extracted negatives: " + countNeg);
                outAutoLabeledTrainData.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (mode == 1 || mode == 2 || mode == 3) {
            PrintWriter outSvmTrainData = null;

            try {

                if (mode == 1)
                    outSvmTrainData = new PrintWriter(new FileWriter(svmTrainDataFileName));

                int count = 0;
                HashMap<ByteArrayWrapper, Integer> trainDataManualMapCopy = null;
                trainDataManualMapCopy = new HashMap<ByteArrayWrapper, Integer>(trainDataManualMap);

                Collection trainDataCollection = trainDataAutoMap.keySet();
                Iterator trainDataIterator = trainDataCollection.iterator();
                while (trainDataIterator.hasNext()) {

                    ByteArrayWrapper placeId0 = ((ByteArrayWrapper) trainDataIterator.next());
                    int label = trainDataAutoMap.get(placeId0);
                    int originalLabel = label;
                    //System.out.println("read auto labels: " + placeId0);
                    ByteArrayWrapper placeId1 = placeId0.splitUpPlaceIdPair();

                    //System.out.println("read auto labels split: " + placeId0 + " " + placeId1 + " " + placeId0.getData().length + " " + placeId1.getData().length + " " + label);

                    Place place0 = placeMap.get(placeId0);
                    Place place1 = placeMap.get(placeId1);
                    if (place0 != null && place1 != null) {
                        boolean validated = false;

                        Integer labelObj = trainDataManualMap.get(new ByteArrayWrapper(placeId0, placeId1));
                        if (labelObj != null) {
                            debugOutputln("overwrite label old: " + label + " new: " + labelObj);
                            label = labelObj;
                            validated = true;

                            trainDataManualMapCopy.remove(new ByteArrayWrapper(placeId0, placeId1));
                            //  System.out.println("debug map copy: " + trainDataManualMapCopy.containsKey(new ByteArrayWrapper(placeId0, placeId1)) + " " + trainDataManualMap.containsKey(new ByteArrayWrapper(placeId0, placeId1)));
                        }


                        if (label != 0) {
                            FeatureVector featureVector = featureProcessor.extractFeatures(place0, place1, label);

                            if (mode == 1) {
                                count++;
                                featureVector.writeSVMlightWriteFeatures(outSvmTrainData, label);
                            } else if (mode == 2) {
                                //System.out.println("pos - prediction: " + predict(featureList, model,2,0));
                                if (!validated && featureVector.predict(model, 0, 0) * label < 0) {
                                    if (label == 1)
                                        collectLabel(manuallyLabeledDataFileName, label, place0, place1, featureVector, "validated-merged");
                                    else
                                        collectLabel(manuallyLabeledDataFileName, label, place0, place1, featureVector, "validated-negative");
                                } else if (!validated && ((label > 0 && place0.notEqualsStrongIdentifiers(place1)) || (label < 1 && place0.equalsStrongIdentifiers(place1)))) {
                                    //System.out.println("debug: " + ((label > 0 && place0.notEqualsStrongIdentifiers(place1)) || (label < 1 && place0.equalsStrongIdentifiers(place1))) + " " + place0.notEqualsStrongIdentifiers(place1) + " " + label);

                                    if (label == 1)
                                        collectLabel(manuallyLabeledDataFileName, label, place0, place1, featureVector, "validated-merged");
                                    else
                                        collectLabel(manuallyLabeledDataFileName, label, place0, place1, featureVector, "validated-negative");
                                }
                            } else if (mode == 3) {
                                if (originalLabel > 0 && label < 1) {
                                    count++;
                                    System.out.println("count: " + count);
                                    //System.out.println("placeId from auto labels not found: " + placeId0 + " " + placeId1 + " " + place0 + " " + place1);
                                    collectLabel("dummy_label.file", originalLabel, place0, place1, featureVector, "wrong-merged");

                                }
                            }
                        } else {
                            // System.out.println("label 0: " + placeId0 + " " + placeId1);
                        }
                    } else {
                        System.out.println("placeId from auto labels not found: " + placeId0 + " " + placeId1 + " " + place0 + " " + place1);
                    }
                }
                System.out.println("num data written: " + count);

                if (mode == 1)
                    outSvmTrainData.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
                        int count = 0;
                        if (mode == 1) {
                            trainDataCollection = trainDataManualMapCopy.keySet();
                            trainDataIterator = trainDataCollection.iterator();
                            while (trainDataIterator.hasNext()) {

                                ByteArrayWrapper placeId0 = ((ByteArrayWrapper) trainDataIterator.next());
                                int label = trainDataManualMapCopy.get(placeId0);
                                if (label != 0) {
                                    count++;
                                    //System.out.println("read auto labels: " + placeId0);
                                    ByteArrayWrapper placeId1 = placeId0.splitUpPlaceIdPair();
                                    Place place0 = placeMap.get(placeId0);
                                    Place place1 = placeMap.get(placeId1);
                                    if (place0 != null && place1 != null) {
                                        TreeSet<Feature> featureList = new TreeSet<Feature>();
                                        extractFeatures(place0, place1, featureList, label);
                                        if (mode == 1) {
                                            writeSVMlightWriteFeatures(outSvmTrainData, label, featureList);
                                        }
                                    }
                                }
                            }
                            System.out.println("count additional manual data: " + count);
                        }

        */
        /*

  // output merged as positive examples
  int countMerged = 0;
  Collection places = placeMap.values();
  Iterator places_it = places.iterator();
  while (places_it.hasNext()) {
      Place refPlace = ((Place) places_it.next());
      if (refPlace.mergedWith != null) {
          Place mergedWithPlace = placeMap.get(refPlace.mergedWith);
          if (mergedWithPlace != null) {
              countMerged++;
              debugOutputln("countMerged: " + countMerged);
              TreeSet<Feature> featureList = new TreeSet<Feature>();
              extractFeatures(refPlace, mergedWithPlace, featureList);

              int label = 1;
              if (mode == 0) {

              } else {
                  boolean validated = false;
                  Integer labelObj = trainDataManualMap.get(new ByteArrayWrapper(refPlace.placeId, mergedWithPlace.placeId));
                  if (labelObj != null) {
                      debugOutputln("overwrite label old: " + label + " new: " + labelObj);
                      label = labelObj;
                      validated = true;


                  }

                  if (label != 0) {
                      if (mode == 1) {
                          writeSVMlightWriteFeatures(outSvmTrainData, label, featureList);
                      } else if (mode == 2) {
                          //System.out.println("pos - prediction: " + predict(featureList, model,2,0));
                          if (!validated && predict(featureList, model, 0, 0) * label < 0) {
                              collectLabel(manuallyLabeledDataFileName, label, refPlace, mergedWithPlace, featureList, "validated-merged");
                          }

                      }
                  }
              }

          }
      }
  }




  System.out.println("negatives ----------------- ");

  // output random pairs as negative examples
  Place[] placesArray = (Place[]) places.toArray(new Place[0]);
  //###############################################
  //countMerged=1000;

  int countNeg = 0;
  Random randomGenerator = new Random(123456);
  while (countNeg < countMerged) {
      int randomRefId = randomGenerator.nextInt(placesArray.length);
      Place refPlace = placesArray[randomRefId];
      if (!refPlace.merged) {
          double search_radius = 0.5; // 200m
          ArrayList<Place> searchResults;
          searchResults = gh.nearBySearch(refPlace.latitude, refPlace.longitude, search_radius, 1);
          if (!searchResults.isEmpty()) {
              Place[] searchResultsArray = (Place[]) searchResults.toArray(new Place[0]);
              int randomCandId = randomGenerator.nextInt(searchResultsArray.length);
              Place candPlace = searchResultsArray[randomCandId];
              if (!candPlace.merged && !refPlace.placeId.equals(candPlace.placeId)) {
                  countNeg++;
                  debugOutputln("countNeg: " + countNeg);
                  TreeSet<Feature> featureList = new TreeSet<Feature>();
//                                System.out.println("distance neg:" + GeoHash.distance(refPlace.latitude, candPlace.latitude, refPlace.longitude, candPlace.longitude));
                  extractFeatures(refPlace, candPlace, featureList);

                  int label = -1;
                  if (mode == 0) {

                  } else {
                      boolean validated = false;
                      Integer labelObj = trainDataManualMap.get(new ByteArrayWrapper(refPlace.placeId, candPlace.placeId));
                      if (labelObj != null) {
                          debugOutputln("overwrite label old: " + label + " new: " + labelObj);
                          label = labelObj.intValue();
                          validated = true;
                      }
                      if (label != 0) {
                          if (mode == 1) {
                              writeSVMlightWriteFeatures(outSvmTrainData, label, featureList);

                          }
                      } else if (mode == 2) {
                          //System.out.println("neg - prediction: " + predict(featureList, model,2,0));
                          if (!validated && predict(featureList, model, 0, 0) * label < 0) {
                              collectLabel(manuallyLabeledDataFileName, label, refPlace, candPlace, featureList, "validated-negative");
                          }
                      }
                  }
              }
          }
      }
  }

  if (mode == 1)
      outSvmTrainData.close();

        */


        /*
                    double search_radius = 1.0; // 1 km
                    ArrayList<Place> search_results;
                    search_results = gh.nearBySearch(refPlace.latitude, refPlace.longitude, search_radius, 1);

                    //System.out.println("found nearby: " + search_results.size());


                    for(int i=0;i<search_results.size();i++) {
                        Place duplPlace = search_results.get(i);
                        if(refPlace != duplPlace) {
                            if(refPlace.equalsPhone(duplPlace) || refPlace.equalsEmail(duplPlace) || refPlace.equalsWebsite(duplPlace)) {
        System.out.println("\n" + refPlace.toString(duplPlace));
                        //        System.out.println("\n\nrefplace: " + refPlace);
                        //        System.out.println("dupplace: " + duplPlace);
                            }
                        }
                    }
        */


        if (mode == 5) {

            // actively collect train data

            Collection places = placeMap.values();
            Place[] placesArray = (Place[]) places.toArray(new Place[0]);

            int countCollected = 0;
            int countPredicted = 0;

            Random randomGenerator = new Random(123456);
            while (true) {

                PredictedPair currentBest = null;

                for (int i = 0; i < 500; i++) {
                    int randomRefId = randomGenerator.nextInt(placesArray.length);
                    Place refPlace = placesArray[randomRefId];
                    if (!refPlace.merged) {
                        double search_radius = 1.0;
                        ArrayList<Place> searchResults;
                        searchResults = gh.nearBySearch(refPlace.latitude, refPlace.longitude, search_radius, 1);
                        if (!searchResults.isEmpty()) {

                            Place[] searchResultsArray = searchResults.toArray(new Place[0]);
                            for (int cand = 0; cand < searchResultsArray.length; cand++) {
                                Place candPlace = searchResultsArray[cand];
                                if (!candPlace.merged && !refPlace.placeId.equals(candPlace.placeId)) {

                                    Integer labelObj = trainDataManualMap.get(new ByteArrayWrapper(refPlace.placeId, candPlace.placeId));
                                    if (labelObj == null) {
                                        countPredicted++;
                                        if (countPredicted % 1000 == 0)
                                            System.out.print(".");
                                        debugOutputln("count: " + countPredicted);
                                        FeatureVector featureVector = featureProcessor.extractFeatures(refPlace, candPlace, 0);
                                        PredictedPair candPredictedPair = new PredictedPair(Math.abs(featureVector.predict(model, 2, 0) - probabilityThreshold), refPlace, candPlace, featureVector, 0);

                                        if (currentBest == null || currentBest.compareTo(candPredictedPair) > 0)
                                            currentBest = candPredictedPair;

                                        //System.out.println("collect candidates count: " + count + " currentBest: " + currentBest);

                                        //System.out.println("collect candidates count: " + count + " " + (new PredictedPair(Math.abs(predict(featureList, model,1,0)),refPlace.placeId,candPlace.placeId)) + " " + predict(featureList, model,1,0) + " " + predict(featureList, model,2,0) + " " + featureList);
                                    } else {
                                        //System.out.println("candidate already in train data: " + refPlace.placeId + " " + candPlace.placeId);
                                    }
                                }
                            }
                        }
                    }
                }

                System.out.println("best candidate: " + currentBest);
                collectLabel(manuallyLabeledDataFileName, 0, currentBest.place1, currentBest.place2, currentBest.featureVector, "random-active");
                countCollected++;

            }
        }

        if (mode == 6) {

            try {

                BufferedReader br = new BufferedReader(new FileReader(rawWhereLogFileName));
                PrintWriter extractedWhereLogWriter = null;
                extractedWhereLogWriter = new PrintWriter(new FileWriter(extractedWhereLogDataFileName));


                String strLine = "";
                StringTokenizer st = null;
                int placeCount = -1;
                String curPlaceId = "", newPlaceId = "";
                Place curPlace = null;
                String s;

                int count = 0;
                int countLabel = 0;
                int countFound = 0;
                //read comma separated file line by line
                while ((strLine = br.readLine()) != null) {
                    count++;

                    st = new StringTokenizer(strLine, "\t|");

                    int pairCount;
                    String countString = st.nextToken();
                    pairCount = Integer.parseInt(countString);
                    String placeId0String = st.nextToken();
                    String placeId1String = st.nextToken();

                    Place place0 = placeMap.get(new ByteArrayWrapper(placeId0String));
                    if (place0 != null) {
                        Place place1 = placeMap.get(new ByteArrayWrapper(placeId1String));
                        if (place1 != null) {
                            countFound++;

                            Integer labelManual = trainDataManualMap.get(new ByteArrayWrapper(place0.placeId, place1.placeId));
                            Integer labelAuto = trainDataAutoMap.get(new ByteArrayWrapper(place0.placeId, place1.placeId));

                            if (labelManual != null) {
                                if (labelManual != 0) {
                                    countLabel++;
                                    for (int i = 0; i < pairCount; i++) {
                                        extractedWhereLogWriter.println(labelManual + "|1|" + DQUtils.sortConcatStringPairsBar(placeId0String, placeId1String));
                                    }
                                    //System.out.println("manual match count: " + countString + " p0: " + placeId0String + " p1: " + placeId1String);
                                }
                            } else if (labelAuto != null) {
                                countLabel++;
                                for (int i = 0; i < pairCount; i++) {
                                    extractedWhereLogWriter.println(labelAuto + "|1|" + DQUtils.sortConcatStringPairsBar(placeId0String, placeId1String));
                                }
                                // System.out.println("auto match count: " + countString + " p0: " + placeId0String + " p1: " + placeId1String);

                            } else {
                                for (int i = 0; i < pairCount; i++) {
                                    extractedWhereLogWriter.println("0|1|" + DQUtils.sortConcatStringPairsBar(placeId0String, placeId1String));
                                }
                            }

                        }
                    }
                    //    dataMap.put(new ByteArrayWrapper(placeId1, placeId2), new Integer(label));

                    //    debugOutputln("debug load train data: " + (new ByteArrayWrapper(placeId1, placeId2)) + " label: " + label);


                    if (count % 10000 == 0)
                        System.out.print(".");

                    if (count % 1000000 == 0)
                        System.out.println("\n count: " + count + " countFound: " + countFound + " countLabel: " + countLabel);

                }
                System.out.println("\n count: " + count + " countFound: " + countFound + " countLabel: " + countLabel);

                extractedWhereLogWriter.close();
                br.close();
            }
            catch (Exception e) {
                System.out.println("###########Exception: " + e);
                e.printStackTrace();
                System.exit(-1);
            }
        }


        if (!isWhereLogFullResultSetFormat) {
            if (mode == 7 || mode == 8 || mode == 9 || mode == 11 || mode == 14) {

                trainDataWhereManualMap = new HashMap<ByteArrayWrapper, Integer>(100);
                readLabels(manuallyLabeledDataWhereFileName, trainDataWhereManualMap);

                extractedUnlabeledDataWhereMap = new HashMap<ByteArrayWrapper, Integer>(100);
                readLabels(whereLogDataFileName, extractedUnlabeledDataWhereMap);

                trainDataWhereAggrManualMap = new HashMap<ByteArrayWrapper, Integer>(100);

                PrintWriter outSvmTrainData = null;
                PrintWriter extractedManualAndAutomaticLabelsOPRFormat = null;

                TreeSet<PredictedPair> predictions = null;

                try {

                    if (mode == 7 || mode == 9)
                        outSvmTrainData = new PrintWriter(new FileWriter(svmTrainDataWhereFileName));
                    else if (mode == 8 || mode == 14)
                        predictions = new TreeSet<PredictedPair>();

                    int count = 0;

                    Collection trainDataCollection = extractedUnlabeledDataWhereMap.keySet();
                    Iterator trainDataIterator = trainDataCollection.iterator();
                    while (trainDataIterator.hasNext()) {

                        ByteArrayWrapper placeId0 = ((ByteArrayWrapper) trainDataIterator.next());
                        // placeId0 contains both place ids concatenated and will be split up later
                        int label = extractedUnlabeledDataWhereMap.get(placeId0);

                        boolean validated = false;
                        boolean ignore = false;
                        // System.out.println("debug1: " + label);
                        Integer labelObj = trainDataWhereManualMap.get(placeId0);
                        if (labelObj != null) {

                            debugOutputln("overwrite label old: " + label + " new: " + labelObj);
                            //System.out.println("overwrite label old: " + label + " new: " + labelObj + " " + placeId0);
                            label = labelObj;
                            validated = true;
                            if (labelObj == 0) {
                                ignore = true;
                            }

                            //  System.out.println("debug map copy: " + trainDataManualMapCopy.containsKey(new ByteArrayWrapper(placeId0, placeId1)) + " " + trainDataManualMap.containsKey(new ByteArrayWrapper(placeId0, placeId1)));
                        }
                        //   System.out.println("debug2: " + label);


                        //System.out.println("read auto labels: " + placeId0);
                        ByteArrayWrapper placeId1 = placeId0.splitUpPlaceIdPair();

                        //System.out.println("read auto labels split: " + placeId0 + " " + placeId1 + " " + placeId0.getData().length + " " + placeId1.getData().length + " " + label);

                        Place place0 = placeMap.get(placeId0);
                        Place place1 = placeMap.get(placeId1);
                        if (place0 != null && place1 != null) {

                            // System.out.println("count: " + count + " label: " + label);
                            if (mode == 7 && label != 0) {
                                count++;
                                FeatureVector featureVector = featureProcessor.extractFeatures(place0, place1, label);

                                featureVector.writeSVMlightWriteFeatures(outSvmTrainData, label);
                            } else if (mode == 9 && !ignore) {
                                if (place0.merged || place1.merged) {
                                    System.out.println("merged:");
                                }
                                count++;

                                FeatureVector featureVector = featureProcessor.extractFeatures(place0, place1, label);
                                if (label == 0) {
                                    double score = featureVector.predict(model, 2, 0);
                                    if (score > 0.5)
                                        label = 1;
                                    else
                                        label = -1;
                                    //System.out.println("debug score label: " + score + " " + predLabel);
                                }

                                featureVector.writeSVMlightWriteFeatures(outSvmTrainData, label);

                                writeOPRData(label, validated, place0.placeId.toString(), place1.placeId.toString());
                            } else if ((mode == 8 && !validated) || mode == 14) {

                                /*
                                if(label==1 && validated && place0.distance(place1)>2) {
                                    System.out.println("\n\n\n" + place0.toString(place1));
                                }
                                  */

                                FeatureVector featureVector = featureProcessor.extractFeatures(place0, place1, label);

                                double score = featureVector.predict(model, 2, 0);

                                int predictedLabel;
                                if (score > 0.5) {
                                    predictedLabel = 1;
                                } else {
                                    predictedLabel = -1;
                                }
                                if (mode == 8 && place0.distance(place1) < 1 && !validated && ((predictedLabel > 0 && place0.notEqualsStrongIdentifiers(place1)) || (predictedLabel < 1 && place0.equalsStrongIdentifiers(place1)))) {
                                    System.out.println("tempLabel: " + predictedLabel);

                                    collectLabel(manuallyLabeledDataWhereFileName, 0, place0, place1, featureVector, "validated");
                                } else if (mode == 8) {
                                    predictions.add(new PredictedPair(Math.abs(score), place0, place1, featureVector, label));
                                } else if (!validated || label != 0) {
                                    //System.out.println("debug prob: " + score);
                                    if (!validated) {
                                        label = predictedLabel;
                                    }
                                    if (label > 0) {
                                        predictions.add(new PredictedPair(score, place0, place1, featureVector, label));
                                    } else if (label < 0) {
                                        predictions.add(new PredictedPair(1 - score, place0, place1, featureVector, label));
                                    }
                                }
                            } else if (mode == 11 && label == -1) {

                                if (place0.normalizedName.equals(place1.normalizedName) && place0.distance(place1) < 1.0) {
                                    collectLabel(manuallyLabeledDataWhereAggrFileName, label, place0, place1, null, "aggr_labels");


                                }
                                //runHungarianAlgorithm(p1, p2, p1.normalizedName, p2.normalizedName, "normalizedName_", featureList, false, label);


                            }
                        } else {
                            System.out.println("placeId not found in opr data: " + placeId0 + " " + placeId1 + " " + place0 + " " + place1);
                        }
                    }

                    if (mode == 8 || mode == 14) {
                        while (true) {
                            PredictedPair currentBest = null;
                            Iterator it = predictions.iterator();
                            if (mode == 8) {
                                while (it.hasNext()) {
                                    PredictedPair prediction = ((PredictedPair) it.next());
                                    if (currentBest == null || Math.abs(currentBest.predictionScore - probabilityThreshold) > Math.abs(prediction.predictionScore - probabilityThreshold))
                                        currentBest = prediction;
                                }
                            }
                            if (mode == 14) {
                                if (it.hasNext()) {
                                    currentBest = ((PredictedPair) it.next());
                                }
                            }
                            if (currentBest != null) {
                                count++;
                                if (mode == 8) {
                                    collectLabel(manuallyLabeledDataWhereFileName, 0, currentBest.place1, currentBest.place2, currentBest.featureVector, "validated-where");
                                } else if (mode == 14) {
                                    System.out.println("\n\n\n" + currentBest.place1.toString(currentBest.place2));
                                    double probability = currentBest.featureVector.predict(model, 2, 1);
                                    System.out.println("\nduplicate probability: " + probability + " score: " + currentBest.predictionScore + " label: " + currentBest.label);
                                }
                                predictions.remove(currentBest);
                                System.out.println("debug: " + count);
                            }
                        }
                    }

                    if (mode == 7 || mode == 9) {
                        System.out.println("num data written: " + count);
                        outSvmTrainData.close();
                    }
                } catch (IOException
                        e) {
                    e.printStackTrace();
                }
            }
        } else {
            // isWhereLogFullResultSetFormat == true

            if (mode == 7 || mode == 8 || mode == 9 || mode == 11 || mode == 14 || mode == 15) {

                trainDataWhereManualMap = new HashMap<ByteArrayWrapper, Integer>(100);
                readLabels(manuallyLabeledDataWhereFileName, trainDataWhereManualMap);

                TreeSet<PredictedPair> predictions = new TreeSet<PredictedPair>();

                ArrayList<ArrayList<PredictedPair>> predictionsGroupedByResultSet = new ArrayList<ArrayList<PredictedPair>>();

                PrintWriter outSvmTrainData = null;

                PrintWriter extractedManualAndAutomaticLabelsOPRFormat = null;

                int stopAtResultSetCount = Integer.parseInt(freeCommandLineParam1);

                try {

                    if (mode == 7 || mode == 9) {
                        outSvmTrainData = new PrintWriter(new FileWriter(svmTrainDataWhereFileName));
                    }

                    int pairCounter = 0;
                    int resultSetCounter = 0;
                    int nonSingleResultSetCounter = 0;
                    int processedPairCounter = 0;

                    int metricCountNumberOfResultSetsWithValidResults = 0;
                    int metricCountNumberOfResultSetsWithValidResultsAtLeast2 = 0;
                    int metricCountNumberOfValidResults = 0;
                    int metricCountNumberOfValidResultsAtLeast2 = 0;
                    int metricCountNumberOfDuplicateResults = 0;
                    int metricCountNumberOfResultSetsWithAtLeastOneDupl = 0;
                    boolean[] resultInvolvedInDuplicate = null;


                    BufferedReader br = new BufferedReader(new FileReader(whereLogDataFileName));
                    String strLine;
                    //read tab delimited file line by line
                    while ((strLine = br.readLine()) != null) {
                        resultSetCounter++;

                        ArrayList<PredictedPair> currentResultSetPredictions = null;

                        if (resultSetCounter > stopAtResultSetCount)
                            break;

                        //System.out.println("\nresult set: " + strLine + "\n");
                        StringTokenizer stringTokenizer1 = new StringTokenizer(strLine, "\t");

                        String placeIdInResultSet[] = strLine.split("\t");

                        boolean hasDuplicate = false;
                        boolean hasValidResult = false;
                        resultInvolvedInDuplicate = new boolean[10];

                        if (placeIdInResultSet.length > 1) {
                            nonSingleResultSetCounter++;
                            for (int i = 0; i < placeIdInResultSet.length; i++) {
                                //System.out.println("placeid: " + placeIdInResultSet[i]);
                                ByteArrayWrapper placeId0 = new ByteArrayWrapper(placeIdInResultSet[i]);
                                Place place0 = placeMap.get(placeId0);
                                if (place0 != null) {
                                    metricCountNumberOfValidResults++;
                                    metricCountNumberOfValidResultsAtLeast2++;
                                    if (!hasValidResult) {
                                        hasValidResult = true;
                                        metricCountNumberOfResultSetsWithValidResults++;
                                        metricCountNumberOfResultSetsWithValidResultsAtLeast2++;
                                    }

                                    for (int j = i + 1; j < placeIdInResultSet.length; j++) {

                                        ByteArrayWrapper placeId1 = new ByteArrayWrapper(placeIdInResultSet[j]);
                                        Place place1 = placeMap.get(placeId1);
                                        if (place1 != null) {

                                            ByteArrayWrapper placeIdPair = new ByteArrayWrapper(placeId0, placeId1);
                                            pairCounter++;
                                            //System.out.println("pair: " + placeIdInResultSet[i] + " " + placeIdInResultSet[j] + " " + placeIdPair);

                                            /*
                                            if (placeIdInResultSet[i].equals("276u33db-c4624626cafe49b3889c78d43b3913ea") && placeIdInResultSet[j].equals("276u33db-aa8715d04411454db076803c730e5c5a")) {
                                                System.out.println("pair: " + placeIdInResultSet[i] + " " + placeIdInResultSet[j] + " " + placeIdPair);
                                            }
                                            if (placeIdInResultSet[j].equals("276u33db-c4624626cafe49b3889c78d43b3913ea") && placeIdInResultSet[i].equals("276u33db-aa8715d04411454db076803c730e5c5a")) {
                                                System.out.println("pair: " + placeIdInResultSet[i] + " " + placeIdInResultSet[j] + " " + placeIdPair);
                                            }
                                            */

                                            boolean validated = false;
                                            boolean ignore = false;
                                            // System.out.println("debug1: " + label);

                                            int label = 0;
                                            Integer labelObj = trainDataWhereManualMap.get(placeIdPair);
                                            if (labelObj != null) {
                                                /*
                                                if(labelObj==1 && place0.distance(place1)>2) {
                                                    System.out.println("\n\n\n" + place0.toString(place1));
                                                }
                                                */


                                                debugOutputln("set overwrite label old: " + label + " new: " + labelObj);
                                                //System.out.println("overwrite label old: " + label + " new: " + labelObj + " " + placeId0);
                                                label = labelObj;
                                                validated = true;
                                                if (labelObj == 0) {
                                                    ignore = true;
                                                }

                                                //  System.out.println("debug map copy: " + trainDataManualMapCopy.containsKey(new ByteArrayWrapper(placeId0, placeId1)) + " " + trainDataManualMap.containsKey(new ByteArrayWrapper(placeId0, placeId1)));
                                            }
                                            //   System.out.println("debug2: " + label);


                                            //System.out.println("read auto labels split: " + placeId0 + " " + placeId1 + " " + placeId0.getData().length + " " + placeId1.getData().length + " " + label);


                                            // System.out.println("count: " + count + " label: " + label);
                                            if (mode == 7 && label != 0) {
                                                processedPairCounter++;
                                                FeatureVector featureVector = featureProcessor.extractFeatures(place0, place1, label);

                                                featureVector.writeSVMlightWriteFeatures(outSvmTrainData, label);
                                            } else if ((mode == 9 || mode == 15) && !ignore) {

                                                /*
                                                if (place0.merged || place1.merged) {
                                                    System.out.println("merged:");
                                                }
                                                */
                                                processedPairCounter++;

                                                FeatureVector featureVector = null;

                                                //     System.out.println("debug place: " + (place0 == null) + " " + (place1 == null));
                                                featureVector = featureProcessor.extractFeatures(place0, place1, label);

                                                if (label == 0) {
                                                    double score = featureVector.predict(model, 2, 0);
                                                    if (score > 0.5)
                                                        label = 1;
                                                    else
                                                        label = -1;
                                                    //System.out.println("debug score label: " + score + " " + predLabel);
                                                }


                                                if (label == 1) {
                                                    if (!resultInvolvedInDuplicate[j]) {
                                                        resultInvolvedInDuplicate[j] = true;
                                                        metricCountNumberOfDuplicateResults++;
                                                    }
                                                    if (!hasDuplicate) {
                                                        hasDuplicate = true;
                                                        metricCountNumberOfResultSetsWithAtLeastOneDupl++;
                                                    }
                                                }

                                                if (mode == 9) {
                                                    featureVector.writeSVMlightWriteFeatures(outSvmTrainData, label);
                                                } else if (mode == 15) {
                                                    if (currentResultSetPredictions == null) {
                                                        currentResultSetPredictions = new ArrayList<PredictedPair>();
                                                        predictionsGroupedByResultSet.add(currentResultSetPredictions);
                                                    }
                                                    currentResultSetPredictions.add(new PredictedPair(place0, place1, featureVector, label));
                                                }

                                                //writeOPRData(label, validated, place0.placeId.toString(), place1.placeId.toString());
                                            } else if ((mode == 8 && !validated) || mode == 14) {
                                                processedPairCounter++;
                                                FeatureVector featureVector = featureProcessor.extractFeatures(place0, place1, label);

                                                double score = featureVector.predict(model, 2, 0);

                                                int predictedLabel;
                                                if (score > 0.5) {
                                                    predictedLabel = 1;
                                                } else {
                                                    predictedLabel = -1;
                                                }
                                                if (mode == 8 && place0.distance(place1) < 1 && !validated && ((predictedLabel > 0 && place0.notEqualsStrongIdentifiers(place1)) || (predictedLabel < 1 && place0.equalsStrongIdentifiers(place1)))) {
                                                    System.out.println("tempLabel: " + predictedLabel);
                                                    collectLabel(manuallyLabeledDataWhereFileName, 0, place0, place1, featureVector, "validated");
                                                } else if (mode == 8) {
                                                    predictions.add(new PredictedPair(Math.abs(score), place0, place1, featureVector, label));
                                                } else if (!validated || label != 0) {

                                                    //System.out.println("debug prob: " + score);
                                                    if (!validated) {
                                                        label = predictedLabel;
                                                    }
                                                    if (label > 0) {
                                                        predictions.add(new PredictedPair(score, place0, place1, featureVector, label));
                                                    } else if (label < 0) {
                                                        predictions.add(new PredictedPair(1 - score, place0, place1, featureVector, label));
                                                    }
                                                }
                                            } else if (mode == 11 && label == -1) {
                                                if (place0.normalizedName.equals(place1.normalizedName) && place0.distance(place1) < 1.0) {
                                                    collectLabel(manuallyLabeledDataWhereAggrFileName, label, place0, place1, null, "aggr_labels");
                                                }
                                                //runHungarianAlgorithm(p1, p2, p1.normalizedName, p2.normalizedName, "normalizedName_", featureList, false, label);
                                            }
                                        }
                                    }

                                    //System.out.println("Iteration stats resultSetCounter: " + resultSetCounter + " nonSingleResultSetCounter: " + nonSingleResultSetCounter + " pairCounter: " + pairCounter + " processedPairCounter: " + processedPairCounter);
                                } else {
                                    //System.out.println("placeId not found in opr data: " + placeId0);
                                }

                            }
                        } else {
                            metricCountNumberOfResultSetsWithValidResults++;
                            metricCountNumberOfValidResults++;
                        }
                    }
                    System.out.println("Iteration stats resultSetCounter: " + resultSetCounter + " nonSingleResultSetCounter: " + nonSingleResultSetCounter + " pairCounter: " + pairCounter + " processedPairCounter: " + processedPairCounter);


                    if (mode == 8 || mode == 14) {
                        while (true) {
                            PredictedPair currentBest = null;
                            Iterator<PredictedPair> it = predictions.iterator();
                            if (mode == 8) {
                                while (it.hasNext()) {
                                    PredictedPair prediction = it.next();
                                    if (currentBest == null || Math.abs(currentBest.predictionScore - probabilityThreshold) > Math.abs(prediction.predictionScore - probabilityThreshold))
                                        currentBest = prediction;
                                }
                            }
                            if (mode == 14) {
                                if (it.hasNext()) {
                                    currentBest = ((PredictedPair) it.next());
                                }
                            }
                            if (currentBest != null) {
                                processedPairCounter++;
                                if (mode == 8) {
                                    collectLabel(manuallyLabeledDataWhereFileName, 0, currentBest.place1, currentBest.place2, currentBest.featureVector, "validated-where");
                                } else if (mode == 14) {
                                    System.out.println("\n\n\n" + currentBest.place1.toString(currentBest.place2));
                                    double probability = currentBest.featureVector.predict(model, 2, 1);
                                    System.out.println("\nduplicate probability: " + probability + " score: " + currentBest.predictionScore + " label: " + currentBest.label);
                                }
                                predictions.remove(currentBest);
                                System.out.println("debug: " + processedPairCounter);
                            }
                        }
                    }

                    if (mode == 7 || mode == 9) {
                        System.out.println("metric 2 (based all result sets with at least 1 POI): " + metricCountNumberOfResultSetsWithAtLeastOneDupl / ((double) metricCountNumberOfResultSetsWithValidResults));
                        System.out.println("metric 2 (based all result sets with at least 2 POIs): " + metricCountNumberOfResultSetsWithAtLeastOneDupl / ((double) metricCountNumberOfResultSetsWithValidResultsAtLeast2));

                        System.out.println("metric 3 (based all result sets with at least 1 POI): " + metricCountNumberOfDuplicateResults / ((double) metricCountNumberOfValidResults));
                        System.out.println("metric 3 (based all result sets with at least 2 POIs): " + metricCountNumberOfDuplicateResults / ((double) metricCountNumberOfValidResultsAtLeast2));

                        System.out.println("metricCountNumberOfResultSetsWithValidResults: " + metricCountNumberOfResultSetsWithValidResults);
                        System.out.println("metricCountNumberOfResultSetsWithValidResultsAtLeast2: " + metricCountNumberOfResultSetsWithValidResultsAtLeast2);
                        System.out.println("metricCountNumberOfValidResults: " + metricCountNumberOfValidResults);
                        System.out.println("metricCountNumberOfValidResultsAtLeast2: " + metricCountNumberOfValidResultsAtLeast2);
                        System.out.println("metricCountNumberOfDuplicateResults: " + metricCountNumberOfDuplicateResults);
                        System.out.println("metricCountNumberOfResultSetsWithAtLeastOneDupl: " + metricCountNumberOfResultSetsWithAtLeastOneDupl);


                        System.out.println("num data written: " + processedPairCounter);
                        outSvmTrainData.close();
                    }

                    if (mode == 15) {

                        int[] crossValidationSplits = new int[predictionsGroupedByResultSet.size()];
                        for (int i = 0; i < predictionsGroupedByResultSet.size(); i++) {
                            crossValidationSplits[i] = i % numberOfCrossValidationSplits;
                        }


                        for (int currentCrossValidationSplit = 0; currentCrossValidationSplit < numberOfCrossValidationSplits; currentCrossValidationSplit++) {
                            for (int i = 0; i < predictionsGroupedByResultSet.size(); i++) {
                                if (crossValidationSplits[i] == currentCrossValidationSplit) {
                                    // store evaluation data
                                } else {
                                    // store training data
                                }
                            }
                        }

                        System.out.println("after split");
                        /*
                        Iterator<PredictedPair> it = predictions.iterator();
                        while (it.hasNext()) {
                            PredictedPair prediction = it.next();
                            if (currentBest == null || Math.abs(currentBest.predictionScore - probabilityThreshold) > Math.abs(prediction.predictionScore - probabilityThreshold))
                                currentBest = prediction;
                        }
                          */


                    }
                }

                catch (Exception
                        e) {
                    System.out.println("###########Exception: " + e);
                    e.printStackTrace();
                    System.exit(-1);
                }


                /*



                */
            }

        }


        if (mode == 10)

        {

            extractedUnlabeledDataWhereMap = new HashMap<ByteArrayWrapper, Integer>(100);
            readLabels(extractedWhereLogDataSampleActiveEvalFileName, extractedUnlabeledDataWhereMap);

            int numPool = 0;
            int numPosPool = 0;
            double R = 0;

            Collection trainDataCollection = extractedUnlabeledDataWhereMap.keySet();
            Iterator<ByteArrayWrapper> trainDataIterator = trainDataCollection.iterator();
            while (trainDataIterator.hasNext()) {
                int label = 0;
                ByteArrayWrapper placeId0 = trainDataIterator.next();
                //System.out.println("read auto labels: " + placeId0);
                ByteArrayWrapper placeId1 = placeId0.splitUpPlaceIdPair();
                //System.out.println("read auto labels split: " + placeId0 + " " + placeId1 + " " + placeId0.getData().length + " " + placeId1.getData().length + " " + label);
                Place place0 = placeMap.get(placeId0);
                Place place1 = placeMap.get(placeId1);
                if (place0 != null && place1 != null) {
                    numPool++;

                    FeatureVector featureVector = featureProcessor.extractFeatures(place0, place1, label);
                    double pOf1 = featureVector.predict(model, 2, 0);
                    if (pOf1 > 0.5) {
                        label = 1;
                        numPosPool++;
                        R += (1 - pOf1);
                    } else {
                        label = -1;
                        R -= pOf1;
                    }
                } else {
                    System.out.println("placeId from where file not found: " + placeId0 + " " + placeId1 + " " + place0 + " " + place1);
                }
            }
            R = R / numPool;
            double R2 = R * R;
            double fEstimatedRatio = ((double) numPosPool / numPool);
            System.out.println("pos ratio estimated from f(x): " + R + " pos pred. ratio: " + fEstimatedRatio);

            TreeSet<PredictedPair> predictions = null;
            predictions = new TreeSet<PredictedPair>();

            int count2 = 0;
            double sumqOverPool = 0;
            extractedUnlabeledDataWhereMap = new HashMap<ByteArrayWrapper, Integer>(100);
            readLabels(extractedWhereLogDataSampleActiveEvalFileName, extractedUnlabeledDataWhereMap);
            trainDataCollection = extractedUnlabeledDataWhereMap.keySet();
            trainDataIterator = trainDataCollection.iterator();
            while (trainDataIterator.hasNext()) {
                int label = 0;
                ByteArrayWrapper placeId0 = trainDataIterator.next();
                //System.out.println("read auto labels: " + placeId0);
                ByteArrayWrapper placeId1 = placeId0.splitUpPlaceIdPair();
                //System.out.println("read auto labels split: " + placeId0 + " " + placeId1 + " " + placeId0.getData().length + " " + placeId1.getData().length + " " + label);
                Place place0 = placeMap.get(placeId0);
                Place place1 = placeMap.get(placeId1);
                if (place0 != null && place1 != null) {

                    count2++;

                    FeatureVector featureVector = featureProcessor.extractFeatures(place0, place1, label);
                    double pOf1 = featureVector.predict(model, 2, 0);

                    double q = compute_q(pOf1, R, R2);

                    predictions.add(new PredictedPair(q, place0, place1, null, label));

                    sumqOverPool += q;
                    //System.out.println("q debug pOf1: " + pOf1 + " q: " + q + " R: " + R + " R2: " + R2);

                    //if(count2 < 5000)
                    //    System.out.println(((float)pOf1) + ";" + ((float)q));

                } else {
                    System.out.println("placeId from where file not found: " + placeId0 + " " + placeId1 + " " + place0 + " " + place1);
                }
            }


            //Random random = new Random(12345678);
            Random random = new Random();

            double sumWeightedLossOverLabeled = 0;
            double sumWeighted2LossOverLabeled = 0;
            double sumWeighted2Loss2OverLabeled = 0;
            double sumImportanceWeights2OverLabeled = 0;

            double sumImportanceWeightsOverLabeled = 0;
            int numSamplesInEstimate = 0;


            // read previously actively labeled data
            HashMap<ByteArrayWrapper, Integer> activeEvalLabels, activeEvalCounts;
            activeEvalLabels = new HashMap<ByteArrayWrapper, Integer>(100);
            activeEvalCounts = new HashMap<ByteArrayWrapper, Integer>(100);
            readCountLabels(manuallyLabeledDataActiveEvalFileName, activeEvalLabels, activeEvalCounts);
            Collection<ByteArrayWrapper> colKey = activeEvalLabels.keySet();
            Collection<Integer> colValues = activeEvalLabels.values();
            Iterator<ByteArrayWrapper> itKey = colKey.iterator();
            Iterator<Integer> itValues = colValues.iterator();
            while (itKey.hasNext() && itValues.hasNext()) {
                ByteArrayWrapper placeIdPair = itKey.next();
                int label = itValues.next();
                Integer countObj = activeEvalCounts.get(placeIdPair);
                if (countObj != null) {
                    numSamplesInEstimate += countObj;
                    ByteArrayWrapper placeId0 = new ByteArrayWrapper();
                    ByteArrayWrapper placeId1 = new ByteArrayWrapper();
                    placeIdPair.splitUpPlaceIdPair(placeId0, placeId1);

                    Place place0 = placeMap.get(placeId0);
                    Place place1 = placeMap.get(placeId1);


                    FeatureVector featureVector = featureProcessor.extractFeatures(place0, place1, 0);

                    double pOf1 = featureVector.predict(model, 2, 0);
                    double q = compute_q(pOf1, R, R2);
                    if (pOf1 > 0.5 && label == -1) {
                        // false positive
                        sumWeightedLossOverLabeled += 1 / q;
                        sumWeighted2LossOverLabeled += 1 / (q * q);
                        sumWeighted2Loss2OverLabeled += 1 / (q * q);
                    } else if (pOf1 <= 0.5 && label == 1) {
                        // false negative
                        sumWeightedLossOverLabeled -= 1 / q;
                        sumWeighted2LossOverLabeled -= 1 / (q * q);
                        sumWeighted2Loss2OverLabeled += 1 / (q * q);
                    }
                    sumImportanceWeightsOverLabeled += 1 / q;
                    sumImportanceWeights2OverLabeled += 1 / (q * q);
                } else {
                    System.out.println("numPool not found:" + label + "|" + placeIdPair);
                }
            }


            while (true) {
                //for (int i = 0; i < 5; i++) {
                double rand = random.nextDouble();
                double binScore = rand * sumqOverPool;

                Iterator<PredictedPair> it = predictions.descendingIterator();
                double partSumq = 0;
                boolean found = false;
                PredictedPair prediction = null;
                while (it.hasNext()) {
                    prediction = it.next();
                    partSumq += prediction.predictionScore;

                    if (binScore < partSumq) {
                        found = true;
                        break;
                    }

                }
                if (found == false) {
                    System.out.println("q sampling, bin not found: " + binScore + " " + binScore + " " + rand + " " + partSumq);
                    System.exit(-1);
                } else {

                    System.out.println("q sampling, debug: " + binScore + " " + sumqOverPool + " " + rand + " " + partSumq + " " + prediction);


                    FeatureVector featureVector = featureProcessor.extractFeatures(prediction.place1, prediction.place2, 0);

                    int label = 0;
                    int sampleCount = 1;
                    Integer existingLabel = activeEvalLabels.get(new ByteArrayWrapper(prediction.placeId1, prediction.placeId2));
                    if (existingLabel != null) {
                        int currentCount = activeEvalCounts.get(new ByteArrayWrapper(prediction.placeId1, prediction.placeId2));
                        sampleCount += currentCount;
                        label = existingLabel;
                        System.out.println("existing label: " + label + " new sampleCount: " + sampleCount);

                    } else {
                        label = collectLabel(null, 0, prediction.place1, prediction.place2, featureVector, "active-eval");
                        // label = 1;
                    }


                    if (label != 0) {
                        System.out.println("label: " + label + " " + sumqOverPool + " " + rand + " " + partSumq + " " + prediction);

                        double pOf1 = featureVector.predict(model, 2, 0);

                        if (pOf1 > 0.5 && label == -1) {
                            // false positive
                            sumWeightedLossOverLabeled += 1 / prediction.predictionScore;
                            sumWeighted2LossOverLabeled += 1 / (prediction.predictionScore * prediction.predictionScore);
                            sumWeighted2Loss2OverLabeled += 1 / (prediction.predictionScore * prediction.predictionScore);
                        } else if (pOf1 <= 0.5 && label == 1) {
                            // false negative
                            sumWeightedLossOverLabeled -= 1 / prediction.predictionScore;
                            sumWeighted2LossOverLabeled -= 1 / (prediction.predictionScore * prediction.predictionScore);
                            sumWeighted2Loss2OverLabeled += 1 / (prediction.predictionScore * prediction.predictionScore);
                        }
                        sumImportanceWeightsOverLabeled += 1 / prediction.predictionScore;
                        sumImportanceWeights2OverLabeled += 1 / (prediction.predictionScore * prediction.predictionScore);
                        numSamplesInEstimate++;

                        if (sumImportanceWeightsOverLabeled > 0) {
                            double R_nq = sumWeightedLossOverLabeled / sumImportanceWeightsOverLabeled;
                            double R2_nq = R_nq * R_nq;
                            double S2 = sumWeighted2Loss2OverLabeled - R_nq * sumWeighted2LossOverLabeled + R2_nq * sumImportanceWeights2OverLabeled;
                            S2 = (sumqOverPool / (sumImportanceWeightsOverLabeled * numPool)) * S2;
                            double S = Math.sqrt(S2);
                            double alpha = 0.1;
                            double F = Probability.studentTInverse(alpha, numSamplesInEstimate);

                            //double z = F*(1-alpha/2)*S2/Math.sqrt(numSamplesInEstimate);
                            double z = F * S / Math.sqrt(numSamplesInEstimate);

                            System.out.println("current estimate fEstimatedRatio: " + fEstimatedRatio + " active est:" + R_nq + " fEstimatedRatio-active est:" + (fEstimatedRatio - R_nq) + " +/- " + z + " std.err.:" + S / Math.sqrt(numSamplesInEstimate) + " numSamplesInEstimate:" + numSamplesInEstimate);
                            //     System.out.println("current estimate fEstimatedRatio: " + fEstimatedRatio + " active est:" + (sumWeightedLossOverLabeled / sumImportanceWeightsOverLabeled) + " fEstimatedRatio+active est:" + (fEstimatedRatio - sumWeightedLossOverLabeled / sumImportanceWeightsOverLabeled) + " sumImportanceWeightsOverLabeled:"+sumImportanceWeightsOverLabeled  + " sumWeightedLossOverLabeled:"+sumWeightedLossOverLabeled);

                        }

                        if (existingLabel == null) {
                            activeEvalLabels.put(new ByteArrayWrapper(prediction.placeId1, prediction.placeId2), label);
                        }
                        activeEvalCounts.put(new ByteArrayWrapper(prediction.placeId1, prediction.placeId2), sampleCount);

                        writeCountLabels(manuallyLabeledDataActiveEvalFileName, activeEvalLabels, activeEvalCounts);

                    }
                }


            }

            //   System.out.println("treeset.size: " + predictions.size() + " " + numPool + " "+sumqOverPool);


            /*

                        } catch (IOException
                                e) {
                            e.printStackTrace();
                        }

            */
        }


        if (mode == 13) {

            HashSet<String> placeIds = new HashSet<String>();
            int stopAtResultSetCount = Integer.parseInt(freeCommandLineParam1);
            try {
                int resultSetCounter = 0;
                int processedPairCounter = 0;
                BufferedReader br = new BufferedReader(new FileReader(whereLogDataFileName));
                String strLine;
                //read tab delimited file line by line
                while ((strLine = br.readLine()) != null) {
                    resultSetCounter++;

                    if (resultSetCounter > stopAtResultSetCount)
                        break;

                    //System.out.println("\nresult set: " + strLine + "\n");
                    StringTokenizer stringTokenizer1 = new StringTokenizer(strLine, "\t");

                    String placeIdInResultSet[] = strLine.split("\t");

                    /*
                    for(int i=0;i<placeIdInResultSet.length;i++) {
                       System.out.println("\nresult set array: " + placeIdInResultSet[i] + "\n");
                    }
                    */

                    if (placeIdInResultSet.length > 1) {
                        for (int i = 0; i < placeIdInResultSet.length; i++) {
                            placeIds.add(placeIdInResultSet[i]);
                            // System.out.println("placeid: " + placeIdInResultSet[i]);

                        }
                    }

                }
                System.out.println("stats resultSetCounter: " + resultSetCounter + " unique places: " + placeIds.size() + " contains: " + placeIds.contains("276u39g9-ebeaa75f12fa4b82bbbb81cc45e209f2"));

                /*

                //create BufferedReader to read csv file
                BufferedReader rawDataBufferedReader = new BufferedReader(new FileReader(rawOPRDataFileName));

                String oprDataLine = "";
                StringTokenizer st = null;
                String placeId = "";

                PrintWriter out = new PrintWriter(new FileWriter(rawOPRDataFileName + "_filtered"));

                //read comma separated file line by line
                while ((oprDataLine = rawDataBufferedReader.readLine()) != null) {
                    while (oprDataLine.indexOf("\t\t") > -1) {
                        oprDataLine = oprDataLine.replaceAll("\t\t", "\tNULL\t");
                    }
                    st = new StringTokenizer(oprDataLine, "\t");
                    placeId = st.nextToken();
                    if (placeIds.contains(placeId)) {
                        //System.out.println(oprDataLine);
                        out.println(oprDataLine);

                    }
                }
                out.close();
                */
            }
            catch (Exception e) {
                System.out.println("###########Exception: " + e);
                e.printStackTrace();
                System.exit(-1);
            }
        }


        if (mode == 12)

        { // old code with array

            //  sampleFromExtractedWhereLogData(extractedWhereLogDataFileName, extractedWhereLogDataSubsetFileName);


/*

            // actively collect train data

            Collection places = placeMap.values();
            Place[] placesArray = (Place[]) places.toArray(new Place[0]);

            int count=0;            
            Random randomGenerator = new Random(123456);
            while(count<2) {

                TreeSet<PredictedPair> candidateList = new TreeSet<PredictedPair>();

                for(int i=0;i<1000;i++) {
                    int randomRefId = randomGenerator.nextInt(placesArray.length);
                    Place refPlace = placesArray[randomRefId];
                    if(!refPlace.merged) {
                        double search_radius = 1.0;
                        ArrayList<Place> searchResults;
                        searchResults = gh.nearBySearch(refPlace.latitude, refPlace.longitude, search_radius, 1);
                        if(!searchResults.isEmpty()) {

                            Place[] searchResultsArray = (Place[]) searchResults.toArray(new Place[0]);
                            for(int cand=0;cand<searchResultsArray.length;cand++) {
                                Place candPlace = searchResultsArray[cand];
                                if(!candPlace.merged && !refPlace.placeId.equals(candPlace.placeId)) {

                                    int label = 1;
                                    Integer labelObj = trainDataManualMap.get(new ByteArrayWrapper(refPlace.placeId,candPlace.placeId));                            
                                    if(labelObj == null) {
                                        count++;
                                        debugOutputln("count: " + count);
                                        TreeSet<Feature> featureList = new TreeSet<Feature>();
                                        extractFeatures(refPlace,candPlace,featureList);                                    
                                        candidateList.add(new PredictedPair(Math.abs(predict(featureList, model,1,0)),refPlace.placeId,candPlace.placeId));
                                        
                                        System.out.println("collect candidates count: " + count + " " + (new PredictedPair(Math.abs(predict(featureList, model,1,0)),refPlace.placeId,candPlace.placeId)));
                                                                    
                                        //System.out.println("collect candidates count: " + count + " " + (new PredictedPair(Math.abs(predict(featureList, model,1,0)),refPlace.placeId,candPlace.placeId)) + " " + predict(featureList, model,1,0) + " " + predict(featureList, model,2,0) + " " + featureList);
                                    }
                                    else {
                                        System.out.println("candidate already in train data: " + refPlace.placeId + " " + candPlace.placeId);
                                    }
                                }                                
                            }
                        }
                    }
                }

                Iterator<PredictedPair> it = candidateList.iterator();
                if(it.hasNext()) {
                    PredictedPair predictedPair = it.next();
                    System.out.println("best candidate: " + predictedPair); 
                    Place place1 = placeMap.get(predictedPair.placeId1);
                    Place place2 = placeMap.get(predictedPair.placeId2);

                    TreeSet<Feature> featureList = new TreeSet<Feature>();    
                    extractFeatures(place1,place2,featureList);      

                    collectLabel(manuallyLabeledDataFileName, 0, place1, place2, featureList,"random-active");

                }                    
 

            }
*/

        }


    }


    public static double compute_q(double pOf1, double R, double R2) {
        if (pOf1 > 0.5) {
            return Math.sqrt((1 - 2 * R) * (1 - pOf1) + R2);
        } else {
            return Math.sqrt((1 + 2 * R) * pOf1 + R2);
        }
    }


    public static void writeOPRData(int label, boolean validated, String p0, String p1) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(extractedManualAndAutomaticLabelsOPRFormatFilename, true));

            if (label > 0)
                out.print("1");
            else
                out.print("0");

            if (validated)
                out.print(";validated");
            else
                out.print(";not_validated");

            out.println(";" + p0 + ";" + p1);

            out.close();

        }
        catch (Exception e) {
            System.out.println("###########Exception: " + e);
            e.printStackTrace();
            System.exit(-1);
        }


    }

    public static int collectLabel(String fileName, int label, Place p1, Place p2, FeatureVector featureVector, String comment) {
        if (featureVector == null) {
            featureVector = featureProcessor.extractFeatures(p1, p2, label);
        }

        try {
            // System.out.print((char)27 + "[2J");
            // System.out.flush();
            if (model != null) {
                System.out.println("\n\n\nlabel: " + label + " threshold: " + probabilityThreshold + " prediction prob:" + featureVector.predict(model, 2, 0) + " " + p1.toString(p2));
                featureVector.predict(model, 2, 1);
            } else {
                System.out.println("\n\n\n" + p1.toString(p2));
            }
            System.out.println("enter p for positive (duplicate), enter n for negative (non-duplicate), enter i for ignore (don't know), and press enter:");
            byte[] buf = new byte[256];
            final int len = System.in.read(buf);
            String input = new String(buf, 0, len);

            if (fileName != null) {
                PrintWriter out = new PrintWriter(new FileWriter(fileName, true));

                if (input.indexOf('p') > -1) {
                    out.println("1|" + comment + "|" + DQUtils.sortConcatStringPairsBar(p1.placeId.toString(), p2.placeId.toString()));
                    System.out.println("positive");
                } else if (input.indexOf('n') > -1) {
                    out.println("-1|" + comment + "|" + DQUtils.sortConcatStringPairsBar(p1.placeId.toString(), p2.placeId.toString()));
                    System.out.println("negative");
                } else if (input.indexOf('i') > -1) {
                    out.println("0|" + comment + "|" + DQUtils.sortConcatStringPairsBar(p1.placeId.toString(), p2.placeId.toString()));
                    System.out.println("ignore");
                } else if (input.indexOf('u') > -1 && probabilityThreshold < 1.0) {
                    probabilityThreshold += 0.02;
                } else if (input.indexOf('d') > -1 && probabilityThreshold > 0.0) {
                    probabilityThreshold -= 0.02;
                }
                out.close();
            } else {
                if (input.indexOf('p') > -1) {
                    System.out.println("positive");
                    return 1;
                } else if (input.indexOf('n') > -1) {
                    System.out.println("negative");
                    return -1;
                } else if (input.indexOf('i') > -1) {
                    System.out.println("ignore");
                } else if (input.indexOf('u') > -1 && probabilityThreshold < 1.0) {
                    probabilityThreshold += 0.02;
                } else if (input.indexOf('d') > -1 && probabilityThreshold > 0.0) {
                    probabilityThreshold -= 0.02;
                }
            }

        }
        catch (Exception e) {
            System.out.println("###########Exception: " + e);
            e.printStackTrace();
            System.exit(-1);
        }
        return 0;
    }


    public static void storeAutoLabel(PrintWriter out, int label, Place p1, Place p2, String comment) {
        if (out != null) {
            if (label > 0)
                out.println("1|" + comment + "|" + DQUtils.sortConcatStringPairsBar(p1.placeId.toString(), p2.placeId.toString()));
            else
                out.println("-1|" + comment + "|" + DQUtils.sortConcatStringPairsBar(p1.placeId.toString(), p2.placeId.toString()));

            debugOutputln("storeAutoLabel: " + label + " placeid1: " + p1.placeId + " placeid2: " + p2.placeId);
        }
    }

    /*
    public static void sampleFromExtractedWhereLogData(String dataFileName, String dataSubsetFileName) {

        try {

            BufferedReader br = new BufferedReader(new FileReader(dataFileName));

            String strLine = "";
            StringTokenizer st = null;
            int placeCount = -1;
            String curPlaceId = "", newPlaceId = "";
            Place curPlace = null;
            String s;

            //read comma separated file line by line
            while ((strLine = br.readLine()) != null) {
                //    System.out.println("debug: "+ strLine);


                st = new StringTokenizer(strLine, "|");

                int label;
                String labelString = st.nextToken();
                if (labelString.equals("0"))
                    label = 0;
                else {
                    label = Integer.parseInt(labelString);
                    if (label > 0)
                        label = 1;
                    else
                        label = -1;
                }

                String comment = st.nextToken();
                String placeId1 = st.nextToken();
                String placeId2 = st.nextToken();

                //dataMap.put(new ByteArrayWrapper(placeId1, placeId2), new Integer(label));

                //debugOutputln("debug load train data: " + (new ByteArrayWrapper(placeId1, placeId2)) + " label: " + label);

            }

            br.close();
        }
        catch (Exception e) {
            System.out.println("###########Exception: " + e);
            e.printStackTrace();
            System.exit(-1);
        }
    }
    */


    public static void readLabels(String dataFileName, HashMap<ByteArrayWrapper, Integer> dataMap) {

        try {

            BufferedReader br = new BufferedReader(new FileReader(dataFileName));

            String strLine = "";
            StringTokenizer st = null;
            int placeCount = -1;
            String curPlaceId = "", newPlaceId = "";
            Place curPlace = null;
            String s;

            //read comma separated file line by line
            while ((strLine = br.readLine()) != null) {
                //    System.out.println("debug: "+ strLine);


                st = new StringTokenizer(strLine, "|");

                int label;
                String labelString = st.nextToken();
                if (labelString.equals("0"))
                    label = 0;
                else {
                    label = Integer.parseInt(labelString);
                    if (label > 0)
                        label = 1;
                    else
                        label = -1;
                }

                String comment = st.nextToken();
                String placeId1 = st.nextToken();
                String placeId2 = st.nextToken();

                dataMap.put(new ByteArrayWrapper(placeId1, placeId2), new Integer(label));

                debugOutputln("debug load train data: " + (new ByteArrayWrapper(placeId1, placeId2)) + " label: " + label);

            }

            br.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Manual labels file not found: " + dataFileName + ". Continuing without labels.");
        }
        catch (Exception e) {
            System.out.println("###########Exception: " + e);
            e.printStackTrace();
            System.exit(-1);
        }
    }


    public static void writeCountLabels(String dataFileName, HashMap<ByteArrayWrapper, Integer> labelMap, HashMap<ByteArrayWrapper, Integer> countMap) {

        try {
            PrintWriter out = new PrintWriter(new FileWriter(dataFileName));

            Collection<ByteArrayWrapper> colKey = labelMap.keySet();
            Collection<Integer> colValues = labelMap.values();

            Iterator<ByteArrayWrapper> itKey = colKey.iterator();
            Iterator<Integer> itValues = colValues.iterator();
            while (itKey.hasNext() && itValues.hasNext()) {
                ByteArrayWrapper placeIdPair = itKey.next();
                int label = itValues.next();
                Integer count = countMap.get(placeIdPair);
                if (count != null) {

                    ByteArrayWrapper placeId0 = new ByteArrayWrapper();
                    ByteArrayWrapper placeId1 = new ByteArrayWrapper();

                    placeIdPair.splitUpPlaceIdPair(placeId0, placeId1);

                    //System.out.println("debug writeCountLabels:" + label + "|" + count + "|" + placeId0 + "|" + placeId1);
                    out.println(label + "|" + count + "|" + placeId0 + "|" + placeId1);

                } else {
                    System.out.println("count not found:" + label + "|" + placeIdPair);
                }
            }

            out.close();

        }
        catch (Exception e) {
            System.out.println("###########Exception: " + e);
            e.printStackTrace();
            System.exit(-1);
        }


    }


    public static void readCountLabels(String dataFileName, HashMap<ByteArrayWrapper, Integer> labelMap, HashMap<ByteArrayWrapper, Integer> countMap) {

        try {

            BufferedReader br = new BufferedReader(new FileReader(dataFileName));

            String strLine = "";
            StringTokenizer st = null;
            int placeCount = -1;
            String curPlaceId = "", newPlaceId = "";
            Place curPlace = null;
            String s;

            //read comma separated file line by line
            while ((strLine = br.readLine()) != null) {
                //    System.out.println("debug: "+ strLine);


                st = new StringTokenizer(strLine, "|");

                int label;
                String labelString = st.nextToken();
                if (labelString.equals("0"))
                    label = 0;
                else {
                    label = Integer.parseInt(labelString);
                    if (label > 0)
                        label = 1;
                    else
                        label = -1;
                }

                String countString = st.nextToken();
                int count = Integer.parseInt(countString);
                String placeId1 = st.nextToken();
                String placeId2 = st.nextToken();

                labelMap.put(new ByteArrayWrapper(placeId1, placeId2), new Integer(label));
                countMap.put(new ByteArrayWrapper(placeId1, placeId2), new Integer(count));

                debugOutputln("debug load train data: " + (new ByteArrayWrapper(placeId1, placeId2)) + " label: " + label + " count: " + count);

            }

            br.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("no active eval label file found!");
        }
        catch (Exception e) {
            System.out.println("###########Exception: " + e);
            e.printStackTrace();
            System.exit(-1);
        }
    }


    public static void readRawOPRDataMultiLanguage(String rawDataFileName, boolean fillPlaceMap, boolean collectDictionaries) {

        if (fillPlaceMap) {
            placeMap = new HashMap<ByteArrayWrapper, Place>(1000);
        }

        try {

            //create BufferedReader to read csv file
            BufferedReader rawDataBufferedReader = new BufferedReader(new FileReader(rawDataFileName));

            String strLine = "";
            StringTokenizer st = null;
            int placeCount = 0;
            String curPlaceId = "", newPlaceId = "";
            Place curPlace = null;
            String s;


            //read comma separated file line by line
            while ((strLine = rawDataBufferedReader.readLine()) != null) {
                //    System.out.println("debug: "+ strLine);


                while (strLine.indexOf("\t\t") > -1) {
                    strLine = strLine.replaceAll("\t\t", "\tNULL\t");
                }


                //tokenize comma separated line using "\t"
                st = new StringTokenizer(strLine, "\t");

                //System.out.println("place count: " + placeCount + " Count Tokens : "+ st.countTokens());


                newPlaceId = st.nextToken();
                if (newPlaceId.length() > 40) {
                    if (!curPlaceId.equals(newPlaceId)) {

                        if (curPlace != null && curPlace.nameSet != null) {
                            placeCount++;
                            if (fillPlaceMap) {
                                //if(curPlace.extensionSet != null) {
                                //    System.out.println("Extension " + curPlace.extensionSet);
                                //}
                                placeMap.put(curPlace.placeId, curPlace);
                            }
                            if (collectDictionaries) {
                                featureProcessor.fillDictionaries(curPlace);
                            }
                        }

                        //
                        //if (curPlace != null && curPlace.hasMultipleAddresses()) {
                        //     if (curPlace != null && curPlace.hasMultipleNames()) {
                        //     System.out.println("Place " + curPlace);
                        // }

                        //  System.out.println("Place " + curPlace);


                        //                    System.out.println("Place " + curPlace);
                        curPlaceId = newPlaceId;
                        curPlace = new Place(curPlaceId);
                        //System.out.println(curPlaceId);
                        curPlace.setMergedWith(DQUtils.getTokenAndNormalize(st, curPlace, 0));
                        String languageLocation = DQUtils.getTokenAndNormalize(st, curPlace, 0);
                        curPlace.language_laf = DQUtils.getTokenAndNormalize(st, curPlace, 0);
                        curPlace.coreCategories = new HashSet<String>(4);
                        String coreCategory = st.nextToken();
                        if (!coreCategory.equals("NULL")) {
                            curPlace.coreCategories.add(coreCategory);
                        }
                        curPlace.categories = new HashSet<String>(4);
                        String category = st.nextToken();
                        if (!category.equals("NULL")) {
                            curPlace.categories.add(category);
                        }
                        //System.out.println("Place " + curPlace);
                        curPlace.latitude = Double.parseDouble(st.nextToken());
                        curPlace.longitude = Double.parseDouble(st.nextToken());

                        curPlace.name = DQUtils.getTokenAndNormalize(st, curPlace, 0);
                        curPlace.addName(curPlace.name);

                        curPlace.normalizedName = DQUtils.normalizeName(curPlace.name);
                        curPlace.addNormalizedName(languageLocation, curPlace.normalizedName);

                        curPlace.normalizedNameOPR = DQUtils.getTokenAndNormalize(st, curPlace, 0);
                        //curPlace.geoHash = getTokenAndNormalize(st, curPlace,false);
                        DQUtils.getTokenAndNormalize(st, curPlace, 0); // geoHash dummy
                        curPlace.phone = DQUtils.normalizePhone(DQUtils.getTokenAndNormalize(st, curPlace, 1));
                        //                    if(curPlace.phone != null)
                        //                        System.out.println(curPlace.phone);
                        curPlace.fax = DQUtils.getTokenAndNormalize(st, curPlace, 1);
                        curPlace.email = DQUtils.getTokenAndNormalize(st, curPlace, 1);
                        curPlace.website = DQUtils.getTokenAndNormalize(st, curPlace, 1);
                        if (curPlace.website != null && curPlace.website.indexOf("http://") > -1) {
                            curPlace.website = curPlace.website.substring(curPlace.website.indexOf("http://") + 7);
                        }
                        //                    if(curPlace.website != null)
                        //                        System.out.println(curPlace.website);
                        curPlace.country = DQUtils.getTokenAndNormalize(st, curPlace, 1);
                        curPlace.city = DQUtils.getTokenAndNormalize(st, curPlace, 1);
                        curPlace.addCity(curPlace.city);
                        curPlace.district = DQUtils.getTokenAndNormalize(st, curPlace, 1);
                        curPlace.addDistrict(curPlace.district);
                        curPlace.extension = DQUtils.getTokenAndNormalize(st, curPlace, 1);
                        curPlace.addExtension(curPlace.extension);
                        curPlace.region = DQUtils.getTokenAndNormalize(st, curPlace, 1);
                        curPlace.street = DQUtils.getTokenAndNormalize(st, curPlace, 1);
                        String streetNormalized = DQUtils.normalizeName(curPlace.street);
                        curPlace.addStreet(streetNormalized);
                        curPlace.house = DQUtils.getTokenAndNormalize(st, curPlace, 1);
                        String houseNormalized = DQUtils.normalizeName(curPlace.house);
                        curPlace.addHouse(houseNormalized);

                        if (streetNormalized != null && houseNormalized != null)
                            curPlace.addStreetCatHouse(streetNormalized + " " + houseNormalized);
                        else if (streetNormalized != null)
                            curPlace.addStreetCatHouse(streetNormalized);
                        else if (houseNormalized != null)
                            curPlace.addStreetCatHouse(houseNormalized);

                        curPlace.merged = st.nextToken().equals("MERGED");
                        curPlace.provider = DQUtils.getTokenAndNormalize(st, curPlace, 0);

                        // System.out.println("Place " + curPlace);


                    } else {
                        st.nextToken(); // mergedWith
                        String languageLocation = DQUtils.getTokenAndNormalize(st, curPlace, 0);
                        st.nextToken(); // language_laf
                        String coreCategory = st.nextToken();
                        if (!coreCategory.equals("NULL")) {
                            curPlace.coreCategories.add(coreCategory);
                        }
                        String category = st.nextToken();
                        if (!category.equals("NULL")) {
                            curPlace.categories.add(category);
                        }
                        st.nextToken(); // latitude
                        st.nextToken(); // longitude

                        String name = DQUtils.getTokenAndNormalize(st, curPlace, 0);
                        curPlace.addName(name);
                        curPlace.addNormalizedName(languageLocation, DQUtils.normalizeName(name));
                        st.nextToken(); // OPR normalized name
                        st.nextToken(); // geoHash

                        st.nextToken(); // phone
                        st.nextToken(); // fax
                        st.nextToken(); // email
                        st.nextToken(); // website
                        st.nextToken(); // country

                        curPlace.addCity(DQUtils.getTokenAndNormalize(st, curPlace, 1));
                        curPlace.addDistrict(DQUtils.getTokenAndNormalize(st, curPlace, 1));
                        curPlace.addExtension(DQUtils.getTokenAndNormalize(st, curPlace, 1));
                        curPlace.addRegion(DQUtils.getTokenAndNormalize(st, curPlace, 1));
                        String streetNormalized = DQUtils.getTokenAndNormalize(st, curPlace, 2);
                        curPlace.addStreet(streetNormalized);
                        String houseNormalized = DQUtils.getTokenAndNormalize(st, curPlace, 2);
                        curPlace.addHouse(houseNormalized);

                        if (streetNormalized != null && houseNormalized != null)
                            curPlace.addStreetCatHouse(streetNormalized + " " + houseNormalized);
                        else if (streetNormalized != null)
                            curPlace.addStreetCatHouse(streetNormalized);
                        else if (houseNormalized != null)
                            curPlace.addStreetCatHouse(houseNormalized);


// if(curPlace.coreCategories.size() > 1)
//System.out.println("Placec" + curPlace.coreCategories);
//System.out.println("Place " + curPlace.categories);

                    }
                }
            }


            rawDataBufferedReader.close();

            System.out.println("read " + placeCount + " places.");

        }
        catch (Exception e) {
            System.out.println("###########Exception: " + e);
            e.printStackTrace();
            System.exit(-1);
        }

        // resize arrays to actual size
        //gh.compress();

    }





    public static void debugOutput(String s) {
        if (debugOutput) {
            System.out.print(s);
        }
    }

    public static void debugOutputln(String s) {
        if (debugOutput) {
            System.out.println(s);
        }
    }

    public static void debugOutputln(String s, Place p1, Place p2) {
        if (debugOutput) {
            System.out.println(s + p1.toString(p2));
        }
    }

    public static void debugOutputln(String s, Place p) {
        if (debugOutput) {
            System.out.println(s + p.toString());
        }
    }


    /**
     * Loads the model from the file with ISO-8859-1 charset.
     * It uses Locale.ENGLISH for number formatting.
     */
    public static Model loadModel(File modelFile) throws IOException {
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(modelFile), FILE_CHARSET));
        return loadModel(inputReader);
    }


    /**
     * Loads the model from inputReader.
     * It uses Locale.ENGLISH for number formatting.
     * <p/>
     * <p><b>Note: The inputReader is closed after reading or in case of an exception.</b></p>
     */
    public static Model loadModel(Reader inputReader) throws IOException {
        Model model = new Model();

        model.label = null;

        Pattern whitespace = Pattern.compile("\\s+");

        BufferedReader reader = null;
        if (inputReader instanceof BufferedReader) {
            reader = (BufferedReader) inputReader;
        } else {
            reader = new BufferedReader(inputReader);
        }

        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] split = whitespace.split(line);
                if (split[0].equals("solver_type")) {
                    SolverType solver = SolverType.valueOf(split[1]);
                    if (solver == null) {
                        throw new RuntimeException("unknown solver type");
                    }
                    model.solverType = solver;
                } else if (split[0].equals("nr_class")) {
                    model.nr_class = atoi(split[1]);
                    Integer.parseInt(split[1]);
                } else if (split[0].equals("nr_feature")) {
                    model.nr_feature = atoi(split[1]);
                } else if (split[0].equals("bias")) {
                    model.bias = atof(split[1]);
                } else if (split[0].equals("w")) {
                    break;
                } else if (split[0].equals("label")) {
                    model.label = new int[model.nr_class];
                    for (int i = 0; i < model.nr_class; i++) {
                        model.label[i] = atoi(split[i + 1]);
                    }
                } else {
                    throw new RuntimeException("unknown text in model file: [" + line + "]");
                }
            }

            int w_size = model.nr_feature;
            if (model.bias >= 0) w_size++;

            int nr_w = model.nr_class;
            if (model.nr_class == 2 && model.solverType != SolverType.MCSVM_CS) nr_w = 1;

            model.w = new double[w_size * nr_w];
            int[] buffer = new int[128];

            for (int i = 0; i < w_size; i++) {
                for (int j = 0; j < nr_w; j++) {
                    int b = 0;
                    while (true) {
                        int ch = reader.read();
                        if (ch == -1) {
                            throw new EOFException("unexpected EOF");
                        }
                        if (ch == ' ') {
                            // hack, works only for binary -1, +1 labels
                            model.w[i * nr_w + j] = atof(new String(buffer, 0, b)) * model.label[0];
                            //  model.w[i * nr_w + j] = atof(new String(buffer, 0, b)) * model.label[0];
                            break;
                        } else {
                            buffer[b++] = ch;
                        }
                    }
                }
            }
        }
        finally {
            closeQuietly(reader);
        }

        return model;
    }

    /**
     * @param s the string to parse for the double value
     * @throws IllegalArgumentException if s is empty or represents NaN or Infinity
     * @throws NumberFormatException    see {@link Double#parseDouble(String)}
     */
    static double atof(String s) {
        if (s == null || s.length() < 1) throw new IllegalArgumentException("Can't convert empty string to integer");
        double d = Double.parseDouble(s);
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new IllegalArgumentException("NaN or Infinity in input: " + s);
        }
        return (d);
    }

    /**
     * @param s the string to parse for the integer value
     * @throws IllegalArgumentException if s is empty
     * @throws NumberFormatException    see {@link Integer#parseInt(String)}
     */
    static int atoi(String s) throws NumberFormatException {
        if (s == null || s.length() < 1) throw new IllegalArgumentException("Can't convert empty string to integer");
        // Integer.parseInt doesn't accept '+' prefixed strings
        if (s.charAt(0) == '+') s = s.substring(1);
        return Integer.parseInt(s);
    }

    static void closeQuietly(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
        }
    }


}
 