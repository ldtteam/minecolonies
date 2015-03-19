package com.minecolonies.entity.ai;

import org.lwjgl.util.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Miner Level Data Structure
 *
 * A startinglevel contains all the nodes for one startinglevel of the mine
 *
 * @author Colton
 */
public class Level
{
    /**
     * The depth of the startinglevel stored either as an incremental integer or the y startinglevel, not sure yet
     */
    private int depth;
    private List<Node> nodes;

    public Level(int depth)
    {
        this.depth = depth;
        nodes = new ArrayList<Node>();
        nodes.add(new Node(-4,0));
        nodes.add(new Node(0,4));
        nodes.add(new Node(4,0));

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
