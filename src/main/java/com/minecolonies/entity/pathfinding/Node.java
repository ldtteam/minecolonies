package com.minecolonies.entity.pathfinding;

public class Node implements Comparable<Node>
{
    public Node parent;
    public final int x, y, z;

    public int counterAdded;
    public int counterVisited;
    public double cost;     //  A* g value
    public double score;    //  A* f value (g + heuristic) value

    public boolean closed = false;
    public boolean isLadder = false;

    /**
     * Create a Node that inherits from a parent, and has a Cost and Heuristic estimate
     * @param parent parent node arrives from
     * @param x,y,z coordinate of node
     * @param cost
     * @param score
     */
    public Node(Node parent, int x, int y, int z, double cost, double score)
    {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.z = z;
        this.cost = cost;
        this.score = score;
    }

    /**
     * Create initial Node
     * @param x,y,z coordinate of node
     */
    public Node(int x, int y, int z)
    {
        this(null, x, y, z, 0, 0);
    }

    @Override
    public int compareTo(Node o)
    {
        //  Comparing doubles and returning value as int; can't simply cast the result
        if (score < o.score) return -1;
        if (score > o.score) return 1;
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
        return x ^ (z << 12 | z >> 20) ^ (y << 24);
    }
}
