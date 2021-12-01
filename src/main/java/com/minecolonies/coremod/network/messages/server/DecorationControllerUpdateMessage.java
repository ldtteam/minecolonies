package com.minecolonies.coremod.network.messages.server;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.blocks.BlockDecorationController;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to update the decoration control block.
 */
public class DecorationControllerUpdateMessage implements IMessage
{
    /**
     * The position of the block.
     */
    private BlockPos pos;

    /**
     * The name to set.
     */
    private String name;

    /**
     * The level to set.
     */
    private int level;

    /**
     * Default constructor for forge
     */
    public DecorationControllerUpdateMessage() {super();}

    /**
     * Constructor for the decoration controller update
     *
     * @param pos   the position of the controller.
     * @param name  the name to set.
     * @param level the new level to set.
     */
    public DecorationControllerUpdateMessage(@NotNull final BlockPos pos, final String name, final int level)
    {
        super();
        this.pos = pos;
        this.name = name;
        this.level = level;
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        this.name = buf.readUtf(32767);
        this.pos = buf.readBlockPos();
        this.level = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeUtf(this.name);
        buf.writeBlockPos(this.pos);
        buf.writeInt(this.level);
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
        final Player player = ctxIn.getSender();
        final BlockEntity tileEntity = player.getCommandSenderWorld().getBlockEntity(pos);
        if (tileEntity instanceof TileEntityDecorationController)
        {
            final BlockState state = player.getCommandSenderWorld().getBlockState(pos);
            final Direction basicFacing = state.getValue(BlockDecorationController.FACING);
            ((TileEntityDecorationController) tileEntity).setSchematicPath(name + level);
            ((TileEntityDecorationController) tileEntity).setTier(level);
            ((TileEntityDecorationController) tileEntity).setBasicFacing(basicFacing);
        }
    }
}
