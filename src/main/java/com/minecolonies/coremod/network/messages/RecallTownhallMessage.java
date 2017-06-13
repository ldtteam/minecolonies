package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.BuildingTownHall;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.util.TeleportHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to handle citizen recalls to the townhall.
 */
public class RecallTownhallMessage extends AbstractMessage<RecallTownhallMessage, IMessage>
{
    private int colonyId;

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
    public RecallTownhallMessage(@NotNull final BuildingTownHall.View townhall)
    {
        super();
        this.colonyId = townhall.getColony().getID();
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        colonyId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(colonyId);
    }

    @Override
    public void messageOnServerThread(RecallTownhallMessage message, EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingTownHall building = colony.getTownHall();
            if (building != null)
            {
                final BlockPos location = building.getLocation();
                final World world = colony.getWorld();
                for (CitizenData citizenData : colony.getCitizens().values())
                {
                    EntityCitizen citizen = citizenData.getCitizenEntity();
                    if(citizen == null)
                    {

                        Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", colony.getID(), citizenData.getId()));
                        colony.spawnCitizen(citizenData);
                        citizen = citizenData.getCitizenEntity();
                    }

                    if (!TeleportHelper.teleportCitizen(citizen, world, location))
                    {
                        LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.workerHuts.recallFail");
                    }
                }
            }
        }
    }
}
