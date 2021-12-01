package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.crafting.LootTableAnalyzer;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
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
    @NotNull private final List<FormattedText> description;
    @NotNull protected final List<InfoBlock> infoBlocks;

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

        this.infoBlocks = new ArrayList<>();
        this.description = wordWrap(breakLines(translateDescription(
                TranslationConstants.COM_MINECOLONIES_JEI_PREFIX +
                        this.job.getJobRegistryEntry().getRegistryName().getPath())));
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
    public Component getTitle()
    {
        return getTitleAsTextComponent();
    }

    @NotNull
    public Component getTitleAsTextComponent()
    {
        return new TranslatableComponent(this.job.getJobRegistryEntry().getTranslationKey());
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

    public Collection<?> findRecipes(@NotNull final Map<RecipeType<?>, List<IGenericRecipe>> vanilla)
    {
        return Collections.emptyList();
    }

    @Override
    public void draw(@NotNull final T recipe, @NotNull final PoseStack matrixStack, final double mouseX, final double mouseY)
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
        for (final FormattedText line : this.description)
        {
            final int x = 0;
            mc.font.draw(matrixStack, Language.getInstance().getVisualOrder(line), x, y, ChatFormatting.BLACK.getColor());
            y += mc.font.lineHeight + 2;
        }

        for (final InfoBlock block : this.infoBlocks)
        {
            mc.font.drawShadow(matrixStack, block.text, block.bounds.getX(), block.bounds.getY(), ChatFormatting.YELLOW.getColor());
        }
    }

    @NotNull
    @Override
    public List<Component> getTooltipStrings(@NotNull final T recipe, final double mouseX, final double mouseY)
    {
        final List<Component> tooltips = new ArrayList<>();

        for (final InfoBlock block : this.infoBlocks)
        {
            if (block.tip == null) continue;
            if (block.bounds.contains((int) mouseX, (int) mouseY))
            {
                tooltips.add(new TextComponent(block.tip));
            }
        }

        return tooltips;
    }

    @NotNull
    protected static List<InfoBlock> calculateInfoBlocks(@NotNull final List<Component> lines)
    {
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
            if (line instanceof TranslatableComponent)
            {
                final String key = ((TranslatableComponent) line).getKey() + ".tip";
                if (I18n.exists(key))
                {
                    tip = (new TranslatableComponent(key, ((TranslatableComponent) line).getArgs())).getString();
                }
            }
            result.add(new InfoBlock(text, tip, new Rect2i(x, y, width, height)));
            y += height + 2;
        }
        return result;
    }

    protected static class InfoBlock
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
        return Arrays.stream(keys).map(TranslatableComponent::new).collect(Collectors.toList());
    }

    @NotNull
    private static List<FormattedText> breakLines(@NotNull final List<FormattedText> input)
    {
        final List<FormattedText> lines = new ArrayList<>();
        for (final FormattedText component : input)
        {
            final Optional<String[]> expanded = component.visit(line -> Optional.of(line.split("\\\\n")));
            expanded.ifPresent(e -> lines.addAll(Arrays.stream(e).map(TextComponent::new).collect(Collectors.toList())));
        }
        return lines;
    }

    @NotNull
    private static List<FormattedText> wordWrap(@NotNull final List<FormattedText> input)
    {
        final Minecraft mc = Minecraft.getInstance();
        final List<FormattedText> lines = new ArrayList<>();
        for (final FormattedText component : input)
        {
            lines.addAll(mc.font.getSplitter().splitLines(component, WIDTH, Style.EMPTY));
        }
        return lines;
    }

    protected static class LootTableTooltipCallback implements ITooltipCallback<ItemStack>
    {
        private final int firstSlot;
        private final List<LootTableAnalyzer.LootDrop> drops;

        public LootTableTooltipCallback(final int firstSlot, final List<LootTableAnalyzer.LootDrop> drops)
        {
            this.firstSlot = firstSlot;
            this.drops = drops;
        }

        @Override
        public void onTooltip(final int slot, final boolean input, @NotNull final ItemStack stack, @NotNull final List<Component> tooltip)
        {
            final int index = slot - this.firstSlot;
            if (index >= 0 && index < this.drops.size())
            {
                final LootTableAnalyzer.LootDrop drop = this.drops.get(index);
                final String key = TranslationConstants.COM_MINECOLONIES_JEI_PREFIX +
                        (drop.getQuality() < 0 ? "chancenegskill.tip" : drop.getQuality() > 0 ? "chanceskill.tip" : "chance.tip");
                final float probability = drop.getProbability() * 100;

                if (probability >= 1)
                {
                    tooltip.add(new TranslatableComponent(key,
                            Math.round(probability)));
                }
                else
                {
                    tooltip.add(new TranslatableComponent(key,
                            Math.round(probability * 100) / 100f));
                }

                if (drop.getConditional())
                {
                    tooltip.add(new TranslatableComponent(TranslationConstants.COM_MINECOLONIES_JEI_PREFIX + "conditions.tip"));
                }
            }
        }
    }
}
