package com.minecolonies.api.util;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenFoodHandler;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingCook;
import com.minecolonies.core.items.ItemCrop;
import com.minecolonies.core.tileentities.TileEntityRack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Predicate;

import static com.minecolonies.api.research.util.ResearchConstants.SATURATION;
import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;

/**
 * Food specific util functions.
 */
public class FoodUtils
{
    /**
     * Predicate describing food which can be eaten (is not raw).
     */
    public static final Predicate<ItemStack> EDIBLE = itemStack -> ItemStackUtils.ISFOOD.test(itemStack) && !ItemStackUtils.ISCOOKABLE.test(itemStack);

    /**
     * @param stack
     * @param workBuilding
     * @return
     */
    public static boolean canEat(final ItemStack stack, final IBuilding homeBuilding, final IBuilding workBuilding)
    {
        if (!EDIBLE.test(stack))
        {
            return false;
        }

        final int homeBuildingLevel = homeBuilding == null ? 0 : homeBuilding.getBuildingLevelEquivalent();
        return canEatLevel(stack, homeBuildingLevel) && (workBuilding == null || workBuilding.canEat(stack));
    }

    /**
     * Check if that food can be eaten at a given building level.
     * @param stack the stack to check.
     * @param buildingLevel the respective building level.
     * @return true if so.
     */
    public static boolean canEatLevel(final ItemStack stack, final int buildingLevel)
    {
        if (stack.getItem() instanceof ItemCrop)
        {
            return false;
        }

        if (buildingLevel < 3)
        {
            return stack.getItem().getFoodProperties(stack, null) != null;
        }
        final FoodProperties foodProperties = stack.getItem().getFoodProperties(stack, null);
        return foodProperties != null && foodProperties.getNutrition() >= buildingLevel + 1;
    }

    /**
     * Calculate the given max building level for a given food.
     * @param resource the stack to check.
     * @return the building level.
     */
    public static int getBuildingLevelForFood(final ItemStack resource)
    {
        return Math.max(2, Math.min(resource.getFoodProperties(null).getNutrition() - 1, MAX_BUILDING_LEVEL));
    }

    /**
     * Calculate the actual food value for a citizen consuming a given food.
     * @param foodStack the food to consume.
     * @param itemFood the food properties of that food.
     * @param researchBonus the bonus from research (0 for no bonus).
     * @return the saturation adjustment to apply when consuming this food.
     */
    public static double getFoodValue(final ItemStack foodStack, @Nullable final FoodProperties itemFood, final double researchBonus)
    {
        if (itemFood == null)
        {
            return 0;
        }

        // At 40 saturation, if we do a x2 minecolonies bonus, we get 1 top food filling up the full food bar!

        final double saturationBonus = foodStack.getItem() instanceof IMinecoloniesFoodItem ? 2.0 : 1.0;
        return itemFood.getNutrition() * saturationBonus * (1.0 + researchBonus);
    }

    /**
     * Calculate the actual food value for a citizen consuming a given food.
     * @param foodStack the food to consume.
     * @param citizen the citizen consuming the food.
     * @return the saturation adjustment to apply when consuming this food.
     */
    public static double getFoodValue(final ItemStack foodStack, final AbstractEntityCitizen citizen)
    {
        final FoodProperties itemFood = foodStack.getItem().getFoodProperties(foodStack, citizen);
        final double researchBonus = citizen.getCitizenColonyHandler().getColonyOrRegister().getResearchManager().getResearchEffects().getEffectStrength(SATURATION);
        return getFoodValue(foodStack, itemFood, researchBonus);
    }

    /**
     * Calculate the food tier from the food item.
     *
     * @param food the food item.
     * @return the tier.
     */
    public static int getFoodTier(final ItemStack food)
    {
        if (food.getItem() instanceof IMinecoloniesFoodItem foodItem)
        {
            return foodItem.getTier();
        }

        final FoodProperties foodValue = food.getFoodProperties(null);
        if (foodValue == null || foodValue.getNutrition() < 12 || foodValue.getSaturationModifier() < 0.8)
        {
            return 0;
        }

        return 1;
    }

    /**
     * Get the best food for a given citizen from a given inventory and return the index where it is.
     * @param inventoryCitizen the inventory to check.
     * @param citizenData the citizen data the food is for.
     * @param menu the menu that has to be matched. or null
     * @return the matching inv slot, or -1.
     */
    public static int getBestFoodForCitizen(final InventoryCitizen inventoryCitizen, final ICitizenData citizenData, @Nullable final Set<ItemStorage> menu)
    {
        // Smaller score is better.
        int bestScore = Integer.MAX_VALUE;
        int bestSlot = -1;
        Item bestItem = null;
        final boolean restaurantExists = citizenData.getColony().getServerBuildingManager().getBestBuilding(citizenData.getWorkBuilding() == null ? citizenData.getHomePosition() : citizenData.getWorkBuilding().getPosition(), BuildingCook.class) != null;

        final ICitizenFoodHandler foodHandler = citizenData.getCitizenFoodHandler();
        final ICitizenFoodHandler.CitizenFoodStats foodStats = foodHandler.getFoodHappinessStats();
        final int diversityRequirement = FoodUtils.getMinFoodDiversityRequirement(citizenData.getHomeBuilding() == null ? 0 : citizenData.getHomeBuilding().getBuildingLevelEquivalent());
        final int qualityRequirement = FoodUtils.getMinFoodQualityRequirement(citizenData.getHomeBuilding() == null ? 0 : citizenData.getHomeBuilding().getBuildingLevelEquivalent());
        for (int i = 0; i < inventoryCitizen.getSlots(); i++)
        {
            final ItemStorage invStack = new ItemStorage(inventoryCitizen.getStackInSlot(i));
            if ((menu == null || menu.contains(invStack)) && FoodUtils.canEat(invStack.getItemStack(), citizenData.getHomeBuilding(), citizenData.getWorkBuilding()))
            {
                final boolean isMinecolfood = invStack.getItem() instanceof IMinecoloniesFoodItem;
                final int localScore = foodHandler.checkLastEaten(invStack.getItem()) * (isMinecolfood ? 1 : 2);

                // If the quality and diversity requirement would be fulfilled, already go ahead with this food. Don't need to check others.
                if ((localScore < 0 && isMinecolfood)
                || (localScore < 0 && foodStats.quality() > qualityRequirement * 2)
                || (isMinecolfood && foodStats.diversity() > diversityRequirement * 2))
                {
                    return i;
                }

                if (localScore < bestScore)
                {
                    bestScore = localScore;
                    bestSlot = i;
                    bestItem = invStack.getItem();
                }
            }
        }

        // If we're not at the restaurant and are the brink of complaining about food, go to the restaurant instead of eating the food you got in the inventory.
        if (restaurantExists && menu == null && citizenData.getCitizenFoodHandler().hasFullFoodHistory() &&
              ((bestScore >= 0 && foodStats.diversity() <= diversityRequirement)
              || (!(bestItem instanceof IMinecoloniesFoodItem) && foodStats.quality() <= qualityRequirement)))
        {
            return -1;
        }

        return bestSlot;
    }

    /**
     * Get the best food for a given citizen from a given inventory and return the index where it is.
     * @param citizenData the citizen data the food is for.
     * @param menu the menu that has to be matched or null.
     * @return the matching inv slot, or -1.
     */
    public static ItemStorage checkForFoodInBuilding(final ICitizenData citizenData, @Nullable final Set<ItemStorage> menu, final IBuilding building)
    {
        // Smaller score is better.
        int bestScore = Integer.MAX_VALUE;
        ItemStorage bestStorage = null;

        final Level world = building.getColony().getWorld();
        final int homeBuildingLevel = citizenData.getHomeBuilding() == null ? 0 : citizenData.getHomeBuilding().getBuildingLevelEquivalent();

        final ICitizenFoodHandler.CitizenFoodStats foodStats = citizenData.getCitizenFoodHandler().getFoodHappinessStats();
        final int diversityRequirement = FoodUtils.getMinFoodDiversityRequirement(citizenData.getHomeBuilding() == null ? 0 : citizenData.getHomeBuilding().getBuildingLevelEquivalent());
        final int qualityRequirement = FoodUtils.getMinFoodQualityRequirement(citizenData.getHomeBuilding() == null ? 0 : citizenData.getHomeBuilding().getBuildingLevelEquivalent());

        final boolean criticalDiversity = foodStats.diversity() <= diversityRequirement;
        final boolean criticalQuality = foodStats.quality() <= qualityRequirement;
        final ICitizenFoodHandler foodHandler = citizenData.getCitizenFoodHandler();

        for (final BlockPos pos : building.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    for (final ItemStorage storage : rackEntity.getAllContent().keySet())
                    {
                        if ((menu == null || menu.contains(storage)) && FoodUtils.canEat(storage.getItemStack(), citizenData.getHomeBuilding(), citizenData.getWorkBuilding()))
                        {
                            final boolean isMinecolfood = storage.getItem() instanceof IMinecoloniesFoodItem;
                            final int localScore = foodHandler.checkLastEaten(storage.getItem()) * (isMinecolfood ? 1 : 2);
                            final int tier = FoodUtils.getFoodTier(storage.getItemStack());

                            // If this is great food and we're at critical levels, go with it!
                            if ((localScore < 0 && isMinecolfood) && (criticalDiversity || criticalQuality))
                            {
                                return new ItemStorage(storage.getItemStack().copy());
                            }

                            if (localScore > bestScore)
                            {
                                continue;
                            }

                            if (isMinecolfood && !criticalQuality && MathUtils.RANDOM.nextInt(Math.max(1, tier + 2 - homeBuildingLevel)) <= 0)
                            {
                                bestScore = localScore;
                                bestStorage = storage;
                                continue;
                            }

                            bestScore = localScore;
                            bestStorage = storage;

                            // If the quality and diversity requirement would be fulfilled, already go ahead with this food. Don't need to check others.
                            if ((localScore < 0 && isMinecolfood)
                                  || (localScore < 0 && foodStats.quality() > qualityRequirement * 2)
                                  || (isMinecolfood && foodStats.diversity() > diversityRequirement * 2))
                            {
                                return new ItemStorage(storage.getItemStack().copy());
                            }
                        }
                    }
                }
            }
        }

        if (bestStorage == null)
        {
            return null;
        }
        return new ItemStorage(bestStorage.getItemStack().copy());
    }

    /**
     * Get the best food for a given citizen from a given inventory and return the index where it is.
     * @param citizenData the citizen data the food is for.
     * @param menu the menu that has to be matched or null.
     * @return the matching inv slot, or -1.
     */
    public static boolean hasBestOptionInInv(final InventoryCitizen inventoryCitizen, final ICitizenData citizenData, @Nullable final Set<ItemStorage> menu, final IBuilding building)
    {
        final int invSlot = getBestFoodForCitizen(inventoryCitizen, citizenData, menu);
        // Smaller score is better.
        int bestScore = Integer.MAX_VALUE;
        ItemStorage bestStorage = null;

        final Level world = building.getColony().getWorld();
        final int homeBuildingLevel = citizenData.getHomeBuilding() == null ? 0 : citizenData.getHomeBuilding().getBuildingLevelEquivalent();

        final ICitizenFoodHandler.CitizenFoodStats foodStats = citizenData.getCitizenFoodHandler().getFoodHappinessStats();
        final int diversityRequirement = FoodUtils.getMinFoodDiversityRequirement(citizenData.getHomeBuilding() == null ? 0 : citizenData.getHomeBuilding().getBuildingLevelEquivalent());
        final int qualityRequirement = FoodUtils.getMinFoodQualityRequirement(citizenData.getHomeBuilding() == null ? 0 : citizenData.getHomeBuilding().getBuildingLevelEquivalent());

        final boolean criticalDiversity = foodStats.diversity() <= diversityRequirement;
        final boolean criticalQuality = foodStats.quality() <= qualityRequirement;
        final ICitizenFoodHandler foodHandler = citizenData.getCitizenFoodHandler();

        int bestInvScore = Integer.MAX_VALUE;
        if (invSlot >= 0)
        {
            final ItemStack stack = inventoryCitizen.getStackInSlot(invSlot);
            final boolean isMinecolfood = stack.getItem() instanceof IMinecoloniesFoodItem;
            bestInvScore = foodHandler.checkLastEaten(stack.getItem()) * (isMinecolfood ? 1 : 2);;

            // Already good enough food in inventory? Skip building check.
            if ((bestInvScore < 0 && isMinecolfood) && (criticalDiversity || criticalQuality))
            {
                return true;
            }
        }

        for (final BlockPos pos : building.getContainers())
        {
            if (WorldUtil.isBlockLoaded(world, pos))
            {
                final BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof TileEntityRack rackEntity)
                {
                    for (final ItemStorage storage : rackEntity.getAllContent().keySet())
                    {
                        if ((menu == null || menu.contains(storage)) && FoodUtils.canEat(storage.getItemStack(), citizenData.getHomeBuilding(), citizenData.getWorkBuilding()))
                        {
                            final boolean isMinecolfood = storage.getItem() instanceof IMinecoloniesFoodItem;
                            final int localScore = foodHandler.checkLastEaten(storage.getItem())  * (isMinecolfood ? 1 : 2);
                            final int tier = FoodUtils.getFoodTier(storage.getItemStack());

                            // If this is great food and we're at critical levels, go with it!
                            if ((localScore < 0 && isMinecolfood) && (criticalDiversity || criticalQuality))
                            {
                                return false;
                            }

                            if (localScore > bestScore)
                            {
                                continue;
                            }

                            if (isMinecolfood && !criticalQuality && MathUtils.RANDOM.nextInt(Math.max(1, tier + 2 - homeBuildingLevel)) <= 0)
                            {
                                bestScore = localScore;
                                bestStorage = storage;
                                continue;
                            }

                            bestScore = localScore;
                            bestStorage = storage;

                            // If the quality and diversity requirement would be fulfilled, already go ahead with this food. Don't need to check others.
                            if ((localScore < 0 && isMinecolfood)
                                || (localScore < 0 && foodStats.quality() > qualityRequirement * 2)
                                || (isMinecolfood && foodStats.diversity() > diversityRequirement * 2))
                            {
                                return bestInvScore <= localScore;
                            }
                        }
                    }
                }
            }
        }

        if (bestStorage == null)
        {
            return bestInvScore < Integer.MAX_VALUE;
        }
        return bestInvScore <= bestScore;
    }

    /**
     * Get the min food quality requirement.
     * @param buildingLevel the building level to take into account.
     * @return the food quality requirement in number of items.
     */
    public static int getMinFoodQualityRequirement(final int buildingLevel)
    {
        return Math.max(0, buildingLevel - 2);
    }

    /**
     * Get the min food diversity requirement.
     * @param buildingLevel the building level to take into account.
     * @return the food diversity requirement in number of items.
     */
    public static int getMinFoodDiversityRequirement(final int buildingLevel)
    {
        return buildingLevel;
    }

    /**
     * Compute the saturation consumption factor.
     * @param buildingLevel the housing level it's based on.
     * @return the value.
     */
    public static double computeSaturationConsumptionFactor(final int buildingLevel)
    {
        return switch (buildingLevel)
        {
            case 1 -> 0.6;
            case 2 -> 0.725;
            case 3 -> 1.0;
            case 4 -> 1.2;
            case 5 -> 1.5;
            default -> 0.3;
        };
    }
}
