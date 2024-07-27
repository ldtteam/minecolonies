package com.minecolonies.core.event;

import com.google.common.collect.ImmutableMap;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.interfaces.IBuildingBrowsableBlock;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.client.gui.WindowBuildingBrowser;
import com.minecolonies.core.client.render.worldevent.ColonyBorderRenderer;
import com.minecolonies.core.client.render.worldevent.WorldEventContext;
import com.minecolonies.core.colony.crafting.CustomRecipe;
import com.minecolonies.core.colony.crafting.CustomRecipeManager;
import com.minecolonies.core.util.DomumOrnamentumUtils;
import com.minecolonies.core.util.SchemAnalyzerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

import static com.minecolonies.api.sounds.ModSoundEvents.CITIZEN_SOUND_EVENT_PREFIX;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.DebugTranslationConstants.*;

/**
 * Used to handle client events.
 */
@OnlyIn(Dist.CLIENT)
public class ClientEventHandler
{
    /**
     * Lazy cache for crafting module lookups.
     */
    private static final Lazy<Map<String, BuildingEntry>> crafterToBuilding = Lazy.of(ClientEventHandler::buildCrafterToBuildingMap);

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderWorldLastEvent(@NotNull final RenderLevelStageEvent event)
    {
        WorldEventContext.INSTANCE.renderWorldLastEvent(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onwWorldTick(@NotNull final TickEvent.LevelTickEvent event)
    {
        if (event.level.isClientSide && event.phase == TickEvent.Phase.END && ColonyConstants.rand.nextInt(20) == 0)
        {
            WorldEventContext.INSTANCE.checkNearbyColony(event.level);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(@NotNull final ClientPlayerNetworkEvent.LoggingOut event)
    {
        ColonyBorderRenderer.cleanup();
        WindowBuildingBrowser.clearCache();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlaySoundEvent(final PlaySoundEvent event)
    {
        if (event.getSound() == null)
        {
            return;
        }

        final ResourceLocation soundLocation = event.getSound().getLocation();
        if (!MinecoloniesAPIProxy.getInstance().getConfig().getClient().citizenVoices.get()
              && soundLocation.getNamespace().equals(Constants.MOD_ID)
              && soundLocation.getPath().startsWith(CITIZEN_SOUND_EVENT_PREFIX)
        )
        {
            event.setSound(null);
        }
    }

    /**
     * Fires when an item tooltip is requested, generally from inventory, JEI, or when minecraft is first populating the recipe book.
     *
     * @param event An ItemTooltipEvent
     */
    @SubscribeEvent
    public static void onItemTooltipEvent(final ItemTooltipEvent event)
    {
        // Vanilla recipe books populate tooltips once before the player exists on remote clients, some other cases.
        if (event.getEntity() == null)
        {
            return;
        }
        IColony colony = IMinecoloniesAPI.getInstance().getColonyManager().getIColony(event.getEntity().level(), event.getEntity().blockPosition());
        if (colony == null)
        {
            colony = IMinecoloniesAPI.getInstance().getColonyManager().getIColonyByOwner(event.getEntity().level(), event.getEntity());
        }
        handleCrafterRecipeTooltips(colony, event.getToolTip(), event.getItemStack().getItem());
        if (event.getItemStack().getItem() instanceof BlockItem)
        {
            final BlockItem blockItem = (BlockItem) event.getItemStack().getItem();
            if (blockItem.getBlock() instanceof AbstractBlockHut)
            {
                handleHutBlockResearchUnlocks(colony, event.getToolTip(), blockItem.getBlock());
            }

            if (event.getEntity().isCreative() && InventoryUtils.hasItemInItemHandler(new InvWrapper(event.getEntity().getInventory()), ModItems.scanTool.get()))
            {
                int tier = SchemAnalyzerUtil.getBlockTier(blockItem.getBlock());

                if (DomumOrnamentumUtils.isDoBlock(blockItem.getBlock()))
                {
                    for (Block block : MaterialTextureData.deserializeFromItemStack(event.getItemStack()).getTexturedComponents().values())
                    {
                        tier = Math.max(tier, SchemAnalyzerUtil.getBlockTier(block));
                    }
                }

                event.getToolTip().add(Component.translatableEscape("com.minecolonies.coremod.tooltip.schematic.tier", tier));
            }
        }
    }

    /**
     * Display crafter recipe-related information on the client.
     *
     * @param colony  The colony to check against, if one is present.
     * @param toolTip The tooltip to add the text onto.
     * @param item    The item that will have the tooltip text added.
     */
    private static void handleCrafterRecipeTooltips(@Nullable final IColony colony, final List<Component> toolTip, final Item item)
    {
        final List<CustomRecipe> recipes = CustomRecipeManager.getInstance().getRecipeByOutput(item);
        if (recipes.isEmpty())
        {
            return;
        }

        final Map<BuildingEntry, Integer> minimumBuildingLevels = new HashMap<>();

        for (CustomRecipe rec : recipes)
        {
            if (!rec.getShowTooltip() || rec.getCrafter().length() < 2)
            {
                continue;
            }
            final BuildingEntry craftingBuilding = crafterToBuilding.get().get(rec.getCrafter());
            if (craftingBuilding == null)
            {
                continue;
            }
            minimumBuildingLevels.putIfAbsent(craftingBuilding, null);
            if (minimumBuildingLevels.get(craftingBuilding) == null || rec.getMinBuildingLevel() < minimumBuildingLevels.get(craftingBuilding))
            {
                minimumBuildingLevels.put(craftingBuilding, rec.getMinBuildingLevel());
            }
            for (final ResourceLocation id : rec.getRequiredResearchIds())
            {
                final Set<IGlobalResearch> researches;
                if (IMinecoloniesAPI.getInstance().getGlobalResearchTree().hasResearch(id))
                {
                    researches = new HashSet<>();
                    researches.add(IMinecoloniesAPI.getInstance().getGlobalResearchTree().getResearch(id));
                }
                else
                {
                    researches = IMinecoloniesAPI.getInstance().getGlobalResearchTree().getResearchForEffect(id);
                }
                if (researches != null)
                {
                    final ChatFormatting researchFormat;
                    if (colony != null && (colony.getResearchManager().getResearchTree().hasCompletedResearch(id) ||
                                             colony.getResearchManager().getResearchEffects().getEffectStrength(id) > 0))
                    {
                        researchFormat = ChatFormatting.AQUA;
                    }
                    else
                    {
                        researchFormat = ChatFormatting.RED;
                    }

                    for (IGlobalResearch research : researches)
                    {
                        toolTip.add(Component.translatableEscape(COM_MINECOLONIES_COREMOD_ITEM_REQUIRES_RESEARCH_TOOLTIP_GUI,
                          MutableComponent.create(research.getName())).setStyle(Style.EMPTY.withColor(researchFormat)));
                    }
                }
            }
        }

        for (final Entry<BuildingEntry, Integer> crafterBuildingCombination : minimumBuildingLevels.entrySet())
        {
            final Component craftingBuildingName = getFullBuildingName(crafterBuildingCombination.getKey());
            final Integer minimumLevel = crafterBuildingCombination.getValue();
            if (minimumLevel > 0)
            {
                final String schematicName = crafterBuildingCombination.getKey().getRegistryName().getPath();
                // the above is not guaranteed to match (and indeed doesn't for a few buildings), but
                // does match for all currently interesting crafters, at least.  there doesn't otherwise
                // appear to be an easy way to get the schematic name from a BuildingEntry ... or
                // unless we can change how colony.hasBuilding uses its parameter...

                final MutableComponent reqLevelText = Component.translatableEscape(COM_MINECOLONIES_COREMOD_ITEM_BUILDLEVEL_TOOLTIP_GUI, craftingBuildingName, minimumLevel);
                if (colony != null && colony.hasBuilding(schematicName, minimumLevel, true))
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
                final MutableComponent reqBuildingTxt = Component.translatableEscape(COM_MINECOLONIES_COREMOD_ITEM_AVAILABLE_TOOLTIP_GUI, craftingBuildingName)
                                                          .setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY));
                toolTip.add(reqBuildingTxt);
            }
        }
    }

    /**
     * Gets a string like "ModName Building Name" for the specified building entry.
     *
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
     *
     * @return The mapping
     */
    private static Map<String, BuildingEntry> buildCrafterToBuildingMap()
    {
        final ImmutableMap.Builder<String, BuildingEntry> builder = new ImmutableMap.Builder<>();
        for (final BuildingEntry building : IMinecoloniesAPI.getInstance().getBuildingRegistry())
        {
            for (final BuildingEntry.ModuleProducer moduleProducer : building.getModuleProducers())
            {
                final IBuildingModule module = BuildingEntry.produceModuleWithoutBuilding(moduleProducer.key);
                if (module instanceof ICraftingBuildingModule craftingBuildingModule && craftingBuildingModule.getCraftingJob() != null)
                {
                    builder.put(craftingBuildingModule.getCustomRecipeKey(), building);
                }
            }
        }
        return builder.build();
    }

    /**
     * Display research-related information on MineColonies Building hut blocks.
     * While this test can handle other non-hut blocks, research can only currently effect AbstractHutBlocks.
     *
     * @param colony  The colony to check against, if one is present.
     * @param tooltip The tooltip to add the text onto.
     * @param block   The hut block
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
            tooltip.add(Component.translatableEscape(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_1, block.getName()));
            tooltip.add(Component.translatableEscape(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_2, block.getName()));
        }
    }

    /**
     * Event when the debug screen is opened. Event gets called by displayed text on the screen, we only need it when f3 is clicked.
     */
    @SubscribeEvent
    public static void onDebugOverlay(final CustomizeGuiOverlayEvent.DebugText event)
    {
        final Minecraft mc = Minecraft.getInstance();

            final ClientLevel world = mc.level;
            final LocalPlayer player = mc.player;
            final BlockPos pos = player.blockPosition();
            IColony colony = IColonyManager.getInstance().getIColony(world, pos);
            if (colony == null)
            {
                if (IColonyManager.getInstance().isFarEnoughFromColonies(world, pos))
                {
                    event.getLeft().add(Component.translatableEscape(DEBUG_NO_CLOSE_COLONY).getString());
                    return;
                }
                colony = IColonyManager.getInstance().getClosestIColony(world, pos);

                if (colony == null)
                {
                    return;
                }

                event.getLeft()
                  .add(Component.translatableEscape(DEBUG_NEXT_COLONY,
                    (int) Math.sqrt(colony.getDistanceSquared(pos)),
                    IColonyManager.getInstance().getMinimumDistanceBetweenTownHalls()).getString());
                return;
            }

            event.getLeft().add(colony.getName() + " : " + Component.translatableEscape(DEBUG_BLOCKS_FROM_CENTER, (int) Math.sqrt(colony.getDistanceSquared(pos))).getString());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onUseItem(@NotNull final PlayerInteractEvent.RightClickItem event)
    {
        if (!event.getLevel().isClientSide())
        {
            return;
        }

        if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().getItem() instanceof BlockItem blockItem)
        {
            // due to a Forge bug, this event still triggers on right-clicking a block (and there are no properties on
            // the event itself to distinguish the two cases, even though there are likely-sounding ones), so we need
            // to filter that out
            if (Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() != HitResult.Type.MISS)
            {
                return;
            }

            final Block block = blockItem.getBlock();

            if (block instanceof IBuildingBrowsableBlock browsable && browsable.shouldBrowseBuildings(event))
            {
                MinecoloniesAPIProxy.getInstance().getBuildingDataManager().openBuildingBrowser(block);

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }
}
