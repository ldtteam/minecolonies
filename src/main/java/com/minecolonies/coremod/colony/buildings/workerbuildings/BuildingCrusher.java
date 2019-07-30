package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.constant.TypeConstants;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.client.gui.WindowHutCrusher;
import com.minecolonies.coremod.colony.*;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.IJob;
import com.minecolonies.coremod.colony.jobs.JobCrusher;
import com.minecolonies.coremod.network.messages.CrusherSetModeMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.model.animation.IJoint;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * The crusher string.
     */
    private static final String CRUSHER_DESC = "Crusher";

    /**
     * Max building level of the crusher.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * His crusherRecipes.
     */
    private final Map<ItemStorage, IRecipeStorage> crusherRecipes = new HashMap<>();

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
    private ItemStorage crusherMode = null;

    /**
     * Instantiates a new crusher building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCrusher(final Colony c, final BlockPos l)
    {
        super(c, l);
        loadCrusherMode();
    }

    /**
     * Load the crusher settings.
     */
    private void loadCrusherMode()
    {
        for (final Map.Entry<ItemStorage, ItemStorage> mode : IColonyManager.getInstance().getCompatibilityManager().getCrusherModes().entrySet())
        {
            if (this.crusherMode == null)
            {
                this.crusherMode = mode.getKey();
            }
            final List<ItemStack> input = new ArrayList<>();
            input.add(mode.getKey().getItemStack());
            final IRecipeStorage recipe = StandardFactoryController.getInstance().getNewInstance(
              TypeConstants.RECIPE,
              StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
              input, 2, mode.getValue().getItemStack(), ModBlocks.blockHutCrusher);
            crusherRecipes.put(mode.getKey(), recipe);
        }
    }

    /**
     * Get the recipe storage of the current mode.
     * @return the storage.
     */
    public IRecipeStorage getCurrentRecipe()
    {
        return this.crusherRecipes.get(this.crusherMode);
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
    public IJob createJob(final ICitizenData citizen)
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
    public void setCrusherMode(final ItemStorage crusherMode, final int dailyQuantity)
    {
        this.crusherMode = crusherMode;
        this.dailyQuantity = dailyQuantity;
    }

    /**
     * Get the current crusher mode.
     *
     * @return the mode and the quantity.
     */
    public Tuple<ItemStorage, Integer> getCrusherMode()
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
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        super.readFromNBT(compound);
        this.dailyQuantity = compound.getInt(TAG_DAILY);
        this.currentDailyQuantity = compound.getInt(TAG_CURRENT_DAILY);

        if (compound.keySet().contains(TAG_CRUSHER_MODE))
        {
            this.crusherMode = new ItemStorage(new ItemStack(compound.getCompound(TAG_CRUSHER_MODE)));
        }

        if (super.recipes.isEmpty())
        {
            for (final IRecipeStorage recipe : crusherRecipes.values())
            {
                final IToken token = IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(recipe);
                addRecipe(token);
            }
        }
    }

    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        super.write(compound);
        compound.putInt(TAG_DAILY, dailyQuantity);
        compound.putInt(TAG_CURRENT_DAILY, currentDailyQuantity);
        if (crusherMode != null)
        {
            final CompoundNBT crusherModeNBT = new CompoundNBT();
            crusherMode.getItemStack().write(crusherModeNBT);
            compound.put(TAG_CRUSHER_MODE, crusherModeNBT);
        }
        return compound;
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
        if (crusherRecipes.isEmpty())
        {
            loadCrusherMode();
        }

        if (crusherMode == null)
        {
            buf.writeBoolean(false);
        }
        else
        {
            buf.writeBoolean(true);
            buf.writeItemStack(crusherMode.getItemStack());
        }
        buf.writeInt(dailyQuantity);

        buf.writeInt(crusherRecipes.size());
        for (final ItemStorage storage : crusherRecipes.keySet())
        {
            buf.writeItemStack(storage.getItemStack());
        }
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
        private ItemStorage crusherMode;

        /**
         * The current production mode.
         */
        private final List<ItemStorage> crusherModes = new ArrayList<>();

        /**
         * Instantiate the crusher view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);

            if (buf.readBoolean())
            {
                crusherMode = new ItemStorage(buf.readItemStack());
            }
            dailyQuantity = buf.readInt();
            crusherModes.clear();

            final int size = buf.readInt();
            for (int i = 0; i < size; i++)
            {
                crusherModes.add(new ItemStorage(buf.readItemStack()));
            }
        }

        /**
         * The the current crusher mode with a certain quantity.
         *
         * @param crusherMode   the new mode.
         * @param dailyQuantity the new quantity per dya.
         */
        public void setCrusherMode(final ItemStorage crusherMode, final int dailyQuantity)
        {
            this.crusherMode = crusherMode;
            this.dailyQuantity = dailyQuantity;
            Network.getNetwork().sendToServer(new CrusherSetModeMessage(this, crusherMode, dailyQuantity));
        }

        /**
         * Get the current crusher mode.
         *
         * @return the mode and the quantity.
         */
        public Tuple<ItemStorage, Integer> getCrusherMode()
        {
            return new Tuple<>(crusherMode, dailyQuantity);
        }

        /**
         * Get all the possible crusher modes.
         * @return the modes.
         */
        public List<ItemStorage> getCrusherModes()
        {
            return this.crusherModes;
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
            return Skill.ENDURANCE;
        }
    }
}
