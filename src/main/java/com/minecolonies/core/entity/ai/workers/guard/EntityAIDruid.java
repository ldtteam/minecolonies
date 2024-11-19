package com.minecolonies.core.entity.ai.workers.guard;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.jobs.JobDruid;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.entity.pathfinding.navigation.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobWalkRandomEdge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.IDLE;
import static com.minecolonies.api.research.util.ResearchConstants.DRUID_USE_POTIONS;

/**
 * Druid AI class, which deals with equipment and movement specifics
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIDruid extends AbstractEntityAIGuard<JobDruid, AbstractBuildingGuards>
{
    /**
     * Potion meta data.
     */
    public static final String RENDER_META_POTION = "potion";

    public EntityAIDruid(@NotNull final JobDruid job)
    {
        super(job);
        new DruidCombatAI((EntityCitizen) worker, getStateAI(), this);
    }

    @Override
    protected void updateRenderMetaData()
    {
        String renderMeta = getState() == IDLE ? "" : RENDER_META_WORKING;
        if (worker.getCitizenInventoryHandler().hasItemInInventory(Items.POTION))
        {
            renderMeta += RENDER_META_POTION;
        }
        worker.setRenderMetadata(renderMeta);
    }

    @Override
    protected void atBuildingActions()
    {
        super.atBuildingActions();

        if (worker.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(DRUID_USE_POTIONS) > 0)
        {
            // Mistletoes and water bottles
            InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(building,
              item -> item.getItem() == ModItems.magicpotion,
              32,
              worker.getInventoryCitizen());

            if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), item -> item.getItem() == ModItems.magicpotion) < 8)
            {
                checkIfRequestForItemExistOrCreateAsync(new ItemStack(ModItems.magicpotion), 16, 8);
            }
        }
    }

    @Override
    public void guardMovement()
    {
        if (worker.getRandom().nextInt(3) < 1)
        {
            worker.isWorkerAtSiteWithMove(buildingGuards.getGuardPos(), 3);
            return;
        }

        if (worker.isWorkerAtSiteWithMove(buildingGuards.getGuardPos(), 10) || Math.abs(buildingGuards.getGuardPos().getY() - worker.blockPosition().getY()) > 3)
        {
            // Moves the druid randomly to close edges, for better vision to mobs
            ((MinecoloniesAdvancedPathNavigate) worker.getNavigation()).setPathJob(new PathJobWalkRandomEdge(world, buildingGuards.getGuardPos(), 20, worker),
              null,
              1.0, true);
        }
    }
}
