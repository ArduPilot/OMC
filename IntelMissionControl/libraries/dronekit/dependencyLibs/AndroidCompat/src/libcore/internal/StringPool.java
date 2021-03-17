package libcore.internal;

public class StringPool {

    /**
     * Returns a string equal to {@code new String(array, start, length)}.
     */
    public String get(char[] array, int start, int length) {
        return new String(array, start, length);
    }
}
