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
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.File;
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
    private Vec3           position;
    private String         name;

    /**
     * North-West corner
     */
    private int x = -1, y = -1, z = -1;

    //public final  List<RendererSchematicChunk> sortedRendererSchematicChunk = new ArrayList<RendererSchematicChunk>();

    private Schematic(World worldObj, SchematicWorld schematicWorld, String name)
    {
        world = worldObj;
        schematic = schematicWorld;
        this.name = name;
    }

    public static Schematic loadSchematic(World worldObj, IColony hut, int level)
    {
        return loadSchematic(worldObj, getNameFromHut(hut, level));
    }

    public static Schematic loadSchematic(World worldObj, String name)
    {
        return loadSchematic(worldObj, getResourceLocation(name));
    }

    private static Schematic loadSchematic(World worldObj, ResourceLocation res)
    {
        InputStream stream = getStream(res);
        if(stream == null)
        {
            return null;
        }
        return new Schematic(worldObj, SchematicFormat.readFromStream(getStream(res)), getNameFromResourceLocation(res));
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

                    if(block == Blocks.air && !world.getBlock(x + i, y + j, z + k).getMaterial().isSolid())
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
                    if(schematic.getTileEntity(x, y, z) != null)
                    {
                        world.setTileEntity(x + i, y + j, z + k, schematic.getTileEntity(x, y, z));
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

    /**
     * Find the next block that doesn't already exist in the world
     *
     * @return true if a new block is found and false if there is no next block.
     */
    public boolean findNextBlock()
    {
        if(!this.hasSchematic()) return false;

        if(x == -1)
        {
            y = z = 0;
        }

        int count = 0;

        do
        {
            count++;

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
                        return false;
                    }
                }
            }
        }
        while(doesSchematicBlockEqualWorldBlock() && count < 50);//count limits the number of checked blocks per builder update
        //TODO change count to agreed upon value, possibly add config option - possibly remove if we think this shouldn't be a problem

        return true;
    }

    private boolean doesSchematicBlockEqualWorldBlock()
    {
        int[] pos = Utils.vecToInt(this.getBlockPosition());
        return schematic.getBlock(x, y, z) == world.getBlock(pos[0], pos[1], pos[2]) && schematic.getBlockMetadata(x, y, z) == world.getBlockMetadata(pos[0], pos[1], pos[2]);
    }

    public static void saveSchematic(World world, Vec3 from, Vec3 to, String file, String icon)
    {
        if(world == null || from == null || to == null || file == null) return;
        SchematicFormat.writeToFile(MinecraftServer.getServer().getFile(file + ".schematic"), scanSchematic(world, from, to, icon));
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

    private static String getNameFromResourceLocation(ResourceLocation res)
    {
        String path = res.getResourcePath();
        return path.substring(path.indexOf('/') + 1, path.lastIndexOf('.'));
    }

    private static InputStream getStream(ResourceLocation res)
    {
        try
        {
            if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                return Minecraft.getMinecraft().getResourceManager().getResource(res).getInputStream();
            }
            else
            {
                //TODO add more checks here
                return Schematic.class.getResourceAsStream(String.format("/assets/%s/%s", res.getResourceDomain(), res.getResourcePath()));
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String getNameFromHut(IColony hut, int level)
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

    public TileEntity getTileEntity()
    {
        if(!this.hasSchematic() || x == -1) return null;
        return this.schematic.getTileEntity(x, y, z);
    }

    public Vec3 getBlockPosition()
    {
        return position.addVector(x - schematic.getOffsetX(), y  - schematic.getOffsetY(), z  - schematic.getOffsetZ());
    }

    public Vec3 getBlockPosition(int baseX, int baseY, int baseZ)
    {
        return world.getWorldVec3Pool().getVecFromPool(baseX + x, baseY + y, baseZ + z);
    }

    public Vec3 getLocalPosition()
    {
        return Vec3.createVectorHelper(x, y, z);
    }

    public Vec3 getPosition()
    {
        return position;
    }

    public String getName()
    {
        return name;
    }

    public void setPosition(Vec3 position)
    {
        this.position = position;
    }

    public void setLocalPosition(Vec3 localPosition)
    {
        x = (int) localPosition.xCoord;
        y = (int) localPosition.yCoord;
        z = (int) localPosition.zCoord;
    }

    public List<ItemStack> getMaterials()
    {
        return schematic.getBlockList();
    }

    public void useMaterial(ItemStack stack)
    {
        schematic.removeFromBlockList(stack);
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
//    public void refreshSchematic() {
//        for (RendererSchematicChunk renderer : this.sortedRendererSchematicChunk) {
//            renderer.setDirty();
//        }
//    }
}
