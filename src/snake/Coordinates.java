package snake;

class Coordinates{
    // olatile fields ?
    public int x;
    public int y;

    Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return this.x + " " + this.y;
    }

    public boolean equalsCoordinate(Coordinates coordinateToCheck) {
        return coordinateToCheck.x == this.x && coordinateToCheck.y == this.y;
    }
}