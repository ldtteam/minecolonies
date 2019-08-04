package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles the server telling nearby clients to render a particle effect.
 * Created: February 10, 2016
 *
 * @author Colton
 */
public class BlockParticleEffectMessage implements IMessage
{
    public static final int BREAK_BLOCK = -1;

    private BlockPos pos;
    private BlockState    block;
    private int      side;

    /**
     * Empty constructor used when registering the 
     */
    public BlockParticleEffectMessage()
    {
        super();
    }

    /**
     * Sends a message for particle effect.
     *
     * @param pos   Coordinates
     * @param state Block State
     * @param side  Side of the block causing effect
     */
    public BlockParticleEffectMessage(final BlockPos pos, @NotNull final BlockState state, final int side)
    {
        this.pos = pos;
        this.block = state;
        this.side = side;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        pos = buf.readBlockPos();
        block = Block.getStateById(buf.readInt());
        side = buf.readInt();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(Block.getStateId(block));
        buf.writeInt(side);
    }

    @Nullable
    @Override
    public LogicalSide getExecutionSide()
    {
        return LogicalSide.CLIENT;
    }

    @Override
    public void onExecute(final NetworkEvent.Context ctxIn, final boolean isLogicalServer)
    {
        if (side == BREAK_BLOCK)
        {
            Minecraft.getInstance().particles.addBlockDestroyEffects(pos, block);
        }
        else
        {
            Minecraft.getInstance().particles.addBlockHitEffects(pos, Direction.byIndex(side));
        }
    }
}
