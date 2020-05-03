package com.minecolonies.coremod.network.messages.server.colony.building.worker;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Optional;

/**
 * Recalls the citizen to the hut.
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class RecallCitizenMessage extends AbstractBuildingServerMessage<IBuildingWorker>
{
    /**
     * Empty public constructor.
     */
    public RecallCitizenMessage()
    {
        super();
    }

    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {

    }

    @Override
    protected void fromBytesOverride(final PacketBuffer buf)
    {

    }

    public RecallCitizenMessage(final IBuildingView building)
    {
        super(building);
    }

    @Override
    protected void onExecute(
      final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuildingWorker building)
    {
        if (building.getAssignedEntities() == null) return;

        for (int i = 0; i < building.getAssignedEntities().size(); i++)
        {
            Optional<AbstractEntityCitizen> optionalEntityCitizen = building.getAssignedEntities().get(i);
            final ICitizenData citizenData = building.getAssignedCitizen().get(i);
            if (!optionalEntityCitizen.isPresent())
            {
                if (citizenData != null)
                {
                    Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", colony.getID(), citizenData.getId()));
                    citizenData.updateCitizenEntityIfNecessary();
                }
                else
                {
                    Log.getLogger().warn("Citizen is AWOL and citizenData is null!");
                    return;
                }
            }
            else if (optionalEntityCitizen.get().getTicksExisted() == 0)
            {
                citizenData.getCitizenEntity().ifPresent(Entity::remove);
                citizenData.updateCitizenEntityIfNecessary();
            }

            final BlockPos loc = building.getPosition();
            if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), colony.getWorld(), loc))
            {
                final PlayerEntity player = ctxIn.getSender();
                if (player == null) return;

                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.workerhuts.recallFail");
            }
        }
    }
}
