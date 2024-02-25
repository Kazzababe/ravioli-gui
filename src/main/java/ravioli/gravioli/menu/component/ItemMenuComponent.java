package ravioli.gravioli.menu.component;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.menu.renderer.MenuRenderer;

import java.util.function.Consumer;

public class ItemMenuComponent implements MenuComponent {
    private final ItemStack itemStack;
    private final Consumer<InventoryClickEvent> clickEventConsumer;
    private final boolean clone;

    ItemMenuComponent(@NotNull final ItemStack itemStack,
                      @Nullable final Consumer<InventoryClickEvent> clickEventConsumer,
                      final boolean clone) {
        this.itemStack = itemStack;
        this.clickEventConsumer = clickEventConsumer;
        this.clone = clone;
    }

    @Override
    public void queue(@NotNull final MenuRenderer renderer, final int slot) {
        if (this.clone) {
            renderer.queue(slot, this.itemStack.clone());
        } else {
            renderer.queue(slot, this.itemStack);
        }
        if (this.clickEventConsumer != null) {
            renderer.queue(slot, this.clickEventConsumer);
        }
    }
}
