package com.minecolonies.coremod.network.messages;

import com.minecolonies.api.network.IMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Empty constructor used when registering the 
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
        from = buf.readBlockPos();
        to = buf.readBlockPos();
        entityName = buf.readString(32767);
    }

    @Override
    public void toBytes(@NotNull final PacketBuffer buf)
    {
        buf.writeBlockPos(from);
        buf.writeBlockPos(to);
        buf.writeString(entityName);
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
        if (!player.isCreative())
        {
            return;
        }

        final World world = player.world;
        for(int x = Math.min(from.getX(), to.getX()); x <= Math.max(from.getX(), to.getX()); x++)
        {
            for (int y = Math.min(from.getY(), to.getY()); y <= Math.max(from.getY(), to.getY()); y++)
            {
                for (int z = Math.min(from.getZ(), to.getZ()); z <= Math.max(from.getZ(), to.getZ()); z++)
                {
                    final BlockPos here = new BlockPos(x, y, z);
                    final List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(here));

                    for(final Entity entity: list)
                    {
                        if (entity.getName().equals(entityName))
                        {
                            entity.remove();
                        }
                    }
                }
            }
        }
    }
}
