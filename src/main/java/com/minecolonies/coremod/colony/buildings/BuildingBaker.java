package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutBaker;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.citizen.baker.Product.ProductState;
import com.minecolonies.coremod.entity.ai.citizen.baker.Product;
import com.minecolonies.coremod.util.BlockPosUtil;
import com.minecolonies.coremod.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
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
    private final Map<BlockPos, Product> furnaces = new HashMap<>();

    /**
     * Map of tasks for the baker to work on.
     */
    private final Map<ProductState, List<Product>> tasks = new EnumMap(ProductState.class);

    /**
     * Constructor for the baker building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingBaker(final Colony c, final BlockPos l)
    {
        super(c, l);
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
     * @return a hashmap with BlockPos, Product.
     */
    public Map<BlockPos, Product> getFurnacesWithProduct()
    {
        return new HashMap<>(furnaces);
    }

    /**
     * Get the map of current tasks in the baker.
     *
     * @return the map of states and products.
     */
    public Map<ProductState, List<Product>> getTasks()
    {
        return new HashMap<>(tasks);
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        @NotNull final NBTTagList tasksTagList = new NBTTagList();
        for (@NotNull final Map.Entry<ProductState, List<Product>> entry : tasks.entrySet())
        {
            @NotNull final NBTTagCompound taskCompound = new NBTTagCompound();
            taskCompound.setInteger(TAG_STATE, entry.getKey().ordinal());

            @NotNull final NBTTagList productsTaskList = new NBTTagList();
            for (@NotNull final Product product : entry.getValue())
            {
                @NotNull final NBTTagCompound productCompound = new NBTTagCompound();
                product.writeToNBT(productCompound);
            }
            taskCompound.setTag(TAG_PRODUCTS, productsTaskList);
            tasksTagList.appendTag(taskCompound);
        }
        compound.setTag(TAG_TASKS, tasksTagList);

        @NotNull final NBTTagList furnacesTagList = new NBTTagList();
        for (@NotNull final Map.Entry<BlockPos, Product> entry : furnaces.entrySet())
        {
            @NotNull final NBTTagCompound furnaceCompound = new NBTTagCompound();
            BlockPosUtil.writeToNBT(furnaceCompound, TAG_FURNACE_POS, entry.getKey());
            entry.getValue().writeToNBT(furnaceCompound);

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
            final List<Product> products = new ArrayList<>();

            final NBTTagList productTagList = taskCompound.getTagList(TAG_PRODUCTS, Constants.NBT.TAG_COMPOUND);
            for (int j = 0; j < productTagList.tagCount(); ++j)
            {
                final NBTTagCompound productCompound = taskTagList.getCompoundTagAt(i);
                final Product product = Product.createFromNBT(productCompound);
                products.add(product);
            }

            tasks.put(state, products);
        }

        final NBTTagList furnaceTagList = compound.getTagList(TAG_FURNACES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < furnaceTagList.tagCount(); ++i)
        {
            final NBTTagCompound furnaceCompound = taskTagList.getCompoundTagAt(i);
            final BlockPos pos = BlockPosUtil.readFromNBT(furnaceCompound, TAG_FURNACE_POS);
            final Product product = Product.createFromNBT(furnaceCompound);
            furnaces.put(pos, product);
        }
    }

    @Override
    public void registerBlockPosition(@NotNull final Block block, @NotNull final BlockPos pos)
    {
        super.registerBlockPosition(block, pos);
        if (block instanceof BlockFurnace && !furnaces.containsKey(pos))
        {
            addToFurnaces(pos);
        }
    }

    /**
     * Add a task to the tasks list.
     *
     * @param state   the state of the task.
     * @param product the regarding product.
     */
    public void addToTasks(final ProductState state, final Product product)
    {
        if (tasks.containsKey(state))
        {
            tasks.get(state).add(product);
        }
        else
        {
            final List<Product> products = new ArrayList<>();
            products.add(product);
            tasks.put(state, products);
        }
    }

    /**
     * Add a task to the tasks list.
     *
     * @param state   the state of the task.
     * @param product the regarding product.
     */
    public void removeFromTasks(final ProductState state, final Product product)
    {
        if (tasks.containsKey(state))
        {
            tasks.get(state).remove(product);
            if (tasks.get(state).size() == 0)
            {
                tasks.remove(state);
            }
        }
    }

    /**
     * Put a certain Product in the furnace.
     *
     * @param currentFurnace the furnace to put it in.
     * @param product        the Product.
     */
    public void putInFurnace(final BlockPos currentFurnace, final Product product)
    {
        furnaces.replace(currentFurnace, product);
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
            return Skill.DEXTERITY;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.CHARISMA;
        }
    }
}
