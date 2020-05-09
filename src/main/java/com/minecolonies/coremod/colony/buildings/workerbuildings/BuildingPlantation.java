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
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.JobPlanter;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the plantation building.
 */
public class BuildingPlantation extends AbstractBuildingCrafter
{
    /**
     * Description string of the building.
     */
    private static final String PLANTATION = "plantation";

    /**
     * Instantiates a new plantation building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingPlantation(final IColony c, final BlockPos l)
    {
        super(c, l);
    }


    @NotNull
    @Override
    public String getSchematicName()
    {
        return PLANTATION;
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
        return new JobPlanter(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return PLANTATION;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Agility;
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
        if(!super.canRecipeBeAdded(token))
        {
            return false;
        }

        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
        if(storage == null)
        {
            return false;
        }

        return storage.getPrimaryOutput().getItem() == Items.BOOK || storage.getPrimaryOutput().getItem() == Items.PAPER;
    }

    //todo add research to tree

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.plantation;
    }

    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        final UnlockBuildingResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect("Plantation", UnlockBuildingResearchEffect.class);
        if (effect == null)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.havetounlock"));
            return;
        }
        super.requestUpgrade(player, builder);
    }

    /**
     * Plantation View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {

        /**
         * Instantiate the plantation view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        //todo add gui to select one of tree modes
        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, PLANTATION);
        }
    }
}
