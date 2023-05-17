package ravioli.gravioli.menu.component;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.menu.component.input.ItemFilter;
import ravioli.gravioli.menu.component.input.UpdateAction;
import ravioli.gravioli.menu.renderer.MenuRenderer;

public class UserInputComponent implements MenuComponent {
    private static final ItemStack INDICATOR = new ItemStack(Material.BARREL);
    private static final NamespacedKey KEY = new NamespacedKey("gui_library", "indicator");

    static {
        final ItemMeta itemMeta = INDICATOR.getItemMeta();
        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        container.set(KEY, PersistentDataType.STRING, "1");
        INDICATOR.setItemMeta(itemMeta);
    }

    private final ItemFilter itemFilter;
    private final UpdateAction updateAction;
    private final int shiftClickPriority;

    UserInputComponent(@NotNull final ItemFilter itemFilter, @Nullable final UpdateAction updateAction,
                       final int shiftClickPriority) {
        this.itemFilter = itemFilter;
        this.updateAction = updateAction;
        this.shiftClickPriority = shiftClickPriority;
    }

    public @NotNull ItemFilter itemFilter() {
        return this.itemFilter;
    }

    public @Nullable UpdateAction updateAction() {
        return this.updateAction;
    }

    public int shiftClickPriority() {
        return this.shiftClickPriority;
    }

    @Override
    public void queue(@NotNull MenuRenderer renderer, int slot) {
        renderer.queue(slot, INDICATOR.clone());
        renderer.queue(slot, (event) -> event.setCancelled(false));
    }
}
