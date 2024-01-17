package com.minecolonies.core.entity.ai.workers.util;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Vec2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Miner Node Data StructureIterator.
 * <p>
 * When a node is completed we should add the surrounding nodes to level as AVAILABLE also note that we don't want node (0, -1) because there will be a ladder on the back wall of
 * the initial node, and we cant put the connection through the ladder
 */
public class MineNode
{
    /**
     * Tags used to save and retrieve data from NBT.
     */
    private static final String TAG_X       = "idX";
    private static final String TAG_Z       = "idZ";
    private static final String TAG_ROT     = "rotation";
    private static final String TAG_STYLE   = "Style";
    private static final String TAG_STATUS  = "Status";
    private static final String TAG_PARENTX = "ParentX";
    private static final String TAG_PARENTZ = "ParentZ";

    /**
     * Random object.
     */
    private final Random random = new Random();

    /**
     * The distance to the center of the next node.
     */
    private static final int DISTANCE_TO_NEXT_NODE = 7;

    /**
     * X position of the Node.
     */
    private final int x;

    /**
     * Z position of the node.
     */
    private final int z;

    /**
     * The rotation that was calculated and used at build time
     */
    private Optional<Integer> rot = Optional.empty();

    /**
     * Central position of parent node.
     */
    @Nullable
    private final Vec2i parent;

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
     * Initializes the node. Requires a location in the node as parameters
     *
     * @param x      X-coordinate in the node
     * @param z      Z-coordinate in the node
     * @param parent the parent of the node.
     */
    public MineNode(final int x, final int z, @Nullable final Vec2i parent)
    {
        this.x = x;
        this.z = z;
        this.style = NodeType.UNDEFINED;
        this.status = NodeStatus.AVAILABLE;
        this.parent = parent;
    }

    /**
     * Creates a node from the NBT Tag. Returns the created node
     *
     * @param compound Compound to read from
     * @return Node created from compound
     */
    @NotNull
    public static MineNode createFromNBT(@NotNull final CompoundTag compound)
    {
        // for backwards compatibility check if the types are doubles
        final boolean hasDoubles = compound.contains(TAG_X);

        final int x;
        final int z;
        if (hasDoubles)
        {
            x = Mth.floor(compound.getDouble(TAG_X));
            z = Mth.floor(compound.getDouble(TAG_Z));
        }
        else
        {
            x = compound.getInt(TAG_X);
            z = compound.getInt(TAG_Z);
        }

        NodeType style;
        try
        {
            style = NodeType.valueOf(compound.getString(TAG_STYLE));
        }
        catch (final IllegalArgumentException ex)
        {
            style = NodeType.BEND_RIGHT;
        }

        final NodeStatus status = NodeStatus.valueOf(compound.getString(TAG_STATUS));

        Vec2i parent = null;
        if (compound.contains(TAG_PARENTX))
        {
            if (hasDoubles)
            {
                parent = new Vec2i(
                  Mth.floor(compound.getDouble(TAG_PARENTX)),
                  Mth.floor(compound.getDouble(TAG_PARENTZ)));
            }
            else
            {
                parent = new Vec2i(compound.getInt(TAG_PARENTX), compound.getInt(TAG_PARENTZ));
            }
        }

        //Set the node status in all directions.
        @NotNull final MineNode node = new MineNode(x, z, parent);
        if (style == NodeType.UNDEFINED)
        {
            Log.getLogger().error("Minecolonies Node " + x + "," + z + " has an undefined style, please tell the mod author about this");
        }
        node.setStyle(style);
        node.setStatus(status);

        if (compound.contains(TAG_ROT))
        {
            node.setRot(compound.getInt(TAG_ROT));
        }

        return node;
    }

    /**
     * Writes the node to a NBT-compound.
     *
     * @param compound Compound to write to
     */
    public void write(@NotNull final CompoundTag compound)
    {
        compound.putInt(TAG_X, x);
        compound.putInt(TAG_Z, z);

        if (rot.isPresent())
        {
            compound.putInt(TAG_ROT, rot.get());
        }

        compound.putString(TAG_STYLE, style.name());
        compound.putString(TAG_STATUS, status.name());

        if (parent != null)
        {
            compound.putInt(TAG_PARENTX, parent.getX());
            compound.putInt(TAG_PARENTZ, parent.getZ());
        }
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
    public Vec2i getParent()
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
     * Returns the x-coordinate in the node.
     *
     * @return x-coordinate
     */
    public int getX()
    {
        return x;
    }

    /**
     * Returns the z-coordinate in the node.
     *
     * @return z-coordinate
     */
    public int getZ()
    {
        return z;
    }

    /**
     * Calculates the next Node north.
     *
     * @return position of the new Node.
     */
    public Vec2i getNorthNodeCenter()
    {
        return new Vec2i(getX(), getZ() - DISTANCE_TO_NEXT_NODE);
    }

    /**
     * Calculates the next Node south.
     *
     * @return position of the new Node.
     */
    public Vec2i getSouthNodeCenter()
    {
        return new Vec2i(getX(), getZ() + DISTANCE_TO_NEXT_NODE);
    }

    /**
     * Calculates the next Node east.
     *
     * @return position of the new Node.
     */
    public Vec2i getEastNodeCenter()
    {
        return new Vec2i(getX() + DISTANCE_TO_NEXT_NODE, getZ());
    }

    /**
     * Calculates the next Node west.
     *
     * @return position of the new Node.
     */
    public Vec2i getWestNodeCenter()
    {
        return new Vec2i(getX() - DISTANCE_TO_NEXT_NODE, getZ());
    }

    /**
     * Return a random next node to work at, might be at this node or at a parent.
     *
     * @param level the level it is part of.
     * @param step  the step.
     * @return the next node to go to.
     */
    @Nullable
    public MineNode getRandomNextNode(final MinerLevel level, final int step)
    {
        if (step > 3)
        {
            return null;
        }

        final MineNode nextNode;
        switch (random.nextInt(3))
        {
            case 0:
                nextNode = level.getOpenNode(getNorthNodeCenter());
                break;
            case 1:
                nextNode = level.getOpenNode(getSouthNodeCenter());
                break;
            case 2:
                nextNode = level.getOpenNode(getEastNodeCenter());
                break;
            default:
                nextNode = level.getOpenNode(getWestNodeCenter());
        }

        if (nextNode == null || nextNode.style == NodeType.SHAFT)
        {
            final MineNode parent = level.getOpenNode(getParent());
            return parent == null ? null : parent.getRandomNextNode(level, step + 1);
        }
        return nextNode;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final MineNode node = (MineNode) o;
        return x == node.x &&
                 z == node.z;
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(x, z);
    }

    /**
     * Sets the status of the node. AVAILABLE means it can be mined IN_PROGRESS means it is currently being mined COMPLETED means it has been mined and all torches/wood structure
     * has been placed LADDER means this side has the ladder and must not be mined
     */
    public enum NodeStatus
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
    public enum NodeType
    {
        //Main shaft
        SHAFT("infrastructure/mineshafts/minermainshaft"),
        //Node on the back of the ladder (Don't mine the ladder)
        LADDER_BACK(""),
        //Simple straight tunnel.
        TUNNEL("infrastructure/mineshafts/minerx2top"),
        //Crossroad structure
        CROSSROAD("infrastructure/mineshafts/minerx4"),
        //Bending tunnel
        BEND_RIGHT("infrastructure/mineshafts/minerx2right"),
        //New node, undefined
        UNDEFINED(""),
        BEND_LEFT("infrastructure/mineshafts/minerx2left"),
        // Three way cross
        CROSS_THREE_LEFT_RIGHT("infrastructure/mineshafts/minerx3leftright"),
        CROSS_THREE_TOP_LEFT("infrastructure/mineshafts/minerx3topleft"),
        CROSS_THREE_TOP_RIGHT("infrastructure/mineshafts/minerx3topright");

        /**
         * List of all valid types.
         */
        public static final ImmutableList<NodeType> SIDE_NODES =
          ImmutableList.of(TUNNEL, CROSSROAD, BEND_RIGHT, BEND_LEFT, CROSS_THREE_LEFT_RIGHT, CROSS_THREE_TOP_LEFT, CROSS_THREE_TOP_RIGHT);

        /**
         * The schematic string.
         */
        private String schemName;

        /**
         * Create a new node type.
         *
         * @param schemName the schematic name.
         */
        NodeType(final String schemName)
        {
            this.schemName = schemName;
        }

        /**
         * Get the matching schematic name.
         *
         * @return the file name string.
         */
        public String getSchematicName()
        {
            return schemName;
        }
    }

    /**
     * Get rotation stored in the node as an optional, only set if actually stored.
     *
     * @return
     */
    public Optional<Integer> getRot()
    {
        return rot;
    }

    /**
     * Set rotation storaged in the node
     *
     * @param rot
     */
    public void setRot(int rot)
    {
        this.rot = Optional.of(rot);
    }
}
