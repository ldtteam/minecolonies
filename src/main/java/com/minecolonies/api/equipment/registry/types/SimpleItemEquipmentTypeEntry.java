package com.minecolonies.api.equipment.registry.types;

import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Equipment type for a simple, single-item, fixed-level equipment type with no tiering at all: lead.
 */
public class SimpleItemEquipmentTypeEntry extends EquipmentTypeEntry
{
    /**
     * The item this equipment type matches.
     */
    private final Item item;

    /**
     * The fixed equipment level reported for a matching stack.
     */
    private final int level;

    public SimpleItemEquipmentTypeEntry(final ResourceLocation registryName, final Component displayName, final Item item, final int level)
    {
        super(registryName, displayName);
        this.item = item;
        this.level = level;
    }

    @Override
    protected boolean isEquipment(final ItemStack itemStack)
    {
        return itemStack.is(item);
    }

    @Override
    protected int getLevel(final ItemStack itemStack)
    {
        return level;
    }

    @Override
    @Nullable
    protected Integer getBlockRequirement(final BlockState state)
    {
        return null;
    }
}
