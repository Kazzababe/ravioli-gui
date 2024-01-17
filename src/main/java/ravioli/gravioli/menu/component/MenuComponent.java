package ravioli.gravioli.menu.component;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ravioli.gravioli.menu.component.input.ItemFilter;
import ravioli.gravioli.menu.component.input.UpdateAction;
import ravioli.gravioli.menu.mask.Mask;
import ravioli.gravioli.menu.renderer.MenuRenderer;

import java.util.List;
import java.util.function.Consumer;

public interface MenuComponent {
    static @NotNull MenuComponent item(@NotNull final ItemStack itemStack,
                                       @NotNull final Consumer<InventoryClickEvent> clickEventConsumer) {
        return new ItemMenuComponent(itemStack, clickEventConsumer);
    }

    static @NotNull MenuComponent item(@NotNull final ItemStack itemStack) {
        return new ItemMenuComponent(itemStack, null);
    }

    static @NotNull UserInputComponent userInput(@NotNull final ItemFilter itemFilter, final int shiftClickPriority,
                                                 @Nullable final UpdateAction updateAction) {
        return new UserInputComponent(itemFilter, updateAction, shiftClickPriority);
    }

    static @NotNull UserInputComponent userInput(@NotNull final ItemFilter itemFilter,
                                                 @Nullable final UpdateAction updateAction) {
        return userInput(itemFilter, 0, updateAction);
    }


    static @NotNull UserInputComponent userInput(@NotNull final ItemFilter itemFilter, final int shiftClickPriority) {
        return userInput(itemFilter, shiftClickPriority, null);
    }

    static @NotNull UserInputComponent userInput(@NotNull final ItemFilter itemFilter) {
        return userInput(itemFilter, null);
    }

    static @NotNull UserInputComponent userInput() {
        return userInput((itemStack) -> true);
    }

    static @NotNull FilledMaskComponent filledMask(@NotNull final Mask mask, @NotNull final ItemStack itemStack) {
        return new FilledMaskComponent(mask, itemStack);
    }

    static @NotNull GridComponent grid(@NotNull final Mask mask, @NotNull final List<MenuComponent> contents) {
        return new GridComponent(mask, contents);
    }

    void queue(@NotNull MenuRenderer renderer, int slot);
}
