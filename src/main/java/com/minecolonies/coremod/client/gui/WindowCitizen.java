package com.minecolonies.coremod.client.gui;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.blockout.views.View;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.request.RequestState;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.HappinessConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLibrary;
import com.minecolonies.coremod.network.messages.server.colony.OpenInventoryMessage;
import com.minecolonies.coremod.network.messages.server.colony.UpdateRequestStateMessage;
import com.minecolonies.coremod.network.messages.server.colony.citizen.AdjustSkillCitizenMessage;
import com.minecolonies.coremod.network.messages.server.colony.citizen.TransferItemsToCitizenRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.*;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_CANT_TAKE_EQUIPPED;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.client.gui.modules.WindowBuilderResModule.BLACK;
import static com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenExperienceHandler.PRIMARY_DEPENDENCY_SHARE;
import static com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenExperienceHandler.SECONDARY_DEPENDENCY_SHARE;

/**
 * Window for the citizen.
 */
public class WindowCitizen extends AbstractWindowRequestTree
{
    /**
     * The citizenData.View object.
     */
    private final ICitizenDataView citizen;

    /**
     * Tick function for updating every second.
     */
    private int tick = 0;

    /**
     * Enum for the available hearts
     */
    private enum HeartsEnum
    {
        EMPTY(Screen.GUI_ICONS_LOCATION, EMPTY_HEART_ICON_X, HEART_ICON_MC_Y, EMPTY_HEART_VALUE, null, null),
        HALF_RED(Screen.GUI_ICONS_LOCATION, HALF_RED_HEART_ICON_X, HEART_ICON_MC_Y, RED_HEART_VALUE - 1, null, EMPTY),
        RED(Screen.GUI_ICONS_LOCATION, RED_HEART_ICON_X, HEART_ICON_MC_Y, RED_HEART_VALUE, HALF_RED, EMPTY),
        HALF_GOLDEN(Screen.GUI_ICONS_LOCATION, HALF_GOLD_HEART_ICON_X, HEART_ICON_MC_Y, GOLDEN_HEART_VALUE - 1, null, RED),
        GOLDEN(Screen.GUI_ICONS_LOCATION, GOLD_HEART_ICON_X, HEART_ICON_MC_Y, GOLDEN_HEART_VALUE, HALF_GOLDEN, RED),
        HALF_GREEN(GREEN_BLUE_ICON, GREEN_HALF_HEART_ICON_X, GREEN_HEARTS_ICON_Y, GREEN_HEART_VALUE - 1, null, GOLDEN),
        GREEN(GREEN_BLUE_ICON, GREEN_HEART_ICON_X, GREEN_HEARTS_ICON_Y, GREEN_HEART_VALUE, HALF_GREEN, GOLDEN),
        HALF_BLUE(GREEN_BLUE_ICON, BLUE_HALF_HEART_ICON_X, BLUE_HEARTS_ICON_Y, BLUE_HEART_VALUE - 1, null, GREEN),
        BLUE(GREEN_BLUE_ICON, BLUE_HEART_ICON_X, BLUE_HEARTS_ICON_Y, BLUE_HEART_VALUE, HALF_BLUE, GREEN);

        public final int              X;
        public final int              Y;
        public final int              hpValue;
        public final HeartsEnum       prevHeart;
        public final HeartsEnum       halfHeart;
        public       boolean          isHalfHeart = false;
        public final ResourceLocation Image;

        HeartsEnum(
          final ResourceLocation heartImage, final int x, final int y, final int hpValue,
          final HeartsEnum halfHeart, final HeartsEnum prevHeart)
        {
            this.Image = heartImage;
            this.X = x;
            this.Y = y;
            this.hpValue = hpValue;
            this.halfHeart = halfHeart;
            if (halfHeart == null)
            {
                isHalfHeart = true;
            }
            this.prevHeart = prevHeart;
        }
    }

    /**
     * Inventory of the player.
     */
    private final PlayerInventory inventory = this.mc.player.inventory;

    /**
     * Is the player in creative or not.
     */
    private final boolean isCreative = this.mc.player.isCreative();

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public WindowCitizen(final ICitizenDataView citizen)
    {
        super(citizen.getWorkBuilding(),
          Constants.MOD_ID + CITIZEN_RESOURCE_SUFFIX,
          IColonyManager.getInstance().getColonyView(citizen.getColonyId(), Minecraft.getInstance().world.getDimensionKey()));
        this.citizen = citizen;

        final Image statusIcon = findPaneOfTypeByID(STATUS_ICON, Image.class);
        if (citizen.getVisibleStatus() == null)
        {
            statusIcon.setVisible(false);
        }
        else
        {
            statusIcon.setImage(citizen.getVisibleStatus().getIcon());
            PaneBuilders.tooltipBuilder()
                .append(new StringTextComponent(citizen.getVisibleStatus().getTranslatedText()))
                .hoverPane(statusIcon)
                .build();
        }

        updateJobPage(citizen, this, colony);
    }

    public ICitizenDataView getCitizen()
    {
        return citizen;
    }

    @Override
    public boolean canFulFill()
    {
        return true;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (tick++ == 20)
        {
            tick = 0;
            createSkillContent(citizen, this);
        }
    }

    /**
     * Called when the gui is opened by an player.
     */
    @Override
    public void onOpened()
    {
        super.onOpened();
        findPaneOfTypeByID(WINDOW_ID_NAME, Text.class).setText(citizen.getName());

        createHealthBar(citizen, findPaneOfTypeByID(WINDOW_ID_HEALTHBAR, View.class));
        createSaturationBar(citizen, this);
        createHappinessBar(citizen, this);
        createSkillContent(citizen, this);
        updateHappiness(citizen, this);

        //Tool of class:§rwith minimal level:§rWood or Gold§r and§rwith maximal level:§rWood or Gold§r

        if (citizen.isFemale())
        {
            findPaneOfTypeByID(WINDOW_ID_GENDER, Image.class).setImage(FEMALE_SOURCE);
        }

        setPage(false, 0);
    }

    /**
     * Creates an health bar according to the citizen maxHealth and currentHealth.
     *
     * @param citizen       the citizen.
     * @param healthBarView the health bar view.
     */
    public static void createHealthBar(final ICitizenDataView citizen, final View healthBarView)
    {
        int health = (int) citizen.getHealth();

        healthBarView.setAlignment(Alignment.MIDDLE_RIGHT);
        healthBarView.findPaneOfTypeByID(WINDOW_ID_HEALTHLABEL, Text.class).setText(Integer.toString(health / 2));

        // Add Empty heart background
        for (int i = 0; i < MAX_HEART_ICONS; i++)
        {
            addHeart(healthBarView, i, HeartsEnum.EMPTY);
        }

        // Current Heart we're filling
        int heartPos = 0;

        // Order we're filling the hearts with from high to low
        final List<HeartsEnum> heartList = new ArrayList<>();
        heartList.add(HeartsEnum.BLUE);
        heartList.add(HeartsEnum.GREEN);
        heartList.add(HeartsEnum.GOLDEN);
        heartList.add(HeartsEnum.RED);

        // Iterate through hearts
        for (final HeartsEnum heart : heartList)
        {
            if (heart.isHalfHeart || heart.prevHeart == null)
            {
                continue;
            }

            // Add full hearts
            for (int i = heartPos; i < MAX_HEART_ICONS && health > (heart.prevHeart.hpValue * MAX_HEART_ICONS + 1); i++)
            {
                addHeart(healthBarView, heartPos, heart);
                health -= (heart.hpValue - heart.prevHeart.hpValue);
                heartPos++;
            }

            // Add half heart
            if (health % 2 == 1 && heartPos < MAX_HEART_ICONS && heart.halfHeart != null && health > heart.prevHeart.hpValue * MAX_HEART_ICONS)
            {
                addHeart(healthBarView, heartPos, heart.prevHeart);
                addHeart(healthBarView, heartPos, heart.halfHeart);

                health -= (heart.halfHeart.hpValue - heart.prevHeart.hpValue);
                heartPos++;
            }
            // Finished
            if (heartPos >= MAX_HEART_ICONS)
            {
                return;
            }
        }
    }

    /**
     * Adds a heart to the healthbarView at the given Position
     *
     * @param healthBarView the health bar to add the heart to.
     * @param heartPos      the number of the heart to add.
     * @param heart         the heart to add.
     */
    private static void addHeart(final View healthBarView, final int heartPos, final HeartsEnum heart)
    {
        @NotNull final Image heartImage = new Image();
        heartImage.setImage(heart.Image, heart.X, heart.Y, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH, false);
        heartImage.setPosition(heartPos * HEART_ICON_POS_X + HEART_ICON_OFFSET_X, HEART_ICON_POS_Y);
        healthBarView.addChild(heartImage);
    }

    /**
     * Get vertical offset for the saturation icon based on the iteration
     * If i >= 10, move the icons down another line
     * @param i the current iteration
     * @return the y offset
     */
    private static int getYOffset(final int i)
    {
        return (i >= 10 ? SATURATION_ICON_POS_Y : 0);
    }

    /**
     * Get horizontal offset modifier for the saturation icon based on the iteration
     * if i >= 10, decrease i by 10 to start the line from the beginning
     * @param i the current iteration
     * @return the x offset modifier
     */
    private static int getXOffsetModifier(final int i)
    {
        return (i >= 10 ? i - 10 : i);
    }

    /**
     * Creates an health bar according to the citizen maxHealth and currentHealth.
     *
     * @param citizen the citizen.
     * @param view    the view to add these to.
     */
    public static void createSaturationBar(final ICitizenDataView citizen, final View view)
    {
        view.findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).setAlignment(Alignment.MIDDLE_RIGHT);

        //Max saturation (Black food items).
        for (int i = 0; i < ICitizenData.MAX_SATURATION; i++)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(Screen.GUI_ICONS_LOCATION,
              EMPTY_SATURATION_ITEM_ROW_POS,
              SATURATION_ICON_COLUMN,
              SATURATION_ICON_HEIGHT_WIDTH,
              SATURATION_ICON_HEIGHT_WIDTH,
              false);

            saturation.setPosition(getXOffsetModifier(i) * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y + getYOffset(i));
            view.findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }

        //Current saturation (Full food hearts).
        int saturationPos;
        for (saturationPos = 0; saturationPos < ((int) citizen.getSaturation()); saturationPos++)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(Screen.GUI_ICONS_LOCATION, FULL_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH, false);
            saturation.setPosition(getXOffsetModifier(saturationPos) * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y + getYOffset(saturationPos));
            view.findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }

        //Half food items.
        if (citizen.getSaturation() / 2 % 1 > 0)
        {
            @NotNull final Image saturation = new Image();
            saturation.setImage(Screen.GUI_ICONS_LOCATION, HALF_SATURATION_ITEM_ROW_POS, SATURATION_ICON_COLUMN, SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH, false);
            saturation.setPosition(getXOffsetModifier(saturationPos) * SATURATION_ICON_POS_X + SATURATION_ICON_OFFSET_X, SATURATION_ICON_POS_Y + getYOffset(saturationPos));
            view.findPaneOfTypeByID(WINDOW_ID_SATURATION_BAR, View.class).addChild(saturation);
        }
    }

    /**
     * Creates an Happiness bar according to the citizen maxHappiness and currentHappiness.
     *
     * @param citizen pointer to the citizen data view
     * @param window  pointer to the current window
     */
    public static void createHappinessBar(final ICitizenDataView citizen, final AbstractWindowSkeleton window)
    {
        //Calculates how much percent of the next level has been completed. 
        final double experienceRatio = (citizen.getHappiness() / HappinessConstants.MAX_HAPPINESS) * XP_BAR_WIDTH;
        window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).setAlignment(Alignment.MIDDLE_RIGHT);
        window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS, Text.class).setText(Integer.toString((int) citizen.getHappiness()));

        @NotNull final Image xpBar = new Image();
        xpBar.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN, HAPPINESS_BAR_EMPTY_ROW, XP_BAR_WIDTH, XP_HEIGHT, false);
        xpBar.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y);

        @NotNull final Image xpBar2 = new Image();
        xpBar2.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN_END, HAPPINESS_BAR_EMPTY_ROW, XP_BAR_ICON_COLUMN_END_WIDTH, XP_HEIGHT, false);
        xpBar2.setPosition(XP_BAR_ICON_END_OFFSET + LEFT_BORDER_X, LEFT_BORDER_Y);

        window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).addChild(xpBar);
        window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).addChild(xpBar2);

        if (experienceRatio > 0)
        {
            @NotNull final Image xpBarFull = new Image();
            xpBarFull.setImage(Screen.GUI_ICONS_LOCATION, XP_BAR_ICON_COLUMN, HAPPINESS_BAR_FULL_ROW, (int) experienceRatio, XP_HEIGHT, false);
            xpBarFull.setPosition(LEFT_BORDER_X, LEFT_BORDER_Y);
            window.findPaneOfTypeByID(WINDOW_ID_HAPPINESS_BAR, View.class).addChild(xpBarFull);
        }
    }

    /**
     * Fills the citizen gui with it's skill values.
     *  @param citizen the citizen to use.
     * @param window  the window to fill.
     */
    public static void createSkillContent(final ICitizenDataView citizen, final AbstractWindowSkeleton window)
    {
        final boolean isCreative = Minecraft.getInstance().player.isCreative();
        for (final Map.Entry<Skill, Tuple<Integer, Double>> entry : citizen.getCitizenSkillHandler().getSkills().entrySet())
        {
            final String id = entry.getKey().name().toLowerCase(Locale.US);
            window.findPaneOfTypeByID(id, Text.class).setText(new StringTextComponent(Integer.toString(entry.getValue().getA())));

            final Pane buttons = window.findPaneByID(id + "_bts");
            if (buttons != null)
            {
                buttons.setEnabled(isCreative);
            }
        }
    }

    @Override
    public ImmutableList<IRequest<?>> getOpenRequestsFromBuilding(final IBuildingView building)
    {
        return building.getOpenRequests(citizen);
    }

    /**
     * Go to the request list.
     */
    public void goToRequestList()
    {
        findPaneOfTypeByID(VIEW_HEAD, SwitchView.class).nextView();
        buttonPrevPage.off();
        buttonNextPage.off();
        pageNum.off();
    }

    /**
     * Called when a button in the citizen has been clicked.
     *
     * @param button the clicked button.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button.getID().contains(PLUS_PREFIX))
        {
            final String label = button.getID().replace(PLUS_PREFIX, "");
            final Skill skill = Skill.valueOf(StringUtils.capitalize(label));

            Network.getNetwork().sendToServer(new AdjustSkillCitizenMessage(colony, citizen, 1, skill));
        }
        else if (button.getID().contains(MINUS_PREFIX))
        {
            final String label = button.getID().replace(MINUS_PREFIX, "");
            final Skill skill = Skill.valueOf(StringUtils.capitalize(label));

            Network.getNetwork().sendToServer(new AdjustSkillCitizenMessage(colony, citizen, -1, skill));
        }

        switch (button.getID())
        {
            case BUTTON_REQUESTS:
                goToRequestList();
                break;
            case BUTTON_BACK:
                findPaneOfTypeByID(VIEW_HEAD, SwitchView.class).previousView();
                pageNum.on();
                setPage(true, 0);
                break;
            case INVENTORY_BUTTON_ID:
                Network.getNetwork().sendToServer(new OpenInventoryMessage(colony, citizen.getName(), citizen.getEntityId()));
                break;
            default:
                super.onButtonClicked(button);
                break;
        }
    }

    @Override
    public void fulfill(@NotNull final IRequest<?> tRequest)
    {
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
            final List<Integer> slots = InventoryUtils.findAllSlotsInItemHandlerWith(new InvWrapper(inventory), requestPredicate);
            final int invSize = inventory.getSizeInventory() - 5; // 4 armour slots + 1 shield slot
            int slot = -1;
            for (final Integer possibleSlot : slots)
            {
                if (possibleSlot < invSize)
                {
                    slot = possibleSlot;
                    break;
                }
            }

            if (slot == -1)
            {
                final ITextComponent chatMessage = new StringTextComponent("<" + citizen.getName() + "> " +
                                                                             LanguageHandler.format(COM_MINECOLONIES_CANT_TAKE_EQUIPPED, citizen.getName()))
                                                     .setStyle(Style.EMPTY.setBold(false).setFormatting(TextFormatting.WHITE)
                                                     );
                Minecraft.getInstance().player.sendMessage(chatMessage, Minecraft.getInstance().player.getUniqueID());

                return; // We don't have one that isn't in our armour slot
            }
            itemStack = inventory.getStackInSlot(slot);
        }


        if (citizen.getWorkBuilding() != null)
        {
            colony.getBuilding(citizen.getWorkBuilding()).onRequestedRequestComplete(colony.getRequestManager(), tRequest);
        }
        Network.getNetwork().sendToServer(
          new TransferItemsToCitizenRequestMessage(colony, citizen, itemStack, isCreative ? amount : Math.min(amount, count)));

        final ItemStack copy = itemStack.copy();
        copy.setCount(isCreative ? amount : Math.min(amount, count));
        Network.getNetwork().sendToServer(new UpdateRequestStateMessage(colony, request.getId(), RequestState.OVERRULED, copy));
    }

    /**
     * Update the display for the happiness.
     *
     * @param citizen the citizen to update it for.
     * @param window  the window to add things to.
     */
    public static void updateHappiness(final ICitizenDataView citizen, final AbstractWindowSkeleton window)
    {
        final View pane = window.findPaneOfTypeByID("happinessModifierView", View.class);
        window.findPaneOfTypeByID("happinessModifier", Text.class).setText(LanguageHandler.format("com.minecolonies.coremod.gui.happiness.happinessmodifier"));
        int yPos = 62;
        for (final String name : citizen.getHappinessHandler().getModifiers())
        {
            final double value = citizen.getHappinessHandler().getModifier(name).getFactor();

            final Image image = new Image();
            image.setSize(11, 11);
            image.setPosition(25, yPos);
            pane.addChild(image);

            final Text label = new Text();
            label.setSize(136, 11);
            label.setPosition(50, yPos);
            label.setColors(BLACK);
            label.setText(LanguageHandler.format("com.minecolonies.coremod.gui.townhall.happiness." + name));
            pane.addChild(label);
            PaneBuilders.tooltipBuilder().hoverPane(label).append(new TranslationTextComponent("com.minecolonies.coremod.gui.townhall.happiness.desc." + name)).build();

            if (value > 1.0)
            {
                image.setImage(GREEN_ICON);
                PaneBuilders.tooltipBuilder()
                    .append(new TranslationTextComponent("com.minecolonies.coremod.gui.happiness.positive"))
                    .hoverPane(image)
                    .build();
            }
            else if (value == 1)
            {
                image.setImage(BLUE_ICON);
                PaneBuilders.tooltipBuilder()
                    .append(new TranslationTextComponent("com.minecolonies.coremod.gui.happiness.neutral"))
                    .hoverPane(image)
                    .build();
            }
            else if (value > 0.75)
            {
                image.setImage(YELLOW_ICON);
                PaneBuilders.tooltipBuilder()
                    .append(new TranslationTextComponent("com.minecolonies.coremod.gui.happiness.slightlynegative"))
                    .hoverPane(image)
                    .build();
            }
            else
            {
                image.setImage(RED_ICON);
                PaneBuilders.tooltipBuilder()
                    .append(new TranslationTextComponent("com.minecolonies.coremod.gui.happiness.negative"))
                    .hoverPane(image)
                    .build();
            }

            yPos += 12;
        }
    }

    /**
     * Update the job page of the citizen.
     *
     * @param citizen       the citizen.
     * @param windowCitizen the window.
     * @param colony        the colony.
     */
    private static void updateJobPage(final ICitizenDataView citizen, final WindowCitizen windowCitizen, final IColonyView colony)
    {
        final IBuildingView building = colony.getBuilding(citizen.getWorkBuilding());

        if (building instanceof AbstractBuildingWorker.View && !(building instanceof BuildingLibrary.View))
        {
            windowCitizen.findPaneOfTypeByID(JOB_TITLE_LABEL, Text.class).setText(LanguageHandler.format("com.minecolonies.coremod.gui.citizen.job.label",
              LanguageHandler.format(citizen.getJob())));
            windowCitizen.findPaneOfTypeByID(JOB_DESC_LABEL, Text.class).setText(LanguageHandler.format("com.minecolonies.coremod.gui.citizen.job.desc"));

            final Skill primary = ((AbstractBuildingWorker.View) building).getPrimarySkill();
            windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_LABEL, Text.class)
              .setText(LanguageHandler.format("com.minecolonies.coremod.gui.citizen.job.skills." + primary.name().toLowerCase(Locale.US)) + " (100% XP)");
            windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_LABEL + IMAGE_APPENDIX, Image.class)
              .setImage(BASE_IMG_SRC + primary.name().toLowerCase(Locale.US) + ".png");

            if (primary.getComplimentary() != null && primary.getAdverse() != null)
            {
                windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_COM, Text.class)
                  .setText(LanguageHandler.format("com.minecolonies.coremod.gui.citizen.job.skills." + primary.getComplimentary().name().toLowerCase(Locale.US)) + " ("
                                  + PRIMARY_DEPENDENCY_SHARE + "% XP)");
                windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_COM + IMAGE_APPENDIX, Image.class)
                  .setImage(BASE_IMG_SRC + primary.getComplimentary().name().toLowerCase(Locale.US) + ".png");

                windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_ADV, Text.class)
                  .setText(LanguageHandler.format("com.minecolonies.coremod.gui.citizen.job.skills." + primary.getAdverse().name().toLowerCase(Locale.US)) + " (-"
                                  + PRIMARY_DEPENDENCY_SHARE + "% XP)");
                windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_ADV + IMAGE_APPENDIX, Image.class)
                  .setImage(BASE_IMG_SRC + primary.getAdverse().name().toLowerCase(Locale.US) + ".png");
            }

            final Skill secondary = ((AbstractBuildingWorker.View) building).getSecondarySkill();
            windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_LABEL, Text.class)
              .setText(LanguageHandler.format("com.minecolonies.coremod.gui.citizen.job.skills." + secondary.name().toLowerCase(Locale.US)) + " (50% XP)");
            windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_LABEL + IMAGE_APPENDIX, Image.class)
              .setImage(BASE_IMG_SRC + secondary.name().toLowerCase(Locale.US) + ".png");

            if (secondary.getComplimentary() != null && secondary.getAdverse() != null)
            {
                windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_COM, Text.class)
                  .setText(LanguageHandler.format("com.minecolonies.coremod.gui.citizen.job.skills." + secondary.getComplimentary().name().toLowerCase(Locale.US)) + " ("
                                  + SECONDARY_DEPENDENCY_SHARE + "% XP)");
                windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_COM + IMAGE_APPENDIX, Image.class)
                  .setImage(BASE_IMG_SRC + secondary.getComplimentary().name().toLowerCase(Locale.US) + ".png");

                windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_ADV, Text.class)
                  .setText(LanguageHandler.format("com.minecolonies.coremod.gui.citizen.job.skills." + secondary.getAdverse().name().toLowerCase(Locale.US)) + " (-"
                                  + SECONDARY_DEPENDENCY_SHARE + "% XP)");
                windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_ADV + IMAGE_APPENDIX, Image.class)
                  .setImage(BASE_IMG_SRC + secondary.getAdverse().name().toLowerCase(Locale.US) + ".png");
            }
        }
        else
        {
            windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_LABEL + IMAGE_APPENDIX, Image.class).hide();
            windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_COM + IMAGE_APPENDIX, Image.class).hide();
            windowCitizen.findPaneOfTypeByID(PRIMARY_SKILL_ADV + IMAGE_APPENDIX, Image.class).hide();
            windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_LABEL + IMAGE_APPENDIX, Image.class).hide();
            windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_COM + IMAGE_APPENDIX, Image.class).hide();
            windowCitizen.findPaneOfTypeByID(SECONDARY_SKILL_ADV + IMAGE_APPENDIX, Image.class).hide();
        }
    }
}
