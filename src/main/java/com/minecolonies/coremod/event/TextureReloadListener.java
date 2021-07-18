package com.minecolonies.coremod.event;

import com.ldtteam.blockout.Log;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

/**
 * Specific texture reload listener.
 */
@Mod.EventBusSubscriber(value= Dist.CLIENT, modid= Constants.MOD_ID, bus=MOD)
public class TextureReloadListener extends ReloadListener<TextureReloadListener.TexturePacks>
{
    /**
     * List of all texture packs available.
     */
    public static final List<String> TEXTURE_PACKS = new ArrayList<>();

    @NotNull
    @Override
    protected TexturePacks prepare(@NotNull final IResourceManager manager, @NotNull final IProfiler profiler)
    {
        final Set<String> set = new HashSet<>();
        final List<ResourceLocation> resLocs = new ArrayList<>(manager.listResources("textures/entity/citizen", f -> true));
        for (final ResourceLocation res : resLocs)
        {
            if (!res.getPath().contains("png"))
            {
                final String folder = res.getPath().replace("textures/entity/citizen", "").replace("/", "");
                if (!folder.isEmpty())
                {
                    set.add(folder);
                }
            }
        }

        final TexturePacks packs = new TexturePacks();
        packs.packs = new ArrayList<>(set);
        return packs;
    }

    @Override
    protected void apply(@NotNull final TexturePacks packs, @NotNull final IResourceManager manager, @NotNull final IProfiler profiler)
    {
       TextureReloadListener.TEXTURE_PACKS.clear();
       TextureReloadListener.TEXTURE_PACKS.addAll(packs.packs);
    }

    /**
     * Storage class to hand the texture packs from off-thread to the main thread.
     */
    public static class TexturePacks
    {
        public List<String> packs = new ArrayList<>();
    }

    @SubscribeEvent
    public static void modInitClient(final FMLConstructModEvent event)
    {
        event.enqueueWork(() -> ((SimpleReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(new TextureReloadListener()));
    }
}

