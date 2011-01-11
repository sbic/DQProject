

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


public class PlaceNameFeatureExtractor {

    boolean isHungarianMinMatchChars_best = false;
    boolean isAtLeastOnePerfectMatchToken_best = false;
    boolean isLeftOverToken_best = true;

    HungarianFeatureExtractor[] pairwiseHungarianFeatureExtractors;
    boolean[] hasPrefixTokenMatch;
    boolean atLeastOnePrefixTokenMatch = false;
    HashSet<String> addressTokens0, addressTokens1;

    public PlaceNameFeatureExtractor(Place p0, Place p1, int label, FeatureProcessor featureProcessor, Set<String> nameSet0, Set<String> nameSet1) {
        //System.out.println(" street feature exractor ");


        addressTokens0 = new HashSet<String>();
        addressTokens1 = new HashSet<String>();

        extractAddressTokenDictionary(p0, addressTokens0);
        extractAddressTokenDictionary(p1, addressTokens1);


        pairwiseHungarianFeatureExtractors = new HungarianFeatureExtractor[nameSet0.size() * nameSet1.size()];
        hasPrefixTokenMatch = new boolean[nameSet0.size() * nameSet1.size()];

        // System.out.println("\n\n\n\n $############### new place " );


        boolean atLeastOneLanguageMatch = false;
        if (nameSet0 != null && nameSet1 != null) {
            OUTERMOST_LANGUAGE_LOOP:
            for (String languageTabName0 : nameSet0) {
                for (String languageTabName1 : nameSet1) {
                    String language0 = Place.getStringBeforeTab(languageTabName0);
                    String language1 = Place.getStringBeforeTab(languageTabName1);
                    if (language0.equals(language1)) {
                        atLeastOneLanguageMatch = true;
                        break OUTERMOST_LANGUAGE_LOOP;
                    }
                }
            }

            int count = 0;
            for (String languageTabName0 : nameSet0) {
                for (String languageTabName1 : nameSet1) {
                    //System.out.println("debug place name feat extractor: " + atLeastOneLanguageMatch + " " + languageTabName0 + " " + languageTabName1);

                    String language0 = Place.getStringBeforeTab(languageTabName0);
                    String language1 = Place.getStringBeforeTab(languageTabName1);
                    String name0 = Place.getStringAfterTab(languageTabName0);
                    String name1 = Place.getStringAfterTab(languageTabName1);

                    /*
                    System.out.println("debug place name feat extractor1: " + languageTabName0 + " " + languageTabName1);
                    System.out.println("debug place name feat extractor2: " + language0 + " " + language1);
                    System.out.println("debug place name feat extractor3: " + name0 + " " + name1);
                      */

                    HungarianFeatureExtractor normalizedNameHungarianFeatureExtractor;
                    hasPrefixTokenMatch[count] = false;

                    // if at least one language matches then the cross-language name matches need to be exact
                    if (atLeastOneLanguageMatch && !language0.equals(language1)) {
                        normalizedNameHungarianFeatureExtractor = new HungarianFeatureExtractor(p0, p1, name0, name1, featureProcessor, addressTokens0, addressTokens1, true, false, true);
                    } else {
                        normalizedNameHungarianFeatureExtractor = new HungarianFeatureExtractor(p0, p1, name0, name1, featureProcessor, addressTokens0, addressTokens1, false, false, true);
                    }

                    isHungarianMinMatchChars_best = isHungarianMinMatchChars_best | normalizedNameHungarianFeatureExtractor.isHungarianMinMatch();
                    isAtLeastOnePerfectMatchToken_best = isAtLeastOnePerfectMatchToken_best | normalizedNameHungarianFeatureExtractor.isAtLeastOnePerfectMatchToken();
                    if (normalizedNameHungarianFeatureExtractor.isHungarianMinMatch()) {
                        isLeftOverToken_best = isLeftOverToken_best & normalizedNameHungarianFeatureExtractor.isLeftOverToken();
                    }

                    pairwiseHungarianFeatureExtractors[count] = normalizedNameHungarianFeatureExtractor;

                    if (!normalizedNameHungarianFeatureExtractor.isHungarianMinMatch() && !isHungarianMinMatchChars_best) {
                        hasPrefixTokenMatch[count] = false;
                        OUTER_STARTSWITH_CHECK:
                        for (String token0 : normalizedNameHungarianFeatureExtractor.getTokens0()) {
                            for (String token1 : normalizedNameHungarianFeatureExtractor.getTokens1()) {
                                if (!token0.equals(token1) && (token0.startsWith(token1) || token0.endsWith(token1) || token1.startsWith(token0) || token1.endsWith(token0))) {
                                    hasPrefixTokenMatch[count] = true;
                                    atLeastOnePrefixTokenMatch = true;
                                    break OUTER_STARTSWITH_CHECK;
                                }
                            }
                        }
                    }
                    count++;
                }
            }


            if (!isHungarianMinMatchChars_best && atLeastOnePrefixTokenMatch) {
                count = 0;
                HungarianFeatureExtractor normalizedNameHungarianFeatureExtractor;
                for (String languageTabName0 : nameSet0) {
                    for (String languageTabName1 : nameSet1) {
                        //System.out.println("debug place name feat extractor: " + atLeastOneLanguageMatch + " " + languageTabName0 + " " + languageTabName1);

                        if (hasPrefixTokenMatch[count]) {
                            String language0 = Place.getStringBeforeTab(languageTabName0);
                            String language1 = Place.getStringBeforeTab(languageTabName1);
                            String name0 = Place.getStringAfterTab(languageTabName0);
                            String name1 = Place.getStringAfterTab(languageTabName1);

                            for (int j = 0; j < pairwiseHungarianFeatureExtractors[count].getTokens0().length - 1; j++) {
                                boolean singleCharToken = (pairwiseHungarianFeatureExtractors[count].getTokens0()[j].length() == 1) | (pairwiseHungarianFeatureExtractors[count].getTokens0()[j + 1].length() == 1);

                                StringBuffer concatTokens = new StringBuffer();
                                for (int k = 0; k < j; k++) {
                                    concatTokens.append(pairwiseHungarianFeatureExtractors[count].getTokens0()[k]);
                                    concatTokens.append(' ');
                                }
                                concatTokens.append(pairwiseHungarianFeatureExtractors[count].getTokens0()[j]);
                                concatTokens.append(pairwiseHungarianFeatureExtractors[count].getTokens0()[j + 1]);
                                for (int k = j + 2; k < pairwiseHungarianFeatureExtractors[count].getTokens0().length; k++) {
                                    concatTokens.append(' ');
                                    concatTokens.append(pairwiseHungarianFeatureExtractors[count].getTokens0()[k]);
                                }

                                if ((atLeastOneLanguageMatch && !language0.equals(language1)) || singleCharToken) {
                                    normalizedNameHungarianFeatureExtractor = new HungarianFeatureExtractor(p0, p1, concatTokens.toString(), name1, featureProcessor, addressTokens0, addressTokens1, true, true, true);
                                } else {
                                    normalizedNameHungarianFeatureExtractor = new HungarianFeatureExtractor(p0, p1, concatTokens.toString(), name1, featureProcessor, addressTokens0, addressTokens1, false, true, true);
                                }
                                isHungarianMinMatchChars_best = isHungarianMinMatchChars_best | normalizedNameHungarianFeatureExtractor.isHungarianMinMatch();
                                isAtLeastOnePerfectMatchToken_best = isAtLeastOnePerfectMatchToken_best | normalizedNameHungarianFeatureExtractor.isAtLeastOnePerfectMatchToken();
                                if (normalizedNameHungarianFeatureExtractor.isHungarianMinMatch()) {
                                    isLeftOverToken_best = isLeftOverToken_best & normalizedNameHungarianFeatureExtractor.isLeftOverToken();
                                }
                            }

                            for (int j = 0; j < pairwiseHungarianFeatureExtractors[count].getTokens1().length - 1; j++) {
                                boolean singleCharToken = (pairwiseHungarianFeatureExtractors[count].getTokens1()[j].length() == 1) | (pairwiseHungarianFeatureExtractors[count].getTokens1()[j + 1].length() == 1);

                                StringBuffer concatTokens = new StringBuffer();
                                for (int k = 0; k < j; k++) {
                                    concatTokens.append(pairwiseHungarianFeatureExtractors[count].getTokens1()[k]);
                                    concatTokens.append(' ');
                                }
                                concatTokens.append(pairwiseHungarianFeatureExtractors[count].getTokens1()[j]);
                                concatTokens.append(pairwiseHungarianFeatureExtractors[count].getTokens1()[j + 1]);
                                for (int k = j + 2; k < pairwiseHungarianFeatureExtractors[count].getTokens1().length; k++) {
                                    concatTokens.append(' ');
                                    concatTokens.append(pairwiseHungarianFeatureExtractors[count].getTokens1()[k]);
                                }

                                if ((atLeastOneLanguageMatch && !language0.equals(language1)) || singleCharToken) {
                                    normalizedNameHungarianFeatureExtractor = new HungarianFeatureExtractor(p0, p1, name0, concatTokens.toString(), featureProcessor, addressTokens0, addressTokens1, true, true, true);
                                } else {
                                    normalizedNameHungarianFeatureExtractor = new HungarianFeatureExtractor(p0, p1, name0, concatTokens.toString(), featureProcessor, addressTokens0, addressTokens1, false, true, true);
                                }
                                isHungarianMinMatchChars_best = isHungarianMinMatchChars_best | normalizedNameHungarianFeatureExtractor.isHungarianMinMatch();
                                isAtLeastOnePerfectMatchToken_best = isAtLeastOnePerfectMatchToken_best | normalizedNameHungarianFeatureExtractor.isAtLeastOnePerfectMatchToken();
                                if (normalizedNameHungarianFeatureExtractor.isHungarianMinMatch()) {
                                    isLeftOverToken_best = isLeftOverToken_best & normalizedNameHungarianFeatureExtractor.isLeftOverToken();
                                }
                            }
                            count++;
                        }
                    }
                }

            }


            /*
            if (isHungarianMinMatchChars_best) {

                // loop over pairs of languages
                for (HungarianFeatureExtractor pairwiseHungarianFeatureExtractor : pairwiseHungarianFeatureExtractors) {
                    if (pairwiseHungarianFeatureExtractor.getNumberOfFilteredMatchTokens() > 0) {
                        // loop over all pairs of categories
                        for (String category0 : p0.coreCategories) {
                            int totalNumberOfPlacesPerCategory0 = featureProcessor.getDictionaryTotalNumberOfPlacesPerCategory(pairwiseHungarianFeatureExtractor.getLanguage0(), category0);
                            boolean statsValid0 = true;
                            if (totalNumberOfPlacesPerCategory0 < 100) {
                                statsValid0 = false;
                            }

                            for (String category1 : p1.coreCategories) {
                                int totalNumberOfPlacesPerCategory1 = featureProcessor.getDictionaryTotalNumberOfPlacesPerCategory(pairwiseHungarianFeatureExtractor.getLanguage1(), category0);
                                boolean statsValid1 = true;
                                if (totalNumberOfPlacesPerCategory1 < 100) {
                                    statsValid1 = false;
                                }

                                double normalizer0 = 0;
                                double normalizer1 = 0;

                                // compute normalizer from matching tokens and non-matching tokens
                                // first collect normalizer for matching tokens
                                for (StringPair matchPair : pairwiseHungarianFeatureExtractor.getMatchTokenPairs()) {
                                    int tokenCount0 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage0(), category0, matchPair.getString0());
                                    int tokenCount1 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage1(), category1, matchPair.getString0());

                                    if (statsValid0) {
                                        normalizer0 += Math.pow(Math.log(totalNumberOfPlacesPerCategory0 / tokenCount0), 2);
                                    } else {
                                        normalizer0 += 1;
                                    }
                                    if (statsValid1) {
                                        normalizer1 += Math.pow(Math.log(totalNumberOfPlacesPerCategory1 / tokenCount1), 2);
                                    } else {
                                        normalizer1 += 1;
                                    }
                                    //System.out.println("idf debug " + matchPair.getString0() + " " + tokenCount0 + " " + totalNumberOfPlacesPerCategory0 + " " + matchPair.getString0() + " " + tokenCount1 + " " + totalNumberOfPlacesPerCategory1);

                                }

                                // now collect normalizer for non-matching tokens from name0
                                for (String nonMatchToken0 : pairwiseHungarianFeatureExtractor.getNonMatchingTokens0()) {
                                    int tokenCount0 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage0(), category0, nonMatchToken0);

                                    if (statsValid0) {
                                        normalizer0 += Math.pow(Math.log(totalNumberOfPlacesPerCategory0 / tokenCount0), 2);
                                    } else {
                                        normalizer0 += 1;
                                    }
                                }

                                // now collect normalizer for non-matching tokens from name1
                                for (String nonMatchToken1 : pairwiseHungarianFeatureExtractor.getNonMatchingTokens1()) {
                                    int tokenCount1 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage1(), category1, nonMatchToken1);

                                    if (statsValid1) {
                                        normalizer1 += Math.pow(Math.log(totalNumberOfPlacesPerCategory1 / tokenCount1), 2);
                                    } else {
                                        normalizer1 += 1;
                                    }


                                }

                                normalizer0 = 1/Math.sqrt(normalizer0);
                                normalizer1 = 1/Math.sqrt(normalizer1);

                                // normalizer is now complete

                                // compute cosine on IDF values
                                double cosineOnIDF = 0;
                                for (StringPair matchPair : pairwiseHungarianFeatureExtractor.getMatchTokenPairs()) {
                                    int tokenCount0 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage0(), category0, matchPair.getString0());
                                    int tokenCount1 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage1(), category1, matchPair.getString0());

                                    double factor0, factor1;
                                    if (statsValid0) {
                                        factor0 = normalizer0*Math.log(totalNumberOfPlacesPerCategory0 / tokenCount0);
                                    } else {
                                        factor0 = normalizer0;
                                    }
                                    if (statsValid1) {
                                        factor1 = normalizer1*Math.log(totalNumberOfPlacesPerCategory1 / tokenCount1);
                                    } else {
                                        factor1 = normalizer1;
                                    }

                                    cosineOnIDF+=factor0*factor1;
                                    //System.out.println("idf debug " + matchPair.getString0() + " " + tokenCount0 + " " + totalNumberOfPlacesPerCategory0 + " " + matchPair.getString0() + " " + tokenCount1 + " " + totalNumberOfPlacesPerCategory1 + " factor0:" + factor0 + " factor1: " + factor1 + " product: " + factor0*factor1);
                                    //System.out.println(" " + matchPair.getString0() + " " + tokenCount0 + " " + totalNumberOfPlacesPerCategory0 + " " + matchPair.getString0() + " " + tokenCount1 + " " + totalNumberOfPlacesPerCategory1);

                                }

                                cosineOnIDF_max = Math.max(cosineOnIDF_max, cosineOnIDF);
                                //System.out.println("idf debug: " + pairwiseHungarianFeatureExtractor.getInputString0() + ":" +  pairwiseHungarianFeatureExtractor.getLanguage0() + ":" + category0 + " --- " + pairwiseHungarianFeatureExtractor.getInputString1() + ":" + pairwiseHungarianFeatureExtractor.getLanguage1()  + ":" + category1 + " --- " + cosineOnIDF + " " + cosineOnIDF_max);

                            }
                        }


                        //  featureProcessor.getDictionaryTotalNumberOfPlacesPerCategory(pairwiseHungarianFeatureExtractors[i].get)
                    }
                }
            }
            */

        } else

        {
            System.out.println("WARNING: name feature extractor: name null " + p0 + " " + p1);
            System.exit(-1);
        }

    }

    void extractAddressTokenDictionary(Place place, HashSet<String> addressTokenSet) {
        // System.out.println(place);

        /*
        if (place.extensionSet != null) {
            for (String string : place.extensionSet) {
                StringTokenizer tokenizer = DQUtils.normalizeNameTokenizerWithApostrophe(string);
                while (tokenizer.hasMoreTokens()) {
                    addressTokenSet.add(tokenizer.nextToken());
                }
            }
        }
        */


        if (place.districtSet != null) {
            for (String string : place.districtSet) {
                StringTokenizer tokenizer = DQUtils.normalizeNameTokenizerWithApostrophe(string);
                while (tokenizer.hasMoreTokens()) {
                    addressTokenSet.add(tokenizer.nextToken());
                }
            }
        }
        if (place.regionSet != null) {
            for (String string : place.regionSet) {
                StringTokenizer tokenizer = DQUtils.normalizeNameTokenizerWithApostrophe(string);
                while (tokenizer.hasMoreTokens()) {
                    addressTokenSet.add(tokenizer.nextToken());
                }
            }
        }

        if (place.citySet != null) {
            for (String string : place.citySet) {
                StringTokenizer tokenizer = DQUtils.normalizeNameTokenizerWithApostrophe(string);
                while (tokenizer.hasMoreTokens()) {
                    addressTokenSet.add(tokenizer.nextToken());
                }
            }
        }
        if (place.streetSet != null) {
            for (String string : place.streetSet) {
                StringTokenizer tokenizer = DQUtils.normalizeNameTokenizerWithApostrophe(string);
                while (tokenizer.hasMoreTokens()) {
                    addressTokenSet.add(tokenizer.nextToken());
                }
            }
        }

    }

    public boolean isHungarianMinMatchChars() {
        return isHungarianMinMatchChars_best;
    }

    public boolean isAtLeastOnePerfectMatchToken() {
        return isAtLeastOnePerfectMatchToken_best;
    }

    public boolean isLeftOverToken() {
        return isLeftOverToken_best;
    }

}


/*

// IDF filter, did not work
if (!isHungarianMinMatchChars_best && numberOfFilteredMatchTokens_max > 0) {

    // loop over pairs of languages
    for (HungarianFeatureExtractor pairwiseHungarianFeatureExtractor : pairwiseHungarianFeatureExtractors) {
        if (pairwiseHungarianFeatureExtractor.getNumberOfFilteredMatchTokens() > 0) {
            // loop over all pairs of categories
            for (String category0 : p0.coreCategories) {
                int totalNumberOfPlacesPerCategory0 = featureProcessor.getDictionaryTotalNumberOfPlacesPerCategory(pairwiseHungarianFeatureExtractor.getLanguage0(), category0);
                boolean statsValid0 = true;
                if (totalNumberOfPlacesPerCategory0 < 100) {
                    statsValid0 = false;
                }

                for (String category1 : p1.coreCategories) {
                    int totalNumberOfPlacesPerCategory1 = featureProcessor.getDictionaryTotalNumberOfPlacesPerCategory(pairwiseHungarianFeatureExtractor.getLanguage1(), category0);
                    boolean statsValid1 = true;
                    if (totalNumberOfPlacesPerCategory1 < 100) {
                        statsValid1 = false;
                    }

                    double normalizer0 = 0;
                    double normalizer1 = 0;

                    // compute normalizer from matching tokens and non-matching tokens
                    // first collect normalizer for matching tokens
                    for (StringPair matchPair : pairwiseHungarianFeatureExtractor.getMatchTokenPairs()) {
                        int tokenCount0 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage0(), category0, matchPair.getString0());
                        int tokenCount1 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage1(), category1, matchPair.getString0());

                        if (statsValid0) {
                            normalizer0 += Math.pow(Math.log(totalNumberOfPlacesPerCategory0 / tokenCount0), 2);
                        } else {
                            normalizer0 += 1;
                        }
                        if (statsValid1) {
                            normalizer1 += Math.pow(Math.log(totalNumberOfPlacesPerCategory1 / tokenCount1), 2);
                        } else {
                            normalizer1 += 1;
                        }
                        //System.out.println("idf debug " + matchPair.getString0() + " " + tokenCount0 + " " + totalNumberOfPlacesPerCategory0 + " " + matchPair.getString0() + " " + tokenCount1 + " " + totalNumberOfPlacesPerCategory1);

                    }

                    // now collect normalizer for non-matching tokens from name0
                    for (String nonMatchToken0 : pairwiseHungarianFeatureExtractor.getNonMatchingTokens0()) {
                        int tokenCount0 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage0(), category0, nonMatchToken0);

                        if (statsValid0) {
                            normalizer0 += Math.pow(Math.log(totalNumberOfPlacesPerCategory0 / tokenCount0), 2);
                        } else {
                            normalizer0 += 1;
                        }
                    }

                    // now collect normalizer for non-matching tokens from name1
                    for (String nonMatchToken1 : pairwiseHungarianFeatureExtractor.getNonMatchingTokens1()) {
                        int tokenCount1 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage1(), category1, nonMatchToken1);

                        if (statsValid1) {
                            normalizer1 += Math.pow(Math.log(totalNumberOfPlacesPerCategory1 / tokenCount1), 2);
                        } else {
                            normalizer1 += 1;
                        }


                    }

                    normalizer0 = 1/Math.sqrt(normalizer0);
                    normalizer1 = 1/Math.sqrt(normalizer1);

                    // normalizer is now complete

                    // compute cosine on IDF values
                    double cosineOnIDF = 0;
                    for (StringPair matchPair : pairwiseHungarianFeatureExtractor.getMatchTokenPairs()) {
                        int tokenCount0 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage0(), category0, matchPair.getString0());
                        int tokenCount1 = featureProcessor.getDictionaryCount(pairwiseHungarianFeatureExtractor.getLanguage1(), category1, matchPair.getString0());

                        double factor0, factor1;
                        if (statsValid0) {
                            factor0 = normalizer0*Math.log(totalNumberOfPlacesPerCategory0 / tokenCount0);
                        } else {
                            factor0 = normalizer0;
                        }
                        if (statsValid1) {
                            factor1 = normalizer1*Math.log(totalNumberOfPlacesPerCategory1 / tokenCount1);
                        } else {
                            factor1 = normalizer1;
                        }

                        cosineOnIDF+=factor0*factor1;
                        System.out.println("idf debug " + matchPair.getString0() + " " + tokenCount0 + " " + totalNumberOfPlacesPerCategory0 + " " + matchPair.getString0() + " " + tokenCount1 + " " + totalNumberOfPlacesPerCategory1 + " factor0:" + factor0 + " factor1: " + factor1 + " product: " + factor0*factor1);
                        //System.out.println(" " + matchPair.getString0() + " " + tokenCount0 + " " + totalNumberOfPlacesPerCategory0 + " " + matchPair.getString0() + " " + tokenCount1 + " " + totalNumberOfPlacesPerCategory1);

                    }

                    cosineOnIDF_max = Math.max(cosineOnIDF_max, cosineOnIDF);
                    //System.out.println("idf debug: " + pairwiseHungarianFeatureExtractor.getInputString0() + ":" +  pairwiseHungarianFeatureExtractor.getLanguage0() + ":" + category0 + " --- " + pairwiseHungarianFeatureExtractor.getInputString1() + ":" + pairwiseHungarianFeatureExtractor.getLanguage1()  + ":" + category1 + " --- " + cosineOnIDF + " " + cosineOnIDF_max);

                }
            }


            //  featureProcessor.getDictionaryTotalNumberOfPlacesPerCategory(pairwiseHungarianFeatureExtractors[i].get)
        }
    }
}

*/