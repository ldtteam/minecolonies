package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.client.gui.WindowHutBaker;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingSmelterCrafter;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.JobBaker;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PrivateWorkerCraftingRequestResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingProductionResolver;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import com.minecolonies.coremod.util.FurnaceRecipes;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Building for the bakery.
 */
public class BuildingBaker extends AbstractBuildingSmelterCrafter
{
    /**
     * General bakery description key.
     */
    private static final String BAKER = "baker";

    /**
     * Max hut level of the bakery.
     */
    private static final int BAKER_HUT_MAX_LEVEL = 5;

    /**
     * Always try to keep at least 2 stacks of recipe inputs in the inventory and in the worker chest.
     */
    private static final int RECIPE_INPUT_HOLD = 128;

    /**
     * Constructor for the bakery building.
     *
     * @param c Colony the building is in.
     * @param l Location of the building.
     */
    public BuildingBaker(final IColony c, final BlockPos l)
    {
        super(c, l);
        for (final IToken<?> token : getRecipes())
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
            for (final ItemStorage itemStorage : storage.getCleanedInput())
            {
                final ItemStack stack = itemStorage.getItemStack();
                keepX.put(stack::isItemEqual, new Tuple<>(RECIPE_INPUT_HOLD, false));
            }
        }
    }

    /**
     * Gets the name of the schematic.
     *
     * @return Baker schematic name.
     */
    @NotNull
    @Override
    public String getSchematicName()
    {
        return BAKER;
    }

    /**
     * Gets the max level of the bakery's hut.
     *
     * @return The max level of the bakery's hut.
     */
    @Override
    public int getMaxBuildingLevel()
    {
        return BAKER_HUT_MAX_LEVEL;
    }

    @Override
    protected boolean keepFood()
    {
        return false;
    }

    /**
     * Create a Baker job.
     *
     * @param citizen the citizen to take the job.
     * @return The new Baker job.
     */
    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobBaker(citizen);
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        final Collection<IRequestResolver<?>> supers =
          super.createResolvers().stream()
            .filter(r -> !(r instanceof PrivateWorkerCraftingProductionResolver || r instanceof PrivateWorkerCraftingRequestResolver))
            .collect(Collectors.toList());
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();

        builder.addAll(supers);
        builder.add(new PublicWorkerCraftingRequestResolver(getRequester().getLocation(),
          getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));
        builder.add(new PublicWorkerCraftingProductionResolver(getRequester().getLocation(),
          getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));

        return builder.build();
    }

    @Override
    public void openCraftingContainer(final ServerPlayerEntity player)
    {
        NetworkHooks.openGui(player, new INamedContainerProvider()
        {
            @NotNull
            @Override
            public ITextComponent getDisplayName()
            {
                return new StringTextComponent("Crafting GUI");
            }

            @NotNull
            @Override
            public Container createMenu(final int id, @NotNull final PlayerInventory inv, @NotNull final PlayerEntity player)
            {
                return new ContainerCrafting(id, inv, canCraftComplexRecipes(), getID());
            }
        }, buffer -> new PacketBuffer(buffer.writeBoolean(canCraftComplexRecipes())).writeBlockPos(getID()));
    }

    /**
     * The name of the bakery's job.
     *
     * @return The name of the bakery's job.
     */
    @NotNull
    @Override
    public String getJobName()
    {
        return BAKER;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken<?> token)
    {

        Optional<Boolean> isRecipeAllowed;

        if (!super.canRecipeBeAdded(token))
        {
            return false;
        }

        isRecipeAllowed = super.canRecipeBeAddedBasedOnTags(token);
        if (isRecipeAllowed.isPresent())
        {
            return isRecipeAllowed.get();
        }
        else
        {
            // Additional recipe rules

            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

            boolean hasWheat = false;
            for (final ItemStorage input : storage.getCleanedInput())
            {
                if (Tags.Items.CROPS_WHEAT.contains(input.getItemStack().getItem()))
                {
                    hasWheat = true;
                }
            }

            return hasWheat && ItemStackUtils.ISFOOD.test(storage.getPrimaryOutput());

            // End Additional recipe rules
        }
    }

    @Override
    public boolean addRecipe(final IToken<?> token)
    {
        final boolean recipeAdded = super.addRecipe(token);

        if(recipeAdded)
        {
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

            ItemStack smeltResult = FurnaceRecipes.getInstance().getSmeltingResult(storage.getPrimaryOutput());

            if(smeltResult != null)
            {
                final IRecipeStorage smeltingRecipe =  StandardFactoryController.getInstance().getNewInstance(
                    TypeConstants.RECIPE,
                    StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                    ImmutableList.of(storage.getPrimaryOutput().copy()),
                    1,
                    smeltResult,
                    Blocks.FURNACE);
                    addRecipeToList(IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(smeltingRecipe));
            }
        }

        return recipeAdded;
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return true;
    }

    @Override
    public boolean isRecipeAlterationAllowed()
    {
        return getBuildingLevel() >= 3;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Knowledge;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Dexterity;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.bakery;
    }

    @Override
    public boolean canEat(final ItemStack stack)
    {
        if (stack.getItem() == Items.WHEAT)
        {
            return false;
        }
        return super.canEat(stack);
    }

    /**
     * The client view for the bakery building.
     */
    public static class View extends AbstractBuildingWorker.View
    {
        /**
         * The client view constructor for the bakery building.
         *
         * @param c The ColonyView the building is in.
         * @param l The location of the building.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        /**
         * Creates a new window for the building.
         *
         * @return A BlockOut window.
         */
        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutBaker(this);
        }
    }
}
