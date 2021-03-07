package com.minecolonies.coremod.compatibility.jei;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.colony.crafting.CustomRecipe;
import com.minecolonies.coremod.colony.crafting.CustomRecipeManager;
import com.minecolonies.coremod.colony.crafting.SifterRecipe;
import com.minecolonies.coremod.colony.jobs.JobSifter;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"MethodParameterOfConcreteClass", "ClassWithoutLogger"})
public class SifterRecipeCategory extends JobBasedRecipeCategory<SifterRecipeCategory.SifterRecipeWrapper>
{
    @SuppressWarnings({"MethodReturnOfConcreteClass", "InstanceVariableOfConcreteClass"})
    public static class SifterRecipeWrapper
    {
        private final SifterRecipe meshRecipe;
        private final CustomRecipe inputRecipe;
        private final List<LootTableAnalyzer.LootDrop> drops;
        private final List<ITextComponent> restrictions;

        public SifterRecipeWrapper(@NotNull final SifterRecipe meshRecipe,
                                   @NotNull final CustomRecipe inputRecipe,
                                   @NotNull final List<LootTableAnalyzer.LootDrop> drops,
                                   @NotNull final List<ITextComponent> restrictions)
        {
            this.meshRecipe = meshRecipe;
            this.inputRecipe = inputRecipe;
            this.drops = Collections.unmodifiableList(drops);
            this.restrictions = Collections.unmodifiableList(restrictions);
        }

        @NotNull public SifterRecipe getMeshRecipe() { return this.meshRecipe; }
        @NotNull public CustomRecipe getInputRecipe() { return this.inputRecipe; }
        @NotNull public List<LootTableAnalyzer.LootDrop> getDrops() { return this.drops; }
        @NotNull public List<ITextComponent> getRestrictions() { return restrictions; }

        @Override
        public String toString()
        {
            return "SifterRecipeWrapper{" +
                    "meshRecipe=" + meshRecipe +
                    ", inputRecipe=" + inputRecipe +
                    '}';
        }
    }

    private final IDrawableStatic meshSlot;

    public SifterRecipeCategory(@NotNull final IGuiHelper guiHelper)
    {
        super(new JobSifter(null), new ItemStack(ModBlocks.blockHutSifter.asItem()), guiHelper);

        this.meshSlot = guiHelper.createDrawable(TEXTURE, 50, 121, 19, 19);
    }

    public static List<SifterRecipeWrapper> findRecipes()
    {
        final Set<SifterRecipe> sifterRecipes = CustomRecipeManager.getInstance().getSifterRecipes();
        final List<SifterRecipeWrapper> recipes = new ArrayList<>();

        for (final SifterRecipe sifterRecipe : sifterRecipes)
        {
            for (final CustomRecipe inputRecipe : sifterRecipe.getInputRecipes())
            {
                try
                {
                    final JsonObject json = LootTableAnalyzer.getLootTableJson(inputRecipe.getRecipeStorage().getLootTable());
                    final List<LootTableAnalyzer.LootDrop> drops = LootTableAnalyzer.toDrops(json);
                    final List<ITextComponent> restrictions = GenericRecipeFactory.calculateRestrictions(inputRecipe);

                    if (!drops.isEmpty())
                    {
                        recipes.add(new SifterRecipeWrapper(sifterRecipe, inputRecipe, drops, restrictions));
                    }
                }
                catch (final JsonParseException ex)
                {
                    Log.getLogger().error(String.format("Failed to parse loot table from %s for %s",
                            inputRecipe.getRecipeStorage().getLootTable().toString(),
                            inputRecipe.getRecipeId().toString()), ex);
                }
            }
        }

        return recipes;
    }

    @NotNull
    @Override
    public Class<? extends SifterRecipeWrapper> getRecipeClass()
    {
        return SifterRecipeWrapper.class;
    }

    @Override
    public void setIngredients(@NotNull final SifterRecipeWrapper recipe, @NotNull final IIngredients ingredients)
    {
        final List<ItemStack> inputs = Stream.of(recipe.getMeshRecipe().getMesh(),
                recipe.getInputRecipe().getRecipeStorage().getCleanedInput().get(0).getItemStack())
                .collect(Collectors.toList());

        ingredients.setInputs(VanillaTypes.ITEM, inputs);
        ingredients.setOutputs(VanillaTypes.ITEM, recipe.getDrops().stream()
                .map(LootTableAnalyzer.LootDrop::getItemStack)
                .collect(Collectors.toList()));
    }

    private static final int SLOTS_X = CITIZEN_X + CITIZEN_W + 4;
    private static final int SLOTS_W = WIDTH - SLOTS_X;

    @Override
    public void setRecipe(@NotNull final IRecipeLayout layout, @NotNull final SifterRecipeWrapper recipe, @NotNull final IIngredients ingredients)
    {
        final IGuiItemStackGroup guiItemStacks = layout.getItemStacks();

        int x = SLOTS_X;
        int y = CITIZEN_Y;
        int slot = 0;
        guiItemStacks.init(slot, false, x, y);
        guiItemStacks.set(slot, ingredients.getInputs(VanillaTypes.ITEM).get(1));
        ++slot;

        x += this.slot.getWidth() + 2;
        guiItemStacks.init(slot, false, x, y);
        guiItemStacks.set(slot, ingredients.getInputs(VanillaTypes.ITEM).get(0));
        ++slot;

        final int initialColumns = SLOTS_W / this.slot.getWidth();
        final int rows = (recipe.getDrops().size() + initialColumns - 1) / initialColumns;
        final int columns = (recipe.getDrops().size() + rows - 1) / rows;
        final int startX = SLOTS_X + (SLOTS_W - (columns * this.slot.getWidth())) / 2;
        x = startX;
        y = CITIZEN_Y + CITIZEN_H - rows * this.slot.getHeight() + 1;
        int c = 0;

        guiItemStacks.addTooltipCallback(new LootTableTooltipCallback(slot, recipe.getDrops()));
        for (final LootTableAnalyzer.LootDrop drop : recipe.getDrops())
        {
            guiItemStacks.init(slot, true, x, y);
            guiItemStacks.setBackground(slot, this.chanceSlot);
            guiItemStacks.set(slot, drop.getItemStack());
            ++slot;
            if (++c >= columns)
            {
                c = 0;
                x = startX;
                y += this.slot.getHeight();
            }
            else
            {
                x += this.slot.getWidth();
            }
        }

        this.infoBlocks.clear();
        this.infoBlocks.addAll(calculateInfoBlocks(recipe.getRestrictions()));
    }

    @Override
    public void draw(@NotNull final SifterRecipeWrapper recipe, @NotNull final MatrixStack matrixStack, final double mouseX, final double mouseY)
    {
        super.draw(recipe, matrixStack, mouseX, mouseY);

        this.meshSlot.draw(matrixStack, SLOTS_X + this.slot.getWidth() + 1, CITIZEN_Y - 1);
    }
}
