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
    private NodeStatus status;
    private NodeStatus directionPosX; //+X
    private NodeStatus directionNegX; //-X
    private NodeStatus directionPosZ; //+Z
    private NodeStatus directionNegZ; //-Z

    public Node(int x, int z) {
        this.x = x;
        this.z = z;
        status = NodeStatus.AVAILABLE;
        directionPosX = NodeStatus.AVAILABLE;
        directionNegX = NodeStatus.AVAILABLE;
        directionPosZ = NodeStatus.AVAILABLE;
        directionNegZ = NodeStatus.AVAILABLE;
    }

    public static Node createFromNBT(NBTTagCompound compound) {
        int x = compound.getInteger(TAG_X);
        int z = compound.getInteger(TAG_Z);

        NodeStatus status = NodeStatus.valueOf(compound.getString(TAG_STATUS));

        NodeStatus directionPosX = NodeStatus.valueOf(compound.getString(TAG_STATUS_POSITIVE_X));
        NodeStatus directionNegX = NodeStatus.valueOf(compound.getString(TAG_STATUS_NEGATIVE_X));
        NodeStatus directionPosZ = NodeStatus.valueOf(compound.getString(TAG_STATUS_POSITIVE_Z));
        NodeStatus directionNegZ = NodeStatus.valueOf(compound.getString(TAG_STATUS_NEGATIVE_Z));

        Node node = new Node(x, z);
        node.setStatus(status);
        node.setDirectionPosX(directionPosX);
        node.setDirectionNegX(directionNegX);
        node.setDirectionPosZ(directionPosZ);
        node.setDirectionNegZ(directionNegZ);

        return node;
    }

    public NodeStatus getDirectionPosX() {
        return directionPosX;
    }

    public void setDirectionPosX(NodeStatus directionPosX) {
        this.directionPosX = directionPosX;
    }

    public NodeStatus getDirectionNegX() {
        return directionNegX;
    }

    public void setDirectionNegX(NodeStatus directionNegX) {
        this.directionNegX = directionNegX;
    }

    public NodeStatus getDirectionPosZ() {
        return directionPosZ;
    }

    public void setDirectionPosZ(NodeStatus directionPosZ) {
        this.directionPosZ = directionPosZ;
    }

    public NodeStatus getDirectionNegZ() {
        return directionNegZ;
    }

    public void setDirectionNegZ(NodeStatus directionNegZ) {
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

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
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

}