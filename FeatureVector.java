
import java.io.PrintWriter;
import java.util.*;

public class FeatureVector {

    TreeSet<Feature> featureList;

    static HashMap<Integer, TreeSet> reverseFeatureDictionary = new HashMap<Integer, TreeSet>(100);

    static int numBuckets = 50000;
    static int numBucketsCorrected = calculateNumBuckets(numBuckets);
    static int numFeaturesWithBias = numBucketsCorrected + 2;

    public FeatureVector() {

        featureList = new TreeSet<Feature>();


    }


    public static int getNumBucketsCorrected() {
        return numBucketsCorrected;
    }

    public void updateFeatureDictionary(int bucket, String s) {
        TreeSet<String> featSet = reverseFeatureDictionary.get(new Integer(bucket));
        if (featSet == null) {
            featSet = new TreeSet<String>();
            featSet.add(s);
            reverseFeatureDictionary.put(bucket, featSet);
        } else if (!featSet.contains(s)) {
            featSet.add(s);
            //System.out.println("collition "+featSet + " " + bucket);
        }
    }


    private static int calculateNumBuckets(int x) {
        if (x >= 1 << 30) {
            return 1 << 30;
        }
        if (x == 0) {
            return 16;
        }
        x = x - 1;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return x + 1;
    }


    public void storeFeature(String key, double value) {
        if (Math.abs(value) > 0) {
            if (featureList != null) {
                // if(key.equals("distance_>2000") || key.equals("distance_<2000")) {

                //debugOutputln("store feature key: " + key + " value: " + value + " feature bucket: " + bucketHash(key) + " corrected value: " + binaryHash(key) * value);
                int bucket = bucketHash(key);
                // if(bucket==64924) {
                //      System.out.println("dupl " + bucket);
                // }
                Iterator<Feature> it = featureList.iterator();
                boolean exists = false;
                while (it.hasNext()) {
                    Feature f = it.next();
                    if (f.index == bucket) {
                        f.value = f.value + binaryHash(key) * value;
                        exists = true;
                        updateFeatureDictionary(bucket, key);
                        //System.out.println("dupl " + f.index);
                        break;
                    }
                }
                if (!exists) {
                    featureList.add(new Feature(bucket, binaryHash(key) * value));
                    updateFeatureDictionary(bucket, key);
                }

                //   }
                //writeSVMlightFeature(bucketHash(key), binaryHash(key)*value, out);
            }
        }
    }


    public int size() {
        return featureList.size();
    }


    public static double binaryHash(String s) {
        if ((s.hashCode() & 0x1) > 0)
            return +1;
        else
            return -1;
    }


    public int bucketHash(String s) {
        if (s == null) {
            System.out.println("feature string null!");
            System.exit(-1);
            return 0;
        } else {
            return (s.hashCode() & (numBucketsCorrected - 1)) + 1;
        }
    }


    public void writeSVMlightWriteFeatures(PrintWriter out, int label) {
        if (out != null) {
            if (label > 0) {
                out.print("1");
            } else {
                out.print("-1");
            }
            for (Feature aFeatureList : featureList) {
                Feature curFeature = (aFeatureList);
                out.print(" " + curFeature.index + ":" + ((float) curFeature.value));
            }
            out.println();
            //            out.format("%g",(float) value);
        }
    }

    public double predict(Model model, int mode, int debug) {
        int n;
        if (model.bias >= 0)
            n = model.nr_feature + 1;
        else
            n = model.nr_feature;

        double[] w = model.w;
        double dec_value = 0;

        TreeMap<Double, String> debugList = null;
        if (debug > 0)
            debugList = new TreeMap<Double, String>();

        Iterator<Feature> it = featureList.iterator();
        while (it.hasNext()) {
            Feature f = it.next();
            if (f.index > model.nr_feature) {
                System.out.println("########### index to large: " + f.index + " " + n);
                //  System.exit(-1);
            } else {
                dec_value += w[f.index - 1] * f.value;

                if (debug > 0) {
                    TreeSet<String> featSet = reverseFeatureDictionary.get(new Integer(f.index));
                    debugList.put(new Double(w[f.index - 1] * f.value),
                            " index:" + f.index + " w:" + w[f.index - 1] + " f.value:" + f.value + " feat string:" + featSet);
                }
                //System.out.println("mult w:" + w[f.index - 1] + " f.index:" + f.index + " f.value:" + f.value);
            }
        }
        if (model.bias > 0) {
            dec_value += w[model.nr_feature] * model.bias;
            if (debug > 0) {
                debugList.put(new Double(w[model.nr_feature] * model.bias),
                        " index:" + model.nr_feature + " w:" + w[model.nr_feature] + " f.value:" + model.bias + " feat string: bias");
            }

            //System.out.println("mult bias w:" + w[model.nr_feature] + " f.index:" + model.nr_feature + " model.bias:" + model.bias);
        }

        if (debug > 0) {
            System.out.println("final decision function value:" + dec_value);
            Collection<Double> colKey = debugList.keySet();
            Collection<String> colValues = debugList.values();

            Iterator<Double> itKey = colKey.iterator();
            Iterator<String> itValues = colValues.iterator();
            while (itKey.hasNext() && itValues.hasNext()) {
                double dfunc = itKey.next().doubleValue();
                String details = itValues.next();
                System.out.println("predict debug w*feat:" + dfunc + details);
            }
        }


        if (mode == 0)
            //return (dec_value > 0) ? model.label[0] : model.label[1];
            return (dec_value > 0) ? 1 : -1;
        else if (mode == 1)
            return dec_value;
        else {
            if (model.solverType == SolverType.L2R_LR) {
                return 1 / (1 + Math.exp(-dec_value));
            } else {
                System.out.println("########### probability outputs only for logistic regression models!");
                System.exit(-1);
                return 0;
            }
        }
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        Iterator<Feature> it = featureList.iterator();
        while (it.hasNext()) {
            Feature f = it.next();
            TreeSet<String> featSet = reverseFeatureDictionary.get(new Integer(f.index));
            result.append(" index:" + f.index + " f.value:" + f.value + " feat string:" + featSet + "\n");
        }
        return result.toString();
    }


    public String toString(Model model) {
        StringBuilder result = new StringBuilder();

        int n;
        if (model.bias >= 0)
            n = model.nr_feature + 1;
        else
            n = model.nr_feature;

        TreeMap<Double, String> debugList = null;

        debugList = new TreeMap<Double, String>();

        Iterator<Feature> it = featureList.iterator();
        while (it.hasNext()) {
            Feature f = it.next();
            if (f.index > model.nr_feature) {
                System.out.println("########### index to large: " + f.index + " " + n);
                System.exit(-1);
            } else {
                TreeSet<String> featSet = reverseFeatureDictionary.get(new Integer(f.index));
                result.append(" index:" + f.index + " f.value:" + f.value + " feat string:" + featSet + "\n");

                //System.out.println("mult w:" + w[f.index - 1] + " f.index:" + f.index + " f.value:" + f.value);
            }
        }
        if (model.bias > 0) {
            result.append(" index:" + model.nr_feature + " f.value:" + model.bias + " feat string: bias" + "\n");
        }
        return result.toString();
    }


}

