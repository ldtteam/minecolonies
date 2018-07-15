package com.minecolonies.coremod.event;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import com.minecolonies.structures.client.TemplateRenderHandler;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
    private static final int SECONDS_TO_SHOW = 5;

    /**
     * The currently displayed citizen.
     */
    private CitizenDataView citizen = null;

    /**
     * The ticks passed since showing the sign.
     */
    private double ticksPassed = 0;

    /**
     * Waypoint template to be rendered.
     */
    private Template template;

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
                final ColonyView view = ColonyManager.getClosestColonyView(world, player.getPosition());
                if (view != null)
                {
                    final EntityPlayer perspectiveEntity = Minecraft.getMinecraft().player;
                    final double interpolatedEntityPosX = perspectiveEntity.lastTickPosX + (perspectiveEntity.posX - perspectiveEntity.lastTickPosX) * event.getPartialTicks();
                    final double interpolatedEntityPosY = perspectiveEntity.lastTickPosY + (perspectiveEntity.posY - perspectiveEntity.lastTickPosY) * event.getPartialTicks();
                    final double interpolatedEntityPosZ = perspectiveEntity.lastTickPosZ + (perspectiveEntity.posZ - perspectiveEntity.lastTickPosZ) * event.getPartialTicks();

                    if (template == null)
                    {
                        template = new Structure(null,
                      "schematics/infrastructure/Waypoint",
                      new PlacementSettings().setRotation(BlockUtils.getRotation(Settings.instance.getRotation())).setMirror(Settings.instance.getMirror())).getTemplate();
                    }
                    else
                    {
                        for (final BlockPos coord : view.getWayPoints())
                        {
                            final BlockPos pos = coord.down();
                            final double renderOffsetX = pos.getX() - interpolatedEntityPosX;
                            final double renderOffsetY = pos.getY() - interpolatedEntityPosY;
                            final double renderOffsetZ = pos.getZ() - interpolatedEntityPosZ;
                            final Vector3d renderOffset = new Vector3d();
                            renderOffset.x = renderOffsetX;
                            renderOffset.y = renderOffsetY;
                            renderOffset.z = renderOffsetZ;

                            TemplateRenderHandler.getInstance()
                              .draw(template, structure.getSettings().getRotation(), structure.getSettings().getMirror(), renderOffset);
                        }
                    }
                }
            }
            else
            {
                //TODO: Implement rendering of the colony border.
            }
            return;
        }
        else if (Settings.instance.getBox() != null)
        {
            final BlockPos posA = Settings.instance.getBox().getFirst();
            final BlockPos posB = Settings.instance.getBox().getSecond();

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

            final double renderPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)event.getPartialTicks();
            final double renderPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)event.getPartialTicks();
            final double renderPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)event.getPartialTicks();

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);

            final AxisAlignedBB axisalignedbb = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
            RenderGlobal.drawSelectionBoundingBox(axisalignedbb.grow(0.002D).offset(-renderPosX, -renderPosY, -renderPosZ), 1.0F, 1.0F, 1.0F, 1.0F);


            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
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
}
