package com.minecolonies.core.network.messages.client;

import com.ldtteam.common.network.AbstractClientPlayMessage;
import com.ldtteam.common.network.PlayMessageType;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Handles the server telling nearby clients to render a particle effect. Created: February 10, 2016
 *
 * @author Colton
 */
public class BlockParticleEffectMessage extends AbstractClientPlayMessage
{
    public static final PlayMessageType<?> TYPE = PlayMessageType.forClient(Constants.MOD_ID, "block_particle_effect", BlockParticleEffectMessage::new);

    public static final int BREAK_BLOCK = -1;

    private final BlockPos   pos;
    private final BlockState block;
    private final int        side;

    /**
     * Sends a message for particle effect.
     *
     * @param pos   Coordinates
     * @param state Block State
     * @param side  Side of the block causing effect
     */
    public BlockParticleEffectMessage(final BlockPos pos, @NotNull final BlockState state, final int side)
    {
        super(TYPE);
        this.pos = pos;
        this.block = state;
        this.side = side;
    }

    public BlockParticleEffectMessage(final RegistryFriendlyByteBuf buf, final PlayMessageType<?> type)
    {
        super(buf, type);
        pos = buf.readBlockPos();
        block = Block.stateById(buf.readInt());
        side = buf.readInt();
    }

    @Override
    protected void toBytes(@NotNull final RegistryFriendlyByteBuf buf)
    {
        buf.writeBlockPos(pos);
        buf.writeInt(Block.getId(block));
        buf.writeInt(side);
    }

    @Override
    protected void onExecute(final IPayloadContext ctxIn, final Player player)
    {
        if (side == BREAK_BLOCK)
        {
            Minecraft.getInstance().particleEngine.destroy(pos, block);
        }
        else
        {
            Minecraft.getInstance().particleEngine.crack(pos, Direction.from3DDataValue(side));
        }
    }
}
