package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.network.IMessage;
import com.minecolonies.coremod.blocks.BlockDecorationController;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;

import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Message to update the decoration control block.
 */
public class DecorationControllUpdateMessage implements IMessage
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
    public DecorationControllUpdateMessage() {super();}

    /**
     * Constructor for the decoration controller update 
     * @param pos the position of the controller.
     * @param name the name to set.
     * @param level the new level to set.
     */
    public DecorationControllUpdateMessage(@NotNull final BlockPos pos, final String name, final int level)
    {
        super();
        this.pos = pos;
        this.name = name;
        this.level = level;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        this.name = buf.readString();
        this.pos = buf.readBlockPos();
        this.level = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeString(this.name);
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
        final PlayerEntity player = ctxIn.getSender();
        final TileEntity tileEntity = player.getEntityWorld().getTileEntity(pos);
        if (tileEntity instanceof TileEntityDecorationController)
        {
            final BlockState state = player.getEntityWorld().getBlockState(pos);
            final Direction basicFacing = state.get(BlockDecorationController.HORIZONTAL_FACING);
            ((TileEntityDecorationController) tileEntity).setSchematicName(name);
            ((TileEntityDecorationController) tileEntity).setLevel(level);
            ((TileEntityDecorationController) tileEntity).setBasicFacing(basicFacing);
        }
    }
}
