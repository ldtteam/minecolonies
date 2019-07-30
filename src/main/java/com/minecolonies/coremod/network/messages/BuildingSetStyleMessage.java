package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.IBuilding;
import com.minecolonies.coremod.colony.buildings.views.IBuildingView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;

import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to set the style of a building.
 */
public class BuildingSetStyleMessage implements IMessage
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
     * The style to set.
     */
    private String style;

    /**
     * The dimension of the 
     */
    private int dimension;


    /**
     * Empty constructor used when registering the 
     */
    public BuildingSetStyleMessage()
    {
        super();
    }

    /**
     * Creates object for the style of a building.
     *
     * @param building View of the building to read data from.
     * @param style    style of the building.
     */
    public BuildingSetStyleMessage(@NotNull final IBuildingView building, final String style)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.style = style;
        this.dimension = building.getColony().getDimension();
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        buildingId = buf.readBlockPos();
        style = buf.readString();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeBlockPos(buildingId);
        buf.writeString(style);
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

            @Nullable final IBuilding building = colony.getBuildingManager().getBuilding(buildingId, AbstractBuilding.class);
            if (building != null)
            {
                building.setStyle(style);
                if(building.getTileEntity() != null)
                {
                    building.getTileEntity().setStyle(style);
                    if(building.getBuildingLevel() > 0)
                    {
                        building.onUpgradeComplete(building.getBuildingLevel());
                    }
                }
            }
        }
    }
}
