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
    public int depth;

    private List<Node> nodes = new ArrayList<>();

    private static final String TAG_DEPTH = "Depth";
    private static final String TAG_NODES = "Nodes";

    private Level(){}

    public Level(int x, int depth, int z, BuildingMiner b)
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
        addNewNode(cobbleCenter.posX,cobbleCenter.posZ,buildingMiner.vectorX,buildingMiner.vectorZ);
        addNewNode(ladderCenter.posX,ladderCenter.posZ,buildingMiner.vectorX,buildingMiner.vectorZ);
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

    public void addNewNode(int x, int z,int vektorX, int vektorY)
    {
        nodes.add(new Node(x,z,vektorX,vektorY));
    }
}
