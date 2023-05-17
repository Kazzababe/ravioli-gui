package ravioli.gravioli.menu.handler;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.content.MenuContentProvider;
import ravioli.gravioli.menu.property.MenuPropertyHandler;
import ravioli.gravioli.menu.provider.MenuProviderHandler;
import ravioli.gravioli.menu.renderer.MenuRenderer;

public interface MenuHandler {
    @NotNull MenuPropertyHandler properties();

    @NotNull MenuProviderHandler providers();

    @NotNull MenuRenderer renderer();

    @NotNull MenuContentProvider content();
}
