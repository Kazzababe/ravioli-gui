package ravioli.gravioli.menu.inventory;

import com.google.common.base.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class VirtualInventory {
    private final ItemStack[][] itemStacks;
    private final Component title;

    public VirtualInventory(@Nullable final Component title, @NotNull final ItemStack[][] itemStacks) {
        this.title = title;
        this.itemStacks = itemStacks;
    }

    @Override
    public boolean equals(@Nullable final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        final VirtualInventory that = (VirtualInventory) other;

        return Arrays.deepEquals(this.itemStacks, that.itemStacks) && Objects.equal(this.title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.itemStacks, this.title);
    }
}
