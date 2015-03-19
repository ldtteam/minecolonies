package com.minecolonies.entity.ai;

import org.lwjgl.util.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Miner Node Data Structure
 *
 *  When a node is completed we should add the surrounding nodes to the startinglevel as AVAILABLE
 *      also note that we don't want node (0, -1) because there will be a ladder on the back
 *      wall of the initial node, and we cant put the connection through the ladder
 *
 * @author Colton
 */
public class Node
{
    /**
     * Each node has a unique id stored as a point.
     * The point also doubles as the location of the node, ex: first point id = (0,0)
     */
    private Point id;
    /**
     * This is a list containing the id's of nodes that are adjacent to the current node
     * and have a completed/built connection between them
     */
    private List<Point> connections;
    private Status status;

    /**
     * Sets the status of the node
     *  AVAILABLE means it can be mined
     *  IN_PROGRESS means it is currently being mined
     *  COMPLETED means it has been mined and all torches/wood structure has been placed
     *
     *  this doesn't have to be final, more stages can be added or this doesn't have to be used
     */
    enum Status
    {
        AVAILABLE,
        IN_PROGRESS,
        COMPLETED
    }

    public Node(int x, int y)
    {
        this(new Point(x, y));
    }

    public Node(Point id)
    {
        this.id = id;
        connections = new ArrayList<Point>();
        status = Status.AVAILABLE;
    }

    public Point getID()
    {
        return id;
    }

    public List<Point> getConnections()
    {
        return connections;
    }

    public void addConnection(Point node)
    {
        connections.add(node);
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }
}