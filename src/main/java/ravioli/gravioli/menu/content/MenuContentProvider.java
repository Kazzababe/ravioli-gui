package ravioli.gravioli.menu.content;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.component.ImplicitMenuComponent;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.position.Position;
import ravioli.gravioli.menu.renderer.MenuRenderer;

public interface MenuContentProvider {
    void set(int x, int y, @NotNull MenuComponent menuComponent);

    void set(int slot, @NotNull MenuComponent menuComponent);

    void set(@NotNull MenuComponent menuComponent, @NotNull Position... positions);

    void add(@NotNull ImplicitMenuComponent menuComponent);

    void supply(@NotNull MenuRenderer menuRenderer);

    default int positionToSlot(final int x, final int y) {
        return y * 9 + x;
    }
}
