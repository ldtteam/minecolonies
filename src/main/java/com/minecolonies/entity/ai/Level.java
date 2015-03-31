package com.minecolonies.entity.ai;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChunkCoordinates;
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

    public List<Node> getNodes() {
        return nodes;
    }

    private List<Node> nodes;

    public Level()
    {}

    public Level(int x, int depth, int z)
    {
        this.depth = depth;
        nodes = new ArrayList<Node>();
        nodes.add(new Node(x-4,z,-1,0));
        nodes.add(new Node(x,z+4,0,+1));
        nodes.add(new Node(x+4,z+0,+1,0));

    }

    public static void writeToNBT(NBTTagCompound compound,Level level)
    {

        int i = 0;
        int[] iX = new int[level.nodes.size()];
        int[] iY = new int[level.nodes.size()];
        int[] iVX = new int[level.nodes.size()];
        int[] iVY = new int[level.nodes.size()];

        for(Node n: level.nodes)
        {
            iX[i] = n.getID().getX();
            iY[i] = n.getID().getY();
            iVX[i] = n.getVectorX();
            iVY[i++] = n.getVectorZ();
        }

        compound.setIntArray("x",iX);
        compound.setIntArray("y",iY);
        compound.setIntArray("vX",iVX);
        compound.setIntArray("vY",iVY);
        compound.setInteger("depth", level.depth);



    }

    public static Level readFromNBT(NBTTagCompound compound)
    {

        Level level = new Level();


        int i = 0;
        int[] iX = compound.getIntArray("x");
        int[] iY = compound.getIntArray("y");
        int[] iVX = compound.getIntArray("vX");
        int[] iVY = compound.getIntArray("vY");
        level.depth = compound.getInteger("depth");

        for(int node: iX)
        {
            level.nodes.add(new Node(iX[i],iY[i],iVX[i],iVY[i]));
        }

        return level;
    }

    public static void writeToNBTTagList(NBTTagList tagList, Level level)
    {
        NBTTagCompound compound = new NBTTagCompound();


        int i = 0;
        int[] iX = new int[level.nodes.size()];
        int[] iY = new int[level.nodes.size()];
        int[] iVX = new int[level.nodes.size()];
        int[] iVY = new int[level.nodes.size()];

        for(Node n: level.nodes)
        {
            iX[i] = n.getID().getX();
            iY[i] = n.getID().getY();
            iVX[i] = n.getVectorX();
            iVY[i++] = n.getVectorZ();
        }

        compound.setIntArray("x",iX);
        compound.setIntArray("y",iY);
        compound.setIntArray("vX", iVX);
        compound.setIntArray("vY", iVY);
        compound.setInteger("depth", level.depth);



        tagList.appendTag(compound);
    }

    public static Level readFromNBTTagList(NBTTagList tagList, int index)
    {
        NBTTagCompound compound = tagList.getCompoundTagAt(index);


        Level level = new Level();


        int i = 0;
        int[] iX = compound.getIntArray("x");
        int[] iY = compound.getIntArray("y");
        int[] iVX = compound.getIntArray("vX");
        int[] iVY = compound.getIntArray("vY");
        level.depth = compound.getInteger("depth");

        for(int node: iX)
        {
            level.nodes.add(new Node(iX[i],iY[i],iVX[i],iVY[i]));
        }

        return level;
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
