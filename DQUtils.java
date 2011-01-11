

import java.util.StringTokenizer;

public class DQUtils {

    public static String sortConcatStringPairs(String s1, String s2) {
        if (s1.compareTo(s2) > 0)
            return s1 + "_" + s2;
        else
            return s2 + "_" + s1;
    }

    public static String sortConcatStringPairsBar(String s1, String s2) {
        if (s1.compareTo(s2) > 0)
            return s1 + "|" + s2;
        else
            return s2 + "|" + s1;
    }

    public static StringTokenizer normalizeNameTokenizerNoApostrophe(String name) {
        if (name != null) {
            StringTokenizer st = new StringTokenizer(name.toLowerCase(), "- !$%&*()_[]{}.,;:#\\/\"|+<>@=?~–@—\ufffd");
            return st;
        } else
            return null;
    }

    public static StringTokenizer normalizeNameTokenizerWithApostrophe(String name) {
        if (name != null) {
            StringTokenizer st = new StringTokenizer(name.toLowerCase(), "- !$%&*()_[]{}.,;:#\\/\"\'`’|+<>@=?~–@—\ufffd");
            return st;
        } else
            return null;
    }

    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        } else if (phone.charAt(0) == '0') {
            //System.out.println("normalize phone org: " + phone + " new: " + phone.substring(1));
            return phone.substring(1);
        } else {
            return phone;
        }
    }

    public static String normalizeName(String name) {
        StringTokenizer st = normalizeNameTokenizerNoApostrophe(name);
        if (st != null) {
            StringBuilder result = new StringBuilder();
            while (st.hasMoreTokens()) {
                if (result.length() > 0)
                    result.append(" ");

                String s = st.nextToken();
                int index1 = 0;
                int index2 = 0;
                int index_a = 0;
                int index_b = 0;
                int index_c = 0;
                // remove apostrophe
                while (s.indexOf('\'', index1) >= 0 || s.indexOf('`', index1) >= 0 || s.indexOf('’', index1) >= 0) {
                    index_a = s.indexOf('\'', index1);
                    index_b = s.indexOf('`', index1);
                    index_c = s.indexOf('’', index1);
                    if (index_a < 0)
                        index_a = Integer.MAX_VALUE;
                    if (index_b < 0)
                        index_b = Integer.MAX_VALUE;
                    if (index_c < 0)
                        index_c = Integer.MAX_VALUE;
                    
                    index2 = Math.min(Math.min(index_a, index_b), index_c);

                    // System.out.println("norm debug2: " + s + " " + index1 + " " + index2 + " " + index_a + " " + index_b);

                    // index2 = Math.min(Math.max(10000,s.indexOf('\'', index1)), Math.max(10000,s.indexOf('`', index1)));
                    //System.out.println("norm debug2: " + s + " " + index1 + " " + index2 + " " + s.substring(index1, index2) + " " + s.substring(index2));
                    result.append(s.substring(index1, index2));
                    index1 = index2 + 1;
                    index2++;
                }
                result.append(s.substring(index2));

            }


            //if(name.indexOf('`') >=0) {
            //    System.out.println("norm debug2: " + name + " " + result);
            //}

            if (result.length() == 0)
                return null;
            else
                return result.toString();
        } else
            return null;
    }

    public static String getTokenAndNormalize(StringTokenizer st, Place p, int mode) {
        if (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.equals("NULL")) {
                return null;
            } else if (mode == 0) {
                // no normalization
                return s;
            } else if (mode == 1) {
                // trim and lowercase
                String trimLower = s.trim().toLowerCase();
                if (trimLower.length() > 0)
                    return trimLower;
                else
                    return null;
            } else if (mode == 2) {
                // replace all sorts of characters with white space and remove apostrophe
                return normalizeName(s);
            }
        } else {
            System.out.println("----------unexpected end of columns Place " + p);
            System.exit(-1);
        }
        return null;
    }


}

