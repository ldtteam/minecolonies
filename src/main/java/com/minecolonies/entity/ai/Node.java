package com.minecolonies.entity.ai;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Miner Node Data Structure
 *
 *  When a node is completed we should add the surrounding nodes to level as AVAILABLE
 *      also note that we don't want node (0, -1) because there will be a ladder on the back
 *      wall of the initial node, and we cant put the connection through the ladder
 *
 * @author Colton
 */
public class Node
{
    /**
     * Location of the node
     */
    private int x, z;

    private Status status;
    private int vectorX;
    private int vectorZ;

    /**
     * Sets the status of the node
     *  AVAILABLE means it can be mined
     *  IN_PROGRESS means it is currently being mined
     *  COMPLETED means it has been mined and all torches/wood structure has been placed
     *
     *  this doesn't have to be final, more stages can be added or this doesn't have to be used
     */
    enum Status
    {
        AVAILABLE,
        IN_PROGRESS,
        COMPLETED
    }

    private static final String TAG_X = "idX";
    private static final String TAG_Z = "idY";//TODO change to z, but will break saves
    private static final String TAG_STATUS = "Status";
    private static final String TAG_VECTOR_X = "vectorX";
    private static final String TAG_VECTOR_Z = "vectorZ";

    public Node(int x, int z, int vectorX, int vectorZ)
    {
        this.x = x;
        this.z = z;
        status = Status.AVAILABLE;
        this.vectorX = vectorX;
        this.vectorZ = vectorZ;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setInteger(TAG_X, x);
        compound.setInteger(TAG_Z, z);

        compound.setString(TAG_STATUS, status.name());

        compound.setInteger(TAG_VECTOR_X, vectorX);
        compound.setInteger(TAG_VECTOR_Z, vectorZ);
    }

    public static Node createFromNBT(NBTTagCompound compound)
    {
        int x = compound.getInteger(TAG_X);
        int z = compound.getInteger(TAG_Z);
        Status status = Status.valueOf(compound.getString(TAG_STATUS));
        int vectorX = compound.getInteger(TAG_VECTOR_X);
        int vectorZ = compound.getInteger(TAG_VECTOR_Z);

        Node node = new Node(x, z, vectorX, vectorZ);
        node.status = status;

        return node;
    }

    public int getX()
    {
        return x;
    }

    public int getZ()
    {
        return z;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public int getVectorZ()
    {
        return vectorZ;
    }

    public int getVectorX()
    {
        return vectorX;
    }
}