package com.minecolonies.coremod.fixers;

import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class EventBasedIdFixer
{
    @SubscribeEvent
    public static void onItemRegistryMissingMappings(final RegistryEvent.MissingMappings<Item> event)
    {
        Log.getLogger().warn("Remapping of minecolonies items started.");
        final int remappedCount = event.getMappings().stream().mapToInt(missingMapping -> {
            if (missingMapping.key.getNamespace().equals(Constants.MOD_ID))
            {
                final String path = missingMapping.key.getPath();
                final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("structurize", path));
                if (item != null && item != Items.AIR)
                {
                    missingMapping.remap(item);
                    return 1;
                }
            }
            return 0;
        }).sum();
        Log.getLogger().warn("Remapping completed. Remapped: " + remappedCount + " entries.");
    }

    @SubscribeEvent
    public static void onBlockRegistryMissingMappings(final RegistryEvent.MissingMappings<Block> event)
    {
        Log.getLogger().warn("Remapping of minecolonies blocks started.");
        final int remappedCount = event.getMappings().stream().mapToInt(missingMapping -> {
            if (missingMapping.key.getNamespace().equals(Constants.MOD_ID))
            {
                final String path = missingMapping.key.getPath();
                final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("structurize", path));
                if (block != null && block != Blocks.AIR)
                {
                    missingMapping.remap(block);
                    return 1;
                }
            }
            return 0;
        }).sum();
        Log.getLogger().warn("Remapping completed. Remapped: " + remappedCount + " entries.");
    }
}
