package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.AbstractFilterableListBuilding;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message which handles the assignment of items to filterable item lists.
 */
public class AssignFilterableItemMessage implements IMessage
{
    /**
     * The id of the colony.
     */
    private int         colonyId;

    /**
     * The id of the building.
     */
    private BlockPos    buildingId;

    /**
     * True if assign, false if remove.
     */
    private boolean     assign;

    /**
     * The item in question.
     */
    private ItemStorage item;

    /**
     * The dimension of the message.
     */
    private int dimension;

    /**
     * The id of the list.
     */
    private String id;

    /**
     * Empty standard constructor.
     */
    public AssignFilterableItemMessage()
    {
        super();
    }

    /**
     * Creates the message to add an item.
     *
     * @param building the composter
     * @param id the id of the list of filterables.
     * @param assign   compost if true, dont if false.
     * @param item    the item to assign
     */
    public AssignFilterableItemMessage(@NotNull final AbstractBuildingWorker.View building, final String id, final ItemStorage item, final boolean assign)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.assign = assign;
        this.item = item;
        this.dimension = building.getColony().getDimension();
        this.id = id;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        this.colonyId = buf.readInt();
        this.buildingId = BlockPosUtil.readFromByteBuf(buf);
        this.assign = buf.readBoolean();
        this.item = new ItemStorage(ByteBufUtils.readItemStack(buf));
        this.dimension = buf.readInt();
        this.id = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(this.colonyId);
        BlockPosUtil.writeToByteBuf(buf, this.buildingId);
        buf.writeBoolean(this.assign);
        ByteBufUtils.writeItemStack(buf, this.item.getItemStack());
        buf.writeInt(this.dimension);
        ByteBufUtils.writeUTF8String(buf, this.id);
    }

    @Override
    public void messageOnServerThread(final AssignFilterableItemMessage message, final ServerPlayerEntity player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final AbstractFilterableListBuilding building = colony.getBuildingManager().getBuilding(message.buildingId, AbstractFilterableListBuilding.class);
            if (building != null)
            {
                if(message.assign)
                {
                    building.addItem(message.id, message.item);
                }
                else
                {
                    building.removeItem(message.id,message.item);
                }
            }
        }
    }
}

