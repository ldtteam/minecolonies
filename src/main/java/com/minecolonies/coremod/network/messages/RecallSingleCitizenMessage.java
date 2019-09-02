package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.network.IMessage;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Recalls the citizen to the location.
 */
public class RecallSingleCitizenMessage implements IMessage
{
    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * The building id.
     */
    private BlockPos buildingId;

    /**
     * The citizen id.
     */
    private int citizenId;

    /**
     * The dimension of the 
     */
    private int dimension;

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
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.citizenId = citizenid;
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        citizenId = buf.readInt();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeInt(citizenId);
        buf.writeInt(dimension);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.SERVER;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony != null)
        {
            final PlayerEntity player = ctxIn.getSender();
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

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

            final BlockPos loc = buildingId;
            if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), colony.getWorld(), loc))
            {
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.workerhuts.recallFail");
            }
        }
    }
}
