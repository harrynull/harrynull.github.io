package util;

// Definition of a simple List interface
public interface List extends Collection {
    public void add(Object item);

    public Object get(int index);

    public int size();
}
