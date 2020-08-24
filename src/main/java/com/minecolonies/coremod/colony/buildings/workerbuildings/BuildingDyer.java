package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.client.gui.WindowHutDyer;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingSmelterCrafter;
import com.minecolonies.coremod.colony.jobs.JobDyer;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the dyer building.
 */
public class BuildingDyer extends AbstractBuildingSmelterCrafter
{
    /**
     * Description string of the building.
     */
    private static final String DYER = "dyer";

    /**
     * Instantiates a new dyer building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingDyer(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return DYER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobDyer(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return DYER;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Creativity;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Dexterity;
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
        return isRecipeAllowed.orElse(false);
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return true;
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

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.dyer;
    }

    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        final UnlockBuildingResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect("Dyer", UnlockBuildingResearchEffect.class);
        if (effect == null)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.havetounlock"));
            return;
        }
        super.requestUpgrade(player, builder);
    }

    @Override
    public IRecipeStorage getFirstRecipe(Predicate<ItemStack> stackPredicate)
    {
        IRecipeStorage recipe = super.getFirstRecipe(stackPredicate);

        if(recipe == null && stackPredicate.test(new ItemStack(Items.WHITE_WOOL)))
        {
            final HashMap<ItemStorage, Integer> inventoryCounts = new HashMap<>();

            if (!colony.getBuildingManager().hasWarehouse())
            {
                return null;
            }
            
            final List<ItemStorage> woolItems = ItemTags.WOOL.getAllElements().stream()
                                                        .filter(item -> !item.equals(Items.WHITE_WOOL))
                                                        .map(i -> new ItemStorage(new ItemStack(i))).collect(Collectors.toList());

            for(ItemStorage color : woolItems)
            {
                for(IBuilding wareHouse: colony.getBuildingManager().getWareHouses())
                {
                    final int colorCount = InventoryUtils.hasBuildingEnoughElseCount(wareHouse, color, 1);
                    inventoryCounts.replace(color, inventoryCounts.getOrDefault(color, 0) + colorCount);
                }
            }

            ItemStorage woolToUse = inventoryCounts.entrySet().stream().min(java.util.Map.Entry.comparingByValue(Comparator.reverseOrder())).get().getKey();

            recipe = StandardFactoryController.getInstance().getNewInstance(
                TypeConstants.RECIPE,
                StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                ImmutableList.of(woolToUse.getItemStack(), new ItemStack(Items.WHITE_DYE, 1)),
                1,
                new ItemStack(Items.WHITE_WOOL, 1),
                Blocks.AIR);
        }
        return recipe;
    }

    @Override
    public IRecipeStorage getFirstFullFillableRecipe(Predicate<ItemStack> stackPredicate, int count)
    {
        IRecipeStorage recipe =  super.getFirstFullFillableRecipe(stackPredicate, count);

        if(recipe == null)
        {
            final IRecipeStorage storage = getFirstRecipe(stackPredicate);
            if (storage != null && stackPredicate.test(storage.getPrimaryOutput()))
            {
                final List<IItemHandler> handlers = getHandlers();
                if (storage.canFullFillRecipe(count, handlers.toArray(new IItemHandler[0])))
                {
                    return storage;
                }
            }
        }
        return recipe;        
    }

    /**
     * Dyer View.
     */
    public static class View extends AbstractBuildingSmelterCrafter.View
    {

        /**
         * Instantiate the dyer view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutDyer(this);
        }
    }
}
