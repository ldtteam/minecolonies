package com.minecolonies.util;

import com.minecolonies.blocks.AbstractBlockHut;
import com.minecolonies.configuration.Configurations;
import com.schematica.world.SchematicWorld;
import com.schematica.world.schematic.SchematicFormat;
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
public final class Schematic
{
    /**
     * The minecraft world this schematic is displayed in
     */
    private World            world;
    /**
     * the schematic world this schematic comes from
     */
    private SchematicWorld   schematicWorld;
    /**
     * the anchor position this schematic will be
     * placed on in the minecraft world
     */
    private ChunkCoordinates position;
    /**
     * the name this schematic has
     */
    private String           name;

    /**
     * North-West corner
     */
    private int x = -1;
    private int y = -1;
    private int z = -1;

    /**
     * Load a schematic into this world
     *
     * @param worldObj the world to load in
     * @param name     the schematics name
     */
    public Schematic(World worldObj, String name)
    {
        this(worldObj, getResourceLocation(name), name);
    }

    /**
     * Load a schematic into this world
     *
     * @param worldObj the world to load in
     * @param res      the resource location of this schematic
     * @param name     the schematics name
     */
    private Schematic(World worldObj, ResourceLocation res, String name)
    {
        this(worldObj, SchematicFormat.readFromStream(getStream(res)), name);
    }

    /**
     * Create a new Schematic
     *
     * @param worldObj       the world to show it in
     * @param schematicWorld the SchematicWorld it comes from
     * @param name           the name this schematic has
     */
    private Schematic(World worldObj, SchematicWorld schematicWorld, String name)
    {
        world = worldObj;
        this.schematicWorld = schematicWorld;
        this.name = name;
    }

    /**
     * Generate the stream from a resource location
     *
     * @param res the location to pull the stream from
     * @return a stream from this location
     */
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
                //todo: add more checks here
                return Schematic.class.getResourceAsStream(String.format("/assets/%s/%s", res.getResourceDomain(), res.getResourcePath()));
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Could not load stream!", e);
        }
    }

    /**
     * Generate a resource location from a schematics name
     *
     * @param name the schematics name
     * @return the resource location pointing towards the schematic
     */
    private static ResourceLocation getResourceLocation(String name)
    {
        return new ResourceLocation("minecolonies:schematics/" + name + ".schematic");
    }

    /**
     * Load a schematic into this world
     * and place it in the right position and rotation
     *
     * @param worldObj  the world to load it in
     * @param name      the schmetics name
     * @param x         x coordinate
     * @param y         y coordinate
     * @param z         z coordinate
     * @param rotations number of times rotated
     */
    public static void loadAndPlaceSchematicWithRotation(World worldObj, String name, int x, int y, int z, int rotations)
    {
        Schematic schematic;
        try
        {
            schematic = new Schematic(worldObj, name);
        }
        catch (IllegalStateException e)
        {
            Log.logger.warn("Could not load schematic!", e);
            return;
        }
        for (int i = 0; i < rotations; i++)
        {
            schematic.rotate();
        }
        schematic.placeSchematic(x,y,z);
    }

    /**
     * Place a schematic into the world.
     *
     * @param x anchor x position
     * @param y anchor y position
     * @param z anchor z position
     */
    private void placeSchematic(int x, int y, int z)
    {
        List<ChunkCoordinates> delayedBlocks = new ArrayList<>();

        for (int j = 0; j < schematicWorld.getHeight(); j++)
        {
            for (int k = 0; k < schematicWorld.getLength(); k++)
            {
                for (int i = 0; i < schematicWorld.getWidth(); i++)
                {

                    Block block    = this.schematicWorld.getBlock(i, j, k);
                    int   metadata = this.schematicWorld.getBlockMetadata(i, j, k);

                    if (block == Blocks.air && !world.getBlock(x + i, y + j, z + k).getMaterial().isSolid())
                    {
                        world.setBlockToAir(pos.add(i, j, k));
                    }
                    else if (block.getMaterial().isSolid())
                    {
                        world.setBlock(x + i, y + j, z + k, block, metadata, 0x03);
                        if (world.getBlock(x + i, y + j, z + k) == block)
                        {
                            if (world.getBlockMetadata(x + i, y + j, z + k) != metadata)
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
                    if (schematicWorld.getTileEntity(x,y,z) != null)
                    {
                        world.setTileEntity(pos.add(i, j, k), schematicWorld.getTileEntity(pos));
                    }
                }
            }
        }

        for (ChunkCoordinates coords : delayedBlocks)
        {
            int   i        = coords.posX;
            int   j        = coords.posY;
            int   k        = coords.posZ;
            Block block    = this.schematicWorld.getBlock(i, j, k);
            int   metadata = this.schematicWorld.getBlockMetadata(i, j, k);
            world.setBlock(x + i, y + j, z + k, block, metadata, 0x03);
            if (world.getBlock(x + i, y + j, z + k) == block)
            {
                if (world.getBlockMetadata(x + i, y + j, z + k) != metadata)
                {
                    world.setBlockMetadataWithNotify(x + i, y + j, z + k, metadata, 0x03);
                }
                block.onPostBlockPlaced(world, x + i, y + j, z + k, metadata);
            }
        }
    }

    /**
     * Rotate this schematic
     */
    private void rotate()
    {
        schematicWorld.rotate();
    }

    public static String saveSchematic(World world, ChunkCoordinates from, ChunkCoordinates to)
    {
        if (world == null || from == null || to == null)
        { throw new NullPointerException("Invalid method call, contact a developer."); }

        SchematicWorld schematic = scanSchematic(world, from, to, new ItemStack(Blocks.red_flower));

        String fileName = LanguageHandler.format("item.scepterSteel.scanFormat", schematic.getType(), System.currentTimeMillis());
        File   file     = new File(getScanDirectory(world), fileName);

        if (SchematicFormat.writeToFile(file, schematic))
        {
            return LanguageHandler.format("item.scepterSteel.scanSuccess", fileName);
        }
        return LanguageHandler.format("item.scepterSteel.scanFailure");
    }

    private static File getScanDirectory(World world)
    {
        File minecolonies;
        if (world.isRemote)
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
        if (!directory.exists())
        {
            directory.mkdirs();
        }
    }

    private static SchematicWorld scanSchematic(World world, ChunkCoordinates from, ChunkCoordinates to, ItemStack icon)
    {
        //todo: split up this method
        int   minX   = Math.min(from.posX, to.posX);
        int   maxX   = Math.max(from.posX, to.posX);
        int   minY   = Math.min(from.posY, to.posY);
        int   maxY   = Math.max(from.posY, to.posY);
        int   minZ   = Math.min(from.posZ, to.posZ);
        int   maxZ   = Math.max(from.posZ, to.posZ);
        short width  = (short) (Math.abs(maxX - minX) + 1);
        short height = (short) (Math.abs(maxY - minY) + 1);
        short length = (short) (Math.abs(maxZ - minZ) + 1);

        short[][][]      blocks       = new short[width][height][length];
        byte[][][]       metadata     = new byte[width][height][length];
        List<TileEntity> tileEntities = new ArrayList<>();
        TileEntity       tileEntity;
        NBTTagCompound   tileEntityNBT;

        int xOffset = 0, yOffset = 0, zOffset = 0;

        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++)
            {
                for (int z = minZ; z <= maxZ; z++)
                {
                    blocks[x - minX][y - minY][z - minZ] = (short) GameData.getBlockRegistry().getId(world.getBlock(x, y, z));
                    metadata[x - minX][y - minY][z - minZ] = (byte) world.getBlockMetadata(x, y, z);

                    if (world.getBlock(x, y, z) instanceof AbstractBlockHut)
                    {
                        if (xOffset == 0 && yOffset == 0 && zOffset == 0)
                        {
                            xOffset = x;
                            yOffset = y;
                            zOffset = z;
                        }
                        else
                        {
                            Log.logger.warn("Scan contained multiple AbstractBlockHut's ignoring this one");
                            blocks[x - minX][y - minY][z - minZ] = 0;
                            metadata[x - minX][y - minY][z - minZ] = 0;
                        }
                    }

                    tileEntity = world.getTileEntity(x, y, z);
                    if (tileEntity != null)//creates a new tileEntity and formats its data for saving
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
        @SuppressWarnings("unchecked")
        List<EntityHanging> entityHangings = world.getEntitiesWithinAABB(EntityHanging.class, region);
        @SuppressWarnings("unchecked")
        List<EntityMinecart> entityMinecarts = world.getEntitiesWithinAABB(EntityMinecart.class, region);
        NBTTagList entityList = new NBTTagList();

        entityHangings.stream().filter(entity -> entity != null).forEach(entity -> {
            NBTTagCompound entityData = new NBTTagCompound();

            entityData.setString("id", EntityList.getEntityString(entity));
            entity.writeToNBT(entityData);

            entityData.setTag("TileX", new NBTTagInt(entity.field_146063_b - minX));
            entityData.setTag("TileY", new NBTTagInt(entity.field_146064_c - minY));
            entityData.setTag("TileZ", new NBTTagInt(entity.field_146062_d - minZ));

            entityList.appendTag(entityData);
        });
        entityMinecarts.stream().filter(minecart -> minecart != null).forEach(minecart -> {

            NBTTagCompound entityData = new NBTTagCompound();

            entityData.setString("id", EntityList.getEntityString(minecart));
            minecart.writeToNBT(entityData);

            NBTTagList pos = new NBTTagList();
            pos.appendTag(new NBTTagDouble(minecart.posX - minX));
            pos.appendTag(new NBTTagDouble(minecart.posY - minY));
            pos.appendTag(new NBTTagDouble(minecart.posZ - minZ));
            entityData.setTag("Pos", pos);

            entityList.appendTag(entityData);
        });

        if (icon != null)
        {
            return new SchematicWorld(icon, blocks, metadata, tileEntities, entityList, width, height, length, xOffset, yOffset, zOffset);
        }
        else
        {
            return new SchematicWorld(new ItemStack(Blocks.red_mushroom), blocks, metadata, tileEntities, entityList, width, height, length, xOffset, yOffset, zOffset);
        }
    }

    /**
     * Find the next block that doesn't already exist in the world
     *
     * @return true if a new block is found and false if there is no next block.
     */
    public boolean findNextBlock()
    {
        int count = 0;
        do
        {
            count++;
            if (!incrementBlock())
            {
                return false;
            }

        }
        while (doesSchematicBlockEqualWorldBlock() && count < Configurations.maxBlocksCheckedByBuilder);

        return true;
    }

    public boolean incrementBlock()
    {
        if (x == -1)
        {
            y = z = 0;
        }

        x++;
        if (x == schematicWorld.getWidth())
        {
            x = 0;
            z++;
            if (z == schematicWorld.getLength())
            {
                z = 0;
                y++;
                if (y == schematicWorld.getHeight())
                {
                    x = y = z = -1;
                    return false;
                }
            }
        }

        return true;
    }

    public boolean doesSchematicBlockEqualWorldBlock()
    {
        ChunkCoordinates pos = this.getBlockPosition();
        //had this problem in a superflat world, causes builder to sit doing nothing because placement failed
        return pos.posY <= 0 || schematicWorld.getBlock(x, y, z) == ChunkCoordUtils.getBlock(world, pos) && schematicWorld.getBlockMetadata(x, y, z)
                                                                                                            == ChunkCoordUtils.getBlockMetadata(world, pos);
    }

    public ChunkCoordinates getBlockPosition()
    {
        return ChunkCoordUtils.add(getOffsetPosition(), x, y, z);
    }

    public ChunkCoordinates getOffsetPosition()
    {
        return ChunkCoordUtils.subtract(position, getOffset());
    }

    public ChunkCoordinates getOffset()
    {
        return new ChunkCoordinates(schematicWorld.getOffsetX(), schematicWorld.getOffsetY(), schematicWorld.getOffsetZ());
    }

    public boolean findNextBlockToClear()
    {

        int count = 0;
        do
        {
            count++;
            if (!decrementBlock())
            {
                return false;
            }

        }
        //Check for air blocks and if blocks below the hut are different from the schematicWorld
        while ((worldBlockAir() || (y <= getOffset().posY && doesSchematicBlockEqualWorldBlock())) && count < Configurations.maxBlocksCheckedByBuilder);

        return true;
    }

    public boolean findNextBlockSolid()
    {

        int count = 0;
        do
        {
            count++;
            if (!incrementBlock())
            {
                return false;
            }

        }
        while ((doesSchematicBlockEqualWorldBlock() || (!schematicWorld.getBlock(x, y, z).getMaterial().isSolid() && !schematicWorld.isAirBlock(x, y, z)))
               && count < Configurations.maxBlocksCheckedByBuilder);

        return true;
    }

    public boolean findNextBlockNonSolid()
    {

        int count = 0;
        do
        {
            count++;
            if (!incrementBlock())
            {
                return false;
            }

        }
        while ((doesSchematicBlockEqualWorldBlock() || (schematicWorld.getBlock(x, y, z).getMaterial().isSolid() || schematicWorld.isAirBlock(x, y, z)))
               && count < Configurations.maxBlocksCheckedByBuilder);

        return true;
    }

    public boolean decrementBlock()
    {
        if (x == -1 && y == -1 && z == -1)
        {
            x = schematicWorld.getWidth();
            y = schematicWorld.getHeight() - 1;
            z = schematicWorld.getLength() - 1;
        }

        x--;
        if (x == -1)
        {
            x = schematicWorld.getWidth() - 1;
            z--;
            if (z == -1)
            {
                z = schematicWorld.getLength() - 1;
                y--;
                if (y == -1)
                {
                    x = y = z = -1;
                    return false;
                }
            }
        }

        return true;
    }

    private boolean worldBlockAir()
    {
        ChunkCoordinates pos = this.getBlockPosition();
        //had this problem in a superflat world, causes builder to sit doing nothing because placement failed
        return pos.posY <= 0 || world.isAirBlock(pos.posX, pos.posY, pos.posZ);
    }

    public void rotate(int times)
    {
        for (int i = 0; i < times; i++)
        {
            rotate();
        }
    }

    public Block getBlock()
    {
        if (x == -1)
        {
            return null;
        }
        return this.schematicWorld.getBlock(x, y, z);
    }

    public int getMetadata()
    {
        if (x == -1)
        {
            return 0;
        }
        return this.schematicWorld.getBlockMetadata(x, y, z);
    }

    public TileEntity getTileEntity()
    {
        if (x == -1)
        {
            return null;
        }
        return this.schematicWorld.getTileEntity(x, y, z);
    }

    public ChunkCoordinates getBlockPosition(int baseX, int baseY, int baseZ)
    {
        return new ChunkCoordinates(baseX + x, baseY + y, baseZ + z);
    }

    public ChunkCoordinates getLocalPosition()
    {
        return new ChunkCoordinates(x, y, z);
    }

    public void setLocalPosition(ChunkCoordinates localPosition)
    {
        x = localPosition.posX;
        y = localPosition.posY;
        z = localPosition.posZ;
    }

    public ChunkCoordinates getPosition()
    {
        return position;
    }

    public void setPosition(ChunkCoordinates position)
    {
        this.position = position;
    }

    public String getName()
    {
        return name;
    }

    public List<ItemStack> getMaterials()
    {
        return schematicWorld.getBlockList();
    }

    public void useMaterial(ItemStack stack)
    {
        schematicWorld.removeFromBlockList(stack);
    }

    public int getHeight()
    {
        return schematicWorld.getHeight();
    }

    public int getWidth()
    {
        return schematicWorld.getWidth();
    }

    public int getLength()
    {
        return schematicWorld.getLength();
    }

    @SuppressWarnings("unchecked")
    public List<Entity> getEntities()
    {
        return schematicWorld.loadedEntityList;
    }

    public void reset()
    {
        x = -1;
        y = -1;
        z = -1;
    }

    public SchematicWorld getWorldForRender()
    {
        return schematicWorld;
    }

    //TODO rendering
//    public void refreshSchematic() {
//        for (RendererSchematicChunk renderer : this.sortedRendererSchematicChunk) {
//            renderer.setDirty();
//        }
//    }
}
