
import java.util.HashSet;

public class Feature implements Comparable {

    public int index;
    public double value;

    public Feature(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[");
        result.append(index);
        result.append(":");
        result.append(value);
        result.append("]");
        return result.toString();
    }

    public int compareTo(Object o1) {
        if (this.index == ((Feature) o1).index)
            return 0;
        else if (this.index > ((Feature) o1).index)
            return 1;
        else
            return -1;
    }

}

