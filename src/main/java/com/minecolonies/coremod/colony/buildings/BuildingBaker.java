package com.minecolonies.coremod.colony.buildings;

import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutBaker;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import com.minecolonies.coremod.entity.ai.citizen.baker.Product.ProductState;
import com.minecolonies.coremod.entity.ai.citizen.baker.Product;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
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
     * List of furnaces added to this building.
     */
    private final List<BlockPos> furnaces = new ArrayList<>();

    /**
     * Map of tasks for the baker to work on.
     */
    private final Map<ProductState, List<Product>> tasks = new EnumMap(ProductState.class);

    /**
     * Amounts of dough the Baker left in the oven.
     */
    private int breadsInOvens = 0;

    /**
     * Amount of dough the Baker prepared already.
     */
    private int preparedDough = 0;

    /**
     * Amounts of breads which are baked but need some final preparing.
     */
    private int bakedBreads = 0;

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
        furnaces.add(pos);
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
        return new ArrayList<>(furnaces);
    }

    /**
     * Getter for the breads in oven.
     *
     * @return the amount.
     */
    public int getBreadsInOvens()
    {
        return breadsInOvens;
    }

    /**
     * Setter for the breads in oven.
     *
     * @param breadsInOvens new amount.
     */
    public void setBreadsInOvens(final int breadsInOvens)
    {
        this.breadsInOvens = breadsInOvens;
    }

    /**
     * Getter for the prepared dough.
     *
     * @return the amount.
     */
    public int getPreparedDough()
    {
        return preparedDough;
    }

    /**
     * Setter for the prepared dough.
     *
     * @param preparedDough new amount.
     */
    public void setPreparedDough(final int preparedDough)
    {
        this.preparedDough = preparedDough;
    }

    /**
     * Getter for the baked breads.
     *
     * @return the amount.
     */
    public int getBakedBreads()
    {
        return bakedBreads;
    }

    /**
     * Setter for the bked breads.
     *
     * @param bakedBreads the new amount.
     */
    public void setBakedBreads(final int bakedBreads)
    {
        this.bakedBreads = bakedBreads;
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
            for(@NotNull final Product product: entry.getValue())
            {
                @NotNull final NBTTagCompound productCompound = new NBTTagCompound();
                product.writeToNBT(productCompound);
            }
            taskCompound.setTag(TAG_PRODUCTS, productsTaskList);
            tasksTagList.appendTag(taskCompound);
        }
        compound.setTag(TAG_TASKS, tasksTagList);
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
