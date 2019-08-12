package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.network.IMessage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to execute the renaiming of the townHall.
 */
public class ChangeFreeToInteractBlockMessage implements IMessage
{

    /**
     * The id of the colony.
     */
    private int colonyId;

    /**
     * The position of the free to interact block.
     */
    private BlockPos pos = new BlockPos(0, 0, 0);

    /**
     * The blockState which can be freely interacted with.
     */
    private BlockState block = Blocks.DIRT.getDefaultState();

    /**
     * The type of the 
     */
    private MessageType type;
    private MessageMode mode;

    /**
     * The dimension of the 
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
     * @param type   the type of 
     */
    public ChangeFreeToInteractBlockMessage(@NotNull final IColonyView colony, @NotNull final Block block, @NotNull final MessageType type)
    {
        super();
        this.colonyId = colony.getID();
        this.pos = new BlockPos(0, 0, 0);
        this.block = block.getDefaultState();
        this.type = type;
        this.mode = MessageMode.BLOCK;
        this.dimension = colony.getDimension();
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
        super();
        this.colonyId = colony.getID();
        this.pos = pos;
        this.block = Blocks.DIRT.getDefaultState();
        this.type = type;
        this.mode = MessageMode.LOCATION;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        colonyId = buf.readInt();
        block = Block.getStateById(buf.readInt());
        pos = buf.readBlockPos();
        type = MessageType.values()[buf.readInt()];
        mode = MessageMode.values()[buf.readInt()];
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeInt(colonyId);
        buf.writeInt(Block.getStateId(block));
        buf.writeBlockPos(pos);
        buf.writeInt(type.ordinal());
        buf.writeInt(mode.ordinal());
        buf.writeInt(dimension);
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
        final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, dimension);
        if (colony != null)
        {
            final PlayerEntity player = ctxIn.getSender();
            //Verify player has permission to change this huts settings
            if (!colony.getPermissions().hasPermission(player, Action.EDIT_PERMISSIONS))
            {
                LanguageHandler.sendPlayerMessage(
                  player,
                  "com.minecolonies.coremod.item.permissionscepter.permission.deny"
                );
                return;
            }

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
    }

    /**
     * Enums for Message Type for the freeBlock 
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
