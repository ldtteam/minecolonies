package com.minecolonies.api.util.constant;

import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.data.*;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.requestable.*;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PrivateCrafting;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.colony.requestsystem.token.StandardToken;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.crafting.RecipeStorage;
import com.minecolonies.api.research.IResearch;
import com.minecolonies.api.research.ModifierResearchEffect;
import com.minecolonies.api.research.UnlockResearchEffect;

import java.util.UUID;

/**
 * Class holds type constants to reduce the formatting errors.
 */
public class TypeConstants
{
    /////Java types
    public static final TypeToken<Integer>   INTEGER   = TypeToken.of(Integer.class);
    public static final TypeToken<TypeToken> TYPETOKEN = TypeToken.of(TypeToken.class);
    public static final TypeToken<Class>     CLASS     = TypeToken.of(Class.class);

    /////General purpose
    public static final TypeToken<IToken>                 ITOKEN             = TypeToken.of(IToken.class);
    public static final TypeToken<ILocation>              ILOCATION          = TypeToken.of(ILocation.class);
    public static final TypeToken<UUID>                   UUID               = TypeToken.of(UUID.class);
    public static final TypeToken<FactoryVoidInput>       FACTORYVOIDINPUT   = TypeToken.of(FactoryVoidInput.class);
    public static final TypeToken<Object>                 OBJECT             = TypeToken.of(Object.class);
    public static final TypeToken<IRequestable>           REQUESTABLE        = TypeToken.of(IRequestable.class);
    public static final TypeToken<IRetryable>             RETRYABLE          = TypeToken.of(IRetryable.class);
    public static final TypeToken<RecipeStorage>          RECIPE             = TypeToken.of(RecipeStorage.class);
    public static final TypeToken<IDeliverable>           DELIVERABLE        = TypeToken.of(IDeliverable.class);
    public static final TypeToken<ItemStorage>            ITEMSTORAGE        = TypeToken.of(ItemStorage.class);
    public static final TypeToken<UnlockResearchEffect>   UNLOCK_RESEARCH_EF = TypeToken.of(UnlockResearchEffect.class);
    public static final TypeToken<ModifierResearchEffect> MOD_RESEARCH_EF    = TypeToken.of(ModifierResearchEffect.class);
    public static final TypeToken<IResearch>              RESEARCH           = TypeToken.of(IResearch.class);

    /////Request system specific
    public static final TypeToken<IPlayerRequestResolver>                             PLAYER_REQUEST_RESOLVER                         = TypeToken.of(IPlayerRequestResolver.class);
    public static final TypeToken<IRetryingRequestResolver>                           RETRYING_REQUEST_RESOLVER                       =
      TypeToken.of(IRetryingRequestResolver.class);
    public static final TypeToken<IRequestIdentitiesDataStore>                        REQUEST_IDENTITIES_DATA_STORE                   =
      TypeToken.of(IRequestIdentitiesDataStore.class);
    public static final TypeToken<IRequestResolverIdentitiesDataStore>                REQUEST_RESOLVER_IDENTITIES_DATA_STORE          =
      TypeToken.of(IRequestResolverIdentitiesDataStore.class);
    public static final TypeToken<IProviderResolverAssignmentDataStore>               PROVIDER_REQUEST_RESOLVER_ASSIGNMENT_DATA_STORE =
      TypeToken.of(IProviderResolverAssignmentDataStore.class);
    public static final TypeToken<IRequestResolverRequestAssignmentDataStore>         REQUEST_RESOLVER_REQUEST_ASSIGNMENT_DATA_STORE
                                                                                                                                      =
      TypeToken.of(IRequestResolverRequestAssignmentDataStore.class);
    public static final TypeToken<IRequestableTypeRequestResolverAssignmentDataStore> REQUESTABLE_TYPE_REQUEST_RESOLVER_ASSIGNMENT_DATA_STORE
                                                                                                                                      =
      TypeToken.of(IRequestableTypeRequestResolverAssignmentDataStore.class);
    public static final TypeToken<IDataStoreManager>                                  DATA_STORE_MANAGER                              = TypeToken.of(IDataStoreManager.class);
    public static final TypeToken<IRequestSystemBuildingDataStore>                    REQUEST_SYSTEM_BUILDING_DATA_STORE              =
      TypeToken.of(IRequestSystemBuildingDataStore.class);
    public static final TypeToken<IRequestSystemDeliveryManJobDataStore>              REQUEST_SYSTEM_DELIVERY_MAN_JOB_DATA_STORE      =
      TypeToken.of(IRequestSystemDeliveryManJobDataStore.class);
    public static final TypeToken<IRequestSystemCrafterJobDataStore>                  REQUEST_SYSTEM_CRAFTER_JOB_DATA_STORE           =
      TypeToken.of(IRequestSystemCrafterJobDataStore.class);

    /////Implementations
    public static final TypeToken<StandardToken>   STANDARDTOKEN    = TypeToken.of(StandardToken.class);
    public static final TypeToken<PrivateCrafting> PRIVATE_CRAFTING = TypeToken.of(PrivateCrafting.class);
    public static final TypeToken<PublicCrafting>  PUBLIC_CRAFTING  = TypeToken.of(PublicCrafting.class);
    public static final TypeToken<Delivery>        DELIVERY         = TypeToken.of(Delivery.class);
    public static final TypeToken<Tool>            TOOL             = TypeToken.of(Tool.class);
}
