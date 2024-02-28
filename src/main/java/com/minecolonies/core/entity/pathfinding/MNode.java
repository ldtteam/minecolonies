package com.minecolonies.core.entity.pathfinding;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Nodes used in pathfinding.
 */
public class MNode implements Comparable<MNode>
{
    /**
     * Values used in the generation of the hash of the node.
     */
    private static final int HASH_A = 12;
    private static final int HASH_B = 20;
    private static final int HASH_C = 24;

    /**
     * The position of the node.
     */
    @NotNull
    public final int x;
    public final int y;
    public final int z;

    /**
     * The hash of the node.
     */
    private final int hash;

    /**
     * The parent of the node (Node preceding this node).
     */
    @Nullable
    public MNode parent;

    /**
     * Added counter.
     */
    private int counterAdded;

    /**
     * Visited counter.
     */
    private int counterVisited;

    /**
     * Number of steps.
     */
    private int steps;

    /**
     * The cost of the node. A* g value.
     */
    private double cost;

    /**
     * The heuristic of the node. A* h value.
     */
    private double heuristic;

    /**
     * The score of the node. A* f value (g + h).
     */
    private double score;

    /**
     * Checks if the node has been closed already.
     */
    private boolean closed = false;

    /**
     * Checks if the node is on a ladder.
     */
    private boolean ladder = false;

    /**
     * Checks if the node is in water.
     */
    private boolean swimming = false;

    /**
     * If is on rails.
     */
    private boolean isOnRails = false;

    /**
     * Whether this is an air node
     */
    private boolean isCornerNode = false;

    /**
     * Wether this node got reached by an entity, for debug drawing
     */
    private boolean isReachedByWorker = false;

    /**
     * Create initial Node.
     *
     * @param heuristic heuristic estimate.
     */
    public MNode(@NotNull final int posX, final int posY, final int posZ, final double heuristic)
    {
        this(null, posX, posY, posZ, 0, heuristic, heuristic);
    }

    /**
     * Create a Node that inherits from a parent, and has a Cost and Heuristic estimate.
     *
     * @param parent    parent node arrives from.
     * @param cost      node cost.
     * @param heuristic heuristic estimate.
     * @param score     node total score.
     */
    public MNode(@Nullable final MNode parent, @NotNull final int posX, final int posY, final int posZ, final double cost, final double heuristic, final double score)
    {
        this.parent = parent;
        this.x = posX;
        this.y = posY;
        this.z = posZ;
        this.steps = parent == null ? 0 : (parent.steps + 1);
        this.cost = cost;
        this.heuristic = heuristic;
        this.score = score;
        this.hash = posX ^ ((posZ << HASH_A) | (posZ >> HASH_B)) ^ (posY << HASH_C);
    }

    /**
     * Create an MNode from a bytebuf.
     * @param byteBuf the buffer to load it from.
     */
    public MNode(final FriendlyByteBuf byteBuf)
    {
        if (byteBuf.readBoolean())
        {
            this.parent = new MNode(byteBuf.readVarInt(), byteBuf.readVarInt(), byteBuf.readVarInt(), 0);
        }
        this.x = byteBuf.readVarInt();
        this.y = byteBuf.readVarInt();
        this.z = byteBuf.readVarInt();
        this.cost = byteBuf.readDouble();
        this.heuristic = byteBuf.readDouble();
        this.score = byteBuf.readDouble();
        this.hash = x ^ ((z << HASH_A) | (z >> HASH_B)) ^ (y << HASH_C);
        this.isReachedByWorker = byteBuf.readBoolean();
    }

    /**
     * Serialize the Node to buf.
     * @param byteBuf
     */
    public void serializeToBuf(final FriendlyByteBuf byteBuf)
    {
        byteBuf.writeBoolean(this.parent != null);
        if (this.parent != null)
        {
            byteBuf.writeVarInt(this.parent.x);
            byteBuf.writeVarInt(this.parent.y);
            byteBuf.writeVarInt(this.parent.z);
        }
        byteBuf.writeVarInt(this.x);
        byteBuf.writeVarInt(this.y);
        byteBuf.writeVarInt(this.z);
        byteBuf.writeDouble(this.cost);
        byteBuf.writeDouble(this.heuristic);
        byteBuf.writeDouble(this.score);
        byteBuf.writeBoolean(this.isReachedByWorker);
    }

    @Override
    public int compareTo(@NotNull final MNode o)
    {
        //  Comparing doubles and returning value as int; can't simply cast the result
        if (score < o.score)
        {
            return -1;
        }

        if (score > o.score)
        {
            return 1;
        }

        if (heuristic < o.heuristic)
        {
            return -1;
        }

        if (heuristic > o.heuristic)
        {
            return 1;
        }

        //  In case of score tie, older node has better score
        return counterAdded - o.counterAdded;
    }

    @Override
    public int hashCode()
    {
        return hash;
    }

    @Override
    public boolean equals(@Nullable final Object o)
    {
        if (o != null && o.getClass() == this.getClass())
        {
            @Nullable final MNode other = (MNode) o;
            return x == other.x
                     && y == other.y
                     && z == other.z;
        }

        return false;
    }

    /**
     * Checks if node is closed.
     *
     * @return true if so.
     */
    public boolean isClosed()
    {
        return closed;
    }

    /**
     * Checks if node is on a ladder.
     *
     * @return true if so.
     */
    public boolean isLadder()
    {
        return ladder;
    }

    /**
     * Checks if node is in water.
     *
     * @return true if so.
     */
    public boolean isSwimming()
    {
        return swimming;
    }

    /**
     * Sets the node as closed.
     */
    public void setClosed()
    {
        closed = true;
    }

    /**
     * Getter for the visited counter.
     *
     * @return the amount.
     */
    public int getCounterVisited()
    {
        return counterVisited;
    }

    /**
     * Setter for the visited counter.
     *
     * @param counterVisited amount to set.
     */
    public void setCounterVisited(final int counterVisited)
    {
        this.counterVisited = counterVisited;
    }

    /**
     * Getter of the score of the node.
     *
     * @return the score.
     */
    public double getScore()
    {
        return score;
    }

    /**
     * Sets the node score.
     *
     * @param score the score.
     */
    public void setScore(final double score)
    {
        this.score = score;
    }

    /**
     * Getter of the cost of the node.
     *
     * @return the cost.
     */
    public double getCost()
    {
        return cost;
    }

    /**
     * Sets the node cost.
     *
     * @param cost the cost.
     */
    public void setCost(final double cost)
    {
        this.cost = cost;
    }

    /**
     * Getter of the steps.
     *
     * @return the steps.
     */
    public int getSteps()
    {
        return steps;
    }

    /**
     * Sets the amount of steps.
     *
     * @param steps the amount.
     */
    public void setSteps(final int steps)
    {
        this.steps = steps;
    }

    /**
     * Sets the node as a ladder node.
     */
    public void setLadder()
    {
        ladder = true;
    }

    /**
     * Sets the node as a swimming node.
     */
    public void setSwimming()
    {
        swimming = true;
    }

    /**
     * Getter of the heuristic.
     *
     * @return the heuristic.
     */
    public double getHeuristic()
    {
        return heuristic;
    }

    /**
     * Sets the node heuristic.
     *
     * @param heuristic the heuristic.
     */
    public void setHeuristic(final double heuristic)
    {
        this.heuristic = heuristic;
    }

    /**
     * Getter of the added counter.
     *
     * @return the amount.
     */
    public int getCounterAdded()
    {
        return counterAdded;
    }

    /**
     * Sets the added counter.
     *
     * @param counterAdded amount to set.
     */
    public void setCounterAdded(final int counterAdded)
    {
        this.counterAdded = counterAdded;
    }

    /**
     * Setup rails params.
     *
     * @param isOnRails if on rails.
     */
    public void setOnRails(final boolean isOnRails)
    {
        this.isOnRails = isOnRails;
    }

    /**
     * Check if is on rails.
     *
     * @return true if so.
     */
    public boolean isOnRails()
    {
        return isOnRails;
    }

    /**
     * Marks the node as reached by the worker
     * @param reached if reached or reset.
     */
    public void setReachedByWorker(final boolean reached)
    {
        isReachedByWorker = reached;
    }

    /**
     * Whether the node is reached by a worker
     *
     * @return reached
     */
    public boolean isReachedByWorker()
    {
        return isReachedByWorker;
    }

    /**
     * Marks the node as reached by the worker
     */
    public void setCornerNode(boolean isCornerNode)
    {
        this.isCornerNode = isCornerNode;
    }

    /**
     * Whether the node is reached by a worker
     *
     * @return reached
     */
    public boolean isCornerNode()
    {
        return isCornerNode;
    }
}
