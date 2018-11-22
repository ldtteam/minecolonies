package com.minecolonies.coremod.event;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import com.minecolonies.coremod.items.ModItems;
import com.structurize.structures.client.TemplateRenderHandler;
import com.structurize.structures.helpers.Settings;
import com.structurize.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.Constants.BLOCKS_PER_CHUNK;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Used to handle client events.
 */
public class ClientEventHandler
{
    /**
     * List of all BlockPos in the colony border.
     */
    private static final List<BlockPos> colonyBorder = new ArrayList<>();

    /**
     * Seconds to show the citizen info sign.
     */
    private static final int SECONDS_TO_SHOW       = 5;

    /**
     * Intervals between border blocks to show.
     */
    private static final int BORDER_BLOCK_INTERVAL = 3;

    /**
     * The currently displayed citizen.
     */
    private CitizenDataView citizen = null;

    /**
     * The ticks passed since showing the sign.
     */
    private double ticksPassed = 0;

    /**
     * Cached wayPointTemplate.
     */
    private Template wayPointTemplate;

    /**
     * Cached wayPointTemplate.
     */
    private Template colonyBorderTemplate;

    /**
     * Cached wayPointTemplate.
     */
    private Template partolPointTemplate;

    /**
     * The colony view required here.
     */
    private ColonyView view = null;

    /**
     * Used to catch the renderWorldLastEvent in order to draw the debug nodes for pathfinding.
     *
     * @param event the catched event.
     */
    @SubscribeEvent
    public void renderWorldLastEvent(@NotNull final RenderWorldLastEvent event)
    {
        Pathfinding.debugDraw(event.getPartialTicks());

        final Structure structure = Settings.instance.getActiveStructure();
        final WorldClient world = Minecraft.getMinecraft().world;
        final EntityPlayer player = Minecraft.getMinecraft().player;

        if (structure != null)
        {
            if (Settings.instance.getStructureName().contains(AbstractEntityAIStructure.WAYPOINT_STRING))
            {
                final ColonyView tempView = ColonyManager.getClosestColonyView(world, player.getPosition());
                if (tempView != null)
                {
                    if (wayPointTemplate == null)
                    {
                        wayPointTemplate = new Structure(null,
                          "schematics/infrastructure/Waypoint",
                          new PlacementSettings().setRotation(BlockUtils.getRotation(Settings.instance.getRotation())).setMirror(Settings.instance.getMirror())).getTemplate();
                    }
                    TemplateRenderHandler.getInstance().drawTemplateAtListOfPositions(new ArrayList<>(tempView.getWayPoints()), event.getPartialTicks(), wayPointTemplate);
                }
            }
            else
            {
                final ColonyView tempView = ColonyManager.getClosestColonyView(world, player.getPosition());
                if (tempView != null)
                {
                    if (colonyBorder.isEmpty() || this.view == null || !tempView.getCenter().equals(this.view.getCenter()))
                    {
                        calculateColonyBorder(world, tempView);
                    }
                    this.view = tempView;

                    if (colonyBorderTemplate == null)
                    {
                        colonyBorderTemplate = new Structure(null,
                          "schematics/infrastructure/BorderBlock",
                          new PlacementSettings().setRotation(BlockUtils.getRotation(Settings.instance.getRotation())).setMirror(Settings.instance.getMirror())).getTemplate();
                    }
                }
                if (!colonyBorder.isEmpty() && colonyBorderTemplate != null)
                {
                    TemplateRenderHandler.getInstance().drawTemplateAtListOfPositions(colonyBorder, event.getPartialTicks(), colonyBorderTemplate);
                }
            }
            return;
        }
        else if (player.getHeldItemMainhand().getItem() == ModItems.scepterGuard)
        {
            final ItemStack stack = player.getHeldItemMainhand();
            if (!stack.hasTagCompound())
            {
                return;
            }
            final NBTTagCompound compound = stack.getTagCompound();

            final ColonyView colony = ColonyManager.getColonyView(compound.getInteger(TAG_ID));
            if (colony == null)
            {
                return;
            }

            final BlockPos guardTower = BlockPosUtil.readFromNBT(compound, TAG_POS);
            final AbstractBuildingView hut = colony.getBuilding(guardTower);

            if (partolPointTemplate == null)
            {
                partolPointTemplate = new Structure(null,
                  "schematics/infrastructure/PatrolPoint",
                  new PlacementSettings().setRotation(BlockUtils.getRotation(Settings.instance.getRotation())).setMirror(Settings.instance.getMirror())).getTemplate();
            }

            if (hut instanceof AbstractBuildingGuards.View)
            {
                TemplateRenderHandler.getInstance()
                  .drawTemplateAtListOfPositions(((AbstractBuildingGuards.View) hut).getPatrolTargets().stream().map(BlockPos::up).collect(Collectors.toList()),
                    event.getPartialTicks(),
                    partolPointTemplate);
            }
        }
        else
        {
            if (citizen != null)
            {
                final Entity entityCitizen = world.getEntityByID(citizen.getEntityId());
                if (entityCitizen instanceof EntityCitizen)
                {
                    //TODO: Reimplement status rendering.
                    ticksPassed += event.getPartialTicks();
                    if (ticksPassed > Constants.TICKS_SECOND * SECONDS_TO_SHOW)
                    {
                        ticksPassed = 0;
                        citizen = null;
                    }
                }
                else
                {
                    citizen = null;
                    ticksPassed = 0;
                }

                return;
            }

            final ColonyView colony = ColonyManager.getClosestColonyView(world, player.getPosition());
            if (colony != null && player != null && colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
            {
                for (final CitizenDataView citizenDataView : new ArrayList<CitizenDataView>(colony.getCitizens().values()))
                {
                    final Entity entityCitizen = world.getEntityByID(citizenDataView.getEntityId());
                    if (entityCitizen instanceof EntityCitizen && entityCitizen.getPosition().distanceSq(player.getPosition()) <= 2)
                    {
                        //TODO: Reimplement sign rendering.
                        citizen = citizenDataView;
                        return;
                    }
                }
            }
        }
        colonyBorder.clear();
    }

    private void calculateColonyBorder(final WorldClient world, final ColonyView view)
    {
        final Chunk chunk = world.getChunk(view.getCenter());
        final BlockPos center = new BlockPos(chunk.x * BLOCKS_PER_CHUNK + BLOCKS_PER_CHUNK / 2, view.getCenter().getY(), chunk.z * BLOCKS_PER_CHUNK + BLOCKS_PER_CHUNK / 2);

        final int range = (Configurations.gameplay.workingRangeTownHallChunks * BLOCKS_PER_CHUNK) + (BLOCKS_PER_CHUNK / 2);
        for (int i = 0; i < (Configurations.gameplay.workingRangeTownHallChunks * 2 + 1) * BLOCKS_PER_CHUNK;i++)
        {
            if (i % BORDER_BLOCK_INTERVAL == 0)
            {
                colonyBorder.add(BlockPosUtil.findLand(new BlockPos(center.getX() - range + i, center.getY(), center.getZ() + range - 1), world).up());
                colonyBorder.add(BlockPosUtil.findLand(new BlockPos(center.getX() - range + i, center.getY(), center.getZ() - range), world).up());
                colonyBorder.add(BlockPosUtil.findLand(new BlockPos(center.getX() + range - 1, center.getY(), center.getZ() - range + i), world).up());
                colonyBorder.add(BlockPosUtil.findLand(new BlockPos(center.getX() - range, center.getY(), center.getZ() - range + i), world).up());
            }
        }
    }
}
