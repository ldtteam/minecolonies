package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.crafting.PublicCrafting;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.colony.jobs.AbstractJobCrafter;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Abstract class for all buildings which require a filterable list of allowed items AND can also craft stuff.
 *
 * TODO: The crafter logic is just a copypaste from {@link AbstractBuildingCrafter} to avoid diamond inheritance.
 * This should be fixed at some point.
 */
public abstract class AbstractFilterableListCrafter extends AbstractFilterableListBuilding
{
    /**
     * Extra amount of recipes the crafters can learn.
     */
    private static final int EXTRA_RECIPE_MULTIPLIER = 10;

    /**
     * The constructor of the building.
     *
     * @param c the colony
     * @param l the position
     */
    protected AbstractFilterableListCrafter(@NotNull final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public boolean canBeGathered()
    {
        return super.canBeGathered() &&
                 this.getAssignedCitizen().stream()
                   .map(c -> c.getJob(AbstractJobCrafter.class))
                   .filter(Objects::nonNull)
                   .allMatch(AbstractJobCrafter::hasTask);
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        final Collection<IRequestResolver<?>> supers =
          super.createResolvers().stream()
            .filter(r -> !(r instanceof PrivateWorkerCraftingProductionResolver || r instanceof PrivateWorkerCraftingRequestResolver)).collect(
            Collectors.toList());
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();

        builder.addAll(supers);
        builder.add(new PublicWorkerCraftingRequestResolver(getRequester().getLocation(),
          getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
        builder.add(new PublicWorkerCraftingProductionResolver(getRequester().getLocation(),
          getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));

        return builder.build();
    }

    @Override
    public Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> getRequiredItemsAndAmount()
    {
        final Map<ItemStorage, Tuple<Integer, Boolean>> recipeOutputs = new HashMap<>();
        for (final ICitizenData citizen : getAssignedCitizen())
        {
            if (citizen.getJob() instanceof AbstractJobCrafter)
            {
                final List<IToken<?>> assignedTasks = citizen.getJob(AbstractJobCrafter.class).getAssignedTasks();
                for (final IToken taskToken : assignedTasks)
                {
                    final IRequest<? extends PublicCrafting> request = (IRequest<? extends PublicCrafting>) colony.getRequestManager().getRequestForToken(taskToken);
                    final IRecipeStorage recipeStorage = getFirstRecipe(request.getRequest().getStack());
                    if (recipeStorage != null)
                    {
                        for (final ItemStorage itemStorage : recipeStorage.getCleanedInput())
                        {
                            int amount = itemStorage.getAmount();
                            if (recipeOutputs.containsKey(itemStorage))
                            {
                                amount = recipeOutputs.get(itemStorage).getA() + itemStorage.getAmount();
                            }
                            recipeOutputs.put(itemStorage, new Tuple<>(amount, false));
                        }

                        final ItemStorage output = new ItemStorage(recipeStorage.getPrimaryOutput());
                        if (recipeOutputs.containsKey(output))
                        {
                            output.setAmount(recipeOutputs.get(output).getA() + output.getAmount());
                        }
                        recipeOutputs.put(output, new Tuple<>(output.getAmount(), false));
                    }
                }
            }
        }

        final Map<Predicate<ItemStack>, Tuple<Integer, Boolean>> toKeep = new HashMap<>(keepX);
        toKeep.putAll(recipeOutputs.entrySet().stream().collect(Collectors.toMap(key -> (stack -> stack.isItemEqual(key.getKey().getItemStack())), Map.Entry::getValue)));
        return toKeep;
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return true;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken token)
    {
        return AbstractFilterableListCrafter.canBuildingCanLearnMoreRecipes(getBuildingLevel(), super.getRecipes().size());
    }

    @Override
    public void openCraftingContainer(final ServerPlayerEntity player)
    {
        NetworkHooks.openGui(player, new INamedContainerProvider()
        {
            @Override
            public ITextComponent getDisplayName()
            {
                return new StringTextComponent("Crafting GUI");
            }

            @NotNull
            @Override
            public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
            {
                final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
                buffer.writeBoolean(canCraftComplexRecipes());
                buffer.writeBlockPos(getID());
                return new ContainerCrafting(id, inv, buffer);
            }
        }, buffer -> new PacketBuffer(buffer.writeBoolean(canCraftComplexRecipes())).writeBlockPos(getID()));
    }

    /**
     * Crafter building View.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * Instantiate the crafter view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Check if an additional recipe can be added.
         *
         * @return true if so.
         */
        public boolean canRecipeBeAdded()
        {
            return AbstractFilterableListCrafter.canBuildingCanLearnMoreRecipes(getBuildingLevel(), super.getRecipes().size());
        }
    }

    /**
     * Check if an additional recipe can be added.
     *
     * @param learnedRecipes the learned recipes.
     * @param buildingLevel  the building level.
     * @return true if so.
     */
    public static boolean canBuildingCanLearnMoreRecipes(final int buildingLevel, final int learnedRecipes)
    {
        return (Math.pow(2, buildingLevel) * EXTRA_RECIPE_MULTIPLIER) >= (learnedRecipes + 1);
    }
}
