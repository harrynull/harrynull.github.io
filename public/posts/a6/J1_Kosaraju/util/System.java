package util;

public class System {
    public System() {
    }

    // assumes !(src == null || dest == null)
    // assumes !(srcPos < 0 || destPos < 0 || length < 0 || srcPos + length >
    // size(src) || destPos + length > size(dest))
    public static void arraycopy(Object[] src, int srcPos, Object[] dest, int destPos, int length) {
        if (src == dest && srcPos < destPos) {
            for (int i = length - 1; i >= 0; i = i - 1) {
                dest[destPos + i] = src[srcPos + i];
            }
        } else {
            for (int i = 0; i < length; i = i + 1) {
                dest[destPos + i] = src[srcPos + i];
            }
        }
    }

}
