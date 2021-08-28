package com.minecolonies.coremod.network.messages.server.colony;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.coremod.network.messages.server.AbstractColonyServerMessage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to execute the renaiming of the townHall.
 */
public class ChangeFreeToInteractBlockMessage extends AbstractColonyServerMessage
{

    /**
     * The position of the free to interact block.
     */
    private BlockPos pos = new BlockPos(0, 0, 0);

    /**
     * The blockState which can be freely interacted with.
     */
    private BlockState block = Blocks.DIRT.defaultBlockState();

    /**
     * The type of the
     */
    private MessageType type;
    private MessageMode mode;

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
     * @param type   the type of
     */
    public ChangeFreeToInteractBlockMessage(@NotNull final IColonyView colony, @NotNull final Block block, @NotNull final MessageType type)
    {
        super(colony);
        this.pos = new BlockPos(0, 0, 0);
        this.block = block.defaultBlockState();
        this.type = type;
        this.mode = MessageMode.BLOCK;
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
        super(colony);
        this.pos = pos;
        this.block = Blocks.DIRT.defaultBlockState();
        this.type = type;
        this.mode = MessageMode.LOCATION;
    }

    @Override
    public void fromBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        block = Block.stateById(buf.readInt());
        pos = buf.readBlockPos();
        type = MessageType.values()[buf.readInt()];
        mode = MessageMode.values()[buf.readInt()];
    }

    @Override
    public void toBytesOverride(@NotNull final FriendlyByteBuf buf)
    {

        buf.writeInt(Block.getId(block));
        buf.writeBlockPos(pos);
        buf.writeInt(type.ordinal());
        buf.writeInt(mode.ordinal());
    }

    @Nullable
    @Override
    public Action permissionNeeded()
    {
        return Action.EDIT_PERMISSIONS;
    }

    @Override
    protected void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer, final IColony colony)
    {
        final Player player = ctxIn.getSender();
        if (player == null)
        {
            return;
        }

        //Verify player has permission to change this huts settings

        if (type == MessageType.ADD_BLOCK)
        {
            switch (mode)
            {
                case LOCATION:
                    colony.addFreePosition(pos);
                    LanguageHandler.sendPlayerMessage(
                      player,
                      "com.minecolonies.coremod.item.permissionscepter.addposition.success",
                      pos.getX(),
                      pos.getY(),
                      pos.getZ()
                    );
                    break;
                case BLOCK:
                    colony.addFreeBlock(block.getBlock());
                    LanguageHandler.sendPlayerMessage(
                      player,
                      "com.minecolonies.coremod.item.permissionscepter.addblock.success",
                      block.getBlock().getRegistryName()
                    );
                    break;
                default:
                    // Error!
            }
        }
        else
        {
            switch (mode)
            {
                case LOCATION:
                    colony.removeFreePosition(pos);
                    LanguageHandler.sendPlayerMessage(
                      player,
                      "com.minecolonies.coremod.item.permissionscepter.removelocation.success",
                      pos.getX(),
                      pos.getY(),
                      pos.getZ());
                    break;
                case BLOCK:
                    colony.removeFreeBlock(block.getBlock());
                    LanguageHandler.sendPlayerMessage(
                      player,
                      "com.minecolonies.coremod.item.permissionscepter.removeblock.success",
                      block.getBlock().getRegistryName()
                    );
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
