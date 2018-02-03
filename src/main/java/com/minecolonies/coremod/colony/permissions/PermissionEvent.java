package com.minecolonies.coremod.colony.permissions;

import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.network.PacketUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.UUID;

/**
 * Permission event class, used to store events happening in the colony.
 */
public class PermissionEvent
{
    /**
     * Player UUID.
     */
    private final UUID     id;

    /**
     * Player name.
     */
    private final String   name;

    /**
     * Action happening.
     */
    private final Action   action;

    /**
     * Impact permission.
     */
    private final BlockPos position;

    /**
     * Constructor for permissionevents.
     * @param id the player UUID.
     * @param name the player name.
     * @param action the action happening.
     * @param position the position of the action.
     */
    public PermissionEvent(final UUID id, final String name, final Action action, final BlockPos position)
    {
        this.id = id;
        this.name = name;
        this.action = action;
        this.position = position;
    }

    /**
     * Constructor for permissionevents. to load them from a ByteBuf.
     * @param buf the ByteBuf.
     */
    public PermissionEvent(final ByteBuf buf)
    {
        final UUID uuid = PacketUtils.readUUID(buf);
        if(uuid.equals(UUID.fromString("1-2-3-4-5")))
        {
            this.id = null;
        }
        else
        {
            this.id = uuid;
        }
        this.name = ByteBufUtils.readUTF8String(buf);
        this.action = Action.valueOf(ByteBufUtils.readUTF8String(buf));
        this.position = BlockPosUtil.readFromByteBuf(buf);
    }

    /**
     * The UUID of the player causing the event.
     * @return the UUID.
     */
    public UUID getId()
    {
        return id;
    }

    /**
     * The name of the player causing the event.
     * @return the name String.
     */
    public String getName()
    {
        return name;
    }

    /**
     * The action causing the event.
     * @return the Action
     */
    public Action getAction()
    {
        return action;
    }

    /**
     * The position at which the event had happened.
     * @return the BlockPos.
     */
    public BlockPos getPosition()
    {
        return position;
    }

    /**
     * Serialize the PermissioNEvent to a ByteBuf.
     * @param buf the buffer.
     */
    public void serialize(final ByteBuf buf)
    {
        if(id == null)
        {
            PacketUtils.writeUUID(buf, UUID.fromString("1-2-3-4-5"));
        }
        else
        {
            PacketUtils.writeUUID(buf, id);
        }
        ByteBufUtils.writeUTF8String(buf, name);
        ByteBufUtils.writeUTF8String(buf, action.toString());
        BlockPosUtil.writeToByteBuf(buf, position);
    }
}
