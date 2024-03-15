import shapes.*;

public class Main {
    public Main() {
    }

    public static int test() {
        shapes.Square square = new shapes.Square(5);
        square.setColor("red");
        int x = square.getArea() - square.colorToInt();

        square = new ResizableSquare(5);
        square.setColor("green");
        ((ResizableSquare) square).resize(2);
        int y = square.getArea() + square.colorToInt();

        return x + y;
    }
}
