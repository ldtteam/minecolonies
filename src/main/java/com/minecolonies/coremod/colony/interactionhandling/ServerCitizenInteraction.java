package com.minecolonies.coremod.colony.interactionhandling;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.interactionhandling.AbstractInteractionResponseHandler;
import com.minecolonies.api.colony.interactionhandling.IChatPriority;
import com.minecolonies.api.colony.interactionhandling.InteractionValidatorRegistry;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.citizen.MainWindowCitizen;
import com.minecolonies.coremod.network.messages.server.colony.InteractionResponse;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

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
      final Component validatorId,
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
    public void onServerResponseTriggered(final Component response, final Player player, final ICitizenData data)
    {
        if (response.getContents() instanceof TranslatableContents)
        {
            if (((TranslatableContents) response.getContents()).getKey().equals("com.minecolonies.coremod.gui.chat.remindmelater"))
            {
                displayAtWorldTick = (int) (player.level.getGameTime() + (TICKS_SECOND * 60 * 10));
            }
            else if (((TranslatableContents) response.getContents()).getKey().equals("com.minecolonies.coremod.gui.chat.ignore"))
            {
                displayAtWorldTick = (int) (player.level.getGameTime() + (TICKS_SECOND * 60 * 20));
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClientResponseTriggered(final Component response, final Player player, final ICitizenDataView data, final BOWindow window)
    {
        if (((TranslatableContents) response.getContents()).getKey().equals("com.minecolonies.coremod.gui.chat.skipchitchat"))
        {
            final MainWindowCitizen windowCitizen = new MainWindowCitizen(data);
            windowCitizen.open();
        }

        Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), this.getInquiry(), response));
        return true;
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundNBT = super.serializeNBT();
        compoundNBT.putInt(TAG_DELAY, displayAtWorldTick);
        final ListTag list = new ListTag();
        for (final Component element : parents)
        {
            final CompoundTag elementTag = new CompoundTag();
            elementTag.putString(TAG_PARENT, Component.Serializer.toJson(element));
            list.add(elementTag);
        }
        compoundNBT.put(TAG_PARENTS, list);
        compoundNBT.putString(TAG_VALIDATOR_ID, Component.Serializer.toJson(validatorId));
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(@NotNull final CompoundTag compoundNBT)
    {
        super.deserializeNBT(compoundNBT);
        this.displayAtWorldTick = compoundNBT.getInt(TAG_DELAY);
        this.parents.clear();
        final ListTag list = compoundNBT.getList(TAG_PARENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            this.parents.add(Component.Serializer.fromJson(compoundNBT.getString(TAG_PARENT)));
        }
        this.validatorId = Component.Serializer.fromJson(compoundNBT.getString(TAG_VALIDATOR_ID));
        loadValidator();
    }

    /**
     * Load the validator.
     */
    protected void loadValidator()
    {
        this.validator = InteractionValidatorRegistry.getStandardInteractionValidatorPredicate(validatorId);
    }

    @Override
    public Component getId()
    {
        return getInquiry();
    }
}
