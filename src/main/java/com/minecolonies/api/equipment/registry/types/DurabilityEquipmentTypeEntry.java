package com.minecolonies.api.equipment.registry.types;

import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Equipment type for items whose level is based on their durability relative to a vanilla reference item,
 * rather than a wood/stone/iron/diamond/netherite tier: bow, crossbow, fishing rod, shears, shield, flint
 * and steel, spear. Not applicable to blocks.
 */
public class DurabilityEquipmentTypeEntry extends EquipmentTypeEntry
{
    /**
     * Predicate determining whether a given item stack is this equipment type.
     */
    private final Predicate<ItemStack> isEquipment;

    /**
     * The max damage of the vanilla reference item this type's durability-based level is relative to.
     */
    private final int referenceMaxDamage;

    public DurabilityEquipmentTypeEntry(final ResourceLocation registryName, final Component displayName, final Predicate<ItemStack> isEquipment, final int referenceMaxDamage)
    {
        super(registryName, displayName);
        this.isEquipment = isEquipment;
        this.referenceMaxDamage = referenceMaxDamage;
    }

    @Override
    protected boolean isEquipment(final ItemStack itemStack)
    {
        return isEquipment.test(itemStack);
    }

    @Override
    protected int getLevel(final ItemStack itemStack)
    {
        if (!itemStack.isDamageableItem())
        {
            return 5;
        }

        return Mth.clamp(itemStack.getMaxDamage() / referenceMaxDamage, 1, 5);
    }

    @Override
    @Nullable
    protected Integer getBlockRequirement(final BlockState state)
    {
        return null;
    }
}
