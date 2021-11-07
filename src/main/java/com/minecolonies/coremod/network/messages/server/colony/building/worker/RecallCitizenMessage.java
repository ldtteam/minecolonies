package com.minecolonies.coremod.network.messages.server.colony.building.worker;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.EntityUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Recalls the citizen to the hut. Created: May 26, 2014
 *
 * @author Colton
 */
public class RecallCitizenMessage extends AbstractBuildingServerMessage<IBuilding>
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
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final List<ICitizenData> citizens = new ArrayList<>(building.getAllAssignedCitizen());
        for (int i = 0; i < building.getAllAssignedCitizen().size(); i++)
        {
            Optional<AbstractEntityCitizen> optionalEntityCitizen = citizens.get(i).getEntity();
            final ICitizenData citizenData = citizens.get(i);
            if (!optionalEntityCitizen.isPresent())
            {
                if (citizenData != null)
                {
                    Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", colony.getID(), citizenData.getId()));
                    citizenData.setNextRespawnPosition(EntityUtils.getSpawnPoint(colony.getWorld(), building.getPosition()));
                    citizenData.updateEntityIfNecessary();
                }
                else
                {
                    Log.getLogger().warn("Citizen is AWOL and citizenData is null!");
                    return;
                }
            }
            else if (optionalEntityCitizen.get().getTicksExisted() == 0)
            {
                citizenData.getEntity().ifPresent(Entity::remove);
                citizenData.updateEntityIfNecessary();
            }

            final BlockPos loc = building.getPosition();
            if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), colony.getWorld(), loc))
            {
                final PlayerEntity player = ctxIn.getSender();
                if (player == null)
                {
                    return;
                }

                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.workerhuts.recallFail");
            }
        }
    }
}
