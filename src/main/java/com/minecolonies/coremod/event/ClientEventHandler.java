package com.minecolonies.coremod.event;

import com.google.common.collect.ImmutableMap;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.client.render.worldevent.ColonyBorderRenderer;
import com.minecolonies.coremod.client.render.worldevent.WorldEventContext;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ITEM_AVAILABLE_TOOLTIP_GUI;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ITEM_BUILDLEVEL_TOOLTIP_GUI;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_ITEM_REQUIRES_RESEARCH_TOOLTIP_GUI;
import static com.minecolonies.api.util.constant.translation.DebugTranslationConstants.*;

/**
 * Used to handle client events.
 */
@OnlyIn(Dist.CLIENT)
public class ClientEventHandler
{
    private static final String MOB_SOUND_EVENT_PREFIX = "mob.";

    /**
     * Lazy cache for crafting module lookups.
     */
    private static final Lazy<Map<String, BuildingEntry>> crafterToBuilding = Lazy.of(ClientEventHandler::buildCrafterToBuildingMap);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderWorldLastEvent(@NotNull final RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS)
        {
            return;
        }
        WorldEventContext.INSTANCE.renderWorldLastEvent(event);
    }

    @SubscribeEvent
    public static void onPlayerLogout(@NotNull final ClientPlayerNetworkEvent.LoggingOut event)
    {
        ColonyBorderRenderer.cleanup();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlaySoundEvent(final PlaySoundEvent event)
    {
        if (event.getSound() == null)
        {
            return;
        }

        if (event.getSound().getLocation().getNamespace().equals(Constants.MOD_ID)
            && !MinecoloniesAPIProxy.getInstance().getConfig().getClient().citizenVoices.get())
        {
            final String path = event.getSound().getLocation().getPath();
            if (!path.startsWith(MOB_SOUND_EVENT_PREFIX))
            {
                return;
            }
            final int secondDotPos = path.indexOf('.', MOB_SOUND_EVENT_PREFIX.length());
            if (secondDotPos == -1)
            {
                return;
            }
            final String mobName = path.substring(MOB_SOUND_EVENT_PREFIX.length(), secondDotPos);
            if (ModSoundEvents.CITIZEN_SOUND_EVENTS.containsKey(mobName))
            {
                event.setSound(null);
            }
        }
    }

    /**
     * Fires when an item tooltip is requested, generally from inventory, JEI, or when minecraft is first populating the recipe book.
     * @param event An ItemTooltipEvent
     */
    @SubscribeEvent
    public static void onItemTooltipEvent(final ItemTooltipEvent event)
    {
        // Vanilla recipe books populate tooltips once before the player exists on remote clients, some other cases.
        if(event.getEntity() == null)
        {
            return;
        }
        IColony colony = IMinecoloniesAPI.getInstance().getColonyManager().getIColony(event.getEntity().level, event.getEntity().blockPosition());
        if(colony == null)
        {
            colony = IMinecoloniesAPI.getInstance().getColonyManager().getIColonyByOwner(event.getEntity().level, event.getEntity());
        }
        handleCrafterRecipeTooltips(colony, event.getToolTip(), event.getItemStack().getItem());
        if(event.getItemStack().getItem() instanceof BlockItem)
        {
            final BlockItem blockItem = (BlockItem) event.getItemStack().getItem();
            if(blockItem.getBlock() instanceof AbstractBlockHut)
            {
                handleHutBlockResearchUnlocks(colony, event.getToolTip(), blockItem.getBlock());
            }
        }
    }

    /**
     * Display crafter recipe-related information on the client.
     * @param colony   The colony to check against, if one is present.
     * @param toolTip  The tooltip to add the text onto.
     * @param item     The item that will have the tooltip text added.
     */
    private static void handleCrafterRecipeTooltips(@Nullable final IColony colony, final List<Component> toolTip, final Item item)
    {
        final List<CustomRecipe> recipes = CustomRecipeManager.getInstance().getRecipeByOutput(item);
        if(recipes.isEmpty())
        {
            return;
        }

        for(CustomRecipe rec : recipes)
        {
            if(!rec.getShowTooltip() || rec.getCrafter().length() < 2)
            {
                continue;
            }
            final BuildingEntry craftingBuilding = crafterToBuilding.get().get(rec.getCrafter());
            if (craftingBuilding == null) continue;
            final Component craftingBuildingName = getFullBuildingName(craftingBuilding);
            if (rec.getMinBuildingLevel() > 0)
            {
                final String schematicName = craftingBuilding.getRegistryName().getPath();
                // the above is not guaranteed to match (and indeed doesn't for a few buildings), but
                // does match for all currently interesting crafters, at least.  there doesn't otherwise
                // appear to be an easy way to get the schematic name from a BuildingEntry ... or
                // unless we can change how colony.hasBuilding uses its parameter...

                final MutableComponent reqLevelText = Component.translatable(COM_MINECOLONIES_COREMOD_ITEM_BUILDLEVEL_TOOLTIP_GUI, craftingBuildingName, rec.getMinBuildingLevel());
                if(colony != null && colony.hasBuilding(schematicName, rec.getMinBuildingLevel(), true))
                {
                    reqLevelText.setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA));
                }
                else
                {
                    reqLevelText.setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                }
                toolTip.add(reqLevelText);
            }
            else
            {
                final MutableComponent reqBuildingTxt = Component.translatable(COM_MINECOLONIES_COREMOD_ITEM_AVAILABLE_TOOLTIP_GUI, craftingBuildingName)
                    .setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY));
                toolTip.add(reqBuildingTxt);
            }
            if(rec.getRequiredResearchId() != null)
            {
                final Set<IGlobalResearch> researches;
                if(IMinecoloniesAPI.getInstance().getGlobalResearchTree().hasResearch(rec.getRequiredResearchId()))
                {
                    researches = new HashSet<>();
                    researches.add(IMinecoloniesAPI.getInstance().getGlobalResearchTree().getResearch(rec.getRequiredResearchId()));
                }
                else
                {
                    researches = IMinecoloniesAPI.getInstance().getGlobalResearchTree().getResearchForEffect(rec.getRequiredResearchId());
                }
                if(researches != null)
                {
                    final ChatFormatting researchFormat;
                    if (colony != null && (colony.getResearchManager().getResearchTree().hasCompletedResearch(rec.getRequiredResearchId()) ||
                                             colony.getResearchManager().getResearchEffects().getEffectStrength(rec.getRequiredResearchId()) > 0))
                    {
                        researchFormat = ChatFormatting.AQUA;
                    }
                    else
                    {
                        researchFormat = ChatFormatting.RED;
                    }

                    for (IGlobalResearch research : researches)
                    {
                        toolTip.add(Component.translatable(COM_MINECOLONIES_COREMOD_ITEM_REQUIRES_RESEARCH_TOOLTIP_GUI,
                                MutableComponent.create(research.getName())).setStyle(Style.EMPTY.withColor(researchFormat)));
                    }
                }
            }
        }
    }

    /**
     * Gets a string like "ModName Building Name" for the specified building entry.
     * @param building The building entry
     * @return The translated building name
     */
    private static Component getFullBuildingName(@NotNull final BuildingEntry building)
    {
        final String namespace = building.getBuildingBlock().getRegistryName().getNamespace();
        final String modName = ModList.get().getModContainerById(namespace)
                .map(m -> m.getModInfo().getDisplayName())
                .orElse(namespace);
        final Component buildingName = building.getBuildingBlock().getName();
        return Component.literal(modName + " ").append(buildingName);
    }

    /**
     * Builds a mapping from crafting module ids to the corresponding buildings.
     * @return The mapping
     */
    private static Map<String, BuildingEntry> buildCrafterToBuildingMap()
    {
        final ImmutableMap.Builder<String, BuildingEntry> builder = new ImmutableMap.Builder<>();
        for (final BuildingEntry building : IMinecoloniesAPI.getInstance().getBuildingRegistry())
        {
            building.getModuleProducers().stream()
                    .map(Supplier::get)
                    .filter(m -> m instanceof ICraftingBuildingModule)
                    .map(m -> (ICraftingBuildingModule) m)
                    .filter(m -> m.getCraftingJob() != null)
                    .forEach(crafting ->
                    {
                        builder.put(crafting.getCustomRecipeKey(), building);
                    });
        }
        return builder.build();
    }

    /**
     * Display research-related information on MineColonies Building hut blocks.
     * While this test can handle other non-hut blocks, research can only currently effect AbstractHutBlocks.
     * @param colony   The colony to check against, if one is present.
     * @param tooltip  The tooltip to add the text onto.
     * @param block    The hut block
     */
    private static void handleHutBlockResearchUnlocks(final IColony colony, final List<Component> tooltip, final Block block)
    {
        if (colony == null)
        {
            return;
        }
        final ResourceLocation effectId = colony.getResearchManager().getResearchEffectIdFrom(block);
        if (colony.getResearchManager().getResearchEffects().getEffectStrength(effectId) > 0)
        {
            return;
        }
        if (MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().getResearchForEffect(effectId) != null)
        {
            tooltip.add(Component.translatable(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_1, block.getName()));
            tooltip.add(Component.translatable(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_2, block.getName()));
        }
    }

    /**
     * Event when the debug screen is opened. Event gets called by displayed text on the screen, we only need it when f3 is clicked.
     */
    @SubscribeEvent
    public static void onDebugOverlay(final CustomizeGuiOverlayEvent.DebugText event)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.options.renderDebug)
        {
            final ClientLevel world = mc.level;
            final LocalPlayer player = mc.player;
            final BlockPos pos = new BlockPos(player.position());
            IColony colony = IColonyManager.getInstance().getIColony(world, pos);
            if (colony == null)
            {
                if (IColonyManager.getInstance().isFarEnoughFromColonies(world, pos))
                {
                    event.getLeft().add(Component.translatable(DEBUG_NO_CLOSE_COLONY).getString());
                    return;
                }
                colony = IColonyManager.getInstance().getClosestIColony(world, pos);

                if (colony == null)
                {
                    return;
                }

                event.getLeft()
                  .add(Component.translatable(DEBUG_NEXT_COLONY,
                    (int) Math.sqrt(colony.getDistanceSquared(pos)),
                    IColonyManager.getInstance().getMinimumDistanceBetweenTownHalls()).getString());
                return;
            }

            event.getLeft().add(colony.getName() + " : " + Component.translatable(DEBUG_BLOCKS_FROM_CENTER, (int) Math.sqrt(colony.getDistanceSquared(pos))).getString());
        }
    }
}
