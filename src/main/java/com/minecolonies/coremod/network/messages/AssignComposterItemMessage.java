package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message which handles the assignment of items to composters
 */
public class AssignComposterItemMessage extends AbstractMessage<AssignComposterItemMessage, IMessage>
{

    private int         colonyId;
    private BlockPos    buildingId;
    private boolean     assign;
    private ItemStorage item;

    /**
     * Empty standard constructor.
     */
    public AssignComposterItemMessage()
    {
        super();
    }

    /**
     * Creates the message to add an item.
     *
     * @param building the composter
     * @param assign   compost if true, dont if false.
     * @param item    the item to assign
     */
    public AssignComposterItemMessage(@NotNull final BuildingComposter.View building,final ItemStorage item, final boolean assign)
    {
        super();
        this.colonyId = building.getColony().getID();
        this.buildingId = building.getID();
        this.assign = assign;
        this.item = item;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        buildingId = BlockPosUtil.readFromByteBuf(buf);
        assign = buf.readBoolean();
        this.item = new ItemStorage(ByteBufUtils.readItemStack(buf));
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeBoolean(assign);
        ByteBufUtils.writeItemStack(buf, item.getItemStack());
    }

    @Override
    public void messageOnServerThread(final AssignComposterItemMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS))
            {
                return;
            }

            @Nullable final BuildingComposter building = colony.getBuildingManager().getBuilding(message.buildingId, BuildingComposter.class);
            if (building != null)
            {
                if(message.assign)
                {
                    building.addCompostableItem(message.item);
                }
                else
                {
                    building.removeCompostableItem(message.item);
                }
            }
        }
    }
}

