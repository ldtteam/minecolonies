package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.client.gui.WindowHutGlassblower;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingSmelterCrafter;
import com.minecolonies.coremod.colony.jobs.JobGlassblower;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the glassblower building.
 */
public class BuildingGlassblower extends AbstractBuildingSmelterCrafter
{
    /**
     * Description string of the building.
     */
    private static final String GLASS_BLOWER = "glassblower";

    /**
     * Instantiates a new stone smeltery building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingGlassblower(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return GLASS_BLOWER;
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
        return new JobGlassblower(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return GLASS_BLOWER;
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

    //todo allow any recipe for glass
    //todo add some way to accept all sand -> glass recipes.

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
            return block.getDefaultState().getMaterial() == Material.GLASS;
        }

        return false;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.glassblower;
    }

    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        super.requestUpgrade(player, builder);
        //todo change.
        if (true)
        {
            return;
        }
        final UnlockBuildingResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect("Glassblower", UnlockBuildingResearchEffect.class);
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
            return new WindowHutGlassblower(this);
        }
    }
}
