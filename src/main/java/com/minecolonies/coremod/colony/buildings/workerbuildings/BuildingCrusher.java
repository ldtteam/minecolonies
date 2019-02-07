package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowHutCrusher;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobCrusher;
import com.minecolonies.coremod.network.messages.CrusherSetModeMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Class of the crusher building.
 */
public class BuildingCrusher extends AbstractBuildingCrafter
{
    /**
     * The multiplier to define the max craft per day.
     */
    private static final double BUILDING_LEVEL_MULTIPLIER = 16;

    /**
     * The different modes the crusher can have.
     */
    public enum CrusherMode
    {
        GRAVEL,
        SAND,
        CLAY
    }

    /**
     * The crusher string.
     */
    private static final String CRUSHER_DESC = "Crusher";

    /**
     * Max building level of the crusher.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * The cobble input list.
     */
    private static final List<ItemStack> cobbleInput = new ArrayList<>();

    /**
     * The gravel input list.
     */
    private static final List<ItemStack> gravelInput = new ArrayList<>();

    /**
     * The sand input list.
     */
    private static final List<ItemStack> sandInput = new ArrayList<>();
    /*
     * Fill the input lists.
     */
    static
    {
        cobbleInput.add(new ItemStack(Blocks.COBBLESTONE, 2));
        gravelInput.add(new ItemStack(Blocks.GRAVEL, 2));
        sandInput.add(new ItemStack(Blocks.SAND, 2));
    }

    /**
     * The cobble crushing recipe.
     */
    private static final IRecipeStorage cobbleCrushing = StandardFactoryController.getInstance().getNewInstance(
      TypeConstants.RECIPE,
      StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
      cobbleInput, 2, new ItemStack(Blocks.GRAVEL, 1), ModBlocks.blockHutCrusher);

    /**
     * The gravel crushing recipe.
     */
    private static final IRecipeStorage gravelCrushing = StandardFactoryController.getInstance().getNewInstance(
      TypeConstants.RECIPE,
      StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
      gravelInput, 2, new ItemStack(Blocks.SAND, 1), ModBlocks.blockHutCrusher);

    /**
     * The sand crushing recipe.
     */
    private static final IRecipeStorage sandCrushing = StandardFactoryController.getInstance().getNewInstance(
      TypeConstants.RECIPE,
      StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
      sandInput, 2, new ItemStack(Blocks.CLAY, 1), ModBlocks.blockHutCrusher);

    /**
     * Daily quantity to produce.
     */
    private int dailyQuantity = 0;

    /**
     * The current daily quantity.
     */
    private int currentDailyQuantity = 0;

    /**
     * The current productionmode.
     */
    private CrusherMode crusherMode = CrusherMode.GRAVEL;

    /**
     * Instantiates a new crusher building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCrusher(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Get the recipe storage of the current mode.
     *
     * @return the storage.
     */
    public IRecipeStorage getCurrentRecipe()
    {
        switch (crusherMode)
        {
            case SAND:
                return gravelCrushing;
            case CLAY:
                return sandCrushing;
            default:
                return cobbleCrushing;
        }
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return CRUSHER_DESC;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobCrusher(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return CRUSHER_DESC;
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return false;
    }

    /**
     * The the current crusher mode with a certain quantity.
     *
     * @param crusherMode   the new mode.
     * @param dailyQuantity the new quantity per dya.
     */
    public void setCrusherMode(final CrusherMode crusherMode, final int dailyQuantity)
    {
        this.crusherMode = crusherMode;
        this.dailyQuantity = dailyQuantity;
    }

    /**
     * Get the current crusher mode.
     *
     * @return the mode and the quantity.
     */
    public Tuple<CrusherMode, Integer> getCrusherMode()
    {
        return new Tuple<>(crusherMode, dailyQuantity);
    }

    /**
     * Set the current daily quantity.
     *
     * @param currentDailyQuantity the current quantity.
     */
    public void setCurrentDailyQuantity(final int currentDailyQuantity)
    {
        this.currentDailyQuantity = currentDailyQuantity;
    }

    /**
     * Calculate the max quantity to be crafted per day.
     *
     * @return the max.
     */
    public int getMaxDailyQuantity()
    {
        if (getBuildingLevel() >= MAX_BUILDING_LEVEL)
        {
            return Integer.MAX_VALUE;
        }

        return (int) (Math.pow(getBuildingLevel(), 2) * BUILDING_LEVEL_MULTIPLIER);
    }

    @Override
    public void onWakeUp()
    {
        super.onWakeUp();
        this.currentDailyQuantity = 0;
    }

    /**
     * Get the current daily quantity.
     *
     * @return the quantity.
     */
    public int getCurrentDailyQuantity()
    {
        return currentDailyQuantity;
    }

    @Override
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.dailyQuantity = compound.getInteger(TAG_DAILY);
        this.currentDailyQuantity = compound.getInteger(TAG_CURRENT_DAILY);
        this.crusherMode = CrusherMode.values()[compound.getInteger(TAG_MODE)];

        if (super.recipes.isEmpty())
        {
            final IToken token1 = ColonyManager.getRecipeManager().checkOrAddRecipe(cobbleCrushing);
            final IToken token2 = ColonyManager.getRecipeManager().checkOrAddRecipe(gravelCrushing);
            final IToken token3 = ColonyManager.getRecipeManager().checkOrAddRecipe(sandCrushing);

            addRecipe(token1);
            addRecipe(token2);
            addRecipe(token3);
        }
    }

    @Override
    public void writeToNBT(@NotNull final NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger(TAG_DAILY, dailyQuantity);
        compound.setInteger(TAG_CURRENT_DAILY, currentDailyQuantity);
        compound.setInteger(TAG_MODE, crusherMode.ordinal());
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        buf.writeInt(crusherMode.ordinal());
        buf.writeInt(dailyQuantity);
    }

    /**
     * BuildingCrusher View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {
        /**
         * Daily quantity to produce.
         */
        private int dailyQuantity = 0;

        /**
         * The current production mode.
         */
        private CrusherMode crusherMode = CrusherMode.GRAVEL;

        /**
         * Instantiate the crusher view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
            crusherMode = CrusherMode.values()[buf.readInt()];
            dailyQuantity = buf.readInt();
        }

        /**
         * The the current crusher mode with a certain quantity.
         *
         * @param crusherMode   the new mode.
         * @param dailyQuantity the new quantity per dya.
         */
        public void setCrusherMode(final CrusherMode crusherMode, final int dailyQuantity)
        {
            this.crusherMode = crusherMode;
            this.dailyQuantity = dailyQuantity;
            MineColonies.getNetwork().sendToServer(new CrusherSetModeMessage(this, crusherMode.ordinal(), dailyQuantity));
        }

        /**
         * Get the current crusher mode.
         *
         * @return the mode and the quantity.
         */
        public Tuple<CrusherMode, Integer> getCrusherMode()
        {
            return new Tuple<>(crusherMode, dailyQuantity);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutCrusher(this);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.STRENGTH;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.STRENGTH;
        }
    }
}
