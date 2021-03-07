package com.minecolonies.coremod.compatibility.jei;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.GenericRecipe;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"PublicMethodWithoutLogging", "MethodParameterOfConcreteClass", "ClassWithoutLogger"})
@OnlyIn(Dist.CLIENT)
public class GenericRecipeCategory extends JobBasedRecipeCategory<IGenericRecipe>
{
    public GenericRecipeCategory(@NotNull final BuildingEntry building, @NotNull final IJob<?> job,
                                 @NotNull final ICraftingBuildingModule crafting, @NotNull final IGuiHelper guiHelper)
    {
        super(job, getCatalyst(building), guiHelper);

        this.building = building;
        this.crafting = crafting;
        this.arrow = guiHelper.createDrawable(TEXTURE, 20, 121, 24, 18);

        outputSlotX = CITIZEN_X + CITIZEN_W + 2 + (30 - this.slot.getWidth()) / 2;
        outputSlotY = CITIZEN_Y + CITIZEN_H + 1 - this.slot.getHeight();
    }

    @NotNull private final BuildingEntry building;
    @NotNull private final ICraftingBuildingModule crafting;
    @NotNull private final IDrawableStatic arrow;

    private static final int INPUT_SLOT_X = CITIZEN_X + CITIZEN_W + 32;
    private static final int INPUT_SLOT_W = WIDTH - INPUT_SLOT_X;
    private final int outputSlotX;
    private final int outputSlotY;

    @NotNull
    private static ItemStack getCatalyst(@NotNull final BuildingEntry building)
    {
        return new ItemStack(building.getBuildingBlock().asItem());
    }

    public ItemStack getCatalyst() {
        return getCatalyst(this.building);
    }

    @NotNull
    @Override
    public Class<? extends IGenericRecipe> getRecipeClass() {
        return IGenericRecipe.class;
    }

    @Override
    public void setIngredients(@NotNull final IGenericRecipe recipe, @NotNull final IIngredients ingredients)
    {
        ingredients.setInputLists(VanillaTypes.ITEM, recipe.getInputs());
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getAllMultiOutputs()));
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayout layout, @NotNull final IGenericRecipe recipe, @NotNull final IIngredients ingredients)
    {
        final IGuiItemStackGroup guiItemStacks = layout.getItemStacks();
        final List<List<ItemStack>> inputs = recipe.getInputs();

        int x = outputSlotX;
        int y = outputSlotY;
        int slot = 0;
        guiItemStacks.init(slot, false, x, y);
        guiItemStacks.setBackground(slot, this.slot);
        guiItemStacks.set(slot, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
        x += this.slot.getWidth();
        ++slot;

        for (final ItemStack extra : recipe.getAdditionalOutputs())
        {
            guiItemStacks.init(slot, false, x, y);
            guiItemStacks.setBackground(slot, this.slot);
            guiItemStacks.set(slot, extra);
            x += this.slot.getWidth();
            ++slot;
        }

        if (recipe.getLootTable() != null)
        {
            try
            {
                final JsonObject lootTableJson = LootTableAnalyzer.getLootTableJson(recipe.getLootTable());
                final List<LootTableAnalyzer.LootDrop> drops = LootTableAnalyzer.toDrops(lootTableJson);

                guiItemStacks.addTooltipCallback(new LootTableTooltipCallback(slot, drops));
                for (final LootTableAnalyzer.LootDrop drop : drops)
                {
                    guiItemStacks.init(slot, false, x, y);
                    guiItemStacks.setBackground(slot, this.chanceSlot);
                    guiItemStacks.set(slot, drop.getItemStack());
                    x += this.chanceSlot.getWidth();
                    ++slot;
                }
            }
            catch (final JsonParseException ex)
            {
                Log.getLogger().error(String.format("Failed to parse loot table from %s",
                        recipe.getLootTable().toString()), ex);
            }
        }

        if (!inputs.isEmpty())
        {
            final int initialInputColumns = INPUT_SLOT_W / this.slot.getWidth();
            final int inputRows = (inputs.size() + initialInputColumns - 1) / initialInputColumns;
            final int inputColumns = (inputs.size() + inputRows - 1) / inputRows;

            x = INPUT_SLOT_X;
            y = CITIZEN_Y + (CITIZEN_H - inputRows * this.slot.getHeight()) / 2;
            int c = 0;
            for (int i = 0; i < inputs.size(); ++i) {
                guiItemStacks.init(slot, true, x, y);
                guiItemStacks.set(slot, inputs.get(i));
                ++slot;
                if (++c >= inputColumns)
                {
                    c = 0;
                    x = INPUT_SLOT_X;
                    y += this.slot.getHeight();
                } else {
                    x += this.slot.getWidth();
                }
            }
        }

        this.infoBlocks.clear();
        this.infoBlocks.addAll(calculateInfoBlocks(recipe.getRestrictions()));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void draw(@NotNull final IGenericRecipe recipe, @NotNull final MatrixStack matrixStack, final double mouseX, final double mouseY)
    {
        super.draw(recipe, matrixStack, mouseX, mouseY);

        this.arrow.draw(matrixStack, CITIZEN_X + CITIZEN_W + 4, CITIZEN_Y + (CITIZEN_H - this.arrow.getHeight()) / 2);

        if (recipe.getIntermediate() != Blocks.AIR)
        {
            final BlockState block = recipe.getIntermediate().getDefaultState();
            RenderHelper.renderBlock(matrixStack, block, outputSlotX + 8, CITIZEN_Y + 6, 100, -30F, 30F, 16F);
        }
    }

    @Override
    public List<ITextComponent> getTooltipStrings(@NotNull final IGenericRecipe recipe, final double mouseX, final double mouseY)
    {
        final List<ITextComponent> tooltips = new ArrayList<>();

        tooltips.addAll(super.getTooltipStrings(recipe, mouseX, mouseY));

        if (recipe.getIntermediate() != Blocks.AIR)
        {
            if (new Rectangle2d(CITIZEN_X + CITIZEN_W + 4, CITIZEN_Y - 2, 24, 24).contains((int) mouseX, (int) mouseY))
            {
                tooltips.add(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + "intermediate.tip", recipe.getIntermediate().getTranslatedName()));
            }
        }

        return tooltips;
    }

    @NotNull
    public List<IGenericRecipe> findRecipes()
    {
        final List<IGenericRecipe> recipes = new ArrayList<>();
        final RecipeManager recipeManager = Minecraft.getInstance().world.getRecipeManager();

        // vanilla shaped and shapeless crafting recipes
        if (this.crafting.canLearnCraftingRecipes())
        {
            for (final IRecipe<CraftingInventory> recipe : recipeManager.getRecipes(IRecipeType.CRAFTING).values())
            {
                if (!this.crafting.canLearnLargeRecipes() && !recipe.canFit(2, 2)) continue;

                final IGenericRecipe genericRecipe = GenericRecipeFactory.create(recipe);
                if (this.crafting.isRecipeCompatible(genericRecipe))
                {
                    recipes.add(genericRecipe);
                }
            }
        }

        // vanilla furnace recipes (do we want to check smoking and blasting too?)
        if (this.crafting.canLearnFurnaceRecipes())
        {
            for (final IRecipe<IInventory> recipe : recipeManager.getRecipes(IRecipeType.SMELTING).values())
            {
                final IGenericRecipe genericRecipe = GenericRecipe.of(recipe);
                if (this.crafting.isRecipeCompatible(genericRecipe))
                {
                    recipes.add(genericRecipe);
                }
            }
        }

        // custom MineColonies additional recipes
        final String craftingJobName = this.job.getJobRegistryEntry().getRegistryName().getPath();
        final Set<CustomRecipe> customRecipes = CustomRecipeManager.getInstance().getRecipes(craftingJobName);
        for (final CustomRecipe newRecipe : customRecipes)
        {
            final IRecipeStorage recipeStorage = newRecipe.getRecipeStorage();
            if (!recipeStorage.getAlternateOutputs().isEmpty())
            {
                // this is a multi-output recipe; assume it replaces a bunch of vanilla
                // recipes we already added above
                recipes.removeIf(r -> ItemStackUtils.compareItemStacksIgnoreStackSize(recipeStorage.getPrimaryOutput(), r.getPrimaryOutput()));
                recipes.removeIf(r -> recipeStorage.getAlternateOutputs().stream()
                        .anyMatch(s -> ItemStackUtils.compareItemStacksIgnoreStackSize(s, r.getPrimaryOutput())));
            }
            recipes.add(GenericRecipeFactory.create(newRecipe, recipeStorage));
        }

        // and even more building-specific recipes generated in code
        for (final IRecipeStorage storage : this.crafting.getAdditionalRecipes(recipeManager))
        {
            recipes.add(GenericRecipe.of(storage));
        }

        // and even more recipes that can't be taught
        recipes.addAll(this.crafting.getPossibleRecipesForDisplayPurposesOnly(this.job));

        return recipes;
    }
}
