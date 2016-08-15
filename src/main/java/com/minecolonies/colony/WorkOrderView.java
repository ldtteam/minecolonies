package com.minecolonies.colony;

import com.minecolonies.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * The WorkOrderView is the client-side representation of a WorkOrders.
 * Views contain the WorkOrder's data that is relevant to a Client, in a more client-friendly form
 * Mutable operations on a View result in a message to the server to perform the operation
 */
public class WorkOrderView
{
    /**
     * Attributes
     */
    private int id;
    private int priority;
    private String value;
    private int type;

    public int getPriority()
    {
        return priority;
    }

    public String getValue()
    {
        return value;
    }

    public int getType()
    {
        return type;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }


    /**
     * Deserialize the attributes and variables from transition
     *
     * @param buf Byte buffer to deserialize
     */
    public void deserialize(ByteBuf buf)
    {
        id = buf.readInt();
        priority = buf.readInt();
        type = buf.readInt();
        value = ByteBufUtils.readUTF8String(buf);
    }
}
