package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

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
     * The dimension of the message.
     */
    private int dimension;


    /**
     * Empty constructor used when registering the message.
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
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        style = buf.readString();
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeString(style);
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final BuildingSetStyleMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final IBuilding building = colony.getBuildingManager().getBuilding(message.buildingId, AbstractBuilding.class);
            if (building != null)
            {
                building.setStyle(message.style);
                if(building.getTileEntity() != null)
                {
                    building.getTileEntity().setStyle(message.style);
                    if(building.getBuildingLevel() > 0)
                    {
                        building.onUpgradeComplete(building.getBuildingLevel());
                    }
                }
            }
        }
    }
}
