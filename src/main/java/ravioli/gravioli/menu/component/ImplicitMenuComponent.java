package ravioli.gravioli.menu.component;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.renderer.MenuRenderer;

/**
 * Implicit components are not associated with a specific slot in a menu but rather they
 * have no set position and affect content through other means.
 */
public interface ImplicitMenuComponent extends MenuComponent {
    void queue(@NotNull MenuRenderer renderer);

    @Override
    default void queue(@NotNull MenuRenderer renderer, int slot) {
        throw new UnsupportedOperationException("Implicit menu components cannot be applied to a slot.");
    }
}
