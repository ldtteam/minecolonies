package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.colony.buildings.views.AbstractFilterableListsView;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Window for all the filterable lists.
 */
public abstract class AbstractHutFilterableLists extends AbstractWindowWorkerBuilding<AbstractFilterableListsView>
{
    /**
     * Window filterable list views.
     */
    protected final Map<String, ViewFilterableList> views = new HashMap<>();

    /**
     * The list of filter predicates for the filterable list.
     */
    protected Map<String, Predicate<ItemStack>> itemStackPredicate = new HashMap<>();

    /**
     * Constructor for the window of the the filterable lists.
     *
     * @param building {@link AbstractFilterableListsView}.
     * @param res the resource String.
     * @param predicates the restriction.
     */
    @SafeVarargs
    public AbstractHutFilterableLists(final AbstractFilterableListsView building, final String res, final Tuple<String, Predicate<ItemStack>>... predicates)
    {
        super(building, res);
        for (final Tuple<String, Predicate<ItemStack>> tuple : predicates)
        {
            this.itemStackPredicate.put(tuple.getFirst(), tuple.getSecond());
        }
    }

    /**
     * The classic block list.
     * @param filterPredicate the predicate filter.
     * @param id the id of the specific predicate.
     * @return the list of itemStorages.
     */
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        if (!itemStackPredicate.containsKey(id))
        {
            return Collections.emptyList();
        }
        return ImmutableList.copyOf(IColonyManager.getInstance().getCompatibilityManager().getBlockList().stream().filter(filterPredicate.and(itemStackPredicate.get(id))).map(ItemStorage::new).collect(Collectors.toList()));
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);
        for (final Map.Entry<String, ViewFilterableList> view : views.entrySet())
        {
            View parent = button.getParent();
            int maxDepth = 10;
            while (parent != null && maxDepth > 0)
            {
                parent = parent.getParent();
                if (parent != null && view.getKey().equals(parent.getID()))
                {
                    view.getValue().onButtonClick(button);
                    return;
                }
                maxDepth--;
            }
        }
    }

    @Override
    public boolean onKeyTyped(final char ch, final int key)
    {
        final boolean result = super.onKeyTyped(ch, key);
        for (final ViewFilterableList view : views.values())
        {
            view.onKeyTyped();
        }
        return result;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        for (final ViewFilterableList view : views.values())
        {
            view.onOpened();
        }
    }
}

