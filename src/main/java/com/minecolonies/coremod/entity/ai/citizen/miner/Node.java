package com.minecolonies.coremod.entity.ai.citizen.miner;

import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;

/**
 * Miner Node Data Structure.
 * <p>
 * When a node is completed we should add the surrounding nodes to level as AVAILABLE
 * also note that we don't want node (0, -1) because there will be a ladder on the back
 * wall of the initial node, and we cant put the connection through the ladder
 *
 */
public class Node
{
    /**
     * Tags used to save and retrieve data from NBT.
     */
    private static final String TAG_X       = "idX";
    private static final String TAG_Z       = "idZ";
    private static final String TAG_STYLE   = "Style";
    private static final String TAG_STATUS  = "Status";
    private static final String TAG_PARENTX = "ParentX";
    private static final String TAG_PARENTZ = "ParentZ";

    /**
     * The distance to the center of the next node.
     */
    private static final int DISTANCE_TO_NEXT_NODE = 7;

    /**
     * X position of the Node.
     */
    private final double x;

    /**
     * Z position of the node.
     */
    private final double z;

    /**
     * Style of the node.
     */
    @NotNull
    private NodeType style;

    /**
     * Status of the node.
     */
    @NotNull
    private NodeStatus status;

    /**
     * Central position of parent node.
     */
    @Nullable
    private final Point2D parent;


    /**
     * Initializes the node.
     * Requires a location in the node as parameters
     *
     * @param x X-coordinate in the node
     * @param z Z-coordinate in the node
     * @param parent the parent of the node.
     */
    public Node(final double x, final double z, @Nullable Point2D parent)
    {
        this.x = x;
        this.z = z;
        this.style = NodeType.CROSSROAD;
        this.status = NodeStatus.AVAILABLE;
        this.parent = parent;
    }

    /**
     * Creates a node from the NBT Tag.
     * Returns the created node
     *
     * @param compound Compound to read from
     * @return Node created from compound
     */
    @NotNull
    public static Node createFromNBT(@NotNull final NBTTagCompound compound)
    {
        final double x = compound.getDouble(TAG_X);
        final double z = compound.getDouble(TAG_Z);

        final NodeType style = NodeType.valueOf(compound.getString(TAG_STYLE));

        final NodeStatus status = NodeStatus.valueOf(compound.getString(TAG_STATUS));

        final Point2D tempParent = compound.hasKey(TAG_PARENTX) ? new Point2D.Double(compound.getDouble(TAG_PARENTX), compound.getDouble(TAG_PARENTZ)) : null;

        //Set the node status in all directions.
        @NotNull final Node node = new Node(x, z, tempParent);
        node.setStyle(style);
        node.setStatus(status);

        return node;
    }


    /**
     * Writes the node to a NBT-compound.
     *
     * @param compound Compound to write to
     */
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        compound.setDouble(TAG_X, x);
        compound.setDouble(TAG_Z, z);

        compound.setString(TAG_STYLE, style.name());
        compound.setString(TAG_STATUS, status.name());

        if(parent != null)
        {
            compound.setDouble(TAG_PARENTX, parent.getX());
            compound.setDouble(TAG_PARENTZ, parent.getY());
        }
    }

    /**
     * Returns the x-coordinate in the node.
     *
     * @return x-coordinate
     */
    public double getX()
    {
        return x;
    }

    /**
     * Returns the z-coordinate in the node.
     *
     * @return z-coordinate
     */
    public double getZ()
    {
        return z;
    }

    /**
     * Returns the {@link NodeStatus} of the current node.
     *
     * @return {@link NodeStatus}
     */
    @NotNull
    public NodeStatus getStatus()
    {
        return status;
    }

    /**
     * Sets the status of the current node.
     *
     * @param status {@link NodeStatus}
     */
    public void setStatus(@NotNull final NodeStatus status)
    {
        this.status = status;
    }

    /**
     * Getter for the parent value.
     *
     * @return tuple of parent position.
     */
    @Nullable
    public Point2D getParent()
    {
        return this.parent;
    }

    @NotNull
    @Override
    public String toString()
    {
        return "Node{" + "x=" + x
                + ", z=" + z
                + ", style=" + style
                + ", status=" + status
                + '}';
    }

    /**
     * Returns the {@link NodeType} of the current node.
     *
     * @return {@link NodeType}
     */
    @NotNull
    public NodeType getStyle()
    {
        return style;
    }

    /**
     * Sets the {@link NodeType} of the current node.
     *
     * @param style {@link NodeType}
     */
    public void setStyle(@NotNull final NodeType style)
    {
        this.style = style;
    }

    /**
     * Calculates the next Node north.
     *
     * @return position of the new Node.
     */
    public Point2D.Double getNorthNodeCenter()
    {
        return new Point2D.Double(getX(), getZ() - DISTANCE_TO_NEXT_NODE);
    }

    /**
     * Calculates the next Node south.
     *
     * @return position of the new Node.
     */
    public Point2D.Double getSouthNodeCenter()
    {
        return new Point2D.Double(getX(), getZ() + DISTANCE_TO_NEXT_NODE);
    }

    /**
     * Calculates the next Node east.
     *
     * @return position of the new Node.
     */
    public Point2D.Double getEastNodeCenter()
    {
        return new Point2D.Double(getX() + DISTANCE_TO_NEXT_NODE, getZ());
    }

    /**
     * Calculates the next Node west.
     *
     * @return position of the new Node.
     */
    public Point2D.Double getWesthNodeCenter()
    {
        return new Point2D.Double(getX() - DISTANCE_TO_NEXT_NODE, getZ());
    }

    /**
     * Sets the status of the node.
     * AVAILABLE means it can be mined
     * IN_PROGRESS means it is currently being mined
     * COMPLETED means it has been mined and all torches/wood structure has been placed
     * LADDER means this side has the ladder and must not be mined
     */
    enum NodeStatus
    {
        //Not built yet.
        AVAILABLE,
        //In building progress
        IN_PROGRESS,
        //Already finished
        COMPLETED
    }

    /**
     * Sets the node style used.
     */
    enum NodeType
    {
        //Main shaft
        SHAFT,
        //Node on the back of the ladder (Don't mine the ladder)
        LADDER_BACK,
        //Simple straight tunnle.
        TUNNEL,
        //Crossroad structure
        CROSSROAD,
        //Bending tunnle
        BEND
    }
}
