package ravioli.gravioli.menu.util;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public final class ReflectionUtil {
    public static @NotNull ServerPlayer getServerPlayer(@NotNull final Player player) {
        final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

        try {
            final Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            final Method getHandleMethod = craftPlayerClass.getMethod("getHandle");

            return (ServerPlayer) getHandleMethod.invoke(player);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
