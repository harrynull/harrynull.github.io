package util;

public class ArrayStack implements Stack {
    protected Object[] array;
    protected int size;

    public ArrayStack() {
        array = new Object[8];
        size = 0;
    }

    public void push(Object item) {
        if (size >= array.length) {
            resize();
        }
        array[size] = item;
        size = size + 1;
    }

    // assumes !(size == 0)
    public Object pop() {
        size = size - 1;
        Object item = array[size];
        array[size] = null;
        return item;
    }

    public boolean empty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    protected void resize() {
        Object[] newArray = new Object[array.length * 2];
        System.arraycopy(array, 0, newArray, 0, size);
        array = newArray;
    }
}
