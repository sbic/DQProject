
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;


public class FeatureProcessor {


    boolean useHungarianDetailsFeatures = false;
    String dictionariesFileName;

    static HashMap<String, HashMap<String, HashMap<String, Integer>>> languageCategoryDictionaries = null;
    boolean debugOutput;

    HashMap<String, HashSet<String>> equivalentCoreCategoriesMap;


    public FeatureProcessor(boolean debugOutput, String dictionariesFileName) {
        this.debugOutput = debugOutput;
        this.dictionariesFileName = dictionariesFileName;

        fillEquivalentCoreCategoriesMap();
        verifyEquivalentCoreCategoriesMap();

        System.out.println("number of feature vector buckets: " + FeatureVector.getNumBucketsCorrected());
    }


    public void storeMismatchCategoryFeatures(HashSet<String> c1, HashSet<String> c2, String token, FeatureVector featureVector, String storeFeature) {
        Iterator<String> it1;
        it1 = c1.iterator();
        while (it1.hasNext()) {
            String cat1 = it1.next();
            if (c2.contains(cat1)) {
                featureVector.storeFeature(storeFeature + "hungarian_mismatch_category_" + cat1 + "_" + token, 1.0);
            }
        }
    }

    public void storeFeatureDiscretizedProbability(String key, double value, FeatureVector featureVector) {
        if (value >= 1.0) {
            featureVector.storeFeature(key + "_>1.0", 1.0);
            featureVector.storeFeature(key + "_>0.8", 1.0);
            featureVector.storeFeature(key + "_>0.6", 1.0);
            featureVector.storeFeature(key + "_>0.4", 1.0);
            featureVector.storeFeature(key + "_>0.2", 1.0);
        } else if (value >= 0.8) {
            featureVector.storeFeature(key + "_>0.8", 1.0);
            featureVector.storeFeature(key + "_>0.6", 1.0);
            featureVector.storeFeature(key + "_>0.4", 1.0);
            featureVector.storeFeature(key + "_>0.2", 1.0);
        } else if (value >= 0.6) {
            featureVector.storeFeature(key + "_>0.6", 1.0);
            featureVector.storeFeature(key + "_>0.4", 1.0);
            featureVector.storeFeature(key + "_>0.2", 1.0);
        } else if (value >= 0.4) {
            featureVector.storeFeature(key + "_>0.4", 1.0);
            featureVector.storeFeature(key + "_>0.2", 1.0);
        } else if (value >= 0.2) {
            featureVector.storeFeature(key + "_>0.2", 1.0);
        }
    }


    public FeatureVector extractFeatures(Place p0, Place p1, int label) {
        FeatureVector featureVector = new FeatureVector();

        //System.out.println(" extract features muliple lang: " + useMultipleLanguages);

        extractFeaturesMultipleLanguages(p0, p1, featureVector, label);
        return featureVector;
    }


    public void extractFeaturesMultipleLanguages(Place p0, Place p1, FeatureVector featureVector, int label) {

        try {
            if (p0.normalizedName.indexOf("tate modern") >= 0) {
                debugOutputln("--------------------- extract features\n", p0, p1);
            }
            debugOutputln("--------------------- extract features\n", p0, p1);
        }
        catch (Exception nfe) {

            System.out.println("-----------------Exception name: " + p0.nameSet);
            System.out.println("-----------------Exception normname: " + p0.normalizedNameSet);
            if (p0.name.length() > 0)
                System.out.println("-----------------Exception normname single: " + p0.name + " " + Integer.toHexString(p0.name.codePointAt(0)));
            System.out.println("-----------------Exception name single: " + p0.normalizedName);
        }

        double distance = GeoHash.distance(p0.latitude, p1.latitude, p0.longitude, p1.longitude);
        // todo: distance classes
//        if(distance > 0)
//            featureVector.storeFeature("raw_distance",distance);


        if (distance < 0.05) {
            featureVector.storeFeature("distance_<50", 1.0);
        } else if (distance >= 0.05 && distance < 0.1) {
            featureVector.storeFeature("distance_50-100", 1.0);
        } else if (distance >= 0.1 && distance < 0.3) {
            featureVector.storeFeature("distance_100-300", 1.0);
        } else if (distance >= 0.3 && distance < 0.6) {
            featureVector.storeFeature("distance_300-600", 1.0);
        } else if (distance >= 0.6 && distance < 1.0) {
            featureVector.storeFeature("distance_600-1000", 1.0);
        } else if (distance >= 1.0 && distance <= 2.0) {
            featureVector.storeFeature("distance_1000-2000", 1.0);
        } else if (distance >= 2.0 && distance < 4.0) {
            featureVector.storeFeature("distance_2000-4000", 1.0);
            //     System.out.println("distance debug: " + distance + " " + label);
        } else if (distance >= 4.0 && distance < 8.0) {
            featureVector.storeFeature("distance_4000-8000", 1.0);
        } else {
            featureVector.storeFeature("distance_>8000", 1.0);
        }


        /*
      if (label>0) {
          featureVector.storeFeature("distance_<2000", 1.0);
      } else {
          featureVector.storeFeature("distance_>2000", 1.0);
         // System.out.println("distance debug: " + distance + " " + label);
      }
        */

        /*
              if (distance <= 2.0) {
            featureVector.storeFeature("distance_<2000", 0.9);
        } else {
            featureVector.storeFeature("distance_>2000", 1.0);
            System.out.println("distance debug: " + distance + " " + label);
        }
*/


        /*
            if (distance < 0.05) {
            featureVector.storeFeature("distance_<50", 1.0);
            featureVector.storeFeature("distance_<100", 1.0);
            featureVector.storeFeature("distance_<200", 1.0);
            featureVector.storeFeature("distance_<300", 1.0);
            featureVector.storeFeature("distance_<400", 1.0);
            featureVector.storeFeature("distance_<600", 1.0);
            featureVector.storeFeature("distance_<800", 1.0);
            featureVector.storeFeature("distance_<1000", 1.0);
            featureVector.storeFeature("distance_<2000", 1.0);
        } else if (distance < 0.1) {
            featureVector.storeFeature("distance_<100", 1.0);
            featureVector.storeFeature("distance_<200", 1.0);
            featureVector.storeFeature("distance_<300", 1.0);
            featureVector.storeFeature("distance_<400", 1.0);
            featureVector.storeFeature("distance_<600", 1.0);
            featureVector.storeFeature("distance_<800", 1.0);
            featureVector.storeFeature("distance_<1000", 1.0);
            featureVector.storeFeature("distance_<2000", 1.0);
        } else if (distance < 0.2) {
            featureVector.storeFeature("distance_<200", 1.0);
            featureVector.storeFeature("distance_<300", 1.0);
            featureVector.storeFeature("distance_<400", 1.0);
            featureVector.storeFeature("distance_<600", 1.0);
            featureVector.storeFeature("distance_<800", 1.0);
            featureVector.storeFeature("distance_<1000", 1.0);
            featureVector.storeFeature("distance_<2000", 1.0);
        } else if (distance < 0.3) {
            featureVector.storeFeature("distance_<300", 1.0);
            featureVector.storeFeature("distance_<400", 1.0);
            featureVector.storeFeature("distance_<600", 1.0);
            featureVector.storeFeature("distance_<800", 1.0);
            featureVector.storeFeature("distance_<1000", 1.0);
            featureVector.storeFeature("distance_<2000", 1.0);
        } else if (distance < 0.4) {
            featureVector.storeFeature("distance_<400", 1.0);
            featureVector.storeFeature("distance_<600", 1.0);
            featureVector.storeFeature("distance_<800", 1.0);
            featureVector.storeFeature("distance_<1000", 1.0);
            featureVector.storeFeature("distance_<2000", 1.0);
        } else if (distance < 0.6) {
            featureVector.storeFeature("distance_<600", 1.0);
            featureVector.storeFeature("distance_<800", 1.0);
            featureVector.storeFeature("distance_<1000", 1.0);
            featureVector.storeFeature("distance_<2000", 1.0);
        } else if (distance < 0.8) {
            featureVector.storeFeature("distance_<800", 1.0);
            featureVector.storeFeature("distance_<1000", 1.0);
            featureVector.storeFeature("distance_<2000", 1.0);
        } else if (distance < 1.0) {
            featureVector.storeFeature("distance_<1000", 1.0);
            featureVector.storeFeature("distance_<2000", 1.0);
       } else if (distance < 2.0) {
            featureVector.storeFeature("distance_<2000", 1.0);
        }
        */


        //       featureVector.storeFeature("exp_distance_1", Math.exp(-distance));
/*
        featureVector.storeFeature("exp_distance_0.1",Math.exp(-distance*0.1));
        featureVector.storeFeature("exp_distance_5",Math.exp(-distance*5));
        featureVector.storeFeature("exp_distance_10",Math.exp(-distance*10));
        featureVector.storeFeature("exp_distance_100",Math.exp(-distance*100));

        featureVector.storeFeature("provider_"+DQUtils.sortConcatStringPairs(p0.provider,p1.provider),1.0);
*/

        if (p0.provider.equals(p1.provider))
            featureVector.storeFeature("provider_match", 1.0);
        else
            featureVector.storeFeature("provider_mismatch", 1.0);

        // todo: categories normalization?

        Iterator<String> it0;
        Iterator<String> it1;
        boolean coreCategoryMatch = false;

        /*
        boolean coreCategoryAccomodation = p0.coreCategories.contains("accommodation") || p1.coreCategories.contains("accommodation");
        boolean coreCategoryEatAndDrink = p0.coreCategories.contains("eat-drink") || p1.coreCategories.contains("eat-drink");
        boolean coreCategoryLandmarkSight = p0.coreCategories.contains("landmark-sight") || p1.coreCategories.contains("landmark-sight");
        boolean coreCategoryTransportationFacility = p0.coreCategories.contains("transportation-facility") || p1.coreCategories.contains("transportation-facility");
        boolean coreCategoryAreaComplex = p0.coreCategories.contains("area-complex") || p1.coreCategories.contains("area-complex");
        */


        //  [eat-drink]


        int numberOfMatchingCoreCats = 0;
        it0 = p0.coreCategories.iterator();
        while (it0.hasNext()) {
            String cat1 = it0.next();
            if (p1.coreCategories.contains(cat1)) {
                numberOfMatchingCoreCats++;
            }
        }

        /*
         it0 = p0.coreCategories.iterator();
        while (it0.hasNext()) {
            String cat1 = it0.next();
            if (p1.coreCategories.contains(cat1)) {
                if (distance < 0.05) {
                    featureVector.storeFeature("distance_<50_"+cat1, 1.0 / Math.sqrt(numberOfMatchingCoreCats));
                } else if (distance >= 0.05 && distance < 0.1) {
                    featureVector.storeFeature("distance_50-100_"+cat1, 1.0 / Math.sqrt(numberOfMatchingCoreCats));
                } else if (distance >= 0.1 && distance < 0.3) {
                    featureVector.storeFeature("distance_100-300_"+cat1, 1.0 / Math.sqrt(numberOfMatchingCoreCats));
                } else if (distance >= 0.3 && distance < 0.6) {
                    featureVector.storeFeature("distance_300-600_"+cat1, 1.0 / Math.sqrt(numberOfMatchingCoreCats));
                } else if (distance >= 0.6 && distance < 1.0) {
                    featureVector.storeFeature("distance_600-1000_"+cat1, 1.0 / Math.sqrt(numberOfMatchingCoreCats));
                } else if (distance >= 1.0 && distance <= 2.0) {
                    featureVector.storeFeature("distance_1000-2000_"+cat1, 1.0 / Math.sqrt(numberOfMatchingCoreCats));
                } else if (distance >= 2.0 && distance < 4.0) {
                    featureVector.storeFeature("distance_2000-4000_"+cat1, 1.0 / Math.sqrt(numberOfMatchingCoreCats));
                    //     System.out.println("distance debug: " + distance + " " + label);
                } else if (distance >= 4.0 && distance < 8.0) {
                    featureVector.storeFeature("distance_4000-8000_"+cat1, 1.0 / Math.sqrt(numberOfMatchingCoreCats));
                } else {
                    featureVector.storeFeature("distance_>8000_"+cat1, 1.0 / Math.sqrt(numberOfMatchingCoreCats));
                }
            }
        }
        */

        it0 = p0.coreCategories.iterator();
        while (it0.hasNext()) {
            String cat1 = it0.next();
            if (p1.coreCategories.contains(cat1)) {
                coreCategoryMatch = true;
                //featureVector.storeFeature("coreCategory_match_" + cat1, 1.0);
                featureVector.storeFeature("coreCategory_match_" + cat1, 1.0 / Math.sqrt(numberOfMatchingCoreCats));
            }
        }

        if (coreCategoryMatch) {
            featureVector.storeFeature("coreCategory_match", 1.0);
        } else {

            /*
            if (coreCategoryAccomodation || coreCategoryLandmarkSight || coreCategoryTransportationFacility || coreCategoryAreaComplex) {
                if (coreCategoryAccomodation) {
                    featureVector.storeFeature("coreCategory_mismatch_accomodation", 1.0);
                    // System.out.println("category accomodation debug: " + p0.toString(p1));
                }
                if (coreCategoryLandmarkSight) {
                    featureVector.storeFeature("coreCategory_mismatch_landmark_sight", 1.0);
                    //System.out.println("category landmark_sight debug: ");
                }
                if (coreCategoryTransportationFacility) {
                    featureVector.storeFeature("coreCategory_mismatch_transportation-facility", 1.0);
                    //System.out.println("category landmark_sight debug: ");
                }
                if (coreCategoryAreaComplex) {
                                    featureVector.storeFeature("coreCategory_mismatch_area-complex", 1.0);
                                    //System.out.println("category landmark_sight debug: ");
                }
            } else {
                featureVector.storeFeature("coreCategory_mismatch", 1.0);
            }
            */


            int numberOfCoreCatsConcat = p0.coreCategories.size() + p1.coreCategories.size();
            it0 = p0.coreCategories.iterator();
            while (it0.hasNext()) {
                String cat0 = it0.next();
                //featureVector.storeFeature("coreCategory_mismatch_" + cat0, 1.0/numberOfCoreCatsConcat);
                featureVector.storeFeature("coreCategory_mismatch_" + cat0, 1.0 / Math.sqrt(numberOfCoreCatsConcat));
                //featureVector.storeFeature("coreCategory_mismatch_" + cat0, 1.0);
            }
            it1 = p1.coreCategories.iterator();
            while (it1.hasNext()) {
                String cat1 = it1.next();
                //featureVector.storeFeature("coreCategory_mismatch_" + cat1, 1.0/numberOfCoreCatsConcat);
                featureVector.storeFeature("coreCategory_mismatch_" + cat1, 1.0 / Math.sqrt(numberOfCoreCatsConcat));
                //featureVector.storeFeature("coreCategory_mismatch_" + cat1, 1.0);
            }

            featureVector.storeFeature("coreCategory_mismatch", 1.0);


            int numberOfCoreCatCombinations = p0.coreCategories.size() * p1.coreCategories.size();
            it0 = p0.coreCategories.iterator();
            while (it0.hasNext()) {
                String cat1 = it0.next();
                it1 = p1.coreCategories.iterator();
                while (it1.hasNext()) {
                    //featureVector.storeFeature("coreCategory_mismatch_" + DQUtils.sortConcatStringPairs(cat1, it1.next()), 1.0);
                    featureVector.storeFeature("coreCategory_mismatch_" + DQUtils.sortConcatStringPairs(cat1, it1.next()), 1.0 / Math.sqrt(numberOfCoreCatCombinations));
                }
            }
        }

        boolean hasEquivalentCoreCategoryMatch = false;
        if (!coreCategoryMatch) {
            OUTER_LOOP_EQUI:
            for (String coreCategory0 : p0.coreCategories) {
                for (String coreCategory1 : p1.coreCategories) {
                    if (equivalentCoreCategoriesMap.get(coreCategory0).contains(coreCategory1)) {
                        hasEquivalentCoreCategoryMatch = true;
                        featureVector.storeFeature("equivalentCoreCategory_match", 1.0);
                        break OUTER_LOOP_EQUI;
                    }
                }
            }
        }
        if (!hasEquivalentCoreCategoryMatch) {
            featureVector.storeFeature("equivalentCoreCategory_mismatch", 1.0);
        }


        while (it0.hasNext()) {
            String cat1 = it0.next();
            if (p1.coreCategories.contains(cat1)) {
                coreCategoryMatch = true;
                //featureVector.storeFeature("coreCategory_match_" + cat1, 1.0);
                featureVector.storeFeature("coreCategory_match_" + cat1, 1.0 / Math.sqrt(numberOfMatchingCoreCats));
            }
        }


        boolean categoryMatch = false;
        it0 = p0.categories.iterator();
        while (it0.hasNext()) {
            String cat1 = it0.next();
            if (p1.categories.contains(cat1)) {
                categoryMatch = true;
                //featureVector.storeFeature("category_match_"+cat1,1.0);
            }
        }
        if (categoryMatch) {
            featureVector.storeFeature("category_match", 1.0);
        } else {
            featureVector.storeFeature("category_mismatch", 1.0);
            it0 = p0.categories.iterator();
            while (it0.hasNext()) {
                String cat1 = it0.next();
                it1 = p1.categories.iterator();
                //-----while(it1.hasNext()) {
                //-----    featureVector.storeFeature("category_mismatch_"+DQUtils.sortConcatStringPairs(cat1,it1.next()),1.0);
                //-----}
            }
        }
        // parking-facility
        //public-transport


        if (p0.phone != null && p1.phone != null) {
            //if (p0.phone.equals(p1.phone) || getLevenshteinDistance(p0.phone, p1.phone, 1) <= 1)
            if (p0.phone.equals(p1.phone) || p0.phone.indexOf(p1.phone) >= 0 || p1.phone.indexOf(p0.phone) >= 0)
                featureVector.storeFeature("phone_match", 1.0);
                //else if(Math.abs(p0.phone.length() - p1.phone.length()) < 4)
            else
                featureVector.storeFeature("phone_mismatch", 1.0);
            //else
            //    System.out.println("phone debug large len difff: " + p0.phone + " " + p1.phone);
            //  if(!p0.phone.equals(p1.phone) && getLevenshteinDistance(p0.phone,  p1.phone, 1) < 4 && label == 1)
            //      System.out.println("phone debug: " + getLevenshteinDistance(p0.phone,  p1.phone, 1) + " " + p0.phone + " " + p1.phone);
        } else if (p0.phone != null || p1.phone != null) {
            featureVector.storeFeature("phone_1null", 1.0);
        } else {
            featureVector.storeFeature("phone_2null", 1.0);
        }

        /*
        if (p0.fax != null && p1.fax != null) {
            if (p0.fax.equals(p1.fax))
                featureVector.storeFeature("fax_match", 1.0);
            else
                featureVector.storeFeature("fax_mismatch", 1.0);
        }
        */

        if (p0.email != null && p1.email != null) {
            if (p0.email.equals(p1.email))
                featureVector.storeFeature("email_match", 1.0);
            else
                featureVector.storeFeature("email_mismatch", 1.0);
        } else if (p0.email != null || p1.email != null) {
            featureVector.storeFeature("email_1null", 1.0);
        } else {
            featureVector.storeFeature("email_2null", 1.0);
        }


        if (p0.email != null && p1.email != null) {
            if (p0.equalsEmailDomain(p1))
                featureVector.storeFeature("email_domain_match", 1.0);
            else
                featureVector.storeFeature("email_domain_mismatch", 1.0);

            //if (p0.equalsEmailDomain(p1) && !p0.email.equals(p1.email)) {
            //    System.out.println("email debug: " + label + " " + p0.email + " " + p1.email);
            //}
        }


        if (p0.website != null && p1.website != null) {
            if (p0.equalsWebsite(p1)) {
                featureVector.storeFeature("website_match", 1.0);
            } else {
                featureVector.storeFeature("website_mismatch", 1.0);
            }
        } else if (p0.website != null || p1.website != null) {
            featureVector.storeFeature("website_1null", 1.0);
        } else {
            featureVector.storeFeature("website_2null", 1.0);
        }


        /*
        if (p0.website != null && p1.website != null) {
            if (p0.website.equals(p1.website))
                featureVector.storeFeature("website_match", 1.0);
            //  else
            //      featureVector.storeFeature("website_mismatch", 1.0);
        }
        */


        if (p0.citySet != null && p1.citySet != null) {
            boolean match = false;
            boolean hungarianMatch = false;
            for (String city0 : p0.citySet) {
                for (String city1 : p1.citySet) {
                    HungarianFeatureExtractor hungarianFeatureExtractor = new HungarianFeatureExtractor(p0, p1, city0, city1, this, false, false, false);
                    if (hungarianFeatureExtractor.isHungarianMinMatch()) {
                        hungarianMatch = true;
                    }
                    /*
                    if (city0.equals(city1)) {
                        match = true;
                    }
                    */
                }
            }
            if (hungarianMatch) {
                featureVector.storeFeature("city_match", 1.0);
            } else {
                featureVector.storeFeature("city_mismatch", 1.0);
            }
            /*
            if(label == -1 && match != hungarianMatch) {
                System.out.println("\n\ncity regression error: " + match + " " + hungarianMatch + " " + p0.toString(p1));
            }
            */

        }


        /*
        if (p0.district != null && p1.district != null) {
            if (p0.district.equals(p1.district))
                featureVector.storeFeature("district_match", 1.0);
            else
                featureVector.storeFeature("district_mismatch", 1.0);
        }
        */

        /*
      if (p0.region != null && p1.region != null) {
          if (p0.region.equals(p1.region))
              featureVector.storeFeature("region_match", 1.0);
          else
              featureVector.storeFeature("region_mismatch", 1.0);
      }
        */

        StreetFeatureExtractorOld streetFeatureExtractorOld = new StreetFeatureExtractorOld(p0, p1, featureVector, label, this, p0.streetSet, p1.streetSet, p0.houseSet, p1.houseSet, p0.streetCatHouseSet, p1.streetCatHouseSet);

        StreetFeatureExtractor streetFeatureExtractor = new StreetFeatureExtractor(p0, p1, featureVector, label, this, p0.streetSet, p1.streetSet, p0.houseSet, p1.houseSet, p0.streetCatHouseSet, p1.streetCatHouseSet);

        /*
        if (streetFeatureExtractor.isStreetNull1() ^ streetFeatureExtractorOld.isStreetNull1()) {
            System.out.println("\n\nstreetNull1 regression error: " + streetFeatureExtractor.isStreetNull1() + " " + streetFeatureExtractorOld.isStreetNull1() + " " + p0.toString(p1));
        }

        if (streetFeatureExtractor.isStreetNull2() ^ streetFeatureExtractorOld.isStreetNull2()) {
            System.out.println("\n\nstreetNull2 regression error: " + streetFeatureExtractor.isStreetNull2() + " " + streetFeatureExtractorOld.isStreetNull2() + " " + p0.toString(p1));
        }

        if (streetFeatureExtractor.isHouseMatch() ^ streetFeatureExtractorOld.isHouseMatch()) {
            System.out.println("\n\nhouseMatch regression error: " + streetFeatureExtractor.isHouseMatch() + " " + streetFeatureExtractorOld.isHouseMatch() + p0.toString(p1));
        }
        if (streetFeatureExtractor.isHouseMismatch() ^ streetFeatureExtractorOld.isHouseMismatch()) {
            System.out.println("\n\nhouseMismatch regression error: " + streetFeatureExtractor.isHouseMismatch() + " " + streetFeatureExtractorOld.isHouseMismatch() + " " + p0.toString(p1));
        }
          */
        if (streetFeatureExtractor.isStreetMatch() ^ streetFeatureExtractorOld.isStreetMatch()) {
            System.out.println("\n\nstreetMatch regression error: " + streetFeatureExtractor.isStreetMatch() + " " + streetFeatureExtractorOld.isStreetMatch() + " " + p0.toString(p1));
        }
        /*
  if (streetFeatureExtractor.isStreetMismatch() ^ streetFeatureExtractorOld.isStreetMismatch()) {
      System.out.println("\n\nstreetMismatch regression error: " + streetFeatureExtractor.isStreetMismatch() + " " + streetFeatureExtractorOld.isStreetMismatch() + " " + p0.toString(p1));
  }
        */


        if (streetFeatureExtractor.isStreetNull1()) {
            featureVector.storeFeature("street_1null", 1.0);
        }

        if (streetFeatureExtractor.isStreetNull2()) {
            featureVector.storeFeature("street_2null", 1.0);
        }

        if (streetFeatureExtractor.isHouseMatch()) {
            featureVector.storeFeature("house_number_match", 1.0);
        }

        if (streetFeatureExtractor.isHouseMismatch()) {
            featureVector.storeFeature("house_number_mismatch", 1.0);
        }


        if (streetFeatureExtractor.isStreetMatch()) {
            featureVector.storeFeature("street_hungarian_chars_min_match", 1.0);
        }

        if (streetFeatureExtractor.isStreetMismatch()) {
            featureVector.storeFeature("street_hungarian_chars_min_mismatch", 1.0);
        }


        /*
        if(streetFeatureExtractor.isStreetMatch() != streetFeatureExtractorOld.isStreetMatch()) {
          System.out.println("\n\nstreet match diff: " + streetFeatureExtractor.isStreetMatch() + " : " + streetFeatureExtractorOld.isStreetMatch() + " " + p0.toString(p1));
        }
        */

/*
        if(p0.street != null && p1.street != null) {
            if(p0.street.equals(p1.street))
                featureVector.storeFeature("street_match",1.0);
            else
                featureVector.storeFeature("street_mismatch",1.0);
        }

        if(p0.house != null && p1.house != null) {
            if(p0.house.equals(p1.house))
                featureVector.storeFeature("house_match",1.0);
            else
                featureVector.storeFeature("house_mismatch",1.0);
        }
*/


        // HungarianFeatureExtractor placeNameFeatureExtractorOld = new HungarianFeatureExtractor(p0, p1, p0.normalizedName, p1.normalizedName, "normalizedName_", label, this);

        //System.out.println("before: \n" + featureVector);

        PlaceNameFeatureExtractorOld placeNameFeatureExtractorOld = new PlaceNameFeatureExtractorOld(p0, p1, label, this, p0.normalizedNameSet, p1.normalizedNameSet);

        PlaceNameFeatureExtractor placeNameFeatureExtractor = new PlaceNameFeatureExtractor(p0, p1, label, this, p0.normalizedNameSet, p1.normalizedNameSet);

        if (placeNameFeatureExtractor.isHungarianMinMatchChars()) {
            featureVector.storeFeature("normalizedName_is_hungarian_min_match_chars", 1.0);
            if (placeNameFeatureExtractor.isLeftOverToken()) {
                featureVector.storeFeature("normalizedName_is_left_over_token", 1.0);
            }
        } else {
            if (placeNameFeatureExtractor.isAtLeastOnePerfectMatchToken()) {
                featureVector.storeFeature("normalizedName_is_at_least_one_perfect_match_token", 1.0);
            } else {
                featureVector.storeFeature("normalizedName_is_hungarian_min_mismatch_chars", 1.0);
            }
        }

        /*
        if (distance < 1 && placeNameFeatureExtractor.isLeftOverToken() ) {
            System.out.println("\n\n has left over token: " + p0.toString(p1));

        }
        */
        /*
        if(p0.placeId.toString().equals("276u1qmb-05adbd7544454065895e940d40a9311f") && p1.placeId.toString().equals("276u1qmb-cd15c95a2ce448969a61abc58b221320")) {
            System.out.println("\n\n editdist c: " + p0.toString(p1));
             placeNameFeatureExtractor = new PlaceNameFeatureExtractor(p0, p1, label, this, p0.normalizedNameSet, p1.normalizedNameSet);
      

        }
        */


        if (distance < 1 && placeNameFeatureExtractor.isHungarianMinMatchChars() != placeNameFeatureExtractorOld.isHungarianMinMatchChars()) {
          //   if(p0.placeId.toString().equals("276u0x89-1b7e80a65eb5460db6fedcc43f6f3b66")) {
            System.out.println("\n\ndiff check new: " + placeNameFeatureExtractor.isHungarianMinMatchChars() + " old: " + placeNameFeatureExtractorOld.isHungarianMinMatchChars() + " " + p0.toString(p1));
            placeNameFeatureExtractorOld = new PlaceNameFeatureExtractorOld(p0, p1, label, this, p0.normalizedNameSet, p1.normalizedNameSet);

            //     placeNameFeatureExtractorNew = new PlaceNameFeatureExtractorOld(p0, p1, label, this, p0.normalizedNameSet, p1.normalizedNameSet);
          //       placeNameFeatureExtractor = new PlaceNameFeatureExtractor(p0, p1, label, this, p0.normalizedNameSet, p1.normalizedNameSet);

         //    }
        }

        /*
        if (placeNameFeatureExtractor.isHungarianMinMatchChars()) {
        if (distance < 1 && placeNameFeatureExtractor.isLeftOverToken() != placeNameFeatureExtractorOld.isLeftOverToken()) {
          //  if(p0.placeId.toString().equals("276u336t-223243b4a1c5469faaa5aab8969495ea")) {

            System.out.println("\n\ndiff check leftover new: " + placeNameFeatureExtractor.isLeftOverToken() + " old: " + placeNameFeatureExtractorOld.isLeftOverToken() + " " + "\ndiff check new: " + placeNameFeatureExtractor.isHungarianMinMatchChars() + " old: " + placeNameFeatureExtractorOld.isHungarianMinMatchChars() + " " + p0.toString(p1));
          //      placeNameFeatureExtractor = new PlaceNameFeatureExtractor(p0, p1, label, this, p0.normalizedNameSet, p1.normalizedNameSet);

          //  }

        }
        }
        */

        if (distance < 1 && placeNameFeatureExtractor.isAtLeastOnePerfectMatchToken() != placeNameFeatureExtractorOld.isAtLeastOnePerfectMatchToken()) {
            System.out.println("\n\ndiff check atleast1 new: " + placeNameFeatureExtractor.isAtLeastOnePerfectMatchToken() + " old: " + placeNameFeatureExtractorOld.isAtLeastOnePerfectMatchToken() + " " + p0.toString(p1));
        }


        /*
        if(hasEquivalentCoreCategoryMatch) {
            System.out.println("\n\nequi core cat match: " + p0.toString(p1));
        }
        */


        /*
        if(distance<1 && !placeNameFeatureExtractor.isHungarianPerfectMatch() && placeNameFeatureExtractor.isHungarianMinMatch() && coreCategoryMatch && placeNameFeatureExtractor.getCosineOnIDF_max() < 0.5  && placeNameFeatureExtractor.getCosineOnIDF_max() < 0.5) {
            System.out.println("\n\ncore cat match cosIDF: " + placeNameFeatureExtractor.getCosineOnIDF_max() + " minmatch: " + placeNameFeatureExtractor.isHungarianMinMatch() + " " + p0.toString(p1));
        }
        */


        /*
                if (placeNameFeatureExtractor.isHungarianMinMatch() && !placeNameFeatureExtractor.isAtLeastOnePerfectMatchToken()) {
                    System.out.println("\n\nname week match: " + placeNameFeatureExtractor.getHungarianPercentageMatchTokensMin() + " " + placeNameFeatureExtractor.getHungarianPercentageMatchTokensMax() + " " + p0.toString(p1));

                }
        */

        /*
        if (placeNameFeatureExtractor.isHungarianPerfectMatch()) {
            featureVector.storeFeature("normalizedName_is_hungarian_perfect_match", 1.0);
        } else {
            featureVector.storeFeature("normalizedName_is_hungarian_perfect_mismatch", 1.0);
        }





        featureVector.storeFeature("normalizedName_hungarian_percentage_match_tokens_max", placeNameFeatureExtractor.getHungarianPercentageMatchTokensMax());
        featureVector.storeFeature("normalizedName_hungarian_percentage_match_tokens_min", placeNameFeatureExtractor.getHungarianPercentageMatchTokensMin());
        featureVector.storeFeature("normalizedName_hungarian_percentage_match_chars_max", placeNameFeatureExtractor.getHungarianPercentageMatchCharsMax());
       */

        /*
            if (placeNameFeatureExtractor.isHungarianMinMatch() != placeNameFeatureExtractorNew.isHungarianMinMatch()) {
            // if(!streetFeatureExtractor.isStreetMatch()) {
            //    System.out.println("\n\nsize : " + sizefeatlist1 + " " + sizefeatlist2);

           // System.out.println("\n\n getHungarianPercentageMatchTokensMax() : " + placeNameFeatureExtractor.getHungarianPercentageMatchTokensMax());
           // System.out.println("\n\n getHungarianPercentageMatchTokensMin() : " + placeNameFeatureExtractor.getHungarianPercentageMatchTokensMin());
           // System.out.println("\n\n getHungarianPercentageMatchCharsMax() : " + placeNameFeatureExtractor.getHungarianPercentageMatchCharsMax());
           System.out.println("\n\n isHungarianMinMatch() : " + placeNameFeatureExtractor.isHungarianMinMatch());
                System.out.println("\n\n isHungarianMinMatch() : " + placeNameFeatureExtractorNew.isHungarianMinMatch());

            //  System.out.println("\n\n getNumNonCharMatchTokens() : " + placeNameFeatureExtractor.getNumNonCharMatchTokens());


            System.out.println("\n\n" + p0.toString(p1));
           // System.out.println("after: \n" + featureVector);
             }

*/

        /*

        if (placeNameFeatureExtractor.isHungarianMinMatch() != placeNameFeatureExtractor.isHungarianPerfectMatch()) {
            // if(!streetFeatureExtractor.isStreetMatch()) {
            //    System.out.println("\n\nsize : " + sizefeatlist1 + " " + sizefeatlist2);

            System.out.println("\n\n getHungarianPercentageMatchTokensMax() : " + placeNameFeatureExtractor.getHungarianPercentageMatchTokensMax());
            System.out.println("\n\n getHungarianPercentageMatchTokensMin() : " + placeNameFeatureExtractor.getHungarianPercentageMatchTokensMin());
            System.out.println("\n\n getHungarianPercentageMatchCharsMax() : " + placeNameFeatureExtractor.getHungarianPercentageMatchCharsMax());
            System.out.println("\n\n isHungarianMinMatch() : " + placeNameFeatureExtractor.isHungarianMinMatch());
            //  System.out.println("\n\n getNumNonCharMatchTokens() : " + placeNameFeatureExtractor.getNumNonCharMatchTokens());


            System.out.println("\n\n" + p0.toString(p1));
            System.out.println("after: \n" + featureVector);
            // }
        }
        */

    }


    public void debugOutput(String s) {
        if (debugOutput) {
            System.out.print(s);
        }
    }

    public void debugOutputln(String s) {
        if (debugOutput) {
            System.out.println(s);
        }
    }

    public void debugOutputln(String s, Place p1, Place p2) {
        if (debugOutput) {
            System.out.println(s + p1.toString(p2));
        }
    }

    public void debugOutputln(String s, Place p) {
        if (debugOutput) {
            System.out.println(s + p.toString());
        }
    }


/*

    public static int predictProbability(Model model, FeatureNode[] x, double[] prob_estimates) {
        if (model.solverType == SolverType.L2R_LR) {
            int nr_class = model.nr_class;
            int nr_w;
            if (nr_class == 2)
                nr_w = 1;
            else
                nr_w = nr_class;

            int label = predictValues(model, x, prob_estimates);
            for (int i = 0; i < nr_w; i++)
                prob_estimates[i] = 1 / (1 + Math.exp(-prob_estimates[i]));

            if (nr_class == 2) // for binary classification
                prob_estimates[1] = 1. - prob_estimates[0];
            else {
                double sum = 0;
                for (int i = 0; i < nr_class; i++)
                    sum += prob_estimates[i];

                for (int i = 0; i < nr_class; i++)
                    prob_estimates[i] = prob_estimates[i] / sum;
            }

            return label;
        } else
            return 0;
    }

    public static int predictValues(Model model, FeatureNode[] x, double[] dec_values) {
        int n;
        if (model.bias >= 0)
            n = model.nr_feature + 1;
        else
            n = model.nr_feature;

        double[] w = model.w;

        int nr_w;
        if (model.nr_class == 2 && model.solverType != SolverType.MCSVM_CS)
            nr_w = 1;
        else
            nr_w = model.nr_class;

        for (int i = 0; i < nr_w; i++)
            dec_values[i] = 0;

        for (FeatureNode lx : x) {
            int idx = lx.index;
            // the dimension of testing data may exceed that of training
            if (idx <= n) {
                for (int i = 0; i < nr_w; i++) {
                    dec_values[i] += w[(idx - 1) * nr_w + i] * lx.value;
                }
            }
        }

        if (model.nr_class == 2)
            return (dec_values[0] > 0) ? model.label[0] : model.label[1];
        else {
            int dec_max_idx = 0;
            for (int i = 1; i < model.nr_class; i++) {
                if (dec_values[i] > dec_values[dec_max_idx]) dec_max_idx = i;
            }
            return model.label[dec_max_idx];
        }
    }

*/


    public void writeDictionaries() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(dictionariesFileName));

            Iterator<String> languageIterator = languageCategoryDictionaries.keySet().iterator();
            while (languageIterator.hasNext()) {
                String language = languageIterator.next();
                HashMap<String, HashMap<String, Integer>> categoryDictionaries = languageCategoryDictionaries.get(language);

                for (String s : categoryDictionaries.keySet()) {
                    String category = s;
                    HashMap<String, Integer> dictionary = categoryDictionaries.get(category);

                    out.print(language + "\t" + category);

                    for (String s1 : dictionary.keySet()) {
                        String token = s1;
                        Integer tokenCount = dictionary.get(token);
                        if (tokenCount > 1 || token.startsWith("###")) {
                            out.print("\t" + token + ":" + tokenCount);
                        }
                    }
                    out.println();
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

    public void loadDictionaries() {
        languageCategoryDictionaries = new HashMap<String, HashMap<String, HashMap<String, Integer>>>();

        try {
            BufferedReader rawDataBufferedReader = new BufferedReader(new FileReader(dictionariesFileName));

            String line = "";
            StringTokenizer st;

            while ((line = rawDataBufferedReader.readLine()) != null) {
                //    System.out.println("debug: "+ strLine);
                //tokenize comma separated line using "\t"
                st = new StringTokenizer(line, "\t");

                String language = st.nextToken();
                String category = st.nextToken();

                while (st.hasMoreTokens()) {
                    String tokenColonFrequency = st.nextToken();
                    String token = tokenColonFrequency.substring(0, tokenColonFrequency.lastIndexOf(":"));
                    String count = tokenColonFrequency.substring(tokenColonFrequency.lastIndexOf(":") + 1);

                    //System.out.println(language + " " + category + " " + token + " " + count);

                    HashMap<String, HashMap<String, Integer>> categoryDictionaries = languageCategoryDictionaries.get(language);
                    if (categoryDictionaries == null) {
                        categoryDictionaries = new HashMap<String, HashMap<String, Integer>>();
                        languageCategoryDictionaries.put(language, categoryDictionaries);
                    }

                    HashMap<String, Integer> categoryDictionary = categoryDictionaries.get(category);
                    if (categoryDictionary == null) {
                        categoryDictionary = new HashMap<String, Integer>();
                        categoryDictionaries.put(category, categoryDictionary);
                    }

                    categoryDictionary.put(token, new Integer(count));
                }

            }
            rawDataBufferedReader.close();

        }
        catch (Exception e) {
            System.out.println("###########Exception: " + e);
            e.printStackTrace();
            System.exit(-1);
        }
    }


    public void fillDictionaries(Place place) {
        if (place.normalizedNameSet != null) {
            for (String languageTabName : place.normalizedNameSet) {
                String language = Place.getStringBeforeTab(languageTabName);
                String name = Place.getStringAfterTab(languageTabName);

                HashMap<String, HashMap<String, Integer>> categoryDictionaries = languageCategoryDictionaries.get(language);
                if (categoryDictionaries == null) {
                    categoryDictionaries = new HashMap<String, HashMap<String, Integer>>();
                    languageCategoryDictionaries.put(language, categoryDictionaries);
                }

                for (String coreCategory1 : place.coreCategories) {
                    String coreCategory = coreCategory1;
                    HashMap<String, Integer> categoryDictionary = categoryDictionaries.get(coreCategory);
                    if (categoryDictionary == null) {
                        categoryDictionary = new HashMap<String, Integer>();
                        categoryDictionaries.put(coreCategory, categoryDictionary);
                    }

                    StringTokenizer tokenizer = new StringTokenizer(name, " ");

                    // this is just the sum over all hash table entries
                    Integer sumCounts = categoryDictionary.get("### total sum of counts ###");
                    if (sumCounts == null) {
                        sumCounts = tokenizer.countTokens();
                        categoryDictionary.put("### total sum of counts ###", sumCounts);
                    } else {
                        categoryDictionary.put("### total sum of counts ###", sumCounts + tokenizer.countTokens());
                    }

                    // this corresponds to the number of documents in TF-IDF
                    Integer totalFrequency = categoryDictionary.get("### total number of names ###");
                    if (totalFrequency == null) {
                        totalFrequency = 1;
                        categoryDictionary.put("### total number of names ###", totalFrequency);
                    } else {
                        categoryDictionary.put("### total number of names ###", totalFrequency + 1);
                    }

                    while (tokenizer.hasMoreTokens()) {
                        String token = tokenizer.nextToken();
                        Integer tokenFrequency = categoryDictionary.get(token);
                        if (tokenFrequency == null) {
                            tokenFrequency = 1;
                            categoryDictionary.put(token, tokenFrequency);
                        } else {
                            categoryDictionary.put(token, tokenFrequency + 1);
                        }
                    }
                }
            }
        }

        //System.out.println("split lang: "+Place.getStringBeforeTab(name)+ " name: " + Place.getStringAfterTab(name));

    }

    public int getDictionaryCount(String language, String category, String token) {
        HashMap<String, HashMap<String, Integer>> categoryDictionaries = languageCategoryDictionaries.get(language);
        if (categoryDictionaries != null) {
            HashMap<String, Integer> categoryDictionary = categoryDictionaries.get(category);
            if (categoryDictionary != null) {

                Integer tokenFrequency = categoryDictionary.get(token);
                if (tokenFrequency != null) {
                    return tokenFrequency;
                }
            }
        }
        return 1;
    }

    public int getDictionaryTotalNumberOfPlacesPerCategory(String language, String category) {
        HashMap<String, HashMap<String, Integer>> categoryDictionaries = languageCategoryDictionaries.get(language);
        if (categoryDictionaries != null) {
            HashMap<String, Integer> categoryDictionary = categoryDictionaries.get(category);
            if (categoryDictionary != null) {
                Integer tokenFrequency = categoryDictionary.get("### total number of names ###");
                if (tokenFrequency != null) {
                    return tokenFrequency;
                }
            }
        }
        return 1;
    }


    void fillEquivalentCoreCategoriesMap() {
        equivalentCoreCategoriesMap = new HashMap<String, HashSet<String>>();
        HashSet<String> hashSet;

        hashSet = new HashSet<String>();
        hashSet.add("locality");
        hashSet.add("area-complex");
        equivalentCoreCategoriesMap.put("administrative-region", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("administrative-region");
        hashSet.add("area-complex");
        equivalentCoreCategoriesMap.put("locality", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("transportation-facility");
        hashSet.add("business-service");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("transportation-network", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("administrative-region");
        hashSet.add("locality");
        hashSet.add("landmark-sight");
        hashSet.add("business-service");
        equivalentCoreCategoriesMap.put("area-complex", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("landmark-sight");
        hashSet.add("government-facility");
        hashSet.add("religious-facility");
        hashSet.add("sports-facility");
        hashSet.add("educational-facility");
        hashSet.add("arts-entertainment-venue");
        hashSet.add("healthcare-facility");
        hashSet.add("accommodation");
        hashSet.add("eat-drink");
        hashSet.add("going-out");
        hashSet.add("shopping-repair");
        hashSet.add("business-service");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("building", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("area-complex");
        hashSet.add("building");
        hashSet.add("hydrographic-feature");
        hashSet.add("hypsographic-feature");
        hashSet.add("undersea-feature");
        hashSet.add("vegetation");
        hashSet.add("government-facility");
        hashSet.add("religious-facility");
        hashSet.add("sports-facility");
        hashSet.add("educational-facility");
        hashSet.add("arts-entertainment-venue");
        hashSet.add("accommodation");
        hashSet.add("eat-drink");
        hashSet.add("going-out");
        hashSet.add("shopping-repair");
        hashSet.add("business-service");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("landmark-sight", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("sports-facility");
        hashSet.add("landmark-sight");
        equivalentCoreCategoriesMap.put("hydrographic-feature", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("sports-facility");
        hashSet.add("landmark-sight");
        equivalentCoreCategoriesMap.put("hypsographic-feature", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("landmark-sight");
        equivalentCoreCategoriesMap.put("undersea-feature", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("landmark-sight");
        equivalentCoreCategoriesMap.put("vegetation", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("landmark-sight");
        hashSet.add("building");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("government-facility", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("transportation-network");
        hashSet.add("business-service");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("transportation-facility", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("building");
        hashSet.add("landmark-sight");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("religious-facility", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("building");
        hashSet.add("landmark-sight");
        hashSet.add("hydrographic-feature");
        hashSet.add("hypsographic-feature");
        hashSet.add("educational-facility");
        hashSet.add("business-service");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("sports-facility", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("building");
        hashSet.add("landmark-sight");
        hashSet.add("sports-facility");
        hashSet.add("other-facility");
        hashSet.add("business-service");
        equivalentCoreCategoriesMap.put("educational-facility", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("building");
        hashSet.add("landmark-sight");
        hashSet.add("eat-drink");
        hashSet.add("going-out");
        hashSet.add("shopping-repair");
        hashSet.add("business-service");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("arts-entertainment-venue", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("building");
        hashSet.add("business-service");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("healthcare-facility", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("building");
        hashSet.add("landmark-sight");
        hashSet.add("eat-drink");
        hashSet.add("going-out");
        hashSet.add("shopping-repair");
        hashSet.add("business-service");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("accommodation", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("building");
        hashSet.add("landmark-sight");
        hashSet.add("arts-entertainment-venue");
        hashSet.add("accommodation");
        hashSet.add("going-out");
        hashSet.add("shopping-repair");
        hashSet.add("business-service");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("eat-drink", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("building");
        hashSet.add("landmark-sight");
        hashSet.add("arts-entertainment-venue");
        hashSet.add("accommodation");
        hashSet.add("eat-drink");
        hashSet.add("shopping-repair");
        hashSet.add("business-service");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("going-out", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("building");
        hashSet.add("landmark-sight");
        hashSet.add("arts-entertainment-venue");
        hashSet.add("accommodation");
        hashSet.add("eat-drink");
        hashSet.add("going-out");
        hashSet.add("business-service");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("shopping-repair", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("transportation-network");
        hashSet.add("area-complex");
        hashSet.add("building");
        hashSet.add("landmark-sight");
        hashSet.add("transportation-facility");
        hashSet.add("sports-facility");
        hashSet.add("educational-facility");
        hashSet.add("arts-entertainment-venue");
        hashSet.add("healthcare-facility");
        hashSet.add("accommodation");
        hashSet.add("eat-drink");
        hashSet.add("going-out");
        hashSet.add("shopping-repair");
        hashSet.add("other-facility");
        equivalentCoreCategoriesMap.put("business-service", hashSet);

        hashSet = new HashSet<String>();
        hashSet.add("transportation-network");
        hashSet.add("building");
        hashSet.add("landmark-sight");
        hashSet.add("transportation-facility");
        hashSet.add("government-facility");
        hashSet.add("religious-facility");
        hashSet.add("sports-facility");
        hashSet.add("educational-facility");
        hashSet.add("arts-entertainment-venue");
        hashSet.add("healthcare-facility");
        hashSet.add("accommodation");
        hashSet.add("eat-drink");
        hashSet.add("going-out");
        hashSet.add("shopping-repair");
        hashSet.add("business-service");
        equivalentCoreCategoriesMap.put("other-facility", hashSet);


    }

    void verifyEquivalentCoreCategoriesMap() {
        for (String key : equivalentCoreCategoriesMap.keySet()) {
            HashSet<String> set = equivalentCoreCategoriesMap.get(key);
            for (String element : set) {
                if (!equivalentCoreCategoriesMap.get(element).contains(key)) {
                    System.out.println("inconsistency: " + key + " " + element);
                }
            }
        }
    }

}

