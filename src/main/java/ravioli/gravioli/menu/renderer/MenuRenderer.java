package ravioli.gravioli.menu.renderer;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.component.ImplicitMenuComponent;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.inventory.VirtualInventory;

import java.util.function.Consumer;

public interface MenuRenderer {
    @NotNull VirtualInventory createVirtualInventory();

    @NotNull Inventory getInventory();

    void queue(int slot, @NotNull MenuComponent menuComponent);

    void queue(int slot, @NotNull ItemStack itemStack);

    void queue(int slot, @NotNull Consumer<InventoryClickEvent> clickEventConsumer);

    void queue(@NotNull ImplicitMenuComponent menuComponent);

    void init();

    void render();

    boolean shouldReRender();

    void cleanup();

    @NotNull RenderType renderType();

    void renderType(@NotNull RenderType renderType);

    @NotNull RenderBoundary renderBoundary();

    void renderBoundary(@NotNull RenderBoundary renderBoundary);

    boolean isPerformingComparison();
}
