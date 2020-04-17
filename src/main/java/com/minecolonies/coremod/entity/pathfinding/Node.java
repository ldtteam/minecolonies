package com.minecolonies.coremod.entity.pathfinding;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Nodes used in pathfinding.
 */
public class Node implements Comparable<Node>
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
    public final BlockPos pos;

    /**
     * The hash of the node.
     */
    private final int hash;

    /**
     * The parent of the node (Node preceding this node).
     */
    @Nullable
    public Node parent;

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
     * The cost of the node.
     * A* g value.
     */
    private double cost;

    /**
     * The heuristic of the node.
     * A* h value.
     */
    private double heuristic;

    /**
     * The score of the node.
     * A* f value (g + h).
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
     * Create initial Node.
     *
     * @param pos       coordinates of node.
     * @param heuristic heuristic estimate.
     */
    public Node(@NotNull final BlockPos pos, final double heuristic)
    {
        this(null, pos, 0, heuristic, heuristic);
    }

    /**
     * Create a Node that inherits from a parent, and has a Cost and Heuristic estimate.
     *
     * @param parent    parent node arrives from.
     * @param pos       coordinate of node.
     * @param cost      node cost.
     * @param heuristic heuristic estimate.
     * @param score     node total score.
     */
    public Node(@Nullable final Node parent, @NotNull final BlockPos pos, final double cost, final double heuristic, final double score)
    {
        this.parent = parent;
        this.pos = pos;
        this.steps = parent == null ? 0 : (parent.steps + 1);
        this.cost = cost;
        this.heuristic = heuristic;
        this.score = score;
        this.hash = pos.getX() ^ ((pos.getZ() << HASH_A) | (pos.getZ() >> HASH_B)) ^ (pos.getY() << HASH_C);
    }

    @Override
    public int compareTo(@NotNull final Node o)
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
            @Nullable final Node other = (Node) o;
            return pos.getX() == other.pos.getX()
                     && pos.getY() == other.pos.getY()
                     && pos.getZ() == other.pos.getZ();
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
     * @param isOnRails if on rails.
     */
    public void setOnRails(final boolean isOnRails)
    {
        this.isOnRails = isOnRails;
    }

    /**
     * Check if is on rails.
     * @return true if so.
     */
    public boolean isOnRails()
    {
        return isOnRails;
    }
}
