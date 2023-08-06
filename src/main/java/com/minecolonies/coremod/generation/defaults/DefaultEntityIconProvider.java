package com.minecolonies.coremod.generation.defaults;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.resource.ResourcePackLoader;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    @Override
    public void run(@NotNull final CachedOutput cache) throws IOException
    {
        final DataGenerator.PathProvider outputProvider = generator.createPathProvider(DataGenerator.Target.RESOURCE_PACK, "textures/entity_icon");

        final IModFileInfo modFileInfo = ModList.get().getModFileById(MOD_ID);
        try (final PackResources pack = ResourcePackLoader.createPackForMod(modFileInfo))
        {
            for (final ResourceLocation skinId : pack.getResources(PackType.CLIENT_RESOURCES, MOD_ID,
                    "textures/entity", DefaultEntityIconProvider::IsEntitySkin))
            {
                try (final NativeImage skin = NativeImage.read(pack.getResource(PackType.CLIENT_RESOURCES, skinId)))
                {
                    try (final NativeImage icon = createIconForSkin(skin))
                    {
                        final ResourceLocation iconId = new ResourceLocation(skinId.getNamespace(),
                                skinId.getPath().replace("textures/entity/", "").replace(".png", ""));
                        saveIcon(outputProvider, iconId, icon, cache);
                    }
                }
            }
        }
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

    @SuppressWarnings("UnstableApiUsage")
    private static void saveIcon(@NotNull final DataGenerator.PathProvider outputProvider,
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
