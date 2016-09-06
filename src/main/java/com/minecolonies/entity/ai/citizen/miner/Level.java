package com.minecolonies.entity.ai.citizen.miner;

import com.minecolonies.colony.buildings.BuildingMiner;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Miner Level Data Structure
 * <p>
 * A Level contains all the nodes for one level of the mine
 *
 * @author Colton, Kostronor
 */
public class Level
{
    private static final String TAG_DEPTH   = "Depth";
    private static final String TAG_NODES   = "Nodes";
    private static final String TAG_LADDERX = "LadderX";
    private static final String TAG_LADDERZ = "LadderZ";
    /**
     * The depth of the level stored as the y coordinate
     */
    private int depth;
    private List<Node> nodes      = new ArrayList<>();
    private Node       ladderNode = null;

    //TODO document class
    private Level()
    {
    }

    /**
     * Create a new level model.
     *
     * @param buildingMiner reference to the miner building.
     * @param depth         the depth of this level.
     */
    public Level(BuildingMiner buildingMiner, int depth)
    {
        this.depth = depth;
        //TODO: Store in HashMap for faster access
        nodes = new ArrayList<>();

        int cobbleX = buildingMiner.getCobbleLocation().getX();
        int cobbleZ = buildingMiner.getCobbleLocation().getZ();

        //check for orientation
        BlockPos cobbleCenter = new BlockPos(cobbleX - (buildingMiner.getVectorX() * 3), depth, cobbleZ - (buildingMiner.getVectorZ() * 3));
        BlockPos ladderCenter = new BlockPos(cobbleX + (buildingMiner.getVectorX() * 4), depth, cobbleZ + (buildingMiner.getVectorZ() * 4));
        //TODO: let them know they are ladder and cobble (they are handled different)
        Node cobbleNode = new Node(cobbleCenter.getX(), cobbleCenter.getZ());
        cobbleNode.setStyle(Node.NodeType.LADDER_BACK);
        ladderNode = new Node(ladderCenter.getX(), ladderCenter.getZ());
        ladderNode.setStyle(Node.NodeType.SHAFT);
        ladderNode.setStatus(Node.NodeStatus.COMPLETED);
        ladderNode.setDirectionNegX(Node.NodeStatus.COMPLETED);
        ladderNode.setDirectionPosX(Node.NodeStatus.COMPLETED);
        ladderNode.setDirectionNegZ(Node.NodeStatus.COMPLETED);
        ladderNode.setDirectionPosZ(Node.NodeStatus.COMPLETED);
        if (buildingMiner.getVectorX() > 0)
        {
            ladderNode.setDirectionNegX(Node.NodeStatus.LADDER);
            cobbleNode.setDirectionPosX(Node.NodeStatus.LADDER);
        }
        else if (buildingMiner.getVectorX() < 0)
        {
            ladderNode.setDirectionPosX(Node.NodeStatus.LADDER);
            cobbleNode.setDirectionNegX(Node.NodeStatus.LADDER);
        }
        else if (buildingMiner.getVectorZ() > 0)
        {
            ladderNode.setDirectionNegZ(Node.NodeStatus.LADDER);
            cobbleNode.setDirectionPosZ(Node.NodeStatus.LADDER);
        }
        else if (buildingMiner.getVectorZ() < 0)
        {
            ladderNode.setDirectionPosZ(Node.NodeStatus.LADDER);
            cobbleNode.setDirectionNegZ(Node.NodeStatus.LADDER);
        }
        nodes.add(cobbleNode);
        nodes.add(ladderNode);
    }

    public static Level createFromNBT(NBTTagCompound compound)
    {
        Level level = new Level();

        level.depth = compound.getInteger(TAG_DEPTH);

        NBTTagList nodeTagList = compound.getTagList(TAG_NODES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nodeTagList.tagCount(); i++)
        {
            Node node = Node.createFromNBT(nodeTagList.getCompoundTagAt(i));
            level.nodes.add(node);
        }
        int ladderx = compound.getInteger(TAG_LADDERX);
        int ladderz = compound.getInteger(TAG_LADDERZ);

        level.ladderNode = level.nodes
                             .stream()
                             .filter(node -> node.getX() == ladderx && node.getZ() == ladderz)
                             .findFirst()
                             .orElseThrow(() -> new IllegalStateException("No ladder node found."));

        return level;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Level{");
        sb.append("depth=").append(depth);
        sb.append(", nodes=").append(nodes);
        sb.append(", ladderNode=").append(ladderNode);
        sb.append('}');
        return sb.toString();
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setInteger(TAG_DEPTH, depth);

        NBTTagList nodeTagList = new NBTTagList();
        for (Node node : nodes)
        {
            NBTTagCompound nodeCompound = new NBTTagCompound();
            node.writeToNBT(nodeCompound);
            nodeTagList.appendTag(nodeCompound);
        }
        compound.setTag(TAG_NODES, nodeTagList);

        compound.setInteger(TAG_LADDERX, ladderNode.getX());
        compound.setInteger(TAG_LADDERZ, ladderNode.getZ());
    }

    public List<Node> getNodes()
    {
        return Collections.unmodifiableList(nodes);
    }

    public int getNumberOfNodes()
    {
        return nodes.size();
    }

    public int getDepth()
    {
        return depth;
    }

    public Node getLadderNode()
    {
        return ladderNode;
    }

    public void addNode(Node newnode)
    {
        nodes.add(newnode);
    }
}
