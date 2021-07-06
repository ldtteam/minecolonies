package com.minecolonies.coremod.colony.interactionhandling;

import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.Box;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.*;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.ModInteractionResponseHandlers;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.wrapper.InvWrapper;

import java.util.Collections;
import java.util.List;

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
    private static final Tuple<ITextComponent, ITextComponent> recruitAnswer = new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.recruit"), null);

    @SuppressWarnings("unchecked")
    private static final Tuple<ITextComponent, ITextComponent>[] responses = (Tuple<ITextComponent, ITextComponent>[]) new Tuple[] {
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.showstats"), null),
      recruitAnswer,
      new Tuple<>(new TranslationTextComponent("com.minecolonies.coremod.gui.chat.notnow"), null)};

    public RecruitmentInteraction(final ICitizen data)
    {
        super(data);
    }

    public RecruitmentInteraction(
      final ITextComponent inquiry,
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
    public void onWindowOpened(final Window window, final ICitizenDataView dataView)
    {
        final ButtonImage recruitButton = window.findPaneOfTypeByID(BUTTON_RESPONSE_ID + 2, ButtonImage.class);
        final Box group = window.findPaneOfTypeByID(RESPONSE_BOX_ID, Box.class);


        if (recruitButton != null && dataView instanceof IVisitorViewData)
        {
            final ItemStack recruitCost = ((IVisitorViewData) dataView).getRecruitCost();
            final IColonyView colony = ((IVisitorViewData) dataView).getColonyView();

            window.findPaneOfTypeByID(CHAT_LABEL_ID, Text.class).setText(PaneBuilders.textBuilder()
                .append(new StringTextComponent(dataView.getName() + ": "))
                .append(this.getInquiry())
                .emptyLines(1)
                .append(new TranslationTextComponent(
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
    public boolean onClientResponseTriggered(final ITextComponent response, final PlayerEntity player, final ICitizenDataView data, final Window window)
    {
        // Validate recruitment before returning true
        if (response.equals(recruitAnswer.getA()) && data instanceof IVisitorViewData)
        {
            if (player.isCreative() || InventoryUtils.getItemCountInItemHandler(new InvWrapper(player.inventory), ((IVisitorViewData) data).getRecruitCost().getItem())
                  >= ((IVisitorViewData) data).getRecruitCost().getCount())
            {
                return super.onClientResponseTriggered(response, player, data, window);
            }
            else
            {
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.chat.notenoughitems");
            }
        }
        return true;
    }

    @Override
    public void onServerResponseTriggered(final ITextComponent response, final PlayerEntity player, final ICitizenData data)
    {
        if (response.equals(recruitAnswer.getA()) && data instanceof IVisitorData)
        {
            IColony colony = data.getColony();
            if (colony.getCitizenManager().getCurrentCitizenCount() < colony.getCitizenManager().getPotentialMaxCitizens())
            {
                if (player.isCreative() || InventoryUtils.attemptReduceStackInItemHandler(new InvWrapper(player.inventory),
                  ((IVisitorData) data).getRecruitCost(),
                  ((IVisitorData) data).getRecruitCost().getCount()))
                {
                    // Recruits visitor as new citizen and respawns entity
                    colony.getVisitorManager().removeCivilian(data);
                    data.setWorkBuilding(null);
                    data.setHomeBuilding(null);
                    data.setJob(null);

                    if (colony.getWorld().random.nextInt(100) <= MineColonies.getConfig().getServer().badVisitorsChance.get())
                    {
                        LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(), "com.minecolonies.coremod.recruit.runaway", data.getName());
                        return;
                    }

                    // Create and read new citizen
                    ICitizenData newCitizen = colony.getCitizenManager().createAndRegisterCivilianData();
                    newCitizen.deserializeNBT(data.serializeNBT());
                    newCitizen.setParents("", "");

                    // Exchange entities
                    newCitizen.updateEntityIfNecessary();
                    data.getEntity().ifPresent(Entity::remove);

                    LanguageHandler.sendPlayersMessage(colony.getMessagePlayerEntities(), "com.minecolonies.coremod.recruit.message", data.getName());
                }
            }
            else
            {
                LanguageHandler.sendPlayerMessage(player, "com.minecolonies.coremod.gui.chat.nospace");
            }
        }
    }

    @Override
    public ResourceLocation getInteractionIcon()
    {
        return icon;
    }
}
