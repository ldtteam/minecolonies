package com.minecolonies.coremod.colony;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorker;
import com.minecolonies.api.colony.interactionhandling.IInteractionResponseHandler;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.jobs.registry.IJobDataManager;
import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.entity.citizen.citizenhandlers.ICitizenSkillHandler;
import com.minecolonies.api.inventory.InventoryCitizen;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.interactionhandling.ServerCitizenInteractionResponseHandler;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenHappinessHandler;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.CitizenSkillHandler;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.citizen.AbstractEntityCitizen.*;
import static com.minecolonies.api.util.constant.CitizenConstants.BASE_MAX_HEALTH;
import static com.minecolonies.api.util.constant.CitizenConstants.MAX_CITIZEN_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Extra data for Citizens.
 */
@SuppressWarnings({Suppression.BIG_CLASS, "PMD.ExcessiveClassLength"})
public class CitizenData implements ICitizenData
{
    /**
     * The max health.
     */
    private static final float MAX_HEALTH = 20.0F;

    /**
     * Max levels of an attribute a citizen may initially have.
     */
    private static final int LETTERS_IN_THE_ALPHABET = 26;

    /**
     * Minimum saturation of a citizen.
     */
    private static final int MIN_SATURATION = 0;

    /**
     * The unique citizen id.
     */
    private final int id;

    /**
     * The colony the citizen belongs to.
     */
    private final IColony colony;

    /**
     * Inventory of the citizen.
     */
    private final InventoryCitizen inventory;

    /**
     * The name of the citizen.
     */
    private String name;

    /**
     * Boolean gender, true = female, false = male.
     */
    private boolean female;

    /**
     * Whether the citizen is still a child
     */
    private boolean isChild = false;

    /**
     * Boolean paused, true = paused, false = working.
     */
    private boolean paused;

    /**
     * If restart is scheduled.
     */
    private boolean restartScheduled;

    /**
     * Report end message to:
     */
    private ServerPlayerEntity originPlayerRestart;

    /**
     * The id of the citizens texture.
     */
    private int textureId;

    /**
     * If the citizen is asleep right now.
     */
    private boolean isAsleep;

    /**
     * The citizens current bedBos.
     */
    private BlockPos bedPos = BlockPos.ZERO;

    /**
     * The home building of the citizen.
     */
    @Nullable
    private IBuilding homeBuilding;

    /**
     * The work building of the citizen.
     */
    @Nullable
    private IBuildingWorker workBuilding;

    /**
     * The job of the citizen.
     */
    private IJob job;

    /**
     * If the citizen is dirty (Has to be updated on client side).
     */
    private boolean dirty;

    /**
     * Its entitity.
     */
    @NotNull
    private WeakReference<AbstractEntityCitizen> entity = new WeakReference<>(null);

    /**
     * The citizens saturation at the current moment.
     */
    private double saturation;

    /**
     * Variable indicating if a citizen just ate.
     */
    private boolean justAte;

    /**
     * The last position of the citizen.
     */
    private BlockPos lastPosition = new BlockPos(0, 0, 0);

    /**
     * The citizen happiness handler.
     */
    private final CitizenHappinessHandler citizenHappinessHandler;

    /**
     * The citizen skill handler.
     */
    private final CitizenSkillHandler citizenSkillHandler;

    /**
     * The citizen chat options on the server side.
     */
    private final Map<ITextComponent, IInteractionResponseHandler> citizenChatOptions = new HashMap<>();

    /**
     * Create a CitizenData given an ID. Used as a super-constructor or during loading.
     *
     * @param id     ID of the Citizen.
     * @param colony Colony the Citizen belongs to.
     */
    public CitizenData(final int id, final IColony colony)
    {
        this.id = id;
        this.colony = colony;
        inventory = new InventoryCitizen("Minecolonies Inventory", true, this);
        this.citizenHappinessHandler = new CitizenHappinessHandler(this);
        this.citizenSkillHandler = new CitizenSkillHandler();
    }

    @Override
    public void onResponseTriggered(@NotNull final ITextComponent key, @NotNull final ITextComponent response, final World world)
    {
        if (citizenChatOptions.containsKey(key))
        {
            citizenChatOptions.get(key).onServerResponseTriggered(response, world, this);
            markDirty();
        }
    }

    /**
     * Return the entity instance of the citizen data. Respawn the citizen if needed.
     *
     * @return {@link EntityCitizen} of the citizen data.
     */
    @Override
    @NotNull
    public Optional<AbstractEntityCitizen> getCitizenEntity()
    {
        if (entity == null)
        {
            return Optional.empty();
        }

        final AbstractEntityCitizen citizen = entity.get();
        return Optional.ofNullable(citizen);
    }

    @Override
    public void setCitizenEntity(@Nullable final AbstractEntityCitizen citizen)
    {
        if (entity.get() != null)
        {
            entity.clear();
        }

        if (citizen != null)
        {
            entity = new WeakReference<>(citizen);
        }
    }

    @Override
    public void markDirty()
    {
        dirty = true;
        colony.getCitizenManager().markCitizensDirty();
    }

    /**
     * Returns a random element in a list.
     *
     * @param rand  Random object.
     * @param array Array to select from.
     * @return Random element from array.
     */
    private static String getRandomElement(@NotNull final Random rand, @NotNull final String[] array)
    {
        return array[rand.nextInt(array.length)];
    }

    /**
     * Returns a random capital letter from the alphabet.
     *
     * @param rand Random object.
     * @return Random capital letter.
     */
    private static char getRandomLetter(@NotNull final Random rand)
    {
        return (char) (rand.nextInt(LETTERS_IN_THE_ALPHABET) + 'A');
    }

    @Override
    public int hashCode()
    {
        return id;
    }

    @SuppressWarnings(Suppression.TOO_MANY_RETURNS)
    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final CitizenData data = (CitizenData) o;

        if (id != data.id)
        {
            return false;
        }

        return colony != null ? (data.colony != null && colony.getID() == data.colony.getID()) : (data.colony == null);
    }

    @Override
    public IColony getColony()
    {
        return colony;
    }

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public void initForNewCitizen()
    {
        final Random rand = new Random();
        //Assign the gender before name
        female = rand.nextBoolean();
        paused = false;
        name = generateName(rand);
        textureId = rand.nextInt(255);

        saturation = MAX_SATURATION;
        final int levelCap = (int) colony.getOverallHappiness();

        citizenSkillHandler.init(levelCap);

        markDirty();
    }

    /**
     * Initializes the entities values from citizen data.
     */
    @Override
    public void initEntityValues()
    {
        if (!getCitizenEntity().isPresent())
        {
            return;
        }

        final AbstractEntityCitizen citizen = getCitizenEntity().get();

        citizen.setCitizenId(getId());
        citizen.getCitizenColonyHandler().setColonyId(getColony().getID());

        citizen.setIsChild(isChild());
        citizen.setCustomName(new StringTextComponent(getName()));

        citizen.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(BASE_MAX_HEALTH);

        citizen.setFemale(isFemale());
        citizen.setTextureId(getTextureId());

        citizen.getDataManager().set(DATA_COLONY_ID, colony.getID());
        citizen.getDataManager().set(DATA_CITIZEN_ID, citizen.getCitizenId());
        citizen.getDataManager().set(DATA_IS_FEMALE, citizen.isFemale() ? 1 : 0);
        citizen.getDataManager().set(DATA_TEXTURE, citizen.getTextureId());
        citizen.getDataManager().set(DATA_IS_ASLEEP, isAsleep());
        citizen.getDataManager().set(DATA_IS_CHILD, isChild());
        citizen.getDataManager().set(DATA_BED_POS, getBedPos());

        citizen.getCitizenExperienceHandler().updateLevel();

        setLastPosition(citizen.getPosition());

        citizen.getCitizenJobHandler().onJobChanged(citizen.getCitizenJobHandler().getColonyJob());

        markDirty();
    }

    /**
     * Generates a random name from a set of names.
     *
     * @param rand Random object.
     * @return Name of the citizen.
     */
    private String generateName(@NotNull final Random rand)
    {
        String citizenName;
        final String firstName;
        final String middleInitial;
        final String lastName;

        if (female)
        {
            firstName = getRandomElement(rand, MineColonies.getConfig().getServer().femaleFirstNames.get().toArray(new String[0]));
        }
        else
        {
            firstName = getRandomElement(rand, MineColonies.getConfig().getServer().maleFirstNames.get().toArray(new String[0]));
        }

        middleInitial = String.valueOf(getRandomLetter(rand));
        lastName = getRandomElement(rand, MineColonies.getConfig().getServer().lastNames.get().toArray(new String[0]));

        if (MineColonies.getConfig().getServer().useMiddleInitial.get())
        {
            citizenName = String.format("%s %s. %s", firstName, middleInitial, lastName);
        }
        else
        {
            citizenName = String.format("%s %s", firstName, lastName);
        }

        // Check whether there's already a citizen with this name
        for (final ICitizenData citizen : this.getColony().getCitizenManager().getCitizens())
        {
            if (citizen != null && citizen.getName().equals(citizenName))
            {
                // Oops - recurse this function and try again
                citizenName = generateName(rand);
                break;
            }
        }

        return citizenName;
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
    public void setIsFemale(@NotNull final boolean isFemale)
    {
        this.female = isFemale;
        this.name = generateName(new Random());
        markDirty();
    }

    @Override
    public void setPaused(final boolean p)
    {
        this.paused = p;
        markDirty();
    }

    @Override
    public boolean isPaused()
    {
        return paused;
    }

    @Override
    public int getTextureId()
    {
        return textureId;
    }

    @Override
    public boolean isDirty()
    {
        return dirty;
    }

    @Override
    public void clearDirty()
    {
        dirty = false;
    }

    @Override
    public void onRemoveBuilding(final IBuilding building)
    {
        if (getHomeBuilding() == building)
        {
            setHomeBuilding(null);
        }

        if (getWorkBuilding() == building)
        {
            setWorkBuilding(null);
        }
    }

    @Override
    @Nullable
    public IBuilding getHomeBuilding()
    {
        return homeBuilding;
    }

    @Override
    public void setHomeBuilding(@Nullable final IBuilding building)
    {
        if (homeBuilding != null && building != null && !homeBuilding.equals(building))
        {
            homeBuilding.removeCitizen(this);
        }

        homeBuilding = building;
        markDirty();

        if (getCitizenEntity().isPresent() && getCitizenEntity().get().getCitizenJobHandler().getColonyJob() == null)
        {
            getCitizenEntity().get().getCitizenJobHandler().setModelDependingOnJob(null);
        }
    }

    @Override
    @Nullable
    public IBuildingWorker getWorkBuilding()
    {
        return workBuilding;
    }

    @Override
    public void setWorkBuilding(@Nullable final IBuildingWorker building)
    {
        if (workBuilding != null && building != null && workBuilding != building)
        {
            Log.getLogger().warn("CitizenData.setWorkBuilding() - already assigned a work building when setting a new work building");
        }
        else if (workBuilding != building)
        {
            workBuilding = building;

            if (workBuilding != null)
            {
                //  We have a place to work, do we have the assigned Job?
                if (job == null)
                {
                    //  No job, create one!
                    setJob(workBuilding.createJob(this));
                    colony.getWorkManager().clearWorkForCitizen(this);
                }
            }
            else if (job != null)
            {
                getCitizenEntity().ifPresent(entityCitizen -> {
                    entityCitizen.getTasks()
                      .goals.stream()
                      .filter(task -> task.getGoal() instanceof AbstractAISkeleton)
                      .findFirst()
                      .ifPresent(e -> entityCitizen.getTasks().removeGoal(e));
                });

                //  No place of employment, get rid of our job
                setJob(null);
                colony.getWorkManager().clearWorkForCitizen(this);
            }

            markDirty();
        }
    }

    @Override
    public void updateCitizenEntityIfNecessary()
    {
        if (getCitizenEntity().isPresent())
        {
            return;
        }


        colony.getCitizenManager().spawnOrCreateCitizen(this, colony.getWorld(), lastPosition, true);
    }

    @Override
    public IJob getJob()
    {
        return job;
    }

    @Override
    public void setJob(final IJob job)
    {
        this.job = job;

        getCitizenEntity().ifPresent(entityCitizen -> entityCitizen.getCitizenJobHandler().onJobChanged(job));

        markDirty();
    }

    @Override
    @Nullable
    public <J extends IJob> J getJob(@NotNull final Class<J> type)
    {
        if (type.isInstance(job))
        {
            return type.cast(job);
        }

        return null;
    }

    @Override
    public void serializeViewNetworkData(@NotNull final PacketBuffer buf)
    {
        buf.writeString(name);
        buf.writeBoolean(female);

        buf.writeInt(getCitizenEntity().map(AbstractEntityCitizen::getEntityId).orElse(-1));

        buf.writeBoolean(paused);

        buf.writeBoolean(isChild);

        buf.writeBoolean(homeBuilding != null);
        if (homeBuilding != null)
        {
            buf.writeBlockPos(homeBuilding.getID());
        }

        buf.writeBoolean(workBuilding != null);
        if (workBuilding != null)
        {
            buf.writeBlockPos(workBuilding.getID());
        }

        // If the entity is not present we assumes standard values.
        buf.writeFloat(getCitizenEntity().map(AbstractEntityCitizen::getHealth).orElse(MAX_HEALTH));
        buf.writeFloat(getCitizenEntity().map(AbstractEntityCitizen::getMaxHealth).orElse(MAX_HEALTH));

        buf.writeDouble(getSaturation());
        buf.writeDouble(citizenHappinessHandler.getHappiness());

        citizenHappinessHandler.serializeViewNetworkData(buf);
        buf.writeCompoundTag(citizenSkillHandler.write());

        buf.writeString((job != null) ? job.getName() : "");

        buf.writeInt(colony.getID());

        final CompoundNBT compound = new CompoundNBT();
        compound.put("inventory", inventory.write(new ListNBT()));
        buf.writeCompoundTag(compound);
        buf.writeBlockPos(lastPosition);

        if (colony.getWorld() != null)
        {
            final List<IInteractionResponseHandler> subInteractions = citizenChatOptions.values().stream().filter(e -> e.isVisible(colony.getWorld())).collect(Collectors.toList());

            buf.writeInt(subInteractions.size());
            for (final IInteractionResponseHandler interactionHandler : subInteractions)
            {
                buf.writeCompoundTag(interactionHandler.serializeNBT());
            }
        }
        else
        {
            buf.writeInt(0);
        }
    }

    @Override
    public void increaseSaturation(final double extraSaturation)
    {
        this.saturation = Math.min(MAX_SATURATION, this.saturation + Math.abs(extraSaturation));
    }

    @Override
    public void decreaseSaturation(final double extraSaturation)
    {
        this.saturation = Math.max(MIN_SATURATION, this.saturation - Math.abs(extraSaturation * MineColonies.getConfig().getCommon().foodModifier.get()));
        this.justAte = false;
    }

    @Override
    public void setName(final String name)
    {
        this.name = name;
    }

    @Override
    public void setLastPosition(final BlockPos lastPosition)
    {
        this.lastPosition = lastPosition;
    }

    @Override
    public BlockPos getLastPosition()
    {
        return lastPosition;
    }

    @Override
    public double getSaturation()
    {
        return this.saturation;
    }

    @Override
    public InventoryCitizen getInventory()
    {
        return inventory;
    }

    @Override
    public boolean isAsleep()
    {
        return isAsleep;
    }

    @Override
    public BlockPos getBedPos()
    {
        return bedPos;
    }

    @Override
    public void setAsleep(final boolean asleep)
    {
        isAsleep = asleep;
    }

    @Override
    public void setBedPos(final BlockPos bedPos)
    {
        this.bedPos = bedPos;
    }

    @Override
    public CitizenHappinessHandler getCitizenHappinessHandler()
    {
        return citizenHappinessHandler;
    }

    @Override
    public ICitizenSkillHandler getCitizenSkillHandler()
    {
        return citizenSkillHandler;
    }

    @Override
    public void scheduleRestart(final ServerPlayerEntity player)
    {
        originPlayerRestart = player;
        restartScheduled = true;
    }

    @Override
    public boolean shouldRestart()
    {
        return restartScheduled;
    }

    @Override
    public void restartDone()
    {
        restartScheduled = false;
        LanguageHandler.sendPlayerMessage(originPlayerRestart, "com.minecolonies.coremod.gui.hiring.restartMessageDone", getName());
    }

    @Override
    public void setIsChild(final boolean isChild)
    {
        this.isChild = isChild;
        markDirty();

        if (colony != null)
        {
            colony.updateHasChilds();
        }
    }

    @Override
    public boolean isChild()
    {
        return isChild;
    }

    @Override
    public boolean justAte()
    {
        return this.justAte;
    }

    @Override
    public void setJustAte(final boolean justAte)
    {
        this.justAte = justAte;
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT nbtTagCompound = new CompoundNBT();

        nbtTagCompound.putInt(TAG_ID, id);
        nbtTagCompound.putString(TAG_NAME, name);
        nbtTagCompound.putBoolean(TAG_FEMALE, female);
        nbtTagCompound.putBoolean(TAG_PAUSED, paused);
        nbtTagCompound.putBoolean(TAG_CHILD, isChild);
        nbtTagCompound.putInt(TAG_TEXTURE, textureId);

        BlockPosUtil.write(nbtTagCompound, TAG_POS, lastPosition);

        nbtTagCompound.put(TAG_NEW_SKILLS, citizenSkillHandler.write());

        BlockPosUtil.write(nbtTagCompound, TAG_POS, lastPosition);
        nbtTagCompound.putDouble(TAG_SATURATION, saturation);

        if (job != null)
        {
            @NotNull final INBT jobCompound = job.serializeNBT();
            nbtTagCompound.put("job", jobCompound);
        }

        nbtTagCompound.put(TAG_INVENTORY, inventory.write(new ListNBT()));
        nbtTagCompound.putInt(TAG_HELD_ITEM_SLOT, inventory.getHeldItemSlot(Hand.MAIN_HAND));
        nbtTagCompound.putInt(TAG_OFFHAND_HELD_ITEM_SLOT, inventory.getHeldItemSlot(Hand.OFF_HAND));

        BlockPosUtil.write(nbtTagCompound, TAG_POS, bedPos);
        nbtTagCompound.putBoolean(TAG_ASLEEP, isAsleep);
        nbtTagCompound.putBoolean(TAG_JUST_ATE, justAte);

        citizenHappinessHandler.write(nbtTagCompound);

        @NotNull final ListNBT chatTagList = new ListNBT();
        for (@NotNull final IInteractionResponseHandler entry : citizenChatOptions.values())
        {
            @NotNull final CompoundNBT chatOptionCompound = new CompoundNBT();
            chatOptionCompound.put(TAG_CHAT_OPTION, entry.serializeNBT());
            chatTagList.add(chatOptionCompound);
        }
        nbtTagCompound.put(TAG_CHAT_OPTIONS, chatTagList);
        return nbtTagCompound;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbtTagCompound)
    {
        name = nbtTagCompound.getString(TAG_NAME);
        female = nbtTagCompound.getBoolean(TAG_FEMALE);
        paused = nbtTagCompound.getBoolean(TAG_PAUSED);
        isChild = nbtTagCompound.getBoolean(TAG_CHILD);
        textureId = nbtTagCompound.getInt(TAG_TEXTURE);

        lastPosition = BlockPosUtil.read(nbtTagCompound, TAG_POS);

        citizenSkillHandler.read(nbtTagCompound.getCompound(TAG_NEW_SKILLS));

        saturation = nbtTagCompound.getDouble(TAG_SATURATION);

        if (nbtTagCompound.keySet().contains("job"))
        {
            setJob(IJobDataManager.getInstance().createFrom(this, nbtTagCompound.getCompound("job")));
        }

        if (nbtTagCompound.keySet().contains(TAG_INVENTORY))
        {
            final ListNBT nbttaglist = nbtTagCompound.getList(TAG_INVENTORY, 10);
            this.inventory.read(nbttaglist);
            this.inventory.setHeldItem(Hand.MAIN_HAND, nbtTagCompound.getInt(TAG_HELD_ITEM_SLOT));
            this.inventory.setHeldItem(Hand.OFF_HAND, nbtTagCompound.getInt(TAG_OFFHAND_HELD_ITEM_SLOT));
        }

        if (name.isEmpty())
        {
            name = generateName(new Random());
        }

        if (nbtTagCompound.keySet().contains(TAG_ASLEEP))
        {
            bedPos = BlockPosUtil.read(nbtTagCompound, TAG_POS);
            isAsleep = nbtTagCompound.getBoolean(TAG_ASLEEP);
        }

        if (nbtTagCompound.keySet().contains(TAG_JUST_ATE))
        {
            justAte = nbtTagCompound.getBoolean(TAG_JUST_ATE);
        }

        //  Citizen chat options.
        if (nbtTagCompound.keySet().contains(TAG_CHAT_OPTIONS))
        {
            final ListNBT handlerTagList = nbtTagCompound.getList(TAG_CHAT_OPTIONS, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < handlerTagList.size(); ++i)
            {
                final ServerCitizenInteractionResponseHandler handler =
                  (ServerCitizenInteractionResponseHandler) MinecoloniesAPIProxy.getInstance()
                                                              .getInteractionResponseHandlerDataManager()
                                                              .createFrom(this, handlerTagList.getCompound(i).getCompound(TAG_CHAT_OPTION));
                citizenChatOptions.put(handler.getInquiry(), handler);
            }
        }
        citizenHappinessHandler.read(nbtTagCompound);

        if (nbtTagCompound.keySet().contains(TAG_LEVEL_MAP) && !nbtTagCompound.keySet().contains(TAG_NEW_SKILLS))
        {
            citizenSkillHandler.init((int) citizenHappinessHandler.getHappiness());
            final Map<String, Integer> levels = new HashMap<>();
            final ListNBT levelTagList = nbtTagCompound.getList(TAG_LEVEL_MAP, Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < levelTagList.size(); ++i)
            {
                final CompoundNBT levelExperienceAtJob = levelTagList.getCompound(i);
                final String jobName = levelExperienceAtJob.getString(TAG_NAME);
                final int level = Math.min(levelExperienceAtJob.getInt(TAG_LEVEL), MAX_CITIZEN_LEVEL);
                levels.put(jobName, level);
            }

            final Random random = new Random();
            for (final Map.Entry<String, Integer> entry : levels.entrySet())
            {
                final Skill primary = Skill.values()[random.nextInt(Skill.values().length)];
                final Skill secondary = Skill.values()[random.nextInt(Skill.values().length)];

                citizenSkillHandler.incrementLevel(primary, entry.getValue() / 2);
                citizenSkillHandler.incrementLevel(secondary, entry.getValue() / 4);
            }
        }
    }

    @Override
    public void tick()
    {
        if (!getCitizenEntity().isPresent() || !getCitizenEntity().get().isAlive())
        {
            return;
        }

        final List<IInteractionResponseHandler> toRemove = new ArrayList<>();
        for (final IInteractionResponseHandler handler : citizenChatOptions.values())
        {
            try
            {
                if (!handler.isValid(this))
                {
                    toRemove.add(handler);
                }
            }
            catch (final Exception e)
            {
                Log.getLogger().warn("Error during validation of handler: " + handler.getInquiry(), e);
                // If anything goes wrong in checking validity, remove handler.
                toRemove.add(handler);
            }
        }

        if (!toRemove.isEmpty())
        {
            markDirty();
        }

        for (final IInteractionResponseHandler handler : toRemove)
        {
            citizenChatOptions.remove(handler.getInquiry());
            for (final ITextComponent comp : handler.getPossibleResponses())
            {
                if (citizenChatOptions.containsKey(handler.getResponseResult(comp)))
                {
                    citizenChatOptions.get(handler.getResponseResult(comp)).removeParent(handler.getInquiry());
                }
            }
        }
    }

    @Override
    public void triggerInteraction(@NotNull final IInteractionResponseHandler handler)
    {
        if (!this.citizenChatOptions.containsKey(handler.getInquiry()))
        {
            this.citizenChatOptions.put(handler.getInquiry(), handler);
            for (final IInteractionResponseHandler childHandler : handler.genChildInteractions())
            {
                this.citizenChatOptions.put(childHandler.getInquiry(), (ServerCitizenInteractionResponseHandler) childHandler);
            }
            markDirty();
        }
    }

    @Override
    public int getJobModifier()
    {
        return getCitizenSkillHandler().getJobModifier(this);
    }

    // --------------------------- Request Handling --------------------------- //

    @Override
    public <R extends IRequestable> IToken createRequest(@NotNull final R requested)
    {
        return getWorkBuilding().createRequest(this, requested, false);
    }

    @Override
    public <R extends IRequestable> IToken createRequestAsync(@NotNull final R requested)
    {
        return getWorkBuilding().createRequest(this, requested, true);
    }

    @Override
    public void onRequestCancelled(@NotNull final IToken token)
    {
        if (isRequestAsync(token))
        {
            job.getAsyncRequests().remove(token);
        }
    }

    @Override
    public boolean isRequestAsync(@NotNull final IToken token)
    {
        if (job != null)
        {
            return job.getAsyncRequests().contains(token);
        }
        return false;
    }
}
