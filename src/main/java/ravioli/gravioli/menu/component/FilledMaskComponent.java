package ravioli.gravioli.menu.component;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.mask.Mask;
import ravioli.gravioli.menu.renderer.MenuRenderer;

public class FilledMaskComponent implements ImplicitMenuComponent {
    private final Mask mask;
    private final ItemStack itemStack;

    FilledMaskComponent(@NotNull final Mask mask, @NotNull final ItemStack itemStack) {
        this.mask = mask;
        this.itemStack = itemStack;
    }

    @Override
    public void queue(@NotNull final MenuRenderer renderer) {
        for (final Integer slot : this.mask) {
            renderer.queue(slot, this.itemStack);
        }
    }
}
