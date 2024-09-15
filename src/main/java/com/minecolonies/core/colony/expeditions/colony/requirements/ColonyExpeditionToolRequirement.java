package com.minecolonies.core.colony.expeditions.colony.requirements;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.IToolType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

/**
 * Colony expedition requirements for providing any kind of {@link com.minecolonies.api.util.constant.ToolType}.
 */
public class ColonyExpeditionToolRequirement extends ColonyExpeditionRequirement
{
    /**
     * The required tool type.
     */
    private final IToolType toolType;

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
     * @param toolType the required tool type.
     * @param amount   the minimum amount.
     */
    public ColonyExpeditionToolRequirement(final IToolType toolType, final int amount)
    {
        this.toolType = toolType;
        this.amount = amount;
    }

    @Override
    @NotNull
    public ResourceLocation getId()
    {
        return new ResourceLocation(Constants.MOD_ID, "tool/" + toolType.getName());
    }

    @Override
    public int getAmount()
    {
        return amount;
    }

    @Override
    public ToolRequirementHandler createHandler(final IItemHandler inventorySupplier)
    {
        return new ToolRequirementHandler(new RequirementHandlerOptions(inventorySupplier, (builder, stack) -> {
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
     * Get the required tool type.
     *
     * @return the tool type.
     */
    public IToolType getToolType()
    {
        return toolType;
    }

    /**
     * Tool handler instance used for verifying if the given item handler contains the required tool.
     */
    public class ToolRequirementHandler extends RequirementHandler
    {
        /**
         * Default constructor.
         *
         * @param options the options for this requirement handler.
         */
        private ToolRequirementHandler(final RequirementHandlerOptions options)
        {
            super(options);
        }

        @Override
        public ResourceLocation getId()
        {
            return ColonyExpeditionToolRequirement.this.getId();
        }

        @Override
        public Predicate<ItemStack> getItemPredicate()
        {
            return stack -> ItemStackUtils.isTool(stack, toolType);
        }

        @Override
        public ItemStack getDefaultItemStack()
        {
            final List<ItemStack> allowedTools = IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems().stream()
                                                   .filter(stack -> ItemStackUtils.isTool(stack, toolType))
                                                   .toList();

            if (allowedTools.isEmpty())
            {
                return ItemStack.EMPTY;
            }

            ItemStack bestTool = allowedTools.get(0);
            for (int i = 1; i < allowedTools.size(); i++)
            {
                final ItemStack tool = allowedTools.get(i);
                if (ItemStackUtils.isBetterTool(tool, bestTool))
                {
                    bestTool = tool;
                }
            }

            return bestTool;
        }

        @Override
        public Component getName()
        {
            return Component.translatable("com.minecolonies.core.expedition.gui.items.requirement.type.tool", toolType.getDisplayName());
        }

        @Override
        public List<ItemStack> getIcon()
        {
            if (iconsCached == null)
            {
                iconsCached =
                  IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems().stream().filter(stack -> ItemStackUtils.isTool(stack, toolType)).toList();
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