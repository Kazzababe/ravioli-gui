package ravioli.gravioli.menu.provider.impl;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.handler.MenuHandler;
import ravioli.gravioli.menu.provider.MenuProvider;
import ravioli.gravioli.menu.provider.MenuProviderHandler;

import java.util.ArrayList;
import java.util.List;

public class SimpleMenuProviderHandler implements MenuProviderHandler {
    private final MenuHandler menu;
    private final List<MenuProvider> menuProviders = new ArrayList<>();

    public SimpleMenuProviderHandler(@NotNull final MenuHandler menu) {
        this.menu = menu;
    }

    @Override
    public <T extends MenuProvider> @NotNull T register(@NotNull final T menuProvider) {
        this.menuProviders.add(menuProvider);

        return menuProvider;
    }

    @Override
    public void init() {
        this.menuProviders.forEach((menuProvider) -> menuProvider.init(this.menu));
    }

    @Override
    public void update() {
        this.menuProviders.forEach(MenuProvider::update);
    }
}
