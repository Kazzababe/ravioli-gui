package ravioli.gravioli.menu.renderer;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public enum RenderBoundary {
    MENU {
        @Override
        public boolean isValid(@NotNull final Inventory inventory, int slot) {
            return slot >= 0 && slot < inventory.getSize();
        }

        @Override
        public int getTotalSize(@NotNull final Inventory inventory) {
            return inventory.getSize();
        }
    },
    FULL {
        @Override
        public boolean isValid(@NotNull final Inventory inventory, int slot) {
            return slot >= 0 && slot < inventory.getSize() + PLAYER_INVENTORY_SIZE;
        }

        @Override
        public int getTotalSize(@NotNull final Inventory inventory) {
            return inventory.getSize() + PLAYER_INVENTORY_SIZE;
        }
    };

    private static final int PLAYER_INVENTORY_SIZE = 9 * 4;

    public abstract boolean isValid(@NotNull Inventory inventory, int slot);

    public abstract int getTotalSize(@NotNull Inventory inventory);
}
