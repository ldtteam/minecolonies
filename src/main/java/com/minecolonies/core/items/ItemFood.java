package com.minecolonies.core.items;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.client.gui.containers.WindowCitizenInventory;
import com.minecolonies.core.client.gui.modules.building.RestaurantMenuModuleWindow;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import java.util.Optional;
/**
 * A custom item class for food items.
 */
public class ItemFood extends Item implements IMinecoloniesFoodItem
{
    /**
     * The food tier.
     */
    private final int tier;
    /**
     * Creates a new food item.
     *
     * @param builder the item properties to use.
     * @param tier    the nutrition tier.
     */
    public ItemFood(@NotNull final Properties builder, final int tier)
    {
        super(builder);
        this.tier = tier;
    }
    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final TooltipContext ctx, @NotNull final TooltipDisplay display, Consumer<Component> tooltipConsumer, @NotNull final TooltipFlag flagIn)
    
    {
        final List<Component> tooltip = new ArrayList<>();
        if (WindowCitizenInventory.activeCitizenInventory == null)
        {
            tooltip.add(Component.translatable(TranslationConstants.TIER_TOOLTIP + this.tier));
        }
        tooltip.forEach(tooltipConsumer);
    }
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack)
    {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        int qty = 0;
        for (final ItemStorage ingredient : RestaurantMenuModuleWindow.getRecipeFromStack(new ItemStorage(stack), false, 1))
        {
            // Max Render Quantity.
            if (qty > 16)
            {
                break;
            }
            nonnulllist.add(ingredient.getItemStack());
            qty++;
        }
        return Optional.of(new BundleTooltip(new BundleContents(nonnulllist.stream().map(ItemStackTemplate::fromStack).toList())));
    }
    @Override
    public int getUseDuration(final ItemStack stack, final LivingEntity entity)
    {
        return super.getUseDuration(stack, entity) * Math.max(1, tier);
    }
    @Override
    public int getTier()
    {
        return this.tier;
    }

    /**
     * Nutrition used by the compostability data-map generator before item
     * component maps are bound.  Standard MineColonies foods are all at least
     * six nutrition, which is the generator's cap; specialised breads override
     * this value when they use a lower nutrition value.
     *
     * @return the food nutrition value.
     */
    public int getFoodNutrition()
    {
        return 6;
    }
}
