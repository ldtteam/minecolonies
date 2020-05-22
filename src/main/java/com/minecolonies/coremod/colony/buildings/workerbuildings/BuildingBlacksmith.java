package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.JobBlacksmith;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;
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
 * Creates a new building for the blacksmith.
 */
public class BuildingBlacksmith extends AbstractBuildingCrafter
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String BLACKSMITH = "blacksmith";

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingBlacksmith(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return BLACKSMITH;
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
        return new JobBlacksmith(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return BLACKSMITH;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Strength;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Focus;
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
        if (ItemTags.getCollection().getOrCreate(builder_products_excluded).contains(storage.getPrimaryOutput().getItem()))
        {
            return false;
        }

        // Check against excluded ingredients
        for (final ItemStack stack : storage.getInput())
        {
            if (ItemTags.getCollection().getOrCreate(builder_ingredients_excluded).contains(stack.getItem()))
            {
                return false;
            }
        }

        // Check against allowed products
        if (ItemTags.getCollection().getOrCreate(builder_products).contains(storage.getPrimaryOutput().getItem()))
        {
            return true;
        }

        // Check against allowed ingredients
        for (final ItemStack stack : storage.getInput())
        {
            if (ItemTags.getCollection().getOrCreate(builder_ingredients).contains(stack.getItem()))
            {
                return true;
            }
        }

        // Additional recipe rules

        final ItemStack output = storage.getPrimaryOutput();
        return output.getItem() instanceof ToolItem ||
                 output.getItem() instanceof SwordItem ||
                 output.getItem() instanceof ArmorItem ||
                 output.getItem() instanceof HoeItem ||
                 output.getItem() instanceof ShieldItem ||
                 Compatibility.isTinkersWeapon(output);

        // End Additional recipe rules

    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.blacksmith;
    }

    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        final UnlockBuildingResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect("Blacksmith", UnlockBuildingResearchEffect.class);
        if (effect == null)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.havetounlock"));
            return;
        }
        super.requestUpgrade(player, builder);
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingCrafter.View
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, BLACKSMITH);
        }
    }
}
