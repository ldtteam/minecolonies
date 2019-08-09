package com.minecolonies.coremod.entity;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.api.util.MathUtils;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Creates a custom fishHook for the Fisherman to throw.
 * This class manages the entity.
 */
public final class EntityFishHook extends Entity
{
    /**
     * Number of seconds to wait before removing a stuck hook.
     */
    private static final int TTL = 360;

    /**
     * Entity size to scale it down.
     */
    private static final float ENTITY_SIZE = 0.25F;

    /**
     * 180 degree used in trig. functions.
     */
    private static final double HALF_CIRCLE = 180.0;

    /**
     * Used as a speed multiplicator for drifting movement.
     */
    private static final double RANDOM_MOVEMENT_OFFSET = 0.007499999832361937;

    /**
     * Limits initial horizontal movement speed.
     */
    private static final double INITIAL_MOVEMENT_LIMITER = 0.16;

    /**
     * The hook starts a bit lower.
     */
    private static final double SUNKEN_OFFSET = 0.10000000149011612;

    /**
     * Multiplicator to get sum of edges.
     */
    private static final double NUM_BOUNDING_BOX_EDGES = 4.0;

    /**
     * factor to scale up to distance.
     */
    private static final double DISTANCE_FACTOR = 64.0;

    /**
     * Limits horizontal movement speed while bouncing.
     */
    private static final double BOUNCE_MOVEMENT_LIMITER = 0.2;

    /**
     * Limits horizontal movement speed while in air.
     */
    private static final float AIR_MOVEMENT_LIMITER = 0.92F;

    /**
     * Limits horizontal movement speed while on the ground.
     */
    private static final double GROUND_MOVEMENT_LIMITER = 0.5;

    /**
     * Limits horizontal movement speed while in the water.
     */
    private static final double WATER_MOVEMENT_LIMITER = 0.03999999910593033;

    /**
     * Chance to slow down fishing while the sky is not visible.
     */
    private static final double NO_CLEAR_SKY_CHANCE = 0.5;

    /**
     * Chance to get rare drops while fishing. Higher value leads to a lower
     * chance.
     */
    private static final int INCREASE_RARENESS_MODIFIER = 200;

    /**
     * entity creation time.
     * Used to check it the hook got stuck.
     */
    private final long                  creationTime;
    /**
     * The citizen who threw this rod.
     */
    private       AbstractEntityCitizen citizen;
    /**
     * The fishing speed enchantment level on the rod that threw this hook.
     */
    private       int                   fishingSpeedEnchantment;
    /**
     * The fishing loot enchantment level on the rod that threw this hook.
     */
    private       int                   fishingLootEnchantment;
    /**
     * If this hook is in the ground.
     */
    private       boolean               inGround;
    /**
     * A counter for at what position in the shaking movement the hook is.
     */
    private       int                   shake;
    private       int                   countdownNoFish;
    private       int                   countdownFishNear;
    private       int                   countdownFishBites;
    private       double                relativeRotation;
    /**
     * When a fish is on the hook, this will be true.
     */
    private       boolean               isFishCaugth = false;

    /**
     * Constructor for throwing out a hook.
     *
     * @param world   the world the hook lives in.
     * @param citizen the citizen throwing the hook.
     */
    public EntityFishHook(final World world, @NotNull final AbstractEntityCitizen citizen)
    {
        this(world);
        this.citizen = citizen;
        this.setLocationAndAngles(citizen.getPosX(),
          citizen.getPosY() + 1.62 - citizen.getYOffset(),
          citizen.getPosZ(),
          citizen.getRotationYaw(),
          citizen.getRotationPitch());
        this.posX -= Math.cos(this.rotationYaw / HALF_CIRCLE * Math.PI) * INITIAL_MOVEMENT_LIMITER;
        this.posY -= SUNKEN_OFFSET;
        this.posZ -= Math.sin(this.rotationYaw / HALF_CIRCLE * Math.PI) * INITIAL_MOVEMENT_LIMITER;
        this.setPosition(this.posX, this.posY, this.posZ);
        final double f = 0.4;
        this.setMotion(-Math.sin(this.rotationYaw / HALF_CIRCLE * Math.PI) * Math.cos(this.rotationPitch / HALF_CIRCLE * Math.PI) * f,
          Math.cos(this.rotationYaw / HALF_CIRCLE * Math.PI) * Math.cos(this.rotationPitch / HALF_CIRCLE * Math.PI) * f,
          -Math.sin(this.rotationPitch / HALF_CIRCLE * Math.PI) * f);
        this.setPosition(this.getMotion().x, this.getMotion().y, this.getMotion().z, 1.5, 1.0);
        fishingSpeedEnchantment = EnchantmentHelper.getEnchantmentLevel(Enchantments.LURE, citizen.getHeldItemMainhand());
        fishingLootEnchantment = EnchantmentHelper.getEnchantmentLevel(Enchantments.LUCK_OF_THE_SEA, citizen.getHeldItemMainhand());
    }

    /**
     * Lowest denominator constructor.
     * Used by other constructors to do general stuff.
     *
     * @param world the world this entity lives in.
     */
    public EntityFishHook(final World world)
    {
        super(world);
        this.setSize(ENTITY_SIZE, ENTITY_SIZE);
        this.ignoreFrustumCheck = true;
        this.creationTime = System.nanoTime();
        fishingLootEnchantment = 0;
        fishingSpeedEnchantment = 0;
    }

    private void setPosition(final double x, final double y, final double z, final double yaw, final double pitch)
    {
        final double squareRootXYZ = MathHelper.sqrt(x * x + y * y + z * z);
        double newX = x / squareRootXYZ;
        double newY = y / squareRootXYZ;
        double newZ = z / squareRootXYZ;
        newX += this.rand.nextGaussian() * RANDOM_MOVEMENT_OFFSET * pitch;
        newY += this.rand.nextGaussian() * RANDOM_MOVEMENT_OFFSET * pitch;
        newZ += this.rand.nextGaussian() * RANDOM_MOVEMENT_OFFSET * pitch;
        newX *= yaw;
        newY *= yaw;
        newZ *= yaw;
        this.setMotion(newX, newY, newZ);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(newX, newZ) * HALF_CIRCLE / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(newY, Math.sqrt(newX * newX + newZ * newZ)) * HALF_CIRCLE / Math.PI);
    }

    /**
     * Returns the citizen throwing the hook.
     *
     * @return a citizen.
     */
    public AbstractEntityCitizen getCitizen()
    {
        return citizen;
    }

    @Override
    public boolean equals(@Nullable final Object o)
    {
        return !(o == null || getClass() != o.getClass() || !super.equals(o));
    }

    @Override
    public int hashCode()
    {
        return getEntityId();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick()
    {
        super.tick();
        if (fishHookIsOverTimeToLive())
        {
            this.remove();
        }
        bounceFromGround();
        if (this.inGround)
        {
            return;
        }

        moveSomeStuff();
    }

    /**
     * Checks if the entity is in range to render by using the past in distance
     * and comparing it to its average edge. length * 64 * renderDistanceWeight
     * Args: distance.
     *
     * @param range the real range.
     * @return true or false.
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(final double range)
    {
        double maxLength = this.getBoundingBox().getAverageEdgeLength() * NUM_BOUNDING_BOX_EDGES;
        maxLength *= DISTANCE_FACTOR;
        return range < maxLength * maxLength;
    }

    /**
     * If a hook gets loaded, kill it immediately.
     */
    @Override
    public void readAdditional(final CompoundNBT unused)
    {
        this.remove();
    }

    /**
     * No need to write anything to NBT.
     * A hook does not need to be saved.
     */
    @Override
    public void writeAdditional(final CompoundNBT unused)
    {
        //We don't save this
    }

    /**
     * Sets the velocity to the args.
     *
     * @param vectorX directionX
     * @param vectorY directionY
     * @param vectorZ directionZ
     */
    @Override
    @OnlyIn(Dist.CLIENT.CLIENT)
    public void setVelocity(final double vectorX, final double vectorY, final double vectorZ)
    {
        this.setMotion(vectorX, vectorY, vectorZ);
    }

    /**
     * Check if a fishhook is there for too long.
     * and became bugged.
     * After 360 seconds remove the hook.
     *
     * @return true if the time to live is over
     */
    public boolean fishHookIsOverTimeToLive()
    {
        return MathUtils.nanoSecondsToSeconds(System.nanoTime() - creationTime) > TTL;
    }

    /**
     * Update some movement things for the hook.
     * Detect if the hook is on ground and maybe bounce.
     * Also count how long the hook is laying on the ground or in water.
     */
    private void bounceFromGround()
    {
        if (this.shake > 0)
        {
            --this.shake;
        }

        if (!this.inGround)
        {
            return;
        }

        this.inGround = false;
        this.setMotion(this.getMotion().getX() * (this.rand.nextDouble() * BOUNCE_MOVEMENT_LIMITER),
          this.getMotion().getY() * (this.rand.nextDouble() * BOUNCE_MOVEMENT_LIMITER),
        this.getMotion().getZ() * (this.rand.nextDouble() * BOUNCE_MOVEMENT_LIMITER));
    }

    /**
     * Main update method thingy.
     * hopefully I'm able to reduce it somewhat...
     */
    private void moveSomeStuff()
    {
        updateMotionAndRotation();
        double movementLimiter = AIR_MOVEMENT_LIMITER;

        if (this.onGround || this.collidedHorizontally)
        {
            movementLimiter = GROUND_MOVEMENT_LIMITER;
        }

        final byte numSteps = 5;
        double waterDensity = 0.0;

        //Check how much water is around the hook
        for (int j = 0; j < numSteps; ++j)
        {
            final double d3 = this.getBoundingBox().minY + (this.getBoundingBox().maxY - this.getBoundingBox().minY) * j / numSteps;
            final double d4 = this.getBoundingBox().minY + (this.getBoundingBox().maxY - this.getBoundingBox().minY) * (j + 1) / numSteps;

            @NotNull final AxisAlignedBB axisAlignedBB1 = new AxisAlignedBB(
              this.getBoundingBox().minX,
              d3,
              this.getBoundingBox().minZ,
              this.getBoundingBox().maxX,
              d4,
              this.getBoundingBox().maxZ);

            //If the hook is swimming
            if (CompatibilityUtils.getWorldFromEntity(this).isMaterialInBB(axisAlignedBB1, Material.WATER))
            {
                waterDensity += 1.0 / numSteps;
            }
        }

        checkIfFishBites(waterDensity);

        final double currentDistance = waterDensity * 2.0D - 1.0;
        this.setMotion(this.getMotion().getX(), this.getMotion().getY() + WATER_MOVEMENT_LIMITER * currentDistance, this.getMotion().getZ());

        if (waterDensity > 0.0)
        {
            movementLimiter *= 0.9;
            this.setMotion(this.getMotion().getX(), this.getMotion().getY() * 0.8, this.getMotion().getZ());
        }

        this.setMotion(this.getMotion().getX() * movementLimiter,
          this.getMotion().getY() * movementLimiter,
          this.getMotion().getZ() * movementLimiter);
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    /**
     * Update the fishing hooks motion.
     * and its rotation.
     */
    private void updateMotionAndRotation()
    {
        this.move(MoverType.SELF, this.getMotion());
        final double motion = Math.sqrt(this.getMotion().getX() * this.getMotion().getX() + this.getMotion().getZ() * this.getMotion().getZ());
        this.rotationYaw = (float) (Math.atan2(this.getMotion().getY(), this.getMotion().getZ()) * HALF_CIRCLE / Math.PI);
        this.rotationPitch = (float) (Math.atan2(this.getMotion().getY(), motion) * HALF_CIRCLE / Math.PI);
        while ((double) this.rotationPitch - (double) this.prevRotationPitch < -HALF_CIRCLE)
        {
            this.prevRotationPitch -= 360.0;
        }

        while ((double) this.rotationPitch - (double) this.prevRotationPitch >= HALF_CIRCLE)
        {
            this.prevRotationPitch += 360.0;
        }

        while ((double) this.rotationYaw - (double) this.prevRotationYaw < -HALF_CIRCLE)
        {
            this.prevRotationYaw -= 360.0;
        }

        while ((double) this.rotationYaw - (double) this.prevRotationYaw >= HALF_CIRCLE)
        {
            this.prevRotationYaw += 360.0;
        }

        this.rotationPitch = (float) ((double) this.prevRotationPitch + ((double) this.rotationPitch - (double) this.prevRotationPitch) * 0.2D);
        this.rotationYaw = (float) ((double) this.prevRotationYaw + ((double) this.rotationYaw - (double) this.prevRotationYaw) * 0.2D);
    }

    /**
     * Server side method to do.
     * some animation and movement stuff.
     * when the hook swims in water.
     * <p>
     * will set isFishCaught if a fish bites.
     *
     * @param waterDensity the amount of water around.
     */
    private void checkIfFishBites(final double waterDensity)
    {
        if (!CompatibilityUtils.getWorldFromEntity(this).isRemote && waterDensity > 0.0)
        {
            int fishingProgressStep = 1;

            if (this.rand.nextDouble() < NO_CLEAR_SKY_CHANCE
                  && !CompatibilityUtils.getWorldFromEntity(this)
                        .canBlockSeeSky(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY) + 1, MathHelper.floor(this.posZ))))
            {
                --fishingProgressStep;
            }

            if (this.countdownNoFish > 0)
            {
                updateNoFishCounter();
                return;
            }

            @NotNull final ServerWorld worldServer = (ServerWorld) CompatibilityUtils.getWorldFromEntity(this);

            if (this.countdownFishNear > 0)
            {
                renderBubble(fishingProgressStep, worldServer);
                return;
            }

            if (this.countdownFishBites > 0)
            {
                this.countdownFishBites -= fishingProgressStep;

                renderFishBiteOrSwim(worldServer);
            }
            else
            {
                this.countdownFishNear = MathHelper.getInt(this.rand, 100, 900);
                this.countdownFishNear -= fishingSpeedEnchantment * 20 * 5;
            }
        }
    }

    private void updateNoFishCounter()
    {
        --this.countdownNoFish;

        if (this.countdownNoFish <= 0)
        {
            this.countdownFishNear = 0;
            this.countdownFishBites = 0;
        }
        else
        {
            this.motionY -= (this.rand.nextDouble() * this.rand.nextDouble() * this.rand.nextDouble()) * BOUNCE_MOVEMENT_LIMITER;
        }
    }

    private void renderBubble(final int fishingProgressStep, @NotNull final ServerWorld worldServer)
    {
        this.countdownFishNear -= fishingProgressStep;

        double bubbleY = 0.15;

        if (this.countdownFishNear < 20)
        {
            bubbleY = bubbleY + (double) (20 - this.countdownFishNear) * 0.05;
        }
        else if (this.countdownFishNear < 40)
        {
            bubbleY = bubbleY + (double) (40 - this.countdownFishNear) * 0.02;
        }
        else if (this.countdownFishNear < 60)
        {
            bubbleY = bubbleY + (double) (60 - this.countdownFishNear) * 0.01;
        }

        if (this.rand.nextDouble() < bubbleY)
        {
            renderLittleSplash(worldServer);
        }

        if (this.countdownFishNear <= 0)
        {
            this.relativeRotation = MathHelper.nextFloat(this.rand, 0.0F, 360.0F);
            this.countdownFishBites = MathHelper.getInt(this.rand, 20, 80);
        }
    }

    private void renderFishBiteOrSwim(@NotNull final ServerWorld worldServer)
    {
        if (this.countdownFishBites <= 0)
        {
            //Show fish bite animation
            showFishBiteAnimation(worldServer);
        }
        else
        {
            //Show fish swim animation
            showFishSwimmingTowardsHookAnimation(worldServer);
        }
    }

    /**
     * Render little splashes around the fishing hook.
     * simulating fish movement.
     *
     * @param worldServer the server side world.
     */
    private void renderLittleSplash(@NotNull final ServerWorld worldServer)
    {
        final double sinYPosition = (double) MathHelper.nextFloat(this.rand, 0.0F, 360.0F) * 0.017453292D;
        final double cosYPosition = MathHelper.nextFloat(this.rand, 25.0F, 60.0F);
        final double bubbleX = this.posX + (Math.sin(sinYPosition) * cosYPosition * 0.1);
        final double increasedYPosition = Math.floor(this.getBoundingBox().minY) + 1.0;
        final double bubbleZ = this.posZ + (Math.cos(sinYPosition) * cosYPosition * 0.1);
        worldServer.spawnParticle(ParticleTypes.SPLASH,
          bubbleX,
          increasedYPosition,
          bubbleZ,
          2 + this.rand.nextInt(2),
          SUNKEN_OFFSET,
          0.0,
          SUNKEN_OFFSET,
          0.0);
    }

    /**
     * Show bubbles towards the hook.
     * Let the hook sink in a bit.
     * Play a sound to signal to the player,
     * that a fish bit.
     *
     * @param worldServer the server side world.
     */
    private void showFishBiteAnimation(@NotNull final ServerWorld worldServer)
    {
        this.motionY -= 0.20000000298023224D;
        this.playSound(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, ENTITY_SIZE, (float) (1.0D + this.rand.nextGaussian() * 0.4D));
        final double bubbleY = Math.floor(this.getBoundingBox().minY);
        worldServer.spawnParticle(ParticleTypes.BUBBLE,
          this.posX,
          bubbleY + 1.0,
          this.posZ,
          (int) (1.0 + this.width * 20.0),
          (double) this.width,
          0.0,
          (double) this.width,
          0.20000000298023224);
        worldServer.spawnParticle(ParticleTypes.FALLING_WATER,
          this.posX,
          bubbleY + 1.0,
          this.posZ,
          (int) (1.0 + this.width * 20.0),
          (double) this.width,
          0.0,
          (double) this.width,
          0.20000000298023224);
        this.countdownNoFish = MathHelper.getInt(this.rand, 10, 30);
        isFishCaugth = true;
    }

    /**
     * Show bubbles moving towards the hook.
     * make it look like a fish will bite soon.
     *
     * @param worldServer the server side world.
     */
    private void showFishSwimmingTowardsHookAnimation(@NotNull final ServerWorld worldServer)
    {
        this.relativeRotation = this.relativeRotation + this.rand.nextGaussian() * NUM_BOUNDING_BOX_EDGES;
        final double bubbleY = this.relativeRotation * 0.017453292;
        final double sinYPosition = Math.sin(bubbleY);
        final double cosYPosition = Math.cos(bubbleY);
        final double bubbleX = this.posX + (sinYPosition * this.countdownFishBites * 0.1);
        final double increasedYPosition = Math.floor(this.getBoundingBox().minY) + 1.0;
        final double bubbleZ = this.posZ + (cosYPosition * this.countdownFishBites * 0.1);

        if (this.rand.nextDouble() < 0.15)
        {
            worldServer.spawnParticle(WATER_BUBBLE, bubbleX, increasedYPosition - SUNKEN_OFFSET, bubbleZ, 1, sinYPosition, 0.1D, cosYPosition, 0.0);
        }

        final double f3 = sinYPosition * 0.04;
        final double f4 = cosYPosition * 0.04;
        worldServer.spawnParticle(WATER_WAKE, bubbleX, increasedYPosition, bubbleZ, 0, f4, 0.01, -f3, 1.0);
        worldServer.spawnParticle(WATER_WAKE, bubbleX, increasedYPosition, bubbleZ, 0, -f4, 0.01, f3, 1.0);
    }

    /**
     * Returns a damage value by how much the fishingRod should be damaged.
     * Also spawns loot and exp and destroys the hook.
     *
     * @param citizen the fisherman fishing.
     * @return the number of damage points to be deducted.
     */
    public int getDamage(@NotNull final AbstractEntityCitizen citizen)
    {
        if (CompatibilityUtils.getWorldFromEntity(this).isRemote)
        {
            this.setDead();
            return 0;
        }
        byte itemDamage = 0;
        if (isFishCaugth)
        {
            if (this.countdownNoFish > 0)
            {
                spawnLootAndExp(citizen);
                itemDamage = 1;
            }

            if (this.inGround)
            {
                itemDamage = 0;
            }
        }
        this.setDead();
        return itemDamage;
    }

    /**
     * Spawns a random loot from the loot table.
     * and some exp orbs.
     * Should be called when retrieving a hook.
     *
     * @param citizen the fisherman getting the loot.
     */
    private void spawnLootAndExp(@NotNull final AbstractEntityCitizen citizen)
    {
        final double citizenPosX = citizen.getPosX();
        final double citizenPosY = citizen.getPosY();
        final double citizenPosZ = citizen.getPosZ();
        @NotNull final ItemEntity ItemEntity = new ItemEntity(CompatibilityUtils.getWorldFromEntity(this), this.posX, this.posY, this.posZ, this.getFishingLoot(citizen));
        final double distanceX = citizenPosX - this.posX;
        final double distanceY = citizenPosY - this.posY;
        final double distanceZ = citizenPosZ - this.posZ;

        ItemEntity.motionX = distanceX * 0.1;
        ItemEntity.motionY = distanceY * 0.1 + Math.sqrt(Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ)) * 0.08;
        ItemEntity.motionZ = distanceZ * 0.1;
        CompatibilityUtils.getWorldFromEntity(this).addEntity(ItemEntity);
        CompatibilityUtils.getWorldFromCitizen(citizen).addEntity(new ExperienceOrbEntity(CompatibilityUtils.getWorldFromCitizen(citizen),
          citizenPosX,
          citizenPosY + 0.D,
          citizenPosZ + 0.5,
          this.rand.nextInt(6) + 1));
    }

    /**
     * Determines which loot table should be used.
     * <p>
     * The selection is somewhat random and depends on enchantments.
     * and the level of the fisherman hut.
     *
     * @param citizen the fisherman getting the loot.
     * @return an ItemStack randomly from the loot table.
     */
    private ItemStack getFishingLoot(final AbstractEntityCitizen citizen)
    {
        //Reduce random to get more fish drops
        final int random = CompatibilityUtils.getWorldFromEntity(this).rand.nextInt(INCREASE_RARENESS_MODIFIER);
        final int buildingLevel = citizen.getCitizenColonyHandler().getWorkBuilding().getBuildingLevel();
        //Cut to minimum value of 0.
        final int lootBonus = MathHelper.clamp(fishingLootEnchantment - fishingSpeedEnchantment, 0, Integer.MAX_VALUE);

        if (random >= buildingLevel * (lootBonus + 1) || buildingLevel == 1)
        {
            if (random >= INCREASE_RARENESS_MODIFIER - buildingLevel * (lootBonus + 1) && buildingLevel >= 2)
            {
                return getLootForLootTable(LootTableList.GAMEPLAY_FISHING_JUNK);
            }

            return getLootForLootTable(LootTableList.GAMEPLAY_FISHING_FISH);
        }
        else
        {
            return getLootForLootTable(LootTableList.GAMEPLAY_FISHING_TREASURE);
        }
    }

    /**
     * Return some random loot of a defined lootTable.
     *
     * @param lootTable the lootTable.
     * @return the ItemStack of the loot.
     */
    private ItemStack getLootForLootTable(final ResourceLocation lootTable)
    {
        final LootContext.Builder lootContextBuilder = new LootContext.Builder((ServerWorld) CompatibilityUtils.getWorldFromEntity(this));
        return CompatibilityUtils.getWorldFromEntity(this).getLootTableManager()
                 .getLootTableFromLocation(lootTable)
                 .generateLootForPools(this.rand, lootContextBuilder.build()).stream().findFirst().orElse(null);
    }

    /**
     * returns true if a fish was caught.
     *
     * @return true | false
     */
    public boolean caughtFish()
    {
        return isFishCaugth;
    }
}
