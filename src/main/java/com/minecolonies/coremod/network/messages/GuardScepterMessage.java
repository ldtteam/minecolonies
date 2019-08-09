package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Message to set the guard scepter in the player inventory.
 */
public class GuardScepterMessage extends AbstractMessage<GuardScepterMessage, IMessage>
{
    /**
     * The id of the task.
     */
    private int taskId;

    /**
     * The position of the building.
     */
    private BlockPos buildingId;

    /**
     * The position of the building.
     */
    private int colonyId;

    /**
     * Empty standard constructor.
     */
    public GuardScepterMessage()
    {
        super();
    }

    /**
     * Creates a new message of this type to set the guard scepter in the player inventory.
     *
     * @param taskId     the task id.
     * @param buildingId the position of the building.
     * @param colonyId   the id of the colony.
     */
    public GuardScepterMessage(final int taskId, final BlockPos buildingId, final int colonyId)
    {
        super();
        this.taskId = taskId;
        this.buildingId = buildingId;
        this.colonyId = colonyId;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        this.taskId = buf.readInt();
        this.buildingId = BlockPosUtil.readFromByteBuf(buf);
        this.colonyId = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(taskId);
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(colonyId);
    }

    @Override
    public void messageOnServerThread(final GuardScepterMessage message, final EntityPlayerMP player)
    {
        final ItemStack scepter;
        boolean giveToPlayer = true;
        if (player.getHeldItemMainhand().getItem() == ModItems.scepterGuard)
        {
            scepter = player.getHeldItemMainhand();
            giveToPlayer = false;
        }
        else
        {
            scepter = new ItemStack(ModItems.scepterGuard);
        }

        if (!scepter.hasTagCompound())
        {
            scepter.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound compound = scepter.getTagCompound();

        //Should never happen.
        if (compound == null)
        {
            return;
        }
        compound.setInteger("task", message.taskId);

        final int emptySlot = player.inventory.getFirstEmptyStack();
        BlockPosUtil.writeToNBT(compound, TAG_POS, message.buildingId);
        compound.setInteger(TAG_ID, message.colonyId);

        if (giveToPlayer)
        {
            final ItemStack item = player.inventory.getStackInSlot(player.inventory.currentItem);
            player.inventory.setInventorySlotContents(emptySlot, item);
            player.inventory.setInventorySlotContents(player.inventory.currentItem, scepter);
        }
        player.inventory.markDirty();
    }
}
