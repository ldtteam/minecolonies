package com.minecolonies.coremod.colony.requestsystem.init;

import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.factory.standard.IntegerFactory;
import com.minecolonies.api.colony.requestsystem.factory.standard.TypeTokenFactory;
import com.minecolonies.api.colony.requestsystem.token.InitializedTokenFactory;
import com.minecolonies.api.colony.requestsystem.token.RandomSeededTokenFactory;
import com.minecolonies.api.colony.requestsystem.token.StandardTokenFactory;
import com.minecolonies.api.crafting.RecipeStorageFactory;
import com.minecolonies.coremod.colony.requestsystem.data.*;
import com.minecolonies.coremod.colony.requestsystem.locations.EntityLocation;
import com.minecolonies.coremod.colony.requestsystem.locations.StaticLocation;
import com.minecolonies.coremod.colony.requestsystem.requesters.factories.BuildingBasedRequesterFactory;
import com.minecolonies.coremod.colony.requestsystem.requests.StandardRequestFactories;
import com.minecolonies.coremod.colony.requestsystem.resolvers.factory.*;

/**
 * Initializer for the {@link StandardFactoryControllerInitializer}
 */
public final class StandardFactoryControllerInitializer
{

    /**
     * Private constructor to hide the implicit public one.
     */
    private StandardFactoryControllerInitializer()
    {
    }

    public static void onPreInit()
    {
        StandardFactoryController.getInstance().registerNewFactory(new StandardTokenFactory());
        StandardFactoryController.getInstance().registerNewFactory(new InitializedTokenFactory());
        StandardFactoryController.getInstance().registerNewFactory(new RandomSeededTokenFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StaticLocation.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new EntityLocation.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.ItemStackRequestFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.DeliveryRequestFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.ToolRequestFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.FoodRequestFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.SmeltableOreRequestFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestFactories.BurnableRequestFactory());
        StandardFactoryController.getInstance().registerNewFactory(new BuildingBasedRequesterFactory());
        StandardFactoryController.getInstance().registerNewFactory(new DeliveryRequestResolverFactory());
        StandardFactoryController.getInstance().registerNewFactory(new WarehouseRequestResolverFactory());
        StandardFactoryController.getInstance().registerNewFactory(new PrivateWorkerCraftingRequestResolverFactory());
        StandardFactoryController.getInstance().registerNewFactory(new BuildingRequestResolverFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardPlayerRequestResolverFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRetryingRequestResolverFactory());
        StandardFactoryController.getInstance().registerNewFactory(new RecipeStorageFactory());
        StandardFactoryController.getInstance().registerNewFactory(new IntegerFactory());
        StandardFactoryController.getInstance().registerNewFactory(new TypeTokenFactory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestIdentitiesDataStore.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestResolversIdentitiesDataStore.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardProviderRequestResolverAssignmentDataStore.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestResolverRequestAssignmentDataStore.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestableTypeRequestResolverAssignmentDataStore.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestSystemBuildingDataStore.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardRequestSystemDeliveryManJobDataStore.Factory());
        StandardFactoryController.getInstance().registerNewFactory(new StandardDataStoreManager.Factory());

        StandardFactoryController.getInstance().registerNewTypeOverrideHandler(new TypeTokenFactory.TypeTokenSubTypeOverrideHandler());

        StandardFactoryController.getInstance().registerNewClassRenaming("com.minecolonies.coremod.colony.requestsystem.resolvers.PlayerRequestResolver", "com.minecolonies.coremod.colony.requestsystem.resolvers.StandardPlayerRequestResolver");
    }
}
