package ravioli.gravioli.menu.provider.impl;

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.component.ImplicitMenuComponent;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.handler.MenuHandler;
import ravioli.gravioli.menu.pagination.Mask;
import ravioli.gravioli.menu.property.MenuProperty;
import ravioli.gravioli.menu.provider.MenuProvider;
import ravioli.gravioli.menu.renderer.MenuRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PaginationMenuProvider implements MenuProvider, ImplicitMenuComponent {
    private final Mask mask;

    private MenuProperty<Integer> page;
    private MenuProperty<List<MenuComponent>> items;

    public PaginationMenuProvider(@NotNull final Mask mask) {
        this.mask = mask;
    }

    @Override
    public void queue(@NotNull final MenuRenderer renderer) {
        final Iterator<Integer> slotIterator = this.mask.iterator();
        final List<MenuComponent> pageComponents = this.items.get()
            .stream()
            .skip((long) this.page.get() * this.mask.getSize())
            .limit(this.mask.getSize())
            .toList();

        for (final MenuComponent item : pageComponents) {
            if (!slotIterator.hasNext()) {
                break;
            }
            final int slot = slotIterator.next();

            renderer.queue(slot, item);
        }
    }

    @Override
    public void init(@NotNull final MenuHandler menu) {
        this.page = menu.properties().create(0);
        this.items = menu.properties().create(new ArrayList<>());
    }

    @Override
    public void update() {

    }

    public void resetPage() {
        this.page.set(0);
    }

    public void items(@NotNull final MenuComponent... menuComponents) {
        this.items.set(List.of(menuComponents));
    }

    public int page() {
        return this.page.get();
    }

    public void next() {
        this.page.set(this.page.get() + 1);
    }

    public void previous() {
        this.page.set(this.page.get() - 1);
    }

    public boolean hasNext() {
        return (this.page.get() + 1) * this.mask.getSize() < this.items.get().size();
    }

    public boolean hasPrevious() {
        return this.page.get() > 0;
    }
}
