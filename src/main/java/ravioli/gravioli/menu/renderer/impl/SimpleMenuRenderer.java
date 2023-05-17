package ravioli.gravioli.menu.renderer.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.menu.Menu;
import ravioli.gravioli.menu.component.ImplicitMenuComponent;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.component.UserInputComponent;
import ravioli.gravioli.menu.inventory.VirtualInventory;
import ravioli.gravioli.menu.renderer.MenuRenderer;
import ravioli.gravioli.menu.renderer.RenderBoundary;
import ravioli.gravioli.menu.renderer.RenderType;
import ravioli.gravioli.menu.util.ReflectionUtil;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SimpleMenuRenderer extends PacketAdapter implements MenuRenderer {
    private static final ItemStack AIR = new ItemStack(Material.AIR);
    private static final NamespacedKey INDICATOR_KEY = new NamespacedKey("gui_library", "indicator");

    private final Menu menu;
    protected final Plugin plugin;

    private final Lock renderLock;
    private final ReadWriteLock itemLock;

    private final Map<Integer, Consumer<InventoryClickEvent>> clickEvents;
    private final Map<Integer, Consumer<InventoryClickEvent>> temporaryClickEvents = new HashMap<>();
    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final Map<Integer, ItemStack> temporaryItems = new HashMap<>();
    private final Map<Integer, MenuComponent> lastComponents = new HashMap<>();
    private final ProtocolManager protocolManager;

    private Inventory inventory;
    private Inventory visibleInventory;
    private Component previousTitle;
    private VirtualInventory virtualInventory;
    private boolean rendering;
    private RenderType renderType;
    private RenderBoundary renderBoundary;

    public SimpleMenuRenderer(@NotNull final Menu menu, @NotNull final Plugin plugin,
                              @NotNull final Map<Integer, Consumer<InventoryClickEvent>> clickEvents) {
        super(plugin, ListenerPriority.HIGHEST, PacketType.Play.Client.WINDOW_CLICK, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS);

        this.menu = menu;
        this.plugin = plugin;
        this.clickEvents = clickEvents;

        this.renderLock = new ReentrantLock();
        this.itemLock = new ReentrantReadWriteLock();
        this.renderType = RenderType.PHYSICAL;
        this.renderBoundary = RenderBoundary.MENU;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void onPacketSending(@NotNull final PacketEvent event) {
        final Player player = event.getPlayer();

        if (!player.getUniqueId().equals(this.menu.getPlayer().getUniqueId())) {
            return;
        }
        if (this.renderType != RenderType.PACKET) {
            return;
        }
        final PacketType packetType = event.getPacketType();

        if (packetType == PacketType.Play.Server.SET_SLOT) {
            this.handleSetSlot(event);
        } else {
            this.handleWindowItems(event);
        }
    }

    private void handleWindowItems(@NotNull final PacketEvent event) {
        final PacketContainer packet = event.getPacket();
        final ServerPlayer serverPlayer = ReflectionUtil.getServerPlayer(event.getPlayer());
        final AbstractContainerMenu container = serverPlayer.containerMenu;
        final int windowId = packet.getIntegers().read(0);

        if (windowId != container.containerId) {
            return;
        }
--
        final List<ItemStack> itemStacks = packet.getItemListModifier().read(0);
        final int inventorySize = this.inventory.getSize();
        final int totalSize = this.renderBoundary.getTotalSize(this.inventory);

        for (int i = 0; i < totalSize; i++) {
            final ItemStack itemStack;

            if (i < inventorySize) {
                itemStack = this.inventory.getItem(i);
            } else {
                itemStack = Objects.requireNonNullElse(
                        this.items.get(i),
                        AIR
                );
            }
            if (itemStack == null) {
                continue;
            }
            itemStacks.set(i, itemStack);
        }
        packet.getItemListModifier().write(0, itemStacks);
    }

    @Override
    public void onPacketReceiving(@NotNull final PacketEvent event) {
        final Player player = event.getPlayer();

        if (!player.getUniqueId().equals(this.menu.getPlayer().getUniqueId())) {
            return;
        }
        if (this.renderType != RenderType.PACKET) {
            return;
        }
        final PacketType packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.WINDOW_CLICK) {
            this.handleWindowClick(event);
        }
    }

    private void handleSetSlot(@NotNull final PacketEvent event) {
        final PacketContainer packet = event.getPacket();
        final ServerPlayer serverPlayer = ReflectionUtil.getServerPlayer(event.getPlayer());
        final AbstractContainerMenu container = serverPlayer.containerMenu;
        final int windowId = packet.getIntegers().read(0);

        if (windowId != container.containerId) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().updateInventory();
    }

    private void handleWindowClick(@NotNull final PacketEvent event) {
        final PacketContainer packet = event.getPacket();
        final ServerPlayer serverPlayer = ReflectionUtil.getServerPlayer(this.menu.getPlayer());
        final AbstractContainerMenu container = serverPlayer.containerMenu;
        final int windowId = packet.getIntegers().read(0);

        if (windowId != container.containerId) {
            return;
        }
        final int slot = packet.getIntegers().read(2);

        if (!this.renderBoundary.isValid(this.inventory, slot)) {
            return;
        }
        final int button = packet.getIntegers().read(3);
        final InventoryClickType mode = packet.getEnumModifier(InventoryClickType.class, 4).read(0);
        ClickType clickType = ClickType.UNKNOWN;

        event.setCancelled(true);

        switch (mode) {
            case PICKUP -> {
                if (button == 0) {
                    clickType = ClickType.LEFT;
                } else if (button == 1) {
                    clickType = ClickType.RIGHT;
                }
            }
            case QUICK_MOVE -> {
                if (button == 0) {
                    clickType = ClickType.SHIFT_LEFT;
                } else if (button == 1) {
                    clickType = ClickType.SHIFT_RIGHT;
                }
            }
            case SWAP -> {
                if ((button >= 0 && button < 9) || button == 40) {
                    if (button == 40) {
                        clickType = ClickType.SWAP_OFFHAND;
                    } else {
                        clickType = ClickType.NUMBER_KEY;
                    }
                }
            }
            case CLONE -> {
                if (button == 2) {
                    clickType = ClickType.MIDDLE;
                }
            }
            case THROW -> {
                if (slot >= 0) {
                    if (button == 0) {
                        clickType = ClickType.DROP;
                    } else if (button == 1) {
                        clickType = ClickType.CONTROL_DROP;
                    }
                } else if (button == 1) {
                    clickType = ClickType.RIGHT;
                } else {
                    clickType = ClickType.LEFT;
                }
            }
            case PICKUP_ALL -> clickType = ClickType.DOUBLE_CLICK;
        }
        final ClickType finalClickType = clickType;

        Bukkit.getScheduler().getMainThreadExecutor(this.plugin).execute(() -> {
            this.menu.click(slot, finalClickType);
            event.getPlayer().updateInventory();
        });
    }

    @Override
    public @NotNull VirtualInventory createVirtualInventory() {
        final int height = this.menu.properties().height() + switch (this.renderBoundary) {
            case MENU -> 0;
            case FULL -> 4;
        };
        final ItemStack[][] itemStacks = new ItemStack[height][9];

        for (int y = 0, slot = 0; y < height; y++) {
            for (int x = 0; x < 9; x++, slot++) {
                final ItemStack itemStack = this.temporaryItems.get(slot);

                if (itemStack == null) {
                    continue;
                }
                itemStacks[y][x] = itemStack;
            }
        }
        return new VirtualInventory(this.menu.properties().title(), itemStacks);
    }

    @Override
    public void queue(final int slot, @NotNull final MenuComponent menuComponent) {
        menuComponent.queue(this, slot);

        this.lastComponents.put(slot, menuComponent);
    }

    @Override
    public void queue(final int slot, @NotNull final ItemStack itemStack) {
        this.itemLock.writeLock().lock();

        try {
            if (!this.renderBoundary.isValid(this.inventory, slot)) {
                throw new IllegalArgumentException("Cannot set a slot outside of the menu boundary.");
            }
            this.temporaryItems.put(slot, itemStack);
        } finally {
            this.itemLock.writeLock().unlock();
        }
    }

    @Override
    public void queue(final int slot, @NotNull final Consumer<InventoryClickEvent> clickEventConsumer) {
        if (!this.renderBoundary.isValid(this.inventory, slot)) {
            throw new IllegalArgumentException("Cannot set a slot outside of the menu boundary.");
        }
        this.temporaryClickEvents.put(slot, clickEventConsumer);
    }

    @Override
    public void queue(@NotNull final ImplicitMenuComponent menuComponent) {
        menuComponent.queue(this);
    }

    @Override
    public void init() {
        this.inventory = this.createInventory();
        this.visibleInventory = this.createInventory();
        this.previousTitle = this.menu.properties().title();

        this.protocolManager.addPacketListener(this);
    }

    @Override
    public void cleanup() {
        this.protocolManager.removePacketListener(this);

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> this.menu.getPlayer().updateInventory());
    }

    @Override
    public void render() {
        this.itemLock.readLock().lock();

        try {
            final int inventorySize = this.inventory.getSize();

            this.virtualInventory = this.createVirtualInventory();
            this.clickEvents.clear();
            this.clickEvents.putAll(this.temporaryClickEvents);
            this.items.clear();
            this.items.putAll(this.temporaryItems);

            for (int i = 0; i < inventorySize; i++) {
                final ItemStack currentItem = Objects.requireNonNullElse(this.inventory.getItem(i), AIR.clone());
                final ItemStack newItem = Objects.requireNonNullElse(this.items.get(i), AIR.clone());

                if (this.isIndicator(newItem)) {
                    continue;
                }
                if (Objects.equals(currentItem, newItem)) {
                    continue;
                }
                if (currentItem.getType() == newItem.getType()) {
                    final ItemMeta itemMeta = newItem.hasItemMeta() ? newItem.getItemMeta() : null;

                    currentItem.setItemMeta(itemMeta);
                    currentItem.setAmount(newItem.getAmount());

                    if (this.renderType == RenderType.PHYSICAL) {
                        final ItemStack currentVisibleItem = this.visibleInventory.getItem(i);

                        if (currentVisibleItem == null) {
                            this.visibleInventory.setItem(i, newItem.clone());
                        } else {
                            currentVisibleItem.setItemMeta(itemMeta);
                            currentVisibleItem.setAmount(newItem.getAmount());
                        }
                    }
                } else if (this.renderType == RenderType.PHYSICAL) {
                    this.visibleInventory.setItem(i, newItem.clone());
                }
                this.inventory.setItem(i, newItem.clone());
            }
            for (final HumanEntity viewer : this.visibleInventory.getViewers()) {
                if (!(viewer instanceof final Player player)) {
                    continue;
                }
                player.updateInventory();
            }
        } finally {
            this.itemLock.readLock().unlock();
        }
        this.itemLock.writeLock().lock();

        try {
            this.temporaryItems.clear();
            this.temporaryClickEvents.clear();
        } finally {
            this.itemLock.writeLock().unlock();
        }
        this.renderLock.lock();

        try {
            if (!Objects.equals(this.menu.properties().title(), this.previousTitle)) {
                final Inventory newInventory = this.createInventory();

                for (int i = 0; i < this.inventory.getSize(); i++) {
                    final ItemStack itemStack = this.inventory.getItem(i);

                    if (itemStack == null || itemStack.getType().isAir()) {
                        continue;
                    }
                    newInventory.setItem(i, itemStack);
                }
                final List<HumanEntity> viewers = new ArrayList<>(this.visibleInventory.getViewers());

                this.visibleInventory = newInventory;
                this.previousTitle = this.menu.properties().title();

                Bukkit.getScheduler().getMainThreadExecutor(this.plugin).execute(() -> {
                    for (final HumanEntity viewer : viewers) {
                        viewer.openInventory(this.visibleInventory);
                    }
                });
            }
        } finally {
            this.renderLock.unlock();
        }
    }

    @Override
    public boolean shouldReRender() {
        if (this.virtualInventory == null || this.rendering) {
            return false;
        }
        this.rendering = true;
        this.temporaryItems.clear();

        this.menu.update();
        this.menu.providers().update();
        this.menu.content().supply(this);

        this.rendering = false;

        final VirtualInventory newVirtualInventory = this.createVirtualInventory();

        return !this.virtualInventory.equals(newVirtualInventory);
    }

    @Override
    public @NotNull RenderType renderType() {
        return this.renderType;
    }

    @Override
    public void renderType(@NotNull final RenderType renderType) {
        if (renderType == this.renderType) {
            return;
        }
        this.renderType = renderType;

        if (this.inventory != null && this.visibleInventory != null) {
            this.inventory.clear();
            this.visibleInventory.clear();
            this.render();
        }
    }

    @Override
    public @NotNull RenderBoundary renderBoundary() {
        return this.renderBoundary;
    }

    @Override
    public void renderBoundary(@NotNull final RenderBoundary renderBoundary) {
        this.renderBoundary = renderBoundary;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.visibleInventory;
    }

    @Override
    public boolean isPerformingComparison() {
        return this.rendering;
    }

    public void clearComponents() {
        this.lastComponents.clear();
    }

    public @Nullable MenuComponent getLastComponent(final int slot) {
        return this.lastComponents.get(slot);
    }

    public @NotNull Map<Integer, UserInputComponent> getInputComponents() {
        return this.lastComponents.entrySet()
                .stream()
                .filter((entry) -> entry.getValue() instanceof UserInputComponent)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        (entry) -> (UserInputComponent) entry.getValue()
                ));
    }

    private @NotNull Inventory createInventory() {
        final Component title = this.menu.properties().title();

        if (title != null) {
            return Bukkit.createInventory(this.menu, 9 * this.menu.properties().height(), title);
        }
        return Bukkit.createInventory(this.menu, 9 * this.menu.properties().height());
    }

    private boolean isIndicator(@Nullable final ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }
        final ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) {
            return false;
        }
        final PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        return container.has(INDICATOR_KEY, PersistentDataType.STRING);
    }

    private enum InventoryClickType {
        PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL;
    }
}
