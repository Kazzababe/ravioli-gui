package ravioli.gravioli.menu.pagination;

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StringMask implements Mask {
    private final String originalMaskString;
    private final int[] validSlots;

    private StringMask(@NotNull final String maskString) {
        this.originalMaskString = maskString;
        this.validSlots = this.findValidSlots(maskString);
    }

    @Override
    public int getSize() {
        return this.validSlots.length;
    }

    @Override
    public int[] getValidSlots() {
        return this.validSlots;
    }

    @Override
    public @NotNull StringMask inverse() {
        final String newMask = this.originalMaskString.replace("1", "2")
                .replace("0", "1")
                .replace("2", "0");

        return new StringMask(newMask);
    }

    private int[] findValidSlots(@NotNull final String maskString) {
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
        return Ints.toArray(slots);
    }
}
