package com.minecolonies.coremod.event;

import com.ldtteam.blockout.Log;
import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.client.StructureClientHandler;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.management.Structures;
import com.ldtteam.structurize.network.messages.SchematicRequestMessage;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.RenderUtils;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.sounds.ModSoundEvents;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.views.EmptyView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.PostBox;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import com.minecolonies.coremod.items.ItemBannerRallyGuards;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.antlr.v4.runtime.misc.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.CitizenConstants.WAYPOINT_STRING;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;
import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Used to handle client events.
 */
@OnlyIn(Dist.CLIENT)
public class ClientEventHandler
{
    private static final String MOB_SOUND_EVENT_PREFIX = "mob.";

    /**
     * The distance in which previews of nearby buildings are rendered
     */
    private static final double PREVIEW_RANGE = 25.0f;

    /**
     * Cached wayPointBlueprint.
     */
    private static Blueprint wayPointTemplate;

    /**
     * Cached wayPointBlueprint.
     */
    private static Blueprint partolPointTemplate;

    /**
     * The cached map of blueprints of nearby buildings that are rendered.
     */
    private static Map<BlockPos, Triple<Blueprint, BlockPos, BlockPos>> blueprintCache = new HashMap<>();

    /**
     * Render buffers.
     */
    public static final RenderTypeBuffers renderBuffers = new RenderTypeBuffers();
    private static final IRenderTypeBuffer.Impl renderBuffer = renderBuffers.getBufferSource();
    private static final Supplier<IVertexBuilder> linesWithCullAndDepth = () -> renderBuffer.getBuffer(RenderType.getLines());
    private static final Supplier<IVertexBuilder> linesWithoutCullAndDepth = () -> renderBuffer.getBuffer(RenderUtils.LINES_GLINT);

    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        if (MineColonies.getConfig().getClient().pathfindingDebugDraw.get())
        {
            Pathfinding.debugDraw(event.getPartialTicks(), event.getMatrixStack());
        }
        final Blueprint structure = Settings.instance.getActiveStructure();
        final ClientWorld world = Minecraft.getInstance().world;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (structure != null)
        {
            handleRenderStructure(event, world, player);
        }

        if (player.getHeldItemMainhand().getItem() == ModItems.scepterGuard)
        {
            handleRenderScepterGuard(event, world, player);
        }
        else if (player.getHeldItemMainhand().getItem() == ModItems.bannerRallyGuards)
        {
            handleRenderBannerRallyGuards(event, world, player);
        }
        else if (player.getHeldItemMainhand().getItem() == com.ldtteam.structurize.items.ModItems.buildTool.get())
        {
            handleRenderBuildTool(event, world, player);
        }

        DebugRendererChunkBorder.renderWorldLastEvent(event);

        renderBuffer.finish();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlaySoundEvent(final PlaySoundEvent event)
    {
        if (event.getSound() == null)
        {
            return;
        }

        if (event.getSound().getSoundLocation().getNamespace().equals(Constants.MOD_ID)
            && !MinecoloniesAPIProxy.getInstance().getConfig().getClient().citizenVoices.get())
        {
            final String path = event.getSound().getSoundLocation().getPath();
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
                event.setResultSound(null);
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
        if(event.getPlayer() == null)
        {
            return;
        }
        IColony colony = IMinecoloniesAPI.getInstance().getColonyManager().getIColony(event.getPlayer().world, event.getPlayer().getPosition());
        if(colony == null)
        {
            colony = IMinecoloniesAPI.getInstance().getColonyManager().getIColonyByOwner(event.getPlayer().world, event.getPlayer());
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
    private static void handleCrafterRecipeTooltips(@Nullable final IColony colony, final List<ITextComponent> toolTip, final Item item)
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
            final String crafterCapitalized = rec.getCrafter().substring(0, 1).toUpperCase(Locale.ROOT) + rec.getCrafter().substring(1);
            if(rec.getMinBuildingLevel() > 0)
            {
                final IFormattableTextComponent reqLevelText = new TranslationTextComponent(COM_MINECOLONIES_COREMOD_ITEM_BUILDLEVEL_TOOLTIP_GUI, crafterCapitalized, rec.getMinBuildingLevel());
                if(colony != null && colony.hasBuilding(rec.getCrafter(), rec.getMinBuildingLevel(), true))
                {
                    reqLevelText.setStyle(Style.EMPTY.setFormatting(TextFormatting.AQUA));
                }
                else
                {
                    reqLevelText.setStyle(Style.EMPTY.setFormatting(TextFormatting.RED));
                }
                toolTip.add(reqLevelText);
            }
            else
            {
                final IFormattableTextComponent reqBuildingTxt = new TranslationTextComponent(COM_MINECOLONIES_COREMOD_ITEM_AVAILABLE_TOOLTIP_GUI, crafterCapitalized)
                .setStyle(Style.EMPTY.setItalic(true).setFormatting(TextFormatting.GRAY));
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
                    final TextFormatting researchFormat;
                    if (colony != null && (colony.getResearchManager().getResearchTree().hasCompletedResearch(rec.getRequiredResearchId()) ||
                                             colony.getResearchManager().getResearchEffects().getEffectStrength(rec.getRequiredResearchId()) > 0))
                    {
                        researchFormat = TextFormatting.AQUA;
                    }
                    else
                    {
                        researchFormat = TextFormatting.RED;
                    }

                    for (IGlobalResearch research : researches)
                    {
                        toolTip.add(new TranslationTextComponent(COM_MINECOLONIES_COREMOD_ITEM_REQUIRES_RESEARCH_TOOLTIP_GUI,
                          research.getName()).setStyle(Style.EMPTY.setFormatting(researchFormat)));
                    }
                }
            }
        }
    }

    /**
     * Display research-related information on MineColonies Building hut blocks.
     * While this test can handle other non-hut blocks, research can only currently effect AbstractHutBlocks.
     * @param colony   The colony to check against, if one is present.
     * @param tooltip  The tooltip to add the text onto.
     * @param block    The hut block
     */
    private static void handleHutBlockResearchUnlocks(final IColony colony, final List<ITextComponent> tooltip, final Block block)
    {
        if (colony == null)
        {
            return;
        }
        final ResourceLocation effectId = colony.getResearchManager().getResearchEffectIdFrom(block.getBlock());
        if (colony.getResearchManager().getResearchEffects().getEffectStrength(effectId) > 0)
        {
            return;
        }
        if (MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().getResearchForEffect(effectId) != null)
        {
            tooltip.add(new TranslationTextComponent(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_1, block.getBlock().getTranslatedName()));
            tooltip.add(new TranslationTextComponent(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_2, block.getBlock().getTranslatedName()));
        }
    }

    /**
     * Renders building bounding boxes into the client
     *
     * @param event  The caught event
     * @param world  The world in which to render
     * @param player The player for which to render
     */
    private static void handleRenderBuildTool(@NotNull final RenderWorldLastEvent event, final ClientWorld world, final PlayerEntity player)
    {
        if (Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        final IColonyView colony = IColonyManager.getInstance().getClosestColonyView(world, new BlockPos(player.getPositionVec()));
        if (colony == null)
        {
            return;
        }

        final BlockPos activePosition = Settings.instance.getPosition();
        final Map<BlockPos, Triple<Blueprint, BlockPos, BlockPos>> newCache = new HashMap<>();
        for (final IBuildingView buildingView : colony.getBuildings())

        {
            if (MinecoloniesAPIProxy.getInstance().getConfig().getClient().neighborbuildingrendering.get())
            {
                if (buildingView instanceof PostBox.View || buildingView instanceof EmptyView)
                {
                    continue;
                }
                final BlockPos currentPosition = buildingView.getPosition();

                if (activePosition.withinDistance(currentPosition, PREVIEW_RANGE))
                {
                    if (blueprintCache.containsKey(currentPosition))
                    {
                        newCache.put(currentPosition, blueprintCache.get(currentPosition));
                    }
                    else
                    {
                        final StructureName sn =
                                new StructureName(Structures.SCHEMATICS_PREFIX,
                                        buildingView.getStyle(),
                                        buildingView.getSchematicName() + buildingView.getBuildingMaxLevel());

                        final String structureName = sn.toString();
                        final String md5 = Structures.getMD5(structureName);

                        final IStructureHandler wrapper = new LoadOnlyStructureHandler(world, buildingView.getID(), structureName, new PlacementSettings(), true);
                        if (!wrapper.hasBluePrint() || !wrapper.isCorrectMD5(md5))
                        {
                            Log.getLogger().debug("Blueprint error, requesting" + structureName + " from server.");
                            if (ServerLifecycleHooks.getCurrentServer() == null)
                            {
                                Network.getNetwork().sendToServer(new SchematicRequestMessage(structureName));
                                continue;
                            }
                        }

                        final Blueprint blueprint = wrapper.getBluePrint();

                        if (blueprint != null)
                        {
                            final Mirror mirror = buildingView.isMirrored() ? Mirror.FRONT_BACK : Mirror.NONE;
                            blueprint.rotateWithMirror(BlockPosUtil.getRotationFromRotations(buildingView.getRotation()),
                                    mirror,
                                    world);

                            final BlockPos primaryOffset = blueprint.getPrimaryBlockOffset();
                            final BlockPos pos = currentPosition.subtract(primaryOffset);
                            final BlockPos size = new BlockPos(blueprint.getSizeX(), blueprint.getSizeY(), blueprint.getSizeZ());
                            final BlockPos renderSize = pos.add(size).subtract(new BlockPos(1, 1, 1));
                            blueprint.setRenderSource(buildingView.getID());

                            if (buildingView.getBuildingLevel() < buildingView.getBuildingMaxLevel())
                            {
                                newCache.put(currentPosition, new Triple<>(blueprint, pos, renderSize));
                            }
                            else
                            {
                                newCache.put(currentPosition, new Triple<>(null, pos, renderSize));
                            }
                        }
                    }
                }
            }
        }

        blueprintCache = newCache;

        for (final Map.Entry<BlockPos, Triple<Blueprint, BlockPos, BlockPos>> nearbyBuilding : blueprintCache.entrySet())
        {
            final Triple<Blueprint, BlockPos, BlockPos> buildingData = nearbyBuilding.getValue();
            final BlockPos position = nearbyBuilding.getKey();
            if (buildingData.a != null)
            {
                StructureClientHandler.renderStructureAtPos(buildingData.a,
                  event.getPartialTicks(),
                  position,
                  event.getMatrixStack());
            }

            RenderUtils.renderBox(buildingData.b, buildingData.c, 0, 0, 1, 1.0F, 0.002D, event.getMatrixStack(), linesWithCullAndDepth.get());
        }
    }

    /**
     * Renders structures into the client
     *
     * @param event  The caught event
     * @param world  The world in which to render
     * @param player The player for which to render
     */
    private static void handleRenderStructure(@NotNull final RenderWorldLastEvent event, final ClientWorld world, final PlayerEntity player)
    {
        final PlacementSettings settings = new PlacementSettings(Settings.instance.getMirror(), BlockPosUtil.getRotationFromRotations(Settings.instance.getRotation()));
        if (Settings.instance.getStructureName() != null && Settings.instance.getStructureName().contains(WAYPOINT_STRING))
        {
            final IColonyView tempView = IColonyManager.getInstance().getClosestColonyView(world, new BlockPos(player.getPositionVec()));
            if (tempView != null)
            {
                if (wayPointTemplate == null)
                {
                    wayPointTemplate = new LoadOnlyStructureHandler(world, BlockPos.ZERO, "schematics/infrastructure/waypoint", settings, true).getBluePrint();
                }
                StructureClientHandler.renderStructureAtPosList(Settings.instance.getActiveStructure().hashCode() == wayPointTemplate.hashCode() ? Settings.instance.getActiveStructure() : wayPointTemplate,
                    event.getPartialTicks(),
                    new ArrayList<>(tempView.getWayPoints().keySet()),
                    event.getMatrixStack());
            }
        }
    }

    /**
     * Renders the guard scepter objects into the world.
     *
     * @param event  The caught event
     * @param world  The world in which to render
     * @param player The player for which to render
     */
    private static void handleRenderScepterGuard(@NotNull final RenderWorldLastEvent event, final ClientWorld world, final PlayerEntity player)
    {
        final PlacementSettings settings = new PlacementSettings(Settings.instance.getMirror(), BlockPosUtil.getRotationFromRotations(Settings.instance.getRotation()));
        final ItemStack stack = player.getHeldItemMainhand();
        if (!stack.hasTag())
        {
            return;
        }
        final CompoundNBT compound = stack.getTag();

        final IColonyView colony = IColonyManager.getInstance().getColonyView(compound.getInt(TAG_ID), player.world.getDimensionKey());
        if (colony == null)
        {
            return;
        }

        final BlockPos guardTower = BlockPosUtil.read(compound, TAG_POS);
        final IBuildingView hut = colony.getBuilding(guardTower);

        if (partolPointTemplate == null)
        {
            partolPointTemplate = new LoadOnlyStructureHandler(world, hut.getPosition(), "schematics/infrastructure/patrolpoint", settings, true).getBluePrint();
        }

        if (hut instanceof AbstractBuildingGuards.View)
        {
            StructureClientHandler.renderStructureAtPosList(partolPointTemplate, event.getPartialTicks(),((AbstractBuildingGuards.View) hut).getPatrolTargets().stream().map(BlockPos::up).collect(Collectors.toList()), event.getMatrixStack());
        }
    }

    /**
     * Renders the rallying banner guard tower indicators into the world.
     *
     * @param event  The caught event
     * @param world  The world in which to render
     * @param player The player for which to render
     */
    private static void handleRenderBannerRallyGuards(@NotNull final RenderWorldLastEvent event, final ClientWorld world, final PlayerEntity player)
    {
        final ItemStack stack = player.getHeldItemMainhand();

        final List<ILocation> guardTowers = ItemBannerRallyGuards.getGuardTowerLocations(stack);

        for (final ILocation guardTower : guardTowers)
        {
            if (world.getDimensionKey() != guardTower.getDimension())
            {
                RenderUtils.renderBox(guardTower.getInDimensionLocation(), guardTower.getInDimensionLocation(), 0, 0, 0, 1.0F, 0.002D, event.getMatrixStack(), linesWithCullAndDepth.get());
            }
        }
    }
}
