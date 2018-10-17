package com.minecolonies.coremod.colony;

import com.minecolonies.api.colony.requestsystem.requestable.IRequestable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Suppression;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingBarracksTower;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingHome;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.registry.JobRegistry;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.basic.AbstractAISkeleton;
import com.minecolonies.coremod.entity.citizenhandlers.CitizenHappinessHandler;
import com.minecolonies.coremod.inventory.InventoryCitizen;
import com.minecolonies.coremod.util.TeleportHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.minecolonies.api.util.constant.CitizenConstants.MAX_CITIZEN_LEVEL;
import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * Extra data for Citizens.
 */
@SuppressWarnings(Suppression.BIG_CLASS)
public class CitizenData
{
    /**
     * Maximum saturation of a citizen.
     */
    public static final int MAX_SATURATION = 10;

    /**
     * The max health.
     */
    private static final float MAX_HEALTH = 20.0F;

    /**
     * Max level of an attribute a citizen may initially have.
     */
    private static final int LETTERS_IN_THE_ALPHABET = 26;

    /**
     * Minimum saturation of a citizen.
     */
    private static final int MIN_SATURATION = 0;

    /**
     * The chance the citizen has to level. is 1 in this number.
     */
    private static final int CHANCE_TO_LEVEL = 100;

    /**
     * The number of skills the citizen has.
     */
    private static final int AMOUNT_OF_SKILLS = 5;

    /**
     * The unique citizen id.
     */
    private final int id;

    /**
     * The colony the citizen belongs to.
     */
    private final Colony colony;

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
    private EntityPlayerMP originPlayerRestart;

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
    private BlockPos bedPos = BlockPos.ORIGIN;

    /**
     * The home building of the citizen.
     */
    @Nullable
    private AbstractBuilding homeBuilding;

    /**
     * The work building of the citizen.
     */
    @Nullable
    private AbstractBuildingWorker workBuilding;

    /**
     * The job of the citizen.
     */
    private AbstractJob job;

    /**
     * If the citizen is dirty (Has to be updated on client side).
     */
    private boolean dirty;

    /**
     * Its entitity.
     */
    @NotNull
    private WeakReference<EntityCitizen> entity;

    /**
     * Attributes, which influence the workers behaviour.
     * May be added more later.
     */
    private int    strength;
    private int    endurance;
    private int    charisma;
    private int    intelligence;
    private int    dexterity;
    private double health;
    private double maxHealth;

    /**
     * The citizens saturation at the current moment.
     */
    private double saturation;

    /**
     * The current experience level the citizen is on.
     */
    private int level = 0;

    /**
     * The total amount of experience the citizen has.
     * This also includes the amount of experience within their Experience Bar.
     */
    private double experience;

    /**
     * The last position of the citizen.
     */
    private BlockPos lastPosition = new BlockPos(0, 0, 0);

    /**
     * The citizen happiness handler.
     * +
     */
    private final CitizenHappinessHandler citizenHappinessHandler;

    /**
     * Create a CitizenData given an ID.
     * Used as a super-constructor or during loading.
     *
     * @param id     ID of the Citizen.
     * @param colony Colony the Citizen belongs to.
     */
    public CitizenData(final int id, final Colony colony)
    {
        this.id = id;
        this.colony = colony;
        inventory = new InventoryCitizen("Minecolonies Inventory", true, this);
        this.citizenHappinessHandler = new CitizenHappinessHandler(this);
    }

    /**
     * Creates CitizenData from tag compound.
     *
     * @param compound NBT compound to build from.
     * @param colony   Colony of the citizen.
     * @return CitizenData.
     */
    @NotNull
    public static CitizenData createFromNBT(@NotNull final NBTTagCompound compound, final Colony colony)
    {
        final int id = compound.getInteger(TAG_ID);
        final @NotNull CitizenData citizen = new CitizenData(id, colony);
        citizen.readFromNBT(compound);
        return citizen;
    }

    /**
     * Reads data from NBT-tag compound.
     *
     * @param compound NBT-Tag compound.
     */
    public void readFromNBT(@NotNull final NBTTagCompound compound)
    {
        name = compound.getString(TAG_NAME);
        female = compound.getBoolean(TAG_FEMALE);
        paused = compound.getBoolean(TAG_PAUSED);
        textureId = compound.getInteger(TAG_TEXTURE);

        //  Attributes
        level = compound.getInteger(TAG_LEVEL);
        experience = compound.getInteger(TAG_EXPERIENCE);
        health = compound.getFloat(TAG_HEALTH);
        maxHealth = compound.getFloat(TAG_MAX_HEALTH);

        final NBTTagCompound nbtTagSkillsCompound = compound.getCompoundTag("skills");
        strength = nbtTagSkillsCompound.getInteger("strength");
        endurance = nbtTagSkillsCompound.getInteger("endurance");
        charisma = nbtTagSkillsCompound.getInteger("charisma");
        intelligence = nbtTagSkillsCompound.getInteger("intelligence");
        dexterity = nbtTagSkillsCompound.getInteger("dexterity");
        saturation = compound.getDouble(TAG_SATURATION);

        if (compound.hasKey("job"))
        {
            setJob(JobRegistry.createFromNBT(this, compound.getCompoundTag("job")));
        }

        if (compound.hasKey(TAG_INVENTORY))
        {
            final NBTTagList nbttaglist = compound.getTagList(TAG_INVENTORY, 10);
            this.inventory.readFromNBT(nbttaglist);
            this.inventory.setHeldItem(EnumHand.MAIN_HAND, compound.getInteger(TAG_HELD_ITEM_SLOT));
            this.inventory.setHeldItem(EnumHand.OFF_HAND, compound.getInteger(TAG_OFFHAND_HELD_ITEM_SLOT));
        }
        citizenHappinessHandler.readFromNBT(compound);

        if (name.isEmpty())
        {
            name = generateName(new Random());
        }

        if (compound.hasKey(TAG_ASLEEP))
        {
            bedPos = BlockPosUtil.readFromNBT(compound, TAG_POS);
            isAsleep = compound.getBoolean(TAG_ASLEEP);
        }

        if (level > MAX_CITIZEN_LEVEL)
        {
            level = MAX_CITIZEN_LEVEL;
        }
    }

    /**
     * Return the entity instance of the citizen data. Respawn the citizen if
     * needed.
     *
     * @return {@link EntityCitizen} of the citizen data.
     */
    @NotNull
    public Optional<EntityCitizen> getCitizenEntity()
    {
        if (entity == null)
        {
            return Optional.empty();
        }

        final EntityCitizen citizen = entity.get();
        return Optional.ofNullable(citizen);
    }

    /**
     * Sets the entity of the citizen data.
     *
     * @param citizen {@link EntityCitizen} instance of the citizen data.
     */
    public void setCitizenEntity(@Nullable final EntityCitizen citizen)
    {
        if (entity != null)
        {
            entity.clear();
        }

        if (citizen != null)
        {
            entity = new WeakReference<>(citizen);
        }
    }

    /**
     * Marks the instance dirty.
     */
    public void markDirty()
    {
        dirty = true;
        colony.getCitizenManager().markCitizensDirty();
    }

    /**
     * Create a CitizenData View given it's saved NBTTagCompound.
     *
     * @param id  The citizen's id.
     * @param buf The network data.
     * @return View object of the citizen.
     */
    @Nullable
    public static CitizenDataView createCitizenDataView(final int id, final ByteBuf buf)
    {
        @Nullable CitizenDataView citizenDataView = new CitizenDataView(id);

        try
        {
            citizenDataView.deserialize(buf);
        }
        catch (final RuntimeException ex)
        {
            Log.getLogger().error(String.format("A CitizenData.View for #%d has thrown an exception during loading, its state cannot be restored. Report this to the mod author",
              citizenDataView.getId()), ex);
            citizenDataView = null;
        }

        return citizenDataView;
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
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (female ? 1 : 0);
        result = 31 * result + (colony != null ? colony.hashCode() : 0);
        result = 31 * result + strength;
        result = 31 * result + endurance;
        result = 31 * result + charisma;
        result = 31 * result + intelligence;
        result = 31 * result + dexterity;
        return result;
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
        if (female != data.female)
        {
            return false;
        }
        if (strength != data.strength)
        {
            return false;
        }
        if (endurance != data.endurance)
        {
            return false;
        }
        if (charisma != data.charisma)
        {
            return false;
        }
        if (intelligence != data.intelligence)
        {
            return false;
        }
        if (dexterity != data.dexterity)
        {
            return false;
        }
        if (name != null ? !name.equals(data.name) : (data.name != null))
        {
            return false;
        }
        return colony != null ? (data.colony != null && colony.getID() == data.colony.getID()) : (data.colony == null);
    }

    /**
     * Returns the colony of the citizen.
     *
     * @return colony of the citizen.
     */
    public Colony getColony()
    {
        return colony;
    }

    /**
     * Returns the id of the citizen.
     *
     * @return id of the citizen.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Create a CitizenData given a CitizenEntity.
     *
     * @param entity Entity to initialize from.
     */
    public void initializeFromEntity(@NotNull final EntityCitizen entity)
    {
        final Random rand = entity.getRNG();

        setCitizenEntity(entity);

        //Assign the gender before name
        female = rand.nextBoolean();
        paused = false;
        name = generateName(rand);

        textureId = CompatibilityUtils.getWorld(entity).rand.nextInt(Integer.MAX_VALUE);
        health = entity.getHealth();
        maxHealth = entity.getMaxHealth();
        experience = 0;
        level = 0;
        saturation = MAX_SATURATION;
        final int levelCap = (int) colony.getOverallHappiness();
        @NotNull final Random random = new Random();

        if (levelCap <= 1)
        {
            intelligence = 1;
            charisma = 1;
            strength = 1;
            endurance = 1;
            dexterity = 1;
        }
        else
        {
            intelligence = random.nextInt(levelCap - 1) + 1;
            charisma = random.nextInt(levelCap - 1) + 1;
            strength = random.nextInt(levelCap - 1) + 1;
            endurance = random.nextInt(levelCap - 1) + 1;
            dexterity = random.nextInt(levelCap - 1) + 1;
        }
        //Initialize the citizen skills and make sure they are never 0

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
            firstName = getRandomElement(rand, Configurations.names.femaleFirstNames);
        }
        else
        {
            firstName = getRandomElement(rand, Configurations.names.maleFirstNames);
        }

        middleInitial = String.valueOf(getRandomLetter(rand));
        lastName = getRandomElement(rand, Configurations.names.lastNames);

        if (Configurations.names.useMiddleInitial)
        {
            citizenName = String.format("%s %s. %s", firstName, middleInitial, lastName);
        }
        else
        {
            citizenName = String.format("%s %s", firstName, lastName);
        }

        // Check whether there's already a citizen with this name
        for (final CitizenData citizen : this.getColony().getCitizenManager().getCitizens())
        {
            if (citizen != null && citizen.getName().equals(citizenName))
            {
                // Oops - recurse this function and try again
                citizenName = generateName(rand);
            }
        }

        return citizenName;
    }

    /**
     * Returns the name of the citizen.
     *
     * @return name of the citizen.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns true if citizen is female, false for male.
     *
     * @return true for female, false for male.
     */
    public boolean isFemale()
    {
        return female;
    }

    /**
     * Check if the citizen is paused.
     */
    public void setPaused(final boolean p)
    {
        this.paused = p;
        markDirty();
    }

    /**
     * Check if the citizen is paused.
     *
     * @return true for paused, false for working.
     */
    public boolean isPaused()
    {
        return paused;
    }

    /**
     * Returns the texture id for the citizen.
     *
     * @return texture ID.
     */
    public int getTextureId()
    {
        return textureId;
    }

    /**
     * Adds experience of the citizen.
     *
     * @param xp the amount of xp to add.
     */
    public void addExperience(final double xp)
    {
        this.experience += xp;
    }

    /**
     * Sets the level of the citizen.
     */
    public void increaseLevel()
    {
        if (this.level < MAX_CITIZEN_LEVEL)
        {
            this.level += 1;
        }
    }

    /**
     * Returns whether or not the instance is dirty.
     *
     * @return true when dirty, otherwise false.
     */
    public boolean isDirty()
    {
        return dirty;
    }

    /**
     * Markt the instance not dirty.
     */
    public void clearDirty()
    {
        dirty = false;
    }

    /**
     * When a building is destroyed, inform the citizen so it can do any cleanup
     * of associations that the building's. own AbstractBuilding.onDestroyed did
     * not do.
     *
     * @param building building that is destroyed.
     */
    public void onRemoveBuilding(final AbstractBuilding building)
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

    /**
     * Returns the home building of the citizen.
     *
     * @return home building.
     */
    @Nullable
    public AbstractBuilding getHomeBuilding()
    {
        return homeBuilding;
    }

    /**
     * Sets the home of the citizen.
     *
     * @param building home building.
     */
    public void setHomeBuilding(@Nullable final AbstractBuilding building)
    {
        if (homeBuilding != null && building != null && !homeBuilding.equals(building))
        {
            homeBuilding.removeCitizen(this);
            markDirty();
        }

        if (building == null || building instanceof BuildingHome || building instanceof BuildingBarracksTower)
        {
            homeBuilding = building;
            markDirty();
        }

        if (getCitizenEntity().isPresent() && getCitizenEntity().get().getCitizenJobHandler().getColonyJob() == null)
        {
            getCitizenEntity().get().getCitizenJobHandler().setModelDependingOnJob(null);
        }
    }

    /**
     * Returns the work building of a citizen.
     *
     * @return home building of a citizen.
     */
    @Nullable
    public AbstractBuildingWorker getWorkBuilding()
    {
        return workBuilding;
    }

    /**
     * Sets the work building of a citizen.
     *
     * @param building work building.
     */
    public void setWorkBuilding(@Nullable final AbstractBuildingWorker building)
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
                    entityCitizen.tasks.removeTask(entityCitizen.tasks.taskEntries.stream()
                                                     .filter(task -> task.action instanceof AbstractAISkeleton)
                                                     .findFirst()
                                                     .orElse(null).action);
                });

                //  No place of employment, get rid of our job
                setJob(null);
                colony.getWorkManager().clearWorkForCitizen(this);
            }

            markDirty();
        }
    }

    /**
     * Updates {@link EntityCitizen} for the instance.
     */
    public void updateCitizenEntityIfNecessary()
    {
        final List<EntityCitizen> list = colony.getWorld()
                                           .getEntities(EntityCitizen.class,
                                             entityCitizen -> entityCitizen.getCitizenColonyHandler().getColonyId() == colony.getID()
                                                                && entityCitizen.getCitizenData().getId() == getId());

        if (!list.isEmpty())
        {
            setCitizenEntity(list.get(0));
            return;
        }

        //The current citizen entity seems to be gone (either on purpose or the game unloaded the entity)
        //No biggy lets respawn an entity.
        colony.getCitizenManager().spawnCitizen(this, colony.getWorld());

        //Since we might have respawned an entity in an unloaded chunk (Townhall is not loaded)
        //We check if we created one or not.
        getCitizenEntity().ifPresent(entityCitizen -> {

            BlockPos location = null;
            if (getWorkBuilding() == null)
            {
                if (colony.hasTownHall())
                {
                    location = colony.getBuildingManager().getTownHall().getLocation();
                }
            }
            else
            {
                location = getWorkBuilding().getLocation();
            }

            if (location != null)
            {
                TeleportHelper.teleportCitizen(entityCitizen, colony.getWorld(), location);
            }
        });
    }

    /**
     * Returns the job of the citizen.
     *
     * @return Job of the citizen.
     */
    public AbstractJob getJob()
    {
        return job;
    }

    /**
     * Sets the job of this citizen.
     *
     * @param job Job of the citizen.
     */
    public void setJob(final AbstractJob job)
    {
        this.job = job;

        getCitizenEntity().ifPresent(entityCitizen -> entityCitizen.getCitizenJobHandler().onJobChanged(job));

        markDirty();
    }

    /**
     * Returns the job subclass needed. Returns null on type mismatch.
     *
     * @param type the type of job wanted.
     * @param <J>  The job type returned.
     * @return the job this citizen has.
     */
    @Nullable
    public <J extends AbstractJob> J getJob(@NotNull final Class<J> type)
    {
        if (type.isInstance(job))
        {
            return type.cast(job);
        }

        return null;
    }

    /**
     * Writes the citizen data to an NBT-compound.
     *
     * @param compound NBT-Tag compound.
     * @return return the data in NBT format
     */
    public NBTTagCompound writeToNBT(@NotNull final NBTTagCompound compound)
    {
        compound.setInteger(TAG_ID, id);
        compound.setString(TAG_NAME, name);
        compound.setBoolean(TAG_FEMALE, female);
        compound.setBoolean(TAG_PAUSED, paused);
        compound.setInteger(TAG_TEXTURE, textureId);

        //  Attributes
        compound.setInteger(TAG_LEVEL, level);
        compound.setDouble(TAG_EXPERIENCE, experience);
        compound.setDouble(TAG_HEALTH, health);
        compound.setDouble(TAG_MAX_HEALTH, maxHealth);


        @NotNull final NBTTagCompound nbtTagSkillsCompound = new NBTTagCompound();
        nbtTagSkillsCompound.setInteger(TAG_SKILL_STRENGTH, strength);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_STAMINA, endurance);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_SPEED, charisma);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_INTELLIGENCE, intelligence);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_DEXTERITY, dexterity);
        compound.setTag(TAG_SKILLS, nbtTagSkillsCompound);
        compound.setDouble(TAG_SATURATION, saturation);

        if (job != null)
        {
            @NotNull final NBTTagCompound jobCompound = new NBTTagCompound();
            job.writeToNBT(jobCompound);
            compound.setTag("job", jobCompound);
        }

        compound.setTag(TAG_INVENTORY, inventory.writeToNBT(new NBTTagList()));
        compound.setInteger(TAG_HELD_ITEM_SLOT, inventory.getHeldItemSlot(EnumHand.MAIN_HAND));
        compound.setInteger(TAG_OFFHAND_HELD_ITEM_SLOT, inventory.getHeldItemSlot(EnumHand.OFF_HAND));

        BlockPosUtil.writeToNBT(compound, TAG_POS, bedPos);
        compound.setBoolean(TAG_ASLEEP, isAsleep);

        citizenHappinessHandler.writeToNBT(compound);
        return compound;
    }

    /**
     * Writes the citizen data to a byte buf for transition.
     *
     * @param buf Buffer to write to.
     */
    public void serializeViewNetworkData(@NotNull final ByteBuf buf)
    {
        ByteBufUtils.writeUTF8String(buf, name);
        buf.writeBoolean(female);

        buf.writeInt(getCitizenEntity().map(Entity::getEntityId).orElse(-1));

        buf.writeBoolean(paused);

        buf.writeBoolean(homeBuilding != null);
        if (homeBuilding != null)
        {
            BlockPosUtil.writeToByteBuf(buf, homeBuilding.getID());
        }

        buf.writeBoolean(workBuilding != null);
        if (workBuilding != null)
        {
            BlockPosUtil.writeToByteBuf(buf, workBuilding.getID());
        }

        //  Attributes
        buf.writeInt(getLevel());
        buf.writeDouble(getExperience());

        // If the entity is not present we assumes standard values.
        buf.writeFloat(getCitizenEntity().map(EntityCitizen::getHealth).orElse(MAX_HEALTH));
        buf.writeFloat(getCitizenEntity().map(EntityCitizen::getMaxHealth).orElse(MAX_HEALTH));

        buf.writeInt(getStrength());
        buf.writeInt(getEndurance());
        buf.writeInt(getCharisma());
        buf.writeInt(getIntelligence());
        buf.writeInt(getDexterity());
        buf.writeDouble(getSaturation());
        buf.writeDouble(citizenHappinessHandler.getHappiness());

        citizenHappinessHandler.serializeViewNetworkData(buf);

        ByteBufUtils.writeUTF8String(buf, (job != null) ? job.getName() : "");

        writeStatusToBuffer(buf);

        buf.writeInt(colony.getID());

        final NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("inventory", inventory.writeToNBT(new NBTTagList()));
        ByteBufUtils.writeTag(buf, compound);

        BlockPosUtil.writeToByteBuf(buf, lastPosition);
    }

    /**
     * Writes the citizen status to the byteBuffer.
     *
     * @param buf the buffer.
     */
    private void writeStatusToBuffer(@NotNull final ByteBuf buf)
    {
        final Optional<EntityCitizen> optionalEntityCitizen = getCitizenEntity();
        buf.writeInt(optionalEntityCitizen.map(entityCitizen -> entityCitizen.getCitizenStatusHandler().getLatestStatus().length).orElse(0));

        optionalEntityCitizen.ifPresent(entityCitizen -> {
            final ITextComponent[] latestStatus = entityCitizen.getCitizenStatusHandler().getLatestStatus();
            for (int i = 0; i < latestStatus.length; i++)
            {
                ByteBufUtils.writeUTF8String(buf, latestStatus[i] == null ? "" : latestStatus[i].getUnformattedText());
            }
        });
    }

    /**
     * Returns the level of the citizen.
     *
     * @return level of the citizen.
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Sets the level of the citizen.
     *
     * @param lvl the new level for the citizen.
     */
    public void setLevel(final int lvl)
    {
        if (level < MAX_CITIZEN_LEVEL)
        {
            this.level = lvl;
        }
    }

    /**
     * Returns the default chance to levelup
     */
    public int getChanceToLevel() {return CHANCE_TO_LEVEL;}

    /**
     * Getter for the saturation.
     *
     * @param extraSaturation the extra saturation
     */
    public void increaseSaturation(final double extraSaturation)
    {
        this.saturation = Math.min(MAX_SATURATION, this.saturation + Math.abs(extraSaturation));
    }

    /**
     * Getter for the saturation.
     *
     * @param extraSaturation the saturation to remove.
     */
    public void decreaseSaturation(final double extraSaturation)
    {
        this.saturation = Math.max(MIN_SATURATION, this.saturation - Math.abs(extraSaturation));
    }

    /**
     * Resets the experience and the experience level of the citizen.
     */
    public void resetExperienceAndLevel()
    {
        this.level = 0;
        this.experience = 0;
    }

    /**
     * Set the citizen name.
     *
     * @param name the name to set.
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Returns the experience of the citizen.
     *
     * @return experience of the citizen.
     */
    public double getExperience()
    {
        return experience;
    }

    /**
     * Strength getter.
     *
     * @return citizen Strength value.
     */
    public int getStrength()
    {
        return strength;
    }

    /**
     * Endurance getter.
     *
     * @return citizen Endurance value.
     */
    public int getEndurance()
    {
        return endurance;
    }

    /**
     * Charisma getter.
     *
     * @return citizen Charisma value.
     */
    public int getCharisma()
    {
        return charisma;
    }

    /**
     * Intelligence getter.
     *
     * @return citizen Intelligence value.
     */
    public int getIntelligence()
    {
        return intelligence;
    }

    /**
     * Dexterity getter.
     *
     * @return citizen Dexterity value.
     */
    public int getDexterity()
    {
        return dexterity;
    }

    /**
     * Set the last position of the citizen.
     *
     * @param lastPosition the last position.
     */
    public void setLastPosition(final BlockPos lastPosition)
    {
        this.lastPosition = lastPosition;
    }

    /**
     * Get the last position of the citizen.
     *
     * @return the last position.
     */
    public BlockPos getLastPosition()
    {
        return lastPosition;
    }

    /**
     * Getter for the saturation.
     *
     * @return the saturation.
     */
    public double getSaturation()
    {
        return this.saturation;
    }

    /**
     * Getter for the inventory.
     *
     * @return the direct reference to the citizen inventory.
     */
    public InventoryCitizen getInventory()
    {
        return inventory;
    }

    /**
     * Check if citizen is asleep.
     *
     * @return true if so.
     */
    public boolean isAsleep()
    {
        return isAsleep;
    }

    /**
     * Getter for the bedPos.
     *
     * @return the bedPos.
     */
    public BlockPos getBedPos()
    {
        return bedPos;
    }

    /**
     * Set asleep.
     *
     * @param asleep true if asleep.
     */
    public void setAsleep(final boolean asleep)
    {
        isAsleep = asleep;
    }

    /**
     * Set the bed pos.
     *
     * @param bedPos the pos to set.
     */
    public void setBedPos(final BlockPos bedPos)
    {
        this.bedPos = bedPos;
    }

    /**
     * Create a blocking request.
     *
     * @param requested the request to create.
     * @param <R>       the Type
     * @return the token of the request.
     */
    public <R extends IRequestable> IToken createRequest(@NotNull final R requested)
    {
        return getWorkBuilding().createRequest(this, requested, false);
    }

    /**
     * Create an async request.
     *
     * @param requested the request to create.
     * @param <R>       the Type
     * @return the token of the request.
     */
    public <R extends IRequestable> IToken createRequestAsync(@NotNull final R requested)
    {
        return getWorkBuilding().createRequest(this, requested, true);
    }

    /**
     * Called on request canceled.
     *
     * @param token the token to be canceled.
     */
    public void onRequestCancelled(@NotNull final IToken token)
    {
        if (isRequestAsync(token))
        {
            job.getAsyncRequests().remove(token);
        }
    }

    /**
     * Check if a request is async.
     *
     * @param token the token to check.
     * @return true if it is.
     */
    public boolean isRequestAsync(@NotNull final IToken token)
    {
        if (job != null)
        {
            return job.getAsyncRequests().contains(token);
        }
        return false;
    }

    /**
     * The Handler for the citizens happiness.
     *
     * @return the instance of the handler
     */
    public CitizenHappinessHandler getCitizenHappinessHandler()
    {
        return citizenHappinessHandler;
    }

    /**
     * Try a random level up.
     */
    public void tryRandomLevelUp(final Random random)
    {
        tryRandomLevelUp(random, 0);
    }

    /**
     * Try a random level up.
     *
     * @param customChance set to 0 to not use, chance for levelup is 1/customChance
     */
    public void tryRandomLevelUp(final Random random, final int customChance)
    {
        if ((customChance > 0 && random.nextInt(customChance) > 0) || (customChance < 1 && random.nextInt(CHANCE_TO_LEVEL) > 0))
        {
            return;
        }

        final int levelCap = (int) getCitizenHappinessHandler().getHappiness();
        switch (random.nextInt(AMOUNT_OF_SKILLS))
        {
            case 0:
                intelligence = Math.min(intelligence + 1, levelCap);
                break;
            case 1:
                charisma = Math.min(charisma + 1, levelCap);
                break;
            case 2:
                strength = Math.min(strength + 1, levelCap);
                break;
            case 3:
                endurance = Math.min(endurance + 1, levelCap);
                break;
            default:
                dexterity = Math.min(dexterity + 1, levelCap);
                break;
        }
        markDirty();
    }

    /**
     * Schedule restart and cleanup
     * {@link com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIBasic#restart}
     */
    public void scheduleRestart(final EntityPlayerMP player)
    {
        originPlayerRestart = player;
        restartScheduled = true;
    }

    /**
     * AI will be restarted, also restart building etc
     */
    public boolean shouldRestart()
    {
        return restartScheduled;
    }

    /**
     * Restart done successfully
     */
    public void restartDone()
    {
        restartScheduled = false;
        LanguageHandler.sendPlayerMessage(originPlayerRestart, "com.minecolonies.coremod.gui.hiring.restartMessageDone", getName());
    }
}
