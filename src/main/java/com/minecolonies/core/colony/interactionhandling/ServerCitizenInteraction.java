package com.minecolonies.core.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.interactionhandling.AbstractInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.api.util.Utils;
import com.minecolonies.core.client.gui.citizen.MainWindowCitizen;
import com.minecolonies.core.network.messages.server.colony.InteractionResponse;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.core.colony.interactionhandling.StandardInteraction.*;

/**
 * The server side interaction response handler.
 */
public abstract class ServerCitizenInteraction extends AbstractInteractionResponseHandler
{
    private static final String TAG_DELAY        = "delay";
    private static final String TAG_PARENT       = "parent";
    private static final String TAG_PARENTS      = "parents";
    private static final String TAG_VALIDATOR_ID = "validator";

    /**
     * At which world tick this should be displayed again.
     */
    private int displayAtWorldTick = 0;

    /**
     * Validator to test for this.
     */
    private Predicate<ICitizenData> validator;

    /**
     * The id of the validator.
     */
    protected Component validatorId;

    /**
     * All registered parents of this response handler.
     */
    protected Set<Component> parents = new HashSet<>();

    /**
     * The server interaction response handler.
     *
     * @param inquiry        the client inquiry.
     * @param primary        if primary interaction.
     * @param priority       the interaction priority.
     * @param validator      validation predicate to check if this interaction is still valid.
     * @param validatorId    the id of the validator.
     * @param responseTuples the tuples mapping player responses to further interactions.
     */
    @SafeVarargs
    public ServerCitizenInteraction(
      final Component inquiry,
      final boolean primary,
      final IChatPriority priority,
      final Predicate<ICitizenData> validator,
      @NotNull final Component validatorId,
      final Tuple<Component, Component>... responseTuples)
    {
        super(inquiry, primary, priority, responseTuples);
        this.validator = validator;
        this.validatorId = validatorId;
    }

    /**
     * Way to load the response handler for a citizen.
     *
     * @param data the citizen owning this handler.
     */
    public ServerCitizenInteraction(final ICitizen data)
    {
        super();
    }

    @Override
    public boolean isVisible(final Level world)
    {
        return displayAtWorldTick == 0 || displayAtWorldTick < world.getGameTime();
    }

    @Override
    public boolean isValid(final ICitizenData citizen)
    {
        return (validator == null && !this.parents.isEmpty()) || (validator != null && validator.test(citizen));
    }

    /**
     * Add a parent to the list.
     *
     * @param parent the parent to add.
     */
    public void addParent(final Component parent)
    {
        this.parents.add(parent);
    }

    /**
     * Remove an old parent and return true if no parent is left.
     *
     * @param oldParent the parent to remove.
     */
    public void removeParent(final Component oldParent)
    {
        this.parents.remove(oldParent);
    }

    @Override
    public void onServerResponseTriggered(final int responseId, final Player player, final ICitizenData data)
    {
        final Component response = getPossibleResponses().get(responseId);
        tryHandleIgnoreResponse(response, player);
    }

    /**
     * Check if the response was an ignore response.
     * @param response the response to compare.
     * @param player the player that triggered it.
     */
    private void tryHandleIgnoreResponse(final Component response, final Player player)
    {
        if (response.getContents() instanceof TranslatableContents)
        {
            if (((TranslatableContents) response.getContents()).getKey().equals(INTERACTION_R_IGNORE))
            {
                // 6 hours later
                displayAtWorldTick = (int) (player.level().getGameTime() + (TICKS_SECOND * 60 * 60 * 6));
            }
            else if (((TranslatableContents) response.getContents()).getKey().equals(INTERACTION_R_REMIND))
            {
                // 1 hour later
                displayAtWorldTick = (int) (player.level().getGameTime() + (TICKS_SECOND * 60 * 60));
            }
            else if (((TranslatableContents) response.getContents()).getKey().equals(INTERACTION_R_OKAY) || ((TranslatableContents) response.getContents()).getKey().equals(INTERACTION_R_SKIP))
            {
                // 5 minutes
                displayAtWorldTick = (int) (player.level().getGameTime() + (TICKS_SECOND * 60 * 5));
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClientResponseTriggered(final int responseId, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        final Component response = getPossibleResponses().get(responseId);
        tryHandleIgnoreResponse(response, player);
        if (((TranslatableContents) getPossibleResponses().get(responseId).getContents()).getKey().equals("com.minecolonies.coremod.gui.chat.skipchitchat"))
        {
            final MainWindowCitizen windowCitizen = new MainWindowCitizen(data);
            windowCitizen.open();
        }

        new InteractionResponse(data.getColonyId(), data.getId(), player.level().dimension(), this.getInquiry(), responseId).sendToServer();
        return true;
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider)
    {
        final CompoundTag compoundNBT = super.serializeNBT(provider);
        compoundNBT.putInt(TAG_DELAY, displayAtWorldTick);
        final ListTag list = new ListTag();
        for (final Component element : parents)
        {
            list.add(Utils.serializeCodecMess(ComponentSerialization.CODEC, provider, element));
        }
        compoundNBT.put(TAG_PARENTS, list);
        compoundNBT.put(TAG_VALIDATOR_ID, Utils.serializeCodecMess(ComponentSerialization.CODEC, provider, validatorId));
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag compoundNBT)
    {
        super.deserializeNBT(provider, compoundNBT);
        this.displayAtWorldTick = compoundNBT.getInt(TAG_DELAY);
        this.parents.clear();
        final ListTag list = compoundNBT.getList(TAG_PARENTS, Tag.TAG_COMPOUND);
        for (Tag tag : list)
        {
            this.parents.add(Utils.deserializeCodecMess(ComponentSerialization.CODEC, provider, tag));
        }
        this.validatorId = Utils.deserializeCodecMess(ComponentSerialization.CODEC, provider, compoundNBT.get(TAG_VALIDATOR_ID));
        loadValidator();
    }

    /**
     * Load the validator.
     */
    protected void loadValidator()
    {
        if (validatorId == null)
        {
            validatorId = Component.empty();
            Log.getLogger().error("Validator id is null: " + this.getClass() + " " + this.getInquiry());
        }
        this.validator = InteractionValidatorRegistry.getStandardInteractionValidatorPredicate(validatorId);
    }

    @Override
    public Component getId()
    {
        return getInquiry();
    }
}
