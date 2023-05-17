package ravioli.gravioli.menu.content.impl;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.component.ImplicitMenuComponent;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.content.MenuContentProvider;
import ravioli.gravioli.menu.position.Position;
import ravioli.gravioli.menu.renderer.MenuRenderer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimpleMenuContentProvider implements MenuContentProvider {
    private final Map<Integer, MenuComponent> menuComponents = new HashMap<>();
    private final Set<ImplicitMenuComponent> implicitComponents = new HashSet<>();

    @Override
    public void set(final int x, final int y, @NotNull final MenuComponent menuComponent) {
        this.menuComponents.put(
            this.positionToSlot(x, y),
            menuComponent
        );
    }

    @Override
    public void set(final int slot, @NotNull final MenuComponent menuComponent) {
        this.menuComponents.put(slot, menuComponent);
    }

    @Override
    public void set(@NotNull final MenuComponent menuComponent, @NotNull final Position... positions) {
        for (final Position position : positions) {
            this.set(position.x(), position.y(), menuComponent);
        }
    }

    @Override
    public void add(@NotNull final ImplicitMenuComponent menuComponent) {
        this.implicitComponents.add(menuComponent);
    }

    @Override
    public void supply(@NotNull final MenuRenderer menuRenderer) {
        for (final Map.Entry<Integer, MenuComponent> entry : this.menuComponents.entrySet()) {
            menuRenderer.queue(entry.getKey(), entry.getValue());
        }
        for (final ImplicitMenuComponent implicitComponent : this.implicitComponents) {
            menuRenderer.queue(implicitComponent);
        }
        this.menuComponents.clear();
        this.implicitComponents.clear();
    }
}
