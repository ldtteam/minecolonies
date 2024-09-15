package com.minecolonies.core.colony.expeditions.colony.requirements;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * Colony expedition requirements for providing any kind of {@link com.minecolonies.api.equipment.registry.EquipmentTypeEntry}.
 */
public class ColonyExpeditionEquipmentRequirement extends ColonyExpeditionRequirement
{
    /**
     * The required equipment type.
     */
    private final EquipmentTypeEntry equipmentType;

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
     * @param equipmentType the required equipment type.
     * @param amount        the minimum amount.
     */
    public ColonyExpeditionEquipmentRequirement(final EquipmentTypeEntry equipmentType, final int amount)
    {
        this.equipmentType = equipmentType;
        this.amount = amount;
    }

    @Override
    @NotNull
    public ResourceLocation getId()
    {
        return new ResourceLocation(Constants.MOD_ID, "equipment/" + equipmentType.getRegistryName());
    }

    @Override
    public int getAmount()
    {
        return amount;
    }

    @Override
    public EquipmentRequirementHandler createHandler(final IItemHandler inventorySupplier)
    {
        return new EquipmentRequirementHandler(new RequirementHandlerOptions(inventorySupplier, (builder, stack) -> {
            if (stack.getItem() instanceof ArmorItem armorItem)
            {
                builder.getLeader().setArmor(armorItem.getEquipmentSlot(), stack);
            }
            else
            {
                builder.addEquipment(stack);
            }
        }));
    }

    /**
     * Get the required equipment type.
     *
     * @return the equipment type.
     */
    public EquipmentTypeEntry getEquipmentType()
    {
        return equipmentType;
    }

    /**
     * Equipment handler instance used for verifying if the given item handler contains the required equipment.
     */
    public class EquipmentRequirementHandler extends RequirementHandler
    {
        /**
         * Default constructor.
         *
         * @param options the options for this requirement handler.
         */
        private EquipmentRequirementHandler(final RequirementHandlerOptions options)
        {
            super(options);
        }

        @Override
        public ResourceLocation getId()
        {
            return ColonyExpeditionEquipmentRequirement.this.getId();
        }

        @Override
        public Predicate<ItemStack> getItemPredicate()
        {
            return equipmentType::checkIsEquipment;
        }

        @Override
        public ItemStack getDefaultItemStack()
        {
            final List<ItemStack> allowedEquipment = IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems().stream()
                                                       .filter(equipmentType::checkIsEquipment)
                                                       .toList();

            if (allowedEquipment.isEmpty())
            {
                return ItemStack.EMPTY;
            }

            ItemStack bestEquipment = allowedEquipment.get(0);
            for (int i = 1; i < allowedEquipment.size(); i++)
            {
                final ItemStack equipment = allowedEquipment.get(i);
                if (ItemStackUtils.isBetterEquipment(equipment, bestEquipment))
                {
                    bestEquipment = equipment;
                }
            }

            return bestEquipment;
        }

        @Override
        public Component getName()
        {
            return Component.translatable("com.minecolonies.core.expedition.gui.items.requirement.type.equipment", equipmentType.getDisplayName());
        }

        @Override
        public List<ItemStack> getIcon()
        {
            if (iconsCached == null)
            {
                iconsCached =
                  IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems().stream().filter(equipmentType::checkIsEquipment).toList();
            }

            return iconsCached;
        }

        @Override
        public int getAmount()
        {
            return amount;
        }
    }
}