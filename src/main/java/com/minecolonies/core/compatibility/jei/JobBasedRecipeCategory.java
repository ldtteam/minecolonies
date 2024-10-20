package com.minecolonies.core.compatibility.jei;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ldtteam.blockui.UiRenderMacros;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.crafting.registry.CraftingType;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.colony.CitizenData;
import com.minecolonies.core.colony.crafting.LootTableAnalyzer;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.platform.Lighting;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for a JEI recipe category that displays a Minecolonies citizen based on a job.
 //* @param <T> The recipe type.
 */
public abstract class JobBasedRecipeCategory<T> extends AbstractRecipeCategory<T>
{
    private static final Map<EquipmentTypeEntry, List<ItemStack>> TOOL_CACHE = new HashMap<>();
    protected static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/jei_recipe.png");
    @NotNull protected final IJob<?> job;
    @NotNull private final ItemStack catalyst;
    @NotNull private final IDrawableStatic background;
    @NotNull protected final IDrawableStatic slot;
    @NotNull protected final IDrawableStatic chanceSlot;
    @NotNull private final EntityCitizen citizen;
    @NotNull private final List<FormattedText> description;
    @NotNull private final LoadingCache<T, List<InfoBlock>> infoBlocksCache;

    protected static final int WIDTH = 167;
    protected static final int HEIGHT = 120;
    protected static final int CITIZEN_X = 2;
    protected static final int CITIZEN_Y = 46;
    protected static final int CITIZEN_W = 47;
    protected static final int CITIZEN_H = 71;

    protected JobBasedRecipeCategory(@NotNull final IJob<?> job,
                                     @NotNull final RecipeType<T> type,
                                     @NotNull final ItemStack icon,
                                     @NotNull final IGuiHelper guiHelper)
    {
        super(
            type,
            getTitleAsTextComponent(job),
            guiHelper.createDrawableItemStack(icon),
            WIDTH,
            HEIGHT
        );
        this.job = job;
        this.catalyst = icon;

        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, WIDTH, HEIGHT);
        this.slot = guiHelper.getSlotDrawable();
        this.chanceSlot = guiHelper.createDrawable(TEXTURE, 0, 121, 18, 18);

        this.citizen = createCitizenWithJob(this.job);

        this.description = translateDescription(
                TranslationConstants.PARTIAL_JEI_INFO +
                        this.job.getJobRegistryEntry().getKey().getPath());

        this.infoBlocksCache = CacheBuilder.newBuilder()
                .maximumSize(6)
                .build(new CacheLoader<>()
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
    private static Component getTitleAsTextComponent(IJob<?> job)
    {
        return Component.translatableEscape(job.getJobRegistryEntry().getTranslationKey());
    }

    public List<T> findRecipes(@NotNull final Map<CraftingType, List<IGenericRecipe>> vanilla,
                               @NotNull final List<Animal> animals,
                               @NotNull final Level world)
    {
        return Collections.emptyList();
    }

    /**
     * Creates a display slot for the specified tool
     * @param builder        the layout builder
     * @param requiredTool   the required tool
     * @param x              the horizontal coordinate
     * @param y              the vertical
     * @param withBackground true to display a slot background when present (no background is shown when no tool)
     */
    protected void addToolSlot(@NotNull final IRecipeLayoutBuilder builder,
                               @NotNull final EquipmentTypeEntry requiredTool,
                               final int x, final int y, final boolean withBackground)
    {
        final IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.CATALYST, x, y).setSlotName("tool");

        if (requiredTool != ModEquipmentTypes.none.get())
        {
            if (withBackground)
            {
                slot.setStandardSlotBackground();
            }

            List<ItemStack> tools = TOOL_CACHE.get(requiredTool);
            if (tools == null)
            {
                tools = MinecoloniesAPIProxy.getInstance().getColonyManager().getCompatibilityManager().getListOfAllItems().stream()
                        .filter(requiredTool::checkIsEquipment)
                        .sorted(Comparator.comparing(requiredTool::getMiningLevel))
                        .toList();
                TOOL_CACHE.put(requiredTool, tools);
            }
            slot.addItemStacks(tools);

        }
    }

    @Override
    public void draw(@NotNull final T recipe,
                     @NotNull final IRecipeSlotsView recipeSlotsView,
                     @NotNull final GuiGraphics stack,
                     final double mouseX, final double mouseY)
    {
        this.background.draw(stack);

        final float scale = CITIZEN_H / 2.4f;
        final int citizen_cx = CITIZEN_X + (CITIZEN_W / 2);
        final int citizen_cy = CITIZEN_Y + (CITIZEN_H / 2);
        final int citizen_by = CITIZEN_Y + CITIZEN_H;
        final int offsetY = 4;

        final float headYaw = (float) Math.atan((citizen_cx - mouseX) / 40.0F) * 40.0F;
        final float yaw = (float) Math.atan((citizen_cx - mouseX) / 40.0F) * 20.0F;
        final float pitch = (float) Math.atan((citizen_cy - offsetY - mouseY) / 40.0F) * 20.0F;
        Lighting.setupForFlatItems();
        UiRenderMacros.drawEntity(stack.pose(), citizen_cx, citizen_by - offsetY, scale, headYaw, yaw, pitch, this.citizen);
        Lighting.setupFor3DItems();
    }

    @Override
    public void createRecipeExtras(@NotNull final IRecipeExtrasBuilder builder,
                                   @NotNull final T recipe,
                                   @NotNull final IFocusGroup focuses)
    {
        builder.addText(this.description, getWidth(), 44)
                .setColor(ChatFormatting.BLACK.getColor());

        for (final InfoBlock block : this.infoBlocksCache.getUnchecked(recipe))
        {
            builder.addText(Component.literal(block.text), block.bounds.getWidth(), block.bounds.getHeight())
                    .setPosition(block.bounds.getX(), block.bounds.getY())
                    .setColor(ChatFormatting.YELLOW.getColor())
                    .setShadow(true);
        }
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull T recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY)
    {
        for (final InfoBlock block : this.infoBlocksCache.getUnchecked(recipe))
        {
            if (block.tip == null) continue;
            if (block.bounds.contains((int) mouseX, (int) mouseY))
            {
                tooltip.add(Component.literal(block.tip));
            }
        }
    }

    @NotNull
    private List<InfoBlock> calculateInfoBlocks(@NotNull T recipe)
    {
        final List<Component> lines = generateInfoBlocks(recipe);

        final Minecraft mc = Minecraft.getInstance();
        final List<InfoBlock> result = new ArrayList<>();
        int y = CITIZEN_Y;
        for (final Component line : lines)
        {
            final String text = line.getString();
            final int width = (int) mc.font.getSplitter().stringWidth(text);
            final int height = mc.font.lineHeight;
            final int x = WIDTH - width;
            String tip = null;
            if (line.getContents() instanceof TranslatableContents contents)
            {
                final String key = contents.getKey() + ".tip";
                if (I18n.exists(key))
                {
                    tip = (Component.translatableEscape(key, contents.getArgs())).getString();
                }
            }
            result.add(new InfoBlock(text, tip, new Rect2i(x, y, width, height)));
            y += height + 2;
        }
        return result;
    }

    @NotNull
    protected abstract List<Component> generateInfoBlocks(@NotNull T recipe);

    private static class InfoBlock
    {
        public InfoBlock(final String text, final String tip, final Rect2i bounds)
        {
            this.text = text;
            this.tip = tip;
            this.bounds = bounds;
        }

        public final String text;
        public final String tip;
        public final Rect2i bounds;
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
    private static List<FormattedText> translateDescription(@NotNull final String... keys)
    {
        return Arrays.stream(keys).map(Component::translatable).collect(Collectors.toList());
    }

    protected static class RecipeIdTooltipCallback implements IRecipeSlotRichTooltipCallback
    {
        private final ResourceLocation id;
        private final IModIdHelper modIdHelper;

        public RecipeIdTooltipCallback(final ResourceLocation id, final IModIdHelper modIdHelper)
        {
            this.id = id;
            this.modIdHelper = modIdHelper;
        }

        @Override
        public void onRichTooltip(@NotNull IRecipeSlotView recipeSlotView, @NotNull ITooltipBuilder tooltip)
        {
            final ItemStack ingredient = recipeSlotView.getDisplayedIngredient().flatMap(d -> d.getIngredient(VanillaTypes.ITEM_STACK)).orElse(ItemStack.EMPTY);

            if (modIdHelper.isDisplayingModNameEnabled())
            {
                final String recipeModId = id.getNamespace();
                final String ingredientModId = ingredient.getItem().getCreatorModId(ingredient);
                if (!recipeModId.equals(ingredientModId))
                {
                    final String modName = modIdHelper.getFormattedModNameForModId(recipeModId);
                    final MutableComponent recipeBy = Component.translatableEscape("jei.tooltip.recipe.by", modName);
                    tooltip.add(recipeBy.withStyle(ChatFormatting.GRAY));
                }
            }

            final boolean showAdvanced = Minecraft.getInstance().options.advancedItemTooltips || Screen.hasShiftDown();
            if (showAdvanced)
            {
                final MutableComponent recipeId = Component.translatableEscape("jei.tooltip.recipe.id", id.toString());
                tooltip.add(recipeId.withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    protected static class LootTableTooltipCallback implements IRecipeSlotRichTooltipCallback
    {
        private final LootTableAnalyzer.LootDrop drop;
        private final ResourceKey<LootTable> id;

        public LootTableTooltipCallback(final LootTableAnalyzer.LootDrop drop, final ResourceKey<LootTable> id)
        {
            this.drop = drop;
            this.id = id;
        }

        @Override
        public void onRichTooltip(@NotNull final IRecipeSlotView recipeSlotView,
                                  @NotNull final ITooltipBuilder tooltip)
        {
            final String key = TranslationConstants.PARTIAL_JEI_INFO +
                    (this.drop.getQuality() < 0 ? "chancenegskill.tip" : this.drop.getQuality() > 0 ? "chanceskill.tip" : "chance.tip");
            final float probability = this.drop.getProbability() * 100;

            if (probability >= 1)
            {
                tooltip.add(Component.translatableEscape(key,
                    Math.round(probability)));
            }
            else
            {
                tooltip.add(Component.translatableEscape(key,
                    Math.round(probability * 100) / 100f));
            }

            if (this.drop.getConditional())
            {
                tooltip.add(Component.translatableEscape(TranslationConstants.PARTIAL_JEI_INFO + "conditions.tip"));
            }

            final boolean showAdvanced = Minecraft.getInstance().options.advancedItemTooltips || Screen.hasShiftDown();
            if (showAdvanced)
            {
                final MutableComponent recipeId = Component.translatableEscape("com.minecolonies.coremod.jei.loottableid", id.location().toString());
                tooltip.add(recipeId.withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }
}
