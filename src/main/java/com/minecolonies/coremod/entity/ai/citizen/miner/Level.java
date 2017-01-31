package com.minecolonies.coremod.entity.ai.citizen.miner;

import com.minecolonies.coremod.colony.buildings.BuildingMiner;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.util.*;

import static com.minecolonies.coremod.entity.ai.citizen.miner.Node.NodeType.*;

/**
 * Miner Level Data Structure.
 * <p>
 * A Level contains all the nodes for one level of the mine.
 */
public class Level
{
    /**
     * Tags used to store and retrieve level data from NBT.
     */
    private static final String TAG_DEPTH      = "Depth";
    private static final String TAG_NODES      = "Nodes";
    private static final String TAG_LADDERX    = "LadderX";
    private static final String TAG_LADDERZ    = "LadderZ";
    private static final String TAG_OPEN_NODES = "OpenNodes";

    /**
     * Possible rotations.
     */
    private static final int ROTATE_ONCE        = 1;
    private static final int ROTATE_TWICE       = 2;
    private static final int ROTATE_THREE_TIMES = 3;
    private static final int MAX_ROTATIONS      = 4;


    /**
     * The depth of the level stored as the y coordinate.
     */
    private int depth;

    /**
     * Random object needed for some tasks.
     */
    private static final Random rand = new Random();

    /**
     * Number to choose random types. It's random.nextInt(RANDOM_TYPES),
     */
    private static final int RANDOM_TYPES = 4;

    /**
     * The hashMap of nodes, check for nodes with the tuple of the parent x and z.
     */
    @NotNull
    private final HashMap<Point2D, Node> nodes = new HashMap<>();

    /**
     * Comparator to compare two nodes, for the priority queue.
     */
    @NotNull
    private static final Comparator<Node> NODE_COMPARATOR = (Node n1, Node n2) -> rand.nextInt(100) > 50 ? 1 : -1;

    /**
     * The queue of open Nodes. Get a new node to work on here.
     */
    @NotNull
    private final Queue<Node> openNodes = new PriorityQueue<>(11, NODE_COMPARATOR);

    /**
     * The node of the ladder.
     */
    @Nullable
    private Node       ladderNode = null;

    /**
     * Private constructor, used to create the level from NBT.
     */
    private Level()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Create a new level model.
     *
     * @param buildingMiner reference to the miner building.
     * @param depth         the depth of this level.
     */
    public Level(@NotNull final BuildingMiner buildingMiner, final int depth)
    {
        this.depth = depth;

        final int cobbleX = buildingMiner.getCobbleLocation().getX();
        final int cobbleZ = buildingMiner.getCobbleLocation().getZ();

        //check for orientation
        @NotNull final Point2D cobbleCenter = new Point2D.Double(cobbleX - (buildingMiner.getVectorX() * 3), cobbleZ - (buildingMiner.getVectorZ() * 3));
        @NotNull final Point2D ladderCenter = new Point2D.Double(cobbleX + (buildingMiner.getVectorX() * 4), cobbleZ + (buildingMiner.getVectorZ() * 4));

        //They are shaft and ladderBack, their parents are the shaft.
        @NotNull final Node cobbleNode = new Node(cobbleCenter.getX(), cobbleCenter.getY(), ladderCenter);
        cobbleNode.setStyle(Node.NodeType.LADDER_BACK);
        cobbleNode.setStatus(Node.NodeStatus.COMPLETED);
        nodes.put(cobbleCenter, cobbleNode);

        ladderNode = new Node(ladderCenter.getX(), ladderCenter.getY(), null);
        ladderNode.setStyle(Node.NodeType.SHAFT);
        ladderNode.setStatus(Node.NodeStatus.COMPLETED);
        nodes.put(ladderCenter, ladderNode);

        final List<Point2D.Double> nodeCenterList = new ArrayList<>();
        //Calculate the center positions of the new nodes.
        nodeCenterList.add(ladderNode.getNorthNodeCenter());
        nodeCenterList.add(ladderNode.getSouthNodeCenter());
        nodeCenterList.add(ladderNode.getEastNodeCenter());
        nodeCenterList.add(ladderNode.getWesthNodeCenter());

        for(final Point2D.Double pos: nodeCenterList)
        {
            if(cobbleCenter.equals(pos) || ladderCenter.equals(pos))
            {
                continue;
            }
            final Node tempNode = new Node(pos.getX(), pos.getY(), ladderCenter);
            tempNode.setStyle(TUNNEL);
            nodes.put(pos, tempNode);
            openNodes.add(tempNode);
        }
    }

    /**
     * Getter for a random Node in the level.
     * @return any random node.
     */
    public Node getRandomNode()
    {
        return openNodes.peek();
    }

    /**
     * Closes the first Node in the list (Has been returned previously probably).
     * Then creates the new nodes connected to it.
     * @param rotation the rotation of the node.
     */
    public void closeNextNode(int rotation)
    {
        final Node tempNode = openNodes.poll();
        final List<Point2D.Double> nodeCenterList = new ArrayList<>();

        switch(tempNode.getStyle())
        {
            case TUNNEL:
                nodeCenterList.add(getNextNodePositionFromNodeWithRotation(tempNode, rotation, 0));
                break;
            case BEND:
                nodeCenterList.add(getNextNodePositionFromNodeWithRotation(tempNode, rotation, ROTATE_THREE_TIMES));
                break;
            case CROSSROAD:
                nodeCenterList.add(getNextNodePositionFromNodeWithRotation(tempNode, rotation, 0));
                nodeCenterList.add(getNextNodePositionFromNodeWithRotation(tempNode, rotation, ROTATE_ONCE));
                nodeCenterList.add(getNextNodePositionFromNodeWithRotation(tempNode, rotation, ROTATE_THREE_TIMES));
                break;
            default:
                return;
        }

        for(final Point2D.Double pos: nodeCenterList)
        {
            if(nodes.containsKey(pos))
            {
                continue;
            }
            final Node tempNodeToAdd = new Node(pos.getX(), pos.getY(), new Point2D.Double(tempNode.getX(), tempNode.getZ()));
            final int randNumber = rand.nextInt(RANDOM_TYPES);
            tempNodeToAdd.setStyle(randNumber <= 1 ? TUNNEL : (randNumber == 2 ? BEND : CROSSROAD));
            nodes.put(pos, tempNodeToAdd);
            openNodes.add(tempNodeToAdd);
        }
        nodes.get(new Point2D.Double(tempNode.getX(), tempNode.getZ())).setStatus(Node.NodeStatus.COMPLETED);
    }

    /**
     * GEts the next node position from the currentNode the rotation of it and the additional rotation.
     * @param node the node.
     * @param rotation the rotation.
     * @param additionalRotation the additional rotation.
     * @return center of the new node.
     */
    private static Point2D.Double getNextNodePositionFromNodeWithRotation(Node node, int rotation, int additionalRotation)
    {
        final int realRotation = Math.floorMod(rotation + additionalRotation, MAX_ROTATIONS);
        switch(realRotation)
        {
            case ROTATE_ONCE:
                return node.getSouthNodeCenter();
            case ROTATE_TWICE:
                return node.getWesthNodeCenter();
            case ROTATE_THREE_TIMES:
                return node.getNorthNodeCenter();
            default:
                return node.getEastNodeCenter();
        }
    }

    /**
     * Create a level from nbt.
     *
     * @param compound compound to use.
     * @return a new level.
     */
    @NotNull
    public static Level createFromNBT(@NotNull final NBTTagCompound compound)
    {
        @NotNull final Level level = new Level();

        level.depth = compound.getInteger(TAG_DEPTH);

        final NBTTagList nodeTagList = compound.getTagList(TAG_NODES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nodeTagList.tagCount(); i++)
        {
            @NotNull final Node node = Node.createFromNBT(nodeTagList.getCompoundTagAt(i));
            level.nodes.put(new Point2D.Double(node.getX(), node.getZ()), node);
        }
        final double ladderX = compound.getDouble(TAG_LADDERX);
        final double ladderZ = compound.getDouble(TAG_LADDERZ);

        level.ladderNode = level.nodes.get(new Point2D.Double(ladderX, ladderZ));


        final NBTTagList openNodeTagList = compound.getTagList(TAG_OPEN_NODES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < openNodeTagList.tagCount(); i++)
        {
            @NotNull final Node node = Node.createFromNBT(openNodeTagList.getCompoundTagAt(i));
            level.openNodes.add(node);
        }

        return level;
    }

    @NotNull
    @Override
    public String toString()
    {
        return "Level{" + "depth=" + depth + ", nodes=" + nodes + ", ladderNode=" + ladderNode + '}';
    }

    /**
     * Store the level to nbt.
     *
     * @param compound compound to use.
     */
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        compound.setInteger(TAG_DEPTH, depth);

        @NotNull final NBTTagList nodeTagList = new NBTTagList();
        for (@NotNull final Node node : nodes.values())
        {
            @NotNull final NBTTagCompound nodeCompound = new NBTTagCompound();
            node.writeToNBT(nodeCompound);
            nodeTagList.appendTag(nodeCompound);
        }
        compound.setTag(TAG_NODES, nodeTagList);

        compound.setDouble(TAG_LADDERX, ladderNode.getX());
        compound.setDouble(TAG_LADDERZ, ladderNode.getZ());

        @NotNull final NBTTagList openNodeTagList = new NBTTagList();
        for (@NotNull final Node node : openNodes)
        {
            @NotNull final NBTTagCompound nodeCompound = new NBTTagCompound();
            node.writeToNBT(nodeCompound);
            openNodeTagList.appendTag(nodeCompound);
        }
        compound.setTag(TAG_OPEN_NODES, openNodeTagList);

    }

    @NotNull
    public Map<Point2D, Node> getNodes()
    {
        return Collections.unmodifiableMap(nodes);
    }

    public int getNumberOfNodes()
    {
        return nodes.size();
    }

    public int getDepth()
    {
        return depth;
    }

    @Nullable
    public Node getLadderNode()
    {
        return ladderNode;
    }

    /**
     * Add a new node to the level.
     *
     * @param newNode the node to add.
     */
    public void addNode(final Node newNode)
    {
        nodes.put(new Point2D.Double(newNode.getX(), newNode.getZ()), newNode);
    }

    /**
     * Returns a node by its key from the map.
     * @param key the Point2D key.
     * @return the Node.
     */
    public Node getNode(final Point2D key)
    {
        return nodes.get(key);
    }
}
