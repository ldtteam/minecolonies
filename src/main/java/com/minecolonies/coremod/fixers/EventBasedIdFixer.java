package com.minecolonies.coremod.fixers;

import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class EventBasedIdFixer
{
    @SubscribeEvent
    public static void onItemRegistryMissingMappings(final RegistryEvent.MissingMappings<Item> event)
    {
        Log.getLogger().warn("Remapping of minecolonies items started.");
        final int remappedCount = onRegistryMissingMappings(event);
        Log.getLogger().warn("Remapping completed. Remapped: " + remappedCount + " entries.");
    }

    @SubscribeEvent
    public static void onBlockRegistryMissingMappings(final RegistryEvent.MissingMappings<Block> event)
    {
        Log.getLogger().warn("Remapping of minecolonies blocks started.");
        final int remappedCount = onRegistryMissingMappings(event);
        Log.getLogger().warn("Remapping completed. Remapped: " + remappedCount + " entries.");
    }


    private static <T extends IForgeRegistryEntry<T>> int onRegistryMissingMappings(final RegistryEvent.MissingMappings<T> event)
    {
        return event.getMappings().stream().mapToInt(missingMapping -> {
            final String path = missingMapping.key.getPath();
            final ResourceLocation remappedTargetId = new ResourceLocation(Constants.MOD_ID.toLowerCase() + ":" + path);

            @Nullable
            final T target = missingMapping.registry.getValue(remappedTargetId);
            if (target != null && target != Blocks.AIR && target != Items.AIR)
            {
                Log.getLogger().info("Remapping: " + missingMapping.key + " to: " + remappedTargetId);
                missingMapping.remap(target);
                return 1;
            }

            return 0;
        }).sum();
    }
}
