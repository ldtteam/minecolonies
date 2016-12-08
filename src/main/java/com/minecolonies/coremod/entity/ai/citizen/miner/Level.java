package com.minecolonies.coremod.entity.ai.citizen.miner;

import com.minecolonies.coremod.colony.buildings.BuildingMiner;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.coremod.entity.ai.citizen.miner.Node.NodeType.TUNNEL;

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
    private static final String TAG_DEPTH   = "Depth";
    private static final String TAG_NODES   = "Nodes";
    private static final String TAG_LADDERX = "LadderX";
    private static final String TAG_LADDERZ = "LadderZ";

    /**
     * The depth of the level stored as the y coordinate.
     */
    private int depth;

    /**
     * The hashMap of nodes, check for nodes with the tuple of the parent x and z.
     */
    @NotNull
    private final HashMap<Tuple<Integer, Integer>,Node> nodes = new HashMap<>();

    /**
     * Comparator to compare two nodes, for the priority queue.
     */
    @NotNull
    private static final Comparator<Node> NODE_COMPARATOR = (Node n1, Node n2) -> new Random().nextInt(100) > 50 ? 1 : -1;

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
        @NotNull final BlockPos cobbleCenter = new BlockPos(cobbleX - (buildingMiner.getVectorX() * 3), depth, cobbleZ - (buildingMiner.getVectorZ() * 3));
        @NotNull final BlockPos ladderCenter = new BlockPos(cobbleX + (buildingMiner.getVectorX() * 4), depth, cobbleZ + (buildingMiner.getVectorZ() * 4));
        Tuple<Integer, Integer> ladderKey = new Tuple<>(ladderCenter.getX(), ladderCenter.getZ());

        //They are shaft and ladderBack, their parents are the shaft.
        @NotNull final Node cobbleNode = new Node(cobbleCenter.getX(), cobbleCenter.getZ(), ladderKey);
        cobbleNode.setStyle(Node.NodeType.LADDER_BACK);
        cobbleNode.setStatus(Node.NodeStatus.COMPLETED);
        nodes.put(new Tuple<>(cobbleCenter.getX(), cobbleCenter.getZ()), cobbleNode);

        ladderNode = new Node(ladderCenter.getX(), ladderCenter.getZ(), null);
        ladderNode.setStyle(Node.NodeType.SHAFT);
        ladderNode.setStatus(Node.NodeStatus.COMPLETED);
        nodes.put(ladderKey, ladderNode);

        List<BlockPos> nodeCenterList = new ArrayList<>();
        //Calculate the center positions of the new nodes.
        nodeCenterList.add(ladderNode.getNorthNodeCenter());
        nodeCenterList.add(ladderNode.getSouthNodeCenter());
        nodeCenterList.add(ladderNode.getEastNodeCenter());
        nodeCenterList.add(ladderNode.getWesthNodeCenter());

        for(final BlockPos pos: nodeCenterList)
        {
            if(cobbleCenter.equals(pos))
            {
                continue;
            }
            final Node tempNode = new Node(pos.getX(), pos.getZ(), ladderKey);
            tempNode.setStyle(TUNNEL);
            nodes.put(new Tuple<>(pos.getX(), pos.getZ()), tempNode);
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
     * @param rotation
     */
    public void closeNextNode(int rotation)
    {
        final Node tempNode = openNodes.poll();
        final List<BlockPos> nodeCenterList = new ArrayList<>();

        //todo let them face the correct direction!
        switch(tempNode.getStyle())
        {
            case TUNNEL:
                nodeCenterList.add(getNextNodePositionFromNodeWithRotation(tempNode, rotation));
                break;
            case BEND:
                nodeCenterList.add(getNextNodePositionFromNodeWithRotation(tempNode, rotation+3));
                break;
            case CROSSROAD:
                nodeCenterList.add(getNextNodePositionFromNodeWithRotation(tempNode, rotation));
                nodeCenterList.add(getNextNodePositionFromNodeWithRotation(tempNode, rotation + 1));
                nodeCenterList.add(getNextNodePositionFromNodeWithRotation(tempNode, rotation + 3));
                break;
        }

        //todo random style!
        for(final BlockPos pos: nodeCenterList)
        {
            Tuple<Integer, Integer> tuple = new Tuple<>(pos.getX(), pos.getZ());
            if(nodes.containsKey(tuple))
            {
                continue;
            }
            final Node tempNodeToAdd = new Node(pos.getX(), pos.getZ(), new Tuple<>(tempNode.getX(), tempNode.getZ()));
            tempNodeToAdd.setStyle(TUNNEL);
            nodes.put(tuple, tempNodeToAdd);
            openNodes.add(tempNodeToAdd);
        }
        nodes.get(new Tuple<>(tempNode.getX(), tempNode.getZ())).setStatus(Node.NodeStatus.COMPLETED);
    }

    private static BlockPos getNextNodePositionFromNodeWithRotation(Node node, int rotation)
    {
        switch(rotation)
        {
            case 1:
                return node.getSouthNodeCenter();
            case 2:
                return node.getWesthNodeCenter();
            case 3:
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
            level.nodes.put(new Tuple<>(node.getX(), node.getZ()), node);
        }
        final int ladderX = compound.getInteger(TAG_LADDERX);
        final int ladderZ = compound.getInteger(TAG_LADDERZ);

        level.ladderNode = level.nodes.get(new Tuple<>(ladderX, ladderZ));

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

        compound.setInteger(TAG_LADDERX, ladderNode.getX());
        compound.setInteger(TAG_LADDERZ, ladderNode.getZ());
    }

    @NotNull
    public Map<Tuple<Integer, Integer>, Node> getNodes()
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
        nodes.put(new Tuple<>(newNode.getX(), newNode.getZ()), newNode);
    }
}
