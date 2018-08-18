package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.WindowConstants;
import com.minecolonies.blockout.Alignment;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.*;
import com.minecolonies.blockout.views.Box;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.views.SwitchView;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.CitizenDataView;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.entity.citizenhandlers.CitizenHappinessHandler; 
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
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the citizen.
 */
public class WindowCitizen extends AbstractWindowSkeleton
{
    /**
     * The citizenData.View object.
     */
    private final CitizenDataView citizen;

    /**
     * Scrollinglist of the resources.
     */
    private ScrollingList   resourceList;

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
     * Button leading to the previous page.
     */
    private Button buttonPrevPage;

    /**
     * Button leading to the next page.
     */
    private Button buttonNextPage;

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

        findPaneOfTypeByID(WindowConstants.BUTTON_PREV_PAGE, Button.class).setEnabled(false);
        buttonPrevPage = findPaneOfTypeByID(WindowConstants.BUTTON_PREV_PAGE, Button.class);
        buttonNextPage = findPaneOfTypeByID(WindowConstants.BUTTON_NEXT_PAGE, Button.class);

        createHealthBar(citizen, findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class));
        createSaturationBar();
        createHappinessBar(); 
        createXpBar(citizen, this);
        createSkillContent(citizen, this);
        updateHappiness();
        
        resourceList = findPaneOfTypeByID(WINDOW_ID_LIST_REQUESTS, ScrollingList.class);
        resourceList.setDataProvider(new ScrollingList.DataProvider()
        {

            private List<RequestWrapper> requestWrappers = null;

            @Override
            public int getElementCount()
            {
                requestWrappers = getOpenRequestTreeOfCitizen();
                return requestWrappers.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                if (index < 0 || index >= requestWrappers.size())
                {
                    return;
                }

                final RequestWrapper wrapper = requestWrappers.get(index);
                final Box wrapperBox = rowPane.findPaneOfTypeByID(WINDOW_ID_REQUEST_BOX, Box.class);
                wrapperBox.setPosition(wrapperBox.getX() + 10*wrapper.getDepth(), wrapperBox.getY());
                wrapperBox.setSize(wrapperBox.getParent().getWidth() - 10*wrapper.getDepth(), wrapperBox.getHeight());

                final IRequest<?> request = wrapper.getRequest();
                final ItemIcon exampleStackDisplay = rowPane.findPaneOfTypeByID(LIST_ELEMENT_ID_REQUEST_STACK, ItemIcon.class);
                final List<ItemStack> displayStacks = request.getDisplayStacks();
                final Image logo = rowPane.findPaneOfTypeByID(DELIVERY_IMAGE, Image.class);

                if (!displayStacks.isEmpty())
                {
                    logo.setVisible(false);
                    exampleStackDisplay.setVisible(true);
                    exampleStackDisplay.setItem(displayStacks.get((lifeCount / LIFE_COUNT_DIVIDER) % displayStacks.size()));
                }
                else
                {
                    exampleStackDisplay.setVisible(false);
                    logo.setVisible(true);
                    logo.setImage(request.getDisplayIcon());
                }

                final ColonyView view = ColonyManager.getColonyView(citizen.getColonyId());
                rowPane.findPaneOfTypeByID(REQUESTER, Label.class)
                        .setLabelText(request.getRequester().getDisplayName(view.getRequestManager(), request.getToken()).getFormattedText());
                rowPane.findPaneOfTypeByID(REQUEST_SHORT_DETAIL, Label.class)
                  .setLabelText(request.getShortDisplayString().getFormattedText().replace("§f", ""));

                if (wrapper.getDepth() > 0)
                {
                    request.getRequestOfType(IDeliverable.class).ifPresent((IDeliverable requestRequest) -> {
                        if (!isCreative && !InventoryUtils.hasItemInItemHandler(new InvWrapper(inventory), requestRequest::matches))
                        {
                            rowPane.findPaneOfTypeByID(REQUEST_FULLFIL, ButtonImage.class).hide();
                        }
                    });

                    if (!(request.getRequest() instanceof IDeliverable))
                    {
                        rowPane.findPaneOfTypeByID(REQUEST_FULLFIL, ButtonImage.class).hide();
                    }

                    rowPane.findPaneOfTypeByID(REQUEST_CANCEL, ButtonImage.class).hide();
                }
                else
                {
                    request.getRequestOfType(IDeliverable.class).ifPresent((IDeliverable requestRequest) -> {
                        if (!isCreative && !InventoryUtils.hasItemInItemHandler(new InvWrapper(inventory), requestRequest::matches))
                        {
                            rowPane.findPaneOfTypeByID(REQUEST_FULLFIL, ButtonImage.class).hide();
                        }
                    });
                }
            }
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
    public static void createHealthBar(final CitizenDataView citizen, final View healthBarView)
    {
        healthBarView.setAlignment(Alignment.MIDDLE_RIGHT);

        //MaxHealth (Black hearts).
        for (int i = 0; i < citizen.getMaxHealth() / 2; i++)
        {
            @NotNull final Image heart = new Image();
            heart.setImage(Gui.ICONS, EMPTY_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH, false);
            heart.setPosition(i * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
            healthBarView.addChild(heart);
        }

        //Current health (Red hearts).
        int heartPos;
        for (heartPos = 0; heartPos < ((int) citizen.getHealth() / 2); heartPos++)
        {
            @NotNull final Image heart = new Image();
            heart.setImage(Gui.ICONS, FULL_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH, false);
            heart.setPosition(heartPos * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
            healthBarView.addChild(heart);
        }

        //Half hearts.
        if (citizen.getHealth() / 2 % 1 > 0)
        {
            @NotNull final Image heart = new Image();
            heart.setImage(Gui.ICONS, HALF_HEART_ICON_ROW_POS, HEART_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH, false);
            heart.setPosition(heartPos * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
            healthBarView.addChild(heart);
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
+
    * Creates an Happiness bar according to the citizen maxHappiness and currentHappiness. 
    */ 
   private void createHappinessBar() 
   { 
       final double experienceRatio = (citizen.getHappiness() / CitizenHappinessHandler.MAX_HAPPINESS) * XP_BAR_WIDTH; 
       findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).setAlignment(Alignment.MIDDLE_RIGHT); 
       window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS, Label.class).setLabelText(Integer.toString((int)citizen.getHappiness())); 

        
       @NotNull final Image xpBar = new Image(); 
       xpBar.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN, HAPPINESS_BAR_EMPTY_ROW, XP_BAR_WIDTH, XP_HEIGHT, false); 
       xpBar.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y); 

       @NotNull final Image xpBar2 = new Image(); 
       xpBar2.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN_END, HAPPINESS_BAR_EMPTY_ROW, XP_BAR_ICON_COLUMN_END_WIDTH, XP_HEIGHT, false); 
       xpBar2.setPosition(XP_BAR_ICON_END_OFFSET + LEFT_BORDER_X, LEFT_BORDER_Y); 

       window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).addChild(xpBar); 
       window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).addChild(xpBar2); 

       if (experienceRatio > 0) 
       { 
           @NotNull final Image xpBarFull = new Image(); 
           xpBarFull.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN, HAPPINESS_BAR_FULL_ROW, (int) experienceRatio, XP_HEIGHT, false); 
           xpBarFull.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y); 
           window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).addChild(xpBarFull); 
       } 
   } 


    /**
     * Creates the xp bar for each citizen.
     * Calculates an xpBarCap which is the maximum of xp to fit into the bar.
     * Then creates an xp bar and fills it up with the available xp.
     * @param citizen the citizen.
     * @param window the window to fill.
     */
    public static void createXpBar(final CitizenDataView citizen, final AbstractWindowSkeleton window)
    {
        //Calculates how much percent of the next level has been completed.
        final double experienceRatio = ExperienceUtils.getPercentOfLevelCompleted(citizen.getExperience(), citizen.getLevel());
        window.findPaneOfTypeByID(WINDOW_ID_XP, Label.class).setLabelText(Integer.toString(citizen.getLevel()));

        @NotNull final Image xpBar = new Image();
        xpBar.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN, XP_BAR_EMPTY_ROW, XP_BAR_WIDTH, XP_HEIGHT, false);
        xpBar.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y);

        @NotNull final Image xpBar2 = new Image();
        xpBar2.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN_END, XP_BAR_EMPTY_ROW, XP_BAR_ICON_COLUMN_END_WIDTH, XP_HEIGHT, false);
        xpBar2.setPosition(XP_BAR_ICON_END_OFFSET + LEFT_BORDER_X, LEFT_BORDER_Y);

        window.findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBar);
        window.findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBar2);

        if (experienceRatio > 0)
        {
            @NotNull final Image xpBarFull = new Image();
            xpBarFull.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN, XP_BAR_FULL_ROW, (int) experienceRatio, XP_HEIGHT, false);
            xpBarFull.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y);
            window.findPaneOfTypeByID(WINDOW_ID_XPBAR, View.class).addChild(xpBarFull);
        }
    }

    /** 
     * Creates an Happiness bar according to the citizen maxHappiness and currentHappiness. 
     *  
     * @param citizen  pointer to the citizen data view 
     * @param window  pointer to the current window 
     */ 
    public static void createHappinessBar(final CitizenDataView citizen, final AbstractWindowSkeleton window) 
    { 
        //Calculates how much percent of the next level has been completed. 
        final double experienceRatio = (citizen.getHappiness() / CitizenHappinessHandler.MAX_HAPPINESS) * XP_BAR_WIDTH; 
        window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).setAlignment(Alignment.MIDDLE_RIGHT); 
        window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS, Label.class).setLabelText(Integer.toString((int)citizen.getHappiness())); 
 
        @NotNull final Image xpBar = new Image(); 
        xpBar.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN, HAPPINESS_BAR_EMPTY_ROW, XP_BAR_WIDTH, XP_HEIGHT, false); 
        xpBar.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y); 
 
        @NotNull final Image xpBar2 = new Image(); 
        xpBar2.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN_END, HAPPINESS_BAR_EMPTY_ROW, XP_BAR_ICON_COLUMN_END_WIDTH, XP_HEIGHT, false); 
        xpBar2.setPosition(XP_BAR_ICON_END_OFFSET + LEFT_BORDER_X, LEFT_BORDER_Y); 
 
        window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).addChild(xpBar); 
        window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).addChild(xpBar2); 
 
        if (experienceRatio > 0) 
        { 
            @NotNull final Image xpBarFull = new Image(); 
            xpBarFull.setImage(Gui.ICONS, XP_BAR_ICON_COLUMN, HAPPINESS_BAR_FULL_ROW, (int) experienceRatio, XP_HEIGHT, false); 
            xpBarFull.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y); 
            window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).addChild(xpBarFull); 
        } 
    } 
 
    /**
     * Fills the citizen gui with it's skill values.
     * @param citizen the citizen to use.
     * @param window the window to fill.
     */
    public static void createSkillContent(final CitizenDataView citizen, final AbstractWindowSkeleton window)
    {
        window.findPaneOfTypeByID(STRENGTH, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.strength", citizen.getStrength()));
        window.findPaneOfTypeByID(ENDURANCE, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.endurance", citizen.getEndurance()));
        window.findPaneOfTypeByID(CHARISMA, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.charisma", citizen.getCharisma()));
        window.findPaneOfTypeByID(INTELLIGENCE, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.intelligence", citizen.getIntelligence()));
        window.findPaneOfTypeByID(DEXTERITY, Label.class).setLabelText(
          LanguageHandler.format("com.minecolonies.coremod.gui.citizen.skills.dexterity", citizen.getDexterity()));
    }

    private ImmutableList<RequestWrapper> getOpenRequestTreeOfCitizen()
    {
        final ColonyView colonyView = ColonyManager.getClosestColonyView(FMLClientHandler.instance().getWorldClient(),
          (citizen.getWorkBuilding() != null) ? citizen.getWorkBuilding() : citizen
                                                                              .getHomeBuilding());
        if (colonyView == null)
        {
            return ImmutableList.of();
        }

        final List<RequestWrapper> treeElements = new ArrayList<>();

        getOpenRequestsOfCitizen().stream().forEach(r -> {
            constructTreeFromRequest(colonyView.getRequestManager(), r, treeElements, 0);
        });

        return ImmutableList.copyOf(treeElements);
    }

    private void constructTreeFromRequest(@NotNull final IRequestManager manager, @NotNull final IRequest<?> request, @NotNull final List<RequestWrapper> list, final int currentDepth)
    {
        list.add(new RequestWrapper(request, currentDepth));
        if (request.hasChildren())
        {
            for (final Object o : request.getChildren())
            {
                if (o instanceof IToken<?>)
                {
                    final IToken<?> iToken = (IToken<?>) o;
                    final IRequest<?> childRequest = manager.getRequestForToken(iToken);

                    if (childRequest != null)
                    {
                        constructTreeFromRequest(manager, childRequest, list, currentDepth + 1);
                    }
                }
            }
        }
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
        if(building == null)
        {
            return ImmutableList.of();
        }
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
            case WindowConstants.BUTTON_NEXT_PAGE:
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView("foodModifierPane");
                buttonPrevPage.setEnabled(true);
                buttonNextPage.setEnabled(false);
                break;
            case WindowConstants.BUTTON_PREV_PAGE:
                findPaneOfTypeByID(VIEW_PAGES, SwitchView.class).setView(PAGE_ACTIONS);
                buttonPrevPage.setEnabled(false);
                buttonNextPage.setEnabled(true);
                break;
            default:
                break;
        }
    }

    private void detailedClicked(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequestTreeOfCitizen().size() > row)
        {
            @NotNull final WindowRequestDetail window = new WindowRequestDetail(citizen, getOpenRequestTreeOfCitizen().get(row).getRequest(), citizen.getColonyId());
            window.open();
        }
    }

    private void cancel(@NotNull final Button button)
    {
        final int row = resourceList.getListElementIndexByPane(button);

        if (getOpenRequestTreeOfCitizen().size() > row && row >= 0)
        {
            @NotNull final IRequest<?> request = getOpenRequestTreeOfCitizen().get(row).getRequest();
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

        if (getOpenRequestTreeOfCitizen().size() > row && row >= 0)
        {
            @NotNull final IRequest tRequest = getOpenRequestTreeOfCitizen().get(row).getRequest();

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
                itemStack = request.getDisplayStacks().stream().findFirst().orElse(ItemStack.EMPTY);
            }
            else
            {
                itemStack = inventory.getStackInSlot(InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(inventory), requestPredicate));
            }
            MineColonies.getNetwork().sendToServer(
                    new TransferItemsToCitizenRequestMessage(citizen, itemStack, isCreative ? amount : Math.min(amount, count), citizen.getColonyId()));
            MineColonies.getNetwork().sendToServer(new UpdateRequestStateMessage(citizen.getColonyId(), request.getToken(), RequestState.OVERRULED, itemStack));
        }
        button.disable();
    }

    /**
     * Update the display for the happiness
     */
    private void updateHappiness()
    {
        final String[] imagesIds = new String[] {FOOD_MODIFIER, HOUSE_MODIFIER, DAMAGE_MODIFIER, JOB_MODIFIER, FIELDS_MODIFIER, TOOLS_MODIFIER};
        final double[] levels = new double[] {citizen.getFoodModifier(), citizen.getHouseModifier(), citizen.getDamageModifier(), citizen.getJobModifier(), citizen.getFieldsModifier(), citizen.getToolsModifiers()};

        findPaneOfTypeByID(FOOD_MODIFIER_PANE, View.class).setAlignment(Alignment.MIDDLE_RIGHT);
        if (findPaneByID(FOOD_MODIFIER_PANE) != null)
        {
            findPaneOfTypeByID("happinessModifier", Label.class).setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.happiness.happinessModifier"));
            findPaneOfTypeByID("food", Label.class).setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.happiness.food"));
            findPaneOfTypeByID("damage", Label.class).setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.happiness.damage"));
            findPaneOfTypeByID("house", Label.class).setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.happiness.house"));
            findPaneOfTypeByID("job", Label.class).setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.happiness.job"));
            findPaneOfTypeByID("farms", Label.class).setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.happiness.farms"));
            findPaneOfTypeByID("tools", Label.class).setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.happiness.tools"));
    
            
            for (int i = 0; i < imagesIds.length; i++)
            {
                final Image image = findPaneOfTypeByID(imagesIds[i], Image.class);
                if (levels[i] < 0)
                {
                    image.setImage(RED_ICON);
                }
                else if (levels[i] == 0)
                {
                    image.setImage(YELLOW_ICON);
                }
                else if (levels[i] > 0)
                {
                    image.setImage(GREEN_ICON);
                }
            }
        }
    }

    
    private final class RequestWrapper
    {
        private final IRequest request;
        private final int depth;

        private RequestWrapper(final IRequest request, final int depth)
        {
            this.request = request;
            this.depth = depth;
        }

        public IRequest getRequest()
        {
            return request;
        }

        public int getDepth()
        {
            return depth;
        }
    }
}
