package com.minecolonies.coremod.entity.ai.citizen.guard;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.jobs.JobDruid;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import com.minecolonies.coremod.entity.pathfinding.pathjobs.PathJobWalkRandomEdge;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.research.util.ResearchConstants.ARCHER_USE_ARROWS;
import static com.minecolonies.api.research.util.ResearchConstants.DRUID_USE_POTIONS;

/**
 * Druid AI class, which deals with equipment and movement specifics
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class EntityAIDruid extends AbstractEntityAIGuard<JobDruid, AbstractBuildingGuards>
{
    public EntityAIDruid(@NotNull final JobDruid job)
    {
        super(job);
        new DruidCombatAI((EntityCitizen) worker, getStateAI(), this);
    }

    @Override
    protected void atBuildingActions()
    {
        super.atBuildingActions();

        if (worker.getCitizenColonyHandler().getColony().getResearchManager().getResearchEffects().getEffectStrength(DRUID_USE_POTIONS) > 0)
        {
            // Mistletoes and water bottles
            InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(getOwnBuilding(),
              item -> item.getItem() instanceof PotionItem,
              32,
              worker.getInventoryCitizen());

            InventoryUtils.transferXOfFirstSlotInProviderWithIntoNextFreeSlotInItemHandler(getOwnBuilding(),
              item -> item.getItem() == ModItems.mistletoe,
              32,
              worker.getInventoryCitizen());

            if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), item -> item.getItem() instanceof PotionItem) < 5)
            {
                checkIfRequestForItemExistOrCreateAsynch(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 9, 1);
            }
            if (InventoryUtils.getItemCountInItemHandler(worker.getInventoryCitizen(), item -> item.getItem() == ModItems.mistletoe) < 8)
            {
                checkIfRequestForItemExistOrCreateAsynch(new ItemStack(ModItems.mistletoe), 32, 8);
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
