package com.minecolonies.coremod.compatibility.jei;

import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.ITooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
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
    @NotNull private final IDrawableStatic background;
    @NotNull private final IDrawable icon;
    @NotNull protected final IDrawableStatic slot;
    @NotNull protected final IDrawableStatic chanceSlot;
    @NotNull private final EntityCitizen citizen;
    @NotNull private final List<ITextProperties> description;
    @NotNull protected final List<InfoBlock> infoBlocks;

    protected static final int WIDTH = 167;
    protected static final int HEIGHT = 120;
    protected static final int CITIZEN_X = 2;
    protected static final int CITIZEN_Y = 46;
    protected static final int CITIZEN_W = 47;
    protected static final int CITIZEN_H = 71;

    protected JobBasedRecipeCategory(@NotNull final IJob<?> job, @NotNull final ItemStack icon, @NotNull final IGuiHelper guiHelper)
    {
        this.job = job;

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
    public IJob<?> getJob()
    {
        return this.job;
    }

    @NotNull
    @Override
    public ResourceLocation getUid()
    {
        return Objects.requireNonNull(this.job.getJobRegistryEntry().getRegistryName());
    }

    @NotNull
    @Override
    public String getTitle()
    {
        return I18n.get(this.job.getName().toLowerCase());
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

    @Override
    public void draw(@NotNull final T recipe, @NotNull final MatrixStack matrixStack, final double mouseX, final double mouseY)
    {
        final float scale = CITIZEN_H / 2.4f;
        final int citizen_cx = CITIZEN_X + (CITIZEN_W / 2);
        final int citizen_cy = CITIZEN_Y + (CITIZEN_H / 2);
        final int citizen_by = CITIZEN_Y + CITIZEN_H;
        final int offsetY = 4;
        RenderHelper.scissor(matrixStack, CITIZEN_X, CITIZEN_Y, CITIZEN_W, CITIZEN_H);
        RenderHelper.renderEntity(matrixStack, citizen_cx, citizen_by - offsetY, scale, citizen_cx - mouseX, citizen_cy - offsetY - mouseY, this.citizen);
        RenderHelper.stopScissor();

        int y = 0;
        final Minecraft mc = Minecraft.getInstance();
        for (final ITextProperties line : this.description)
        {
            final int x = 0;
            mc.font.draw(matrixStack, LanguageMap.getInstance().getVisualOrder(line), x, y, TextFormatting.BLACK.getColor());
            y += mc.font.lineHeight + 2;
        }

        for (final InfoBlock block : this.infoBlocks)
        {
            mc.font.drawShadow(matrixStack, block.text, block.bounds.getX(), block.bounds.getY(), TextFormatting.YELLOW.getColor());
        }
    }

    @NotNull
    @Override
    public List<ITextComponent> getTooltipStrings(@NotNull final T recipe, final double mouseX, final double mouseY)
    {
        final List<ITextComponent> tooltips = new ArrayList<>();

        for (final InfoBlock block : this.infoBlocks)
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
    protected static List<InfoBlock> calculateInfoBlocks(@NotNull final List<ITextComponent> lines)
    {
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

    protected static class InfoBlock
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
        public void onTooltip(final int slot, final boolean input, @NotNull final ItemStack stack, @NotNull final List<ITextComponent> tooltip)
        {
            final int index = slot - this.firstSlot;
            if (index >= 0 && index < this.drops.size())
            {
                final LootTableAnalyzer.LootDrop drop = this.drops.get(index);
                final String key = TranslationConstants.COM_MINECOLONIES_JEI_PREFIX +
                        (drop.getVariableQuality() ? "chanceskill.tip" : "chance.tip");
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
            }
        }
    }
}
