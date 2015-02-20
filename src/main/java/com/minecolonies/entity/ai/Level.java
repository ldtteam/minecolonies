package com.minecolonies.entity.ai;

import org.lwjgl.util.Point;

import java.util.List;

/**
 * Miner Level Data Structure
 *
 * A level contains all the nodes for one level of the mine
 *
 * @author Colton
 */
public class Level
{
    /**
     * The depth of the level stored either as an incremental integer or the y level, not sure yet
     */
    private int depth;
    private List<Node> nodes;

    public Level(int depth)
    {
        this.depth = depth;
    }

    public int getDepth()
    {
        return depth;
    }

    public void addNewNode(Point id)
    {
        nodes.add(new Node(id));
    }
}
