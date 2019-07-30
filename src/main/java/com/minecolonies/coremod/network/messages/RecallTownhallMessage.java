package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.coremod.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.coremod.entity.IEntityCitizen;
import com.minecolonies.coremod.util.TeleportHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
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
     * The dimension of the message.
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
    public void fromBytes(final ByteBuf buf)
    {
        colonyId = buf.readInt();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final RecallTownhallMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
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
                    Optional<IEntityCitizen> optionalEntityCitizen = citizenData.getCitizenEntity();
                    if (!optionalEntityCitizen.isPresent())
                    {
                        Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", colony.getID(), citizenData.getId()));
                        citizenData.updateCitizenEntityIfNecessary();
                        optionalEntityCitizen = citizenData.getCitizenEntity();
                    }

                    if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), world, location))
                    {
                        LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.workerHuts.recallFail");
                    }
                }
            }
        }
    }
}
