package ravioli.gravioli.menu.property.impl;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.Menu;
import ravioli.gravioli.menu.property.MenuProperty;

import java.util.function.Supplier;

public class SimpleMenuProperty<T> implements MenuProperty<T> {
    private final Menu menu;

    private T value;

    SimpleMenuProperty(@NotNull final Menu menu, final T defaultValue) {
        this.value = defaultValue;
        this.menu = menu;
    }

    @Override
    public T get() {
        return this.value;
    }

    @Override
    public void set(final T value) {
        this.value = value;

        if (this.menu.renderer().shouldReRender()) {
            this.menu.render();
        }
    }

    @Override
    public void set(@NotNull final Supplier<T> valueSupplier) {
        Bukkit.getScheduler().runTaskAsynchronously(this.menu.plugin(), () -> {
            final T newValue = valueSupplier.get();

            this.set(newValue);
        });
    }
}
