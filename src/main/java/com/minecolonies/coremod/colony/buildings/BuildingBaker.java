package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutBaker;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import com.minecolonies.coremod.entity.ai.citizen.baker.BakerRecipes;
import com.minecolonies.coremod.entity.ai.citizen.baker.BakingProduct;
import com.minecolonies.coremod.entity.ai.citizen.baker.ProductState;
import com.minecolonies.coremod.entity.ai.util.RecipeStorage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Building for the baker.
 */
public class BuildingBaker extends AbstractBuildingWorker
{
    /**
     * General baker description key.
     */
    private static final String BAKER = "Baker";

    /**
     * Max hut level of the baker.
     */
    private static final int BAKER_HUT_MAX_LEVEL = 5;

    /**
     * Tag to retrieve the tasks hashmap.
     */
    private static final String TAG_TASKS = "tasks";

    /**
     * Tag to retrieve the state of an entry.
     */
    private static final String TAG_STATE = "state";

    /**
     * Tag to retrieve the products list.
     */
    private static final String TAG_PRODUCTS = "products";

    /**
     * Tag used to store the furnaces positions.
     */
    private static final String TAG_FURNACE_POS = "furnacePos";

    /**
     * Tag used to store the furnaces map.
     */
    private static final String TAG_FURNACES = "furnaces";

    /**
     * List of furnaces added to this building.
     */
    private final Map<BlockPos, BakingProduct> furnaces = new HashMap<>();

    /**
     * Map of tasks for the baker to work on.
     */
    private final Map<ProductState, List<BakingProduct>> tasks = new EnumMap(ProductState.class);

    /**
     * Wait this amount of ticks before checking again.
     */
    private static final int WAIT_TICKS = 320;

    /**
     * Always try to keep at least 2 stacks of wheat in the inventory and in the workers chest.
     */
    private static final int WHEAT_TO_KEEP = 128;

    /**
     * Ticks past since the last check.
     */
    private int ticksPassed = 0;


    /**
     * Constructor for the baker building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingBaker(final Colony c, final BlockPos l)
    {
        super(c, l);
        for(final RecipeStorage storage: BakerRecipes.getRecipes())
        {
            for(final ItemStack stack: storage.getInput())
            {
                keepX.put(stack::isItemEqual, WHEAT_TO_KEEP);
            }
        }
    }

    /**
     * Gets the name of the schematic.
     *
     * @return Baker schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return BAKER;
    }

    /**
     * Gets the max level of the baker's hut.
     *
     * @return The max level of the baker's hut.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return BAKER_HUT_MAX_LEVEL;
    }

    /**
     * The name of the baker's job.
     *
     * @return The name of the baker's job.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return BAKER;
    }

    /**
     * Create a Baker job.
     *
     * @param citizen the citizen to take the job.
     * @return The new Baker job.
     */
    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobBaker(citizen);
    }

    /**
     * Clear the furnaces list.
     */
    public void clearFurnaces()
    {
        furnaces.clear();
    }

    /**
     * Add a furnace to the building.
     *
     * @param pos the position of it.
     */
    public void addToFurnaces(final BlockPos pos)
    {
        furnaces.put(pos, null);
    }

    /**
     * Remove a furnace from the building.
     *
     * @param pos the position of it.
     */
    public void removeFromFurnaces(final BlockPos pos)
    {
        furnaces.remove(pos);
    }

    /**
     * Remove a product from the furnace.
     * @param pos the position the furnace is at.
     */
    public void removeProductFromFurnace(final BlockPos pos)
    {
        furnaces.replace(pos, null);
    }

    /**
     * Return a list of furnaces assigned to this hut.
     *
     * @return copy of the list
     */
    public List<BlockPos> getFurnaces()
    {
        return new ArrayList<>(furnaces.keySet());
    }

    /**
     * Return the map of furnaces assigned to this hut and the product in it.
     *
     * @return a hashmap with BlockPos, BakingProduct.
     */
    public Map<BlockPos, BakingProduct> getFurnacesWithProduct()
    {
        return Collections.unmodifiableMap(furnaces);
    }

    /**
     * Get the map of current tasks in the baker.
     *
     * @return the map of states and products.
     */
    public Map<ProductState, List<BakingProduct>> getTasks()
    {
        return Collections.unmodifiableMap(new HashMap<>(tasks));
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList tasksTagList = new NBTTagList();
        for (@NotNull final Map.Entry<ProductState, List<BakingProduct>> entry : tasks.entrySet())
        {
            if(!entry.getValue().isEmpty())
            {
                @NotNull final NBTTagCompound taskCompound = new NBTTagCompound();
                taskCompound.setInteger(TAG_STATE, entry.getKey().ordinal());

                @NotNull final NBTTagList productsTaskList = new NBTTagList();
                for (@NotNull final BakingProduct bakingProduct : entry.getValue())
                {
                    @NotNull final NBTTagCompound productCompound = new NBTTagCompound();
                    bakingProduct.writeToNBT(productCompound);
                }
                taskCompound.setTag(TAG_PRODUCTS, productsTaskList);
                tasksTagList.appendTag(taskCompound);
            }
        }
        compound.setTag(TAG_TASKS, tasksTagList);

        @NotNull final NBTTagList furnacesTagList = new NBTTagList();
        for (@NotNull final Map.Entry<BlockPos, BakingProduct> entry : furnaces.entrySet())
        {
            @NotNull final NBTTagCompound furnaceCompound = new NBTTagCompound();
            BlockPosUtil.writeToNBT(furnaceCompound, TAG_FURNACE_POS, entry.getKey());

            if(entry.getValue() != null)
            {
                entry.getValue().writeToNBT(furnaceCompound);
            }
            furnacesTagList.appendTag(furnaceCompound);
        }
        compound.setTag(TAG_FURNACES, furnacesTagList);
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        tasks.clear();
        super.readFromNBT(compound);
        final NBTTagList taskTagList = compound.getTagList(TAG_TASKS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < taskTagList.tagCount(); ++i)
        {
            final NBTTagCompound taskCompound = taskTagList.getCompoundTagAt(i);
            final ProductState state = ProductState.values()[taskCompound.getInteger(TAG_STATE)];
            final List<BakingProduct> bakingProducts = new ArrayList<>();

            final NBTTagList productTagList = taskCompound.getTagList(TAG_PRODUCTS, Constants.NBT.TAG_COMPOUND);
            for (int j = 0; j < productTagList.tagCount(); ++j)
            {
                final NBTTagCompound productCompound = taskTagList.getCompoundTagAt(i);
                final BakingProduct bakingProduct = BakingProduct.createFromNBT(productCompound);
                bakingProducts.add(bakingProduct);
            }

            tasks.put(state, bakingProducts);
        }

        final NBTTagList furnaceTagList = compound.getTagList(TAG_FURNACES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < furnaceTagList.tagCount(); ++i)
        {
            final NBTTagCompound furnaceCompound = furnaceTagList.getCompoundTagAt(i);
            final BlockPos pos = BlockPosUtil.readFromNBT(furnaceCompound, TAG_FURNACE_POS);
            final BakingProduct bakingProduct = BakingProduct.createFromNBT(furnaceCompound);
            furnaces.put(pos, bakingProduct);
        }
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos, @NotNull final World world)
    {
        super.registerBlockPosition(block, pos, world);
        if (block instanceof BlockFurnace && !furnaces.containsKey(pos))
        {
            addToFurnaces(pos);
        }
        markDirty();
    }

    /**
     * Add a task to the tasks list.
     *
     * @param state   the state of the task.
     * @param bakingProduct the regarding bakingProduct.
     */
    public void addToTasks(final ProductState state, final BakingProduct bakingProduct)
    {
        if (tasks.containsKey(state))
        {
            tasks.get(state).add(bakingProduct);
        }
        else
        {
            final List<BakingProduct> bakingProducts = new ArrayList<>();
            bakingProducts.add(bakingProduct);
            tasks.put(state, bakingProducts);
        }
        markDirty();
    }

    @Override
    public void onWorldTick(@NotNull final TickEvent.WorldTickEvent event)
    {
        super.onWorldTick(event);

        if(ticksPassed != WAIT_TICKS)
        {
            ticksPassed++;
            return;
        }
        ticksPassed = 0;


        checkFurnaces();
    }

    /**
     * Checks the furnaces of the baker if they're ready.
     * @param building the building they belong to.
     */
    private void checkFurnaces()
    {
        final World worldObj = getColony().getWorld();

        if(worldObj == null)
        {
            return;
        }

        final List<Map.Entry<BlockPos, BakingProduct>> copyOfList = new ArrayList<>(this.getFurnacesWithProduct().entrySet());
        for(final Map.Entry<BlockPos, BakingProduct> entry: copyOfList)
        {
            if(!worldObj.isBlockLoaded(entry.getKey()))
            {
                return;
            }
            final IBlockState furnace = worldObj.getBlockState(entry.getKey());
            if(!(furnace.getBlock() instanceof BlockFurnace))
            {
                if(worldObj.getTileEntity(entry.getKey()) instanceof TileEntityFurnace)
                {
                    return;
                }
                Log.getLogger().warn(getColony().getName() + " Removed furnace at: " + entry.getKey() + " because it went missing!");
                this.removeFromFurnaces(entry.getKey());
                continue;
            }

            final BakingProduct bakingProduct = entry.getValue();
            if(bakingProduct != null && bakingProduct.getState() == ProductState.BAKING)
            {
                bakingProduct.increaseBakingProgress();
                worldObj.setBlockState(entry.getKey(), Blocks.LIT_FURNACE.getDefaultState().withProperty(BlockFurnace.FACING, furnace.getValue(BlockFurnace.FACING)));
            }
            else
            {
                worldObj.setBlockState(entry.getKey(), Blocks.FURNACE.getDefaultState().withProperty(BlockFurnace.FACING, furnace.getValue(BlockFurnace.FACING)));
            }
        }
    }

    /**
     * Add a task to the tasks list.
     *
     * @param state   the state of the task.
     * @param bakingProduct the regarding bakingProduct.
     */
    public void removeFromTasks(final ProductState state, final BakingProduct bakingProduct)
    {
        if (tasks.containsKey(state))
        {
            tasks.get(state).remove(bakingProduct);
            if (tasks.get(state).isEmpty())
            {
                tasks.remove(state);
            }
            markDirty();
        }
    }

    /**
     * Put a certain BakingProduct in the furnace.
     *
     * @param currentFurnace the furnace to put it in.
     * @param bakingProduct        the BakingProduct.
     */
    public void putInFurnace(final BlockPos currentFurnace, final BakingProduct bakingProduct)
    {
        furnaces.replace(currentFurnace, bakingProduct);
    }

    /**
     * The client view for the baker building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * The client view constructor for the baker building.
         *
         * @param c The ColonyView the building is in.
         * @param l The location of the building.
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Creates a new window for the building.
         *
         * @return A BlockOut window.
         */
        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutBaker(this);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.INTELLIGENCE;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.DEXTERITY;
        }
    }
}
