package com.minecolonies.coremod.colony.buildings.modules;

import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.resolver.retrying.IRetryingRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.ModCraftingTypes;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.OptionalPredicate;
import com.minecolonies.coremod.util.DomumOrnamentumUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract crafting module for Domum Ornamentum cutter recipes
 */
public abstract class AbstractDOCraftingBuildingModule extends AbstractCraftingBuildingModule.Custom
{
    private final Set<IToken<?>> learnableRequests = new HashSet<>();

    protected AbstractDOCraftingBuildingModule(@NotNull final JobEntry jobEntry)
    {
        super(jobEntry);
    }

    // ideally this should override getId() and return something DO-ish and not "custom" (which would also let a
    // more appropriate icon be shown in the UI), but changing that now would break the existing saved recipes
    // without a special upgrade...

    @Override
    public Set<CraftingType> getSupportedCraftingTypes()
    {
        return Set.of(ModCraftingTypes.ARCHITECTS_CUTTER.get());
    }

    @Override
    public boolean isRecipeCompatible(final @NotNull IGenericRecipe recipe)
    {
        final OptionalPredicate<ItemStack> validator = getIngredientValidator();
        final ItemStack stack = recipe.getPrimaryOutput();
        if (ForgeRegistries.ITEMS.getKey(stack.getItem()).getNamespace().equals("domum_ornamentum"))
        {
            for (final List<ItemStack> slot : recipe.getInputs())
            {
                // when teaching there should only be one stack in each slot; for JEI there may be more.
                // any one compatible ingredient in any slot makes the whole recipe acceptable.
                for (final ItemStack ingredientStack : slot)
                {
                    if (!ItemStackUtils.isEmpty(stack) && validator.test(ingredientStack).orElse(false))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // override getIngredientValidator() to limit compatible ingredients

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);
        checkForLearnableRequests(colony);
    }

    @Override
    public void serializeToView(@NotNull FriendlyByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeVarInt(learnableRequests.size());
        for (final IToken<?> request : learnableRequests)
        {
            StandardFactoryController.getInstance().serialize(buf, request);
        }
    }

    private void checkForLearnableRequests(@NotNull final IColony colony)
    {
        final Set<IToken<?>> requests = new HashSet<>();
        final IRequestManager requestManager = colony.getRequestManager();

        final OptionalPredicate<ItemStack> validator = getIngredientValidator();
        final IPlayerRequestResolver resolver = requestManager.getPlayerResolver();
        final IRetryingRequestResolver retryingRequestResolver = requestManager.getRetryingRequestResolver();

        final Set<IToken<?>> inspected = new HashSet<>();
        final Set<IToken<?>> requestTokens = new HashSet<>();
        requestTokens.addAll(resolver.getAllAssignedRequests());
        requestTokens.addAll(retryingRequestResolver.getAllAssignedRequests());

        for (final IToken<?> token : requestTokens)
        {
            IRequest<?> request = requestManager.getRequestForToken(token);

            while (request != null)
            {
                if (inspected.contains(request.getId()))
                {
                    break;
                }
                inspected.add(request.getId());

                if (isLearnableRequest(request, validator))
                {
                    requests.add(request.getId());
                }

                //noinspection ConstantConditions
                request = request.hasParent() ? requestManager.getRequestForToken(request.getParent()) : null;
            }
        }

        if (!learnableRequests.equals(requests))
        {
            learnableRequests.clear();
            learnableRequests.addAll(requests);
            markDirty();
        }
    }

    private boolean isLearnableRequest(@NotNull final IRequest<?> request,
                                       @NotNull final OptionalPredicate<ItemStack> validator)
    {
        final ItemStack stack = DomumOrnamentumUtils.getRequestedStack(request);
        if (stack.isEmpty()) return false;

        final MaterialTextureData textureData = DomumOrnamentumUtils.getTextureData(stack);
        if (textureData.isEmpty()) return false;

        for (final Block block : textureData.getTexturedComponents().values())
        {
            if (validator.test(new ItemStack(block)).orElse(false))
            {
                return true;
            }
        }

        return false;
    }
}
