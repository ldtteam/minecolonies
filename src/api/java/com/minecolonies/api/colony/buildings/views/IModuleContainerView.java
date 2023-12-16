package com.minecolonies.api.colony.buildings.views;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requester.IRequester;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Suppression.GENERIC_WILDCARD;

public interface IModuleContainerView
{
    /**
     * Get the first module view matching the class.
     * @param clazz the class to match.
     * @param <T> the type of module returned.
     * @return the module.
     */
    <T extends IBuildingModuleView> T getModuleViewByType(Class<T> clazz);

    /**
     * Get the first module with a particular class or interface.
     *
     * @return the module or empty if not existent.
     */
    IBuildingModuleView getModuleView(final int id);

    /**
     * Get the first module with a particular class or interface.
     *
     * @return the module or empty if not existent.
     */
    <M extends IBuildingModule, V extends IBuildingModuleView> V getModuleView(final BuildingEntry.ModuleProducer<M,V> producer);

    /**
     * Check if the object has a particular module.
     *
     * @param producer the module producer for the module
     * @return true if so.
     */
    boolean hasModuleView(final BuildingEntry.ModuleProducer producer);

    /**
     * Get the module view matching a certain predicate.
     * @param clazz the class of the module.
     * @param modulePredicate the predicate to match.
     * @param <T> the optional type.
     * @return the module.
     */
    @Nullable
    <T extends IBuildingModuleView> T getModuleViewMatching(Class<T> clazz, Predicate<? super T> modulePredicate);

    /**
     * Get a list of all modules matching a specific class.
     * @param clazz the class to match.
     * @param <T> the type of module.
     * @return the list or empty if doesn't exist.
     */
    <T extends IBuildingModuleView> List<T> getModuleViews(Class<T> clazz);

    /**
     * Register a view module with the building.
     * @param iModuleView the view.
     */
    void registerModule(IBuildingModuleView iModuleView);

    /**
     * Get all module views in a list.
     * @return the list of views.
     */
    List<IBuildingModuleView> getAllModuleViews();
}
