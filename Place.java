
import java.util.*;


public class Place {

    public ByteArrayWrapper placeId, mergedWith;
    public String language_laf, name, normalizedName, normalizedNameOPR, geoHash, phone, fax, email, website, country, city, district, extension, region, street, house, provider;
    public double latitude, longitude;
    public HashSet<String> coreCategories, categories;
    public boolean merged = false;

    // multi language string sets
    public HashSet<String> nameSet, normalizedNameSet, citySet, districtSet, extensionSet, regionSet, streetSet, houseSet, streetCatHouseSet;


    public Place(double lat, double lon) {
        latitude = lat;
        longitude = lon;
    }

    public Place(String placeId) {
        this.placeId = new ByteArrayWrapper(placeId);
    }


    public boolean hasMultipleNames() {
        if (normalizedNameSet != null && normalizedNameSet.size() > 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasMultipleAddresses() {
        if ((citySet != null && citySet.size() > 1) || (districtSet != null && districtSet.size() > 1)
                || (extensionSet != null && extensionSet.size() > 1)
                || (regionSet != null && regionSet.size() > 1) || (streetSet != null && streetSet.size() > 1)
                || (houseSet != null && houseSet.size() > 1) || (streetCatHouseSet != null && streetCatHouseSet.size() > 1)) {
            return true;
        } else {
            return false;
        }
    }


    public void addName(String string) {
        if (string != null) {
            if (nameSet == null) {
                nameSet = new HashSet<String>();
            }
            nameSet.add(string);
        }
    }

    public void addNormalizedName(String language, String name) {
        if (name != null) {
            if (normalizedNameSet == null) {
                normalizedNameSet = new HashSet<String>();
            }
            if(language.indexOf("iata")>=0) {
               normalizedNameSet.add(language+"\t0123456789iata"+name);
            }
            else {
                normalizedNameSet.add(language+"\t"+name);
            }
        }
    }

    public void addCity(String string) {
        if (string != null) {
            if (citySet == null) {
                citySet = new HashSet<String>();
            }
            citySet.add(string);
        }
    }

    public void addDistrict(String string) {
        if (string != null) {
            if (districtSet == null) {
                districtSet = new HashSet<String>();
            }
            districtSet.add(string);
        }
    }

    public void addExtension(String string) {
        if (string != null) {
            if (extensionSet == null) {
                extensionSet = new HashSet<String>();
            }
            extensionSet.add(string);
        }
    }

    public void addRegion(String string) {
        if (string != null) {
            if (regionSet == null) {
                regionSet = new HashSet<String>();
            }
            regionSet.add(string);
        }
    }

    public void addStreet(String string) {
        if (string != null) {
            if (streetSet == null) {
                streetSet = new HashSet<String>();
            }
            streetSet.add(string);
        }
    }

    public void addHouse(String string) {
        if (string != null) {
            if (houseSet == null) {
                houseSet = new HashSet<String>();
            }
            houseSet.add(string);
        }
    }

    public void addStreetCatHouse(String string) {
        if (string != null) {
            if (streetCatHouseSet == null) {
                streetCatHouseSet = new HashSet<String>();
            }
            streetCatHouseSet.add(string);
        }
    }

    public void setMergedWith(String mergedWith) {
        if (mergedWith != null) {
            this.mergedWith = new ByteArrayWrapper(mergedWith);
        }
    }

    public boolean equalsPhone(Place p) {
        return phone != null && p.phone != null && phone.equals(p.phone);
    }


    public boolean equalsEmail(Place p) {
        return email != null && p.email != null && email.equals(p.email);
    }

    public boolean equalsEmailDomain(Place p) {
        if (email != null && p.email != null) {
            int index0 = email.indexOf('@');
            int index1 = p.email.indexOf('@');
            return index0 >= 0 && index1 >= 0 && email.substring(index0).equals(p.email.substring(index1));
        } else
            return false;
    }

    public boolean equalsWebsite(Place p) {
        String thisWebsiteNormalized = DQUtils.normalizeName(website);
        String otherWebsiteNormalized = DQUtils.normalizeName(p.website);
        return thisWebsiteNormalized != null && otherWebsiteNormalized != null && (thisWebsiteNormalized.equals(otherWebsiteNormalized) || thisWebsiteNormalized.startsWith(otherWebsiteNormalized) || otherWebsiteNormalized.startsWith(thisWebsiteNormalized));
    }

    public boolean notEqualsPhone(Place p) {
        return phone != null && p.phone != null && !phone.equals(p.phone);
    }


    public boolean notEqualsEmail(Place p) {
        return email != null && p.email != null && !email.equals(p.email);
    }

    public boolean notEqualsEmailDomain(Place p) {
        if (email != null && p.email != null) {
            int index0 = email.indexOf('@');
            int index1 = p.email.indexOf('@');
            return index0 >= 0 && index1 >= 0 && !email.substring(index0).equals(p.email.substring(index1));
        } else
            return false;
    }

    public boolean notEqualsWebsite(Place p) {
        return website != null && p.website != null && !(website.equals(p.website) || website.startsWith(p.website) || p.website.startsWith(website) || website.endsWith(p.website) || p.website.endsWith(website));
    }


    public boolean equalsStrongIdentifiers(Place p) {
        return equalsPhone(p) || equalsEmailDomain(p) || equalsWebsite(p);
    }

    public boolean notEqualsStrongIdentifiers(Place p) {
        //return notEqualsPhone(p) | notEqualsEmail(p) | notEqualsWebsite(p);
        return notEqualsEmailDomain(p);
    }

    public double distance(Place p) {
        return GeoHash.distance(latitude, p.latitude, longitude, p.longitude);
    }


    public String toStringOneLanguage() {
        StringBuilder result = new StringBuilder();
        result.append("placeId: " + placeId);
        result.append("\n mergedWith: " + mergedWith);
        result.append("\n language_laf: " + language_laf);
        result.append("\n name: " + name);
        result.append("\n normalizedName: " + normalizedName);
        result.append("\n normalizedNameOPR: " + normalizedNameOPR);
        result.append("\n coreCategories: " + coreCategories);
        result.append("\n categories: " + categories);
        result.append("\n latitude: " + latitude);
        result.append("\n longitude: " + longitude);
        result.append("\n geoHash: " + geoHash);
        result.append("\n phone: " + phone);
        result.append("\n fax: " + fax);
        result.append("\n email: " + email);
        result.append("\n website: " + website);
        result.append("\n country: " + country);
        result.append("\n city: " + city);
        result.append("\n district: " + district);
        result.append("\n extension: " + extension);
        result.append("\n region: " + region);
        result.append("\n street: " + street);
        result.append("\n house: " + house);
        result.append("\n merged: " + merged);
        result.append("\n provider: " + provider);

        return result.toString();
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("\n\n\nplaceId: " + placeId);
        result.append("\n mergedWith: " + mergedWith);
        result.append("\n language_laf: " + language_laf);
        result.append("\n name: " + nameSet);
        result.append("\n normalizedName: " + normalizedNameSet);
        result.append("\n normalizedNameOPR: " + normalizedNameOPR);
        result.append("\n coreCategories: " + coreCategories);
        result.append("\n categories: " + categories);
        result.append("\n latitude: " + latitude);
        result.append("\n longitude: " + longitude);
        result.append("\n geoHash: " + geoHash);
        result.append("\n phone: " + phone);
        result.append("\n fax: " + fax);
        result.append("\n email: " + email);
        result.append("\n website: " + website);
        result.append("\n country: " + country);
        result.append("\n city: " + citySet);
        result.append("\n district: " + districtSet);
        result.append("\n extension: " + extensionSet);
        result.append("\n region: " + regionSet);
        result.append("\n street: " + streetSet);
        result.append("\n house: " + houseSet);
        result.append("\n streetCatHouse: " + streetCatHouseSet);
        result.append("\n merged: " + merged);
        result.append("\n provider: " + provider);

        return result.toString();
    }


    public String toStringOneLanguage(Place p) {
        StringBuilder result = new StringBuilder();
        result.append("\nplaceId1: " + placeId);
        result.append("\nplaceId2: " + p.placeId);
        result.append("\nmergedWith1: " + mergedWith);
        result.append("\nmergedWith2: " + p.mergedWith);
        result.append("\nlanguage_laf1: " + language_laf);
        result.append("\nlanguage_laf2: " + p.language_laf);
        result.append("\nnormalizedName1: " + normalizedName);
        result.append("\nnormalizedName2: " + p.normalizedName);
        result.append("\nnormalizedNameOPR1: " + normalizedNameOPR);
        result.append("\nnormalizedNameOPR2: " + p.normalizedNameOPR);
        result.append("\nlatitude1: " + latitude);
        result.append("\nlatitude2: " + p.latitude);
        result.append("\nlongitude1: " + longitude);
        result.append("\nlongitude2: " + p.longitude);
        //      result.append("\ngeoHash1: " + geoHash);
        //      result.append("\ngeoHash2: " + p.geoHash);
        result.append("\nmerged1: " + merged);
        result.append("\nmerged2: " + p.merged);
        result.append("\nprovider1: " + provider);
        result.append("\nprovider2: " + p.provider);
        result.append("\ncountry1: " + country);
        result.append("\ncountry2: " + p.country);
        result.append("\ndistrict1: " + district);
        result.append("\ndistrict2: " + p.district);
        result.append("\nextension1: " + extension);
        result.append("\nextension2: " + p.extension);
        result.append("\nregion1: " + region);
        result.append("\nregion2: " + p.region);
        result.append("\ncity1: " + city);
        result.append("\ncity2: " + p.city);
        result.append("\nstreet1: " + street);
        result.append("\nstreet2: " + p.street);
        //if(street != null && p.street != null) 
        //result.append("\n" + DQMain.getLevenshteinDistance(street,p.street,1000) + " " + street.length() + " " + p.street.length());
        result.append("\nhouse1: " + house);
        result.append("\nhouse2: " + p.house);
        result.append("\nfax1: " + fax);
        result.append("\nfax2: " + p.fax);
        result.append("\nemail1: " + email);
        result.append("\nemail2: " + p.email);
        result.append("\nwebsite1: " + website);
        result.append("\nwebsite2: " + p.website);
        result.append("\ncoreCategories1: " + coreCategories);
        result.append("\ncoreCategories2: " + p.coreCategories);
        result.append("\ncategories1: " + categories);
        result.append("\ncategories2: " + p.categories);
        result.append("\nphone1: " + phone);
        result.append("\nphone2: " + p.phone);
        result.append("\nname1: " + name);
        result.append("\nname2: " + p.name);
        result.append("\ndistance: " + GeoHash.distance(latitude, p.latitude, longitude, p.longitude));
        return result.toString();
    }

   public String toString(Place p) {
        StringBuilder result = new StringBuilder();
        result.append("\nplaceId1: " + placeId);
        result.append("\nplaceId2: " + p.placeId);
        result.append("\nmergedWith1: " + mergedWith);
        result.append("\nmergedWith2: " + p.mergedWith);
        //result.append("\nlanguage_ln1: " + language_ln);
        //result.append("\nlanguage_ln2: " + p.language_ln);
        //result.append("\nlanguage_laf1: " + language_laf);
        //result.append("\nlanguage_laf2: " + p.language_laf);
        result.append("\nnormalizedName1: " + normalizedNameSet);
        result.append("\nnormalizedName2: " + p.normalizedNameSet);
       result.append("\nnormalizedNameSingle1: " + normalizedName);
        result.append("\nnormalizedNameSingle2: " + p.normalizedName);
        result.append("\nnormalizedNameOPR1: " + normalizedNameOPR);
        result.append("\nnormalizedNameOPR2: " + p.normalizedNameOPR);
        result.append("\nlatitude1: " + latitude);
        result.append("\nlatitude2: " + p.latitude);
        result.append("\nlongitude1: " + longitude);
        result.append("\nlongitude2: " + p.longitude);
        //      result.append("\ngeoHash1: " + geoHash);
        //      result.append("\ngeoHash2: " + p.geoHash);
        result.append("\nmerged1: " + merged);
        result.append("\nmerged2: " + p.merged);
        result.append("\nprovider1: " + provider);
        result.append("\nprovider2: " + p.provider);
        result.append("\ncountry1: " + country);
        result.append("\ncountry2: " + p.country);
        result.append("\ndistrict1: " + districtSet);
        result.append("\ndistrict2: " + p.districtSet);
        result.append("\nextension1: " + extensionSet);
        result.append("\nextension2: " + p.extensionSet);
        result.append("\nregion1: " + regionSet);
        result.append("\nregion2: " + p.regionSet);
        result.append("\ncity1: " + citySet);
        result.append("\ncity2: " + p.citySet);
        result.append("\nstreet1: " + streetSet);
        result.append("\nstreet2: " + p.streetSet);
        result.append("\nstreet1single: " + street);
        result.append("\nstreet2single: " + p.street);
        //if(street != null && p.street != null)
        //result.append("\n" + DQMain.getLevenshteinDistance(street,p.street,1000) + " " + street.length() + " " + p.street.length());
        result.append("\nhouse1: " + houseSet);
        result.append("\nhouse2: " + p.houseSet);
        result.append("\nstreetCatHouse1: " + streetCatHouseSet);
        result.append("\nstreetCatHouse2: " + p.streetCatHouseSet);
        result.append("\nfax1: " + fax);
        result.append("\nfax2: " + p.fax);
        result.append("\nemail1: " + email);
        result.append("\nemail2: " + p.email);
        result.append("\nwebsite1: " + website);
        result.append("\nwebsite2: " + p.website);
        result.append("\ncoreCategories1: " + coreCategories);
        result.append("\ncoreCategories2: " + p.coreCategories);
        result.append("\ncategories1: " + categories);
        result.append("\ncategories2: " + p.categories);
        result.append("\nphone1: " + phone);
        result.append("\nphone2: " + p.phone);
        result.append("\nname1: " + nameSet);
        result.append("\nname2: " + p.nameSet);
        result.append("\ndistance: " + GeoHash.distance(latitude, p.latitude, longitude, p.longitude));
        return result.toString();
    }

    public static String getStringBeforeTab(String languageTabName) {
        return languageTabName.substring(0,languageTabName.indexOf('\t'));
    }

    public static String getStringAfterTab(String languageTabName) {
            return languageTabName.substring(languageTabName.indexOf('\t')+1);
    }


}

