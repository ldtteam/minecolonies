package com.minecolonies.coremod.compatibility.jei;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for a JEI recipe category that displays a Minecolonies citizen based on a job.
 * @param <T> The recipe type.
 */
public abstract class JobBasedRecipeCategory<T> implements IRecipeCategory<T>
{
    protected static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/jei_recipe.png");
    @NotNull protected final IJob<?> job;
    @NotNull private final ResourceLocation uid;
    @NotNull private final ItemStack catalyst;
    @NotNull private final IDrawableStatic background;
    @NotNull private final IDrawable icon;
    @NotNull protected final IDrawableStatic slot;
    @NotNull protected final IDrawableStatic chanceSlot;
    @NotNull private final EntityCitizen citizen;
    @NotNull private final List<ITextProperties> description;
    @NotNull private final LoadingCache<T, List<InfoBlock>> infoBlocksCache;

    protected static final int WIDTH = 167;
    protected static final int HEIGHT = 120;
    protected static final int CITIZEN_X = 2;
    protected static final int CITIZEN_Y = 46;
    protected static final int CITIZEN_W = 47;
    protected static final int CITIZEN_H = 71;

    protected JobBasedRecipeCategory(@NotNull final IJob<?> job,
                                     @NotNull final ResourceLocation uid,
                                     @NotNull final ItemStack icon,
                                     @NotNull final IGuiHelper guiHelper)
    {
        this.job = job;
        this.uid = uid;
        this.catalyst = icon;

        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(icon);
        this.slot = guiHelper.getSlotDrawable();
        this.chanceSlot = guiHelper.createDrawable(TEXTURE, 0, 121, 18, 18);

        this.citizen = createCitizenWithJob(this.job);

        this.description = wordWrap(breakLines(translateDescription(
                TranslationConstants.PARTIAL_JEI_INFO +
                        this.job.getJobRegistryEntry().getRegistryName().getPath())));

        this.infoBlocksCache = CacheBuilder.newBuilder()
                .maximumSize(6)
                .build(new CacheLoader<T, List<InfoBlock>>()
                {
                    @NotNull
                    @Override
                    public List<InfoBlock> load(@NotNull final T key)
                    {
                        return calculateInfoBlocks(key);
                    }
                });
    }

    @NotNull
    public ItemStack getCatalyst()
    {
        return this.catalyst;
    }

    @NotNull
    protected static ItemStack getCatalyst(@NotNull final BuildingEntry building)
    {
        return new ItemStack(building.getBuildingBlock());
    }

    @NotNull
    public IJob<?> getJob()
    {
        return this.job;
    }

    @NotNull
    @Override
    public ResourceLocation getUid()
    {
        return this.uid;
    }

    @NotNull
    @Override
    public String getTitle()
    {
        return getTitleAsTextComponent().getString();
    }

    @NotNull
    @Override
    public ITextComponent getTitleAsTextComponent()
    {
        return new TranslationTextComponent(this.job.getJobRegistryEntry().getTranslationKey());
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

    public Collection<?> findRecipes(@NotNull final Map<CraftingType, List<IGenericRecipe>> vanilla)
    {
        return Collections.emptyList();
    }

    @Override
    public void draw(@NotNull final T recipe, @NotNull final MatrixStack matrixStack, final double mouseX, final double mouseY)
    {
        final float scale = CITIZEN_H / 2.4f;
        final int citizen_cx = CITIZEN_X + (CITIZEN_W / 2);
        final int citizen_cy = CITIZEN_Y + (CITIZEN_H / 2);
        final int citizen_by = CITIZEN_Y + CITIZEN_H;
        final int offsetY = 4;
        final float headYaw = (float) Math.atan((citizen_cx - mouseX) / 40.0F) * 40.0F;
        final float yaw = (float) Math.atan((citizen_cx - mouseX) / 40.0F) * 20.0F;
        final float pitch = (float) Math.atan((citizen_cy - offsetY - mouseY) / 40.0F) * 20.0F;
        RenderHelper.scissor(matrixStack, CITIZEN_X, CITIZEN_Y, CITIZEN_W, CITIZEN_H);
        RenderHelper.renderEntity(matrixStack, citizen_cx, citizen_by - offsetY, scale, headYaw, yaw, pitch, this.citizen);
        RenderHelper.stopScissor();

        int y = 0;
        final Minecraft mc = Minecraft.getInstance();
        for (final ITextProperties line : this.description)
        {
            final int x = 0;
            mc.font.draw(matrixStack, LanguageMap.getInstance().getVisualOrder(line), x, y, TextFormatting.BLACK.getColor());
            y += mc.font.lineHeight + 2;
        }

        for (final InfoBlock block : this.infoBlocksCache.getUnchecked(recipe))
        {
            mc.font.drawShadow(matrixStack, block.text, block.bounds.getX(), block.bounds.getY(), TextFormatting.YELLOW.getColor());
        }
    }

    @NotNull
    @Override
    public List<ITextComponent> getTooltipStrings(@NotNull final T recipe, final double mouseX, final double mouseY)
    {
        final List<ITextComponent> tooltips = new ArrayList<>();

        for (final InfoBlock block : this.infoBlocksCache.getUnchecked(recipe))
        {
            if (block.tip == null) continue;
            if (block.bounds.contains((int) mouseX, (int) mouseY))
            {
                tooltips.add(new StringTextComponent(block.tip));
            }
        }

        return tooltips;
    }

    @NotNull
    private List<InfoBlock> calculateInfoBlocks(@NotNull T recipe)
    {
        final List<ITextComponent> lines = generateInfoBlocks(recipe);

        final Minecraft mc = Minecraft.getInstance();
        final List<InfoBlock> result = new ArrayList<>();
        int y = CITIZEN_Y;
        for (final ITextComponent line : lines)
        {
            final String text = line.getString();
            final int width = (int) mc.font.getSplitter().stringWidth(text);
            final int height = mc.font.lineHeight;
            final int x = WIDTH - width;
            String tip = null;
            if (line instanceof TranslationTextComponent)
            {
                final String key = ((TranslationTextComponent) line).getKey() + ".tip";
                if (I18n.exists(key))
                {
                    tip = (new TranslationTextComponent(key, ((TranslationTextComponent) line).getArgs())).getString();
                }
            }
            result.add(new InfoBlock(text, tip, new Rectangle2d(x, y, width, height)));
            y += height + 2;
        }
        return result;
    }

    @NotNull
    protected abstract List<ITextComponent> generateInfoBlocks(@NotNull T recipe);

    private static class InfoBlock
    {
        public InfoBlock(final String text, final String tip, final Rectangle2d bounds)
        {
            this.text = text;
            this.tip = tip;
            this.bounds = bounds;
        }

        public final String text;
        public final String tip;
        public final Rectangle2d bounds;
    }

    @NotNull
    private static EntityCitizen createCitizenWithJob(@NotNull final IJob<?> job)
    {
        final EntityCitizen citizen = new EntityCitizen(ModEntities.CITIZEN, Minecraft.getInstance().level);
        citizen.setFemale(citizen.getRandom().nextBoolean());
        citizen.setTextureId(citizen.getRandom().nextInt(255));
        citizen.getEntityData().set(EntityCitizen.DATA_TEXTURE_SUFFIX, CitizenData.SUFFIXES.get(citizen.getRandom().nextInt(CitizenData.SUFFIXES.size())));
        citizen.setModelId(job.getModel());
        return citizen;
    }

    @NotNull
    private static List<ITextProperties> translateDescription(@NotNull final String... keys)
    {
        return Arrays.stream(keys).map(TranslationTextComponent::new).collect(Collectors.toList());
    }

    @NotNull
    private static List<ITextProperties> breakLines(@NotNull final List<ITextProperties> input)
    {
        final List<ITextProperties> lines = new ArrayList<>();
        for (final ITextProperties component : input)
        {
            final Optional<String[]> expanded = component.visit(line -> Optional.of(line.split("\\\\n")));
            expanded.ifPresent(e -> lines.addAll(Arrays.stream(e).map(StringTextComponent::new).collect(Collectors.toList())));
        }
        return lines;
    }

    @NotNull
    private static List<ITextProperties> wordWrap(@NotNull final List<ITextProperties> input)
    {
        final Minecraft mc = Minecraft.getInstance();
        final List<ITextProperties> lines = new ArrayList<>();
        for (final ITextProperties component : input)
        {
            lines.addAll(mc.font.getSplitter().splitLines(component, WIDTH, Style.EMPTY));
        }
        return lines;
    }

    protected static class RecipeIdTooltipCallback implements ITooltipCallback<ItemStack>
    {
        private final int slot;
        private final ResourceLocation id;
        private final IModIdHelper modIdHelper;

        public RecipeIdTooltipCallback(final int slot, final ResourceLocation id, IModIdHelper modIdHelper)
        {
            this.slot = slot;
            this.id = id;
            this.modIdHelper = modIdHelper;
        }

        @Override
        public void onTooltip(final int slotIndex,
                              final boolean input,
                              @NotNull final ItemStack ingredient,
                              @NotNull final List<ITextComponent> tooltip)
        {
            if (slotIndex != slot) return;

            if (modIdHelper.isDisplayingModNameEnabled())
            {
                final String recipeModId = id.getNamespace();
                final String ingredientModId = ingredient.getItem().getRegistryName().getNamespace();
                if (!recipeModId.equals(ingredientModId))
                {
                    final String modName = modIdHelper.getFormattedModNameForModId(recipeModId);
                    final TranslationTextComponent recipeBy = new TranslationTextComponent("jei.tooltip.recipe.by", modName);
                    tooltip.add(recipeBy.withStyle(TextFormatting.GRAY));
                }
            }

            final boolean showAdvanced = Minecraft.getInstance().options.advancedItemTooltips || Screen.hasShiftDown();
            if (showAdvanced)
            {
                final TranslationTextComponent recipeId = new TranslationTextComponent("jei.tooltip.recipe.id", id.toString());
                tooltip.add(recipeId.withStyle(TextFormatting.DARK_GRAY));
            }
        }
    }

    protected static class LootTableTooltipCallback implements ITooltipCallback<ItemStack>
    {
        private final int firstSlot;
        private final List<LootTableAnalyzer.LootDrop> drops;
        private final ResourceLocation id;

        public LootTableTooltipCallback(final int firstSlot, final List<LootTableAnalyzer.LootDrop> drops, final ResourceLocation id)
        {
            this.firstSlot = firstSlot;
            this.drops = drops;
            this.id = id;
        }

        @Override
        public void onTooltip(final int slot, final boolean input, @NotNull final ItemStack stack, @NotNull final List<ITextComponent> tooltip)
        {
            final int index = slot - this.firstSlot;
            if (index >= 0 && index < this.drops.size())
            {
                final LootTableAnalyzer.LootDrop drop = this.drops.get(index);
                final String key = TranslationConstants.PARTIAL_JEI_INFO +
                        (drop.getQuality() < 0 ? "chancenegskill.tip" : drop.getQuality() > 0 ? "chanceskill.tip" : "chance.tip");
                final float probability = drop.getProbability() * 100;

                if (probability >= 1)
                {
                    tooltip.add(new TranslationTextComponent(key,
                            Math.round(probability)));
                }
                else
                {
                    tooltip.add(new TranslationTextComponent(key,
                            Math.round(probability * 100) / 100f));
                }

                if (drop.getConditional())
                {
                    tooltip.add(new TranslationTextComponent(TranslationConstants.PARTIAL_JEI_INFO + "conditions.tip"));
                }

                final boolean showAdvanced = Minecraft.getInstance().options.advancedItemTooltips || Screen.hasShiftDown();
                if (showAdvanced)
                {
                    final TranslationTextComponent recipeId = new TranslationTextComponent("com.minecolonies.coremod.jei.loottableid", id.toString());
                    tooltip.add(recipeId.withStyle(TextFormatting.DARK_GRAY));
                }
            }
        }
    }
}
