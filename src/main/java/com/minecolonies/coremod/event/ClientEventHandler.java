package com.minecolonies.coremod.event;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.coremod.entity.pathfinding.Pathfinding;
import com.minecolonies.coremod.util.RenderUtils;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
            final BlockPos position = Settings.instance.getPosition();
            if (Settings.instance.getStructureName().contains(AbstractEntityAIStructure.WAYPOINT_STRING))
            {
                RenderUtils.renderWayPoints(position, world, event.getPartialTicks());
            }
            else
            {
                RenderUtils.renderColonyBorder(position, world, event.getPartialTicks(), player, colonyBorder);
            }
            return;
        }
        else
        {
            final ColonyView colony = ColonyManager.getClosestColonyView(world, player.getPosition());
            if(colony != null && colony.getPermissions().hasPermission(player, Action.ACCESS_HUTS))
            {
                for(final CitizenDataView citizenDataView : colony.getCitizens().values())
                {
                    if(citizenDataView.get)
                }
                final CitizenDataView data = ((ColonyView) colony).getCitizen(1);
                if(data != null)
                {
                    RenderUtils.renderSigns(world, event.getPartialTicks(), data, player);
                }
            }
        }
        colonyBorder.clear();
    }
}
