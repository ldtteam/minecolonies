package com.minecolonies.util;

import com.github.lunatrius.schematica.world.SchematicWorld;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import com.minecolonies.lib.IColony;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for using the Schematica codebase
 *
 * @author Colton
 */
public class Schematic
{
    private World          world;
    private SchematicWorld schematic;

    /**
     * North-West corner
     */
    private int x = -1, y = -1, z = -1;
    private ChunkCache mcWorldCache;
    //public final  List<RendererSchematicChunk> sortedRendererSchematicChunk = new ArrayList<RendererSchematicChunk>();

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
        List<Vec3> delayedBlocks = new ArrayList<Vec3>();

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
                        world.setBlockToAir(x + i, y + j, z + k);
                    }
                    else if(block.getMaterial().isSolid())
                    {
                        world.setBlock(x + i, y + j, z + k, block, metadata, 0x02);
                    }
                    else
                    {
                        delayedBlocks.add(Vec3.createVectorHelper(i, j, k));
                    }
                }
            }
        }

        for(Vec3 vec : delayedBlocks)
        {
            int i = (int) vec.xCoord;
            int j = (int) vec.yCoord;
            int k = (int) vec.zCoord;
            Block block = this.schematic.getBlock(i, j, k);
            int metadata = this.schematic.getBlockMetadata(i, j, k);
            world.setBlock(x + i, y + j, z + k, block, metadata, 0x02);
        }
    }

    public static void loadAndPlaceSchematic(World worldObj, String name, int x, int y, int z)
    {
        loadAndPlaceSchematicWithRotation(worldObj, name, x, y, z, 0);
    }

    public static void loadAndPlaceSchematicWithRotation(World worldObj, String name, int x, int y, int z, int rotations)
    {
        Schematic schematic = loadSchematic(worldObj, name);
        if(schematic.hasSchematic())
        {
            for(int i = 0; i < rotations; i++)
            {
                schematic.rotate();
            }
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

    public static void saveSchematic(World world, Vec3 from, Vec3 to, String file, String icon)
    {
        if(world == null || from == null || to == null || file == null) return;
        SchematicFormat.writeToFile(MinecraftServer.getServer().getFile("/schematics"), file, scanSchematic(world, from, to, icon));
    }

    public static SchematicWorld scanSchematic(World world, Vec3 from, Vec3 to, String icon)
    {
        int minX = (int) Math.min(from.xCoord, to.xCoord);
        int maxX = (int) Math.max(from.xCoord, to.xCoord);
        int minY = (int) Math.min(from.yCoord, to.yCoord);
        int maxY = (int) Math.max(from.yCoord, to.yCoord);
        int minZ = (int) Math.min(from.zCoord, to.zCoord);
        int maxZ = (int) Math.max(from.zCoord, to.zCoord);
        short width = (short) (Math.abs(maxX - minX) + 1);
        short height = (short) (Math.abs(maxY - minY) + 1);
        short length = (short) (Math.abs(maxZ - minZ) + 1);

        short[][][] blocks = new short[width][height][length];
        byte[][][] metadata = new byte[width][height][length];
        List<TileEntity> tileEntities = new ArrayList<TileEntity>();
        TileEntity tileEntity;
        NBTTagCompound tileEntityNBT;

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
            return new SchematicWorld(new ItemStack(Blocks.red_mushroom), blocks, metadata, tileEntities, width, height, length);
        }
    }

    public void setOrientation(ForgeDirection orientation)
    {
        //TODO schematic.rotate();
    }

    public void rotate()
    {
        schematic.rotate();
    }

    private static ResourceLocation getResourceLocation(String name)
    {
        return new ResourceLocation("minecolonies:schematics/" + name + ".schematic");
    }

    private static InputStream getStream(ResourceLocation res)
    {
        try
        {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                return Minecraft.getMinecraft().getResourceManager().getResource(res).getInputStream();
            }
            else
            {
                return Schematic.class.getResourceAsStream(String.format("/assets/%s/%s", res.getResourceDomain(), res.getResourcePath()));
            }
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

    public Vec3 getBlockPosition(int baseX, int baseY, int baseZ)
    {
        return world.getWorldVec3Pool().getVecFromPool(baseX + x, baseY + y, baseZ + z);
    }

    public List<ItemStack> getMaterials()
    {
        return schematic.getBlockList();
    }

    public boolean hasSchematic()
    {
        return schematic != null;
    }

    public int getHeight()
    {
        return schematic.getHeight();
    }

    public int getWidth()
    {
        return schematic.getWidth();
    }

    public int getLength()
    {
        return schematic.getLength();
    }

    //TODO rendering

//    public void reloadChunkCache() {
//        if (schematic != null) {
//            this.mcWorldCache = new ChunkCache(world, x - 1, y - 1, z - 1, x + schematic.getWidth() + 1, y + schematic.getHeight() + 1, z + schematic.getLength() + 1, 0);
//            refreshSchematic();
//        }
//    }
//
//    public void refreshSchematic() {
//        for (RendererSchematicChunk renderer : this.sortedRendererSchematicChunk) {
//            renderer.setDirty();
//        }
//    }
}
