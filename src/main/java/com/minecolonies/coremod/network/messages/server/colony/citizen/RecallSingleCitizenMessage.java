package com.minecolonies.coremod.network.messages.server.colony.citizen;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.network.messages.server.AbstractBuildingServerMessage;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Recalls the citizen to the location.
 */
public class RecallSingleCitizenMessage extends AbstractBuildingServerMessage<IBuilding>
{
    /**
     * The citizen id.
     */
    private int citizenId;

    /**
     * Empty public constructor.
     */
    public RecallSingleCitizenMessage()
    {
        super();
    }

    /**
     * Object creation for the recall.
     *
     * @param building View of the building the citizen should be teleported to.
     * @param citizenid the id of the citizen.
     */
    public RecallSingleCitizenMessage(final IBuildingView building, final int citizenid)
    {
        super(building);
        this.citizenId = citizenid;
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        citizenId = buf.readInt();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeInt(citizenId);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony, final IBuilding building)
    {
        final ICitizenData citizenData = colony.getCitizenManager().getCitizen(citizenId);
        Optional<AbstractEntityCitizen> optionalEntityCitizen = citizenData.getCitizenEntity();
        if (!optionalEntityCitizen.isPresent())
        {
            citizenData.updateCitizenEntityIfNecessary();
            optionalEntityCitizen = citizenData.getCitizenEntity();
        }

        if (optionalEntityCitizen.isPresent() && optionalEntityCitizen.get().getTicksExisted() == 0)
        {
            citizenData.updateCitizenEntityIfNecessary();
        }

        final BlockPos loc = building.getID();
        if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), colony.getWorld(), loc))
        {
            final PlayerEntity player = ctxIn.getSender();
            if (player == null) return;

            LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.workerhuts.recallFail");
        }
    }
}
