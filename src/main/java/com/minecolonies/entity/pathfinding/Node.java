package com.minecolonies.entity.pathfinding;

import com.sun.istack.internal.NotNull;

public class Node implements Comparable<Node>
{
    public Node parent;
    public final int x, y, z;
    public final int hash;

    public int counterAdded;
    public int counterVisited;
    public int steps;
    public double cost;     //  A* g value
    public double heuristic;//  A* h value
    public double score;    //  A* f value (g + h)

    public boolean closed = false;
    public boolean isLadder = false;
    public boolean isSwimming = false;

    /**
     * Create a Node that inherits from a parent, and has a Cost and Heuristic estimate
     * @param parent parent node arrives from
     * @param x,y,z coordinate of node
     * @param cost
     * @param score
     */
    public Node(Node parent, int x, int y, int z, double cost, double heuristic, double score)
    {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.z = z;
        this.steps = parent != null ? parent.steps + 1 : 0;
        this.cost = cost;
        this.heuristic = heuristic;
        this.score = score;
        this.hash = x ^ (z << 12 | z >> 20) ^ (y << 24);
    }

    /**
     * Create initial Node
     * @param x,y,z coordinate of node
     */
    public Node(int x, int y, int z, double heuristic)
    {
        this(null, x, y, z, 0, heuristic, heuristic);
    }

    @Override
    public int compareTo(Node o)
    {
        //  Comparing doubles and returning value as int; can't simply cast the result
        if (score < o.score) return -1;
        if (score > o.score) return 1;
        if (heuristic < o.heuristic)
             return -1;
        if (heuristic > o.heuristic)
            return 1;
////        if ((score - o.score) <= -1E-14) return -1;
////        if ((score - o.score) >= 1E-14) return 1;
//        return 0;
        return counterAdded - o.counterAdded;   //  In case of score tie, older node has better score
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Node)
        {
            Node other = (Node)o;
            return x == other.x &&
                    y == other.y &&
                    z == other.z;
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return hash;
    }
}
