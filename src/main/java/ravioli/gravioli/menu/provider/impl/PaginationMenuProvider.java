package ravioli.gravioli.menu.provider.impl;

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.component.ImplicitMenuComponent;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.handler.MenuHandler;
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

    public static class Mask implements Iterable<Integer> {
        public static @NotNull Mask fromString(@NotNull final String maskString) {
            final String[] lines = maskString.split(" ");
            final List<Integer> slots = new ArrayList<>();

            for (int y = 0, slot = 0; y < lines.length; y++) {
                final String line = lines[y];

                for (int x = 0; x < 9; x++, slot++) {
                    if (x >= line.length()) {
                        continue;
                    }
                    final String lineChar = String.valueOf(line.charAt(x));
                    final Integer lineBit = Ints.tryParse(lineChar);

                    if (lineBit == null || (lineBit != 0 && lineBit != 1)) {
                        continue;
                    }
                    if (lineBit == 1) {
                        slots.add(slot);
                    }
                }
            }
            return new Mask(Ints.toArray(slots));
        }

        private final int[] validSlots;

        private Mask(final int[] validSlots) {
            this.validSlots = validSlots;
        }

        public int getSize() {
            return this.validSlots.length;
        }

        @Override
        public @NotNull Iterator<Integer> iterator() {
            return new MaskIterator();
        }

        private class MaskIterator implements Iterator<Integer> {
            private int index;

            @Override
            public boolean hasNext() {
                return this.index < validSlots.length;
            }

            @Override
            public Integer next() {
                return validSlots[this.index++];
            }
        }
    }
}
