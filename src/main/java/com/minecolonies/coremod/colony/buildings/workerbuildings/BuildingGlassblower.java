package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.inventory.container.ContainerCrafting;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingSmelterCrafter;
import com.minecolonies.coremod.colony.jobs.JobGlassblower;
import com.minecolonies.coremod.util.FurnaceRecipes;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class of the glassblower building.
 */
public class BuildingGlassblower extends AbstractBuildingSmelterCrafter
{
    /**
     * Description string of the building.
     */
    private static final String GLASS_BLOWER = "glassblower";

    /**
     * Instantiates a new stone smeltery building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingGlassblower(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return GLASS_BLOWER;
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
        return new JobGlassblower(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return GLASS_BLOWER;
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
        return Skill.Focus;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken<?> token)
    {
        Optional<Boolean> isRecipeAllowed;

        if (!super.canRecipeBeAdded(token))
        {
            return false;
        }

        checkForWorkerSpecificRecipes();

        isRecipeAllowed = super.canRecipeBeAddedBasedOnTags(token);
        if (isRecipeAllowed.isPresent())
        {
            return isRecipeAllowed.get();
        }

        return false;
    }

    @Override
    public void onColonyTick(@NotNull final IColony colony)
    {
        super.onColonyTick(colony);
    }

    @Override
    public void checkForWorkerSpecificRecipes()
    {
        super.checkForWorkerSpecificRecipes();

        final List<IToken<?>> tokens = new ArrayList<>();
        for (final Item item : Tags.Items.SAND.getAllElements())
        {
            final ItemStack stack = new ItemStack(item);
            final ItemStack output = FurnaceRecipes.getInstance().getSmeltingResult(stack);
            if (Tags.Items.GLASS.contains(output.getItem()))
            {
                final List<ItemStorage> list = new ArrayList<>();
                list.add(new ItemStorage(stack));

                final IRecipeStorage storage = StandardFactoryController.getInstance().getNewInstance(
                  TypeConstants.RECIPE,
                  StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
                  list,
                  1,
                  output,
                  Blocks.FURNACE);
                final IToken<?> token = IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(storage);
                if (recipes.contains(token))
                {
                    return;
                }
                tokens.add(token);
            }
        }
        recipes.addAll(tokens);
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
                return new ContainerCrafting(id, inv, canCraftComplexRecipes(), getID());
            }
        }, buffer -> new PacketBuffer(buffer.writeBoolean(canCraftComplexRecipes())).writeBlockPos(getID()));
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.glassblower;
    }

    /**
     * Stone smeltery View.
     */
    public static class View extends AbstractBuildingSmelterCrafter.View
    {
        /**
         * Instantiate the stone smeltery view.
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
            return new WindowHutWorkerModulePlaceholder<>(this, GLASS_BLOWER);
        }
    }
}
