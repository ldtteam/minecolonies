package com.minecolonies.entity.pathfinding;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Node implements Comparable<Node>
{
    @NotNull
    public final  BlockPos pos;
    private final int      hash;
    @Nullable
    public        Node     parent;
    public        int      counterAdded;
    public        int      counterVisited;
    public        int      steps;

    // A* g value
    public double cost;

    //  A* h value
    public double heuristic;

    //  A* f value (g + h)
    public double score;

    public boolean closed     = false;
    public boolean isLadder   = false;
    public boolean isSwimming = false;

    /**
     * Create initial Node
     *
     * @param pos       coordinates of node
     * @param heuristic heuristic estimate
     */
    public Node(@NotNull BlockPos pos, double heuristic)
    {
        this(null, pos, 0, heuristic, heuristic);
    }

    /**
     * Create a Node that inherits from a parent, and has a Cost and Heuristic estimate
     *
     * @param parent    parent node arrives from
     * @param pos       coordinate of node
     * @param cost      node cost
     * @param heuristic heuristic estimate
     * @param score     node total score
     */
    public Node(@Nullable Node parent, @NotNull BlockPos pos, double cost, double heuristic, double score)
    {
        this.parent = parent;
        this.pos = pos;
        this.steps = parent != null ? (parent.steps + 1) : 0;
        this.cost = cost;
        this.heuristic = heuristic;
        this.score = score;
        this.hash = pos.getX() ^ ((pos.getZ() << 12) | (pos.getZ() >> 20)) ^ (pos.getY() << 24);
    }

    @Override
    public int compareTo(Node o)
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
    public boolean equals(@Nullable Object o)
    {
        if (o != null && o.getClass() == this.getClass())
        {
            @Nullable Node other = (Node) o;
            return pos.getX() == other.pos.getX() &&
                     pos.getY() == other.pos.getY() &&
                     pos.getZ() == other.pos.getZ();
        }

        return false;
    }
}
