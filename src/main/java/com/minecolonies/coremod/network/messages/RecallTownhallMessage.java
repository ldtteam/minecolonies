package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.permissions.Action;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Used to handle citizen recalls to the townhall.
 */
public class RecallTownhallMessage implements IMessage
{
    private int colonyId;

    /**
     * The dimension of the 
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public RecallTownhallMessage()
    {
        super();
    }

    /**
     * Object creation for the recall.
     *
     * @param townhall View of the townhall.
     */
    public RecallTownhallMessage(@NotNull final ITownHallView townhall)
    {
        super();
        this.colonyId = townhall.getColony().getID();
        this.dimension = townhall.getColony().getDimension();
    }

    @Override
    public void fromBytes(final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
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
                        LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.workerhuts.recallFail");
                    }
                }
            }
        }
    }
}
