package com.minecolonies.api.compatibility.gbook;

import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.items.ItemHandlerHelper;

import gigaherz.guidebook.client.BookRegistryEvent;

/**
 * This class is for guidebook integration
 */
@Mod.EventBusSubscriber
public class GbookEventHandler
{
    private static final String GBOOK_ID = "gbook";
    private static final String bookLocation = Constants.MOD_ID + ":book/minecolonies.xml";

    @GameRegistry.ItemStackHolder(value = GBOOK_ID + ":guidebook", nbt = "{Book:\"" + bookLocation + "\"}")
    public static ItemStack gbookStack;

    // Adds our book to gbook registry
    @SubscribeEvent
    @Optional.Method(modid = GBOOK_ID)
    public static void registerBook(final BookRegistryEvent event) {
        event.register(new ResourceLocation(bookLocation));
    }

    // Give one gbook per player on first join
    @SubscribeEvent
    @Optional.Method(modid = GBOOK_ID)
    public static void checkGbookGiven(final EntityJoinWorldEvent event)
    {
        final Entity entity = event.getEntity();
        final String bookPlayerTag = Constants.MOD_ID + ":gbookGiven";

        if (Configurations.gameplay.playerGetsGuidebookOnFirstJoin && entity instanceof PlayerEntity && !entity.getEntityWorld().isRemote && !entity.getTags().contains(bookPlayerTag))
        {
            ItemHandlerHelper.giveItemToPlayer((EntityPlayer) entity, gbookStack.copy());
            entity.addTag(bookPlayerTag);
        }
    }
}
