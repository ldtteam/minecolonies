package com.minecolonies.lib;

import com.github.lunatrius.schematica.world.SchematicWorld;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import com.minecolonies.util.IColony;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Colton on 5/4/2014.
 */
public class Schematic
{
    private World          world;
    private SchematicWorld schematic;

    /**
     * North-West corner
     */
    private int x = -1, y = -1, z = -1;

    private Schematic(World worldObj, SchematicWorld schematicWorld)
    {
        world = worldObj;
        schematic = schematicWorld;
    }

    public static Schematic loadSchematic(World worldObj, IColony hut, int level)
    {
        return loadSchematic(worldObj, getResourceLocation(getName(hut, level)));
    }

    public static Schematic loadSchematic(World worldObj, String name)
    {
        return loadSchematic(worldObj, getResourceLocation(name));
    }

    private static Schematic loadSchematic(World worldObj, ResourceLocation res)
    {
        return new Schematic(worldObj, SchematicFormat.readFromStream(getStream(res)));
    }

    public void placeSchematic(int x, int y, int z)
    {
        for(int j = 0; j < schematic.getHeight(); j++)
        {
            for(int k = 0; k < schematic.getLength(); k++)
            {
                for(int i = 0; i < schematic.getWidth(); i++)
                {

                    Block block = this.schematic.getBlock(i, j, k);
                    int metadata = this.schematic.getBlockMetadata(i, j, k);

                    if(block == Blocks.air)
                    {
                        continue;
                    }

                    world.setBlock(x + i, y + j, z + k, block, metadata, 0x02);
                }
            }
        }
    }

    public static void loadAndPlaceSchematic(World worldObj, String name, int x, int y, int z)
    {
        Schematic schematic = loadSchematic(worldObj, name);
        if(schematic.hasSchematic())
        {
            schematic.placeSchematic(x, y, z);
        }
    }

    public void findNextBlock()
    {
        if(!this.hasSchematic()) return;

        if(x == -1)
        {
            y = z = 0;
        }
        do
        {
            x++;
            if(x == schematic.getWidth())
            {
                x = 0;
                z++;
                if(z == schematic.getLength())
                {
                    z = 0;
                    y++;
                    if(y == schematic.getHeight())
                    {
                        x = y = z = -1;
                    }
                }
            }
        }
        while(schematic.isAirBlock(x, y, z));

    }

    public static void saveSchematic(World world, Vector3f from, Vector3f to, String file, String icon)
    {
        if(world == null || from == null || to == null || file == null) return;
        SchematicFormat.writeToFile(MinecraftServer.getServer().getFile("/schematics"), file, scanSchematic(world, from, to, icon));
    }

    public static SchematicWorld scanSchematic(World world, Vector3f from, Vector3f to, String icon)
    {
        int minX = (int) Math.min(from.x, to.x);
        int maxX = (int) Math.max(from.x, to.x);
        int minY = (int) Math.min(from.y, to.y);
        int maxY = (int) Math.max(from.y, to.y);
        int minZ = (int) Math.min(from.z, to.z);
        int maxZ = (int) Math.max(from.z, to.z);
        short width = (short) (Math.abs(maxX - minX) + 1);
        short height = (short) (Math.abs(maxY - minY) + 1);
        short length = (short) (Math.abs(maxZ - minZ) + 1);

        short[][][] blocks = new short[width][height][length];
        byte[][][] metadata = new byte[width][height][length];
        List<TileEntity> tileEntities = new ArrayList<TileEntity>();
        TileEntity tileEntity = null;
        NBTTagCompound tileEntityNBT = null;

        for(int x = minX; x <= maxX; x++)
        {
            for(int y = minY; y <= maxY; y++)
            {
                for(int z = minZ; z <= maxZ; z++)
                {
                    blocks[x - minX][y - minY][z - minZ] = (short) GameData.getBlockRegistry().getId(world.getBlock(x, y, z));
                    metadata[x - minX][y - minY][z - minZ] = (byte) world.getBlockMetadata(x, y, z);
                    tileEntity = world.getTileEntity(x, y, z);
                    if(tileEntity != null)
                    {
                        tileEntityNBT = new NBTTagCompound();
                        tileEntity.writeToNBT(tileEntityNBT);

                        tileEntity = TileEntity.createAndLoadEntity(tileEntityNBT);
                        tileEntity.xCoord -= minX;
                        tileEntity.yCoord -= minY;
                        tileEntity.zCoord -= minZ;
                        tileEntities.add(tileEntity);
                    }
                }
            }
        }
        if(icon != null)
        {
            return new SchematicWorld(icon, blocks, metadata, tileEntities, width, height, length);
        }
        else
        {
            return new SchematicWorld(new ItemStack(Blocks.grass), blocks, metadata, tileEntities, width, height, length);
        }
    }

    public void setOrientation(int orientation) {
        //TODO schematic.rotate();
    }

    private static ResourceLocation getResourceLocation(String name)
    {
        return new ResourceLocation("minecolonies:schematics/" + name + ".schematic");
    }

    private static InputStream getStream(ResourceLocation res)
    {
        try
        {
            return Minecraft.getMinecraft().getResourceManager().getResource(res).getInputStream();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static String getName(IColony hut, int level)
    {
        return hut.getName() + level;
    }

    public Block getBlock()
    {
        if(!this.hasSchematic() || x == -1) return null;
        return this.schematic.getBlock(x, y, z);
    }

    public int getMetadata()
    {
        if(!this.hasSchematic() || x == -1) return 0;
        return this.schematic.getBlockMetadata(x, y, z);
    }

    public List<ItemStack> getMaterials()
    {
        return schematic.getBlockList();
    }

    public boolean hasSchematic()
    {
        return schematic != null;
    }
}
