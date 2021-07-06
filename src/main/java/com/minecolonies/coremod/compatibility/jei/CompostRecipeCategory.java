package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.blocks.AbstractBlockBarrel;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.blocks.types.BarrelType;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.CompostRecipe;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JEI compost recipe category renderer
 */
@SuppressWarnings("MethodParameterOfConcreteClass")
public class CompostRecipeCategory implements IRecipeCategory<CompostRecipe>
{
    private final String title;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable slot;
    private final ITickTimer timer;

    public CompostRecipeCategory(@NotNull final IGuiHelper guiHelper)
    {
        this.title = I18n.format(ModBlocks.blockBarrel.getTranslationKey());

        this.background = guiHelper.createBlankDrawable(80, 50);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.blockBarrel));
        this.slot = guiHelper.getSlotDrawable();
        this.timer = guiHelper.createTickTimer(60, BarrelType.values().length - 2, false);
    }

    @NotNull
    public static List<CompostRecipe> findRecipes()
    {
        return IColonyManager.getInstance().getCompatibilityManager().getCopyOfCompostRecipes().entrySet().stream()
                .map(entry -> CompostRecipe.individualize(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @NotNull
    @Override
    public ResourceLocation getUid()
    {
        return CompostRecipe.ID;
    }

    @NotNull
    @Override
    public String getTitle()
    {
        return this.title;
    }

    @NotNull
    @Override
    public IDrawable getBackground()
    {
        return this.background;
    }

    @NotNull
    @Override
    public IDrawable getIcon()
    {
        return this.icon;
    }

    @NotNull
    @Override
    public Class<? extends CompostRecipe> getRecipeClass()
    {
        return CompostRecipe.class;
    }

    @Override
    public void setIngredients(@NotNull final CompostRecipe recipe, @NotNull final IIngredients ingredients)
    {
        ingredients.setInputIngredients(recipe.getIngredients());
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
    }

    @Override
    public void setRecipe(@NotNull final IRecipeLayout layout, @NotNull final CompostRecipe recipe, @NotNull final IIngredients ingredients)
    {
        final IGuiItemStackGroup itemStacks = layout.getItemStacks();

        itemStacks.init(0, true, 0, 0);
        itemStacks.init(1, false, 62, 0);
        itemStacks.setBackground(0, this.slot);
        itemStacks.setBackground(1, this.slot);

        itemStacks.set(ingredients);
    }

    @Override
    public void draw(@NotNull final CompostRecipe recipe, @NotNull final MatrixStack matrixStack, final double mouseX, final double mouseY)
    {
        final BarrelType type = BarrelType.byMetadata(this.timer.getValue());
        final BlockState barrel = ModBlocks.blockBarrel.getDefaultState()
                .with(AbstractBlockBarrel.FACING, Direction.SOUTH)
                .with(AbstractBlockBarrel.VARIANT, type);
        RenderHelper.renderBlock(matrixStack, barrel, 40, 20, 100, -30F, 20F, 25F);
    }
}
