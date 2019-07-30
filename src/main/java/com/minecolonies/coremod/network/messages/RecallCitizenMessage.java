package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ICitizenData;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.IBuildingWorker;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.IEntityCitizen;
import com.minecolonies.coremod.util.TeleportHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntityMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Recalls the citizen to the hut.
 * Created: May 26, 2014
 *
 * @author Colton
 */
public class RecallCitizenMessage extends AbstractMessage<RecallCitizenMessage, IMessage>
{
    private int      colonyId;
    private BlockPos buildingId;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * Empty public constructor.
     */
    public RecallCitizenMessage()
    {
        super();
    }

    /**
     * Object creation for the recall.
     *
     * @param building View of the building the citizen is working in.
     */
    public RecallCitizenMessage(@NotNull final com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker.View building)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final RecallCitizenMessage message, final PlayerEntityMP player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final IBuildingWorker building = colony.getBuildingManager().getBuilding(message.buildingId, AbstractBuildingWorker.class);
            if (building != null)
            {
                for (int i = 0; i < building.getAssignedEntities().size(); i++)
                {
                    Optional<IEntityCitizen> optionalEntityCitizen = building.getAssignedEntities().get(i);
                    final ICitizenData citizenData = building.getAssignedCitizen().get(i);
                    if (!optionalEntityCitizen.isPresent())
                    {
                        if (citizenData != null)
                        {
                            Log.getLogger().warn(String.format("Citizen #%d:%d has gone AWOL, respawning them!", colony.getID(), citizenData.getId()));
                            citizenData.updateCitizenEntityIfNecessary();
                            optionalEntityCitizen = citizenData.getCitizenEntity();
                        }
                        else
                        {
                            Log.getLogger().warn("Citizen is AWOL and citizenData is null!");
                            return;
                        }
                    }
                    else if (optionalEntityCitizen.get().getTicksExisted() == 0)
                    {
                        final IEntityCitizen oldCitizen = optionalEntityCitizen.get();
                        final List<IEntityCitizen> list = player.getServerWorld().getLoadedEntityList().stream()
                          .filter(e -> e instanceof IEntityCitizen)
                          .filter(e -> e.equals(oldCitizen))
                          .map(e -> (IEntityCitizen) e)
                          .collect(Collectors.toList());

                        if (list.isEmpty())
                        {
                            citizenData.setCitizenEntity(null);
                            citizenData.updateCitizenEntityIfNecessary();
                        }
                        else
                        {
                            citizenData.setCitizenEntity(list.get(0));
                        }
                    }


                    final BlockPos loc = building.getPosition();
                    if (optionalEntityCitizen.isPresent() && !TeleportHelper.teleportCitizen(optionalEntityCitizen.get(), colony.getWorld(), loc))
                    {
                        LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.workerHuts.recallFail");
                    }
                }
            }
        }
    }
}
