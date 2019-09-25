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
 * Message to set the lumberjack scepter in the player inventory.
 */
public class LumberjackScepterMessage extends AbstractMessage<LumberjackScepterMessage, IMessage>
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
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        this.buildingId = BlockPosUtil.readFromByteBuf(buf);
        this.colonyId = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, buildingId);
        buf.writeInt(colonyId);
    }

    @Override
    public void messageOnServerThread(final LumberjackScepterMessage message, final EntityPlayerMP player)
    {
        final ItemStack scepter;
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
