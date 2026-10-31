package com.minecolonies.api.equipment.registry.types;

import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolAction;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Equipment type for vanilla digger-style tools: pickaxe, shovel, axe, hoe, and sword. Determines
 * equipment-ness via the item's {@link ToolAction} set, level via the item's {@link Tier}, and (for the
 * diggers, not sword) block correctness/required level via a mineable block tag plus
 * {@link TierSortingRegistry}. This is the only place in the equipment type system that {@link Tier} and
 * {@link TierSortingRegistry} are consulted directly.
 */
public class DiggerEquipmentTypeEntry extends EquipmentTypeEntry
{
    /**
     * The set of actions an item stack must be able to perform to count as this equipment type.
     */
    private final Set<ToolAction> actions;

    /**
     * The tag of blocks this equipment type can be used on, or null if not applicable to blocks (sword).
     */
    @Nullable
    private final TagKey<Block> mineableTag;

    public DiggerEquipmentTypeEntry(final ResourceLocation registryName, final Component displayName, final Set<ToolAction> actions)
    {
        this(registryName, displayName, actions, null);
    }

    public DiggerEquipmentTypeEntry(final ResourceLocation registryName, final Component displayName, final Set<ToolAction> actions, @Nullable final TagKey<Block> mineableTag)
    {
        super(registryName, displayName);
        this.actions = actions;
        this.mineableTag = mineableTag;
    }

    @Override
    protected boolean isEquipment(final ItemStack itemStack)
    {
        for (final ToolAction action : actions)
        {
            if (!itemStack.canPerformAction(action))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    protected int getLevel(final ItemStack itemStack)
    {
        if (itemStack.getItem() instanceof final TieredItem tieredItem)
        {
            return Mth.clamp(tieredItem.getTier().getLevel(), 0, 5);
        }
        return -1;
    }

    @Override
    @Nullable
    protected Integer getBlockRequirement(final BlockState state)
    {
        if (mineableTag == null || !state.is(mineableTag))
        {
            return null;
        }

        for (final Tier tier : TierSortingRegistry.getSortedTiers())
        {
            if (TierSortingRegistry.isCorrectTierForDrops(tier, state))
            {
                return Math.max(tier.getLevel(), 0);
            }
        }
        return 0;
    }
}
