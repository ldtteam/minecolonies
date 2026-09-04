package com.minecolonies.core.event;

import com.google.common.collect.ImmutableMap;
import com.ldtteam.domumornamentum.client.model.data.MaterialTextureData;
import com.ldtteam.structurize.items.ModItems;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.interfaces.IBuildingBrowsableBlock;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.ICraftingBuildingModule;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.util.FoodUtils;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.ColonyConstants;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.client.gui.WindowBuildingBrowser;
import com.minecolonies.core.client.gui.containers.WindowCitizenInventory;
import com.minecolonies.core.client.render.worldevent.ColonyBorderRenderer;
import com.minecolonies.core.client.render.worldevent.WorldEventContext;
import com.minecolonies.core.colony.crafting.CustomRecipe;
import com.minecolonies.core.colony.crafting.CustomRecipeManager;
import com.minecolonies.core.util.DomumOrnamentumUtils;
import com.minecolonies.core.util.SchemAnalyzerUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

import static com.minecolonies.api.research.util.ResearchConstants.SATURATION;
import static com.minecolonies.api.sounds.ModSoundEvents.CITIZEN_SOUND_EVENT_PREFIX;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.translation.DebugTranslationConstants.*;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.RESTAURANT_MENU;

/**
 * Used to handle client events.
 */
@OnlyIn(Dist.CLIENT)
public class ClientEventHandler
{
    /**
     * Lazy cache for crafting module lookups.
     */
    private static final Lazy<Map<String, BuildingEntry>> crafterToBuilding = Lazy.of(ClientEventHandler::buildCrafterToBuildingMap);

    private static final int TITLE_SCREEN_ICON_SIZE = Button.DEFAULT_HEIGHT;
    private static final int TITLE_SCREEN_ICON_SPACING = 4;
    private static final Identifier TITLE_SCREEN_ICON_TEXTURE = Identifier.fromNamespaceAndPath(
        "minecolonies", "textures/gui/builderhut/builder_button_mini.png");
    private static final int TITLE_SCREEN_ICON_TEXTURE_WIDTH = 14;
    private static final int TITLE_SCREEN_ICON_TEXTURE_HEIGHT = 15;
    private static final List<MineColoniesLink> MINECOLONIES_LINKS = List.of(
        new MineColoniesLink("Website", "W", "https://www.minecolonies.com/"),
        new MineColoniesLink("Patreon", "P", "https://www.patreon.com/Minecolonies"),
        new MineColoniesLink("Discord", "D", "https://discord.minecolonies.com"),
        new MineColoniesLink("Wiki", "?", "https://wiki.minecolonies.ldtteam.com"),
        new MineColoniesLink("GitHub", "GH", "https://github.com/ldtteam/minecolonies"),
        new MineColoniesLink("CurseForge", "CF", "https://www.curseforge.com/minecraft/mc-mods/minecolonies/files/all"));

    /**
     * Adds the official MineColonies community links to the Minecraft title screen.
     *
     * <p>The upstream Patreon control is available from the Town Hall after entering a world.
     * Keeping the same official destinations on the title screen makes them available from the
     * launcher menu as well. Link controls use compact square badges and tooltips so they fit
     * into the title screen's existing icon row.</p>
     *
     * @param event title-screen initialization event.
     */
    @SubscribeEvent
    public static void addMineColoniesLinksToTitleScreen(final ScreenEvent.Init.Post event)
    {
        if (!(event.getScreen() instanceof final TitleScreen titleScreen))
        {
            return;
        }

        // TitleScreen places Mods, Friends, Language, and Accessibility here after its three
        // primary menu buttons. Include our links in that same row and re-center the complete set.
        final int iconRowY = titleScreen.height / 4 + 32 + 3 * 24;
        final List<AbstractWidget> existingIcons = new ArrayList<>();
        for (final var listener : event.getListenersList())
        {
            if (listener instanceof final AbstractWidget widget
                && widget.getY() == iconRowY
                && widget.getWidth() == TITLE_SCREEN_ICON_SIZE
                && widget.getHeight() == TITLE_SCREEN_ICON_SIZE)
            {
                existingIcons.add(widget);
            }
        }

        existingIcons.sort(Comparator.comparingInt(AbstractWidget::getX));
        final int totalIcons = existingIcons.size() + MINECOLONIES_LINKS.size();
        final int rowWidth = totalIcons * TITLE_SCREEN_ICON_SIZE + (totalIcons - 1) * TITLE_SCREEN_ICON_SPACING;
        final int left = Math.max(0, (titleScreen.width - rowWidth) / 2);
        for (int i = 0; i < existingIcons.size(); i++)
        {
            existingIcons.get(i).setPosition(left + i * (TITLE_SCREEN_ICON_SIZE + TITLE_SCREEN_ICON_SPACING), iconRowY);
        }

        for (int i = 0; i < MINECOLONIES_LINKS.size(); i++)
        {
            final MineColoniesLink link = MINECOLONIES_LINKS.get(i);
            addMineColoniesLink(event, titleScreen,
                left + (existingIcons.size() + i) * (TITLE_SCREEN_ICON_SIZE + TITLE_SCREEN_ICON_SPACING), iconRowY, link);
        }
    }

    /**
     * Adds one title-screen link using the same confirmation flow as the official Town Hall Patreon button.
     */
    private static void addMineColoniesLink(
        final ScreenEvent.Init.Post event,
        final Screen titleScreen,
        final int x,
        final int y,
        final MineColoniesLink link)
    {
        event.addListener(new MineColoniesLinkButton(x, y, Component.literal(link.label()), Component.literal(link.icon()), button ->
            Minecraft.getInstance().setScreenAndShow(new ConfirmLinkScreen(confirmed -> {
                if (confirmed)
                {
                    Util.getPlatform().openUri(link.url());
                }

                Minecraft.getInstance().setScreenAndShow(titleScreen);
            }, link.url(), true))));
    }

    /**
     * Vanilla square-button behavior with a compact link badge and an accessible tooltip.
     */
    private static final class MineColoniesLinkButton extends Button
    {
        private MineColoniesLinkButton(
            final int x,
            final int y,
            final Component message,
            final Component icon,
            final Button.OnPress onPress)
        {
            super(x, y, TITLE_SCREEN_ICON_SIZE, TITLE_SCREEN_ICON_SIZE, message, onPress, DEFAULT_NARRATION);
            setTooltip(Tooltip.create(message));
            this.icon = icon;
        }

        private final Component icon;

        @Override
        protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTick)
        {
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TITLE_SCREEN_ICON_TEXTURE,
                getX(),
                getY(),
                0,
                0,
                getWidth(),
                getHeight(),
                TITLE_SCREEN_ICON_TEXTURE_WIDTH,
                TITLE_SCREEN_ICON_TEXTURE_HEIGHT,
                TITLE_SCREEN_ICON_TEXTURE_WIDTH,
                TITLE_SCREEN_ICON_TEXTURE_HEIGHT,
                ARGB.white(getAlpha()));

            final int textColor = ARGB.color(getAlpha(), isHoveredOrFocused() ? 0x808080 : 0x000000);
            final int textX = getX() + (getWidth() - Minecraft.getInstance().font.width(icon)) / 2;
            final int textY = getY() + (getHeight() - Minecraft.getInstance().font.lineHeight) / 2 + 1;
            graphics.text(Minecraft.getInstance().font, icon, textX, textY, textColor, false);
        }
    }

    private record MineColoniesLink(String label, String icon, String url)
    {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderWorldAfterOpaqueFeatures(@NotNull final RenderLevelStageEvent.AfterOpaqueFeatures event)
    {
        WorldEventContext.INSTANCE.renderWorldLastEvent(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void submitColonyBlueprints(@NotNull final SubmitCustomGeometryEvent event)
    {
        WorldEventContext.INSTANCE.submitBlueprints(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderWorldAfterTranslucentBlocks(@NotNull final RenderLevelStageEvent.AfterTranslucentBlocks event)
    {
        WorldEventContext.INSTANCE.renderWorldLastEvent(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onwWorldTick(@NotNull final LevelTickEvent.Pre event)
    {
        if (event.getLevel().isClientSide() && ColonyConstants.rand.nextInt(20) == 0)
        {
            WorldEventContext.INSTANCE.checkNearbyColony(event.getLevel());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(@NotNull final ClientPlayerNetworkEvent.LoggingOut event)
    {
        ColonyBorderRenderer.cleanup();
        WindowBuildingBrowser.clearCache();
        IColonyManager.getInstance().resetColonyViews();
        Log.getLogger().info("Removed all colony views");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlaySoundEvent(final PlaySoundEvent event)
    {
        if (event.getSound() == null)
        {
            return;
        }

        final Identifier soundLocation = event.getSound().getIdentifier();
        if (!MinecoloniesAPIProxy.getInstance().getConfig().getClient().citizenVoices.get()
            && soundLocation.getNamespace().equals(Constants.MOD_ID)
            && soundLocation.getPath().startsWith(CITIZEN_SOUND_EVENT_PREFIX)
        )
        {
            event.setSound(null);
        }
    }

    /**
     * Additional tooltips added to specific items
     */
    public static Map<Item, Component> extraItemTooltips = new HashMap<>();

    /**
     * Fires when an item tooltip is requested, generally from inventory, JEI, or when minecraft is first populating the recipe book.
     *
     * @param event An ItemTooltipEvent
     */
    @SubscribeEvent
    public static void onItemTooltipEvent(final ItemTooltipEvent event)
    {
        // Vanilla recipe books populate tooltips once before the player exists on remote clients, some other cases.
        if (event.getEntity() == null)
        {
            return;
        }
        IColony colony = IMinecoloniesAPI.getInstance().getColonyManager().getIColony(event.getEntity().level(), event.getEntity().blockPosition());
        final ItemStack stack = event.getItemStack();

        if (extraItemTooltips.containsKey(stack.getItem()))
        {
            event.getToolTip().add(extraItemTooltips.get(stack.getItem()));
        }

        if (stack.has(DataComponents.DYED_COLOR) && IMinecoloniesAPI.getInstance().getConfig().getClient().showdyetooltips.get())
        {
            IMinecoloniesAPI.getInstance().getColonyManager().getCompatibilityManager().getDyeColor(stack).ifPresent(c ->
            {
                event.getToolTip().removeIf(line -> line.getContents() instanceof TranslatableContents t && t.getKey().equals("item.dyed"));
                event.getToolTip().add(1, Component.translatable("%s: %s",
                    Component.translatable("item.dyed"),
                    Component.translatable("color.minecraft." + c.getName()).withStyle(Style.EMPTY.withColor(c.getTextColor()).withItalic(false)))
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true)));
            });
        }

        if (colony == null)
        {
            colony = IMinecoloniesAPI.getInstance().getColonyManager().getIColonyByOwner(event.getEntity().level(), event.getEntity());
        }

        if (colony == null)
        {
            return;
        }

        handleCrafterRecipeTooltips(colony, event.getToolTip(), stack.getItem());
        if (stack.getItem() instanceof BlockItem)
        {
            final BlockItem blockItem = (BlockItem) stack.getItem();
            if (blockItem.getBlock() instanceof AbstractBlockHut)
            {
                handleHutBlockResearchUnlocks(colony, event.getToolTip(), blockItem.getBlock());
            }

            if (event.getEntity().isCreative() && InventoryUtils.hasItemInItemHandler(new InvWrapper(event.getEntity().getInventory()), ModItems.scanTool.get()))
            {
                int tier = SchemAnalyzerUtil.getBlockTier(blockItem.getBlock());

                if (DomumOrnamentumUtils.isDoBlock(blockItem.getBlock()))
                {
                    for (Block block : MaterialTextureData.readFromItemStack(event.getItemStack()).getTexturedComponents().values())
                    {
                        tier = Math.max(tier, SchemAnalyzerUtil.getBlockTier(block));
                    }
                }

                event.getToolTip().add(Component.translatableEscape("com.minecolonies.coremod.tooltip.schematic.tier", tier));
            }
        }

        if (WindowCitizenInventory.activeCitizenInventory != null && ItemStackUtils.ISFOOD.test(stack))
        {
            if (!FoodUtils.EDIBLE.test(stack))
            {
                event.getToolTip().add(Component.translatable("com.minecolonies.coremod.item.tooltip.wrongfood").withStyle(ChatFormatting.RED));
                return;
            }

            final int foodTier = FoodUtils.getFoodTier(stack);

            final ICitizenDataView citizenData = (ICitizenDataView) WindowCitizenInventory.activeCitizenInventory.getCitizenData();
            final IColonyView colonyView = citizenData.getColony();

            IBuildingView cookBuilding = null;
            for (final IBuildingView buildingView : colonyView.getClientBuildingManager().getBuildings().values())
            {
                if (buildingView.getBuildingType() == ModBuildings.cook.get())
                {
                    if (cookBuilding == null || cookBuilding.getID().distSqr(citizenData.getPosition()) > buildingView.getID().distSqr(citizenData.getPosition()))
                    {
                        cookBuilding = buildingView;
                    }
                }
            }

            final int homeBuildingLevel =
                colonyView.getClientBuildingManager().getBuilding(citizenData.getHomeBuilding()) == null ? 0 : colonyView.getClientBuildingManager().getBuilding(citizenData.getHomeBuilding()).getBuildingLevel();
            if (FoodUtils.canEatLevel(event.getItemStack(), homeBuildingLevel))
            {
                event.getToolTip().add(Component.translatable(TranslationConstants.TIER_TOOLTIP + foodTier).withStyle(ChatFormatting.GRAY));
                if (cookBuilding != null && !cookBuilding.getModuleView(RESTAURANT_MENU).getMenu().contains(new ItemStorage(event.getItemStack())))
                {
                    event.getToolTip().add(Component.translatable("com.minecolonies.coremod.item.tooltip.nomenu").withStyle(ChatFormatting.RED));
                }
            }
            else
            {
                event.getToolTip().add(Component.translatable("com.minecolonies.coremod.item.tooltip.needbetterfood").withStyle(ChatFormatting.RED));
            }
        }
    }

    /**
     * Display crafter recipe-related information on the client.
     *
     * @param colony  The colony to check against, if one is present.
     * @param toolTip The tooltip to add the text onto.
     * @param item    The item that will have the tooltip text added.
     */
    private static void handleCrafterRecipeTooltips(@Nullable final IColony colony, final List<Component> toolTip, final Item item)
    {
        final List<CustomRecipe> recipes = CustomRecipeManager.getInstance().getRecipeByOutput(item);
        if (recipes.isEmpty())
        {
            return;
        }

        final Map<BuildingEntry, Integer> minimumBuildingLevels = new HashMap<>();

        for (CustomRecipe rec : recipes)
        {
            if (!rec.getShowTooltip() || rec.getCrafter().length() < 2)
            {
                continue;
            }
            final BuildingEntry craftingBuilding = crafterToBuilding.get().get(rec.getCrafter());
            if (craftingBuilding == null)
            {
                continue;
            }
            minimumBuildingLevels.putIfAbsent(craftingBuilding, null);
            if (minimumBuildingLevels.get(craftingBuilding) == null || rec.getMinBuildingLevel() < minimumBuildingLevels.get(craftingBuilding))
            {
                minimumBuildingLevels.put(craftingBuilding, rec.getMinBuildingLevel());
            }
            for (final Identifier id : rec.getRequiredResearchIds())
            {
                final Set<IGlobalResearch> researches;
                if (IMinecoloniesAPI.getInstance().getGlobalResearchTree().hasResearch(id))
                {
                    researches = new HashSet<>();
                    researches.add(IMinecoloniesAPI.getInstance().getGlobalResearchTree().getResearch(id));
                }
                else
                {
                    researches = IMinecoloniesAPI.getInstance().getGlobalResearchTree().getResearchForEffect(id);
                }
                if (researches != null)
                {
                    final ChatFormatting researchFormat;
                    if (colony != null && (colony.getResearchManager().getResearchTree().hasCompletedResearch(id) ||
                        colony.getResearchManager().getResearchEffects().getEffectStrength(id) > 0))
                    {
                        researchFormat = ChatFormatting.AQUA;
                    }
                    else
                    {
                        researchFormat = ChatFormatting.RED;
                    }

                    for (IGlobalResearch research : researches)
                    {
                        toolTip.add(Component.translatableEscape(COM_MINECOLONIES_COREMOD_ITEM_REQUIRES_RESEARCH_TOOLTIP_GUI,
                          MutableComponent.create(research.getName())).setStyle(Style.EMPTY.withColor(researchFormat)));
                    }
                }
            }
        }

        for (final Entry<BuildingEntry, Integer> crafterBuildingCombination : minimumBuildingLevels.entrySet())
        {
            final Component craftingBuildingName = getFullBuildingName(crafterBuildingCombination.getKey());
            final Integer minimumLevel = crafterBuildingCombination.getValue();
            if (minimumLevel > 0)
            {
                final Identifier schematicName = crafterBuildingCombination.getKey().getRegistryName();
                // the above is not guaranteed to match (and indeed doesn't for a few buildings), but
                // does match for all currently interesting crafters, at least.  there doesn't otherwise
                // appear to be an easy way to get the schematic name from a BuildingEntry ... or
                // unless we can change how colony.hasBuilding uses its parameter...

                final MutableComponent reqLevelText = Component.translatableEscape(COM_MINECOLONIES_COREMOD_ITEM_BUILDLEVEL_TOOLTIP_GUI, craftingBuildingName, minimumLevel);
                if (colony != null && colony.getCommonBuildingManager().hasBuilding(schematicName, minimumLevel, true))
                {
                    reqLevelText.setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA));
                }
                else
                {
                    reqLevelText.setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                }
                toolTip.add(reqLevelText);
            }
            else
            {
                final MutableComponent reqBuildingTxt = Component.translatableEscape(COM_MINECOLONIES_COREMOD_ITEM_AVAILABLE_TOOLTIP_GUI, craftingBuildingName)
                                                          .setStyle(Style.EMPTY.withItalic(true).withColor(ChatFormatting.GRAY));
                toolTip.add(reqBuildingTxt);
            }
        }
    }

    /**
     * Gets a string like "ModName Building Name" for the specified building entry.
     *
     * @param building The building entry
     * @return The translated building name
     */
    private static Component getFullBuildingName(@NotNull final BuildingEntry building)
    {
        final String namespace = building.getBuildingBlock().getRegistryName().getNamespace();
        final String modName = ModList.get().getModContainerById(namespace)
            .map(m -> m.getModInfo().getDisplayName())
            .orElse(namespace);
        final Component buildingName = building.getBuildingBlock().getName();
        return Component.literal(modName + " ").append(buildingName);
    }

    /**
     * Builds a mapping from crafting module ids to the corresponding buildings.
     *
     * @return The mapping
     */
    private static Map<String, BuildingEntry> buildCrafterToBuildingMap()
    {
        final ImmutableMap.Builder<String, BuildingEntry> builder = new ImmutableMap.Builder<>();
        for (final BuildingEntry building : IMinecoloniesAPI.getInstance().getBuildingRegistry())
        {
            for (final BuildingEntry.ModuleProducer moduleProducer : building.getModuleProducers())
            {
                final IBuildingModule module = BuildingEntry.produceModuleWithoutBuilding(moduleProducer.key);
                if (module instanceof ICraftingBuildingModule craftingBuildingModule && craftingBuildingModule.getCraftingJob() != null)
                {
                    builder.put(craftingBuildingModule.getCustomRecipeKey(), building);
                }
            }
        }
        return builder.build();
    }

    /**
     * Display research-related information on MineColonies Building hut blocks.
     * While this test can handle other non-hut blocks, research can only currently effect AbstractHutBlocks.
     *
     * @param colony  The colony to check against, if one is present.
     * @param tooltip The tooltip to add the text onto.
     * @param block   The hut block
     */
    private static void handleHutBlockResearchUnlocks(final IColony colony, final List<Component> tooltip, final Block block)
    {
        if (colony == null)
        {
            return;
        }
        final Identifier effectId = colony.getResearchManager().getResearchEffectIdFrom(block);
        if (colony.getResearchManager().getResearchEffects().getEffectStrength(effectId) > 0)
        {
            return;
        }
        if (MinecoloniesAPIProxy.getInstance().getGlobalResearchTree().getResearchForEffect(effectId) != null)
        {
            tooltip.add(Component.translatableEscape(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_1, block.getName()));
            tooltip.add(Component.translatableEscape(TranslationConstants.HUT_NEEDS_RESEARCH_TOOLTIP_2, block.getName()));
        }
    }

    /**
     * Event when the debug screen is opened. Event gets called by displayed text on the screen, we only need it when f3 is clicked.
     */
    @SubscribeEvent
    public static void onDebugOverlay(final RenderGuiEvent.Post event)
    {
        final Minecraft mc = Minecraft.getInstance();
        if (!mc.getDebugOverlay().showDebugScreen())
        {
            return;
        }

            final ClientLevel world = mc.level;
            final LocalPlayer player = mc.player;
            final BlockPos pos = player.blockPosition();
            IColony colony = IColonyManager.getInstance().getIColony(world, pos);
            final List<Component> debugLines = new ArrayList<>();
            if (colony == null)
            {
                if (IColonyManager.getInstance().isFarEnoughFromColonies(world, pos))
                {
                    debugLines.add(Component.translatableEscape(DEBUG_NO_CLOSE_COLONY));
                    drawDebugLines(event.getGuiGraphics(), mc, debugLines);
                    return;
                }
                colony = IColonyManager.getInstance().getClosestIColony(world, pos);

                if (colony == null)
                {
                    return;
                }

                debugLines.add(Component.translatableEscape(DEBUG_NEXT_COLONY,
                    (int) Math.sqrt(colony.getDistanceSquared(pos)),
                    IColonyManager.getInstance().getMinimumDistanceBetweenTownHalls()));
                drawDebugLines(event.getGuiGraphics(), mc, debugLines);
                return;
            }

            debugLines.add(Component.literal(colony.getName() + " : ")
              .append(Component.translatableEscape(DEBUG_BLOCKS_FROM_CENTER, (int) Math.sqrt(colony.getDistanceSquared(pos)))));
            drawDebugLines(event.getGuiGraphics(), mc, debugLines);
    }

    private static void drawDebugLines(
      final net.minecraft.client.gui.GuiGraphicsExtractor graphics,
      final Minecraft mc,
      final List<Component> lines)
    {
        int y = 5;
        for (final Component line : lines)
        {
            graphics.text(mc.font, line, 5, y, 0xffffffff, true);
            y += mc.font.lineHeight + 1;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onUseItem(@NotNull final PlayerInteractEvent.RightClickItem event)
    {
        if (!event.getLevel().isClientSide())
        {
            return;
        }

        if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().getItem() instanceof BlockItem blockItem)
        {
            // due to a Forge bug, this event still triggers on right-clicking a block (and there are no properties on
            // the event itself to distinguish the two cases, even though there are likely-sounding ones), so we need
            // to filter that out
            if (Minecraft.getInstance().hitResult != null && Minecraft.getInstance().hitResult.getType() != HitResult.Type.MISS)
            {
                return;
            }

            final Block block = blockItem.getBlock();

            if (block instanceof IBuildingBrowsableBlock browsable && browsable.shouldBrowseBuildings(event))
            {
                MinecoloniesAPIProxy.getInstance().getBuildingDataManager().openBuildingBrowser(block);

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }
}
