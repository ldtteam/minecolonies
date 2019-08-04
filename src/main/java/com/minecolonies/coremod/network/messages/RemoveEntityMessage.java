package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Message to remove an entity from the world.
 */
public class RemoveEntityMessage implements IMessage
{
    /**
     * Position to scan from.
     */
    private BlockPos from;

    /**
     * Position to scan to.
     */
    private BlockPos to;

    /**
     * The entity to remove from the world.
     */
    private String entityName;

    /**
     * Empty constructor used when registering the message.
     */
    public RemoveEntityMessage()
    {
        super();
    }

    /**
     * Create a message to remove an entity from the world.
     * @param pos1 start coordinate.
     * @param pos2 end coordinate.
     * @param entityName the entity to remove.
     */
    public RemoveEntityMessage(@NotNull final BlockPos pos1, @NotNull final BlockPos pos2, @NotNull final String entityName)
    {
        super();
        this.from = pos1;
        this.to = pos2;
        this.entityName = entityName;
    }

    @Override
    public void fromBytes(@NotNull final PacketBuffer buf)
    {
        from = BlockPosUtil.readFromByteBuf(buf);
        to = BlockPosUtil.readFromByteBuf(buf);
        entityName = buf.readString();
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        BlockPosUtil.writeToByteBuf(buf, from);
        BlockPosUtil.writeToByteBuf(buf, to);
        buf.writeString(entityName);
    }

    @Override
    public void messageOnServerThread(final RemoveEntityMessage message, final ServerPlayerEntity player)
    {
        if (!player.capabilities.isCreativeMode)
        {
            return;
        }

        final World world = player.getServerWorld();
        for(int x = Math.min(message.from.getX(), message.to.getX()); x <= Math.max(message.from.getX(), message.to.getX()); x++)
        {
            for (int y = Math.min(message.from.getY(), message.to.getY()); y <= Math.max(message.from.getY(), message.to.getY()); y++)
            {
                for (int z = Math.min(message.from.getZ(), message.to.getZ()); z <= Math.max(message.from.getZ(), message.to.getZ()); z++)
                {
                    final BlockPos here = new BlockPos(x, y, z);
                    final List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(here));

                    for(final Entity entity: list)
                    {
                        if (entity.getName().equals(message.entityName))
                        {
                            entity.setDead();
                        }
                    }
                }
            }
        }
    }
}
