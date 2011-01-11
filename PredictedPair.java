import java.util.*;

public class PredictedPair implements Comparable {

    public double predictionScore;
    public ByteArrayWrapper placeId1, placeId2;
    public FeatureVector featureVector;
    public Place place1, place2;
    public int label;

    public PredictedPair(Place place1, Place place2, FeatureVector featureVector, int label) {
        this(-1, place1, place2, featureVector, label);
    }
    
    public PredictedPair(double predictionScore, Place place1, Place place2, FeatureVector featureVector, int label) {
        this.predictionScore = predictionScore;

        ByteArrayWrapper placeId1 = place1.placeId;
        ByteArrayWrapper placeId2 = place2.placeId;

        if(placeId1.compareTo(placeId2) >= 0) {
            this.placeId1 = placeId1;
            this.placeId2 = placeId2;
            this.place1 = place1;
            this.place2 = place2;
        }
        else {
            this.placeId1 = placeId2;
            this.placeId2 = placeId1;
            this.place1 = place2;
            this.place2 = place1;
        }
        this.featureVector = featureVector;
        this.label = label;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(predictionScore);
        result.append("[");
        result.append(placeId1);
        result.append("|");
        result.append(placeId2);
        result.append("]");
        return result.toString();
    }

    public int compareTo(Object o1) {
        if (this.predictionScore == ((PredictedPair) o1).predictionScore && placeId1.equals(((PredictedPair) o1).placeId1) && placeId2.equals(((PredictedPair) o1).placeId2))
            return 0;
        else if (this.predictionScore > ((PredictedPair) o1).predictionScore)
            return +1;
        else
            return -1;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof ByteArrayWrapper))
        {
            return false;
        }
        return placeId1.equals(((PredictedPair) other).placeId1) && placeId2.equals(((PredictedPair) other).placeId2);
    }

}

