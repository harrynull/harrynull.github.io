package shapes;

public class ResizableSquare extends Square implements Resizable {
    public ResizableSquare(int side) {
        this.side = side;
    }

    public void resize(int factor) {
        this.side = this.side * factor;
    }

    public String toString() {
        return "Resizable square with side " + side + " and color " + this.color;
    }

}
