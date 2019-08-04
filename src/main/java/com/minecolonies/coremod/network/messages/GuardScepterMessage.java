package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.network.IMessage;
import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Message to set the guard scepter in the player inventory.
 */
public class GuardScepterMessage implements IMessage
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
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        this.taskId = buf.readInt();
        this.buildingId = buf.readBlockPos();
        this.colonyId = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(taskId);
        buf.writeBlockPos(buildingId);
        buf.writeInt(colonyId);
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
        final ItemStack scepter;
        boolean giveToPlayer = true;
        final PlayerEntity player = ctxIn.getSender();
        if (player.getHeldItemMainhand().getItem() == ModItems.scepterGuard)
        {
            scepter = player.getHeldItemMainhand();
            giveToPlayer = false;
        }
        else
        {
            scepter = new ItemStack(ModItems.scepterGuard);
        }

        if (!scepter.hasTag())
        {
            scepter.setTag(new CompoundNBT());
        }
        final CompoundNBT compound = scepter.getTag();

        //Should never happen.
        if (compound == null)
        {
            return;
        }
        compound.putInt("task", taskId);

        final int emptySlot = player.inventory.getFirstEmptyStack();
        BlockPosUtil.write(compound, TAG_POS, buildingId);
        compound.putInt(TAG_ID, colonyId);

        if (giveToPlayer)
        {
            final ItemStack item = player.inventory.getStackInSlot(player.inventory.currentItem);
            player.inventory.setInventorySlotContents(emptySlot, item);
            player.inventory.setInventorySlotContents(player.inventory.currentItem, scepter);
        }
        player.inventory.markDirty();
    }
}
