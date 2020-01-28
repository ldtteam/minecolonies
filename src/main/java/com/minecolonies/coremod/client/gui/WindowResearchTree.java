package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.DragView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.research.*;
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

        drawTree(0, 0, view, researchList, building.getColony().getResearchTree(), true);

        //todo add how long research will need (3h, 6h, 9h, 12h, 24h, 48h) (above each column)

        //todo integrate the research effects

        //todo final cleanup
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);

        final IGlobalResearch research = GlobalResearchTree.researchTree.getResearch(branch, button.getID());
        if (research != null && building.getBuildingLevel() <= building.getColony().getResearchTree().getResearchInProgress().size())
        {
            Network.getNetwork().sendToServer(new TryResearchMessage(research.getId(), research.getBranch(), building.getColony().getID(), building.getColony().getDimension(), building.getID()));
            final List<String> researchList = GlobalResearchTree.researchTree.getPrimaryResearch(branch);
            final DragView view = findPaneOfTypeByID(DRAG_VIEW_ID, DragView.class);

            drawTree(0, 0, view, researchList, building.getColony().getResearchTree(), true);
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
     * @return the next y offset.
     */
    public int drawTree(final int height, final int depth, final DragView view, final List<String> researchList, final LocalResearchTree tree, final boolean parentResearched)
    {
        int nextHeight = height;
        for (int i = 0; i < researchList.size(); i++)
        {
            final String researchLabel = researchList.get(i);
            int offsetX = (depth * (175 + 20));

            final IGlobalResearch research = GlobalResearchTree.researchTree.getResearch(branch, researchLabel);
            final ILocalResearch localResearch = tree.getResearch(branch, research.getId());
            final ResearchState state = localResearch == null ? ResearchState.NOT_STARTED : localResearch.getState();

            final Gradient gradient = new Gradient();
            gradient.setSize(175, 50);
            gradient.setPosition(x + offsetX + 10, (nextHeight + i) * (50 + 20) + 20);
            if ( state == ResearchState.IN_PROGRESS )
            {
                gradient.setGradientStart(255, 204, 0, 150);
                gradient.setGradientEnd(255, 204, 0, 200);
            }
            else if ( state == ResearchState.FINISHED )
            {
                gradient.setGradientStart(51, 204, 51, 150);
                gradient.setGradientEnd(51, 204, 51, 200);
            }
            else
            {
                gradient.setGradientStart(102, 204, 255, 150);
                gradient.setGradientEnd(102, 204, 255, 200);
            }
            view.addChild(gradient);

            final Label nameLabel = new Label();
            nameLabel.setLabelText(research.getDesc());
            nameLabel.setPosition(x + offsetX + 10 + 50, (nextHeight + i) * (50 + 20) + 5 + 20 + 5);
            view.addChild(nameLabel);

            if (state == ResearchState.IN_PROGRESS)
            {
                //Calculates how much percent of the next level has been completed.
                final double progressRatio = (localResearch.getProgress()+1)/(research.getDepth() * (double) BASE_RESEARCH_TIME);

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
            else if (research.getResearchRequirement() != null)
            {
                final Label requirementLabel = new Label();
                requirementLabel.setLabelText(research.getResearchRequirement().getDesc().getFormattedText());
                requirementLabel.setPosition(offsetX + 10 + 5, nameLabel.getY() + nameLabel.getHeight() + 10);
                view.addChild(requirementLabel);
            }

            final Label effectLabel = new Label();
            effectLabel.setLabelText(research.getEffect().getDesc().getFormattedText());
            effectLabel.setPosition(offsetX + 10 + 5, nameLabel.getY() + nameLabel.getHeight() * 2 + 10 + 10);
            view.addChild(effectLabel);

            if ( parentResearched && state == ResearchState.NOT_STARTED)
            {
                final ButtonImage buttonImage = new ButtonImage();
                buttonImage.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
                buttonImage.setLabel(LanguageHandler.format("com.minecolonies.coremod.research.research"));
                buttonImage.setTextColor(Color.getByName("black", 0));
                buttonImage.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
                buttonImage.setPosition(effectLabel.getX(), effectLabel.getY() + effectLabel.getHeight() + 10);
                buttonImage.setID(research.getId());

                //todo add restriction for OR from here too.
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

            if (!research.getChilds().isEmpty())
            {
                final Image image = new Image();
                image.setImage(new ResourceLocation(Constants.MOD_ID, BUTTON_RIGHT_ARROW));
                image.setSize(20, 6);
                image.setPosition(gradient.getX() + gradient.getWidth(), gradient.getY() + gradient.getHeight()/2);
                view.addChild(image);

                for (int x = 1; x < research.getChilds().size(); x++)
                {
                    if (research.hasOnlyChild())
                    {
                        final Image circle = new Image();
                        circle.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/circle.png"));
                        circle.setSize(23, 24);
                        circle.setPosition(gradient.getX() + gradient.getWidth() / 2 - circle.getWidth()/2, gradient.getY() + x * (gradient.getHeight() + 20) -10);
                        view.addChild(circle);

                        final Label orLabel = new Label();
                        orLabel.setColor(Color.getByName("black", 0));
                        orLabel.setLabelText("or");
                        orLabel.setPosition(gradient.getX() + gradient.getWidth() / 2 - circle.getWidth()/2 + 5, gradient.getY() + x * (gradient.getHeight() + 20) + 2);
                        view.addChild(orLabel);

                        final Image corner = new Image();
                        corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_sketch_arrow_corner_right_a.png"));
                        corner.setSize(16, 20);
                        corner.setPosition(gradient.getX() + gradient.getWidth() / 2, gradient.getY() + x * (gradient.getHeight() + 20) + 15);
                        view.addChild(corner);
                    }
                    else
                    {
                        final Image corner = new Image();
                        corner.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_sketch_arrow_corner_right_a.png"));
                        corner.setSize(16, 20);
                        corner.setPosition(gradient.getX() + gradient.getWidth() / 2, gradient.getY() + x * (gradient.getHeight() + 20) + 5);
                        view.addChild(corner);
                    }
                }

                nextHeight = drawTree(nextHeight + i, depth + 1, view, research.getChilds(), tree, state == ResearchState.FINISHED);
            }
        }

        return nextHeight;
    }

}
