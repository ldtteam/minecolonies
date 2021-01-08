package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ZoomDragView;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.coremod.network.messages.server.colony.building.university.TryResearchMessage;
import com.minecolonies.coremod.research.AlternateBuildingResearchRequirement;
import com.minecolonies.coremod.research.BuildingResearchRequirement;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window to display a particular branch of the tree.
 */
public class WindowResearchTree extends AbstractWindowSkeleton
{
    /**
     * The branch of this research.
     */
    private final String branch;

    /**
     * The university building.
     */
    private final BuildingUniversity.View building;

    /**
     * The previous window.
     */
    private final WindowHutUniversity last;

    /**
     * If has a max research for this branch already.
     */
    private boolean hasMax;

    /**
     * The undo button, if one is present.
     */
    private final ButtonImage undoButton = new ButtonImage();

    /**
     * The undo cost icons, if one is present.
     */
    private ItemIcon[] undoCostIcons = new ItemIcon[0];

    /**
     * Displaced tooltip text, if present.
     */
    private List<IFormattableTextComponent> displacedHoverText;

    /**
     * Button that the tooltip text has been displaced from.
     */
    private Button displacedButton;

    /**
     * Create the research tree window.
     *
     * @param branch   the branch being researched.
     * @param building the associated university.
     * @param last     the GUI we opened this from.
     */
    public WindowResearchTree(final String branch, final BuildingUniversity.View building, final WindowHutUniversity last)
    {
        super(Constants.MOD_ID + R_TREE_RESOURCE_SUFFIX, last);
        this.branch = branch;
        this.building = building;
        this.last = last;
        this.hasMax = false;

        final List<String> researchList = IGlobalResearchTree.getInstance().getPrimaryResearch(branch);
        this.hasMax = building.getColony().getResearchManager().getResearchTree().branchFinishedHighestLevel(branch);

        final ZoomDragView view = findPaneOfTypeByID(DRAG_VIEW_ID, ZoomDragView.class);

        final int maxHeight = drawTree(0, 0, view, researchList, false);
        drawTreeBackground(view, maxHeight);
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);

        // drawUndoProgressButton and drawUndoCompleteButton replace a Research's normal tooltip
        // and adds a button and icon(s) representing the cost of resetting the research.
        // See their respective functions for details on the how.
        // These branches remove those buttons from the DragTreeView, if present, on pressing any button.
        // That should occur no matter what buttons are pressed, even disabled buttons, as a "no, I don't want to".
        // We'll do that even for branches that will close the WindowResearchTree for now,
        // since down the road we may want to be able to cancel or start multiple researches without
        // closing and reopening the WindowResearchTree.
        if (button.getParent().getChildren().contains(undoButton))
        {
            button.getParent().removeChild(undoButton);
        }
        for (ItemIcon icon : undoCostIcons)
        {
            if (button.getParent().getChildren().contains(icon))
            {
                button.getParent().removeChild(icon);
            }
        }
        if (displacedButton != null && displacedHoverText != null)
        {
            displacedButton.setHoverToolTip(displacedHoverText);
        }

        // Check for an empty button Id.  These reflect disabled buttons normally
        // but a sufficiently malformed data pack may also have a blank research id,
        // and we don't want to try to try to parse that.
        // May eventually want a sound handler here, but SoundUtils.playErrorSound is a bit much.
        if (button.getID().isEmpty())
        {
            // intentionally empty.
        }
        // Undo just the selected research.
        else if (button.getID().contains("undo."))
        {
            final ILocalResearch cancelResearch = building.getColony().getResearchManager().getResearchTree().getResearch(branch, button.getID().split("\\.")[1]);
            if (cancelResearch != null)
            {
                // Can't rely on getting an updated research count after the cancellation in any predictable timeframe.
                // Instead, offset the UniversityWindow's count by -1 before the packet could be sent.
                if (cancelResearch.getState() == ResearchState.IN_PROGRESS)
                {
                    last.updateResearchCount(-1);
                }
                // Canceled research will eventually be removed from the local tree on synchronization from server,
                // But this can be long enough (~5 seconds) to cause confusion if the player reopens the WindowResearchTree.
                // Completely removing the research to null will allow players to unintentionally restart it.
                // While the server-side logic prevents this from taking items, it would be confusing.
                // Setting to NOT_STARTED means that it can't be sent, as only null research states
                // are eligible to send TryResearchMessages, and only IN_PROGRESS, or FINISHED
                // are eligible to drawUndo buttons.
                cancelResearch.setState(ResearchState.NOT_STARTED);
                Network.getNetwork().sendToServer(new TryResearchMessage(building, cancelResearch.getId(), cancelResearch.getBranch(), true));
                close();
            }
        }
        else if (IGlobalResearchTree.getInstance().getResearch(branch, button.getID()) != null
                   && (building.getBuildingLevel() >= IGlobalResearchTree.getInstance().getResearch(branch, button.getID()).getDepth()
                         || building.getBuildingLevel() == building.getBuildingMaxLevel()))
        {
            final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(branch, button.getID());
            final ILocalResearch localResearch = building.getColony().getResearchManager().getResearchTree().getResearch(branch, research.getId());
            if (localResearch == null && building.getBuildingLevel() > building.getColony().getResearchManager().getResearchTree().getResearchInProgress().size() &&
                  (research.hasEnoughResources(new InvWrapper(Minecraft.getInstance().player.inventory)) || (mc.player.isCreative())))
            {
                // This side won't actually start research; it'll be overridden the next colony update from the server.
                // It will, however, update for the next WindowResearchTree if the colony update is slow to come back.
                // Again, the server will prevent someone from paying items twice, but this avoids some confusion.
                research.startResearch(building.getColony().getResearchManager().getResearchTree());
                // don't need to offset count here, as the startResearch will pad it until the new Colony data comes in.
                last.updateResearchCount(0);
                if (research.getDepth() > building.getBuildingMaxLevel())
                {
                    hasMax = true;
                }
                Network.getNetwork().sendToServer(new TryResearchMessage(building, research.getId(), research.getBranch(), false));
                close();
            }
            else if (localResearch != null)
            {
                if (localResearch.getState() == ResearchState.IN_PROGRESS)
                {
                    drawUndoProgressButton(button);
                }
                if (localResearch.getState() == ResearchState.FINISHED)
                {
                    if (hasMax && research.getDepth() > building.getBuildingMaxLevel() && building.getBuildingLevel() == building.getBuildingMaxLevel() && !research.isImmutable())
                    {
                        drawUndoCompleteButton(button);
                    }
                    for (String childId : research.getChildren())
                    {
                        if (building.getColony().getResearchManager().getResearchTree().getResearch(branch, childId) != null
                              && building.getColony().getResearchManager().getResearchTree().getResearch(branch, childId).getState() != ResearchState.NOT_STARTED)
                        {
                            return;
                        }
                    }
                    String parentId = IGlobalResearchTree.getInstance().getResearch(branch, research.getId()).getParent();
                    while (!parentId.isEmpty())
                    {
                        if (IGlobalResearchTree.getInstance().getResearch(branch, parentId) != null
                              && IGlobalResearchTree.getInstance().getResearch(branch, parentId).hasOnlyChild())
                        {
                            drawUndoCompleteButton(button);
                            break;
                        }
                        parentId = IGlobalResearchTree.getInstance().getResearch(branch, parentId).getParent();
                    }
                }
            }
        }
        // Cancel the entire WindowResearchTree
        else if (button.getID().equals("cancel"))
        {
            this.close();
            last.open();
        }
    }

    /**
     * Draw the tree of research.
     *
     * @param height       the start y offset.
     * @param depth        the current depth.
     * @param view         the view to append it to.
     * @param researchList the list of research to go through.
     * @param abandoned    if abandoned child.
     * @return the next y offset.
     */
    public int drawTree(
      final int height,
      final int depth,
      final ZoomDragView view,
      final List<String> researchList,
      final boolean abandoned)
    {
        // Data Pack items load non-deterministically, and the underlying researchTree hashmap doesn't guarantee return of items in any specific order.
        // Sort by the number on the "sortOrder" tag if present to allow control of display order.
        researchList.sort(Comparator.comparing(unsortedResearch -> IGlobalResearchTree.getInstance().getResearch(branch, unsortedResearch).getSortOrder()));

        int nextHeight = height;
        for (int i = 0; i < researchList.size(); i++)
        {
            if (i > 0)
            {
                nextHeight++;
            }

            final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(branch, researchList.get(i));
            if (research.isHidden() && !IGlobalResearchTree.getInstance().isResearchRequirementsFulfilled(research.getResearchRequirement(), this.building.getColony()))
            {
                continue;
            }

            final int offsetX = (depth * (GRADIENT_WIDTH + X_SPACING)) + INITIAL_X_OFFSET + ICON_X_OFFSET;
            final int offsetY = nextHeight * (GRADIENT_HEIGHT + Y_SPACING) + Y_SPACING + TIMELABEL_Y_POSITION;

            final boolean trueAbandoned = drawResearchItem(view, offsetX, offsetY, research, abandoned);

            if (!research.getParent().isEmpty())
            {
                drawArrows(view, offsetX - X_SPACING, offsetY - NAME_LABEL_HEIGHT, researchList.size(), research.getParent(), i, nextHeight, height);
            }
            if (!research.getChildren().isEmpty())
            {
                nextHeight =
                  drawTree(nextHeight, depth + 1, view, research.getChildren(), trueAbandoned);
            }
        }
        return nextHeight;
    }

    /**
     * Draw the background gradients and labels for the research tree.
     *
     * @param view          the view to append it to.
     * @param maxHeight     the largest height value of research on the view.
     */
    private void drawTreeBackground(final ZoomDragView view, final int maxHeight)
    {
        for (int i = 1; i <= MAX_DEPTH; i++)
        {
            final Label timeLabel = new Label();
            timeLabel.setSize(TIME_WIDTH, TIME_HEIGHT);
            timeLabel.setLabelText(new TranslationTextComponent("com.minecolonies.coremod.gui.research.tier.header",
              (i > building.getBuildingMaxLevel()) ? building.getBuildingMaxLevel() : i,
              (IGlobalResearchTree.getInstance().getBranchTime(branch) * Math.pow(2, i - 1))));
            timeLabel.setPosition((i - 1) * (GRADIENT_WIDTH + X_SPACING) + GRADIENT_WIDTH / 2 - TIME_WIDTH / 4, TIMELABEL_Y_POSITION);
            if (building.getBuildingLevel() < i && (building.getBuildingLevel() != building.getBuildingMaxLevel() || hasMax))
            {
                final Gradient gradient = new Gradient();
                gradient.setGradientStart(80, 80, 80, 100);
                gradient.setGradientEnd(60, 60, 60, 110);
                // Draw the last gradient beyond the edge of the displayed area, to avoid blank spot on the right.
                gradient.setSize(i == MAX_DEPTH ? 400 : GRADIENT_WIDTH + X_SPACING, (maxHeight + 4) * (GRADIENT_HEIGHT + Y_SPACING) + Y_SPACING + TIMELABEL_Y_POSITION);
                gradient.setPosition((i - 1) * (GRADIENT_WIDTH + X_SPACING), 0);
                view.getChildren().add(0,gradient);
                timeLabel.setColor(COLOR_TEXT_NEGATIVE, COLOR_TEXT_NEGATIVE);
            }
            else
            {
                timeLabel.setColor(COLOR_TEXT_LABEL, COLOR_TEXT_LABEL);
            }
            view.addChild(timeLabel);
        }
    }

    /**
     * Draw the entirety of an individual research item on a tree, including icons and tooltips.
     *
     * @param view      the view to append it to.
     * @param offsetX   the horizontal offset of the left side of the research block.
     * @param offsetY   the vertical offset of the top side of the research block.
     * @param research  the global research characteristics to draw.
     * @param abandoned the abandoned status of the parent of the research, if one is present.
     * @return abandoned status, true if the research is blocked in the local colony the completion of a sibling research, or an ancestor's sibling's research.
     */
    private boolean drawResearchItem(final ZoomDragView view, final int offsetX, final int offsetY, final IGlobalResearch research, boolean abandoned)
    {
        final ILocalResearchTree tree = building.getColony().getResearchManager().getResearchTree();
        final boolean parentResearched = tree.hasCompletedResearch(research.getParent());
        final IGlobalResearch parentResearch = IGlobalResearchTree.getInstance().getResearch(branch, research.getParent());
        final ResearchState state = tree.getResearch(branch, research.getId()) == null ? ResearchState.NOT_STARTED : tree.getResearch(branch, research.getId()).getState();
        final int progress = tree.getResearch(branch, research.getId()) == null ? 0 : tree.getResearch(branch, research.getId()).getProgress();

        if (mc.player.isCreative() && state == ResearchState.IN_PROGRESS && MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchCreativeCompletion.get()
              && progress < BASE_RESEARCH_TIME * IGlobalResearchTree.getInstance().getBranchTime(branch) * Math.pow(2, research.getDepth() - 1))
        {
            Network.getNetwork().sendToServer(new TryResearchMessage(building, research.getId(), research.getBranch(), false));
        }

        if (research.getDepth() != 1 && (state != ResearchState.FINISHED && state != ResearchState.IN_PROGRESS)
              && parentResearch.hasOnlyChild() && parentResearch.hasResearchedChild(tree))
        {
            abandoned = true;
        }

        drawResearchBox(view, offsetX, offsetY, state, research, progress, parentResearched, abandoned);
        drawResearchLabels(view, offsetX, offsetY, research, state, progress);
        drawResearchReqsAndCosts(view, offsetX, offsetY, research, state, abandoned);
        drawResearchIcons(view, offsetX, offsetY, research, state, parentResearched, abandoned);
        drawResearchButtons(view, offsetX, offsetY, research, state, parentResearched, abandoned);

        return abandoned;
    }

    /**
     * Draw the container block of an individual research item on a tree.
     *
     * @param view             the view to append it to.
     * @param offsetX          the horizontal offset of the left side of the research block.
     * @param offsetY          the vertical offset of the top side of the research block.
     * @param state            the status of the selected research.
     * @param research         the research's traits.
     * @param progress         the progress toward research completion.
     * @param parentResearched if the parent
     * @param abandoned        the abandoned status of the research.
     */
    private void drawResearchBox(
      final ZoomDragView view,
      final int offsetX,
      final int offsetY,
      final ResearchState state,
      final IGlobalResearch research,
      final int progress,
      final boolean parentResearched,
      final boolean abandoned)
    {
        final Image nameBar = new Image();
        // Pad the nameBar vertical size a little, to make shadow overlap onto subBar if present.
        nameBar.setSize(NAME_LABEL_WIDTH, NAME_LABEL_HEIGHT + 3);
        nameBar.setPosition(offsetX, offsetY);

        final Image iconBox = new Image();
        iconBox.setSize(ICON_WIDTH + ICON_X_OFFSET * 2, ICON_HEIGHT + ICON_Y_OFFSET * 2);
        iconBox.setPosition(offsetX - ICON_X_OFFSET, offsetY - ICON_Y_OFFSET);

        if (state == ResearchState.FINISHED)
        {
            nameBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_medium_light_green.png"));
            iconBox.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_mini_light_green.png"));
            view.addChild(nameBar);
            view.addChild(iconBox);
            return;
        }

        final Image subBar = new Image();
        subBar.setPosition(offsetX + (ICON_WIDTH / 2), offsetY + NAME_LABEL_HEIGHT);
        subBar.setSize(RESEARCH_WIDTH - ICON_X_OFFSET * 2 - TEXT_X_OFFSET, RESEARCH_HEIGHT - NAME_LABEL_HEIGHT);
        view.addChild(subBar);
        if (state == ResearchState.IN_PROGRESS)
        {
            nameBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium.png"));
            subBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium.png"));
            iconBox.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_mini_light_green.png"));
            final Gradient nameGradient = new Gradient();
            nameGradient.setSize(NAME_LABEL_WIDTH, NAME_LABEL_HEIGHT - ICON_X_OFFSET);
            nameGradient.setPosition(offsetX, offsetY);
            view.addChild(nameGradient);
            nameGradient.setGradientStart(102, 225, 80, 60);
            nameGradient.setGradientEnd(102, 225, 80, 60);

            // scale down subBar to fit smaller progress text, and make gradients of scale to match progress.
            final double progressRatio = (progress + 1) / (Math.pow(2, research.getDepth() - 1)
                                                             * (double) BASE_RESEARCH_TIME * IGlobalResearchTree.getInstance().getBranchTime(branch));
            subBar.setSize(RESEARCH_WIDTH - ICON_X_OFFSET * 2 - TEXT_X_OFFSET, TIME_HEIGHT);
            nameGradient.setSize((int) (progressRatio * nameBar.getWidth()), NAME_LABEL_HEIGHT);

            final Gradient subGradient = new Gradient();
            subGradient.setPosition(offsetX + (ICON_WIDTH / 2), offsetY + NAME_LABEL_HEIGHT);
            subGradient.setSize(subBar.getWidth(), subBar.getHeight());
            // remove a little bit of size from the subGradient height to avoid overlapping onto the shadows of subBar.
            subGradient.setSize((int) (progressRatio * subBar.getWidth()), TIME_HEIGHT - 1);
            subGradient.setGradientStart(230, 200, 20, 60);
            subGradient.setGradientEnd(230, 200, 20, 60);
            view.addChild(subGradient);
        }
        else if (abandoned || !parentResearched || (research.getDepth() > building.getBuildingLevel() && !(research.getDepth() > building.getBuildingMaxLevel()
                                                                                                             && !hasMax
                                                                                                             && building.getBuildingLevel() == building.getBuildingMaxLevel())))
        {
            iconBox.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_mini_light_gray.png"));
            subBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium.png"));
            nameBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_medium_light_gray.png"));
        }
        else if (!IGlobalResearchTree.getInstance().isResearchRequirementsFulfilled(research.getResearchRequirement(), building.getColony()) ||
                   !research.hasEnoughResources(new InvWrapper(Minecraft.getInstance().player.inventory)))
        {
            iconBox.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_mini.png"));
            nameBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium.png"));
            subBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium.png"));
        }
        // eligible to begin research.
        else
        {
            iconBox.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_mini_blue.png"));
            subBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium.png"));
            nameBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_medium_blue.png"));
        }
        view.addChild(subBar);
        view.addChild(nameBar);
        view.addChild(iconBox);
    }

    /**
     * Generates and attaches tooltips for a given research to input tipItem.
     *
     * @param tipItem  the Pane to apply the tooltip.
     * @param research the global research characteristics to draw.
     * @param state    the status of the selected research.
     */
    private void generateResearchTooltips(final Pane tipItem, final IGlobalResearch research, final ResearchState state)
    {
        final List<IFormattableTextComponent> hoverTexts = new ArrayList<>();
        hoverTexts.add(new TranslationTextComponent(research.getName()).setStyle(Style.EMPTY.setBold(true).setFormatting(TextFormatting.GOLD)));
        if (!research.getSubtitle().isEmpty())
        {
            hoverTexts.add(new TranslationTextComponent(research.getSubtitle()).setStyle(Style.EMPTY.setItalic(true).setFormatting(TextFormatting.GRAY)));
        }
        for (int txt = 0; txt < research.getEffects().size(); txt++)
        {
            hoverTexts.add(research.getEffects().get(txt).getDesc());
            if (!research.getEffects().get(txt).getSubtitle().isEmpty())
            {
                hoverTexts.add(new StringTextComponent("-").append(new TranslationTextComponent(research.getEffects().get(txt).getSubtitle())));
            }
        }
        if (state != ResearchState.FINISHED)
        {
            for (int txt = 0; txt < research.getResearchRequirement().size(); txt++)
            {
                if (research.getResearchRequirement().get(txt).isFulfilled(this.building.getColony()))
                {
                    hoverTexts.add(new TranslationTextComponent(" - ").append(research.getResearchRequirement()
                                                                                .get(txt)
                                                                                .getDesc()
                                                                                .setStyle(Style.EMPTY.setFormatting(TextFormatting.AQUA))));
                }
                else
                {
                    hoverTexts.add(new TranslationTextComponent(" - ").append(research.getResearchRequirement()
                                                                                .get(txt)
                                                                                .getDesc()
                                                                                .setStyle(Style.EMPTY.setFormatting(TextFormatting.RED))));
                }
            }
            if (research.getDepth() > building.getBuildingLevel() && building.getBuildingLevel() != building.getBuildingMaxLevel())
            {
                hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.requirement.university.level", research.getDepth()));
            }
            if (research.getDepth() == MAX_DEPTH)
            {
                if (hasMax)
                {
                    hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.limit.onemaxperbranch").setStyle(Style.EMPTY.setFormatting(TextFormatting.GOLD)));
                }
                else
                {
                    hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.limit.onemaxperbranch").setStyle(Style.EMPTY.setFormatting(TextFormatting.RED)));
                }
            }
        }
        if (research.isImmutable())
        {
            hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.limit.immutable").setStyle(Style.EMPTY.setFormatting(TextFormatting.RED)));
        }
        tipItem.setHoverToolTip(hoverTexts);
    }

    /**
     * Draw the labels for a given research
     *
     * @param view     the view to append it to.
     * @param offsetX  the horizontal offset of the left side of the research block.
     * @param offsetY  the vertical offset of the top side of the research block.
     * @param research the global research characteristics to draw.
     * @param state    the local research's state.
     * @param progress the progress toward research completion.
     */
    private void drawResearchLabels(final ZoomDragView view, final int offsetX, final int offsetY, final IGlobalResearch research, final ResearchState state, final int progress)
    {
        final Label nameLabel = new Label();
        nameLabel.setSize(BUTTON_LENGTH, INITIAL_Y_OFFSET);
        nameLabel.setLabelText(new TranslationTextComponent(research.getName()));
        nameLabel.setPosition(offsetX + ICON_WIDTH + TEXT_X_OFFSET, offsetY + TEXT_Y_OFFSET);
        nameLabel.setColor(COLOR_TEXT_DARK, COLOR_TEXT_DARK);
        nameLabel.setScale(1.4f);
        view.addChild(nameLabel);

        if (state == ResearchState.IN_PROGRESS)
        {
            final double progressToGo =
              (int) (Math.pow(2, research.getDepth() - 1) * (double) BASE_RESEARCH_TIME * IGlobalResearchTree.getInstance().getBranchTime(branch)) - progress;
            // Write out the rough remaining time for the research.
            // This will necessarily be an estimate, since adjusting for
            // daytime cycles or simple worker travel time would be a nightmare.
            // With those caveats, treat BASE_RESEARCH_TIME as _roughly_ equal to
            // one half-hour, and we're going to round up to increments of fifteen minutes.
            final int hours = (int) (progressToGo / (BASE_RESEARCH_TIME * 2));
            final int increments = (int) Math.ceil(progressToGo % (BASE_RESEARCH_TIME * 2) / (BASE_RESEARCH_TIME / 2d));
            // TranslationTextComponents don't play well with advanced Java format() tricks,
            // so we'll map just the numeric parts of the string.
            final String timeRemaining;
            if (increments == 4)
            {
                timeRemaining = String.format("%d:%02d", hours + 1, 0);
            }
            else
            {
                timeRemaining = String.format("%d:%02d", hours, increments * 15);
            }
            final Label progressLabel = new Label();
            progressLabel.setSize(BUTTON_LENGTH, INITIAL_Y_OFFSET);
            progressLabel.setLabelText(new TranslationTextComponent("com.minecolonies.coremod.gui.research.time", timeRemaining));
            progressLabel.setPosition(offsetX + ICON_WIDTH + TEXT_X_OFFSET, offsetY + NAME_LABEL_HEIGHT);
            progressLabel.setColor(COLOR_TEXT_DARK, COLOR_TEXT_DARK);
            progressLabel.setScale(0.7f);
            view.addChild(progressLabel);
        }
    }

    /**
     * Draw the buttons and button images for a given research.
     *
     * @param view             the view to append it to.
     * @param offsetX          the horizontal offset of the left side of the research block.
     * @param offsetY          the vertical offset of the top side of the research block.
     * @param research         the global research characteristics to draw.
     * @param state            the research state.
     * @param parentResearched if the parent research has been completed.
     * @param abandoned        if the research or an ancestor research has a completed sibling preventing it from being studied.
     */
    private void drawResearchButtons(
      final ZoomDragView view, final int offsetX, final int offsetY, final IGlobalResearch research,
      final ResearchState state, final boolean parentResearched, final boolean abandoned)
    {
        final Button iconButton = new Button();
        iconButton.setSize(ICON_WIDTH + ICON_X_OFFSET * 2, ICON_HEIGHT + ICON_Y_OFFSET * 2);
        iconButton.setPosition(offsetX - ICON_X_OFFSET, offsetY - ICON_Y_OFFSET);
        view.addChild(iconButton);

        final Button hoverButton = new Button();
        hoverButton.setSize(RESEARCH_WIDTH + ICON_X_OFFSET, NAME_LABEL_HEIGHT);
        hoverButton.setPosition(offsetX + INITIAL_X_OFFSET - ICON_X_OFFSET, offsetY);
        view.addChild(hoverButton);
        generateResearchTooltips(hoverButton, research, state);

        if (!abandoned && parentResearched && building.getBuildingLevel() <= building.getColony().getResearchManager().getResearchTree().getResearchInProgress().size())
        {
            ButtonImage tooMany1 = new ButtonImage();
            tooMany1.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
            tooMany1.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.research.toomanyinprogress.1"));
            tooMany1.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
            tooMany1.setPosition(offsetX + ICON_WIDTH * 2, offsetY + BUTTON_HEIGHT);
            view.addChild(tooMany1);
            final ButtonImage tooMany2 = new ButtonImage();
            tooMany2.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
            tooMany2.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.research.toomanyinprogress.2"));
            tooMany2.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
            tooMany2.setPosition(offsetX + ICON_WIDTH * 2, offsetY + BUTTON_HEIGHT * 2);
            view.addChild(tooMany2);
        }

        if (!abandoned && parentResearched &&
              ((research.getDepth() <= building.getBuildingLevel() || state != ResearchState.NOT_STARTED)
                 || (!hasMax && research.getDepth() > building.getBuildingMaxLevel() && building.getBuildingLevel() == building.getBuildingMaxLevel())))
        {
            iconButton.setID(research.getId());
            hoverButton.setID(research.getId());
        }
    }

    /**
     * Draw an undo button in the middle of the parent research, and manages associated tooltips for in-progress research. This function sets normal button and tooltip information
     * into the displacedButton and displacedHoverText fields, to allow switch back to normal functionality without having to redraw the entire tree.
     *
     * @param parent the parent button to attach it to.
     */
    private void drawUndoProgressButton(final Button parent)
    {
        undoButton.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
        undoButton.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.undo.progress"));
        undoButton.setTextColor(Color.getByName("black", 0));
        undoButton.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
        undoButton.setPosition(parent.getX() + (GRADIENT_WIDTH - BUTTON_LENGTH) / 2, parent.getY() + (GRADIENT_HEIGHT - BUTTON_HEIGHT) / 2);
        undoButton.setID("undo." + parent.getID());
        List<IFormattableTextComponent> hoverTexts = new ArrayList<>();
        hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.undo.progress.tooltip.1").setStyle(Style.EMPTY.setFormatting(TextFormatting.BOLD)
                                                                                                                            .setFormatting(TextFormatting.RED)));
        hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.undo.progress.tooltip.2").setStyle(Style.EMPTY.setFormatting(TextFormatting.BOLD)
                                                                                                                            .setFormatting(TextFormatting.RED)));
        displacedButton = parent;
        displacedHoverText = new ArrayList<>(parent.getHoverToolTip());
        parent.getHoverToolTip().clear();
        parent.setHoverToolTip(hoverTexts);
        parent.getParent().addChild(undoButton);
    }

    /**
     * Draw an undo button in the middle of the parent research, and manages associated tooltips for completed research. This function sets normal button and tooltip information
     * into the displacedButton and displacedHoverText fields, to allow switch back to normal functionality without having to redraw the entire tree.
     *
     * @param parent the parent button to attach it to.
     */
    private void drawUndoCompleteButton(final Button parent)
    {
        final List<? extends String> costList = IGlobalResearchTree.getInstance().getResearchResetCosts();
        undoCostIcons = new ItemIcon[costList.size()];
        boolean missingItems = false;
        for (int i = 0; i < costList.size(); i++)
        {
            final String[] costParts = costList.get(0).split(":");
            undoCostIcons[i] = new ItemIcon();
            ItemStack is = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(costParts[0], costParts[1])));
            if (InventoryUtils.getItemCountInItemHandler(new InvWrapper(Minecraft.getInstance().player.inventory),
              stack -> !ItemStackUtils.isEmpty(stack) && stack.isItemEqual(is)) < Integer.parseInt(costParts[2]))
            {
                missingItems = true;
            }
            is.setCount(Integer.parseInt(costParts[2]));
            undoCostIcons[i].setItem(is);
            undoCostIcons[i].setPosition(parent.getX() + (GRADIENT_WIDTH - BUTTON_LENGTH) / 2 + BUTTON_LENGTH + DEFAULT_COST_SIZE * i,
              parent.getY() + (GRADIENT_HEIGHT - BUTTON_HEIGHT) / 2);
            undoCostIcons[i].setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            parent.getParent().addChild(undoCostIcons[0]);
        }
        undoButton.setTextColor(Color.getByName("black", 0));
        undoButton.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
        undoButton.setPosition(parent.getX() + (GRADIENT_WIDTH - BUTTON_LENGTH) / 2, parent.getY() + (GRADIENT_HEIGHT - BUTTON_HEIGHT) / 2);
        final List<IFormattableTextComponent> hoverTexts = new ArrayList<>();
        hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.undo.remove.tooltip").setStyle(Style.EMPTY.setFormatting(TextFormatting.BOLD)
                                                                                                                        .setFormatting(TextFormatting.RED)));
        if (missingItems)
        {
            undoButton.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
            undoButton.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.research.notenoughresources"));
            for (String cost : IGlobalResearchTree.getInstance().getResearchResetCosts())
            {
                final String[] costParts = cost.split(":");
                hoverTexts.add(new TranslationTextComponent("com.minecolonies.coremod.research.requirement.research",
                  ForgeRegistries.ITEMS.getValue(new ResourceLocation(costParts[0], costParts[1])).getName()));
            }
        }
        else
        {
            undoButton.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
            undoButton.setLabel(new TranslationTextComponent("com.minecolonies.coremod.research.undo.remove"));
            undoButton.setID("undo." + parent.getID());
        }

        displacedButton = parent;
        displacedHoverText = new ArrayList<>(parent.getHoverToolTip());
        parent.getHoverToolTip().clear();
        parent.setHoverToolTip(hoverTexts);
        parent.getParent().addChild(undoButton);
    }

    /**
     * Draw the progress bar for a given research.
     *
     * @param view      the view to append it to.
     * @param offsetX   the horizontal offset of the left side of the research block.
     * @param offsetY   the vertical offset of the top side of the research block.
     * @param research  the global research characteristics to draw.
     * @param state     the research's current state.
     * @param abandoned if the research can not be initiated, because of a completed sibling or ancestor's sibling research.
     */
    public void drawResearchReqsAndCosts(
      final ZoomDragView view,
      final int offsetX,
      final int offsetY,
      final IGlobalResearch research,
      final ResearchState state,
      final boolean abandoned)
    {
        if (!abandoned && (state != ResearchState.IN_PROGRESS && state != ResearchState.FINISHED))
        {
            int storageXOffset = ICON_WIDTH;

            for (final IResearchRequirement requirement : research.getResearchRequirement())
            {
                if (requirement instanceof AlternateBuildingResearchRequirement)
                {
                    for (Map.Entry<String, Integer> building : ((AlternateBuildingResearchRequirement) requirement).getBuildings().entrySet())
                    {
                        final Item item;
                        if (IMinecoloniesAPI.getInstance().getBuildingRegistry().containsKey(
                          new ResourceLocation(Constants.MOD_ID, building.getKey())))
                        {
                            item = IMinecoloniesAPI.getInstance().getBuildingRegistry().getValue(
                              new ResourceLocation(Constants.MOD_ID, building.getKey())).getBuildingBlock().asItem();
                        }
                        else
                        {
                            item = Items.AIR.asItem();
                        }
                        final ItemStack stack = new ItemStack(item);
                        stack.setCount(building.getValue());
                        final ItemIcon icon = new ItemIcon();
                        icon.setItem(stack);
                        icon.setPosition(offsetX + storageXOffset, offsetY + NAME_LABEL_HEIGHT);
                        icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                        view.addChild(icon);

                        storageXOffset += COST_OFFSET;
                    }
                    // There will only ever be one AlternateBuildingRequirement per research, under the current implementation.
                    break;
                }
            }
            // If there are more than one requirement, we want a clear divider before normal building research requirements.
            if (storageXOffset > ICON_WIDTH + COST_OFFSET)
            {
                final Image divider = new Image();
                divider.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_large_stitches.png"));
                divider.setSize(ICON_X_OFFSET, Y_SPACING);
                divider.setPosition(offsetX + storageXOffset, offsetY + NAME_LABEL_HEIGHT + 4);
                view.addChild(divider);
                storageXOffset += ICON_X_OFFSET;
            }

            for (final IResearchRequirement requirement : research.getResearchRequirement())
            {
                if (requirement instanceof BuildingResearchRequirement)
                {
                    final Item item;
                    if (IMinecoloniesAPI.getInstance().getBuildingRegistry().containsKey(
                      new ResourceLocation(Constants.MOD_ID, ((BuildingResearchRequirement) requirement).getBuilding())))
                    {
                        item = IMinecoloniesAPI.getInstance().getBuildingRegistry().getValue(
                          new ResourceLocation(Constants.MOD_ID, ((BuildingResearchRequirement) requirement).getBuilding())).getBuildingBlock().asItem();
                    }
                    else
                    {
                        item = Items.AIR.asItem();
                    }
                    final ItemStack stack = new ItemStack(item);
                    stack.setCount(((BuildingResearchRequirement) requirement).getBuildingLevel());
                    final ItemIcon icon = new ItemIcon();
                    icon.setItem(stack);
                    icon.setPosition(offsetX + storageXOffset, offsetY + NAME_LABEL_HEIGHT + TEXT_Y_OFFSET);
                    icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                    view.addChild(icon);

                    storageXOffset += COST_OFFSET;
                }
            }
            // If there are two or more research requirements, we want a clear divider before the cost side.
            // Again, there can only be one alternate-building requirement, so this would indicate at least two of building or research requirements.
            if (research.getResearchRequirement().size() >= 2)
            {
                final Image divider = new Image();
                divider.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_large_stitches.png"));
                divider.setSize(ICON_X_OFFSET, Y_SPACING);
                divider.setPosition(offsetX + storageXOffset, offsetY + NAME_LABEL_HEIGHT + 4);
                view.addChild(divider);
            }

            storageXOffset = COST_OFFSET;
            for (final ItemStorage storage : research.getCostList())
            {
                final ItemStack stack = storage.getItemStack().copy();
                stack.setCount(storage.getAmount());
                final ItemIcon icon = new ItemIcon();
                icon.setItem(stack);
                icon.setPosition(offsetX + RESEARCH_WIDTH - storageXOffset - INITIAL_X_OFFSET, offsetY + NAME_LABEL_HEIGHT + TEXT_Y_OFFSET);
                icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                view.addChild(icon);

                storageXOffset += COST_OFFSET;
            }
        }
    }

    /**
     * Draw icons for a specific research, showing the research's readiness state.
     * @param view              View to attach the icons to.
     * @param offsetX           Horizontal offset for the research.
     * @param offsetY           Vertical offset for the reserach.
     * @param research          Global research information.
     * @param state             State of the local research, if begun.
     * @param parentResearched  If the parent research has been completed.
     * @param abandoned         If a sibling or ancestor research blocks this research.
     */
    public void drawResearchIcons(
      final ZoomDragView view,
      final int offsetX,
      final int offsetY,
      final IGlobalResearch research,
      final ResearchState state,
      final boolean parentResearched,
      final boolean abandoned)
    {
        if (research.isImmutable() && state != ResearchState.FINISHED)
        {
            final Image immutIcon = new Image();
            immutIcon.setImage(new ResourceLocation("minecraft", "textures/block/redstone_torch.png"));
            immutIcon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            immutIcon.setPosition(offsetX + GRADIENT_WIDTH - DEFAULT_COST_SIZE, offsetY);
            view.addChild(immutIcon);
        }
        if (state == ResearchState.FINISHED && !research.getIcon().isEmpty() && DRAW_ICONS)
        {
            final String[] iconParts = research.getIcon().split(":");
            if (research.getIcon().contains("."))
            {
                final Image icon = new Image();
                icon.setImage(new ResourceLocation(iconParts[0], iconParts[1]));
                icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                icon.setPosition(offsetX, offsetY);
                view.addChild(icon);
            }
            else
            {
                ItemIcon iconItem = new ItemIcon();
                if (iconParts.length == 2 || iconParts.length == 3)
                {
                    ItemStack is = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(iconParts[0], iconParts[1])));
                    iconItem.setItem(is);
                    if (iconParts.length == 3)
                    {
                        is.setCount(Integer.parseInt(iconParts[2]));
                    }
                }
                iconItem.setPosition(offsetX, offsetY);
                iconItem.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                view.addChild(iconItem);
            }
        }
        else if (state == ResearchState.FINISHED)
        {
            final Image icon = new Image();
            icon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/icon_check.png"));
            icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            icon.setPosition(offsetX, offsetY);
            view.addChild(icon);
        }
        else if (state == ResearchState.IN_PROGRESS)
        {
            final Image icon = new Image();
            icon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/icon_cancel.png"));
            icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            icon.setPosition(offsetX, offsetY);
            view.addChild(icon);
        }
        // requires another research be started or reset.
        else if (!parentResearched || abandoned)
        {
            final Image lockIcon = new Image();
            lockIcon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/locked_icon_light_gray.png"));
            lockIcon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            lockIcon.setPosition(offsetX, offsetY);
            view.addChild(lockIcon);
        }
        // is missing a required building or research.
        else if (!IGlobalResearchTree.getInstance().isResearchRequirementsFulfilled(research.getResearchRequirement(), building.getColony()))
        {
            final Image lockIcon = new Image();
            lockIcon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/locked_icon_light_gray.png"));
            lockIcon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            lockIcon.setPosition(offsetX, offsetY);
            view.addChild(lockIcon);
        }
        // has everything but the item cost requirements.
        else if (!research.hasEnoughResources(new InvWrapper(Minecraft.getInstance().player.inventory)))
        {
            final Image lockIcon = new Image();
            lockIcon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/locked_icon_unlocked_blue.png"));
            lockIcon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            lockIcon.setPosition(offsetX, offsetY);
            view.addChild(lockIcon);
        }
        else
        {
            final Image icon = new Image();
            icon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/icon_start.png"));
            icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            icon.setPosition(offsetX, offsetY);
            view.addChild(icon);
        }
    }

    /**
     * Draws arrows connecting sibling researches to their parent.
     *
     * @param view             the view to append it to.
     * @param offsetX          the horizontal offset of the left side of the research block.
     * @param offsetY          the vertical offset of the top side of the research block.
     * @param researchListSize the number of sibling researches.
     * @param parentResearch   the parent research to connect by arrow.
     * @param currentCounter   count of the current target.
     * @param nextHeight       height of the next arrow target.
     * @param parentHeight     height of the parent arrow target.
     */
    public void drawArrows(
      final ZoomDragView view,
      final int offsetX,
      final int offsetY,
      final int researchListSize,
      final String parentResearch,
      final int currentCounter,
      final int nextHeight,
      final int parentHeight)
    {
        final boolean firstSibling = currentCounter == 0;
        final boolean secondSibling = currentCounter >= 1;

        final boolean lastSibling = currentCounter + 1 >= researchListSize;

        if (firstSibling && lastSibling)
        {
            final Image corner = new Image();
            corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right.png"));
            corner.setSize(X_SPACING - ICON_X_OFFSET, GRADIENT_HEIGHT);
            corner.setPosition(offsetX, offsetY);
            view.addChild(corner);
        }
        else
        {
            if (secondSibling)
            {
                for (int dif = 1; dif < nextHeight - parentHeight; dif++)
                {
                    final Image corner = new Image();
                    corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_down.png"));
                    corner.setSize(X_SPACING - ICON_X_OFFSET, GRADIENT_HEIGHT + Y_SPACING);
                    corner.setPosition(offsetX - ICON_X_OFFSET, offsetY - (dif * corner.getHeight()));
                    view.addChild(corner);
                }
            }

            if (firstSibling)
            {
                final Image corner = new Image();
                corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right_down.png"));
                corner.setSize(X_SPACING - ICON_X_OFFSET, GRADIENT_HEIGHT + Y_SPACING);
                corner.setPosition(offsetX - ICON_X_OFFSET, offsetY);
                view.addChild(corner);
            }
            else
            {
                if (IGlobalResearchTree.getInstance().getResearch(branch, parentResearch).hasOnlyChild())
                {
                    final Label orLabel = new Label();
                    orLabel.setSize(OR_WIDTH, OR_HEIGHT);
                    orLabel.setColor(Color.getByName("black", 0), Color.getByName("black", 0));
                    orLabel.setLabelText(new TranslationTextComponent("com.minecolonies.coremod.research.research.or"));
                    orLabel.setPosition(offsetX + INITIAL_X_OFFSET, offsetY + TEXT_Y_OFFSET);
                    view.addChild(orLabel);

                    if (lastSibling)
                    {
                        final Image circle = new Image();
                        circle.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_or.png"));
                        circle.setSize(X_SPACING - ICON_X_OFFSET, GRADIENT_HEIGHT);
                        circle.setPosition(offsetX - ICON_X_OFFSET, offsetY);
                        view.addChild(circle);
                    }
                    else
                    {
                        final Image corner = new Image();
                        corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_or_down.png"));
                        corner.setSize(X_SPACING - ICON_X_OFFSET, GRADIENT_HEIGHT + Y_SPACING);
                        corner.setPosition(offsetX - ICON_X_OFFSET, offsetY + ICON_Y_OFFSET);
                        view.addChild(corner);
                    }
                }
                else
                {
                    final Image corner = new Image();
                    if (lastSibling)
                    {
                        corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right_and.png"));
                        corner.setSize(X_SPACING - ICON_X_OFFSET, GRADIENT_HEIGHT);
                    }
                    else
                    {
                        corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right_and_more.png"));
                        corner.setSize(X_SPACING - ICON_X_OFFSET, GRADIENT_HEIGHT + Y_SPACING);
                    }
                    corner.setPosition(offsetX - ICON_X_OFFSET, offsetY);
                    view.addChild(corner);
                }
            }
        }
    }
}
