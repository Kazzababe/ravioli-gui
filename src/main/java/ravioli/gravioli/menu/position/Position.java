package ravioli.gravioli.menu.position;

public record Position(int x, int y) {
    public Position(final int slot) {
        this(
            slot % 9,
            slot / 9
        );
    }
}
