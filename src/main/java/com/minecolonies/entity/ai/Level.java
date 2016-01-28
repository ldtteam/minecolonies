package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingMiner;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Miner Level Data Structure
 *
 * A Level contains all the nodes for one level of the mine
 *
 * @author Colton, Kostronor
 */
public class Level
{
    /**
     * The depth of the level stored as the y coordinate
     */
    private int depth;

    private List<Node> nodes = new ArrayList<>();

    private static final String TAG_DEPTH = "Depth";
    private static final String TAG_NODES = "Nodes";

    private Level(){}

   /* public Level(int x, int depth, int z, BuildingMiner b)
    {
        this.depth = depth;
        nodes = new ArrayList<>();

        int cobbleX = b.cobbleLocation.posX;
        int cobbleZ = b.cobbleLocation.posZ;

        if(cobbleX != x-4 || cobbleZ != z)
        {
            nodes.add(new Node(x-4,z,-1,0));
        }

        if(cobbleX != x || cobbleZ != z+4)
        {
            nodes.add(new Node(x,z+4,0,+1));
        }

        if(cobbleX != x+4 || cobbleZ != z)
        {
            nodes.add(new Node(x+4,z,+1,0));
        }

        if(cobbleX != x || cobbleZ != z-4)
        {
            nodes.add(new Node(x,z-4,0,-1));
        }
    }*/

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Level{");
        sb.append("depth=").append(depth);
        sb.append(", nodes=").append(nodes);
        sb.append('}');
        return sb.toString();
    }

    public Level(BuildingMiner buildingMiner, int depth){
        this.depth = depth;
        //TODO: Store in HashMap for faster access
        nodes = new ArrayList<>();

        int cobbleX = buildingMiner.cobbleLocation.posX;
        int cobbleZ = buildingMiner.cobbleLocation.posZ;

        //check for orientation
        ChunkCoordinates cobbleCenter = new ChunkCoordinates(
                cobbleX - (buildingMiner.vectorX*3),
                depth,
                cobbleZ - (buildingMiner.vectorZ*3)
        );
        ChunkCoordinates ladderCenter = new ChunkCoordinates(
                cobbleX + (buildingMiner.vectorX*4),
                depth,
                cobbleZ + (buildingMiner.vectorZ*4)
        );
        //TODO: let them know they are ladder and cobble (they are handled different)
        Node cobbleNode = new Node(cobbleCenter.posX,cobbleCenter.posZ);
        Node ladderNode = new Node(ladderCenter.posX,ladderCenter.posZ);
        ladderNode.setStatus(Node.Status.COMPLETED);
        if(buildingMiner.vectorX > 0){
            ladderNode.setDirectionNegX(Node.Status.LADDER);
            cobbleNode.setDirectionPosX(Node.Status.LADDER);
        }else if (buildingMiner.vectorX < 0){
            ladderNode.setDirectionPosX(Node.Status.LADDER);
            cobbleNode.setDirectionNegX(Node.Status.LADDER);
        }else if(buildingMiner.vectorZ > 0){
            ladderNode.setDirectionNegZ(Node.Status.LADDER);
            cobbleNode.setDirectionPosZ(Node.Status.LADDER);
        }else if (buildingMiner.vectorZ < 0){
            ladderNode.setDirectionPosZ(Node.Status.LADDER);
            cobbleNode.setDirectionNegZ(Node.Status.LADDER);
        }
        nodes.add(cobbleNode);
        nodes.add(ladderNode);
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setInteger(TAG_DEPTH, depth);

        NBTTagList nodeTagList = new NBTTagList();
        for (Node node : nodes) {
            NBTTagCompound nodeCompound = new NBTTagCompound();
            node.writeToNBT(nodeCompound);
            nodeTagList.appendTag(nodeCompound);
        }
        compound.setTag(TAG_NODES, nodeTagList);
    }

    public static Level createFromNBT(NBTTagCompound compound)
    {
        Level level = new Level();

        level.depth = compound.getInteger(TAG_DEPTH);

        NBTTagList nodeTagList = compound.getTagList(TAG_NODES, Constants.NBT.TAG_COMPOUND);
        for(int i = 0; i < nodeTagList.tagCount(); i++)
        {
            Node node = Node.createFromNBT(nodeTagList.getCompoundTagAt(i));
            level.nodes.add(node);
        }

        return level;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public int getDepth()
    {
        return depth;
    }

    public void addNewNode(int x, int z)
    {
        nodes.add(new Node(x,z));
    }
}
