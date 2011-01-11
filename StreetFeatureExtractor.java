
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;


public class StreetFeatureExtractor {


    boolean houseMatch = false;
    boolean houseMismatch = false;
    boolean houseNull;

    boolean streetNull1 = false;
    boolean streetNull2 = false;
    boolean streetMatch = false;
    boolean streetMismatch = false;

    Set<String[]> streetCatHouseTokensSet1, streetCatHouseTokensSet2;


    public StreetFeatureExtractor(Place p1, Place p2, FeatureVector featureVector, int label, FeatureProcessor featureProcessor, Set<String> street1Set, Set<String> street2Set, Set<String> house1Set, Set<String> house2Set, Set<String> streetCatHouse1Set, Set<String> streetCatHouse2Set) {
        //System.out.println(" street feature extractor ");


        if (house1Set != null && house2Set != null) {
            houseNull = false;
            OUTERMOST_HOUSE:
            for (String house1 : house1Set) {
                String[] houseTokens1 = house1.split(" ");
                for (String houseToken1 : houseTokens1) {
                    for (String house2 : house2Set) {
                        String[] houseTokens2 = house2.split(" ");
                        for (String houseToken2 : houseTokens2) {
                            if (houseToken1.equals(houseToken2)) {
                                houseMatch = true;
                                break OUTERMOST_HOUSE;
                            }
                        }
                    }
                }
            }
            if (!houseMatch) {
                houseMismatch = true;
            }
        } else {
            houseNull = true;
        }


        if (street1Set != null && street2Set != null) {
            streetNull1 = false;
            OUTERMOST_STREET:
            for (String street1 : street1Set) {
                for (String street2 : street2Set) {
                    if (street1.equals(street2)) {
                        streetMatch = true;
                        break OUTERMOST_STREET;
                    }
                }
            }
        } else if (street1Set != null || street2Set != null) {
            streetNull1 = true;
        } else {
            streetNull2 = true;
        }

        if (!streetNull1 && !streetNull2 && !streetMatch) {
            if (houseMatch || houseMismatch) {
                // hungarian matching only on street because with house match or house mismatch I assume that street is not polluted with house numbers
                OUTERMOST_STREET:
                for (String street1 : street1Set) {
                    for (String street2 : street2Set) {
                        HungarianFeatureExtractor hungarianFeatureExtractor = new HungarianFeatureExtractor(p1, p2, street1, street2, featureProcessor, false, false, false);
                        if (hungarianFeatureExtractor.isHungarianMinMatch()) {
                            streetMatch = true;
                            break OUTERMOST_STREET;
                        }
                    }
                }
                if (!streetMatch) {
                    streetMismatch = true;
                }
            } else {
                // hungarian matching on street concatenated with house
                tokenize(streetCatHouse1Set, streetCatHouse2Set);

                OUTERMOST_STREET_HOUSE:
                for (String[] streetCatHouseTokens1 : streetCatHouseTokensSet1) {
                    for (String[] streetCatHouseTokens2 : streetCatHouseTokensSet2) {
                        HungarianFeatureExtractor hungarianFeatureExtractor = new HungarianFeatureExtractor(p1, p2, getNonDigitTokensConcatenated(streetCatHouseTokens1), getNonDigitTokensConcatenated(streetCatHouseTokens2), featureProcessor, false, false, false);
                        if (hungarianFeatureExtractor.isHungarianMinMatch()) {
                            streetMatch = true;
                            break OUTERMOST_STREET_HOUSE;
                        }
                    }
                }
                if (!streetMatch) {
                    streetMismatch = true;
                }
            }
        }


        if (streetMatch) {
            if (houseNull) {
                if (streetCatHouseTokensSet1 == null) {
                    tokenize(streetCatHouse1Set, streetCatHouse2Set);
                }

                boolean foundDigitsInBoth = false;
                OUTERMOST_HOUSE:
                for (String[] streetCatHouseTokens1 : streetCatHouseTokensSet1) {
                    HashSet<String> streetCatHouseSet1 = getDigitTokenSet(streetCatHouseTokens1);
                    if (streetCatHouseSet1 != null) {
                        for (String streetCatHouse1 : streetCatHouseSet1) {
                            if (streetCatHouse1.length() > 0) {
                                for (String[] streetCatHouseTokens2 : streetCatHouseTokensSet2) {
                                    HashSet<String> streetCatHouseSet2 = getDigitTokenSet(streetCatHouseTokens2);
                                    if (streetCatHouseSet2 != null) {
                                        for (String streetCatHouse2 : streetCatHouseSet2) {
                                            if (streetCatHouse2.length() > 0) {
                                                foundDigitsInBoth = true;
                                                if (streetCatHouse1.equals(streetCatHouse2)) {
                                                    houseMatch = true;
                                                    break OUTERMOST_HOUSE;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!houseMatch && foundDigitsInBoth) {
                    houseMismatch = true;
                }
            }
        } else if (street1Set != null && street2Set != null) {
            // if street does not match then house number match could be from wrong street, it is safer to set houseMatch=false
            houseMatch = false;
        }

    }


    public boolean isHouseMatch() {
        return houseMatch;
    }

    public boolean isHouseMismatch() {
        return houseMismatch;
    }

    public boolean isStreetMatch() {
        return streetMatch;
    }

    public boolean isStreetMismatch() {
        return streetMismatch;
    }

    public boolean isHouseNull() {
        return houseNull;
    }

    public boolean isStreetNull1() {
        return streetNull1;
    }

    public boolean isStreetNull2() {
        return streetNull2;
    }


    public static StringTokenizer streetTokenizer(String s) {
        if (s != null) {
            s = DQUtils.normalizeName(s);
            if (s != null)
                return new StringTokenizer(DQUtils.normalizeName(s), " ");
        }
        return null;
    }

    public static String getNonDigitTokensConcatenated(String[] stringArray) {
        if (stringArray.length == 1) {
            // if street has only one token than this cannot be a house number
            if (stringArray[0].length() > 0)
                return stringArray[0];
            else
                return null;
        } else {
            StringBuilder result = new StringBuilder();
            StringBuilder resultComplete = new StringBuilder();
            boolean hasNonDigitToken = false;
            for (String token : stringArray) {
                if (token.length() > 0) {
                    if (resultComplete.length() > 0) {
                        resultComplete.append(" ");
                    }
                    resultComplete.append(token);
                    if (!token.matches(".*\\d+.*")) {
                        if (result.length() > 0) {
                            result.append(" ");
                        }
                        result.append(token);
                        hasNonDigitToken = true;
                    }
                }
            }
            if (hasNonDigitToken) {
                if (result.length() > 0)
                    return result.toString();
                else
                    return null;
            } else {
                // if there is no token without digits then do not filter out digit tokens
                if (resultComplete.length() > 0)
                    return resultComplete.toString();
                else
                    return null;
            }
        }
    }

    public static HashSet<String> getDigitTokenSet(String[] stringArray) {
        if (stringArray.length == 1) {
            // if street has only one token than this cannot be a house number
            return null;
        } else {
            HashSet<String> result = new HashSet<String>();
            for (String token : stringArray) {
                if (token.length() > 0 && token.matches(".*\\d+.*")) {
                    result.add(token);
                }
            }
            if (result.size() > 0)
                return result;
            else
                return null;
        }
    }


    private void tokenize(Set<String> streetCatHouse1Set, Set<String> streetCatHouse2Set) {
        streetCatHouseTokensSet1 = new HashSet<String[]>();
        for (String streetCatHouse1 : streetCatHouse1Set) {
            streetCatHouseTokensSet1.add(streetCatHouse1.split(" "));
        }

        streetCatHouseTokensSet2 = new HashSet<String[]>();
        for (String streetCatHouse2 : streetCatHouse2Set) {
            streetCatHouseTokensSet2.add(streetCatHouse2.split(" "));
        }
    }
}

