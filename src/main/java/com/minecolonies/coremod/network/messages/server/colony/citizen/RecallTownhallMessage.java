package com.minecolonies.coremod.network.messages.server.colony.citizen;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Used to handle citizen recalls to the townhall.
 */
public class RecallTownhallMessage extends AbstractColonyServerMessage
{
    /**
     * Empty public constructor.
     */
    public RecallTownhallMessage()
    {
        super();
    }

    public RecallTownhallMessage(final IColony colony)
    {
        super(colony);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        @Nullable final ITownHall building = colony.getBuildingManager().getTownHall();
        if (building != null)
        {
            final BlockPos location = building.getPosition();
            final World world = colony.getWorld();
            for (final ICitizenData citizenData : colony.getCitizenManager().getCitizens())
            {
                Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getCitizenEntity();
                if (!optionalEntityCitizen.isPresent())
                {
                    Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", colony.getID(), citizenData.getId()));
                    citizenData.updateCitizenEntityIfNecessary();
                    optionalEntityCitizen = citizenData.getCitizenEntity();
                }

                if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), world, location))
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

    @Override
    protected void toBytesOverride(final PacketBuffer buf)
    {

    }

    @Override
    protected void fromBytesOverride(final PacketBuffer buf)
    {

    }
}
