package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to execute the renaiming of the townHall.
 */
public class AddFreeToInteractBlockMessage extends AbstractMessage<AddFreeToInteractBlockMessage, IMessage>
{
    /**
     * The id of the colony.
     */
    private int colonyId;

    /**
     * The position of the free to interact block.
     */
    private BlockPos pos = new BlockPos(0, 0 ,0);

    /**
     * The blockState which can be freely interacted with.
     */
    private IBlockState blockState = Blocks.DIRT.getDefaultState();

    /**
     * Empty public constructor.
     */
    public AddFreeToInteractBlockMessage()
    {
        super();
    }

    /**
     * Message creation to add a new freely interactable block to the colony.
     *
     * @param colony Colony the block can be interacted with in.
     * @param blockState the blockState.
     */
    public AddFreeToInteractBlockMessage(@NotNull final ColonyView colony, @Nullable final IBlockState blockState)
    {
        super();
        this.colonyId = colony.getID();
        this.pos = new BlockPos(0,0,0);
        this.blockState = blockState;
    }

    /**
     * Message creation to add a new freely interactable position to the colony.
     *
     * @param colony Colony the position can be interacted with in.
     * @param pos the position.
     */
    public AddFreeToInteractBlockMessage(@NotNull final ColonyView colony, @Nullable final BlockPos pos)
    {
        super();
        this.colonyId = colony.getID();
        this.pos = pos;
        this.blockState = Blocks.DIRT.getDefaultState();
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        blockState = NBTUtil.readBlockState(ByteBufUtils.readTag(buf));
        pos = BlockPosUtil.readFromByteBuf(buf);
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ByteBufUtils.writeTag(buf, NBTUtil.writeBlockState(new NBTTagCompound(), blockState));
        BlockPosUtil.writeToByteBuf(buf, pos);
    }

    @Override
    public void messageOnServerThread(final AddFreeToInteractBlockMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Permissions.Action.EDIT_PERMISSIONS))
            {
                return;
            }

            if(message.blockState.getBlock() != Blocks.DIRT)
            {
                colony.addFreeBlock(message.blockState);
            }

            if(pos.getX() != 0 && pos.getZ() != 0 && pos.getY() != 0)
            {
                colony.addFreePosition(message.pos);
            }
        }
    }
}
