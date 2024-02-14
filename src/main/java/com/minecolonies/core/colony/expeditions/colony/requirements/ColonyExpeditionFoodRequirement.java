package com.minecolonies.core.colony.expeditions.colony.requirements;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * Colony expedition requirements for providing any kind of food, with a minimum amount.
 */
public class ColonyExpeditionFoodRequirement extends ColonyExpeditionRequirement
{
    /**
     * The minimum amount to fulfill this requirement.
     */
    private final int amount;

    /**
     * Cached set of icons, because lookup is expensive.
     */
    private List<ItemStack> iconsCached;

    /**
     * Default constructor.
     *
     * @param amount the minimum amount.
     */
    public ColonyExpeditionFoodRequirement(final int amount)
    {
        this.amount = amount;
    }

    @Override
    @NotNull
    public ResourceLocation getId()
    {
        return new ResourceLocation(Constants.MOD_ID, "food");
    }

    @Override
    public RequirementHandler createHandler(final InventorySupplier inventory)
    {
        return new FoodRequirementHandler(inventory);
    }

    /**
     * Food handler instance used for verifying if the given item handler contains any food items.
     */
    public class FoodRequirementHandler extends RequirementHandler
    {
        /**
         * Default constructor.
         *
         * @param inventory supplier for obtaining the inventory.
         */
        private FoodRequirementHandler(final InventorySupplier inventory)
        {
            super(inventory);
        }

        @Override
        public ResourceLocation getId()
        {
            return ColonyExpeditionFoodRequirement.this.getId();
        }

        @Override
        public Component getName()
        {
            return Component.translatable("com.minecolonies.core.expedition.gui.items.requirement.type.food");
        }

        @Override
        public List<ItemStack> getIcon()
        {
            if (iconsCached == null)
            {
                iconsCached = IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems().stream().filter(ItemStackUtils.ISFOOD).toList();
            }

            return iconsCached;
        }

        @Override
        public int getAmount()
        {
            return amount;
        }

        @Override
        public Predicate<ItemStack> getItemPredicate()
        {
            return ItemStackUtils.ISFOOD;
        }
    }
}