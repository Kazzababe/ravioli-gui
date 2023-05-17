package ravioli.gravioli.menu.provider.impl;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ravioli.gravioli.menu.component.ImplicitMenuComponent;
import ravioli.gravioli.menu.component.MenuComponent;
import ravioli.gravioli.menu.handler.MenuHandler;
import ravioli.gravioli.menu.pagination.Mask;
import ravioli.gravioli.menu.property.MenuProperty;
import ravioli.gravioli.menu.provider.MenuProvider;
import ravioli.gravioli.menu.renderer.MenuRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AsyncPaginationMenuProvider implements MenuProvider, ImplicitMenuComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncPaginationMenuProvider.class);

    private final Mask mask;
    private final Function<Integer, CompletableFuture<PageData>> loadFunction;

    private MenuProperty<Integer> page;
    private MenuProperty<Boolean> hasNext;
    private MenuProperty<List<MenuComponent>> items;
    private transient boolean processing;

    public AsyncPaginationMenuProvider(@NotNull final Mask mask,
                                       @NotNull final PageProvider loadFunction) {
        this.mask = mask;
        this.loadFunction = (page) -> CompletableFuture.supplyAsync(() -> loadFunction.load(page))
                .exceptionally((e) -> {
                    LOGGER.error("Unable to load page.", e);

                    return new PageData(Collections.emptyList(), false);
                });
    }

    @Override
    public void queue(@NotNull final MenuRenderer renderer) {
        final Iterator<Integer> slotIterator = this.mask.iterator();
        final List<MenuComponent> pageComponents = this.items.get();

        for (final MenuComponent item : pageComponents) {
            if (!slotIterator.hasNext()) {
                break;
            }
            final int slot = slotIterator.next();

            renderer.queue(slot, item);
        }
    }

    @Override
    public void init(@NotNull final MenuHandler menu) {
        this.page = menu.properties().create(0);
        this.hasNext = menu.properties().create(false);
        this.items = menu.properties().create(new ArrayList<>());

        this.loadFunction.apply(0).thenAccept((pageData) -> {
            this.items.set(pageData.items);
            this.hasNext.set(pageData.hasNext);
        });
    }

    @Override
    public void update() {

    }

    public int page() {
        return this.page.get();
    }

    public void resetPage() {
        this._refresh(0);
    }

    public void refresh() {
        this._refresh(this.page.get());
    }

    private void _refresh(final int page) {
        this.processing = true;
        this.items.set(Collections.emptyList());

        this.loadFunction
                .apply(page)
                .thenAccept((pageData) -> {
                    this.items.set(pageData.items);
                    this.hasNext.set(pageData.hasNext);

                    if (this.page.get() != page) {
                        this.page.set(page);
                    }
                })
                .whenComplete((unused, throwable) -> this.processing = false);
    }

    public @NotNull CompletableFuture<Void> next() {
        if (this.processing) {
            return CompletableFuture.completedFuture(null);
        }
        this.processing = true;

        return this.loadFunction
                .apply(this.page() + 1)
                .thenAccept((pageData) -> {
                    this.items.set(pageData.items);
                    this.hasNext.set(pageData.hasNext);
                    this.page.set(this.page() + 1);
                })
                .exceptionally((e) -> {
                    e.printStackTrace();
                    return null;
                })
                .whenComplete((unused, throwable) -> this.processing = false);
    }

    public @NotNull CompletableFuture<Void> previous() {
        if (this.processing) {
            return CompletableFuture.completedFuture(null);
        }
        this.processing = true;

        return this.loadFunction
                .apply(this.page() - 1)
                .thenAccept((pageData) -> {
                    this.items.set(pageData.items);
                    this.hasNext.set(pageData.hasNext);
                    this.page.set(this.page() - 1);
                })
                .whenComplete((unused, throwable) -> this.processing = false);
    }

    public boolean hasNext() {
        return this.hasNext.get();
    }

    public boolean hasPrevious() {
        return this.page.get() > 0;
    }

    @FunctionalInterface
    public interface PageProvider {
        @NotNull PageData load(int page);
    }

    public record PageData(@NotNull List<MenuComponent> items, boolean hasNext) {

    }
}