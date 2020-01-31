package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.Box;
import com.ldtteam.blockout.views.DragView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.*;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.coremod.network.messages.TryResearchMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.research.ResearchConstants.BASE_RESEARCH_TIME;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window to display a particular branch of the tree.
 */
public class WindowResearchTree extends AbstractWindowSkeleton
{
    private static final int LABEL_OFFSET_X = 10;
    private static final int LABEL_OFFSET_Y = 5;
    private static final int Y_PADDING    = 20;
    private static final String DRAG_VIEW_ID = "dragView";

    private final String branch;

    private final BuildingUniversity.View  building;


    private final WindowHutUniversity last;

    /**
     * Create the research tree window.
     * @param branch the branch being researched.
     * @param building the associated university.
     * @param last the GUI we opened this from.
     */
    public WindowResearchTree(final String branch, final BuildingUniversity.View building, final WindowHutUniversity last)
    {
        super(Constants.MOD_ID + R_TREE_RESOURCE_SUFFIX);
        this.branch = branch;
        this.building = building;
        this.last = last;

        final List<String> researchList = GlobalResearchTree.researchTree.getPrimaryResearch(branch);
        final DragView view = findPaneOfTypeByID(DRAG_VIEW_ID, DragView.class);

        drawTree(0, 0, view, researchList, building.getColony().getResearchTree(), true, false, 0);

        //todo add how long research will need (3h, 6h, 9h, 12h, 24h, 48h) (above each column)

        //todo integrate the research effects

        //todo final cleanup
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);

        final IGlobalResearch research = GlobalResearchTree.researchTree.getResearch(branch, button.getID());
        if (research != null && building.getBuildingLevel() > building.getColony().getResearchTree().getResearchInProgress().size() && building.getBuildingLevel() > building.getColony().getResearchTree().getResearchInProgress().size() && research.hasEnoughResources(new InvWrapper(Minecraft.getInstance().player.inventory)))
        {
            Network.getNetwork().sendToServer(new TryResearchMessage(research.getId(), research.getBranch(), building.getColony().getID(), building.getColony().getDimension(), building.getID()));
            close();
        }

        if (button.getID().equals("cancel"))
        {
            this.close();
            last.open();
        }
    }

    /**
     * Draw the tree of research.
     * @param height the start y offset.
     * @param depth the current depth.
     * @param view the view to append it to.
     * @param researchList the list of research to go through.
     * @param tree the local tree of the colony.
     * @param parentResearched if possibly can be researched.
     * @param abandoned if abandoned child.
     * @return the next y offset.
     */
    public int drawTree(final int height, final int depth, final DragView view, final List<String> researchList, final LocalResearchTree tree, final boolean parentResearched, final boolean abandoned, final int parentHeight)
    {
        int nextHeight = height;
        for (int i = 0; i < researchList.size(); i++)
        {
            final String researchLabel = researchList.get(i);
            int offsetX = (depth * (175 + 40));

            final IGlobalResearch research = GlobalResearchTree.researchTree.getResearch(branch, researchLabel);
            final ILocalResearch localResearch = tree.getResearch(branch, research.getId());
            final ResearchState state = localResearch == null ? ResearchState.NOT_STARTED : localResearch.getState();

            final Gradient gradient = new Gradient();
            gradient.setSize(175, 50);
            gradient.setPosition(x + offsetX + 10, (nextHeight + Math.min(i, 1)) * (50 + 20) + 20);
            if (state == ResearchState.IN_PROGRESS)
            {
                gradient.setGradientStart(227, 249, 184, 255);
                gradient.setGradientEnd(227, 249, 184, 255);
                view.addChild(gradient);
            }
            else if (!parentResearched)
            {
                gradient.setGradientStart(239, 230, 215, 255);
                gradient.setGradientEnd(239, 230, 215, 255);
                view.addChild(gradient);
            }
            else if (abandoned)
            {
                gradient.setGradientStart(191, 184, 172, 255);
                gradient.setGradientEnd(191, 184, 172, 255);
                view.addChild(gradient);
            }
            else if (state != ResearchState.FINISHED)
            {
                gradient.setGradientStart(102, 204, 255, 255);
                gradient.setGradientEnd(102, 204, 255, 255);
                view.addChild(gradient);
            }

            final Box box = new Box();
            box.setColor(218, 202, 171);
            box.setSize(175, 50);
            box.setPosition(gradient.getX(), gradient.getY());
            view.addChild(box);

            final Label nameLabel = new Label();
            nameLabel.setLabelText(research.getDesc());
            nameLabel.setPosition(x + offsetX + 10 + 50, (nextHeight + Math.min(i, 1)) * (50 + 20) + 5 + 20 + 5);
            nameLabel.setColor(Color.rgbaToInt(160 , 160 , 160, 255));
            view.addChild(nameLabel);

            if (state == ResearchState.IN_PROGRESS)
            {
                //Calculates how much percent of the next level has been completed.
                final double progressRatio = (localResearch.getProgress()+1)/(research.getDepth() * (double) BASE_RESEARCH_TIME) * 100;

                @NotNull final Image xpBar = new Image();
                xpBar.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN, XP_BAR_EMPTY_ROW, XP_BAR_WIDTH, XP_HEIGHT, false);
                xpBar.setPosition(offsetX + 40 + 5, nameLabel.getY() + nameLabel.getHeight() + 30);

                @NotNull final Image xpBar2 = new Image();
                xpBar2.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN_END, XP_BAR_EMPTY_ROW, XP_BAR_ICON_COLUMN_END_WIDTH, XP_HEIGHT, false);
                xpBar2.setPosition(90 + offsetX + 40 + 5, nameLabel.getY() + nameLabel.getHeight() + 30);

                view.addChild(xpBar);
                view.addChild(xpBar2);

                if (progressRatio > 0)
                {
                    @NotNull final Image xpBarFull = new Image();
                    xpBarFull.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN, XP_BAR_FULL_ROW, (int) progressRatio, XP_HEIGHT, false);
                    xpBarFull.setPosition(offsetX + 40 + 5, nameLabel.getY() + nameLabel.getHeight() + 30);
                    view.addChild(xpBarFull);
                }
            }
            else if (research.getResearchRequirement() != null && state != ResearchState.FINISHED)
            {
                final Label requirementLabel = new Label();
                requirementLabel.setLabelText(research.getResearchRequirement().getDesc().getFormattedText());
                requirementLabel.setPosition(offsetX + 10 + 5, nameLabel.getY() + nameLabel.getHeight() + 10);
                requirementLabel.setColor(Color.rgbaToInt(160 , 160 , 160, 255));

                view.addChild(requirementLabel);
            }

            final Label effectLabel = new Label();
            effectLabel.setLabelText(research.getEffect().getDesc().getFormattedText());
            effectLabel.setPosition(offsetX + 10 + 5, nameLabel.getY() + nameLabel.getHeight() * 2 + 10 + 10);
            effectLabel.setColor(Color.rgbaToInt(160 , 160 , 160, 255));

            view.addChild(effectLabel);

            if ( parentResearched && state == ResearchState.NOT_STARTED && !abandoned )
            {
                final ButtonImage buttonImage = new ButtonImage();
                buttonImage.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
                buttonImage.setLabel(LanguageHandler.format("com.minecolonies.coremod.research.research"));
                buttonImage.setTextColor(Color.getByName("black", 0));
                buttonImage.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
                buttonImage.setPosition(effectLabel.getX(), effectLabel.getY() + effectLabel.getHeight() + 10);
                buttonImage.setID(research.getId());

                if (building.getBuildingLevel() <= building.getColony().getResearchTree().getResearchInProgress().size() || !research.hasEnoughResources(new InvWrapper(Minecraft.getInstance().player.inventory)))
                {
                    buttonImage.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium_large_disabled.png"));
                }

                view.addChild(buttonImage);

                int storageOffset = 2;
                for (final ItemStorage storage : research.getCostList())
                {
                    final ItemStack stack = storage.getItemStack().copy();
                    stack.setCount(storage.getAmount());
                    final ItemIcon icon = new ItemIcon();
                    icon.setItem(stack);
                    icon.setPosition(buttonImage.getX() + buttonImage.getWidth() + storageOffset, effectLabel.getY() + effectLabel.getHeight() + 10);
                    icon.setSize(16, 16);
                    view.addChild(icon);

                    storageOffset += 20;
                }
            }
            else if (!parentResearched)
            {
                final Image lockIcon = new Image();
                lockIcon.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/locked_icon.png"));
                lockIcon.setSize(15, 17);
                lockIcon.setPosition( effectLabel.getX() + BUTTON_LENGTH, effectLabel.getY() + effectLabel.getHeight() + 10);
                view.addChild(lockIcon);
            }

            final boolean firstSibling = i == 0;
            final boolean secondSibling = i == 1;

            final boolean lastSibling = i+1 >= researchList.size();

            if (!research.getParent().isEmpty())
            {
                if (firstSibling && lastSibling)
                {
                    final Image corner = new Image();
                    corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right.png"));
                    corner.setSize(40, 50);
                    corner.setPosition(gradient.getX() - 40, gradient.getY());
                    view.addChild(corner);
                }
                else
                {
                    if (secondSibling)
                    {
                        for (int dif = 1; dif < (nextHeight + Math.min(i, 1)) - parentHeight; dif++)
                        {
                            final Image corner = new Image();
                            corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_down.png"));
                            corner.setSize(40, 70);
                            corner.setPosition(gradient.getX() - 40, (dif) * (gradient.getHeight() + 20) + 20);
                            view.addChild(corner);
                        }
                    }

                    if (firstSibling)
                    {
                        final Image corner = new Image();
                        corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right_down.png"));
                        corner.setSize(40, 70);
                        corner.setPosition(gradient.getX() - 40, gradient.getY());
                        view.addChild(corner);
                    }
                    else
                    {
                        if (GlobalResearchTree.researchTree.getResearch(branch, research.getParent()).hasOnlyChild())
                        {
                            final Label orLabel = new Label();
                            orLabel.setColor(Color.getByName("black", 0));
                            orLabel.setLabelText("or");
                            orLabel.setPosition(gradient.getX() - 40 + 14, gradient.getY() + 10);
                            view.addChild(orLabel);

                            if (lastSibling)
                            {
                                final Image circle = new Image();
                                circle.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_or.png"));
                                circle.setSize(40, 50);
                                circle.setPosition(gradient.getX() - 40, gradient.getY());
                                view.addChild(circle);
                            }
                            else
                            {
                                final Image corner = new Image();
                                corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_or_down.png"));
                                corner.setSize(40, 70);
                                corner.setPosition(gradient.getX() - 40, gradient.getY());
                                view.addChild(corner);
                            }
                        }
                        else
                        {
                            if (lastSibling)
                            {
                                final Image corner = new Image();
                                corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right_and.png"));
                                corner.setSize(40, 50);
                                corner.setPosition(gradient.getX() - 40, gradient.getY());
                                view.addChild(corner);
                            }
                            else
                            {
                                final Image corner = new Image();
                                corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/research/arrow_right_and_more.png"));
                                corner.setSize(40, 70);
                                corner.setPosition(gradient.getX() - 40, gradient.getY());
                                view.addChild(corner);
                            }
                        }
                    }
                }

            }

            if (!research.getChilds().isEmpty())
            {
                nextHeight = drawTree(nextHeight + Math.min(i, 1), depth + 1, view, research.getChilds(), tree, state == ResearchState.FINISHED, research.hasOnlyChild() && research.hasResearchedChild(tree), (nextHeight + Math.min(i, 1)));
            }
            else
            {
                nextHeight += Math.min(i, 1);
            }
        }

        return nextHeight;
    }

}
