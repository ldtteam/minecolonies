package com.minecolonies.coremod.colony;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenHappinessHandler;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.coremod.colony.interactionhandling.ServerCitizenInteractionResponseHandler;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenHappinessHandler;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenSkillHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_OFFHAND_HELD_ITEM_SLOT;

/**
 * The CitizenDataView is the client-side representation of a CitizenData. Views
 * contain the CitizenData's data that is relevant to a Client, in a more
 * client-friendly form. Mutable operations on a View result in a message to the
 * server to perform the operation.
 */
public class CitizenDataView implements ICitizenDataView
{
    private static final String TAG_HELD_ITEM_SLOT = "HeldItemSlot";

    /**
     * Attributes.
     */
    private final int     id;
    private       int     entityId;
    private       String  name;
    private       boolean female;
    private       boolean paused;
    private       boolean isChild;

    /**
     * colony id of the citizen.
     */
    private int colonyId;

    /**
     * Placeholder skills.
     */
    private double health;
    private double maxHealth;
    private double saturation;

    /**
     * holds the current citizen happiness value
     */
    private double happiness;

    /**
     * holds the current citizen happiness modifiers for
     * each type of modifier.
     */
    private double foodModifier;
    private double healthmodifier;
    private double damageModifier;
    private double houseModifier;
    private double jobModifier;
    private double fieldsModifier;
    private double toolsModifiers;

    /**
     * The position of the guard.
     */
    private BlockPos position;

    /**
     * Job identifier.
     */
    private String job;

    /**
     * Working and home position.
     */
    @Nullable
    private BlockPos homeBuilding;
    @Nullable
    private BlockPos workBuilding;

    private InventoryCitizen inventory;

    /**
     * The citizen chat options on the server side.
     */
    private final Map<ITextComponent, IInteractionResponseHandler> citizenChatOptions = new HashMap<>();

    /**
     * If the citizen has any primary blocking interactions.
     */
    private boolean hasPrimaryBlockingInteractions;

    /**
     * If the citizen has any primary interactions.
     */
    private boolean hasAnyPrimaryInteraction;

    /**
     * List of primary interactions (sorted by priority).
     */
    private List<IInteractionResponseHandler> primaryInteractions;

    /**
     * The citizen skill handler on the client side.
     */
    private final CitizenSkillHandler citizenSkillHandler;

    /**
     * The citizen happiness handler.
     */
    private final CitizenHappinessHandler citizenHappinessHandler;
    /**
     * Set View id.
     *
     * @param id
     *            the id to set.
     */
    protected CitizenDataView(final int id)
    {
        this.id = id;
        this.citizenSkillHandler = new CitizenSkillHandler();
        this.citizenHappinessHandler = new CitizenHappinessHandler();
    }

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public int getEntityId()
    {
        return entityId;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isFemale()
    {
        return female;
    }

    @Override
    public boolean isPaused()
    {
        return paused;
    }

    @Override
    public boolean isChild()
    {
        return isChild;
    }

    /**
     * DEPRECATED
     */
    @Override
    public void setPaused(final boolean p)
    {
        this.paused = p;
    }

    @Override
    public String getJob()
    {
        return job;
    }

    @Override
    @Nullable
    public BlockPos getHomeBuilding()
    {
        return homeBuilding;
    }

    @Override
    @Nullable
    public BlockPos getWorkBuilding()
    {
        return workBuilding;
    }

    /**
     * DEPRECATED
     */
    @Override
    public void setWorkBuilding(final BlockPos bp)
    {
        this.workBuilding = bp;
    }

    @Override
    public int getColonyId()
    {
        return colonyId;
    }

    @Override
    public double getHappiness()
    {
        return happiness;
    }

    @Override
    public double getSaturation()
    {
        return saturation;
    }

    @Override
    public double getHealth()
    {
        return health;
    }

    @Override
    public double getMaxHealth()
    {
        return maxHealth;
    }

    @Override
    public BlockPos getPosition()
    {
        return position;
    }

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        name = buf.readString(32767);
        female = buf.readBoolean();
        entityId = buf.readInt();
        paused = buf.readBoolean();
        isChild = buf.readBoolean();

        homeBuilding = buf.readBoolean() ? buf.readBlockPos() : null;
        workBuilding = buf.readBoolean() ? buf.readBlockPos() : null;

        // Attributes
        health = buf.readFloat();
        maxHealth = buf.readFloat();

        saturation = buf.readDouble();
        happiness = buf.readDouble();

        citizenSkillHandler.read(buf.readCompoundTag());;

        job = buf.readString(32767);

        colonyId = buf.readInt();

        final CompoundNBT compound = buf.readCompoundTag();
        inventory = new InventoryCitizen(this.name, true);
        final ListNBT ListNBT = compound.getList("inventory", 10);
        this.inventory.read(ListNBT);
        this.inventory.setHeldItem(Hand.MAIN_HAND, compound.getInt(TAG_HELD_ITEM_SLOT));
        this.inventory.setHeldItem(Hand.OFF_HAND, compound.getInt(TAG_OFFHAND_HELD_ITEM_SLOT));

        position = buf.readBlockPos();

        citizenChatOptions.clear();
        final int size = buf.readInt();
        for (int i = 0; i < size; i++)
        {
            final CompoundNBT compoundNBT = buf.readCompoundTag();
            final ServerCitizenInteractionResponseHandler handler =
              (ServerCitizenInteractionResponseHandler) MinecoloniesAPIProxy.getInstance().getInteractionResponseHandlerDataManager().createFrom(this, compoundNBT);
            citizenChatOptions.put(handler.getInquiry(), handler);
        }

        primaryInteractions = citizenChatOptions.values().stream().filter(IInteractionResponseHandler::isPrimary).sorted(Comparator.comparingInt(e -> e.getPriority().getPriority())).collect(Collectors.toList());
        if (!primaryInteractions.isEmpty())
        {
            hasAnyPrimaryInteraction = true;
            hasPrimaryBlockingInteractions = primaryInteractions.get(0).getPriority().getPriority() >= ChatPriority.IMPORTANT.ordinal();
        }
        else
        {
            hasAnyPrimaryInteraction = false;
            hasPrimaryBlockingInteractions = false;
        }

        citizenHappinessHandler.read(buf.readCompoundTag());
    }

    @Override
    public InventoryCitizen getInventory()
    {
        return inventory;
    }

    /**
     * @return returns the current modifier related to food.
     */
    @Override
    public double getFoodModifier()
    {
        return foodModifier;
    }

    /**
     * @return returns the current modifier related to food.
     */
    @Override
    public double getHealthmodifier()
    {
        return healthmodifier;
    }

    /**
     * @return returns the current modifier related to damage.
     */
    @Override
    public double getDamageModifier()
    {
        return damageModifier;
    }

    /**
     * @return returns the current modifier related to house.
     */
    @Override
    public double getHouseModifier()
    {
        return houseModifier;
    }

    /**
     * @return returns the current modifier related to job.
     */
    @Override
    public double getJobModifier()
    {
        return jobModifier;
    }

    /**
     * @return returns the current modifier related to fields.
     */
    @Override
    public double getFieldsModifier()
    {
        return fieldsModifier;
    }

    /**
     * @return returns the current modifier related to tools.
     */
    @Override
    public double getToolsModifiers()
    {
        return toolsModifiers;
    }

    @Override
    public List<IInteractionResponseHandler> getOrderedInteractions()
    {
        return primaryInteractions;
    }

    @Override
    @Nullable
    public IInteractionResponseHandler getSpecificInteraction(@NotNull final ITextComponent component)
    {
        return citizenChatOptions.getOrDefault(component, null);
    }

    @Override
    public boolean hasBlockingInteractions()
    {
        return this.hasPrimaryBlockingInteractions;
    }

    @Override
    public boolean hasPendingInteractions()
    {
        return this.hasAnyPrimaryInteraction;
    }

    @Override
    public ICitizenSkillHandler getCitizenSkillHandler()
    {
        return citizenSkillHandler;
    }

    @Override
    public ICitizenHappinessHandler getHappinessHandler()
    {
        return citizenHappinessHandler;
    }
}
