package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.IColonyView;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntityMP;
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
    private MessageMode mode;

    /**
     * The dimension of the message.
     */
    private int dimension;

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
    public ChangeFreeToInteractBlockMessage(@NotNull final IColonyView colony, @NotNull final Block block, @NotNull final MessageType type)
    {
        super();
        this.colonyId = colony.getID();
        this.pos = new BlockPos(0, 0, 0);
        this.block = block;
        this.type = type;
        this.mode = MessageMode.BLOCK;
        this.dimension = colony.getDimension();
    }

    /**
     * Message creation to add a new freely interactable position to the colony.
     *
     * @param colony Colony the position can be interacted with in.
     * @param pos    the position.
     * @param type   the type of message.
     */
    public ChangeFreeToInteractBlockMessage(@NotNull final IColonyView colony, @NotNull final BlockPos pos, @NotNull final MessageType type)
    {
        super();
        this.colonyId = colony.getID();
        this.pos = pos;
        this.block = Blocks.DIRT;
        this.type = type;
        this.mode = MessageMode.LOCATION;
    }

    @Override
    public void fromBytes(@NotNull final ByteBuf buf)
    {
        colonyId = buf.readInt();
        block = Block.getBlockFromName(ByteBufUtils.readUTF8String(buf));
        pos = BlockPosUtil.readFromByteBuf(buf);
        type = MessageType.values()[buf.readInt()];
        mode = MessageMode.values()[buf.readInt()];
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final ByteBuf buf)
    {
        buf.writeInt(colonyId);
        ByteBufUtils.writeUTF8String(buf, block.getRegistryName().toString());
        BlockPosUtil.writeToByteBuf(buf, pos);
        buf.writeInt(type.ordinal());
        buf.writeInt(mode.ordinal());
        buf.writeInt(dimension);
    }

    @Override
    public void messageOnServerThread(final ChangeFreeToInteractBlockMessage message, final PlayerEntityMP player)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(message.colonyId, message.dimension);
        if (colony != null)
        {
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS))
            {
                LanguageHandler.sendPlayerMessage(
                  player,
                  "com.minecolonies.coremod.item.permissionscepter.permission.deny"
                );
                return;
            }

            if (message.type == MessageType.ADD_BLOCK)
            {
                switch (message.mode)
                {
                    case LOCATION:
                        colony.addFreePosition(message.pos);
                        LanguageHandler.sendPlayerMessage(
                          player,
                          "com.minecolonies.coremod.item.permissionscepter.addposition.success",
                          message.pos.getX(),
                          message.pos.getY(),
                          message.pos.getZ()
                        );
                        break;
                    case BLOCK:
                        colony.addFreeBlock(message.block);
                        LanguageHandler.sendPlayerMessage(
                          player,
                          "com.minecolonies.coremod.item.permissionscepter.addblock.success",
                          message.block.getRegistryName()
                        );
                        break;
                    default:
                        // Error!
                }
            }
            else
            {
                switch (message.mode)
                {
                    case LOCATION:
                        colony.removeFreePosition(message.pos);
                        LanguageHandler.sendPlayerMessage(
                          player,
                          "com.minecolonies.coremod.item.permissionscepter.removelocation.success",
                          message.pos.getX(),
                          message.pos.getY(),
                          message.pos.getZ());
                        break;
                    case BLOCK:
                        colony.removeFreeBlock(message.block);
                        LanguageHandler.sendPlayerMessage(
                          player,
                          "com.minecolonies.coremod.item.permissionscepter.removeblock.success",
                          message.block.getRegistryName()
                        );
                        break;
                    default:
                        // Error!
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

    /**
     * Enums of modes this message exists.
     */
    public enum MessageMode
    {
        LOCATION,
        BLOCK,
    }
}
