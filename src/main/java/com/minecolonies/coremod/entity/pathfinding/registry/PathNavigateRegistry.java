package com.minecolonies.coremod.entity.pathfinding.registry;

import com.google.common.collect.Maps;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import com.minecolonies.api.entity.pathfinding.registry.IPathNavigateRegistry;
import com.minecolonies.coremod.entity.pathfinding.MinecoloniesAdvancedPathNavigate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class PathNavigateRegistry implements IPathNavigateRegistry
{
    private static final Function<MobEntity, AbstractAdvancedPathNavigate> DEFAULT = (entityLiving -> new MinecoloniesAdvancedPathNavigate(entityLiving, entityLiving.world));

    private final Map<Predicate<MobEntity>, Function<MobEntity, AbstractAdvancedPathNavigate>> registry = Maps.newLinkedHashMap();

    @Override
    public IPathNavigateRegistry registerNewPathNavigate(
      final Predicate<MobEntity> selectionPredicate, final Function<MobEntity, AbstractAdvancedPathNavigate> navigateProducer)
    {
        registry.put(selectionPredicate, navigateProducer);
        return this;
    }

    @Override
    public AbstractAdvancedPathNavigate getNavigateFor(final MobEntity entityLiving)
    {
        final List<Predicate<MobEntity>> predicates = new ArrayList<>(registry.keySet());
        Collections.reverse(predicates);

        return predicates.stream().filter(predicate -> predicate.test(entityLiving)).findFirst().map(predicate -> registry.get(predicate)).orElse(DEFAULT).apply(entityLiving);
    }
}
