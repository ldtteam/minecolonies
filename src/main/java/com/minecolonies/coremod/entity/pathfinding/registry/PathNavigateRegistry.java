package com.minecolonies.coremod.entity.pathfinding.registry;

import com.google.common.collect.Maps;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class PathNavigateRegistry implements IPathNavigateRegistry
{
    private static final Function<LivingEntity, AbstractAdvancedPathNavigate> DEFAULT = (entityLiving -> new MinecoloniesAdvancedPathNavigate(entityLiving, entityLiving.world));

    private final Map<Predicate<LivingEntity>, Function<LivingEntity, AbstractAdvancedPathNavigate>> registry = Maps.newLinkedHashMap();

    @Override
    public IPathNavigateRegistry registerNewPathNavigate(
      final Predicate<LivingEntity> selectionPredicate, final Function<LivingEntity, AbstractAdvancedPathNavigate> navigateProducer)
    {
        registry.put(selectionPredicate, navigateProducer);
        return this;
    }

    @Override
    public AbstractAdvancedPathNavigate getNavigateFor(final LivingEntity entityLiving)
    {
        final List<Predicate<LivingEntity>> predicates = new ArrayList<>(registry.keySet());
        Collections.reverse(predicates);

        return predicates.stream().filter(predicate -> predicate.test(entityLiving)).findFirst().map(predicate -> registry.get(predicate)).orElse(DEFAULT).apply(entityLiving);
    }
}
