package ravioli.gravioli.menu.component.input;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ItemFilter {
    boolean passes(@NotNull ItemStack itemStack);
}
