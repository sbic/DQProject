import java.util.*;


public final class ByteArrayWrapper {
    private byte[] data;
    private int hashCode;

    public ByteArrayWrapper(byte[] data) {
        if (data == null) {
            throw new NullPointerException();
        }
        this.data = data;
        hashCode = Arrays.hashCode(this.data);
    }

    public ByteArrayWrapper() {
        this.data = null;
        hashCode = 0;
    }
    

    public ByteArrayWrapper(String data) {
        if (data == null) {
            throw new NullPointerException();
        }
        this.data = data.getBytes();
        hashCode = Arrays.hashCode(this.data);
    }

    public ByteArrayWrapper(String data0, String data1) {
        if (data0 == null || data1 == null) {
            throw new NullPointerException();
        }
        this.data = (data0 + "|" + data1).getBytes();
        hashCode = Arrays.hashCode(this.data);
    }

    public ByteArrayWrapper(ByteArrayWrapper b0, ByteArrayWrapper b1) {
        if (b0 == null || b1 == null) {
            throw new NullPointerException();
        }

        if (b0.compareTo(b1) >= 0) {
            this.data = Arrays.copyOf(b0.getData(), b0.getData().length + 1 + b1.getData().length);
            this.data[b0.getData().length] = '|';
            System.arraycopy(b1.getData(), 0, this.data, b0.getData().length + 1, b1.getData().length);
        } else {
            this.data = Arrays.copyOf(b1.getData(), b1.getData().length + 1 + b0.getData().length);
            this.data[b1.getData().length] = '|';
            System.arraycopy(b0.getData(), 0, this.data, b1.getData().length + 1, b0.getData().length);
        }

        hashCode = Arrays.hashCode(this.data);
    }


    public ByteArrayWrapper splitUpPlaceIdPair() {
        int len = data.length;
        byte[] placeId1 = null;

        for (int i = 0; i < len; i++) {
            if (data[i] == '|') {
                placeId1 = Arrays.copyOfRange(data, i + 1, len);
                data = Arrays.copyOf(data, i);
                hashCode = Arrays.hashCode(this.data);
                return new ByteArrayWrapper(placeId1);

            }
        }
        System.out.println("Error in splitUpPlaceIdPair, no delimiter found:" + toString());
        System.exit(-1);
        return null;
    }

    public void splitUpPlaceIdPair(ByteArrayWrapper s0, ByteArrayWrapper s1) {
        int len = data.length;

        byte[] placeId1 = null;

        for (int i = 0; i < len; i++) {
            if (data[i] == '|') {
                placeId1 = Arrays.copyOfRange(data, i + 1, len);
                s1.data = placeId1;
                s0.data = Arrays.copyOf(data, i);
                s0.hashCode = Arrays.hashCode(s0.data);
                s1.hashCode = Arrays.hashCode(s1.data);
                return;
            }
        }
        System.out.println("Error in splitUpPlaceIdPair, no delimiter found:" + toString());
        System.exit(-1);
        return;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ByteArrayWrapper)) {
            return false;
        }
        return Arrays.equals(data, ((ByteArrayWrapper) other).data);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return new String(data);
    }

    public byte[] getData() {
        return data;
    }

    public int compareTo(ByteArrayWrapper b) {
        return compareTo(this.data, b.getData());
    }

    public int compareTo(byte[] b0, byte[] b1) {
        int len0 = b0.length;
        int len1 = b1.length;

        for (int i = 0; ; i++) {
            int a = 0, b = 0;

            if (i < len0) {
                a = (int) b0[i];
            } else if (i >= len1) {
                return 0;
            }

            if (i < len1) {
                b = (int) b1[i];
            }

            if (a > b) {
                return 1;
            }

            if (b > a) {
                return -1;
            }
        }
    }


}
