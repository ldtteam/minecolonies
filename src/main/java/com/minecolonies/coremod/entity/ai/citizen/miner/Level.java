package com.minecolonies.coremod.entity.ai.citizen.miner;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Vec2i;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingMiner;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.coremod.entity.ai.citizen.miner.Node.NodeType.*;

/**
 * Miner Level Data StructureIterator.
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
    private static final String TAG_LEVEL_SIGN = "LevelSign";

    /**
     * Possible rotations.
     */
    private static final int              ROTATE_ONCE        = 1;
    private static final int              ROTATE_TWICE       = 2;
    private static final int              ROTATE_THREE_TIMES = 3;
    private static final int              MAX_ROTATIONS      = 4;
    /**
     * Random object needed for some tasks.
     */
    private static final Random           rand               = new Random();
    /**
     * Number to choose random types. It's random.nextInt(RANDOM_TYPES),
     */
    private static final int              RANDOM_TYPES       = 4;
    /**
     * Comparator to compare two nodes, for the priority queue.
     */
    @NotNull
    private static final Comparator<Node> NODE_COMPARATOR    = (Node n1, Node n2) -> rand.nextInt(100) > 50 ? 1 : -1;
    /**
     * The hashMap of nodes, check for nodes with the tuple of the parent x and z.
     */
    @NotNull
    private final        Map<Vec2i, Node> nodes              = new HashMap<>();
    /**
     * The queue of open Nodes. Get a new node to work on here.
     */
    @NotNull
    private final        Queue<Node>      openNodes          = new PriorityQueue<>(11, NODE_COMPARATOR);

    /**
     * The depth of the level stored as the y coordinate.
     */
    private final int depth;

    /**
     * The node of the ladder.
     */
    private final Node ladderNode;

    /**
     * The node of the ladder.
     */
    @Nullable
    private BlockPos levelSign;

    /**
     * Offset number to make build nodes count proper
     */
    private static final int BUILT_NODES_OFFSET = -2;

    /**
     * Create a new level model.
     *
     * @param buildingMiner reference to the miner building.
     * @param depth         the depth of this level.
     */
    public Level(@NotNull final BuildingMiner buildingMiner, final int depth, final BlockPos levelSign)
    {
        this.depth = depth;
        this.levelSign = levelSign;

        final int cobbleX = buildingMiner.getCobbleLocation().getX();
        final int cobbleZ = buildingMiner.getCobbleLocation().getZ();

        //check for orientation
        @NotNull final Vec2i cobbleCenter = new Vec2i(cobbleX - (buildingMiner.getVectorX() * 3), cobbleZ - (buildingMiner.getVectorZ() * 3));
        @NotNull final Vec2i ladderCenter = new Vec2i(cobbleX + (buildingMiner.getVectorX() * 4), cobbleZ + (buildingMiner.getVectorZ() * 4));

        //They are shaft and ladderBack, their parents are the shaft.
        @NotNull final Node cobbleNode = new Node(cobbleCenter.getX(), cobbleCenter.getZ(), ladderCenter);
        cobbleNode.setStyle(LADDER_BACK);
        cobbleNode.setStatus(Node.NodeStatus.COMPLETED);
        nodes.put(cobbleCenter, cobbleNode);

        ladderNode = new Node(ladderCenter.getX(), ladderCenter.getZ(), null);
        ladderNode.setStyle(SHAFT);
        ladderNode.setStatus(Node.NodeStatus.COMPLETED);
        nodes.put(ladderCenter, ladderNode);

        final List<Vec2i> nodeCenterList = new ArrayList<>(4);
        //Calculate the center positions of the new nodes.
        nodeCenterList.add(ladderNode.getNorthNodeCenter());
        nodeCenterList.add(ladderNode.getSouthNodeCenter());
        nodeCenterList.add(ladderNode.getEastNodeCenter());
        nodeCenterList.add(ladderNode.getWestNodeCenter());

        for (final Vec2i pos : nodeCenterList)
        {
            if (cobbleCenter.equals(pos) || ladderCenter.equals(pos))
            {
                continue;
            }
            final Node tempNode = new Node(pos.getX(), pos.getZ(), ladderCenter);
            tempNode.setStyle(TUNNEL);
            nodes.put(pos, tempNode);
            openNodes.add(tempNode);
        }
    }

    /**
     * Create a level from nbt.
     *
     * @param compound compound to use.
     */
    public Level(@NotNull final CompoundNBT compound)
    {

        this.depth = compound.getInt(TAG_DEPTH);
        if (compound.keySet().contains(TAG_LEVEL_SIGN))
        {
            this.levelSign = BlockPosUtil.read(compound, TAG_LEVEL_SIGN);
        }
        else
        {
            this.levelSign = null;
        }

        final ListNBT nodeTagList = compound.getList(TAG_NODES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nodeTagList.size(); i++)
        {
            @NotNull final Node node = Node.createFromNBT(nodeTagList.getCompound(i));
            this.nodes.put(new Vec2i(node.getX(), node.getZ()), node);
        }

        final boolean hasDoubles = compound.keySet().contains(TAG_LADDERX, Constants.NBT.TAG_DOUBLE);

        final int ladderX;
        final int ladderZ;
        if (hasDoubles)
        {
            ladderX = MathHelper.floor(compound.getDouble(TAG_LADDERX));
            ladderZ = MathHelper.floor(compound.getDouble(TAG_LADDERZ));
        }
        else
        {
            ladderX = compound.getInt(TAG_LADDERX);
            ladderZ = compound.getInt(TAG_LADDERZ);
        }

        this.ladderNode = this.nodes.get(new Vec2i(ladderX, ladderZ));


        final ListNBT openNodeTagList = compound.getList(TAG_OPEN_NODES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < openNodeTagList.size(); i++)
        {
            @NotNull final Node node = Node.createFromNBT(openNodeTagList.getCompound(i));
            this.openNodes.add(node);
        }
    }

    /**
     * Getter for a random Node in the level.
     *
     * @param node the last node.
     * @return any random node.
     */
    public Node getRandomNode(@Nullable final Node node)
    {
        Node nextNode = null;
        if (node != null && rand.nextInt(RANDOM_TYPES) > 0)
        {
            nextNode = node.getRandomNextNode(this, 0);
        }
        return nextNode == null ? openNodes.peek() : nextNode;
    }

    /**
     * Closes a given node. Or close the first in the list if null.
     * Then creates the new nodes connected to it.
     *
     * @param rotation the rotation of the node.
     * @param node the node to close.
     */
    public void closeNextNode(final int rotation, final Node node)
    {
        final Node tempNode = node == null ? openNodes.peek() : node;
        final List<Vec2i> nodeCenterList = new ArrayList<>(3);

        if (tempNode == null)
        {
            return;
        }

        switch (tempNode.getStyle())
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

        for (final Vec2i pos : nodeCenterList)
        {
            if (nodes.containsKey(pos))
            {
                continue;
            }
            final Node tempNodeToAdd = new Node(pos.getX(), pos.getZ(), new Vec2i(tempNode.getX(), tempNode.getZ()));
            tempNodeToAdd.setStyle(getRandomNodeType());
            nodes.put(pos, tempNodeToAdd);
            openNodes.add(tempNodeToAdd);
        }
        nodes.get(new Vec2i(tempNode.getX(), tempNode.getZ())).setStatus(Node.NodeStatus.COMPLETED);
        openNodes.removeIf(tempNode::equals);
    }

    /**
     * GEts the next node position from the currentNode the rotation of it and the additional rotation.
     *
     * @param node               the node.
     * @param rotation           the rotation.
     * @param additionalRotation the additional rotation.
     * @return center of the new node.
     */
    private static Vec2i getNextNodePositionFromNodeWithRotation(final Node node, final int rotation, final int additionalRotation)
    {
        final int realRotation = Math.floorMod(rotation + additionalRotation, MAX_ROTATIONS);
        switch (realRotation)
        {
            case ROTATE_ONCE:
                return node.getSouthNodeCenter();
            case ROTATE_TWICE:
                return node.getWestNodeCenter();
            case ROTATE_THREE_TIMES:
                return node.getNorthNodeCenter();
            default:
                return node.getEastNodeCenter();
        }
    }

    private static Node.NodeType getRandomNodeType()
    {
        final int randNumber = rand.nextInt(RANDOM_TYPES);
        if (randNumber <= 1)
        {
            return TUNNEL;
        }
        else if (randNumber == 2)
        {
            return BEND;
        }

        return CROSSROAD;
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
    public void write(@NotNull final CompoundNBT compound)
    {
        compound.putInt(TAG_DEPTH, depth);
        if (levelSign != null)
        {
            BlockPosUtil.write(compound, TAG_LEVEL_SIGN, levelSign);
        }

        @NotNull final ListNBT nodeTagList = new ListNBT();
        for (@NotNull final Node node : nodes.values())
        {
            @NotNull final CompoundNBT nodeCompound = new CompoundNBT();
            node.write(nodeCompound);
            nodeTagList.add(nodeCompound);
        }
        compound.put(TAG_NODES, nodeTagList);

        compound.putInt(TAG_LADDERX, ladderNode.getX());
        compound.putInt(TAG_LADDERZ, ladderNode.getZ());

        @NotNull final ListNBT openNodeTagList = new ListNBT();
        for (@NotNull final Node node : openNodes)
        {
            @NotNull final CompoundNBT nodeCompound = new CompoundNBT();
            node.write(nodeCompound);
            openNodeTagList.add(nodeCompound);
        }
        compound.put(TAG_OPEN_NODES, openNodeTagList);
    }

    @NotNull
    public Map<Vec2i, Node> getNodes()
    {
        return Collections.unmodifiableMap(nodes);
    }

    public int getNumberOfNodes()
    {
        return nodes.size();
    }

    public int getNumberOfBuiltNodes()
    {
        return nodes.size() - openNodes.size() + BUILT_NODES_OFFSET;
    }

    public int getDepth()
    {
        return depth;
    }

    @NotNull
    public Node getLadderNode()
    {
        return ladderNode;
    }

    /**
     * Add a new node to the level.
     *
     * @param newNode the node to add.
     */
    /* not in use
    public void addNode(final Node newNode)
    {
        nodes.put(new Vec2i(newNode.getX(), newNode.getZ()), newNode);
    }
    */

    /**
     * Returns a node by its key from the map.
     *
     * @param key the Point2D key.
     * @return the Node.
     */
    public Node getNode(final Vec2i key)
    {
        return nodes.get(key);
    }

    /**
     * Returns a node by its key from the map.
     *
     * @param key the Point2D key.
     * @return the Node.
     */
    public Node getOpenNode(final Vec2i key)
    {
        return nodes.get(key);
    }

    /**
     * Returns position of level's levelSign
     * 
     * @return levelSign
     */
    public BlockPos getLevelSign()
    {
        return levelSign;
    }
}
