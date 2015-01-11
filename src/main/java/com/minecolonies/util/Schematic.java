package com.minecolonies.util;

import com.github.lunatrius.schematica.world.SchematicWorld;
import com.github.lunatrius.schematica.world.schematic.SchematicFormat;
import com.minecolonies.MineColonies;
import com.minecolonies.blocks.BlockHut;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.lib.IColony;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.ResourceLocation;
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
    private World            world;
    private SchematicWorld   schematic;
    private ChunkCoordinates position;
    private String           name;

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
        return new Schematic(worldObj, SchematicFormat.readFromStream(stream), getNameFromResourceLocation(res));
    }

    public void placeSchematic(int x, int y, int z)
    {
        List<ChunkCoordinates> delayedBlocks = new ArrayList<ChunkCoordinates>();

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
                        world.setBlock(x + i, y + j, z + k, block, metadata, 0x03);
                        if(world.getBlock(x + i, y + j, z + k) == block)
                        {
                            if(world.getBlockMetadata(x + i, y + j, z + k) != metadata)
                            {
                                world.setBlockMetadataWithNotify(x + i, y + j, z + k, metadata, 0x03);
                            }
                            block.onPostBlockPlaced(world, x + i, y + j, z + k, metadata);
                        }
                    }
                    else
                    {
                        delayedBlocks.add(new ChunkCoordinates(i, j, k));
                    }
                    if(schematic.getTileEntity(x, y, z) != null)
                    {
                        world.setTileEntity(x + i, y + j, z + k, schematic.getTileEntity(x, y, z));
                    }
                }
            }
        }

        for(ChunkCoordinates coords : delayedBlocks)
        {
            int i = coords.posX;
            int j = coords.posY;
            int k = coords.posZ;
            Block block = this.schematic.getBlock(i, j, k);
            int metadata = this.schematic.getBlockMetadata(i, j, k);
            world.setBlock(x + i, y + j, z + k, block, metadata, 0x03);
            if(world.getBlock(x + i, y + j, z + k) == block)
            {
                if(world.getBlockMetadata(x + i, y + j, z + k) != metadata)
                {
                    world.setBlockMetadataWithNotify(x + i, y + j, z + k, metadata, 0x03);
                }
                block.onPostBlockPlaced(world, x + i, y + j, z + k, metadata);
            }
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
        while(doesSchematicBlockEqualWorldBlock() && count < Configurations.maxBlocksCheckedByBuilder);

        return true;
    }

    public boolean doesSchematicBlockEqualWorldBlock()
    {
        ChunkCoordinates pos = this.getBlockPosition();
        if(pos.posY <= 0)//had this problem in a superflat world, causes builder to sit doing nothing because placement failed
        {
            return true;
        }
        return schematic.getBlock(x, y, z) == ChunkCoordUtils.getBlock(world, pos) && schematic.getBlockMetadata(x, y, z) == ChunkCoordUtils.getBlockMetadata(world, pos);
    }

    public static String saveSchematic(World world, ChunkCoordinates from, ChunkCoordinates to)
    {
        if(world == null || from == null || to == null)
            throw new NullPointerException("Invalid method call, contact a developer.");

        SchematicWorld schematic = scanSchematic(world, from, to, new ItemStack(Blocks.red_flower));

        String fileName = LanguageHandler.format("item.scepterSteel.scanFormat", schematic.getType(), System.currentTimeMillis());
        File file = new File(getScanDirectory(world), fileName);

        if(SchematicFormat.writeToFile(file, schematic))
        {
            return LanguageHandler.format("item.scepterSteel.scanSuccess", fileName);
        }
        return LanguageHandler.format("item.scepterSteel.scanFailure");
    }

    private static File getScanDirectory(World world)
    {
        File minecolonies;
        if(world.isRemote)
        {
            minecolonies = new File(Minecraft.getMinecraft().mcDataDir, "minecolonies/");
        }
        else
        {
            minecolonies = MinecraftServer.getServer().getFile("minecolonies/");
        }
        checkDirectory(minecolonies);

        File scans = new File(minecolonies, "scans/");
        checkDirectory(scans);
        return scans;
    }

    private static void checkDirectory(File directory)
    {
        if(!directory.exists())
        {
            directory.mkdir();
        }
    }

    public static SchematicWorld scanSchematic(World world, ChunkCoordinates from, ChunkCoordinates to, ItemStack icon)
    {
        int minX = Math.min(from.posX, to.posX);
        int maxX = Math.max(from.posX, to.posX);
        int minY = Math.min(from.posY, to.posY);
        int maxY = Math.max(from.posY, to.posY);
        int minZ = Math.min(from.posZ, to.posZ);
        int maxZ = Math.max(from.posZ, to.posZ);
        short width = (short) (Math.abs(maxX - minX) + 1);
        short height = (short) (Math.abs(maxY - minY) + 1);
        short length = (short) (Math.abs(maxZ - minZ) + 1);

        short[][][] blocks = new short[width][height][length];
        byte[][][] metadata = new byte[width][height][length];
        List<TileEntity> tileEntities = new ArrayList<TileEntity>();
        TileEntity tileEntity;
        NBTTagCompound tileEntityNBT;

        int xOffset = 0, yOffset = 0, zOffset = 0;

        for(int x = minX; x <= maxX; x++)
        {
            for(int y = minY; y <= maxY; y++)
            {
                for(int z = minZ; z <= maxZ; z++)
                {
                    blocks[x - minX][y - minY][z - minZ] = (short) GameData.getBlockRegistry().getId(world.getBlock(x, y, z));
                    metadata[x - minX][y - minY][z - minZ] = (byte) world.getBlockMetadata(x, y, z);

                    if(world.getBlock(x, y, z) instanceof BlockHut)
                    {
                        if(xOffset == 0 && yOffset == 0 && zOffset == 0)
                        {
                            xOffset = x;
                            yOffset = y;
                            zOffset = z;
                        }
                        else
                        {
                            MineColonies.logger.warn("Scan contained multiple BlockHut's ignoring this one");
                            blocks[x - minX][y - minY][z - minZ] = 0;
                            metadata[x - minX][y - minY][z - minZ] = 0;
                        }
                    }

                    tileEntity = world.getTileEntity(x, y, z);
                    if(tileEntity != null)//creates a new tileEntity and formats its data for saving
                    {                     //a new tileEntity is needed to prevent changes to the real one
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

        AxisAlignedBB region = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        List<EntityHanging> entityHangings = world.getEntitiesWithinAABB(EntityHanging.class, region);
        List<EntityMinecart> entityMinecarts = world.getEntitiesWithinAABB(EntityMinecart.class, region);
        NBTTagList entityList = new NBTTagList();

        for(EntityHanging entity : entityHangings)
        {
            if(entity != null)
            {
                NBTTagCompound entityData = new NBTTagCompound();

                entityData.setString("id", EntityList.getEntityString(entity));
                entity.writeToNBT(entityData);

                entityData.setTag("TileX", new NBTTagInt(entity.field_146063_b - minX));
                entityData.setTag("TileY", new NBTTagInt(entity.field_146064_c - minY));
                entityData.setTag("TileZ", new NBTTagInt(entity.field_146062_d - minZ));

                entityList.appendTag(entityData);
            }
        }
        for(EntityMinecart minecart : entityMinecarts)
        {
            if(minecart != null)
            {

                NBTTagCompound entityData = new NBTTagCompound();

                entityData.setString("id", EntityList.getEntityString(minecart));
                minecart.writeToNBT(entityData);

                NBTTagList pos = new NBTTagList();
                pos.appendTag(new NBTTagDouble(minecart.posX - minX));
                pos.appendTag(new NBTTagDouble(minecart.posY - minY));
                pos.appendTag(new NBTTagDouble(minecart.posZ - minZ));
                entityData.setTag("Pos", pos);

                entityList.appendTag(entityData);
            }
        }

        if(icon != null)
        {
            return new SchematicWorld(icon, blocks, metadata, tileEntities, entityList, width, height, length, xOffset, yOffset, zOffset);
        }
        else
        {
            return new SchematicWorld(new ItemStack(Blocks.red_mushroom), blocks, metadata, tileEntities, entityList, width, height, length, xOffset, yOffset, zOffset);
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
        return new ResourceLocation("minecolonies:schematics/classic/" + name + ".schematic");
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

    public ChunkCoordinates getOffset()
    {
        return new ChunkCoordinates(schematic.getOffsetX(), schematic.getOffsetY(), schematic.getOffsetZ());
    }

    public ChunkCoordinates getOffsetPosition()
    {
        return ChunkCoordUtils.subtract(getOffset(), position);//I know this seems backwards, minecraft's fault
    }

    public ChunkCoordinates getBlockPosition()
    {
        return ChunkCoordUtils.add(getOffsetPosition(), x, y, z);
    }

    public ChunkCoordinates getBlockPosition(int baseX, int baseY, int baseZ)
    {
        return new ChunkCoordinates(baseX + x, baseY + y, baseZ + z);
    }

    public ChunkCoordinates getLocalPosition()
    {
        return new ChunkCoordinates(x, y, z);
    }

    public ChunkCoordinates getPosition()
    {
        return position;
    }

    public String getName()
    {
        return name;
    }

    public void setPosition(ChunkCoordinates position)
    {
        this.position = position;
    }

    public void setLocalPosition(ChunkCoordinates localPosition)
    {
        x = localPosition.posX;
        y = localPosition.posY;
        z = localPosition.posZ;
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

    public List<Entity> getEntities()
    {
        return schematic.loadedEntityList;
    }

    //TODO rendering
//    public void refreshSchematic() {
//        for (RendererSchematicChunk renderer : this.sortedRendererSchematicChunk) {
//            renderer.setDirty();
//        }
//    }
}
