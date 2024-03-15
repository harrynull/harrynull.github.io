package util;

// Definition of a simple ArrayList implementation
public class ArrayList implements List {
    protected Object[] array;
    protected int size;

    public ArrayList() {
        array = new Object[8];
        size = 0;
    }

    public ArrayList(int length) {
        array = new Object[length];
        size = 0;
    }

    public void add(Object item) {
        if (size >= array.length) {
            resize();
        }
        array[size] = item;
        size = size + 1;
    }

    // assumes !(index < 0 || index >= size)
    public Object get(int index) {
        return array[index];
    }

    public int size() {
        return size;
    }

    public boolean empty() {
        return size == 0;
    }

    protected void resize() {
        Object[] newArray = new Object[array.length * 2];
        System.arraycopy(array, 0, newArray, 0, size);
        array = newArray;
    }
}
