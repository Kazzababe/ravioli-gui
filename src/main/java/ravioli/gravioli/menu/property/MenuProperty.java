package ravioli.gravioli.menu.property;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface MenuProperty<T> {
    T get();

    void set(T value);

    void set(@NotNull Supplier<T> valueSupplier);
}
