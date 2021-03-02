package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ZoomDragView;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.*;
import com.minecolonies.api.research.util.ResearchState;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Log;
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
    private final ResourceLocation branch;

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
     * The undo text, if one is present
     */
    private final Text undoText = new Text();

    /**
     * The undo cost icons, if one is present.
     */
    private ItemIcon[] undoCostIcons = new ItemIcon[0];

    /**
     * The current state of a research button's display status.
     */
    enum ResearchButtonState
    {
        AVAILABLE,
        IN_PROGRESS,
        FINISHED,
        ABANDONED,
        MISSING_PARENT,
        MISSING_REQUIREMENT,
        MISSING_COST,
        TOO_MANY_PROGRESS,
        TOO_LOW_UNIVERSITY,
        LOCKED
    }

    /**
     * Create the research tree window.
     *
     * @param branch   the branch being researched.
     * @param building the associated university.
     * @param last     the GUI we opened this from.
     */
    public WindowResearchTree(final ResourceLocation branch, final BuildingUniversity.View building, final WindowHutUniversity last)
    {
        super(Constants.MOD_ID + R_TREE_RESOURCE_SUFFIX, last);
        this.branch = branch;
        this.building = building;
        this.last = last;
        this.hasMax = false;

        final List<ResourceLocation> researchList = IGlobalResearchTree.getInstance().getPrimaryResearch(branch);
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
        if (button.getParent().getChildren().contains(undoText))
        {
            button.getParent().removeChild(undoText);
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
        else if (button.getID().contains("undo:"))
        {
            final String undoName = button.getID().substring(button.getID().indexOf(':'));
            if(!ResourceLocation.isResouceNameValid(undoName))
            {
                return;
            }
            final ResourceLocation undoID = new ResourceLocation(undoName);
            final ILocalResearch cancelResearch = building.getColony().getResearchManager().getResearchTree().getResearch(branch, undoID);
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
        else if (ResourceLocation.isResouceNameValid(button.getID())
                   && IGlobalResearchTree.getInstance().getResearch(branch, new ResourceLocation(button.getID())) != null
                   && (building.getBuildingLevel() >= IGlobalResearchTree.getInstance().getResearch(branch, new ResourceLocation(button.getID())).getDepth()
                         || building.getBuildingLevel() == building.getBuildingMaxLevel()))
        {
            final IGlobalResearch research = IGlobalResearchTree.getInstance().getResearch(branch, new ResourceLocation(button.getID()));
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
                    for (ResourceLocation childId : research.getChildren())
                    {
                        if (building.getColony().getResearchManager().getResearchTree().getResearch(branch, childId) != null
                              && building.getColony().getResearchManager().getResearchTree().getResearch(branch, childId).getState() != ResearchState.NOT_STARTED)
                        {
                            return;
                        }
                    }
                    ResourceLocation parentId = IGlobalResearchTree.getInstance().getResearch(branch, research.getId()).getParent();
                    while (!parentId.getPath().isEmpty())
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
    private int drawTree(
      final int height,
      final int depth,
      final ZoomDragView view,
      final List<ResourceLocation> researchList,
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

            if (!research.getParent().getPath().isEmpty())
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
            final Text timeLabel = new Text();
            timeLabel.setSize(TIME_WIDTH, TIME_HEIGHT);
            timeLabel.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.research.tier.header",
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
                timeLabel.setColors(COLOR_TEXT_NEGATIVE);
            }
            else
            {
                timeLabel.setColors(COLOR_TEXT_LABEL);
            }
            view.addChild(timeLabel);
        }
    }

    /**
     * Calculates the UI status of a given Research Item.
     * @param abandoned         if a research, or one of its ancestors, has a Parent with OnlyChild set, and one alternate branch already begun or completed.
     * @param parentResearched  if the immediate parent research has been completed.
     * @param research          the global research information for the research.
     * @param state             the current LocalResearchTree ResearchState of the research for the colony.
     * @return                  the current set state of the research for display purposes.
     */
    private ResearchButtonState getResearchButtonState(final boolean abandoned, final boolean parentResearched, final IGlobalResearch research, final ResearchState state)
    {
        // Not available as parent has OnlyChild set, and another child research is complete.
        if (abandoned)
        {
            return ResearchButtonState.ABANDONED;
        }
        // Locked, as parent is not completed.
        else if (!parentResearched)
        {
            return ResearchButtonState.MISSING_PARENT;
        }
        else if (state == ResearchState.FINISHED)
        {
            return ResearchButtonState.FINISHED;
        }
        else if (state == ResearchState.IN_PROGRESS)
        {
            return ResearchButtonState.IN_PROGRESS;
        }
        // If the University too low-level for the research, or if this research is max-level, the building is max level, and another max-level research is completed.
        else if (research.getDepth() > building.getBuildingLevel() && !(research.getDepth() > building.getBuildingMaxLevel() && !hasMax
                    && building.getBuildingLevel() == building.getBuildingMaxLevel()))
        {
            return ResearchButtonState.TOO_LOW_UNIVERSITY;
        }
        else if(mc.player.isCreative())
        {
            return ResearchButtonState.AVAILABLE;
        }
        // is missing a requirement, such as a building, alternate building, or research requirement.
        else if (!IGlobalResearchTree.getInstance().isResearchRequirementsFulfilled(research.getResearchRequirement(), building.getColony()))
        {
            return ResearchButtonState.MISSING_REQUIREMENT;
        }
        // has everything but the item cost requirements.
        else if (!research.hasEnoughResources(new InvWrapper(Minecraft.getInstance().player.inventory)))
        {
            return ResearchButtonState.MISSING_COST;
        }
        // is valid to begin.
        else
        {
            return ResearchButtonState.AVAILABLE;
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

        final ResearchButtonState researchState = getResearchButtonState(abandoned, parentResearched, research, state);

        drawResearchBoxes(view, offsetX, offsetY, research, researchState, progress);
        drawResearchReqsAndCosts(view, offsetX, offsetY, research, researchState);
        drawResearchIcons(view, offsetX, offsetY, research, researchState);
        drawResearchTexts(view, offsetX, offsetY, research, researchState, progress);

        return abandoned;
    }

    /**
     * Draw the container block of an individual research item on a tree.
     *
     * @param view             the view to append it to.
     * @param offsetX          the horizontal offset of the left side of the research block.
     * @param offsetY          the vertical offset of the top side of the research block.
     * @param research         the research's traits.
     * @param state            the status of the selected research.
     * @param progress         the progress toward research completion.
     */
    private void drawResearchBoxes(
      final ZoomDragView view,
      final int offsetX,
      final int offsetY,
      final IGlobalResearch research,
      final ResearchButtonState state,
      final int progress)
    {
        final ButtonImage nameBar = new ButtonImage();
        // Pad the nameBar vertical size a little, to make shadow overlap onto subBar if present.
        nameBar.setSize(NAME_LABEL_WIDTH, NAME_LABEL_HEIGHT + 3);
        nameBar.setPosition(offsetX, offsetY);

        final ButtonImage iconBox = new ButtonImage();
        iconBox.setSize(ICON_WIDTH + ICON_X_OFFSET * 2, ICON_HEIGHT + ICON_Y_OFFSET * 2);
        iconBox.setPosition(offsetX - ICON_X_OFFSET, offsetY - ICON_Y_OFFSET);

        final Image subBar = new Image();
        subBar.setPosition(offsetX + (ICON_WIDTH / 2), offsetY + NAME_LABEL_HEIGHT);
        subBar.setSize(RESEARCH_WIDTH - ICON_X_OFFSET * 2 - TEXT_X_OFFSET, RESEARCH_HEIGHT - NAME_LABEL_HEIGHT);

        if(state != ResearchButtonState.FINISHED)
        {
            view.addChild(subBar);
        }
        view.addChild(nameBar);
        view.addChild(iconBox);

        switch (state)
        {
            case AVAILABLE:
                nameBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_medium_blue.png"));
                subBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_sub_medium.png"));
                iconBox.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_mini_blue.png"));
                nameBar.setID(research.getId().toString());
                iconBox.setID(research.getId().toString());
                break;
            case IN_PROGRESS:
                nameBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_medium_light_green.png"));
                subBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_sub_thin.png"));
                iconBox.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_mini_light_green.png"));
                nameBar.setID(research.getId().toString());
                iconBox.setID(research.getId().toString());
                drawProgressBar(view, offsetX, offsetY, research, progress, subBar);
                break;
            case FINISHED:
                nameBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_medium_light_green.png"));
                iconBox.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_mini_light_green.png"));
                nameBar.setID(research.getId().toString());
                iconBox.setID(research.getId().toString());
                break;
            case LOCKED:
            case ABANDONED:
            case MISSING_PARENT:
            case TOO_LOW_UNIVERSITY:
                nameBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_medium_light_gray.png"));
                subBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_sub_medium.png"));
                iconBox.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_mini_light_gray.png"));
                break;
            case TOO_MANY_PROGRESS:
                ButtonImage tooMany1 = new ButtonImage();
                tooMany1.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
                tooMany1.setText(new TranslationTextComponent("com.minecolonies.coremod.research.research.toomanyinprogress.1"));
                tooMany1.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
                tooMany1.setPosition(offsetX + ICON_WIDTH * 2, offsetY + BUTTON_HEIGHT);
                view.addChild(tooMany1);
                final ButtonImage tooMany2 = new ButtonImage();
                tooMany2.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
                tooMany2.setText(new TranslationTextComponent("com.minecolonies.coremod.research.research.toomanyinprogress.2"));
                tooMany2.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
                tooMany2.setPosition(offsetX + ICON_WIDTH * 2, offsetY + BUTTON_HEIGHT * 2);
                view.addChild(tooMany2);
            case MISSING_REQUIREMENT:
            case MISSING_COST:
                nameBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_medium_plain.png"));
                subBar.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_sub_medium.png"));
                iconBox.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/research_button_mini.png"));
                break;
            default:
                Log.getLogger().error("Error in DrawResearchBoxes for " + research.getId() + " state: " + state);
                break;
        }

        // Tooltips can only be assigned /after/ the ButtonImage (or other item) has been added to a Pane.
        generateResearchTooltips(nameBar, research, state);
    }

    /**
     * Draws the progress bar for an in-progress research.
     * @param view          the view to assign the progressbar onto.
     * @param offsetX       the horizontal offset of the containing research.
     * @param offsetY       the vertical offset of the containing research.
     * @param research      the Global research information.
     * @param progress      the numeric absolute progress for the research for the colony.
     * @param subBar        the bar to overlay the gradient over.
     */
    private void drawProgressBar(final ZoomDragView view, final int offsetX, final int offsetY, final IGlobalResearch research, final int progress, final Image subBar)
    {
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
        nameGradient.setSize((int) (progressRatio * NAME_LABEL_WIDTH), NAME_LABEL_HEIGHT);

        final Gradient subGradient = new Gradient();
        subGradient.setPosition(offsetX + (ICON_WIDTH / 2), offsetY + NAME_LABEL_HEIGHT);
        subGradient.setSize(subBar.getWidth(), subBar.getHeight());
        // remove a little bit of size from the subGradient height to avoid overlapping onto the shadows of subBar.
        subGradient.setSize((int) (progressRatio * subBar.getWidth()), TIME_HEIGHT - 1);
        subGradient.setGradientStart(230, 200, 20, 60);
        subGradient.setGradientEnd(230, 200, 20, 60);
        view.addChild(subGradient);
    }

    /**
     * Generates and attaches tooltips for a given research to input tipItem.
     *
     * @param tipItem  the Pane to apply the tooltip.
     * @param research the global research characteristics to draw.
     * @param state    the status of the selected research.
     */
    private void generateResearchTooltips(final Button tipItem, final IGlobalResearch research, final ResearchButtonState state)
    {
        // have to use a deep copy of getName, or the TranslationText will also retain and apply the formatting in other contexts.
        final AbstractTextBuilder.TooltipBuilder hoverPaneBuilder = PaneBuilders.tooltipBuilder().hoverPane(tipItem).append(research.getName().deepCopy()).bold().color(COLOR_TEXT_NAME);
        if (!research.getSubtitle().getKey().isEmpty())
        {
            hoverPaneBuilder.paragraphBreak().italic().colorName("GRAY").append(research.getSubtitle());
        }
        for (int txt = 0; txt < research.getEffects().size(); txt++)
        {
            hoverPaneBuilder.paragraphBreak().append(research.getEffects().get(txt).getDesc());
            if (!research.getEffects().get(txt).getSubtitle().getKey().isEmpty())
            {
                hoverPaneBuilder.paragraphBreak().append(new StringTextComponent("-")).append(research.getEffects().get(txt).getSubtitle());
            }
        }
        if (state != ResearchButtonState.FINISHED && state != ResearchButtonState.IN_PROGRESS)
        {
            for (int txt = 0; txt < research.getResearchRequirement().size(); txt++)
            {
                if (research.getResearchRequirement().get(txt).isFulfilled(this.building.getColony()))
                {
                    hoverPaneBuilder.paragraphBreak().append(new TranslationTextComponent(" - ")).color(COLOR_TEXT_FULFILLED)
                      .append(research.getResearchRequirement().get(txt).getDesc());
                }
                else
                {
                    hoverPaneBuilder.paragraphBreak().append(new TranslationTextComponent(" - ")).color(COLOR_TEXT_UNFULFILLED)
                      .append(research.getResearchRequirement().get(txt).getDesc());
                }
            }
            if (research.getDepth() > building.getBuildingLevel() && building.getBuildingLevel() != building.getBuildingMaxLevel())
            {
                hoverPaneBuilder.paragraphBreak().append(new TranslationTextComponent("com.minecolonies.coremod.research.requirement.university.level", research.getDepth()));
            }
            if (research.getDepth() == MAX_DEPTH)
            {
                if (hasMax)
                {
                    hoverPaneBuilder.paragraphBreak().append(new TranslationTextComponent("com.minecolonies.coremod.research.limit.onemaxperbranch")).color(COLOR_TEXT_FULFILLED);
                }
                else
                {
                    hoverPaneBuilder.paragraphBreak().append(new TranslationTextComponent("com.minecolonies.coremod.research.limit.onemaxperbranch")).color(COLOR_TEXT_UNFULFILLED);
                }
            }
        }
        if (research.isImmutable())
        {
            hoverPaneBuilder.paragraphBreak().append(new TranslationTextComponent("com.minecolonies.coremod.research.limit.immutable")).color(COLOR_TEXT_UNFULFILLED);
        }
        hoverPaneBuilder.build();
    }

    /**
     * Draw the labels for a given research
     *
     * @param view     the view to append it to.
     * @param offsetX  the horizontal offset of the left side of the research block.
     * @param offsetY  the vertical offset of the top side of the research block.
     * @param research the global research characteristics to draw.
     * @param state    the research's state in the view context.
     * @param progress the progress toward research completion.
     */
    private void drawResearchTexts(final ZoomDragView view, final int offsetX, final int offsetY, final IGlobalResearch research, final ResearchButtonState state, final int progress)
    {
        final Text nameText = new Text();
        nameText.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
        nameText.setText(research.getName());
        nameText.setPosition(offsetX + ICON_WIDTH + TEXT_X_OFFSET, offsetY);
        nameText.setColors(COLOR_TEXT_DARK);
        nameText.setTextScale(1.4f);
        nameText.setEnabled(false);
        view.addChild(nameText);

        if (state == ResearchButtonState.IN_PROGRESS)
        {
            final double progressToGo;
            if(research.isInstant() || (mc.player.isCreative() && MinecoloniesAPIProxy.getInstance().getConfig().getServer().researchCreativeCompletion.get()))
            {
                progressToGo = 0;
            }
            else
            {
                progressToGo = (int) (Math.pow(2, research.getDepth() - 1) * (double) BASE_RESEARCH_TIME * IGlobalResearchTree.getInstance().getBranchTime(branch)) - progress;
            }
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
            final Text progressLabel = new Text();
            progressLabel.setSize(BUTTON_LENGTH, INITIAL_Y_OFFSET);
            progressLabel.setText(new TranslationTextComponent("com.minecolonies.coremod.gui.research.time", timeRemaining));
            progressLabel.setPosition(offsetX + ICON_WIDTH + TEXT_X_OFFSET, offsetY + NAME_LABEL_HEIGHT);
            progressLabel.setColors(COLOR_TEXT_DARK);
            progressLabel.setTextScale(0.7f);
            view.addChild(progressLabel);
        }
    }

    /**
     * Draw an undo button in the middle of the parent research, and manages associated tooltips for in-progress research. This function sets normal button and tooltip information
     * into the displacedButton fields, to allow switch back to normal functionality without having to redraw the entire tree.
     *
     * @param parent the parent button to attach it to.
     */
    private void drawUndoProgressButton(final Button parent)
    {
        undoButton.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
        undoButton.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
        undoButton.setPosition(parent.getX() + (GRADIENT_WIDTH - BUTTON_LENGTH) / 2, parent.getY() + TEXT_Y_OFFSET + (GRADIENT_HEIGHT - BUTTON_HEIGHT) / 2);
        undoButton.setID("undo:" + parent.getID());
        parent.getParent().addChild(undoButton);
        undoText.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
        undoText.setPosition(parent.getX() + TEXT_X_OFFSET + (GRADIENT_WIDTH - BUTTON_LENGTH) / 2, parent.getY() + TEXT_Y_OFFSET + (GRADIENT_HEIGHT - BUTTON_HEIGHT) / 2);
        undoText.setColors(COLOR_TEXT_DARK);
        undoText.setText(new TranslationTextComponent("com.minecolonies.coremod.research.undo.progress"));
        undoText.disable();
        parent.getParent().addChild(undoText);
        PaneBuilders.tooltipBuilder().hoverPane(undoButton).append(new TranslationTextComponent("com.minecolonies.coremod.research.undo.progress.tooltip")).color(COLOR_TEXT_UNFULFILLED).bold().build();
    }

    /**
     * Draw an undo button in the middle of the parent research, and manages associated tooltips for completed research. This function sets normal button and tooltip information
     * into the displacedButton and displacedHoverText fields, to allow switch back to normal functionality without having to redraw the entire tree.
     *
     * @param parent the parent button to attach it to.
     */
    private void drawUndoCompleteButton(final Button parent)
    {
        final List<ItemStorage> costList = IGlobalResearchTree.getInstance().getResearchResetCosts();
        undoCostIcons = new ItemIcon[costList.size()];
        final List<ItemStorage> missingItems = new ArrayList<>();
        for (int i = 0; i < costList.size(); i++)
        {
            final ItemStorage is = costList.get(i);
            undoCostIcons[i] = new ItemIcon();
            if (InventoryUtils.getItemCountInItemHandler(new InvWrapper(Minecraft.getInstance().player.inventory),
              stack -> !ItemStackUtils.isEmpty(stack) && stack.isItemEqual(is.getItemStack())) < is.getAmount())
            {
                missingItems.add(is);
            }
            undoCostIcons[i].setItem(is.getItemStack());
            undoCostIcons[i].setPosition(parent.getX() + (GRADIENT_WIDTH - BUTTON_LENGTH) / 2 + BUTTON_LENGTH + DEFAULT_COST_SIZE * i,
              parent.getY() + TEXT_Y_OFFSET + (GRADIENT_HEIGHT - BUTTON_HEIGHT) / 2);
            undoCostIcons[i].setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            parent.getParent().addChild(undoCostIcons[0]);
        }
        undoButton.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
        undoButton.setPosition(parent.getX() + (GRADIENT_WIDTH - BUTTON_LENGTH) / 2, parent.getY() + TEXT_Y_OFFSET + (GRADIENT_HEIGHT - BUTTON_HEIGHT) / 2);
        final AbstractTextBuilder.TooltipBuilder undoTipBuilder = PaneBuilders.tooltipBuilder().hoverPane(undoButton)
                                  .append(new TranslationTextComponent("com.minecolonies.coremod.research.undo.remove.tooltip")).bold().color(COLOR_TEXT_UNFULFILLED);
        undoText.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
        undoText.setPosition(parent.getX() + TEXT_X_OFFSET + (GRADIENT_WIDTH - BUTTON_LENGTH) / 2, parent.getY() + TEXT_Y_OFFSET + (GRADIENT_HEIGHT - BUTTON_HEIGHT) / 2);
        undoText.setColors(COLOR_TEXT_DARK);
        if (!missingItems.isEmpty())
        {
            undoButton.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_DIS));
            undoText.setText(new TranslationTextComponent("com.minecolonies.coremod.research.research.notenoughresources"));
            for (ItemStorage cost : missingItems)
            {
                undoTipBuilder.paragraphBreak().append(new TranslationTextComponent("com.minecolonies.coremod.research.requirement.research",
                  cost.getItem().getName())).color(COLOR_TEXT_UNFULFILLED);
            }
        }
        else
        {
            undoButton.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
            undoButton.setID("undo:" + parent.getID());
            undoText.setText(new TranslationTextComponent("com.minecolonies.coremod.research.undo.remove"));
        }
        undoText.disable();
        parent.getParent().addChild(undoButton);
        parent.getParent().addChild(undoText);
        undoTipBuilder.build();
    }

    /**
     * Draw the progress bar for a given research.
     *
     * @param view      the view to append it to.
     * @param offsetX   the horizontal offset of the left side of the research block.
     * @param offsetY   the vertical offset of the top side of the research block.
     * @param research  the global research characteristics to draw.
     * @param state     the research's current state.
     */
    private void drawResearchReqsAndCosts(
      final ZoomDragView view,
      final int offsetX,
      final int offsetY,
      final IGlobalResearch research,
      final ResearchButtonState state)
    {
        if (state == ResearchButtonState.ABANDONED || state == ResearchButtonState.IN_PROGRESS || state == ResearchButtonState.FINISHED)
        {
            return;
        }
        int storageXOffset = ICON_WIDTH;

        for (final IResearchRequirement requirement : research.getResearchRequirement())
        {
            if (requirement instanceof AlternateBuildingResearchRequirement)
            {
                for (Map.Entry<String, Integer> building : ((AlternateBuildingResearchRequirement) requirement).getBuildings().entrySet())
                {
                    final Item item;
                    //TODO Houses use 'citizen' for schematics, but 'home' for ForgeRegistry purposes.  Consider fixing for 1.17
                    if(((BuildingResearchRequirement) requirement).getBuilding().equals("citizen"))
                    {
                        item = ModBuildings.home.getBuildingBlock().asItem();
                    }
                    else if (IMinecoloniesAPI.getInstance().getBuildingRegistry().containsKey(
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
                //TODO Houses use 'citizen' for schematics, but 'home' for ForgeRegistry purposes.  Consider fixing for 1.17
                if(((BuildingResearchRequirement) requirement).getBuilding().equals("citizen"))
                {
                    item = ModBuildings.home.getBuildingBlock().asItem();
                }
                else if (IMinecoloniesAPI.getInstance().getBuildingRegistry().containsKey(
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

    /**
     * Draw icons for a specific research, showing the research's readiness state.
     * @param view              View to attach the icons to.
     * @param offsetX           Horizontal offset for the research.
     * @param offsetY           Vertical offset for the reserach.
     * @param research          Global research information.
     * @param state             State of the local research, if begun.
     */
    private void drawResearchIcons(
      final ZoomDragView view,
      final int offsetX,
      final int offsetY,
      final IGlobalResearch research,
      final ResearchButtonState state)
    {
        if (research.isImmutable() && state != ResearchButtonState.FINISHED)
        {
            final Image immutIcon = new Image();
            immutIcon.setImage(new ResourceLocation("minecraft", "textures/block/redstone_torch.png"));
            immutIcon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
            immutIcon.setPosition(offsetX + GRADIENT_WIDTH - DEFAULT_COST_SIZE, offsetY);
            view.addChild(immutIcon);
        }

        switch (state)
        {
            case LOCKED:
            case ABANDONED:
            case TOO_MANY_PROGRESS:
            case MISSING_PARENT:
            case MISSING_REQUIREMENT:
            case TOO_LOW_UNIVERSITY:
                final Image lockIcon = new Image();
                lockIcon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/locked_icon_light_gray.png"));
                lockIcon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                lockIcon.setPosition(offsetX, offsetY);
                view.addChild(lockIcon);
                break;
            case MISSING_COST:
                final Image unlockIcon = new Image();
                unlockIcon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/locked_icon_unlocked_blue.png"));
                unlockIcon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                unlockIcon.setPosition(offsetX, offsetY);
                view.addChild(unlockIcon);
                break;
            case AVAILABLE:
                final Image icon = new Image();
                icon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/icon_start.png"));
                icon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                icon.setPosition(offsetX, offsetY);
                view.addChild(icon);
                break;
            case IN_PROGRESS:
                final Image playIcon = new Image();
                playIcon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/icon_cancel.png"));
                playIcon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                playIcon.setPosition(offsetX, offsetY);
                view.addChild(playIcon);
                break;
            case FINISHED:
                if(DRAW_ICONS)
                {
                    if (!research.getIconTextureResourceLocation().getPath().isEmpty())
                    {
                        final Image iconTex = new Image();
                        iconTex.setImage(research.getIconTextureResourceLocation());
                        iconTex.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                        iconTex.setPosition(offsetX, offsetY);
                        view.addChild(iconTex);
                    }
                    else if (!research.getIconItemStack().isEmpty())
                    {
                        ItemIcon iconItem = new ItemIcon();
                        iconItem.setItem(research.getIconItemStack());
                        iconItem.setPosition(offsetX, offsetY);
                        iconItem.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                        view.addChild(iconItem);
                    }
                }
                else
                {
                    final Image checkIcon = new Image();
                    checkIcon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/icon_check.png"));
                    checkIcon.setSize(DEFAULT_COST_SIZE, DEFAULT_COST_SIZE);
                    checkIcon.setPosition(offsetX, offsetY);
                    view.addChild(checkIcon);
                }
                break;
            default:
                Log.getLogger().error("Error with DrawIcons :" + research.getId());
                break;
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
    private void drawArrows(
      final ZoomDragView view,
      final int offsetX,
      final int offsetY,
      final int researchListSize,
      final ResourceLocation parentResearch,
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
                    final Text orLabel = new Text();
                    orLabel.setSize(OR_WIDTH, OR_HEIGHT);
                    orLabel.setColors(COLOR_TEXT_DARK);
                    orLabel.setText(new TranslationTextComponent("com.minecolonies.coremod.research.research.or"));
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
