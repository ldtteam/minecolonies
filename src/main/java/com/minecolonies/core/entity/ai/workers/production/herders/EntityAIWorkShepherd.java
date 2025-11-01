package com.minecolonies.core.entity.ai.workers.production.herders;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingShepherd;
import com.minecolonies.core.colony.jobs.JobShepherd;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import com.minecolonies.core.network.messages.client.LocalizedParticleEffectMessage;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.StatisticsConstants.ITEM_OBTAINED;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;
import static net.minecraft.world.entity.animal.Sheep.ITEM_BY_DYE;

/**
 * The AI behind the {@link JobShepherd} for Breeding, Killing and Shearing sheep.
 */
public class EntityAIWorkShepherd extends AbstractEntityAIHerder<JobShepherd, BuildingShepherd>
{
    /**
     * Constants used for sheep dying calculations.
     */
    private static final int HUNDRED_PERCENT_CHANCE = 100;

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkShepherd(@NotNull final JobShepherd job)
    {
        super(job);
        super.registerTargets(
          new AITarget(SHEPHERD_SHEAR, this::shearSheep, TICKS_SECOND)
        );
    }

    @NotNull
    @Override
    public List<EquipmentTypeEntry> getExtraToolsNeeded()
    {
        final List<EquipmentTypeEntry> toolsNeeded = super.getExtraToolsNeeded();
        if (building.getSetting(BuildingShepherd.SHEARING).getValue())
        {
            toolsNeeded.add(ModEquipmentTypes.shears.get());
        }
        return toolsNeeded;
    }

    @Override
    public Class<BuildingShepherd> getExpectedBuildingClass()
    {
        return BuildingShepherd.class;
    }

    @Override
    public IAIState decideWhatToDo()
    {
        final IAIState result = super.decideWhatToDo();

        final Sheep shearingSheep = findShearableSheep();

        if (building.getSetting(BuildingShepherd.SHEARING).getValue() && result.equals(START_WORKING) && shearingSheep != null)
        {
            return SHEPHERD_SHEAR;
        }

        worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        return result;
    }

    @Override
    public double getButcheringAttackDamage()
    {
        return Math.max(1.0, getSecondarySkillLevel() / 10.0);
    }

    /**
     * @return a shearable {@link Sheep} or null.
     */
    @Nullable
    private Sheep findShearableSheep()
    {
        return searchForAnimals(a -> a instanceof Sheep sheepie && !sheepie.isSheared() && !sheepie.isBaby())
                 .stream().map(a -> (Sheep) a).findAny().orElse(null);
    }

    /**
     * Shears a sheep, with a chance of dying it!
     *
     * @return The next {@link IAIState}
     */
    private IAIState shearSheep()
    {

        final Sheep sheep = findShearableSheep();

        if (sheep == null)
        {
            return DECIDE;
        }

        if (!equipTool(InteractionHand.MAIN_HAND, ModEquipmentTypes.shears.get()))
        {
            return PREPARING;
        }

        if (worker.getMainHandItem() != null)
        {
            if (walkingToAnimal(sheep))
            {
                return getState();
            }

            int enchantmentLevel = worker.getMainHandItem().getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            enchantmentLevel *= Math.max(1.0, (getPrimarySkillLevel() / 5.0));

            worker.swing(InteractionHand.MAIN_HAND);

            final List<ItemStack> items = new ArrayList<>();
            if (!this.world.isClientSide)
            {
                sheep.setSheared(true);
                int qty = 1 + worker.getRandom().nextInt(enchantmentLevel + 1);

                for (int j = 0; j < qty; ++j)
                {
                    items.add(new ItemStack(ITEM_BY_DYE.get(sheep.getColor())));
                }
            }

            sheep.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
            Network.getNetwork().sendToTrackingEntity(new LocalizedParticleEffectMessage(new ItemStack(ITEM_BY_DYE.get(sheep.getColor())), sheep.getOnPos().above()), worker);
            dyeSheepChance(sheep);

            CitizenItemUtils.damageItemInHand(worker, InteractionHand.MAIN_HAND, 1);

            worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
            incrementActionsDoneAndDecSaturation();

            for (final ItemStack item : items)
            {
                building.getModule(STATS_MODULE).incrementBy(ITEM_OBTAINED + ";" + item.getItem().getDescriptionId(), item.getCount());
                InventoryUtils.transferItemStackIntoNextBestSlotInItemHandler(item, (worker.getInventoryCitizen()));
            }
        }

        return DECIDE;
    }

    /**
     * Possibly dyes a sheep based on their Worker Hut Level
     *
     * @param sheep the {@link Sheep} to possibly dye.
     */
    private void dyeSheepChance(final Sheep sheep)
    {
        if (building != null && building.getSetting(BuildingShepherd.DYEING).getValue())
        {
            final int chanceToDye = building.getBuildingLevel();
            final int rand = worker.getRandom().nextInt(HUNDRED_PERCENT_CHANCE);

            if (rand <= chanceToDye)
            {
                final DyeColor[] colors = DyeColor.values();
                final int dyeIndex = worker.getRandom().nextInt(colors.length);
                sheep.setColor(colors[dyeIndex]);
            }
        }
    }
}
