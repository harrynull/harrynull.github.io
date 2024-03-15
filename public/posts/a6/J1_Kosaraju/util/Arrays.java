package util;

public class Arrays {
    public Arrays() {}

    // Note that this implementation assumes that the array is of type Object[].
    // assumes !(array == null)
    public static void fill(boolean[] array, boolean value) {
        for (int i = 0; i < array.length; i = i + 1)
            array[i] = value;
    }

    public static List asList(Object[] elements) {
        List list = new ArrayList(elements.length);
        for (int i = 0; i < elements.length; i = i + 1)
            list.add(elements[i]);
        return list;
    }

}
