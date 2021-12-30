package com.glisco.isometricrenders.screen;

import com.glisco.isometricrenders.util.ImageExporter;
import com.glisco.isometricrenders.util.RuntimeConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import static com.glisco.isometricrenders.IsometricRendersClient.prefix;
import static com.glisco.isometricrenders.util.RuntimeConfig.exportResolution;
import static com.glisco.isometricrenders.util.RuntimeConfig.useExternalRenderer;

public abstract class BatchIsometricRenderScreen<T> extends IsometricRenderScreen {

    protected final Iterator<T> renderObjects;
    protected final int delay;
    protected int delayTicks;
    protected boolean invalid = false;
    protected String name;

    public BatchIsometricRenderScreen(Iterator<T> renderObjects, String name) {
        this.renderObjects = renderObjects;
        this.drawBackground = true;
        this.name = name;

        if (ImageExporter.Threaded.busy()) {
            MinecraftClient.getInstance().player.sendMessage(prefix("§cThe threaded export system is not available, try again in a few seconds. If this doesn't fix itself, restart your client"), false);
            invalid = true;
        }

        ImageExporter.Threaded.init();

        if (useExternalRenderer && exportResolution > 2048 && !RuntimeConfig.allowInsaneResolutions) {
            MinecraftClient.getInstance().player.sendMessage(prefix("§cResolutions over 2048x2048 are not supported for batch-rendering. If you want to risk it, use §6/isorender insanity"), false);
            invalid = true;
        }

        delay = (int) Math.pow(exportResolution / 1024f, 2);
    }

    @Override
    protected void init() {
        super.init();
        children().stream().filter(element -> element instanceof ClickableWidget).forEach(element -> ((ClickableWidget) element).active = false);
    }

    @Override
    public void onClose() {
        super.onClose();
        ImageExporter.Threaded.finish();
    }

    @Override
    protected CompletableFuture<File> addImageToExportQueue(NativeImage image) {
        return ImageExporter.Threaded.submit(image, currentFilename);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (invalid) {
            onClose();
            return;
        }

        if (delayTicks > 0) {
            delayTicks--;
        }
        if (ImageExporter.Threaded.acceptsNew() && delayTicks == 0) {
            setupRender();
            delayTicks = delay;
        }
        super.render(matrices, mouseX, mouseY, delta);
    }

    protected abstract void setupRender();

}