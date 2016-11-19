package com.minecolonies.util;

import com.minecolonies.MineColonies;
import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.SaveScanMessage;
import com.structures.helpers.StructureProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for using the structure codebase
 *
 * @author Colton
 */
public final class StructureWrapper
{
    /**
     * The position we use as our uninitialized value.
     */
    private static final BlockPos                 NULL_POS            = new BlockPos(-1, -1, -1);

    /**
     * The Structure position we are at. Defaulted to NULL_POS.
     */
    private final        BlockPos.MutableBlockPos progressPos         = new BlockPos.MutableBlockPos(-1, -1, -1);
    /**
     * The minecraft world this struture is displayed in.
     */
    private World          world;
    /**
     * The structure this structure comes from.
     */
    private StructureProxy structure;
    /**
     * The anchor position this structure will be
     * placed on in the minecraft world.
     */
    private BlockPos       position;
    /**
     * The name this structure has.
     */
    private String         name;

    /**
     * Create a new StructureProxy.
     *
     * @param worldObj       the world to show it in
     * @param structure the structure it comes from
     * @param name           the name this structure has
     */
    private StructureWrapper(World worldObj, StructureProxy structure, String name)
    {
        world = worldObj;
        this.structure = structure;
        this.name = name;
    }

    /**
     * Load a structure into this world.
     *
     * @param worldObj the world to load in
     * @param name     the structure name
     */
    public StructureWrapper(World worldObj, String name)
    {
        this(worldObj, new StructureProxy(worldObj, name), name);
    }

    /**
     * Generate a resource location from a structures name.
     *
     * @param name the structures name
     * @return the resource location pointing towards the structure
     */
    @NotNull
    private static ResourceLocation getResourceLocation(@NotNull String name)
    {
        return new ResourceLocation("minecolonies:schematics/" + name + ".nbt");
    }

    /**
     * Generate the stream from a resource location.
     *
     * @param res the location to pull the stream from
     * @return a stream from this location
     */
    public static InputStream getStream(@NotNull ResourceLocation res)
    {
        try
        {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            {
                return Minecraft.getMinecraft().getResourceManager().getResource(res).getInputStream();
            }
            else
            {
                return StructureWrapper.class.getResourceAsStream(String.format("/assets/%s/%s", res.getResourceDomain(), res.getResourcePath()));
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Could not load stream!", e);
        }
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj  the world to load it in
     * @param name      the structures name
     * @param pos       coordinates
     * @param rotations number of times rotated
     */
    public static void loadAndPlaceStructureWithRotation(World worldObj, @NotNull String name, @NotNull BlockPos pos, int rotations)
    {
        try
        {
            @NotNull StructureWrapper structureWrapper = new StructureWrapper(worldObj, name);
            structureWrapper.rotate(rotations);
            structureWrapper.placeStructure(pos);
        }
        catch (IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
    }

    /**
     * Place a structure into the world.
     *
     * @param pos coordinates
     */
    private void placeStructure(@NotNull BlockPos pos)
    {
        setLocalPosition(pos);

        @NotNull List<BlockPos> delayedBlocks = new ArrayList<>();

            //structure.getBlockInfo()[0].pos

        for (int j = 0; j < structure.getHeight(); j++)
        {
            for (int k = 0; k < structure.getLength(); k++)
            {
                for (int i = 0; i < structure.getWidth(); i++)
                {
                    @NotNull BlockPos localPos = new BlockPos(i, j, k);
                    IBlockState localState = this.structure.getBlockState(localPos);
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
                    world.setTileEntity(worldPos, structure.getTileEntity(localPos));
                }
            }
        }

        for (@NotNull BlockPos coords : delayedBlocks)
        {
            IBlockState localState = this.structure.getBlockState(coords);
            Block localBlock = localState.getBlock();
            BlockPos newWorldPos = pos.add(coords);

            placeBlock(localState, localBlock, newWorldPos);
        }
    }

    /**
     * Rotates the structure x times.
     * @param times times to rotate.
     */
    public void rotate(int times)
    {
        structure.rotate(times);
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
     * Scan the structure and save it to the disk.
     *
     * @param world Current world.
     * @param from  First corner.
     * @param to    Second corner.
     * @param player causing this action.
     */
    public static void saveStructure(@Nullable World world, @Nullable BlockPos from, @Nullable BlockPos to, @NotNull EntityPlayer player)
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

        String currentMillis = Long.toString(System.currentTimeMillis());
        String fileName = "/minecolonies/scans/" + LanguageHandler.format("item.scepterSteel.scanFormat", "", currentMillis + ".nbt");

        Template template = templatemanager.getTemplate(minecraftserver, new ResourceLocation(fileName));
        template.takeBlocksFromWorld(world, blockpos, size, true, Blocks.STRUCTURE_VOID);
        template.setAuthor(Constants.MOD_ID);

        MineColonies.getNetwork().sendTo(new SaveScanMessage(template.writeToNBT(new NBTTagCompound()), fileName), (EntityPlayerMP) player);
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
        while (doesStructureBlockEqualWorldBlock() && count < Configurations.maxBlocksCheckedByBuilder);

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
        if (this.progressPos.getX() == structure.getWidth())
        {
            this.progressPos.setPos(0, this.progressPos.getY(), this.progressPos.getZ() + 1);
            if (this.progressPos.getZ() == structure.getLength())
            {
                this.progressPos.setPos(this.progressPos.getX(), this.progressPos.getY() + 1, 0);
                if (this.progressPos.getY() == structure.getHeight())
                {
                    reset();
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks if the block in the world is the same as what is in the structure.
     *
     * @return true if the structure block equals the world block.
     */
    public boolean doesStructureBlockEqualWorldBlock()
    {
        IBlockState structureBlockState = structure.getBlockState(this.getLocalPosition());
        Block structureBlock = structureBlockState.getBlock();

        //All worldBlocks are equal the substitution block
        if (structureBlock == ModBlocks.blockSubstitution)
        {
            return true;
        }

        BlockPos worldPos = this.getBlockPosition();

        IBlockState worldBlockState = world.getBlockState(worldPos);

        //list of things to only check block for.
        //For the time being any flower pot is equal to each other.
        if (structureBlock instanceof BlockDoor || structureBlock == Blocks.FLOWER_POT)
        {
            return structureBlock == worldBlockState.getBlock();
        }
        else if (structureBlock instanceof BlockStairs && structureBlockState == worldBlockState)
        {
            return true;
        }

        //had this problem in a super flat world, causes builder to sit doing nothing because placement failed
        return worldPos.getY() <= 0
                 || structureBlockState == worldBlockState;
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
     * Change the current progressPos. Used when loading progress.
     *
     * @param localPosition new progressPos.
     */
    public void setLocalPosition(@NotNull BlockPos localPosition)
    {
        BlockPosUtil.set(this.progressPos, localPosition);
    }

    /**
     * @return World position.
     */
    public BlockPos getBlockPosition()
    {
        return this.progressPos.add(getOffsetPosition());
    }

    /**
     * @return Min world position for the structure.
     */
    public BlockPos getOffsetPosition()
    {
        return position.subtract(getOffset());
    }

    /**
     * @return Where the hut (or any offset) is in the structure.
     */
    public BlockPos getOffset()
    {
        return structure.getOffset();
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
        //Check for air blocks and if blocks below the hut are different from the structure
        while ((worldBlockAir() || doesStructureBlockEqualWorldBlock()) && count < Configurations.maxBlocksCheckedByBuilder);

        return true;
    }

    private boolean isAirBlock()
    {
        return getBlock() == Blocks.AIR;
    }

    /**
     * Calculate the current block in the structure
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
     * Creates the scan directories for the scanTool.
     * @param world the worldIn.
     */
    public static void createScanDirectory(@NotNull World world)
    {
        File minecolonies;
        if (world.isRemote)
        {
            minecolonies = new File(Minecraft.getMinecraft().mcDataDir, "minecolonies/");
        }
        else
        {
            MinecraftServer server = world.getMinecraftServer();
            if(server != null)
            {
                minecolonies = server.getFile("minecolonies/");
            }
            else
            {
                return;
            }
        }
        checkDirectory(minecolonies);

        @NotNull File scans = new File(minecolonies, "scans/");
        checkDirectory(scans);
    }

    /**
     * Checks if directory exists, else creates it.
     * @param directory the directory to check.
     */
    private static void checkDirectory(@NotNull File directory)
    {
        if (!directory.exists() && !directory.mkdirs())
        {
            Log.getLogger().error("Directory doesn't exist and failed to be created: " + directory.toString());
        }
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
        return this.structure.getBlockState(this.progressPos);
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
        while ((doesStructureBlockEqualWorldBlock() || isBlockNonSolid()) && count < Configurations.maxBlocksCheckedByBuilder);

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
        while ((doesStructureBlockEqualWorldBlock() || isBlockSolid()) && count < Configurations.maxBlocksCheckedByBuilder);

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
            this.progressPos.setPos(structure.getWidth(), structure.getHeight() - 1, structure.getLength() - 1);
        }

        this.progressPos.setPos(this.progressPos.getX() - 1, this.progressPos.getY(), this.progressPos.getZ());
        if (this.progressPos.getX() == -1)
        {
            this.progressPos.setPos(structure.getWidth() - 1, this.progressPos.getY(), this.progressPos.getZ() - 1);
            if (this.progressPos.getZ() == -1)
            {
                this.progressPos.setPos(this.progressPos.getX(), this.progressPos.getY() - 1, structure.getLength() - 1);
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
        return this.structure.getTileEntity(this.progressPos);
    }

    /**
     * @return A list of all the entities in the structure.
     */
    @NotNull
    public List<Entity> getEntities()
    {
        return structure.getEntities();
    }

    /**
     * Base position of the structure.
     *
     * @return BlockPos representing where the structure is.
     */
    public BlockPos getPosition()
    {
        if(position == null)
        {
            return new BlockPos(0,0,0);
        }
        return position;
    }

    /**
     * Set the position, used when loading.
     *
     * @param position Where the structure is in the world.
     */
    public void setPosition(BlockPos position)
    {
        this.position = position;
    }

    /**
     * Calculate the item needed to place the current block in the structure
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
     * @return The name of the structure.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return The height of the structure.
     */
    public int getHeight()
    {
        return structure.getHeight();
    }

    /**
     * @return The width of the structure.
     */
    public int getWidth()
    {
        return structure.getWidth();
    }

    /**
     * @return The length of the structure.
     */
    public int getLength()
    {
        return structure.getLength();
    }

    /**
     * @return The StructureProxy that houses all the info about what is stored in a structure.
     */
    public StructureProxy structure()
    {
        return structure;
    }
}
