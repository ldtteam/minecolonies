package com.minecolonies.entity.ai;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Miner Node Data Structure
 * <p>
 * When a node is completed we should add the surrounding nodes to level as AVAILABLE
 * also note that we don't want node (0, -1) because there will be a ladder on the back
 * wall of the initial node, and we cant put the connection through the ladder
 *
 * @author Colton, Kostronor
 */
public class Node {
    private static final String TAG_X = "idX";
    private static final String TAG_Z = "idZ";//TODO change to z, but will break saves
    private static final String TAG_STATUS = "Status";
    private static final String TAG_STATUS_POSITIVE_X = "positiveX";
    private static final String TAG_STATUS_NEGATIVE_X = "negativeX";
    private static final String TAG_STATUS_POSITIVE_Z = "positiveZ";
    private static final String TAG_STATUS_NEGATIVE_Z = "negativeZ";
    /**
     * Location of the node
     */
    private int x, z;
    private Status status;
    private Status directionPosX; //+X
    private Status directionNegX; //-X
    private Status directionPosZ; //+Z
    private Status directionNegZ; //-Z

    public Node(int x, int z) {
        this.x = x;
        this.z = z;
        status = Status.AVAILABLE;
        directionPosX = Status.AVAILABLE;
        directionNegX = Status.AVAILABLE;
        directionPosZ = Status.AVAILABLE;
        directionNegZ = Status.AVAILABLE;
    }

    public static Node createFromNBT(NBTTagCompound compound) {
        int x = compound.getInteger(TAG_X);
        int z = compound.getInteger(TAG_Z);

        Status status = Status.valueOf(compound.getString(TAG_STATUS));

        Status directionPosX = Status.valueOf(compound.getString(TAG_STATUS_POSITIVE_X));
        Status directionNegX = Status.valueOf(compound.getString(TAG_STATUS_NEGATIVE_X));
        Status directionPosZ = Status.valueOf(compound.getString(TAG_STATUS_POSITIVE_Z));
        Status directionNegZ = Status.valueOf(compound.getString(TAG_STATUS_NEGATIVE_Z));

        Node node = new Node(x, z);
        node.setStatus(status);
        node.setDirectionPosX(directionPosX);
        node.setDirectionNegX(directionNegX);
        node.setDirectionPosZ(directionPosZ);
        node.setDirectionNegZ(directionNegZ);

        return node;
    }

    public Status getDirectionPosX() {
        return directionPosX;
    }

    public void setDirectionPosX(Status directionPosX) {
        this.directionPosX = directionPosX;
    }

    public Status getDirectionNegX() {
        return directionNegX;
    }

    public void setDirectionNegX(Status directionNegX) {
        this.directionNegX = directionNegX;
    }

    public Status getDirectionPosZ() {
        return directionPosZ;
    }

    public void setDirectionPosZ(Status directionPosZ) {
        this.directionPosZ = directionPosZ;
    }

    public Status getDirectionNegZ() {
        return directionNegZ;
    }

    public void setDirectionNegZ(Status directionNegZ) {
        this.directionNegZ = directionNegZ;
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger(TAG_X, x);
        compound.setInteger(TAG_Z, z);

        compound.setString(TAG_STATUS, status.name());

        compound.setString(TAG_STATUS_POSITIVE_X, directionPosX.name());
        compound.setString(TAG_STATUS_NEGATIVE_X, directionNegX.name());
        compound.setString(TAG_STATUS_POSITIVE_Z, directionPosZ.name());
        compound.setString(TAG_STATUS_NEGATIVE_Z, directionNegZ.name());
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getVectorX() {
        return 0;
    }

    public int getVectorZ() {
        return 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Node{");
        sb.append("x=").append(x);
        sb.append(", z=").append(z);
        sb.append(", status=").append(status);
        sb.append(", directionPosX=").append(directionPosX);
        sb.append(", directionNegX=").append(directionNegX);
        sb.append(", directionPosZ=").append(directionPosZ);
        sb.append(", directionNegZ=").append(directionNegZ);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Sets the status of the node
     * AVAILABLE means it can be mined
     * IN_PROGRESS means it is currently being mined
     * COMPLETED means it has been mined and all torches/wood structure has been placed
     * <p>
     * this doesn't have to be final, more stages can be added or this doesn't have to be used
     */
    enum Status {
        AVAILABLE,
        IN_PROGRESS,
        COMPLETED,
        LADDER
    }
}