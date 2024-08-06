package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.util.Log;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforgespi.language.IModFileInfo;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static net.minecraft.client.gui.components.PlayerFaceRenderer.*;

/**
 * Datagen for entity_icon
 */
public class DefaultEntityIconProvider implements DataProvider
{
    private final DataGenerator generator;

    public DefaultEntityIconProvider(@NotNull final DataGenerator generator)
    {
        this.generator = generator;
    }

    @NotNull
    @Override
    public String getName()
    {
        return "Default Citizen Icons";
    }

    private static boolean IsEntitySkin(@NotNull final ResourceLocation id)
    {
        return id.getPath().endsWith(".png") &&
                (id.getPath().startsWith("textures/entity/citizen/") || id.getPath().startsWith("textures/entity/raiders/"));
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput cache)
    {
        final PackOutput.PathProvider outputProvider = generator.getPackOutput().createPathProvider(PackOutput.Target.RESOURCE_PACK, "textures/entity_icon");

        final IModFileInfo modFileInfo = ModList.get().getModFileById(MOD_ID);
        try (final PackResources pack = ResourcePackLoader.createPackForMod(modFileInfo).openPrimary(new PackLocationInfo("mod/" + MOD_ID, Component.empty(), PackSource.BUILT_IN, Optional.empty())))
        {
            final List<CompletableFuture<?>> icons = new ArrayList<>();

            pack.listResources(PackType.CLIENT_RESOURCES, MOD_ID, "textures/entity", (id, stream) ->
            {
                if (IsEntitySkin(id))
                {
                    final ResourceLocation iconId = new ResourceLocation(id.getNamespace(),
                            id.getPath().replace("textures/entity/", "").replace(".png", ""));
                    icons.add(generateIcon(outputProvider, iconId, stream, cache));
                }
            });

            return CompletableFuture.allOf(icons.toArray(CompletableFuture[]::new));
        }
    }

    private CompletableFuture<?> generateIcon(@NotNull final PackOutput.PathProvider outputProvider,
                                              @NotNull final ResourceLocation id,
                                              @NotNull final IoSupplier<InputStream> inputSupplier,
                                              @NotNull final CachedOutput cache)
    {
        return CompletableFuture.runAsync(() ->
        {
            try
            {
                try (final NativeImage skin = NativeImage.read(inputSupplier.get()))
                {
                    try (final NativeImage icon = createIconForSkin(skin))
                    {
                        saveIcon(outputProvider, id, icon, cache);
                    }
                }
            }
            catch (final IOException e)
            {
                Log.getLogger().error("Failed to save file to {}", id, e);
            }

        }, Util.backgroundExecutor());
    }

    private static NativeImage createIconForSkin(@NotNull final NativeImage skin)
    {
        final NativeImage icon = new NativeImage(16, 16, false);

        skin.resizeSubRectTo(SKIN_HEAD_U, SKIN_HEAD_V, SKIN_HEAD_WIDTH, SKIN_HEAD_HEIGHT, icon);

        for (int i = 0; i < 16; ++i)
        {
            icon.blendPixel(0, i, 0x80000000);
            icon.blendPixel(15, i, 0x80000000);

            if (i > 0 && i < 15)
            {
                icon.blendPixel(i, 0, 0x80000000);
                icon.blendPixel(i, 15, 0x80000000);
            }
        }

        return icon;
    }

    private static void saveIcon(@NotNull final PackOutput.PathProvider outputProvider,
                                 @NotNull final ResourceLocation id,
                                 @NotNull final NativeImage icon,
                                 @NotNull final CachedOutput cache) throws IOException
    {
        final BufferedImage image;
        try (final ByteArrayInputStream stream = new ByteArrayInputStream(icon.asByteArray()))
        {
            image = ImageIO.read(stream);
        }

        // convert to 24-bit, to reduce file size a bit
        final BufferedImage optimized = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        optimized.getGraphics().drawImage(image, 0, 0, null);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final HashingOutputStream hashStream = new HashingOutputStream(Hashing.sha1(), outputStream);
        ImageIO.write(optimized, "PNG", hashStream);

        cache.writeIfNeeded(outputProvider.file(id, "png"), outputStream.toByteArray(), hashStream.hash());
    }
}
