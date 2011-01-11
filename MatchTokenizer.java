

public class MatchTokenizer {

    String s1, s2;
    int len1, len2;
    double maxScore = Integer.MIN_VALUE;

    // mapping array stores for each char in s1 corresponding char in s2
    int[] maxScoreMap;

    public MatchTokenizer(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
        len1 = s1.length();
        len2 = s2.length();
        tokenize();
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("\ns1: " + s1 + "\n");
        result.append("s2: " + s2 + "\n");
        result.append("score: " + maxScore + "\n");

        if (maxScoreMap != null) {
            for (int i = 0; i < len1; i++) {
                result.append("\n map: " + i + " to " + maxScoreMap[i]);
                if (maxScoreMap[i] != -1) {
                    result.append(" --- chars: " + s1.charAt(i) + " to " + s2.charAt(maxScoreMap[i]));
                } else {
                    result.append(" --- chars: " + s1.charAt(i));
                }
            }
        }
        return result.toString();
    }

    public int[] tokenize() {
        int[] map = getEmptyMap();
        maxScoreMap = map;
        tokenize(0, map);
        return maxScoreMap;
    }

    public void tokenize(int start, int[] map) {
        //System.out.println("tokenize start:" + start);
        if (start < len1) {
            System.out.println("  tokenize start:" + start + " " + s1.charAt(start));
            char char1 = s1.charAt(start);
            for (int i = 0; i < len2; i++) {
                if (char1 == s2.charAt(i) && getReverseMapping(map, i) == -1) {
                    int[] cloneMap = cloneMap(map);
                    cloneMap[start] = i;
                    double score = evaluateScore(cloneMap);
                    if (score > maxScore) {
                        maxScoreMap = cloneMap;
                        maxScore = score;
                    }
                    tokenize(start + 1, cloneMap);
                }
            }
            tokenize(start + 1, map);
        }
    }

    /*
    fill entire map with non-mapping indicator (-1)
     */
    int[] getEmptyMap() {
        int[] map = new int[len1];
        for (int i = 0; i < len1; i++) {
            map[i] = -1;
        }
        return map;
    }

    /*
     get deep copy of map
     */
    int[] cloneMap(int[] map) {
        int[] cloneMap = new int[len1];
        System.arraycopy(map, 0, cloneMap, 0, len1);
        return cloneMap;
    }


    int getReverseMapping(int[] map, int index2) {
        for (int i = 0; i < len1; i++) {
            if (map[i] == index2) {
                return i;
            }
        }
        return -1;
    }

    double evaluateScore(int[] map) {
        // compute reversed map
        int[] map2 = new int[len2];
        for (int i = 0; i < len2; i++) {
            map2[i] = -1;
        }
        for (int i = 0; i < len1; i++) {
            if (map[i] != -1) {
                map2[map[i]] = i;
            }
        }

        // score from s1 perspective
        double score = 0;
        for (int i = 0; i < len1; i++) {
            if (map[i] != -1) {
                // each char match gets a score of +1
                score++;
                if (i + 1 < len1 && map[i + 1] != -1) {
                    // each pair of neighboring char matches in string 1 gets a score of +1
                    score = score++;
                    if (map[i] + 1 == map[i + 1]) {
                        // if the neighboring pair is also a neighboring pair in string 2 this gives an additional score of +1
                        score++;
                    }
                }

                // penalize cross mappings
                for (int j = 0; j < i; j++) {
                    if (map[i] < map[j]) {
                        score = score - 0.01;
                    }
                }
            }
        }

        // score from s2 perspective
        for (int i = 0; i < len2; i++) {
            if (map2[i] != -1) {
                score++;
                if (i + 1 < len2 && map2[i + 1] != -1) {
                    score = score++;
                    if (map2[i] + 1 == map2[i + 1]) {
                        score++;
                    }
                }
            }
        }

        return score;
    }


}

