package com.minecolonies.coremod.colony.interactionhandling;

import com.minecolonies.api.colony.ICitizen;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.interactionhandling.*;
import com.minecolonies.api.util.Tuple;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.network.messages.TriggerServerResponseHandlerMessage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * The server side interaction response handler.
 */
public abstract class ServerCitizenInteractionResponseHandler extends AbstractInteractionResponseHandler
{
    private static final String TAG_DELAY     = "delay";
    private static final String TAG_PARENT    = "parent";
    private static final String TAG_PARENTS   = "parents";
    private static final String TAG_VALIDATOR_ID = "validator";

    /**
     * At which world tick this should be displayed again.
     */
    private int displayAtWorldTick     = 0;

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
     * @param inquiry the client inquiry.
     * @param primary if primary interaction.
     * @param priority the interaction priority.
     * @param validator validation predicate to check if this interaction is still valid.
     * @param validatorId the id of the validator.
     * @param responseTuples the tuples mapping player responses to further interactions.
     */
    @SafeVarargs
    public ServerCitizenInteractionResponseHandler(
      final ITextComponent inquiry,
      final boolean primary,
      final IChatPriority priority,
      final Predicate<ICitizenData> validator,
      final ITextComponent validatorId,
      final Tuple<ITextComponent, ITextComponent>...responseTuples)
    {
        super(inquiry, primary, priority, responseTuples);
        this.validator = validator;
        this.validatorId = validatorId;
    }

    /**
     * Way to load the response handler for a citizen.
     * @param data the citizen owning this handler.
     */
    public ServerCitizenInteractionResponseHandler(final ICitizen data)
    {
        super();
    }

    @Override
    public boolean isVisible(final World world)
    {
        return displayAtWorldTick == 0 || displayAtWorldTick < world.getTotalWorldTime();
    }

    @Override
    public boolean isValid(final ICitizenData citizen)
    {
        return (validator == null && !this.parents.isEmpty()) || ( validator != null && validator.test(citizen) );
    }

    /**
     * Add a parent to the list.
     * @param parent the parent to add.
     */
    public void addParent(final ITextComponent parent)
    {
        this.parents.add(parent);
    }

    /**
     * Remove an old parent and return true if no parent is left.
     * @param oldParent the parent to remove.
     */
    public void removeParent(final ITextComponent oldParent)
    {
        this.parents.remove(oldParent);
    }

    @Override
    public void onServerResponseTriggered(final ITextComponent response, final World world, final ICitizenData data)
    {
        if (response instanceof TranslationTextComponent)
        {
            if (((TranslationTextComponent) response).getKey().equals("com.minecolonies.coremod.gui.chat.remindmelater"))
            {
                displayAtWorldTick = (int) (world.getTotalWorldTime() + (TICKS_SECOND * 60 * 10));
            }
            else if (((TranslationTextComponent) response).getKey().equals("com.minecolonies.coremod.gui.chat.ignore"))
            {
                displayAtWorldTick = Integer.MAX_VALUE;
            }
        }
    }

    @Override
    public boolean onClientResponseTriggered(final ITextComponent response, final World world, final ICitizenDataView data, final Window window)
    {
        MineColonies.getNetwork().sendToServer(new TriggerServerResponseHandlerMessage(data.getColonyId(), data.getId(), world.provider.getDimension(), this.getInquiry(), response));
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        final NBTTagCompound compoundNBT = super.serializeNBT();
        compoundNBT.setInteger(TAG_DELAY, displayAtWorldTick);
        final NBTTagList list = new NBTTagList();
        for (final ITextComponent element : parents)
        {
            final NBTTagCompound elementTag = new NBTTagCompound();
            elementTag.setString(TAG_PARENT, CustomITextComponentSerializer.componentToJson(element));
            list.appendTag(elementTag);
        }
        compoundNBT.setTag(TAG_PARENTS, list);
        compoundNBT.setString(TAG_VALIDATOR_ID, CustomITextComponentSerializer.componentToJson(validatorId));
        return compoundNBT;
    }

    @Override
    public void deserializeNBT(@NotNull final NBTTagCompound compoundNBT)
    {
        super.deserializeNBT(compoundNBT);
        this.displayAtWorldTick = compoundNBT.getInteger(TAG_DELAY);
        this.parents.clear();
        final NBTTagList list = compoundNBT.getTagList(TAG_PARENTS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++)
        {
            this.parents.add(CustomITextComponentSerializer.fromJsonLenient(compoundNBT.getString(TAG_PARENT)));
        }
        this.validatorId = CustomITextComponentSerializer.fromJsonLenient(compoundNBT.getString(TAG_VALIDATOR_ID));
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
