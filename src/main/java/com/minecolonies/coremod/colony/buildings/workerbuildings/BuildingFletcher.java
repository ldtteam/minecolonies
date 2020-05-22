package com.minecolonies.coremod.colony.buildings.workerbuildings;

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
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.JobFletcher;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the fletcher building.
 */
public class BuildingFletcher extends AbstractBuildingCrafter
{
    /**
     * Description string of the building.
     */
    private static final String FLETCHER = "fletcher";

    /**
     * Instantiates a new fletcher building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingFletcher(final IColony c, final BlockPos l)
    {
        super(c, l);
        if (recipes.isEmpty())
        {
            final IRecipeStorage storage = StandardFactoryController.getInstance().getNewInstance(
              TypeConstants.RECIPE,
              StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN),
              ImmutableList.of(new ItemStack(Items.WHITE_WOOL, 1)),
                      1,
                      new ItemStack(Items.STRING, 4),
                      Blocks.AIR);
            recipes.add(IColonyManager.getInstance().getRecipeManager().checkOrAddRecipe(storage));
        }
    }


    @NotNull
    @Override
    public String getSchematicName()
    {
        return FLETCHER;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public IJob createJob(final ICitizenData citizen)
    {
        return new JobFletcher(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return FLETCHER;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Dexterity;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Creativity;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken token)
    {
        ResourceLocation builder_products = new ResourceLocation("minecolonies", this.getJobName().toLowerCase().concat("_product"));
        ResourceLocation builder_ingredients = new ResourceLocation("minecolonies", this.getJobName().toLowerCase().concat("_ingredient"));
        ResourceLocation builder_products_excluded = new ResourceLocation("minecolonies", this.getJobName().toLowerCase().concat("_product_excluded"));
        ResourceLocation builder_ingredients_excluded = new ResourceLocation("minecolonies", this.getJobName().toLowerCase().concat("_ingredient_excluded"));

        if(!super.canRecipeBeAdded(token))
        {
            return false;
        }

        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);
        if(storage == null)
        {
            return false;
        }


        // Check against excluded products
        if (ItemTags.getCollection().getOrCreate(builder_products_excluded).contains(storage.getPrimaryOutput().getItem())) {
            return false;
        }

        // Check against excluded ingredients
        for (final ItemStack stack : storage.getInput()) {
            if (ItemTags.getCollection().getOrCreate(builder_ingredients_excluded).contains(stack.getItem())) {
                return false;
            }
        }

        // Check against allowed products
        if (ItemTags.getCollection().getOrCreate(builder_products).contains(storage.getPrimaryOutput().getItem())) {
            return true;
        }

        // Check against allowed ingredients
        for (final ItemStack stack : storage.getInput()) {
            if (ItemTags.getCollection().getOrCreate(builder_ingredients).contains(stack.getItem())) {
                return true;
            }
        }

        // Additional recipe rules

        boolean hasValidItem = false;

        if (storage.getPrimaryOutput().getItem() instanceof ArrowItem
              || (storage.getPrimaryOutput().getItem() instanceof DyeableArmorItem
                    && ((DyeableArmorItem) storage.getPrimaryOutput().getItem()).getArmorMaterial() == ArmorMaterial.LEATHER))
        {
            return true;
        }


        // End Additional recipe rules

        return false;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.fletcher;
    }

    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        final UnlockBuildingResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect("Fletcher", UnlockBuildingResearchEffect.class);
        if (effect == null)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.havetounlock"));
            return;
        }
        super.requestUpgrade(player, builder);
    }

    /**
     * Fletcher View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {

        /**
         * Instantiate the fletcher view.
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
            return new WindowHutWorkerPlaceholder<>(this, FLETCHER);
        }
    }
}
