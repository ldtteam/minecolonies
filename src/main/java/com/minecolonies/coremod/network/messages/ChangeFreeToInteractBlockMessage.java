package com.minecolonies.coremod.network.messages;

import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.permissions.Permissions;
import com.minecolonies.coremod.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

/**
 * Message to execute the renaiming of the townHall.
 */
public class ChangeFreeToInteractBlockMessage extends AbstractMessage<ChangeFreeToInteractBlockMessage, IMessage>
{

    /**
     * The id of the colony.
     */
    private int colonyId;
    /**
     * The position of the free to interact block.
     */
    private BlockPos pos   = new BlockPos(0, 0, 0);
    /**
     * The blockState which can be freely interacted with.
     */
    private Block    block = Blocks.DIRT;
    /**
     * The type of the message.
     */
    private MessageType type;

    /**
     * Empty public constructor.
     */
    public ChangeFreeToInteractBlockMessage()
    {
        super();
    }

    /**
     * Message creation to add a new freely interactable block to the colony.
     *
     * @param colony Colony the block can be interacted with in.
     * @param block  the blockState.
     * @param type   the type of message.
     */
    public ChangeFreeToInteractBlockMessage(@NotNull final ColonyView colony, @NotNull final Block block, @NotNull final MessageType type)
    {
        super();
        this.colonyId = colony.getID();
        this.pos = new BlockPos(0, 0, 0);
        this.block = block;
        this.type = type;
    }

    /**
     * Message creation to add a new freely interactable position to the colony.
     *
     * @param colony Colony the position can be interacted with in.
     * @param pos    the position.
     * @param type   the type of message.
     */
    public ChangeFreeToInteractBlockMessage(@NotNull final ColonyView colony, @NotNull final BlockPos pos, @NotNull final MessageType type)
    {
        super();
        this.colonyId = colony.getID();
        this.pos = pos;
        this.block = Blocks.DIRT;
        this.type = type;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        block = Block.getBlockFromName(ByteBufUtils.readUTF8String(buf));
        pos = BlockPosUtil.readFromByteBuf(buf);
        type = MessageType.values()[buf.readInt()];
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ByteBufUtils.writeUTF8String(buf, block.getRegistryName().toString());
        BlockPosUtil.writeToByteBuf(buf, pos);
        buf.writeInt(type.ordinal());
    }

    @Override
    public void messageOnServerThread(final ChangeFreeToInteractBlockMessage message, final EntityPlayerMP player)
    {
        final Colony colony = ColonyManager.getColony(message.colonyId);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Permissions.Action.EDIT_PERMISSIONS))
            {
                return;
            }

            if (message.type == MessageType.ADD_BLOCK)
            {
                if (!(message.pos.getX() == 0 && message.pos.getZ() == 0 && message.pos.getY() == 0))
                {
                    colony.addFreePosition(message.pos);
                }
                else
                {
                    colony.addFreeBlock(message.block);
                }
            }
            else
            {
                if (!(message.pos.getX() == 0 && message.pos.getZ() == 0 && message.pos.getY() == 0))
                {
                    colony.removeFreePosition(message.pos);
                }
                else
                {
                    colony.removeFreeBlock(message.block);
                }
            }
        }
    }

    /**
     * Enums for Message Type for the freeBlock message.
     * <p>
     * ADD_BLOCK       Add a block or pos.
     * REMOVE_BLOCK    Removing a block or pos.
     */
    public enum MessageType
    {
        REMOVE_BLOCK,
        ADD_BLOCK,
    }
}
