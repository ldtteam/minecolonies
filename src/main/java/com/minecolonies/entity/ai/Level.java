package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingMiner;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.Constants;
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
    public int depth;

    private List<Node> nodes = new ArrayList<Node>();

    private static final String TAG_DEPTH = "Depth";
    private static final String TAG_NODES = "Nodes";

    private Level(){}

    public Level(int x, int depth, int z,BuildingMiner b)
    {
        this.depth = depth;
        nodes = new ArrayList<Node>();

        int ladderX = b.cobbleLocation.posX;
        int ladderZ = b.cobbleLocation.posZ;

        if(ladderX != x-4 || ladderZ != z)
        {
            nodes.add(new Node(x-4,z,-1,0));
        }

        if(ladderX != x || ladderZ != z+4)
        {
            nodes.add(new Node(x,z+4,0,+1));

        }

        if(ladderX != x+4 || ladderZ != z)
        {
            nodes.add(new Node(x+4,z,+1,0));

        }

        if(ladderX != x || ladderZ != z-4)
        {
            nodes.add(new Node(x,z-4,0,-1));

        }



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
