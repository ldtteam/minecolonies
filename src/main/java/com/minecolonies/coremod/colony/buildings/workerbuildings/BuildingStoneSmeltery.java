package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;
import com.minecolonies.coremod.util.FurnaceRecipes;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutStoneSmelter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingSmelterCrafter;
import com.minecolonies.coremod.colony.jobs.JobStoneSmeltery;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlazedTerracottaBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the stone smeltery building.
 */
public class BuildingStoneSmeltery extends AbstractBuildingSmelterCrafter
{
    /**
     * Description string of the building.
     */
    private static final String STONE_SMELTERY = "stonesmeltery";

    /**
     * Instantiates a new stone smeltery building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingStoneSmeltery(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return STONE_SMELTERY;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobStoneSmeltery(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return STONE_SMELTERY;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Athletics;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Dexterity;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken token)
    {
        if (!super.canRecipeBeAdded(token))
        {
            return false;
        }

        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
        if (storage == null)
        {
            return false;
        }

        if (storage.getIntermediate() != Blocks.FURNACE || storage.getInput().isEmpty())
        {
            return false;
        }

        return isBlockForThisSmelter(storage.getPrimaryOutput()) && FurnaceRecipes.getInstance().getSmeltingResult(storage.getInput().get(0)).isItemEqual(storage.getPrimaryOutput());
    }

    /**
     * Method to check if the stack is craftable for the smeltery.
     *
     * @param stack the stack to craft.
     * @return true if so.
     */
    public boolean isBlockForThisSmelter(final ItemStack stack)
    {
        final Item item = stack.getItem();
        if (item instanceof BlockItem)
        {
            final Block block = ((BlockItem) item).getBlock();
            if (block.isIn(BlockTags.STONE_BRICKS) ||
                    block == Blocks.STONE         ||
                    block == Blocks.STONE_BRICKS  ||
                    block == Blocks.SMOOTH_STONE  ||
                    block instanceof GlazedTerracottaBlock)
            {

                return true;
            }
        }

        return item == Items.BRICK || item == Items.COAL || item == Items.CHARCOAL;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.stoneSmelter;
    }

    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        final UnlockBuildingResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect("Stonesmelter", UnlockBuildingResearchEffect.class);
        if (effect == null)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.havetounlock"));
            return;
        }
        super.requestUpgrade(player, builder);
    }

    /**
     * Stone smeltery View.
     */
    public static class View extends AbstractBuildingSmelterCrafter.View
    {

        /**
         * Instantiate the stone smeltery view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutStoneSmelter(this);
        }
    }
}
