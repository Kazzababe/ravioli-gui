package ravioli.gravioli.menu.component;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.mask.Mask;
import ravioli.gravioli.menu.renderer.MenuRenderer;

import java.util.Iterator;
import java.util.List;

public final class GridComponent implements ImplicitMenuComponent {
    private final Mask mask;
    private final List<MenuComponent> contents;

    GridComponent(@NotNull final Mask mask, @NotNull final List<MenuComponent> contents) {
        this.mask = mask;
        this.contents = contents;
    }

    @Override
    public void queue(@NotNull final MenuRenderer renderer) {
        final Iterator<Integer> slotIterator = this.mask.iterator();

        for (final MenuComponent item : this.contents) {
            if (!slotIterator.hasNext()) {
                break;
            }
            final int slot = slotIterator.next();

            renderer.queue(slot, item);
        }
    }
}
