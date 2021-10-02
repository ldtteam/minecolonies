package com.minecolonies.coremod.generation;

import com.ldtteam.datagenerators.loot_table.LootTableJson;
import com.ldtteam.datagenerators.loot_table.LootTableTypeEnum;
import com.ldtteam.datagenerators.loot_table.pool.PoolJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.EntryTypeEnum;
import com.ldtteam.datagenerators.loot_table.pool.entry.functions.IEntryFunction;
import com.ldtteam.datagenerators.loot_table.pool.entry.functions.SetCountFunctionJson;
import com.ldtteam.datagenerators.loot_table.pool.entry.functions.SetNbtFunctionJson;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a simple builder-pattern factory for {@link LootTableJson} intended to simplify data generators.  It does
 * not expose all the functionality of that, just the minimum we're currently using, so can be extended as needed.
 */
public class LootTableBuilder
{
    private final List<EntryJson> entries = new ArrayList<>();
    private LootTableTypeEnum type = LootTableTypeEnum.GENERIC;
    private int rolls = 1;
    private float bonusRolls = 0f;

    @NotNull
    public LootTableBuilder type(final LootTableTypeEnum type)
    {
        this.type = type;
        return this;
    }

    @NotNull
    public LootTableBuilder rolls(final int rolls)
    {
        this.rolls = rolls;
        return this;
    }

    @NotNull
    public LootTableBuilder bonusRolls(final float bonusRolls)
    {
        this.bonusRolls = bonusRolls;
        return this;
    }

    @NotNull
    public LootTableBuilder empty(final int weight)
    {
        entries.add(new EntryJson(null, EntryTypeEnum.EMPTY, null, null, false, null, weight, 0));
        return this;
    }

    @NotNull
    public LootTableBuilder item(final Item item, final int weight)
    {
        entries.add(new EntryJson(null, EntryTypeEnum.ITEM, item.getRegistryName().toString(), null, false, null, weight, 0));
        return this;
    }

    @NotNull
    public LootTableBuilder item(final ItemStack stack, final int weight)
    {
        if (!stack.isEmpty())
        {
            List<IEntryFunction> functions = new ArrayList<>();
            if (stack.hasTag())
            {
                functions.add(new SetNbtFunctionJson(stack.getTag().toString()));
            }
            if (stack.getCount() > 1)
            {
                functions.add(new SetCountFunctionJson(stack.getCount()));
            }
            entries.add(new EntryJson(null, EntryTypeEnum.ITEM, stack.getItem().getRegistryName().toString(), null, false, functions, weight, 0));
        }
        return this;
    }

    @NotNull
    public LootTableJson build()
    {
        return new LootTableJson(type, Collections.singletonList(new PoolJson(null, rolls, bonusRolls, entries)));
    }
}
