package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.util.TeleportHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Recalls the citizen to the location.
 */
public class RecallSingleCitizenMessage extends AbstractMessage<RecallSingleCitizenMessage, IMessage>
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
    public RecallSingleCitizenMessage(final AbstractBuildingView building, final int citizenid)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.citizenId = citizenid;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        citizenId = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(citizenId);
    }

    @Override
    public void messageOnServerThread(final RecallSingleCitizenMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            final CitizenData citizenData = colony.getCitizenManager().getCitizen(message.citizenId);
            Optional<EntityCitizen> optionalEntityCitizen = citizenData.getCitizenEntity();
            if (!optionalEntityCitizen.isPresent())
            {
                citizenData.updateCitizenEntityIfNecessary();
                optionalEntityCitizen = citizenData.getCitizenEntity();
            }

            if (optionalEntityCitizen.isPresent() && optionalEntityCitizen.get().ticksExisted == 0)
            {
                citizenData.setCitizenEntity(null);
                citizenData.updateCitizenEntityIfNecessary();
            }

            final BlockPos loc = message.buildingId;
            if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), colony.getWorld(), loc))
            {
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.workerHuts.recallFail");
            }
        }
    }
}
