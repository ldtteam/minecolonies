package com.minecolonies.api.entity.mobs;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.entity.mobs.barbarians.IChiefBarbarianEntity;
import com.minecolonies.api.entity.mobs.barbarians.IMeleeBarbarianEntity;
import com.minecolonies.api.entity.mobs.egyptians.IPharaoEntity;
import com.minecolonies.api.entity.mobs.pirates.ICaptainPirateEntity;
import com.minecolonies.api.entity.mobs.pirates.IPirateEntity;
import com.minecolonies.api.entity.mobs.vikings.IMeleeNorsemenEntity;
import com.minecolonies.api.entity.mobs.vikings.INorsemenChiefEntity;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Random;

import static com.minecolonies.api.util.constant.RaiderConstants.*;

/**
 * Util class for raider mobs/spawning
 */
public final class RaiderMobUtils
{

    /**
     * Distances in which spawns are spread
     */
    public static       double                                MOB_SPAWN_DEVIATION_STEPS = 0.3;

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Constants.MOD_ID);

    /**
     * Mob attribute, used for custom attack damage
     */
    public final static RegistryObject<Attribute> MOB_ATTACK_DAMAGE = ATTRIBUTES.register("mc_mob_damage", () -> new RangedAttribute( "mc_mob_damage", 2.0, 1.0, 20));

    /**
     * Damage increased by 1 for every 200 raid level difficulty
     */
    public static int DAMAGE_PER_X_RAID_LEVEL = 400;

    /**
     * Max damage from raidlevels
     */
    public static int MAX_RAID_LEVEL_DAMAGE = 3;

    private RaiderMobUtils()
    {
        throw new IllegalStateException("Tried to initialize: MobSpawnUtils but this is a Utility class.");
    }

    /**
     * Set mob attributes.
     *
     * @param mob    The mob to set the attributes on.
     * @param colony The colony that the mob is attacking.
     */
    public static void setMobAttributes(final AbstractEntityRaiderMob mob, final IColony colony)
    {
        final double difficultyModifier = colony.getRaiderManager().getRaidDifficultyModifier();
        mob.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(FOLLOW_RANGE * 2);
        mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(difficultyModifier < 2.4 ? MOVEMENT_SPEED : MOVEMENT_SPEED * 1.2);
        final int raidLevel = colony.getRaiderManager().getColonyRaidLevel();

        // Base damage
        final double attackDamage =
          ATTACK_DAMAGE +
            difficultyModifier *
              Math.min(raidLevel / DAMAGE_PER_X_RAID_LEVEL, MAX_RAID_LEVEL_DAMAGE);

        // Base health
        final double baseHealth = getHealthBasedOnRaidLevel(raidLevel) * difficultyModifier;

        mob.initStatsFor(baseHealth, difficultyModifier, attackDamage);
    }

    /**
     * Sets the entity's health based on the raidLevel
     *
     * @param raidLevel the raid level.
     * @return returns the health in the form of a double
     */
    public static double getHealthBasedOnRaidLevel(final int raidLevel)
    {
        return Math.max(BARBARIAN_BASE_HEALTH, (BARBARIAN_BASE_HEALTH + raidLevel * BARBARIAN_HEALTH_MULTIPLIER));
    }

    /**
     * Sets up and spawns the Barbarian entities of choice
     *
     * @param entityToSpawn  The entity which should be spawned
     * @param numberOfSpawns The number of times the entity should be spawned
     * @param spawnLocation  the location at which to spawn the entity
     * @param world          the world in which the colony and entity are
     * @param colony         the colony to spawn them close to.
     * @param eventID        the event id.
     */
    public static void spawn(
      final EntityType<?> entityToSpawn,
      final int numberOfSpawns,
      final BlockPos spawnLocation,
      final Level world,
      final IColony colony,
      final int eventID)
    {
        if (spawnLocation != null && entityToSpawn != null && world != null && numberOfSpawns > 0)
        {
            final int x = spawnLocation.getX();
            final int y = BlockPosUtil.getFloor(spawnLocation, world).getY();
            final int z = spawnLocation.getZ();
            double spawnDeviationX = 0;
            double spawnDeviationZ = 0;

            for (int i = 0; i < numberOfSpawns; i++)
            {
                final AbstractEntityRaiderMob entity = (AbstractEntityRaiderMob) entityToSpawn.create(world);

                if (entity != null)
                {
                    entity.absMoveTo(x + spawnDeviationX, y + 1.0, z + spawnDeviationZ, (float) Mth.wrapDegrees(world.random.nextDouble() * WHOLE_CIRCLE), 0.0F);
                    CompatibilityUtils.addEntity(world, entity);
                    entity.setColony(colony);
                    entity.setEventID(eventID);
                    entity.registerWithColony();
                    spawnDeviationZ += MOB_SPAWN_DEVIATION_STEPS;

                    if (spawnDeviationZ > 2)
                    {
                        spawnDeviationZ = 0;
                        spawnDeviationX += MOB_SPAWN_DEVIATION_STEPS;
                    }
                }
            }
        }
    }

    /**
     * Set the equipment of a certain mob.
     *
     * @param mob the equipment to set up.
     */
    public static void setEquipment(final AbstractEntityRaiderMob mob)
    {
        if (mob instanceof IMeleeBarbarianEntity || mob instanceof IMeleeNorsemenEntity || mob instanceof INorsemenChiefEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_AXE));
        }
        else if (mob instanceof IPharaoEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.pharaoscepter));
        }
        else if (mob instanceof IArcherMobEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
        else if (mob instanceof ISpearmanMobEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.spear));
        }
        else if (mob instanceof IChiefBarbarianEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.chiefSword));
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CHAINMAIL_HELMET));
            mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
            mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.CHAINMAIL_LEGGINGS));
            mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.CHAINMAIL_BOOTS));
        }
        else if (mob instanceof IPirateEntity)
        {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.scimitar));
            if (mob instanceof ICaptainPirateEntity)
            {
                if (new Random().nextBoolean())
                {
                    mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.pirateHelmet_1));
                    mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.pirateChest_1));
                    mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.pirateLegs_1));
                    mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.pirateBoots_1));
                }
                else
                {
                    mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.pirateHelmet_2));
                    mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.pirateChest_2));
                    mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(ModItems.pirateLegs_2));
                    mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.pirateBoots_2));
                }
            }
        }
    }

    /**
     * Returns the barbarians close to an entity.
     *
     * @param entity             The entity to test against
     * @param distanceFromEntity The distance to check for
     * @return the barbarians (if any) that is nearest
     */
    public static List<AbstractEntityRaiderMob> getBarbariansCloseToEntity(final Entity entity, final double distanceFromEntity)
    {
        return CompatibilityUtils.getWorldFromEntity(entity).getEntitiesOfClass(
          AbstractEntityRaiderMob.class,
          entity.getBoundingBox().expandTowards(
            distanceFromEntity,
            3.0D,
            distanceFromEntity),
          Entity::isAlive);
    }
}
