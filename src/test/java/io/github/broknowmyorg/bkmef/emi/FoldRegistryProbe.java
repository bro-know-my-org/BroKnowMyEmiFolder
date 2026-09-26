package io.github.broknowmyorg.bkmef.emi;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public final class FoldRegistryProbe {
    private FoldRegistryProbe() {
    }

    public static void main(String[] args) throws Exception {
        FoldRegistry.reloadStaticGroups();
        FoldRegistry.add(id("old"), Component.literal("old"), (FoldMatcher) facts -> false,
            new FoldDisplayOptions(4, 0xFFAA0000));

        CountDownLatch staged = new CountDownLatch(1);
        CountDownLatch publish = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread reload = new Thread(() -> {
            try {
                FoldRegistry.rebuildTogether(() -> {
                    FoldRegistry.reloadStaticGroups();
                    FoldRegistry.add(id("new"), Component.literal("new"), (FoldMatcher) facts -> false,
                        new FoldDisplayOptions(4, 0xFF00AA00));
                    staged.countDown();
                    await(publish);
                });
            } catch (Throwable throwable) {
                failure.set(throwable);
            }
        });
        reload.start();
        await(staged);
        try {
            int visible = CompletableFuture.supplyAsync(FoldRegistry::groupCount).get(5, TimeUnit.SECONDS);
            expect(1, visible, "staged groups must not become visible before publication");
            expect(0xFFAA0000, FoldRegistry.defaultFillColor(id("old")),
                "active group must remain visible during staging");
        } finally {
            publish.countDown();
        }
        reload.join(5_000);
        if (reload.isAlive() || failure.get() != null) {
            throw new AssertionError("Fold rebuild did not complete", failure.get());
        }
        expect(1, FoldRegistry.groupCount(), "published groups");
        expect(0xFF00AA00, FoldRegistry.defaultFillColor(id("new")), "published group color");

        try {
            FoldRegistry.rebuildTogether(() -> {
                FoldRegistry.reloadStaticGroups();
                throw new IllegalStateException("failed script");
            });
            throw new AssertionError("Expected rebuild failure");
        } catch (IllegalStateException expected) {
            expect(1, FoldRegistry.groupCount(), "failed rebuild must preserve published groups");
            expect(0xFF00AA00, FoldRegistry.defaultFillColor(id("new")), "failed rebuild must preserve group");
        }

        CountDownLatch olderReady = new CountDownLatch(1);
        CountDownLatch olderRelease = new CountDownLatch(1);
        Thread older = new Thread(() -> FoldRegistry.rebuildTogether(() -> {
            FoldRegistry.reloadStaticGroups();
            FoldRegistry.add(id("older"), Component.literal("older"), (FoldMatcher) facts -> false,
                new FoldDisplayOptions(4, 0xFF0000AA));
            olderReady.countDown();
            await(olderRelease);
        }));
        older.start();
        await(olderReady);
        try {
            FoldRegistry.rebuildTogether(() -> {
                FoldRegistry.reloadStaticGroups();
                FoldRegistry.add(id("newest"), Component.literal("newest"), (FoldMatcher) facts -> false,
                    new FoldDisplayOptions(4, 0xFF00AAAA));
            });
        } finally {
            olderRelease.countDown();
        }
        older.join(5_000);
        if (older.isAlive()) {
            throw new AssertionError("Older rebuild did not complete");
        }
        expect(0xFF00AAAA, FoldRegistry.defaultFillColor(id("newest")),
            "older rebuild must not overwrite newer publication");
        System.out.println("Fold registry publication checks passed.");
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("bkmef_probe", path);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting for fold rebuild");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }

    private static void expect(int expected, int actual, String label) {
        if (expected != actual) {
            throw new AssertionError(label + ": expected " + expected + ", got " + actual);
        }
    }
}
