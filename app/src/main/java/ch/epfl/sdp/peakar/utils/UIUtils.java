package ch.epfl.sdp.peakar.utils;

public class UIUtils {

    private final static int INTEGER_CHUNK = 3;
    private final static String SPACE = " ";

    /**
     * Convert an given integer into a string follow the pattern of
     * 12 345 678
     * @param number Integer number to convert to string
     * @return String representing int.
     */
    public static String IntegerConvert(Integer number) {
        String numberString = number.toString();
        int length = numberString.length();

        StringBuilder builder = new StringBuilder();
        int counter = length % INTEGER_CHUNK;

        if (counter > 0) {
            builder.append(numberString.substring(0, counter));
            builder.append(SPACE);
        }

        while (counter < length) {
            builder.append(numberString.substring(counter, counter + INTEGER_CHUNK));
            builder.append(SPACE);
            counter += INTEGER_CHUNK;
        }
        return builder.toString();
    }
}
