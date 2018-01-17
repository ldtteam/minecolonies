package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Alignment;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.*;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.OpenInventoryMessage;
import com.minecolonies.coremod.network.messages.TransferItemsToCitizenRequestMessage;
import com.minecolonies.coremod.network.messages.UpdateRequestStateMessage;
import com.minecolonies.coremod.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Suppression.RAWTYPES;

/**
 * Window for the citizen.
 */
public class WindowCitizen extends AbstractWindowSkeleton
{
    /**
     * The label to find the inventory button.
     */
    private static final String INVENTORY_BUTTON_ID = "inventory";

    /**
     * The label to find the gui of the citizen.
     */
    private static final String CITIZEN_RESOURCE_SUFFIX = ":gui/windowcitizen.xml";

    /**
     * The label to find strength in the gui.
     */
    private static final String STRENGTH = "strength";

    /**
     * The label to find endurance in the gui.
     */
    private static final String ENDURANCE = "endurance";

    /**
     * The label to find charisma in the gui.
     */
    private static final String CHARISMA = "charisma";

    /**
     * The label to find intelligence in the gui.
     */
    private static final String INTELLIGENCE = "intelligence";

    /**
     * The label to find dexterity in the gui.
     */
    private static final String DEXTERITY = "dexterity";

    /**
     * Id of the resource add button.
     */
    private static final String REQUEST_FULLFIL = "fulfill";

    /**
     * Id of the resource add button.
     */
    private static final String REQUEST_CANCEL = "cancel";

    /**
     * Nice representation string for a position.
     */
    private static final String POSITION_STRING = "x: %d - y: %d - z: %d";

    /**
     * Xp-bar height.
     */
    private static final int XP_HEIGHT = 5;

    /**
     * The x-distance to the left border of the gui of the xpBar.
     */
    private static final int LEFT_BORDER_X = 10;

    /**
     * The y-distance to the top-left border of the gui of the xpBar.
     */
    private static final int LEFT_BORDER_Y = 10;

    /**
     * The column in which the icon starts.
     */
    private static final int XP_BAR_ICON_COLUMN = 0;

    /**
     * The column where the icon ends.
     */
    private static final int XP_BAR_ICON_COLUMN_END = 172;

    /**
     * The width of the end piece of the xpBar.
     */
    private static final int XP_BAR_ICON_COLUMN_END_WIDTH = 10;

    /**
     * The offset where the end should be placed in the GUI.
     */
    private static final int XP_BAR_ICON_END_OFFSET = 90;

    /**
     * The width of the xpBar (Original width is halved to fit in the gui).
     */
    private static final int XP_BAR_WIDTH = 182 / 2;

    /**
     * The row where the empty xpBar starts.
     */
    private static final int XP_BAR_EMPTY_ROW = 64;

    /**
     * The row where the full xpBar starts.
     */
    private static final int XP_BAR_FULL_ROW = 69;

    /**
     * Row position of the empty heart icon.
     */
    private static final int EMPTY_HEART_ICON_ROW_POS = 16;

    /**
     * Row position of the full heart icon.
     */
    private static final int FULL_HEART_ICON_ROW_POS = 53;

    /**
     * Row position of the half/full heart icon.
     */
    private static final int HALF_HEART_ICON_ROW_POS = 62;

    /**
     * Column position of the heart icons.
     */
    private static final int HEART_ICON_COLUMN = 0;

    /**
     * Dimension of the hearts.
     */
    private static final int HEART_ICON_HEIGHT_WIDTH = 9;

    /**
     * The position x where the heart is placed.
     */
    private static final int HEART_ICON_POS_X = 10;

    /**
     * The offset x where the next heart should be placed.
     */
    private static final int HEART_ICON_OFFSET_X = 10;

    /**
     * The position y where the heart is placed.
     */
    private static final int HEART_ICON_POS_Y = 10;

    /**
     * The position y where the saturation is placed.
     */
    private static final int SATURATION_ICON_POS_Y = 10;

    /**
     * Column of the saturation icon.
     */
    private static final int SATURATION_ICON_COLUMN = 27;

    /**
     * Dimension of the hearts.
     */
    private static final int SATURATION_ICON_HEIGHT_WIDTH = 9;

    /**
     * Saturation icon x position.
     */
    private static final int SATURATION_ICON_POS_X = 10;

    /**
     * Saturation item x offset.
     */
    private static final int SATURATION_ICON_OFFSET_X = 10;

    /**
     * The label to find name in the gui.
     */
    private static final String WINDOW_ID_NAME = "name";

    /**
     * The label to find xpLabel in the gui.
     */
    private static final String WINDOW_ID_XP = "xpLabel";

    /**
     * The label to find xpBar in the gui.
     */
    private static final String WINDOW_ID_XPBAR = "xpBar";

    /**
     * The label to find healthBar in the gui.
     */
    private static final String WINDOW_ID_HEALTHBAR = "healthBar";

    /**
     * The position of the empty saturation icon.
     */
    private static final int EMPTY_SATURATION_ITEM_ROW_POS = 16;

    /**
     * The position of the full saturation icon.
     */
    private static final int FULL_SATURATION_ITEM_ROW_POS = 16 + 36;

    /**
     * The position of the half saturation icon.
     */
    private static final int HALF_SATURATION_ITEM_ROW_POS = 16 + 45;

    /**
     * The saturation bar of the citizen.
     */
    private static final String WINDOW_ID_SATURATION_BAR = "saturationBar";

    /**
     * Id of the gender button.
     */
    private static final String WINDOW_ID_GENDER = "gender";

    /**
     * Requests list id.
     */
    private static final String WINDOW_ID_LIST_REQUESTS = "requests";

    /**
     * Requestst stack id.
     */
    private static final String LIST_ELEMENT_ID_REQUEST_STACK = "requestStack";

    /**
     * Resolver string.
     */
    private static final String DELIVERY_IMAGE = "deliveryImage";

    /**
     * Button id of the requests page.
     */
    private static final String BUTTON_REQUESTS = "requestsTitle";

    /**
     * Button id to get back from the requests page.
     */
    private static final String BUTTON_BACK = "back";

    /**
     * Id of the pages view.
     */
    private static final String VIEW_PAGES = "pages";

    /**
     * Source of the female wax location.
     */
    private static final String FEMALE_SOURCE = "minecolonies:textures/gui/citizen/colonist_wax_female_smaller.png";

    /**
     * Id of the detail button
     */
    private static final String REQUEST_DETAIL = "detail";

    /**
     * Id of the short detail label.
     */
    private static final String REQUEST_SHORT_DETAIL = "shortDetail";

    /**
     * Id of the requester label.
     */
    private static final String REQUESTER = "requester";

    /**
     * The divider for the life count.
     */
    private static final int LIFE_COUNT_DIVIDER = 30;
    /**
     * The citizenData.View object.
     */
    private final CitizenDataView citizen;
    /**
     * Scrollinglist of the resources.
     */
    private final ScrollingList   resourceList;
    /**
     * Inventory of the player.
     */
    private final InventoryPlayer inventory  = this.mc.player.inventory;
    /**
     * Is the player in creative or not.
     */
    private final boolean         isCreative = this.mc.player.capabilities.isCreativeMode;
    /**
     * Life count.
     */
    private       int             lifeCount  = 0;

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public WindowCitizen(final CitizenDataView citizen)
    {
        super(Constants.MOD_ID + CITIZEN_RESOURCE_SUFFIX);
        this.citizen = citizen;

        resourceList = findPaneOfTypeByID(WINDOW_ID_LIST_REQUESTS, ScrollingList.class);
    }

    /**
     * Get a nice represetation of the pos string.
     *
     * @param pos the position.
     * @return a nice string.
     */
    private static String getNicePositionString(final BlockPos pos)
    {
        return String.format(POSITION_STRING, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (!GuiScreen.isShiftKeyDown())
        {
            lifeCount++;
        }
    }

    /**
     * Called when the gui is opened by an player.
     */
    @Override
    public void onOpened()
    {
        findPaneOfTypeByID(WINDOW_ID_NAME, Label.class).setLabelText(citizen.getName());

        createHealthBar();
        createSaturationBar();
        createXpBar();
        createSkillContent();

        resourceList.setDataProvider(() -> getOpenRequestsOfCitizen().size(), (int index, Pane rowPane) ->
        {
            @SuppressWarnings(RAWTYPES)
            final ImmutableList<IRequest> openRequests = getOpenRequestsOfCitizen();
            if (index < 0 || index >= openRequests.size())
            {
                return;
            }

            final IRequest<?> request = openRequests.get(index);
            final ItemIcon exampleStackDisplay = rowPane.findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_STACK, ItemIcon.class);
            final List<ItemStack> displayStacks = request.getDisplayStacks();

            if (!displayStacks.isEmpty())
            {
                exampleStackDisplay.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()));
            }
            else
            {
                final Image logo = findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);
                logo.setVisible(true);
                logo.setImage(request.getDisplayIcon());
            }

            rowPane.findPaneOfTypeByID(REQUESTER, Label.class).setLabelText(request.getRequester().getDisplayName(request.getToken()).getFormattedText());
            rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Label.class)
              .setLabelText(request.getShortDisplayString().getFormattedText().replace("§f", ""));

            request.getRequestOfType(IDeliverable.class).ifPresent((IDeliverable requestRequest) -> {
                if (!isCreative && !InventoryUtils.hasItemInItemHandler(new InvWrapper(inventory), requestRequest::matches))
                {
                    rowPane.findPaneOfTypeByID(REQUEST_FULLFIL, ButtonImage.class).hide();
                }
            });
        });

        //Tool of class:§rwith minimal level:§rWood or Gold§r and§rwith maximal level:§rWood or Gold§r

        if (citizen.isFemale())
        {
            findPaneOfTypeByID(WINDOW_ID_GENDER, Image.class).setImage(FEMALE_SOURCE);
        }
    }

    /**
     * Creates an health bar according to the citizen maxHealth and currentHealth.
     */
    private void createHealthBar()
    {
        findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).setAlignment(Alignment.MIDDLE_RIGHT);

        //MaxHealth (Black hearts).
        for (int i = 0; i < citizen.getMaxHealth() / 2; i++)
        {
            @NotNull final Image heart = new Image();
            heart.setImage(Gui.ICONS, EMPTY_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH, false);
            heart.setPosition(i * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).addChild(heart);
        }

        //Current health (Red hearts).
        int heartPos;
        for (heartPos = 0; heartPos < ((int) citizen.getHealth() / 2); heartPos++)
        {
            @NotNull final Image heart = new Image();
            heart.setImage(Gui.ICONS, FULL_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH, false);
            heart.setPosition(heartPos * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).addChild(heart);
        }

        //Half hearts.
        if (citizen.getHealth() / 2 % 1 > 0)
        {
            @NotNull final Image heart = new Image();
            heart.setImage(Gui.ICONS, HALF_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH, false);
            heart.setPosition(heartPos * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class).addChild(heart);
        }
    }

    /**
     * Creates an health bar according to the citizen maxHealth and currentHealth.
     */
    private void createSaturationBar()
    {
        findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).setAlignment(Alignment.MIDDLE_RIGHT);

        //Max saturation (Black food items).
        for (int i = 0; i < CitizenData.MAX_SATURATION; i++)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(Gui.ICONS, EMPTY_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH, false);

            saturation.setPosition(i * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }

        //Current saturation (Full food hearts).
        int saturationPos;
        for (saturationPos = 0; saturationPos < ((int) citizen.getSaturation()); saturationPos++)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(Gui.ICONS, FULL_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH, false);
            saturation.setPosition(saturationPos * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }

        //Half food items.
        if (citizen.getSaturation() / 2 % 1 > 0)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(Gui.ICONS, HALF_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH, false);
            saturation.setPosition(saturationPos * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y);
            findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }
    }

    /**
     * Creates the xp bar for each citizen.
     * Calculates an xpBarCap which is the maximum of xp to fit into the bar.
     * Then creates an xp bar and fills it up with the available xp.
     */
    private void createXpBar()
    {
        //Calculates how much percent of the next level has been completed.
        final double experienceRatio = ExperienceUtils.getPercentOfLevelCompleted(citizen.getExperience(), citizen.getLevel());

        findPaneOfTypeByID(WINDOW_ID_XP, Label.class).setLabelText(Integer.toString(citizen.getLevel()));

        @NotNull final Image xpBar = new Image();
        xpBar.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN, XP_BAR_EMPTY_ROW, XP_BAR_WIDTH, XP_HEIGHT, false);
        xpBar.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y);

        @NotNull final Image xpBar2 = new Image();
        xpBar2.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN_END, XP_BAR_EMPTY_ROW, XP_BAR_ICON_COLUMN_END_WIDTH, XP_HEIGHT, false);
        xpBar2.setPosition(XP_BAR_ICON_END_OFFSET + LEFT_BORDER_X, LEFT_BORDER_Y);

        findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBar);
        findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBar2);

        if (experienceRatio > 0)
        {
            @NotNull final Image xpBarFull = new Image();
            xpBarFull.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN, XP_BAR_FULL_ROW, (int) experienceRatio, XP_HEIGHT, false);
            xpBarFull.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y);
            findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBarFull);
        }
    }

    /**
     * Fills the citizen gui with it's skill values.
     */
    private void createSkillContent()
    {
        findPaneOfTypeByID(STRENGTH, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.strength", citizen.getStrength()));
        findPaneOfTypeByID(ENDURANCE, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.endurance", citizen.getEndurance()));
        findPaneOfTypeByID(CHARISMA, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.charisma", citizen.getCharisma()));
        findPaneOfTypeByID(INTELLIGENCE, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.intelligence", citizen.getIntelligence()));
        findPaneOfTypeByID(DEXTERITY, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.dexterity", citizen.getDexterity()));
    }

    @SuppressWarnings(RAWTYPES)
    private ImmutableList<IRequest> getOpenRequestsOfCitizen()
    {
        final ArrayList<IRequest> requests = new ArrayList<>();
        if (citizen.getWorkBuilding() != null)
        {
            requests.addAll(getOpenRequestsOfCitizenFromBuilding(citizen.getWorkBuilding()));
        }

        if (citizen.getHomeBuilding() != null && !citizen.getHomeBuilding().equals(citizen.getWorkBuilding()))
        {
            requests.addAll(getOpenRequestsOfCitizenFromBuilding(citizen.getHomeBuilding()));
        }

        final BlockPos playerPos = Minecraft.getMinecraft().player.getPosition();
        requests.sort(Comparator.comparing((IRequest request) -> request.getRequester()
                .getDeliveryLocation().getInDimensionLocation().getDistance(playerPos.getX(), playerPos.getY(), playerPos.getZ()))
                .thenComparingInt(request -> request.getToken().hashCode()));

        return ImmutableList.copyOf(requests);
    }

    @SuppressWarnings(RAWTYPES)
    private ImmutableList<IRequest> getOpenRequestsOfCitizenFromBuilding(final BlockPos buildingPos)
    {
        final ColonyView colonyView = ColonyManager.getClosestColonyView(FMLClientHandler.instance().getWorldClient(), buildingPos);
        if (colonyView == null)
        {
            return ImmutableList.of();
        }

        final AbstractBuildingView building = colonyView.getBuilding(buildingPos);

        return building.getOpenRequests(citizen);
    }

    /**
     * Called when a button in the citizen has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        switch (button.getID())
        {
            case BUTTON_REQUESTS:
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).nextView();
                break;
            case BUTTON_BACK:
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).previousView();
                break;
            case INVENTORY_BUTTON_ID:
                MineColonies.getNetwork().sendToServer(new OpenInventoryMessage(citizen.getName(), citizen.getEntityId()));
                break;
            case REQUEST_DETAIL:
                detailedClicked(button);
                break;
            case REQUEST_CANCEL:
                cancel(button);
                break;
            case REQUEST_FULLFIL:
                fulfill(button);
                break;
            default:
                break;
        }
    }

    private void detailedClicked(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequestsOfCitizen().size() > row)
        {
            @NotNull final WindowRequestDetail window = new WindowRequestDetail(citizen, getOpenRequestsOfCitizen().get(row), citizen.getColonyId());
            window.open();
        }
    }

    private void cancel(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequestsOfCitizen().size() > row && row >= 0)
        {
            @NotNull final IRequest<?> request = getOpenRequestsOfCitizen().get(row);
            MineColonies.getNetwork().sendToServer(new UpdateRequestStateMessage(citizen.getColonyId(), request.getToken(), RequestState.CANCELLED, null));
        }
    }

    /**
     * On Button click transfert Items and fullfil.
     *
     * @param button the clicked button.
     */
    private void fulfill(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequestsOfCitizen().size() > row && row >= 0)
        {
            button.disable();
            @NotNull final IRequest tRequest = getOpenRequestsOfCitizen().get(row);

            if (!(tRequest.getRequest() instanceof IDeliverable))
            {
                return;
            }

            @NotNull final IRequest<? extends IDeliverable> request = (IRequest<? extends IDeliverable>) tRequest;

            final Predicate<ItemStack> requestPredicate = stack -> request.getRequest().matches(stack);
            final int amount = request.getRequest().getCount();

            final int count = InventoryUtils.getItemCountInItemHandler(new InvWrapper(inventory), requestPredicate);

            if (!isCreative && count <= 0)
            {
                return;
            }

            // The itemStack size should not be greater than itemStack.getMaxStackSize, We send 1 instead
            // and use quantity for the size
            @NotNull final ItemStack itemStack;
            if (isCreative)
            {
                itemStack = request.getDisplayStacks().stream().findFirst().orElse(null);
            }
            else
            {
                itemStack = inventory.getStackInSlot(InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(inventory), requestPredicate));
            }
            MineColonies.getNetwork().sendToServer(new TransferItemsToCitizenRequestMessage(citizen, itemStack, isCreative ? amount : count, citizen.getColonyId()));
            MineColonies.getNetwork().sendToServer(new UpdateRequestStateMessage(citizen.getColonyId(), request.getToken(), RequestState.OVERRULED, itemStack));

        }
    }
}
