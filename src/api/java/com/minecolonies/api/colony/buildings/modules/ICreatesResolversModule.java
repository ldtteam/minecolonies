package com.minecolonies.api.colony.buildings.modules;

import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;

import java.util.List;

/**
 * Interface describing core building stats.
 * The first core stats module that is found in the building will define the values.
 */
public interface ICreatesResolversModule extends IBuildingModule
{
    /**
     * Get the max number of inhabitants this module allows.
     * @return the modules max number of assigned citizens.
     */
    List<IRequestResolver<?>> createResolvers();
}
