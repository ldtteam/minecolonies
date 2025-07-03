package com.minecolonies.core.entity.ai.workers.crafting;

import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.util.StatsUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBaker;
import com.minecolonies.core.colony.jobs.JobBaker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.StatisticsConstants.ITEMS_CRAFTED_DETAIL;
import static com.minecolonies.api.util.constant.StatisticsConstants.ITEMS_BAKED_DETAIL;
/**
 * Baker AI class.
 */
public class EntityAIWorkBaker extends AbstractEntityAIRequestSmelter<JobBaker, BuildingBaker>
{
    /**
     * Baking icon
     */
    private final static VisibleCitizenStatus BAKING =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/baker.png"), "com.minecolonies.gui.visiblestatus.baker");

    /**
     * Constructor for the Baker. Defines the tasks the bakery executes.
     *
     * @param job a bakery job to use.
     */
    public EntityAIWorkBaker(@NotNull final JobBaker job)
    {
        super(job);
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingBaker> getExpectedBuildingClass()
    {
        return BuildingBaker.class;
    }

    /**
     * Returns the bakery's worker instance. Called from outside this class.
     *
     * @return citizen object.
     */
    @Nullable
    public AbstractEntityCitizen getCitizen()
    {
        return worker;
    }

    @Override
    protected IAIState craft()
    {
        worker.getCitizenData().setVisibleStatus(BAKING);
        return super.craft();
    }

    @Override
    public boolean isAfterDumpPickupAllowed()
    {
        return true;
    }

    /**
     * Records the crafting request in the building's statistics.
     * @param request the request to record.
     */
    @Override
    public void recordCraftingBuildingStats(IRequest<?> request, IRecipeStorage recipe)
    {
        if (recipe == null) 
        {
            return;
        }

        StatsUtil.trackStatByName(building, ITEMS_CRAFTED_DETAIL, recipe.getPrimaryOutput().getDescriptionId(), recipe.getPrimaryOutput().getCount());
    }

    /**
     * Records the smelting request in the building's statistics.
     *
     * @param cookedStack the item stack that has been smelted.
     */
    @Override
    protected void recordSmeltingBuildingStats(ItemStack cookedStack)
    {
        if (cookedStack == null) 
        {
            return;
        }
        
        StatsUtil.trackStatByName(building, ITEMS_BAKED_DETAIL, cookedStack.getDescriptionId(),cookedStack.getCount());
    }

}
