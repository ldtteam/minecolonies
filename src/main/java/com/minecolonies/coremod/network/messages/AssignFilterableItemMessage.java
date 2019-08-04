package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.AbstractFilterableListBuilding;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;

import net.minecraftforge.fml.network.NetworkEvent;
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
     * The dimension of the 
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
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        this.colonyId = buf.readInt();
        this.buildingId = buf.readBlockPos();
        this.assign = buf.readBoolean();
        this.item = new ItemStorage(buf.readItemStack());
        this.dimension = buf.readInt();
        this.id = buf.readString();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(this.colonyId);
        buf.writeBlockPos(this.buildingId);
        buf.writeBoolean(this.assign);
        buf.writeItemStack(this.item.getItemStack());
        buf.writeInt(this.dimension);
        buf.writeString(this.id);
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
        final ServerPlayerEntity player = ctxIn.getSender();
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final AbstractFilterableListBuilding building = colony.getBuildingManager().getBuilding(buildingId, AbstractFilterableListBuilding.class);
            if (building != null)
            {
                if(assign)
                {
                    building.addItem(id, item);
                }
                else
                {
                    building.removeItem(id,item);
                }
            }
        }
    }
}

