package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
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
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Class of the crusher building.
 */
public class BuildingCrusher extends AbstractBuildingCrafter
{
    /**
     * The different modes the crusher can have.
     */
    public enum CrusherMode
    {
        COBBLESTONE,
        GRAVEL,
        SAND
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
     * Daily quantity to produce.
     */
    private int dailyQuantity = 0;

    /**
     * The current productionmode.
     */
    private CrusherMode crusherMode = CrusherMode.COBBLESTONE;

    /**
     * Instantiates a new crusher building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingCrusher(final Colony c, final BlockPos l)
    {
        super(c, l);
        if (super.recipes.isEmpty())
        {
            final List<ItemStack> cobbleInput = new ArrayList<>();
            cobbleInput.add(new ItemStack(Blocks.COBBLESTONE, 2));

            final List<ItemStack> gravelInput = new ArrayList<>();
            cobbleInput.add(new ItemStack(Blocks.GRAVEL, 2));

            final List<ItemStack> sandInput = new ArrayList<>();
            cobbleInput.add(new ItemStack(Blocks.SAND, 2));

            final IRecipeStorage cobbleCrushing = StandardFactoryController.getInstance().getNewInstance(
              TypeConstants.RECIPE,
              StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
              cobbleInput, 2, new ItemStack(Blocks.GRAVEL, 1), ModBlocks.blockHutCrusher);
            final IRecipeStorage gravelCrushing = StandardFactoryController.getInstance().getNewInstance(
              TypeConstants.RECIPE,
              StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
              gravelInput, 2, new ItemStack(Blocks.SAND, 1), ModBlocks.blockHutCrusher);
            final IRecipeStorage sandCrushing = StandardFactoryController.getInstance().getNewInstance(
              TypeConstants.RECIPE,
              StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
              sandInput, 2, new ItemStack(Blocks.CLAY, 1), ModBlocks.blockHutCrusher);

            final IToken token1 = ColonyManager.getRecipeManager().checkOrAddRecipe(cobbleCrushing);
            final IToken token2 = ColonyManager.getRecipeManager().checkOrAddRecipe(gravelCrushing);
            final IToken token3 = ColonyManager.getRecipeManager().checkOrAddRecipe(sandCrushing);

            addRecipe(token1);
            addRecipe(token2);
            addRecipe(token3);
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
     * @param crusherMode the new mode.
     * @param dailyQuantity the new quantity per dya.
     */
    public void setCrusherMode(final CrusherMode crusherMode, final int dailyQuantity)
    {
        this.crusherMode = crusherMode;
        this.dailyQuantity = dailyQuantity;
    }

    /**
     * Get the current crusher mode.
     * @return the mode and the quantity.
     */
    public Tuple<CrusherMode, Integer> getCrusherMode()
    {
        return new Tuple<>(crusherMode, dailyQuantity);
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
        private CrusherMode crusherMode = CrusherMode.COBBLESTONE;

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
         * @param crusherMode the new mode.
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
