package ravioli.gravioli.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.component.UserInputComponent;
import ravioli.gravioli.menu.component.input.ItemFilter;
import ravioli.gravioli.menu.component.input.UpdateAction;
import ravioli.gravioli.menu.content.MenuContentProvider;
import ravioli.gravioli.menu.content.impl.SimpleMenuContentProvider;
import ravioli.gravioli.menu.handler.MenuHandler;
import ravioli.gravioli.menu.property.MenuPropertyHandler;
import ravioli.gravioli.menu.property.impl.SimpleMenuPropertyHandler;
import ravioli.gravioli.menu.provider.MenuProviderHandler;
import ravioli.gravioli.menu.provider.impl.SimpleMenuProviderHandler;
import ravioli.gravioli.menu.renderer.MenuRenderer;
import ravioli.gravioli.menu.renderer.RenderBoundary;
import ravioli.gravioli.menu.renderer.impl.SimpleMenuRenderer;
import ravioli.gravioli.menu.util.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class Menu implements MenuHandler, InventoryHolder, Listener {
    private static final ItemStack AIR = new ItemStack(Material.AIR);

    private final Plugin plugin;
    protected final Player player;

    private final MenuPropertyHandler menuPropertyHandler;
    private final MenuProviderHandler menuProviderHandler;
    private final MenuContentProvider menuContentProvider;
    private final SimpleMenuRenderer menuRenderer;

    private final Map<Integer, Consumer<InventoryClickEvent>> clickEvents = new ConcurrentHashMap<>();

    public Menu(@NotNull final Plugin plugin, @NotNull final Player player) {
        this.menuPropertyHandler = new SimpleMenuPropertyHandler(this);
        this.menuProviderHandler = new SimpleMenuProviderHandler(this);
        this.menuContentProvider = new SimpleMenuContentProvider();
        this.menuRenderer = new SimpleMenuRenderer(this, plugin, this.clickEvents);

        this.plugin = plugin;
        this.player = player;
    }

    public final void open() {
        Bukkit.getScheduler().getMainThreadExecutor(this.plugin).execute(() -> {
            this.menuProviderHandler.init();
            this.init();
            this.update();
            this.menuRenderer.init();
            this.player.openInventory(this.getInventory());
            this.render();

            Bukkit.getPluginManager().registerEvents(this, this.plugin);

            this.onOpen();
        });
    }

    public void render() {
        this.menuContentProvider.supply(this.menuRenderer);
        this.menuRenderer.render();
    }

    public @NotNull Player getPlayer() {
        return this.player;
    }

    @Override
    public @NotNull MenuPropertyHandler properties() {
        return this.menuPropertyHandler;
    }

    @Override
    public @NotNull MenuProviderHandler providers() {
        return this.menuProviderHandler;
    }

    @Override
    public @NotNull MenuContentProvider content() {
        return this.menuContentProvider;
    }

    @Override
    public @NotNull MenuRenderer renderer() {
        return this.menuRenderer;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.renderer().getInventory();
    }

    @EventHandler
    public final void onClickInventory(final InventoryClickEvent event) {
        final InventoryView inventoryView = event.getView();
        final Inventory inventory = inventoryView.getTopInventory();

        if (!inventory.equals(this.getInventory())) {
            return;
        }
        if (!event.getWhoClicked().equals(this.player)) {
            event.setCancelled(true);

            return;
        }
        final RenderBoundary renderBoundary = this.renderer().renderBoundary();
        final Inventory clickedInventory = event.getClickedInventory();
        final boolean clickedPlayerInventory = clickedInventory != null && !clickedInventory.equals(this.getInventory());

        if (!renderBoundary.isValid(this.getInventory(), event.getRawSlot())) {
            if (!clickedPlayerInventory) {
                return;
            }
            if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                return;
            }
            event.setCancelled(true);

            final ItemStack clickedItemStack = Objects.requireNonNullElse(event.getCurrentItem(), AIR);

            if (clickedItemStack.getType().isAir()) {
                return;
            }
            final Map.Entry<Integer, UserInputComponent> bestInput = this.getBestInput(event, clickedItemStack);

            if (bestInput == null) {
                return;
            }
            final ItemStack targetItemStack = Objects.requireNonNullElse(this.getInventory().getItem(bestInput.getKey()), AIR);
            final UserInputComponent userInputComponent = bestInput.getValue();

            if (targetItemStack.getType().isAir()) {
                this.getInventory().setItem(bestInput.getKey(), clickedItemStack);
                this.player.getInventory().clear(event.getSlot());
                this.runUpdateAction(userInputComponent);

                return;
            }
            final int currentAmount = targetItemStack.getAmount();
            final int theoreticalNewAmount = currentAmount + clickedItemStack.getAmount();
            final int maxStackSize = clickedItemStack.getType().getMaxStackSize();
            final int newAmount;
            final int leftover;

            if (theoreticalNewAmount > maxStackSize) {
                newAmount = maxStackSize;
                leftover = theoreticalNewAmount - maxStackSize;
            } else {
                newAmount = theoreticalNewAmount;
                leftover = 0;
            }
            if (leftover == 0) {
                this.player.getInventory().clear(event.getSlot());
            } else {
                clickedItemStack.setAmount(leftover);
            }
            targetItemStack.setAmount(newAmount);
            this.runUpdateAction(userInputComponent);

            return;
        }
        final MenuComponent menuComponent = this.menuRenderer.getLastComponent(event.getRawSlot());

        if (menuComponent instanceof final UserInputComponent userInputComponent) {
            if (event.isShiftClick()) {
                this.runUpdateAction(userInputComponent);

                return;
            }
            final ItemStack currentItem = Objects.requireNonNullElse(event.getCurrentItem(), AIR);
            final int hotbarButton = event.getHotbarButton();
            final ItemStack cursor = Objects.requireNonNullElse(
                    hotbarButton == -1 ?
                            event.getCursor() :
                            this.player.getInventory().getItem(hotbarButton),
                    AIR
            );
            final ItemFilter itemFilter = userInputComponent.itemFilter();
            final boolean cursorPasses = itemFilter.passes(cursor);

            if ((cursorPasses && itemFilter.passes(currentItem)) ||
                    (!currentItem.getType().isAir() && cursor.getType().isAir())) {
                this.runUpdateAction(userInputComponent);

                return;
            }
            if (!cursorPasses) {
                event.setCancelled(true);
            } else {
                this.runUpdateAction(userInputComponent);
            }
            return;
        }
        event.setCancelled(true);

        this.click(event);
    }

    private void runUpdateAction(@NotNull final UserInputComponent userInputComponent) {
        final UpdateAction updateAction = userInputComponent.updateAction();

        if (updateAction != null) {
            Bukkit.getScheduler().runTaskLater(this.plugin, updateAction::run, 0);
        }
    }

    private @Nullable Map.Entry<Integer, UserInputComponent> getBestInput(@NotNull final InventoryClickEvent event,
                                                                          @NotNull final ItemStack clickedItemStack) {
        final List<Map.Entry<Integer, UserInputComponent>> entries = this.menuRenderer.getInputComponents()
                .entrySet()
                .stream()
                .filter((entry) -> entry.getValue().itemFilter().passes(clickedItemStack))
                .sorted(Comparator.comparing((Map.Entry<Integer, UserInputComponent> entry) ->
                        entry.getValue().shiftClickPriority()).reversed()
                )
                .toList();

        for (final Map.Entry<Integer, UserInputComponent> entry : entries) {
            final int slot = entry.getKey();
            final ItemStack targetItemStack = Objects.requireNonNullElse(this.getInventory().getItem(slot), AIR);

            if (targetItemStack.getType().isAir()) {
                return entry;
            }
            if (targetItemStack.isSimilar(clickedItemStack)) {
                final int maxStackSize = clickedItemStack.getType().getMaxStackSize();

                if (targetItemStack.getAmount() < maxStackSize) {
                    return entry;
                }
            }
        }
        return null;
    }

    @EventHandler
    public final void onDragInventory(final InventoryDragEvent event) {
        final Set<Integer> rawSlots = event.getRawSlots();

        if (rawSlots.isEmpty()) {
            return;
        }
        if (rawSlots.size() == 1) {
            final int slot = rawSlots.iterator().next();

            if (slot >= this.getInventory().getSize()) {
                return;
            }
            final InventoryClickEvent fakeEvent = new InventoryClickEvent(
                event.getView(),
                event.getView().getSlotType(slot),
                slot,
                event.getType() == DragType.SINGLE ?
                    ClickType.RIGHT :
                    ClickType.LEFT,
                InventoryAction.UNKNOWN
            ) {
                @Override
                public @NotNull ItemStack getCursor() {
                    return event.getOldCursor();
                }
            };

            this.onClickInventory(fakeEvent);

            if (fakeEvent.isCancelled()) {
                event.setCancelled(true);
            }
            return;
        }
        for (final int slot : event.getRawSlots()) {
            if (slot < this.getInventory().getSize()) {
                event.setCancelled(true);

                break;
            }
        }
    }

    @EventHandler
    public final void onCloseInventory(final InventoryCloseEvent event) {
        final Inventory inventory = event.getInventory();

        if (!inventory.equals(this.getInventory())) {
            return;
        }
        if (event.getPlayer() instanceof Player) {
            if (!this.onClose(event.getReason())) {
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.player.openInventory(this.getInventory()), 1);

                return;
            }
        }
        HandlerList.unregisterAll(this);

        this.menuRenderer.cleanup();
    }

    public final @NotNull Plugin plugin() {
        return this.plugin;
    }

    protected void onOpen() {}

    protected boolean onClose(@NotNull final InventoryCloseEvent.Reason closeReason) {
        return true;
    }

    private void click(@NotNull final InventoryClickEvent event) {
        final int slot = event.getRawSlot();
        final int totalSize = this.renderer().renderBoundary().getTotalSize(this.getInventory());

        if (slot < 0 || slot >= totalSize) {
            return;
        }
        this.clickEvents.entrySet()
                .stream()
                .filter((entry) -> entry.getKey() == slot)
                .map(Map.Entry::getValue)
                .forEach((consumer) -> consumer.accept(event));
    }

    // TODO: Handle clicking player slots
    public final void click(final int slot, @NotNull final ClickType clickType) {
        final InventoryView view = this.player.getOpenInventory();
        final InventoryClickEvent fakeEvent = new InventoryClickEvent(
                view,
                view.getSlotType(slot),
                slot,
                clickType,
                InventoryAction.UNKNOWN
        );

        this.click(fakeEvent);
    }

    protected final void setTitle(@NotNull final Component title) {
        if (this.properties().title() != null) {
            final ServerPlayer serverPlayer = ReflectionUtil.getServerPlayer(this.player);
            final ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(
                    serverPlayer.containerMenu.containerId,
                    switch (this.properties().height()) {
                        case 1 -> MenuType.GENERIC_9x1;
                        case 2 -> MenuType.GENERIC_9x2;
                        case 3 -> MenuType.GENERIC_9x3;
                        case 4 -> MenuType.GENERIC_9x4;
                        case 5 -> MenuType.GENERIC_9x5;
                        case 6 -> MenuType.GENERIC_9x6;
                        default -> throw new IllegalStateException("Invalid inventory size detected.");
                    },
                    Objects.requireNonNull(
                            net.minecraft.network.chat.Component.Serializer.fromJson(
                                    GsonComponentSerializer.gson().serializeToTree(title)
                            )
                    )
            );

            serverPlayer.connection.send(packet);
            this.player.updateInventory();
        } else {
            this.properties().title(title);
        }
    }

    @Blocking
    public abstract void init();

    @Blocking
    public abstract void update();
}
