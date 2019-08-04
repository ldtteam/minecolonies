package com.minecolonies.api.entity.pathfinding.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.entity.pathfinding.AbstractAdvancedPathNavigate;
import net.minecraft.entity.LivingEntity;

import java.util.function.Function;
import java.util.function.Predicate;

public interface IPathNavigateRegistry
{

    static IPathNavigateRegistry getInstance()
    {
        return IMinecoloniesAPI.getInstance().getPathNavigateRegistry();
    }

    IPathNavigateRegistry registerNewPathNavigate(Predicate<LivingEntity> selectionPredicate, Function<LivingEntity, AbstractAdvancedPathNavigate> navigateProducer);

    AbstractAdvancedPathNavigate getNavigateFor(LivingEntity entityLiving);
}
