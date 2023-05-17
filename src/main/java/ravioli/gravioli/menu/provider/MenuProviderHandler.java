package ravioli.gravioli.menu.provider;

import org.jetbrains.annotations.NotNull;

public interface MenuProviderHandler {
    <T extends MenuProvider> @NotNull T register(@NotNull final T menuProvider);

    void init();

    void update();
}
