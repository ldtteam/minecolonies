package com.minecolonies.core.network.messages.server.colony;

import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.TranslationConstants.*;

/**
 * Message to execute the renaiming of the townHall.
 */
public class ChangeFreeToInteractBlockMessage extends AbstractColonyServerMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forServer(Constants.MOD_ID, "change_free_to_interact_block", ChangeFreeToInteractBlockMessage::new);

    /**
     * The position of the free to interact block.
     */
    private final BlockPos pos;

    /**
     * The blockState which can be freely interacted with.
     */
    private final BlockState block;

    /**
     * The type of the
     */
    private final MessageType msgType;
    private final MessageMode msgMode;

    /**
     * Message creation to add a new freely interactable block to the colony.
     *
     * @param colony Colony the block can be interacted with in.
     * @param block  the blockState.
     * @param type   the type of
     */
    public ChangeFreeToInteractBlockMessage(@NotNull final IColonyView colony, @NotNull final Block block, @NotNull final MessageType type)
    {
        super(TYPE, colony);
        this.pos = new BlockPos(0, 0, 0);
        this.block = block.defaultBlockState();
        this.msgType = type;
        this.msgMode = MessageMode.BLOCK;
    }

    /**
     * Message creation to add a new freely interactable position to the colony.
     *
     * @param colony Colony the position can be interacted with in.
     * @param pos    the position.
     * @param type   the type of
     */
    public ChangeFreeToInteractBlockMessage(@NotNull final IColonyView colony, @NotNull final BlockPos pos, @NotNull final MessageType type)
    {
        super(TYPE, colony);
        this.pos = pos;
        this.block = Blocks.DIRT.defaultBlockState();
        this.msgType = type;
        this.msgMode = MessageMode.LOCATION;
    }

    protected ChangeFreeToInteractBlockMessage(final FriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);

        block = Block.stateById(buf.readInt());
        pos = buf.readBlockPos();
        msgType = MessageType.values()[buf.readInt()];
        msgMode = MessageMode.values()[buf.readInt()];
    }

    @Override
    protected void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        super.toBytes(buf);

        buf.writeInt(Block.getId(block));
        buf.writeBlockPos(pos);
        buf.writeInt(msgType.ordinal());
        buf.writeInt(msgMode.ordinal());
    }

    @Nullable
    @Override
    protected Action permissionNeeded()
    {
        return Action.EDIT_PERMISSIONS;
    }

    @Override
    protected void onExecute(final PlayPayloadContext ctxIn, final ServerPlayer player, final IColony colony)
    {
        //Verify player has permission to change this huts settings
        if (msgType == MessageType.ADD_BLOCK)
        {
            switch (msgMode)
            {
                case LOCATION:
                    colony.addFreePosition(pos);
                    MessageUtils.format(MESSAGE_PERMISSION_SCEPTER_ADD_POSITION_SUCCESS, pos.getX(), pos.getY(), pos.getZ()).sendTo(player);
                    break;
                case BLOCK:
                    colony.addFreeBlock(block.getBlock());
                    MessageUtils.format(MESSAGE_PERMISSION_SCEPTER_ADD_BLOCK_SUCCESS, BuiltInRegistries.BLOCK.getKey(block.getBlock())).sendTo(player);
                    break;
                default:
                    // Error!
            }
        }
        else
        {
            switch (msgMode)
            {
                case LOCATION:
                    colony.removeFreePosition(pos);
                    MessageUtils.format(MESSAGE_PERMISSION_SCEPTER_REMOVE_POSITION_SUCCESS, pos.getX(), pos.getY(), pos.getZ()).sendTo(player);
                    break;
                case BLOCK:
                    colony.removeFreeBlock(block.getBlock());
                    MessageUtils.format(MESSAGE_PERMISSION_SCEPTER_REMOVE_BLOCK_SUCCESS, BuiltInRegistries.BLOCK.getKey(block.getBlock())).sendTo(player);
                    break;
                default:
                    // Error!
            }
        }
    }

    /**
     * Enums for Message Type for the freeBlock
     * <p>
     * ADD_BLOCK       Add a block or pos. REMOVE_BLOCK    Removing a block or pos.
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
