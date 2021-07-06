package com.minecolonies.coremod.colony.interactionhandling;

import com.ldtteam.blockout.views.Window;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
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
    protected ITextComponent validatorId;

    /**
     * All registered parents of this response handler.
     */
    protected Set<ITextComponent> parents = new HashSet<>();

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
      final ITextComponent inquiry,
      final boolean primary,
      final IChatPriority priority,
      final Predicate<ICitizenData> validator,
      final ITextComponent validatorId,
      final Tuple<ITextComponent, ITextComponent>... responseTuples)
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
    public boolean isVisible(final World world)
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
    public void addParent(final ITextComponent parent)
    {
        this.parents.add(parent);
    }

    /**
     * Remove an old parent and return true if no parent is left.
     *
     * @param oldParent the parent to remove.
     */
    public void removeParent(final ITextComponent oldParent)
    {
        this.parents.remove(oldParent);
    }

    @Override
    public void onServerResponseTriggered(final ITextComponent response, final PlayerEntity player, final ICitizenData data)
    {
        if (response instanceof TranslationTextComponent)
        {
            if (((TranslationTextComponent) response).getKey().equals("com.minecolonies.coremod.gui.chat.remindmelater"))
            {
                displayAtWorldTick = (int) (player.level.getGameTime() + (TICKS_SECOND * 60 * 10));
            }
            else if (((TranslationTextComponent) response).getKey().equals("com.minecolonies.coremod.gui.chat.ignore"))
            {
                displayAtWorldTick = (int) (player.level.getGameTime() + (TICKS_SECOND * 60 * 20));
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean onClientResponseTriggered(final ITextComponent response, final PlayerEntity player, final ICitizenDataView data, final Window window)
    {
        if (((TranslationTextComponent) response).getKey().equals("com.minecolonies.coremod.gui.chat.skipchitchat"))
        {
            final MainWindowCitizen windowCitizen = new MainWindowCitizen(data);
            windowCitizen.open();
        }

        Network.getNetwork().sendToServer(new InteractionResponse(data.getColonyId(), data.getId(), player.level.dimension(), this.getInquiry(), response));
        return true;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT compoundNBT = super.serializeNBT();
        compoundNBT.putInt(TAG_DELAY, displayAtWorldTick);
        final ListNBT list = new ListNBT();
        for (final ITextComponent element : parents)
        {
            final CompoundNBT elementTag = new CompoundNBT();
            elementTag.putString(TAG_PARENT, ITextComponent.Serializer.toJson(element));
            list.add(elementTag);
        }
        compoundNBT.put(TAG_PARENTS, list);
        compoundNBT.putString(TAG_VALIDATOR_ID, ITextComponent.Serializer.toJson(validatorId));
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(@NotNull final CompoundNBT compoundNBT)
    {
        super.deserializeNBT(compoundNBT);
        this.displayAtWorldTick = compoundNBT.getInt(TAG_DELAY);
        this.parents.clear();
        final ListNBT list = compoundNBT.getList(TAG_PARENTS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++)
        {
            this.parents.add(ITextComponent.Serializer.fromJson(compoundNBT.getString(TAG_PARENT)));
        }
        this.validatorId = ITextComponent.Serializer.fromJson(compoundNBT.getString(TAG_VALIDATOR_ID));
        loadValidator();
    }

    /**
     * Load the validator.
     */
    protected void loadValidator()
    {
        this.validator = InteractionValidatorRegistry.getStandardInteractionValidatorPredicate(validatorId);
    }
}
