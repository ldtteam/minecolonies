package com.minecolonies.coremod.network.messages.server.colony;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_ID;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_POS;

/**
 * Message to set the guard scepter in the player inventory.
 */
public class GuardScepterMessage extends AbstractColonyServerMessage
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
     * Empty standard constructor.
     */
    public GuardScepterMessage()
    {
        super();
    }

    /**
     * Creates a new message of this type to set the guard scepter in the player inventory.
     *
     * @param taskId   the task id.
     * @param building the building we're executing on.
     */
    public GuardScepterMessage(final IBuildingView building, final int taskId)
    {
        super(building.getColony());
        this.taskId = taskId;
        this.buildingId = building.getPosition();
    }

    @Override
    public void fromBytesOverride(@NotNull final PacketBuffer buf)
    {

        this.taskId = buf.readInt();
        this.buildingId = buf.readBlockPos();
    }

    @Override
    public void toBytesOverride(@NotNull final PacketBuffer buf)
    {

        buf.writeInt(taskId);
        buf.writeBlockPos(buildingId);
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final ItemStack scepter;
        boolean giveToPlayer = true;
        final PlayerEntity player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

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
        compound.putInt(TAG_ID, colony.getID());

        if (giveToPlayer)
        {
            final ItemStack item = player.inventory.getStackInSlot(player.inventory.currentItem);
            player.inventory.setInventorySlotContents(emptySlot, item);
            player.inventory.setInventorySlotContents(player.inventory.currentItem, scepter);
        }
        player.inventory.markDirty();
    }
}
