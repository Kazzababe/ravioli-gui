package ravioli.gravioli.menu.property.impl;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.Menu;
import ravioli.gravioli.menu.property.MenuProperty;
import ravioli.gravioli.menu.property.MenuPropertyHandler;

import java.util.ArrayList;
import java.util.List;

public class SimpleMenuPropertyHandler implements MenuPropertyHandler {
    private final Menu menu;
    private final List<MenuProperty<?>> menuProperties = new ArrayList<>();

    private int height;
    private Component title;

    public SimpleMenuPropertyHandler(@NotNull final Menu menu) {
        this.menu = menu;
    }

    @Override
    public @NotNull <T> MenuProperty<T> create(final T value) {
        final MenuProperty<T> property = new SimpleMenuProperty<>(this.menu, value);

        this.menuProperties.add(property);

        return property;
    }

    @Override
    public void height(final int height) {
        this.height = height;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public void title(@NotNull final Component title) {
        this.title = title;
    }

    @Override
    public @NotNull Component title() {
        return this.title;
    }

    @Override
    public @NotNull List<MenuProperty<?>> properties() {
        return this.menuProperties;
    }
}
