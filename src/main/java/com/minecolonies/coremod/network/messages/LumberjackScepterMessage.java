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
 * Message to set the lumberjack scepter in the player inventory.
 */
public class LumberjackScepterMessage implements IMessage
{
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
    public LumberjackScepterMessage()
    {
        super();
    }

    /**
     * Creates a new message of this type to set the lumberjack scepter in the player inventory.
     *
     * @param buildingId the position of the building.
     * @param colonyId   the id of the colony.
     */
    public LumberjackScepterMessage(final BlockPos buildingId, final int colonyId)
    {
        super();
        this.buildingId = buildingId;
        this.colonyId = colonyId;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        buildingId = buf.readBlockPos();
        this.colonyId = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
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
        final PlayerEntity player = ctxIn.getSender();
        boolean giveToPlayer = true;
        if (player.getHeldItemMainhand().getItem() == ModItems.scepterLumberjack)
        {
            scepter = player.getHeldItemMainhand();
            giveToPlayer = false;
        }
        else
        {
            scepter = new ItemStack(ModItems.scepterLumberjack);
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
