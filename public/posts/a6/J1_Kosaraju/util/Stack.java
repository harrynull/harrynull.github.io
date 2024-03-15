package util;

// Definition of a simple Stack interface
public interface Stack extends Collection {
    public void push(Object item);

    public Object pop();

    public boolean empty();
}
