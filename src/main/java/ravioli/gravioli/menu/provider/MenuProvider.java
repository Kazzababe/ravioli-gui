package ravioli.gravioli.menu.provider;

import org.jetbrains.annotations.NotNull;
import ravioli.gravioli.menu.handler.MenuHandler;

public interface MenuProvider {
    void init(@NotNull MenuHandler menu);

    void update();
}
