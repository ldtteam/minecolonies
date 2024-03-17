package com.minecolonies.core.entity.pathfinding.registry;

import com.google.common.collect.Maps;
import com.minecolonies.core.entity.pathfinding.navigation.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.core.entity.pathfinding.navigation.MinecoloniesAdvancedPathNavigate;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class PathNavigateRegistry implements IPathNavigateRegistry
{
    private static final Function<Mob, AbstractAdvancedPathNavigate> DEFAULT = (entityLiving -> new MinecoloniesAdvancedPathNavigate(entityLiving, entityLiving.level));

    private final Map<Predicate<Mob>, Function<Mob, AbstractAdvancedPathNavigate>> registry = Maps.newLinkedHashMap();

    @Override
    public IPathNavigateRegistry registerNewPathNavigate(
      final Predicate<Mob> selectionPredicate, final Function<Mob, AbstractAdvancedPathNavigate> navigateProducer)
    {
        registry.put(selectionPredicate, navigateProducer);
        return this;
    }

    @Override
    public AbstractAdvancedPathNavigate getNavigateFor(final Mob entityLiving)
    {
        final List<Predicate<Mob>> predicates = new ArrayList<>(registry.keySet());
        Collections.reverse(predicates);

        return predicates.stream().filter(predicate -> predicate.test(entityLiving)).findFirst().map(predicate -> registry.get(predicate)).orElse(DEFAULT).apply(entityLiving);
    }
}
