package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.network.IMessage;
import com.ldtteam.structurize.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
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
public class RecallCitizenMessage implements IMessage
{
    private int      colonyId;
    private BlockPos buildingId;

    /**
     * The dimension of the 
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
    public RecallCitizenMessage(@NotNull final AbstractBuildingWorker.View building)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
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

            @Nullable final IBuildingWorker building = colony.getBuildingManager().getBuilding(buildingId, AbstractBuildingWorker.class);
            if (building != null)
            {
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
                        final AbstractEntityCitizen oldCitizen = optionalEntityCitizen.get();
                        final List<AbstractEntityCitizen> list = ((ServerWorld) player.world).getEntities()
                                                                   .filter(e -> e instanceof AbstractEntityCitizen)
                          .filter(e -> e.equals(oldCitizen))
                                                                   .map(e -> (AbstractEntityCitizen) e)
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
