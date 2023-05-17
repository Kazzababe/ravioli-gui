package ravioli.gravioli.menu.property;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface MenuPropertyHandler {
    <T> @NotNull MenuProperty<T> create(final T value);

    void height(int height);

    int height();

    void title(@NotNull Component title);

    @Nullable Component title();

    @NotNull Collection<MenuProperty<?>> properties();
}
