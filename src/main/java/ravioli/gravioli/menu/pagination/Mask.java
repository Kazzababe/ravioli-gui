package ravioli.gravioli.menu.pagination;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public interface Mask extends Iterable<Integer> {
    int getSize();

    int[] getValidSlots();

    @NotNull Mask inverse();

    @Override
    default @NotNull Iterator<Integer> iterator() {
        return new MaskIterator(this);
    }

    class MaskIterator implements Iterator<Integer> {
        private final Mask mask;

        private int index;

        MaskIterator(@NotNull final Mask mask) {
            this.mask = mask;
        }

        @Override
        public boolean hasNext() {
            return this.index < this.mask.getValidSlots().length;
        }

        @Override
        public Integer next() {
            return this.mask.getValidSlots()[this.index++];
        }
    }
}
