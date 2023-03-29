package com.minecolonies.coremod.colony.interactionhandling;

import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.CHAT_LABEL_ID;
import static com.minecolonies.api.util.constant.WindowConstants.RESPONSE_BOX_ID;
import static com.minecolonies.coremod.client.gui.WindowInteraction.BUTTON_RESPONSE_ID;

/**
 * Interaction for recruiting visitors
 */
public class RecruitmentInteraction extends ServerCitizenInteraction
{
    /**
     * The icon NBT tag
     */
    private static final String RECRUITMENT_ICON = "recruitIcon";

    /**
     * The icon's res location which is displayed for this interaction
     */
    private static final ResourceLocation icon = new ResourceLocation(Constants.MOD_ID, "textures/icons/recruiticon.png");

    /**
     * The recruit answer
     */
    private static final Tuple<Component, Component> recruitAnswer = new Tuple<>(Component.translatable("com.minecolonies.coremod.gui.chat.recruit"), null);

    @SuppressWarnings("unchecked")
    private static final Tuple<Component, Component>[] responses = (Tuple<Component, Component>[]) new Tuple[] {
      new Tuple<>(Component.translatable("com.minecolonies.coremod.gui.chat.showstats"), null),
      recruitAnswer,
      new Tuple<>(Component.translatable("com.minecolonies.coremod.gui.chat.notnow"), null)};

    public RecruitmentInteraction(final ICitizen data)
    {
        super(data);
    }

    public RecruitmentInteraction(
      final Component inquiry,
      final IChatPriority priority)
    {
        super(inquiry, true, priority, d -> true, null, responses);
    }

    @Override
    public List<IInteractionResponseHandler> genChildInteractions()
    {
        return Collections.emptyList();
    }

    @Override
    public String getType()
    {
        return ModInteractionResponseHandlers.RECRUITMENT.getPath();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onWindowOpened(final BOWindow window, final ICitizenDataView dataView)
    {
        final ButtonImage recruitButton = window.findPaneOfTypeByID(BUTTON_RESPONSE_ID + 2, ButtonImage.class);
        final Box group = window.findPaneOfTypeByID(RESPONSE_BOX_ID, Box.class);


        if (recruitButton != null && dataView instanceof IVisitorViewData)
        {
            final ItemStack recruitCost = ((IVisitorViewData) dataView).getRecruitCost();
            final IColonyView colony = (IColonyView) dataView.getColony();

            window.findPaneOfTypeByID(CHAT_LABEL_ID, Text.class).setText(PaneBuilders.textBuilder()
                .append(Component.literal(dataView.getName() + ": "))
                .append(this.getInquiry())
                .emptyLines(1)
                .append(Component.translatable(
                    colony.getCitizens().size() < colony.getCitizenCountLimit() ? "com.minecolonies.coremod.gui.chat.recruitcost"
                        : "com.minecolonies.coremod.gui.chat.nospacerecruit",
                    dataView.getName().split(" ")[0],
                    recruitCost.getCount() + " " + recruitCost.getHoverName().getString()))
                .getText());

            int iconPosX = recruitButton.getX() + recruitButton.getWidth() - 28;
            int iconPosY = recruitButton.getY() + recruitButton.getHeight() - 18;
            ItemIcon icon = new ItemIcon();
            icon.setID(RECRUITMENT_ICON);
            icon.setSize(15, 15);
            group.addChild(icon);
            icon.setItem(((IVisitorViewData) dataView).getRecruitCost());
            icon.setPosition(iconPosX, iconPosY);
            icon.setVisible(true);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClientResponseTriggered(final Component response, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        // Validate recruitment before returning true
        if (response.equals(recruitAnswer.getA()) && data instanceof IVisitorViewData)
        {
            if (player.isCreative() || InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.getInventory()), ((IVisitorViewData) data).getRecruitCost().getItem())
                  >= ((IVisitorViewData) data).getRecruitCost().getCount())
            {
                return super.onClientResponseTriggered(response, player, data, window);
            }
            else
            {
                MessageUtils.format(WARNING_RECRUITMENT_INSUFFICIENT_ITEMS).sendTo(player);
            }
        }
        return true;
    }

    @Override
    public void onServerResponseTriggered(final Component response, final Player player, final ICitizenData data)
    {
        if (response.equals(recruitAnswer.getA()) && data instanceof IVisitorData)
        {
            IColony colony = data.getColony();
            if (colony.getCitizenManager().getCurrentCitizenCount() < colony.getCitizenManager().getPotentialMaxCitizens())
            {
                if (player.isCreative() || InventoryUtils.attemptReduceStackInItemHandler(new InvWrapper(player.getInventory()),
                  ((IVisitorData) data).getRecruitCost(),
                  ((IVisitorData) data).getRecruitCost().getCount(), true, true))
                {
                    // Recruits visitor as new citizen and respawns entity
                    colony.getVisitorManager().removeCivilian(data);
                    data.setWorkBuilding(null);
                    data.setHomeBuilding(null);
                    data.setJob(null);

                    if (colony.getWorld().random.nextInt(100) <= MineColonies.getConfig().getServer().badVisitorsChance.get())
                    {
                        MessageUtils.format(MESSAGE_RECRUITMENT_RAN_OFF, data.getName()).sendTo(colony).forAllPlayers();
                        return;
                    }

                    // Create and read new citizen
                    ICitizenData newCitizen = colony.getCitizenManager().createAndRegisterCivilianData();
                    newCitizen.deserializeNBT(data.serializeNBT());
                    newCitizen.setParents("", "");

                    // Exchange entities
                    newCitizen.updateEntityIfNecessary();
                    data.getEntity().ifPresent(e -> e.remove(Entity.RemovalReason.DISCARDED));

                    if (data.hasCustomTexture())
                    {
                        MessageUtils.format(MESSAGE_RECRUITMENT_SUCCESS_CUSTOM, data.getName()).sendTo(colony).forAllPlayers();
                    }
                    else
                    {
                        MessageUtils.format(MESSAGE_RECRUITMENT_SUCCESS, data.getName()).sendTo(colony).forAllPlayers();
                    }
                }
            }
            else
            {
                MessageUtils.format(WARNING_NO_COLONY_SPACE).sendTo(player);
            }
        }
    }

    @Override
    public ResourceLocation getInteractionIcon()
    {
        return icon;
    }
}
