public class MatchInfo {

    String string0, string1;
    boolean isMatch;
    boolean isExactMatch;

    // if this is a leftover token then the following boolean is true if the token
    // comes from string 0 and false if the token comes from string 1
    boolean isNonMatchToken0;


    boolean isAtLeastOneDigitToken;
    boolean isBothSingleChar;
    boolean isFirstCharMatch;
    boolean isSingleCharAbbreviationMatch;
    boolean isStartsWithOther;
    boolean isAtLeastOneSingleChar;
    int[] levenshteinDetails;
    boolean isSimpleTypoEditDistanceMatch;
    boolean isRelaxedMatch;
    double levenshteinWithReplace;


    public MatchInfo(String string0, String string1, boolean isExactMatch, double editDistanceWithoutReplace) {
        this.string0 = string0;
        this.string1 = string1;
        this.isMatch = true;
        this.isExactMatch = isExactMatch;

        if (!isExactMatch) {
            isAtLeastOneDigitToken = string0.matches(".*\\d+.*") || string1.matches(".*\\d+.*");
            isBothSingleChar = string0.length() == string1.length() && string0.length() == 1;
            isFirstCharMatch = string0.charAt(0) == string1.charAt(0);
            isAtLeastOneSingleChar = string0.length() == 1 || string1.length() == 1;
            isStartsWithOther = string0.startsWith(string1) || string1.startsWith(string0);
            isSingleCharAbbreviationMatch = !isBothSingleChar && isAtLeastOneSingleChar && isStartsWithOther;

            levenshteinDetails = new int[4];
            levenshteinWithReplace = HungarianFeatureExtractor.getLevenshteinDistance(string0, string1, 1, levenshteinDetails);
            int numEditBatches = levenshteinDetails[0];
            int numReplace = levenshteinDetails[1];
            int numInsert = levenshteinDetails[2];
            int numBatchesAtTokenBorder = levenshteinDetails[3];
            isSimpleTypoEditDistanceMatch = levenshteinWithReplace <= 2.0 && numEditBatches <= 1 && numReplace <= 1 && numInsert <= 1;

            isRelaxedMatch = false;
            if (editDistanceWithoutReplace < (string0.length() + string1.length()) / 2.0) {
                if (numEditBatches <= 1) {
                    if ((numReplace <= 1 && numInsert <= 1) || numReplace == 0) {
                        isRelaxedMatch = true;
                    }
                }
            }
        }
    }

    // leftOver token
    public MatchInfo(String string, boolean isNonMatchToken0) {
        this.isNonMatchToken0 = isNonMatchToken0;
        this.string0 = string;
        this.isMatch = false;
        this.isExactMatch = false;
    }

    public boolean isExactMatch() {
        return isExactMatch;
    }

    public boolean isConservativeMatchForConcatMode() {
        if (isExactMatch) {
            return true;
        } else if (!isAtLeastOneDigitToken && !isBothSingleChar && isFirstCharMatch) {
            if (!isAtLeastOneSingleChar) {
                if(isSimpleTypoEditDistanceMatch) {
                    if (Math.min(string0.length(), string1.length()) - Math.max(string0.length(), string1.length()) + levenshteinWithReplace <= 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isRelaxedMatch() {
        if (isExactMatch) {
            return true;
        } else if (!isAtLeastOneDigitToken && !isBothSingleChar && isFirstCharMatch) {
            if (isSingleCharAbbreviationMatch
                    || (!isAtLeastOneSingleChar && isStartsWithOther)
                    || (!isAtLeastOneSingleChar && isRelaxedMatch)) {
                    if (Math.min(string0.length(), string1.length()) - Math.max(string0.length(), string1.length()) + levenshteinWithReplace <= 1) {
                        return true;
                    }
            }
        }
        return false;
    }

    public String getString0() {
        return string0;
    }

    // if only one mismatch token is stored in object then use this getter
    public String getString() {
         return string0;
     }

    public String getString1() {
        return string1;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public boolean isNonMatchToken0() {
        return isNonMatchToken0;
    }

    public double getLevenshteinWithReplace() {
        return levenshteinWithReplace;
    }

    public boolean isSingleCharAbbreviationMatch() {
        return isSingleCharAbbreviationMatch;
    }

    public boolean isStartsWithOther() {
        return isStartsWithOther;
    }
}
