
import java.util.Set;
import java.util.StringTokenizer;


public class HungarianFeatureExtractor {

    boolean doTokenExactMatch = false;
    boolean doApplySingleSubstringFilter;

    boolean isHungarianMinMatch = false;
    boolean isAtLeastOnePerfectMatchToken = false;
    boolean isLeftOverToken = false;

    MatchInfo[] matchInfos;

    boolean conservativeConcatMode = false;

    String[] tokens0 = null;
    String[] tokens1 = null;

    int[][] assignmentSolution = null;

    int numTokens0 = 0;
    int numTokens1 = 0;
    int numNonWhiteChars0 = 0;
    int numNonWhiteChars1 = 0;

    Set<String> filterSet0 = null, filterSet1 = null;

    String inputString0, inputString1;

    FeatureProcessor featureProcessor = null;

    public HungarianFeatureExtractor(Place p0, Place p1, String inputString0, String inputString1, FeatureProcessor featureProcessor, boolean doTokenExactMatch, boolean conservativeConcatMode, boolean doApplySingleSubstringFilter) {
        this.featureProcessor = featureProcessor;
        this.inputString0 = inputString0;
        this.inputString1 = inputString1;
        this.doTokenExactMatch = doTokenExactMatch;
        this.conservativeConcatMode = conservativeConcatMode;
        this.doApplySingleSubstringFilter = doApplySingleSubstringFilter;

        if (isValidStrings(inputString0, inputString1)) {
            if (!isExactMatch()) {
                tokenizeStrings();
                runHungarianAlgorithm(p0, p1);
            }
        }
    }

    public HungarianFeatureExtractor(Place p0, Place p1, String inputString0, String inputString1, FeatureProcessor featureProcessor, Set<String> filterSet0, Set<String> filterSet1, boolean doTokenExactMatch, boolean conservativeConcatMode, boolean doApplySingleSubstringFilter) {
        this.featureProcessor = featureProcessor;
        this.inputString0 = inputString0;
        this.inputString1 = inputString1;
        this.doTokenExactMatch = doTokenExactMatch;
        this.conservativeConcatMode = conservativeConcatMode;
        this.doApplySingleSubstringFilter = doApplySingleSubstringFilter;

        this.filterSet0 = filterSet0;
        this.filterSet1 = filterSet1;

        if (isValidStrings(inputString0, inputString1)) {
            if (!isExactMatch()) {
                tokenizeStrings();
                runHungarianAlgorithm(p0, p1);
            }
        }
    }


    public HungarianFeatureExtractor(Place p0, Place p1, String inputString0, String inputString1, String[] tokens0, String[] tokens1, int numNonWhiteChars0, int numNonWhiteChars1, FeatureProcessor featureProcessor, boolean doTokenExactMatch, boolean conservativeConcatMode, boolean doApplySingleSubstringFilter) {
        this.featureProcessor = featureProcessor;
        this.inputString0 = inputString0;
        this.inputString1 = inputString1;
        this.doTokenExactMatch = doTokenExactMatch;
        this.tokens0 = tokens0;
        this.tokens1 = tokens1;
        this.numNonWhiteChars0 = numNonWhiteChars0;
        this.numNonWhiteChars1 = numNonWhiteChars1;
        this.numTokens0 = tokens0.length;
        this.numTokens1 = tokens1.length;
        this.doTokenExactMatch = doTokenExactMatch;
        this.conservativeConcatMode = conservativeConcatMode;
        this.doApplySingleSubstringFilter = doApplySingleSubstringFilter;


        if (isValidStrings(inputString0, inputString1)) {
            if (!isExactMatch()) {
                runHungarianAlgorithm(p0, p1);
            }
        }
    }

    boolean isExactMatch() {
        if (inputString0.equals(inputString1)) {
            isHungarianMinMatch = true;
            isAtLeastOnePerfectMatchToken = true;
            return true;
        } else {
            return false;
        }
    }

    boolean isValidStrings(String inputString0, String inputString1) {
        return inputString0 != null && inputString1 != null && inputString0.trim().length() > 0 && inputString1.trim().length() > 0;
    }

    void tokenizeStrings() {
        StringTokenizer st0 = new StringTokenizer(inputString0, " ");
        numTokens0 = st0.countTokens();
        tokens0 = new String[numTokens0];
        int i = 0;
        while (st0.hasMoreTokens()) {
            tokens0[i] = st0.nextToken();
            numNonWhiteChars0 += tokens0[i].length();
            i++;
        }

        StringTokenizer st1 = new StringTokenizer(inputString1, " ");
        numTokens1 = st1.countTokens();
        tokens1 = new String[numTokens1];
        int j = 0;
        while (st1.hasMoreTokens()) {
            tokens1[j] = st1.nextToken();
            numNonWhiteChars1 += tokens1[j].length();
            j++;
        }
    }

    public boolean isHungarianMinMatch() {
        return isHungarianMinMatch;
    }

    public boolean isAtLeastOnePerfectMatchToken() {
        return isAtLeastOnePerfectMatchToken;
    }

    public String[] getTokens0() {
        return tokens0;
    }

    public String[] getTokens1() {
        return tokens1;
    }

    public String getInputString0() {
        return inputString0;
    }

    public String getInputString1() {
        return inputString1;
    }

    public int getNumNonWhiteChars0() {
        return numNonWhiteChars0;
    }

    public int getNumNonWhiteChars1() {
        return numNonWhiteChars1;
    }

    public boolean isLeftOverToken() {
        return isLeftOverToken;
    }

    public void runHungarianAlgorithm(Place p0, Place p1) {

        int matrixSize = Math.max(numTokens0, numTokens1);

        int i, j;
        float[][] matrix = new float[matrixSize][];
        for (i = 0; i < matrixSize; i++) {
            matrix[i] = new float[matrixSize];
        }
//                System.out.println("matrix: " + matrix[0][0] + " "+ matrix[1][1]);


        // TODO: levenstein fuer anderes tokens von token paaren die perfekt matchen, nicht mehr ausrechnen zum zeit sparen

        for (i = 0; i < numTokens0; i++) {
            for (j = 0; j < numTokens1; j++) {
                if (tokens0[i].equals(tokens1[j])) {
                    matrix[i][j] = -1000;
                    featureProcessor.debugOutputln("edit dist token1 equal: " + tokens0[i] + " token2: " + tokens1[j] + " value: " + "0");

                } else {
                    float levenshteinDistance = (float) getLevenshteinDistance(tokens0[i], tokens1[j], 1000, null);
                    featureProcessor.debugOutputln("edit dist token1a: " + tokens0[i] + " token2: " + tokens1[j] + " value: " + levenshteinDistance);

                    if (tokens0[i].startsWith(tokens1[j]) || tokens1[j].startsWith(tokens0[i])) {
                        levenshteinDistance -= 0.001;
                    }
                    featureProcessor.debugOutputln("edit dist token1b: " + tokens0[i] + " token2: " + tokens1[j] + " value: " + levenshteinDistance);

                    matrix[i][j] = levenshteinDistance;
                }
            }
        }

        if (numTokens0 > numTokens1) {
            for (i = numTokens1; i < numTokens0; i++) {
                for (int i2 = 0; i2 < numTokens0; i2++) {
                    matrix[i2][i] = 1000;
                }
            }
        } else if (numTokens0 < numTokens1) {
            for (i = numTokens0; i < numTokens1; i++) {
                for (int i2 = 0; i2 < numTokens1; i2++) {
                    matrix[i][i2] = 1000;
                }
            }
        }

        /*
        featureProcessor.debugOutputln("matrix i vertical, j horizontal:\n");
        for (i = 0; i < matrixSize; i++) {
            for (j = 0; j < matrixSize; j++) {
                featureProcessor.debugOutput("\t" + matrix[i][j]);
            }
            featureProcessor.debugOutputln("");

        }
        */


        AssignmentProblem assignmentProblem = new AssignmentProblem(matrix);
        assignmentSolution = assignmentProblem.solve(new HungarianAlgorithm());

        /*
        System.out.print("\nassignmentSolution:\n");
        for (int placeIndex = 0; placeIndex < 2; placeIndex++) {
            for (int tokenIndex = 0; tokenIndex < matrixSize; tokenIndex++) {
                System.out.print(" " + assignmentSolution[tokenIndex][placeIndex]);
            }
            System.out.println();
        }
        */


        matchInfos = new MatchInfo[matrixSize];

        for (i = 0; i < matrixSize; i++) {
            // System.out.println("hungarian debug4: " + inputString0 + "|"+ inputString1);
            if (assignmentSolution[i][0] < numTokens0 && assignmentSolution[i][1] < numTokens1) {
                float editdistance = Math.round(matrix[assignmentSolution[i][0]][assignmentSolution[i][1]]);
                matchInfos[i] = new MatchInfo(tokens0[assignmentSolution[i][0]], tokens1[assignmentSolution[i][1]], editdistance <= 0, editdistance);
            } else if (assignmentSolution[i][0] < numTokens0) {
                matchInfos[i] = new MatchInfo(tokens0[assignmentSolution[i][0]], true);

            } else if (assignmentSolution[i][1] < numTokens1) {
                matchInfos[i] = new MatchInfo(tokens1[assignmentSolution[i][1]], false);
            }
        }


        int numMismatchTokens0 = 0;
        int numMismatchTokens1 = 0;
        isAtLeastOnePerfectMatchToken = false;
        boolean hasSingleCharMatch = false;
        boolean hasNonSingleCharMatch = false;
        boolean hasNonStartsWithOther = false;


        for (MatchInfo matchInfo : matchInfos) {
            if (matchInfo.isExactMatch()) {
                hasNonSingleCharMatch = true;
                hasNonStartsWithOther = true;
                isAtLeastOnePerfectMatchToken = true;
            } else if (!matchInfo.isMatch()) {
                if (matchInfo.isNonMatchToken0()) {
                    numMismatchTokens0++;
                    if (filterSet0 != null && filterSet0 != null) {
                        if (filterSet0.contains(matchInfo.getString())) {
                            numMismatchTokens0--;
                        }
                    }
                } else {
                    numMismatchTokens1++;
                    if (filterSet0 != null && filterSet0 != null) {
                        if (filterSet1.contains(matchInfo.getString())) {
                            numMismatchTokens1--;
                        }
                    }
                }
            } else {
                if (doTokenExactMatch) {
                    numMismatchTokens0++;
                    numMismatchTokens1++;
                    if (filterSet0 != null && filterSet0 != null) {
                        if (filterSet0.contains(matchInfo.getString0())) {
                            numMismatchTokens0--;
                        }
                        if (filterSet1.contains(matchInfo.getString1())) {
                            numMismatchTokens1--;
                        }
                    }
                } else if (conservativeConcatMode) {
                    if (matchInfo.isConservativeMatchForConcatMode()) {
                        hasSingleCharMatch |= matchInfo.isSingleCharAbbreviationMatch();
                        hasNonSingleCharMatch |= !matchInfo.isSingleCharAbbreviationMatch();
                        hasNonStartsWithOther |= !matchInfo.isStartsWithOther();
                    } else {
                        numMismatchTokens0++;
                        numMismatchTokens1++;
                        if (filterSet0 != null && filterSet0 != null) {
                            if (filterSet0.contains(matchInfo.getString0())) {
                                numMismatchTokens0--;
                            }
                            if (filterSet1.contains(matchInfo.getString1())) {
                                numMismatchTokens1--;
                            }
                        }
                    }
                } else {
                    if (matchInfo.isRelaxedMatch()) {
                        hasSingleCharMatch |= matchInfo.isSingleCharAbbreviationMatch();
                        hasNonSingleCharMatch |= !matchInfo.isSingleCharAbbreviationMatch();
                        hasNonStartsWithOther |= !matchInfo.isStartsWithOther();
                    } else if (matchInfo.isMatch()) {
                        numMismatchTokens0++;
                        numMismatchTokens1++;
                        if (filterSet0 != null && filterSet0 != null) {
                            if (filterSet0.contains(matchInfo.getString0())) {
                                numMismatchTokens0--;
                            }
                            if (filterSet1.contains(matchInfo.getString1())) {
                                numMismatchTokens1--;
                            }
                        }
                    }
                }
            }
        }

        // if there are token matches that have a single char on one side then this gives only
        // an overall match if there are no leftover tokens
        if (hasSingleCharMatch && (numMismatchTokens0 > 0 || numMismatchTokens1 > 0)) {
            return;
        }

        // if there are ONLY abbreviation matches (e.g., "a" to "abcd") then do not accept overall match
        if (!hasNonSingleCharMatch) {
            return;
        }

        if (doApplySingleSubstringFilter && !hasNonStartsWithOther) {
            return;
        }


        // leftover tokens are only allowed in one of the strings
        if (Math.min(numMismatchTokens0, numMismatchTokens1) > 0) {
            return;
        }


        /*
        wenn alle tokens von beiden address tokens sind, dann match ja?
        city,street, extension getrennt checken?
        nur komplette namens matches erlauben?
        fuzzy matches?
        extension ist nicht gut, weil da komplette placenamen drin stehen.
        ueber kreuz matchen?
        */

        if (filterSet0 != null && filterSet0 != null) {
            boolean hasNonAddressToken0 = false;
            for(String token:tokens0) {
               // if (!filterSet0.contains(token)) {
               if (!(filterSet0.contains(token) || filterSet1.contains(token))) {
                   hasNonAddressToken0 = true;
                   break;
               }
            }
            boolean hasNonAddressToken1 = false;
            for(String token:tokens1) {
               //  if (!filterSet1.contains(token)) {
               if (!(filterSet0.contains(token) || filterSet1.contains(token))) {
                   hasNonAddressToken1 = true;
                   break;
               }
            }
            if(!hasNonAddressToken0 || !hasNonAddressToken1) {
                return;
            }
        }



        isHungarianMinMatch = true;
        if (numMismatchTokens0 > 0 || numMismatchTokens1 > 0) {
            isLeftOverToken = true;
        }

    }


    public static int getLevenshteinDistance(String s, String t, int replacementCost, int[] pathDetails) {
        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }


        /*
          The difference between this impl. and the previous is that, rather
          than creating and retaining a matrix of size s.length()+1 by t.length()+1,
          we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
          is the 'current working' distance array that maintains the newest distance cost
          counts as we iterate through the characters of String s.  Each time we increment
          the index of String t we are comparing, d is copied to p, the second int[].  Doing so
          allows us to retain the previous cost counts as required by the algorithm (taking
          the minimum of the cost count to the left, up one, and diagonally up and to the left
          of the current cost count being calculated).  (Note that the arrays aren't really
          copied anymore, just switched...this is clearly much better than cloning an array
          or doing a System.arraycopy() each time  through the outer loop.)

          Effectively, the difference between the two implementations is this one does not
          cause an out of memory condition when calculating the LD over two very large strings.
        */

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        if (n > m) {
            // swap the input strings to consume less memory
            String tmp = s;
            s = t;
            t = tmp;
            n = m;
            m = t.length();
        }

        int[][] trackingMatrix = new int[m + 1][n + 1];


        int p[] = new int[n + 1]; //'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
            trackingMatrix[0][i] = i;
        }

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;
            trackingMatrix[j][0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : replacementCost;
                trackingMatrix[j][i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }
            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        if (pathDetails != null) {


            /*
   for (j = 0; j <= m; j++) {
       for (i = 0; i <= n; i++) {
           System.out.print(" " + trackingMatrix[j][i]);
       }
       System.out.println();
   }
           String path = "";
            */

            int numReplace = 0;
            int numInsert = 0;
            int numEditBatches = 0;
            int numBatchesAtTokenBorder = 0;
            j = m;
            i = n;
            boolean lastWasEdit = false;
            while (j > 0 || i > 0) {
                //System.out.println("levensthein debug: " + i + " " + j + " " + path);
                if (i > 0 && trackingMatrix[j][i] - 1 == trackingMatrix[j][i - 1]) {
                    if (!lastWasEdit) {
                        numEditBatches++;
                    }
                    lastWasEdit = true;
                    numInsert++;
                    if ((i == n && j == m) || i == 1) {
                        numBatchesAtTokenBorder++;
                    }
                    //path = "i" + path;
                    i--;
                } else if (j > 0 && trackingMatrix[j][i] - 1 == trackingMatrix[j - 1][i]) {
                    if (!lastWasEdit) {
                        numEditBatches++;
                    }
                    lastWasEdit = true;
                    numInsert++;
                    if ((i == n && j == m) || j == 1) {
                        numBatchesAtTokenBorder++;
                    }
                    //path = "i" + path;
                    j--;
                } else if (trackingMatrix[j][i] == trackingMatrix[j - 1][i - 1]) {
                    lastWasEdit = false;
                    //path = "0" + path;
                    j--;
                    i--;
                } else {
                    if (!lastWasEdit) {
                        numEditBatches++;
                    }
                    lastWasEdit = true;
                    //path = "r" + path;
                    numReplace++;
                    if ((i == n && j == m) || (i == 1 && j == 1)) {
                        numBatchesAtTokenBorder++;
                    }
                    j--;
                    i--;
                }

                /*

                if (trackingMatrix[j - 1][i] < trackingMatrix[j][i - 1]) {
                    if (trackingMatrix[j - 1][i]+1 < trackingMatrix[j - 1][i - 1]) {
                        path = "i" + path;
                        j--;
                    } else {

                        if (trackingMatrix[j - 1][i - 1] < trackingMatrix[j][i]) {
                            path = "r" + path;
                        } else {
                            path = "0" + path;
                        }
                        j--;
                        i--;
                    }
                } else {
                    if (trackingMatrix[j][i - 1]+1 < trackingMatrix[j - 1][i - 1]) {
                        path = "i" + path;
                        i--;
                    } else {

                        if (trackingMatrix[j - 1][i - 1] < trackingMatrix[j][i]) {
                            path = "r" + path;
                        } else {
                            path = "0" + path;
                        }
                        j--;
                        i--;
                    }
                }
                */

            }
            //System.out.println("levensthein debug: " + i + " " + j + " " + path);

            /*
            if (numEditBatches == 1 && numReplace == 1 && numInsert < 2) {
                System.out.println("levensthein debug: " + s);
                System.out.println("levensthein debug: " + t + "\n");

                System.out.println("levensthein debug: " + path + " " + numReplace + " " + numEditBatches + " " + numInsert + "\n");
                System.out.println("\n" + p[n] + "\n\n");
            }
            */

            /*
          if(!(path.charAt(0)=='0')) {
              if(numBatchesAtTokenBorder==0) {
                  System.out.println("numBatchesAtTokenBorder: " + numBatchesAtTokenBorder);

              }
          }
            */

            /*
            if(numEditBatches==1) {
         //      System.out.println("numBatchesAtTokenBorder: " + numBatchesAtTokenBorder);
            }
            */

            pathDetails[0] = numEditBatches;
            pathDetails[1] = numReplace;
            pathDetails[2] = numInsert;
            pathDetails[3] = numBatchesAtTokenBorder;

        }
        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }


}

