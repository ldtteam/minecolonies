package com.minecolonies.coremod.event;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structures.client.BlueprintHandler;
import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.util.BoxRenderer;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import com.minecolonies.coremod.items.ItemBannerRallyGuards;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.CitizenConstants.WAYPOINT_STRING;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Used to handle client events.
 */
@OnlyIn(Dist.CLIENT)
public class ClientEventHandler
{
    /**
     * Cached wayPointBlueprint.
     */
    private static Blueprint wayPointTemplate;

    /**
     * Cached wayPointBlueprint.
     */
    private static Blueprint partolPointTemplate;
    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public static void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        if (MineColonies.getConfig().getCommon().pathfindingDebugDraw.get())
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
        else if (player.getHeldItemMainhand().getItem() == ModItems.scepterGuard)
        {
            handleRenderScepterGuard(event, world, player);
        }
        else if (player.getHeldItemMainhand().getItem() == ModItems.bannerRallyGuards)
        {
            handleRenderBannerRallyGuards(event, world, player);
        }
    }

    private static void handleRenderStructure(@NotNull final RenderWorldLastEvent event, final ClientWorld world, final PlayerEntity player)
    {
        final PlacementSettings settings = new PlacementSettings(Settings.instance.getMirror(), BlockPosUtil.getRotationFromRotations(Settings.instance.getRotation()));
        if (Settings.instance.getStructureName() != null && Settings.instance.getStructureName().contains(WAYPOINT_STRING))
        {
            final IColonyView tempView = IColonyManager.getInstance().getClosestColonyView(world, player.getPosition());
            if (tempView != null)
            {
                if (wayPointTemplate == null)
                {
                    if (wayPointTemplate == null)
                    {
                        wayPointTemplate = new LoadOnlyStructureHandler(world, BlockPos.ZERO, "schematics/infrastructure/waypoint", settings, true).getBluePrint();
                    }
                    BlueprintHandler.getInstance().drawBlueprintAtListOfPositions(new ArrayList<>(tempView.getWayPoints().keySet()),
                        event.getPartialTicks(),
                        // hashcode is safe unless the template needs different rotations/mirrors
                        Settings.instance.getActiveStructure().hashCode() == wayPointTemplate.hashCode() ? Settings.instance.getActiveStructure() : wayPointTemplate,
                        event.getMatrixStack());
                }
                BlueprintHandler.getInstance()
                  .drawBlueprintAtListOfPositions(new ArrayList<>(tempView.getWayPoints().keySet()), event.getPartialTicks(), wayPointTemplate, event.getMatrixStack());
            }
        }
    }

    private static void handleRenderScepterGuard(@NotNull final RenderWorldLastEvent event, final ClientWorld world, final PlayerEntity player)
    {
        final PlacementSettings settings = new PlacementSettings(Settings.instance.getMirror(), BlockPosUtil.getRotationFromRotations(Settings.instance.getRotation()));
        final ItemStack stack = player.getHeldItemMainhand();
        if (!stack.hasTag())
        {
            return;
        }
        final CompoundNBT compound = stack.getTag();

        final IColonyView colony = IColonyManager.getInstance().getColonyView(compound.getInt(TAG_ID), player.world.getDimension().getType().getId());
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
            BlueprintHandler.getInstance()
              .drawBlueprintAtListOfPositions(((AbstractBuildingGuards.View) hut).getPatrolTargets().stream().map(BlockPos::up).collect(Collectors.toList()),
                event.getPartialTicks(),
                partolPointTemplate,
                event.getMatrixStack());
        }
    }

    private static void handleRenderBannerRallyGuards(@NotNull final RenderWorldLastEvent event, final ClientWorld world, final PlayerEntity player)
    {
        final ItemStack stack = player.getHeldItemMainhand();

        final List<ILocation> guardTowers = ItemBannerRallyGuards.getGuardTowerLocations(stack);

        for (final ILocation guardTower : guardTowers)
        {
            if (world.getDimension().getType().getId() != guardTower.getDimension())
            {
                continue;
            }
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            renderBox(guardTower.getInDimensionLocation(), guardTower.getInDimensionLocation(), event, 0, 0, 1);
            RenderSystem.enableDepthTest();
            RenderSystem.enableCull();
        }
    }

    // TODO: Use the Structurize function for this!
    private static void renderBox(
      final BlockPos posA,
      final BlockPos posB,
      final RenderWorldLastEvent event,
      final float red,
      final float green,
      final float blue)
    {
        int x1 = posA.getX();
        int y1 = posA.getY();
        int z1 = posA.getZ();

        int x2 = posB.getX();
        int y2 = posB.getY();
        int z2 = posB.getZ();

        if (x1 > x2)
        {
            x1++;
        }
        else
        {
            x2++;
        }

        if (y1 > y2)
        {
            y1++;
        }
        else
        {
            y2++;
        }

        if (z1 > z2)
        {
            z1++;
        }
        else
        {
            z2++;
        }

        RenderSystem.enableDepthTest();

        final ActiveRenderInfo activeRenderInfo = Minecraft.getInstance().getRenderManager().info;
        final Vec3d viewPosition = activeRenderInfo.getProjectedView();
        final MatrixStack matrix = event.getMatrixStack();
        matrix.push();
        matrix.translate(-viewPosition.x, -viewPosition.y, -viewPosition.z);

        final Matrix4f matrix4f = matrix.getLast().getPositionMatrix();
        final AxisAlignedBB axisalignedbb = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
        BoxRenderer.drawSelectionBoundingBox(matrix4f, axisalignedbb.grow(0.002D), red, green, blue, 1.0F);
        matrix.pop();

        RenderSystem.disableDepthTest();
    }
}
