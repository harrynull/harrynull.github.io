package shapes;

public class Square implements Shape, Colorable {
    protected int side;
    protected String color;

    public Square() {
        this.side = 0;
    }

    public Square(int side) {
        this.side = side;
    }

    public int getArea() {
        return side * side;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String toString() {
        return "Square with side " + side + " and color " + color;
    }

    public int colorToInt() {
        if (color.equals((Object) "red")) {
            return 3;
        } else if (color.equals((Object) "blue")) {
            return 2;
        } else if (color.equals((Object) "green")) {
            return 1;
        } else {
            return 0;
        }
    }
}
