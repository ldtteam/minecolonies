package com.minecolonies.util;

import com.minecolonies.blocks.AbstractBlockHut;
import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.lib.Constants;
import com.schematica.client.util.RotationHelper;
import com.schematica.world.schematic.SchematicFormat;
import com.schematica.world.storage.Schematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public final class SchematicWrapper
{
    /**
     * The position we use as our uninitialized value.
     */
    private static final BlockPos                 NULL_POS            = new BlockPos(-1, -1, -1);
    private static final int                      NUMBER_OF_ROTATIONS = 4;
    private static final int                      REVERSE_ROTATION    = 3;
    private static final int                      TWO_FOR_HALVING     = 2;
    private static final ItemStack                DEFUALT_ICON        = new ItemStack(Blocks.RED_MUSHROOM_BLOCK);
    /**
     * The SchematicWorld position we are at. Defaulted to NULL_POS.
     */
    private final        BlockPos.MutableBlockPos progressPos         = new BlockPos.MutableBlockPos(-1, -1, -1);
    /**
     * The minecraft world this schematic is displayed in.
     */
    private World     world;
    /**
     * The schematic world this schematic comes from.
     */
    private Schematic schematicWorld;
    /**
     * The anchor position this schematic will be
     * placed on in the minecraft world.
     */
    private BlockPos  position;
    /**
     * The name this schematic has.
     */
    private String    name;

    /**
     * Load a schematic into this world.
     *
     * @param worldObj the world to load in
     * @param name     the schematics name
     */
    public SchematicWrapper(World worldObj, @NotNull String name)
    {
        this(worldObj, getResourceLocation(name), name);
    }

    /**
     * Load a schematic into this world.
     *
     * @param worldObj the world to load in
     * @param res      the resource location of this schematic
     * @param name     the schematics name
     */
    private SchematicWrapper(World worldObj, @NotNull ResourceLocation res, String name)
    {
        this(worldObj, SchematicFormat.readFromStream(getStream(res)), name);
    }

    /**
     * Generate a resource location from a schematics name.
     *
     * @param name the schematics name
     * @return the resource location pointing towards the schematic
     */
    @NotNull
    private static ResourceLocation getResourceLocation(@NotNull String name)
    {
        return new ResourceLocation("minecolonies:schematics/" + name + ".schematic");
    }

    /**
     * Create a new Schematic.
     *
     * @param worldObj       the world to show it in
     * @param schematicWorld the SchematicWorld it comes from
     * @param name           the name this schematic has
     */
    private SchematicWrapper(World worldObj, Schematic schematicWorld, String name)
    {
        world = worldObj;
        this.schematicWorld = schematicWorld;
        this.name = name;
    }

    /**
     * Generate the stream from a resource location.
     *
     * @param res the location to pull the stream from
     * @return a stream from this location
     */
    private static InputStream getStream(@NotNull ResourceLocation res)
    {
        try
        {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                return Minecraft.getMinecraft().getResourceManager().getResource(res).getInputStream();
            }
            else
            {
                return SchematicWrapper.class.getResourceAsStream(String.format("/assets/%s/%s", res.getResourceDomain(), res.getResourcePath()));
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Could not load stream!", e);
        }
    }

    /**
     * Load a schematic into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj  the world to load it in
     * @param name      the schematics name
     * @param pos       coordinates
     * @param rotations number of times rotated
     */
    public static void loadAndPlaceSchematicWithRotation(World worldObj, @NotNull String name, @NotNull BlockPos pos, int rotations)
    {
        try
        {
            @NotNull SchematicWrapper schematic = new SchematicWrapper(worldObj, name);
            schematic.rotate(rotations);
            schematic.placeSchematic(pos);
        }
        catch (IllegalStateException e)
        {
            Log.getLogger().warn("Could not load schematic!", e);
        }
    }

    /**
     * Rotate this schematic.
     *
     * @param times how many times to rotate the schematic.
     */
    public void rotate(int times)
    {
        if (times % NUMBER_OF_ROTATIONS == REVERSE_ROTATION)
        {
            //reverse rotate
            rotate(EnumFacing.DOWN);
        }
        else
        {
            for (int i = 0; i < times; i++)
            {
                //normal rotate
                rotate(EnumFacing.UP);
            }
        }
    }

    /**
     * Place a schematic into the world.
     *
     * @param pos coordinates
     */
    private void placeSchematic(@NotNull BlockPos pos)
    {
        setLocalPosition(pos);

        @NotNull List<BlockPos> delayedBlocks = new ArrayList<>();

        for (int j = 0; j < schematicWorld.getHeight(); j++)
        {
            for (int k = 0; k < schematicWorld.getLength(); k++)
            {
                for (int i = 0; i < schematicWorld.getWidth(); i++)
                {
                    @NotNull BlockPos localPos = new BlockPos(i, j, k);
                    IBlockState localState = this.schematicWorld.getBlockState(localPos);
                    Block localBlock = localState.getBlock();

                    BlockPos worldPos = pos.add(localPos);
                    IBlockState worldState = world.getBlockState(worldPos);

                    if (localBlock == ModBlocks.blockSubstitution)
                    {
                        continue;
                    }
                    else if (localBlock == Blocks.AIR && !worldState.getMaterial().isSolid())
                    {
                        world.setBlockToAir(worldPos);
                    }
                    else if (localState.getMaterial().isSolid())
                    {
                        placeBlock(localState, localBlock, worldPos);
                    }
                    else
                    {
                        delayedBlocks.add(localPos);
                    }

                    //setTileEntity checks for null and ignores it.
                    world.setTileEntity(worldPos, schematicWorld.getTileEntity(localPos));
                }
            }
        }

        for (@NotNull BlockPos coords : delayedBlocks)
        {
            IBlockState localState = this.schematicWorld.getBlockState(coords);
            Block localBlock = localState.getBlock();
            BlockPos newWorldPos = pos.add(coords);

            placeBlock(localState, localBlock, newWorldPos);
        }
    }

    private void rotate(@NotNull EnumFacing facing)
    {
        try
        {
            schematicWorld = RotationHelper.rotate(schematicWorld, facing, true);
        }
        catch (RotationHelper.RotationException e)
        {
            Log.getLogger().debug(e);
        }
    }

    private void placeBlock(IBlockState localState, @NotNull Block localBlock, @NotNull BlockPos worldPos)
    {
        world.setBlockState(worldPos, localState, 0x03);
        if (world.getBlockState(worldPos).getBlock() == localBlock)
        {
            if (world.getBlockState(worldPos) != localState)
            {
                world.setBlockState(worldPos, localState, 0x03);
            }
            localBlock.onBlockAdded(world, worldPos, localState);
        }
    }

    /**
     * Scan the schematic and save it to the disk.
     *
     * @param world Current world.
     * @param from  First corner.
     * @param to    Second corner.
     * @return Message to display to the player.
     */
    public static String saveSchematic(@Nullable World world, @Nullable BlockPos from, @Nullable BlockPos to)
    {
        if (world == null || from == null || to == null)
        {
            throw new IllegalArgumentException("Invalid method call, arguments can't be null. Contact a developer.");
        }

        BlockPos blockpos =
                new BlockPos(Math.min(from.getX(), to.getX()), Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        BlockPos blockpos1 =
                new BlockPos(Math.max(from.getX(), to.getX()), Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));
        BlockPos size = blockpos1.subtract(blockpos).add(1, 1, 1);

        WorldServer worldserver = (WorldServer) world;
        MinecraftServer minecraftserver = world.getMinecraftServer();
        TemplateManager templatemanager = worldserver.getStructureTemplateManager();

        String fileName = getScanDirectory(world).getAbsolutePath() + LanguageHandler.format("item.scepterSteel.scanFormat", "/scans/", System.currentTimeMillis());
        Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(fileName));
        template.takeBlocksFromWorld(world, blockpos, size, true, Blocks.STRUCTURE_VOID);
        template.setAuthor(Constants.MOD_ID);

        if (templatemanager.writeTemplate(minecraftserver, new ResourceLocation(fileName)))
        {
            return LanguageHandler.format("item.scepterSteel.scanSuccess", fileName);
        }
        return LanguageHandler.format("item.scepterSteel.scanFailure");
    }

    @NotNull
    private static Schematic scanSchematic(@NotNull World world, @NotNull BlockPos from, @NotNull BlockPos to)
    {
        int minX = Math.min(from.getX(), to.getX());
        int maxX = Math.max(from.getX(), to.getX());
        int minY = Math.min(from.getY(), to.getY());
        int maxY = Math.max(from.getY(), to.getY());
        int minZ = Math.min(from.getZ(), to.getZ());
        int maxZ = Math.max(from.getZ(), to.getZ());
        short width = (short) (Math.abs(maxX - minX) + 1);
        short height = (short) (Math.abs(maxY - minY) + 1);
        short length = (short) (Math.abs(maxZ - minZ) + 1);

        @NotNull BlockPos minPos = new BlockPos(minX, minY, minZ);

        @NotNull Schematic schematic = new Schematic(DEFUALT_ICON, width, height, length);

        @NotNull BlockPos.MutableBlockPos offset = new BlockPos.MutableBlockPos(0, 0, 0);

        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++)
            {
                for (int z = minZ; z <= maxZ; z++)
                {
                    @NotNull BlockPos worldPos = new BlockPos(x, y, z);
                    BlockPos localPos = worldPos.subtract(minPos);

                    IBlockState blockState = world.getBlockState(worldPos);
                    schematic.setBlockState(localPos, blockState);

                    if (blockState.getBlock() instanceof AbstractBlockHut)
                    {
                        if (BlockPosUtil.isEqual(offset, 0, 0, 0))
                        {
                            BlockPosUtil.set(offset, localPos);
                        }
                        else
                        {
                            schematic.setBlockState(localPos, Blocks.AIR.getDefaultState());
                            Log.getLogger().warn("Scan contained multiple AbstractBlockHut's ignoring this one");
                        }
                    }

                    TileEntity tileEntity = world.getTileEntity(worldPos);
                    saveTileEntity(schematic, tileEntity, minPos);
                }
            }
        }
        if (BlockPosUtil.isEqual(offset, 0, 0, 0))
        {
            offset.setPos(width / TWO_FOR_HALVING, 0, length / TWO_FOR_HALVING);
        }
        schematic.setOffset(offset);

        @NotNull AxisAlignedBB region = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
        //schematic::addEntity already does null checking and ignores null values.
        world.getEntitiesWithinAABB(EntityHanging.class, region).forEach(schematic::addEntity);
        world.getEntitiesWithinAABB(EntityMinecart.class, region).forEach(schematic::addEntity);

        return schematic;
    }

    @NotNull
    private static File getScanDirectory(@NotNull World world)
    {
        File minecolonies;
        if (world.isRemote)
        {
            minecolonies = new File(Minecraft.getMinecraft().mcDataDir, "minecolonies/");
        }
        else
        {
            minecolonies = Minecraft.getMinecraft().getIntegratedServer().getFile("minecolonies/");
        }
        checkDirectory(minecolonies);

        @NotNull File scans = new File(minecolonies, "scans/");
        checkDirectory(scans);
        return scans;
    }

    /**
     * Creates a new tileEntity and formats its data for saving. A new tileEntity is needed to prevent changes to the real one.
     *
     * @param schematic  Schematic to add the TileEntity to.
     * @param tileEntity The tile entity.
     * @param minPos     The schematic min pos to subtract of the tile entity.
     */
    private static void saveTileEntity(@NotNull Schematic schematic, @Nullable TileEntity tileEntity, @NotNull BlockPos minPos)
    {
        if (tileEntity != null)
        {
            BlockPos newPos = tileEntity.getPos().subtract(minPos);
            @NotNull NBTTagCompound tileEntityNBT = new NBTTagCompound();
            tileEntity.writeToNBT(tileEntityNBT);

            TileEntity newTileEntity = TileEntity.create(tileEntity.getWorld(), tileEntityNBT);
            newTileEntity.setPos(newPos);
            schematic.setTileEntity(newPos, newTileEntity);
        }
    }

    private static void checkDirectory(@NotNull File directory)
    {
        if (!directory.exists() && !directory.mkdirs())
        {
            Log.getLogger().error("Directory doesn't exist and failed to be created: " + directory.toString());
        }
    }

    /**
     * Find the next block that doesn't already exist in the world.
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

    /**
     * Increment progressPos.
     *
     * @return false if the all the block have been incremented through.
     */
    public boolean incrementBlock()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            this.progressPos.setPos(-1, 0, 0);
        }

        this.progressPos.setPos(this.progressPos.getX() + 1, this.progressPos.getY(), this.progressPos.getZ());
        if (this.progressPos.getX() == schematicWorld.getWidth())
        {
            this.progressPos.setPos(0, this.progressPos.getY(), this.progressPos.getZ() + 1);
            if (this.progressPos.getZ() == schematicWorld.getLength())
            {
                this.progressPos.setPos(this.progressPos.getX(), this.progressPos.getY() + 1, 0);
                if (this.progressPos.getY() == schematicWorld.getHeight())
                {
                    reset();
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if the block in the world is the same as what is in the schematic.
     *
     * @return true if the schematic block equals the world block.
     */
    public boolean doesSchematicBlockEqualWorldBlock()
    {
        IBlockState schematicBlockState = schematicWorld.getBlockState(this.getLocalPosition());
        Block schematicBlock = schematicBlockState.getBlock();

        //All worldBlocks are equal the substitution block
        if (schematicBlock == ModBlocks.blockSubstitution)
        {
            return true;
        }

        BlockPos worldPos = this.getBlockPosition();

        IBlockState worldBlockState = world.getBlockState(worldPos);

        //list of things to only check block for.
        //For the time being any flower pot is equal to each other.
        if (schematicBlock instanceof BlockDoor || schematicBlock == Blocks.FLOWER_POT)
        {
            return schematicBlock == worldBlockState.getBlock();
        }
        else if (schematicBlock instanceof BlockStairs && schematicBlockState == worldBlockState)
        {
            return true;
        }

        //had this problem in a super flat world, causes builder to sit doing nothing because placement failed
        return worldPos.getY() <= 0
                 || schematicBlockState == worldBlockState;
    }

    /**
     * Reset the progressPos.
     */
    public void reset()
    {
        BlockPosUtil.set(this.progressPos, NULL_POS);
    }

    /**
     * @return progressPos as an immutable.
     */
    @NotNull
    public BlockPos getLocalPosition()
    {
        return this.progressPos.toImmutable();
    }

    /**
     * @return World position.
     */
    public BlockPos getBlockPosition()
    {
        return this.progressPos.add(getOffsetPosition());
    }

    /**
     * @return Min world position for the schematic.
     */
    public BlockPos getOffsetPosition()
    {
        return position.subtract(getOffset());
    }

    /**
     * @return Where the hut (or any offset) is in the schematic.
     */
    public BlockPos getOffset()
    {
        return schematicWorld.getOffset();
    }

    /**
     * Change the current progressPos. Used when loading progress.
     *
     * @param localPosition new progressPos.
     */
    public void setLocalPosition(@NotNull BlockPos localPosition)
    {
        BlockPosUtil.set(this.progressPos, localPosition);
    }

    /**
     * Looks for blocks that should be cleared out.
     *
     * @return false if there are no more blocks to clear.
     */
    public boolean findNextBlockToClear()
    {
        int count = 0;
        do
        {
            count++;
            //decrement because we clear from top to bottom.
            if (!decrementBlock())
            {
                return false;
            }
        }
        //Check for air blocks and if blocks below the hut are different from the schematicWorld
        while ((worldBlockAir() || doesSchematicBlockEqualWorldBlock()) && count < Configurations.maxBlocksCheckedByBuilder);

        return true;
    }

    private boolean isAirBlock()
    {
        return getBlock() == Blocks.AIR;
    }

    /**
     * Calculate the current block in the schematic
     *
     * @return the current block or null if not initialized
     */
    @Nullable
    public Block getBlock()
    {
        @Nullable IBlockState state = getBlockState();
        if (state == null)
        {
            return null;
        }
        return state.getBlock();
    }

    /**
     * Gets the block state for the current local block.
     *
     * @return Current local block state.
     */
    @Nullable
    public IBlockState getBlockState()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            return null;
        }
        return this.schematicWorld.getBlockState(this.progressPos);
    }

    /**
     * Looks for structure blocks to place.
     *
     * @return false if there are no more structure blocks left.
     */
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
        while ((doesSchematicBlockEqualWorldBlock() || isBlockNonSolid()) && count < Configurations.maxBlocksCheckedByBuilder);

        return true;
    }

    private boolean isBlockNonSolid()
    {
        return getBlock() != null && !getBlockState().getMaterial().isSolid();
    }

    /**
     * Looks for decoration blocks to place.
     *
     * @return false if there are no more structure blocks left.
     */
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
        while ((doesSchematicBlockEqualWorldBlock() || isBlockSolid()) && count < Configurations.maxBlocksCheckedByBuilder);

        return true;
    }

    private boolean isBlockSolid()
    {
        return getBlock() != null && (getBlockState().getMaterial().isSolid());
    }

    /**
     * Decrement progressPos.
     *
     * @return false if progressPos can't be decremented any more.
     */
    public boolean decrementBlock()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            this.progressPos.setPos(schematicWorld.getWidth(), schematicWorld.getHeight() - 1, schematicWorld.getLength() - 1);
        }

        this.progressPos.setPos(this.progressPos.getX() - 1, this.progressPos.getY(), this.progressPos.getZ());
        if (this.progressPos.getX() == -1)
        {
            this.progressPos.setPos(schematicWorld.getWidth() - 1, this.progressPos.getY(), this.progressPos.getZ() - 1);
            if (this.progressPos.getZ() == -1)
            {
                this.progressPos.setPos(this.progressPos.getX(), this.progressPos.getY() - 1, schematicWorld.getLength() - 1);
                if (this.progressPos.getY() == -1)
                {
                    reset();
                    return false;
                }
            }
        }

        return true;
    }

    private boolean worldBlockAir()
    {
        BlockPos pos = this.getBlockPosition();
        //had this problem in a superflat world, causes builder to sit doing nothing because placement failed
        return pos.getY() <= 0 || world.isAirBlock(pos);
    }

    /**
     * @return The current local tile entity.
     */
    @Nullable
    public TileEntity getTileEntity()
    {
        if (this.progressPos.equals(NULL_POS))
        {
            return null;
        }
        return this.schematicWorld.getTileEntity(this.progressPos);
    }

    /**
     * @return A list of all the entities in the schematic.
     */
    @NotNull
    public List<Entity> getEntities()
    {
        return schematicWorld.getEntities();
    }

    /**
     * Base position of the schematic.
     *
     * @return BlockPos representing where the schematic is.
     */
    public BlockPos getPosition()
    {
        return position;
    }

    /**
     * Set the position, used when loading.
     *
     * @param position Where the schematic is in the world.
     */
    public void setPosition(BlockPos position)
    {
        this.position = position;
    }

    /**
     * Calculate the item needed to place the current block in the schematic
     *
     * @return an item or null if not initialized
     */
    @Nullable
    public Item getItem()
    {
        @Nullable Block block = this.getBlock();
        @Nullable IBlockState blockState = this.getBlockState();
        if (block == null || blockState == null)
        {
            return null;
        }

        return BlockUtils.getItemStackFromBlockState(blockState).getItem();
    }

    /**
     * @return The name of the schematic.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return The height of the schematic.
     */
    public int getHeight()
    {
        return schematicWorld.getHeight();
    }

    /**
     * @return The width of the schematic.
     */
    public int getWidth()
    {
        return schematicWorld.getWidth();
    }

    /**
     * @return The length of the schematic.
     */
    public int getLength()
    {
        return schematicWorld.getLength();
    }

    /**
     * @return The Schematic that houses all the info about what is stored in a schematic.
     */
    public Schematic getSchematic()
    {
        return schematicWorld;
    }
}
