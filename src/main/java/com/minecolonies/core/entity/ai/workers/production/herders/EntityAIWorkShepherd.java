package com.minecolonies.core.entity.ai.workers.production.herders;

import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.StatsUtil;
import com.minecolonies.core.Network;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingShepherd;
import com.minecolonies.core.colony.jobs.JobShepherd;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import com.minecolonies.core.network.messages.client.LocalizedParticleEffectMessage;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.common.IForgeShearable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.StatisticsConstants.DISTINCT_ANIMALS_SHEARED;
import static com.minecolonies.api.util.constant.StatisticsConstants.ITEM_OBTAINED;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;

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
     * Time before retrying an animal whose shearing implementation made no observable progress.
     */
    private static final int FAILED_SHEARING_RETRY_TICKS = 30 * TICKS_SECOND;

    /**
     * Animals temporarily excluded after a no-op shearing implementation was detected.
     */
    private final Map<UUID, Long> failedShearingAnimals = new HashMap<>();

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

        final Animal shearableAnimal = findShearableAnimal();

        if (building.getSetting(BuildingShepherd.SHEARING).getValue() && result.equals(START_WORKING) && shearableAnimal != null)
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
     * Finds an adult animal that is tagged for the shepherd and currently supports shearing.
     *
     * @return a shearable animal, or {@code null} if none is available
     */
    @Nullable
    private Animal findShearableAnimal()
    {
        final long gameTime = world.getGameTime();
        failedShearingAnimals.entrySet().removeIf(entry -> entry.getValue() <= gameTime);

        final ItemStack shears = new ItemStack(Items.SHEARS);
        return searchForAnimals(animal -> animal.getType().is(ModTags.shepherdShearableAnimals)
                                             && !animal.isBaby()
                                             && !failedShearingAnimals.containsKey(animal.getUUID())
                                             && animal instanceof IForgeShearable shearable
                                             && shearable.isShearable(shears, world, animal.blockPosition()))
                 .stream().findAny().orElse(null);
    }

    /**
     * Shears a tagged animal using its Forge shearing implementation.
     *
     * @return The next {@link IAIState}
     */
    private IAIState shearSheep()
    {

        final Animal animal = findShearableAnimal();

        if (animal == null)
        {
            return DECIDE;
        }

        if (!equipTool(InteractionHand.MAIN_HAND, ModEquipmentTypes.shears.get()))
        {
            return PREPARING;
        }

        if (worker.getMainHandItem() != null)
        {
            if (walkingToAnimal(animal))
            {
                return getState();
            }

            int enchantmentLevel = worker.getMainHandItem().getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
            enchantmentLevel *= Math.max(1.0, (getPrimarySkillLevel() / 5.0));

            worker.swing(InteractionHand.MAIN_HAND);

            List<ItemStack> items = List.of();
            boolean failedToShear = false;
            if (!this.world.isClientSide)
            {
                final IForgeShearable shearable = (IForgeShearable) animal;
                items = shearable.onSheared(null,
                  worker.getMainHandItem(),
                  world,
                  animal.blockPosition(),
                  enchantmentLevel);
                failedToShear = items.isEmpty()
                                  && shearable.isShearable(worker.getMainHandItem(), world, animal.blockPosition());
            }

            if (failedToShear)
            {
                failedShearingAnimals.put(animal.getUUID(), world.getGameTime() + FAILED_SHEARING_RETRY_TICKS);
                return DECIDE;
            }

            StatsUtil.trackStatByName(building, DISTINCT_ANIMALS_SHEARED, animal.getType().getDescriptionId(), 1);

            if (!items.isEmpty())
            {
                Network.getNetwork().sendToTrackingEntity(new LocalizedParticleEffectMessage(items.get(0), animal.getOnPos().above()), worker);
            }

            // There is no generic interface to indicate that an animal can be dyed - this remains Sheep-specific.
            if (animal instanceof Sheep sheep)
            {
                dyeSheepChance(sheep);
            }

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
