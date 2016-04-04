package com.minecolonies.entity;

import com.minecolonies.entity.ai.EntityAIWorkFisherman;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Arrays;
import java.util.List;

//Creates a custom fishHook for the Fisherman to throw
public final class EntityFishHook extends Entity
{
    private static final int    TTL             = 360;
    private static final List   possibleDrops_1 = Arrays.asList((new WeightedRandomFishable(new ItemStack(Items.leather_boots), 10)).func_150709_a(0.9F),
                                                                new WeightedRandomFishable(new ItemStack(Items.leather), 10),
                                                                new WeightedRandomFishable(new ItemStack(Items.bone), 10),
                                                                new WeightedRandomFishable(new ItemStack(Items.potionitem), 10),
                                                                new WeightedRandomFishable(new ItemStack(Items.string), 5),
                                                                (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 2)).func_150709_a(0.9F),
                                                                new WeightedRandomFishable(new ItemStack(Items.bowl), 10),
                                                                new WeightedRandomFishable(new ItemStack(Items.stick), 5),
                                                                new WeightedRandomFishable(new ItemStack(Items.dye, 10, 0), 1),
                                                                new WeightedRandomFishable(new ItemStack(Blocks.tripwire_hook), 10),
                                                                new WeightedRandomFishable(new ItemStack(Items.rotten_flesh), 10));
    private static final List   possibleDrops_2 = Arrays.asList(new WeightedRandomFishable(new ItemStack(Blocks.waterlily), 1),
                                                                new WeightedRandomFishable(new ItemStack(Items.name_tag), 1),
                                                                new WeightedRandomFishable(new ItemStack(Items.saddle), 1),
                                                                (new WeightedRandomFishable(new ItemStack(Items.bow), 1)).func_150709_a(0.25F).func_150707_a(),
                                                                (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 1)).func_150709_a(0.25F).func_150707_a(),
                                                                (new WeightedRandomFishable(new ItemStack(Items.book), 1)).func_150707_a());
    private static final List   possibleDrops_3 = Arrays.asList(new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.COD.func_150976_a()), 60),
                                                                new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.SALMON.func_150976_a()), 25),
                                                                new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.CLOWNFISH.func_150976_a()), 2),
                                                                new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.PUFFERFISH.func_150976_a()), 13));
    public  EntityAIWorkFisherman fisherman;
    private boolean               inGround;
    private int                   shake;
    private int                   movedOnX;
    private int                   movedOnY;
    private int                   movedOnZ;
    private double                relativeRotation;
    private int                   newPosRotationIncrements;
    private double                newX;
    private double                newY;
    private double                newZ;
    private double                newRotationYaw;
    private double                newRotationPitch;
    @SideOnly(Side.CLIENT)
    private double                hookVectorX;
    @SideOnly(Side.CLIENT)
    private double                hookVectorY;
    @SideOnly(Side.CLIENT)
    private double                hookVectorZ;
    //Time at which the entity has been created
    private long                  creationTime;
    //Will be set true when the citizen caught a fish (to reset the fisherman)
    private boolean isCaughtFish = false;

    @SideOnly(Side.CLIENT)
    public EntityFishHook(World world, double x, double y, double z, EntityAIWorkFisherman fisherman)
    {
        this(world);
        this.setPosition(x, y, z);
        this.ignoreFrustumCheck = true;
        this.fisherman = fisherman;
        fisherman.setEntityFishHook(this);
        this.creationTime = System.nanoTime();
    }

    public EntityFishHook(World world)
    {
        super(world);
        this.setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck = true;
        this.creationTime = System.nanoTime();
    }

    public EntityFishHook(World world, EntityAIWorkFisherman fisherman)
    {
        super(world);
        this.ignoreFrustumCheck = true;
        this.fisherman = fisherman;
        this.fisherman.setEntityFishHook(this);
        this.setSize(0.25F, 0.25F);
        this.setLocationAndAngles(fisherman.getCitizen().posX,
                                  fisherman.getCitizen().posY + 1.62 - (double) fisherman.getCitizen().yOffset,
                                  fisherman.getCitizen().posZ,
                                  fisherman.getCitizen().rotationYaw,
                                  fisherman.getCitizen().rotationPitch);
        this.posX -= Math.cos(this.rotationYaw / 180.0 * Math.PI) * 0.16;
        this.posY -= 0.10000000149011612;
        this.posZ -= Math.sin(this.rotationYaw / 180.0 * Math.PI) * 0.16;
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        double f = 0.4;
        this.motionX = -Math.sin(this.rotationYaw / 180.0 * Math.PI) * Math.cos(this.rotationPitch / 180.0 * Math.PI) * f;
        this.motionZ = Math.cos(this.rotationYaw / 180.0 * Math.PI) * Math.cos(this.rotationPitch / 180.0 * Math.PI) * f;
        this.motionY = -Math.sin(this.rotationPitch / 180.0 * Math.PI) * f;
        this.setPosition(this.motionX, this.motionY, this.motionZ, 1.5, 1.0);
        this.creationTime = System.nanoTime();
    }

    private void setPosition(double x, double y, double z, double yaw, double pitch)
    {
        double squareRootXYZ = MathHelper.sqrt_double(x * x + y * y + z * z);
        double newX          = x / squareRootXYZ;
        double newY          = y / squareRootXYZ;
        double newZ          = z / squareRootXYZ;
        newX += this.rand.nextGaussian() * 0.007499999832361937 * pitch;
        newY += this.rand.nextGaussian() * 0.007499999832361937 * pitch;
        newZ += this.rand.nextGaussian() * 0.007499999832361937 * pitch;
        newX *= yaw;
        newY *= yaw;
        newZ *= yaw;
        this.motionX = newX;
        this.motionY = newY;
        this.motionZ = newZ;
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(newX, newZ) * 180.0 / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(newY, Math.sqrt(newX * newX + newZ * newZ)) * 180.0 / Math.PI);
    }

    /**
     * Minecraft may call this method
     */
    @Override
    protected void entityInit(){}

    //Returns time to life of the entity
    public int getTtl()
    {
        return TTL;
    }

    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight Args: distance
     *
     * @param range the real range
     * @return true or false
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double range)
    {
        double maxLength = this.boundingBox.getAverageEdgeLength() * 4.0;
        maxLength *= 64.0;
        return range < maxLength * maxLength;
    }

    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation.
     *
     * @param x                  posX
     * @param y                  posY
     * @param z                  posZ
     * @param yaw                The rotation yaw
     * @param pitch              The rotation pitch
     * @param rotationIncrements rotation increments
     */
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double x, double y, double z, double yaw, double pitch, int rotationIncrements)
    {
        this.newX = x;
        this.newY = y;
        this.newZ = z;
        this.newRotationYaw = yaw;
        this.newRotationPitch = pitch;
        this.newPosRotationIncrements = rotationIncrements;
        this.motionX = this.hookVectorX;
        this.motionY = this.hookVectorY;
        this.motionZ = this.hookVectorZ;
    }

    /**
     * Sets the velocity to the args.
     *
     * @param vectorX directionX
     * @param vectorY directionY
     * @param vectorZ directionZ
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double vectorX, double vectorY, double vectorZ)
    {
        this.hookVectorX = this.motionX = vectorX;
        this.hookVectorY = this.motionY = vectorY;
        this.hookVectorZ = this.motionZ = vectorZ;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        super.onUpdate();
        if (preconditionsFail())
        {
            return;
        }

        moveSomeStuff();
    }

    private boolean preconditionsFail()
    {
        if (hasToRotateIncrementally())
        {
            return true;
        }
        if (hasToUpdateServerSide())
        {
            return true;
        }
        if (isInGround())
        {
            return true;
        }
        if (this.inGround)
        {
            return true;
        }
        return false;
    }

    /**
     * Update some movement things for the hook.
     * Detect if the hook is on ground and maybe bounce.
     * Also count how long the hook is laying on the ground or in water.
     *
     * @return true if the hook is killed.
     */
    private boolean isInGround()
    {
        if (this.shake > 0)
        {
            --this.shake;
        }

        if (!this.inGround)
        {
            return false;
        }

        this.inGround = false;
        this.motionX *= (this.rand.nextDouble() * 0.2);
        this.motionY *= (this.rand.nextDouble() * 0.2);
        this.motionZ *= (this.rand.nextDouble() * 0.2);

        return false;
    }

    /**
     * Remove hook if the fisher became invalid.
     *
     * @return true if fisher became invalid
     */
    private boolean hasToUpdateServerSide()
    {
        if (!this.worldObj.isRemote && this.fisherman == null)
        {
            this.setDead();
            return true;
        }
        return false;
    }

    /**
     * Will get destroyed next tick.
     */
    @Override
    public void setDead()
    {
        super.setDead();

        if (this.fisherman != null)
        {
            this.fisherman.setEntityFishHook(null);
        }
    }

    private boolean hasToRotateIncrementally()
    {
        if (this.newPosRotationIncrements > 0)
        {
            double x           = this.posX + (this.newX - this.posX) / (double) this.newPosRotationIncrements;
            double y           = this.posY + (this.newY - this.posY) / (double) this.newPosRotationIncrements;
            double z           = this.posZ + (this.newZ - this.posZ) / (double) this.newPosRotationIncrements;
            double newRotation = MathHelper.wrapAngleTo180_double(this.newRotationYaw - (double) this.rotationYaw);
            this.rotationYaw = (float) ((double) this.rotationYaw + newRotation / (double) this.newPosRotationIncrements);
            this.rotationPitch = (float) ((double) this.rotationPitch + (this.newRotationPitch - (double) this.rotationPitch) / (double) this.newPosRotationIncrements);
            --this.newPosRotationIncrements;
            this.setPosition(x, y, z);
            this.setRotation(this.rotationYaw, this.rotationPitch);
            return true;
        }
        return false;
    }

    private void moveSomeStuff()
    {
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        double motion = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float) (Math.atan2(this.motionY, this.motionZ) * 180.0 / Math.PI);
        this.rotationPitch = (float) (Math.atan2(this.motionY, motion) * 180.0 / Math.PI);
        while ((double) this.rotationPitch - (double) this.prevRotationPitch < -180.0)
        {
            this.prevRotationPitch -= 360.0;
        }

        while ((double) this.rotationPitch - (double) this.prevRotationPitch >= 180.0)
        {
            this.prevRotationPitch += 360.0;
        }

        while ((double) this.rotationYaw - (double) this.prevRotationYaw < -180.0)
        {
            this.prevRotationYaw -= 360.0;
        }

        while ((double) this.rotationYaw - (double) this.prevRotationYaw >= 180.0)
        {
            this.prevRotationYaw += 360.0;
        }

        this.rotationPitch =
                (float) ((double) this.prevRotationPitch + ((double) this.rotationPitch - (double) this.prevRotationPitch) * 0.2D);
        this.rotationYaw =
                (float) ((double) this.prevRotationYaw + ((double) this.rotationYaw - (double) this.prevRotationYaw) * 0.2D);
        double f6 = 0.92F;

        if (this.onGround || this.isCollidedHorizontally)
        {
            f6 = 0.5;
        }

        byte   b0  = 5;
        double d10 = 0.0;

        for (int j = 0; j < b0; ++j)
        {
            double        d3             = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * j / b0 - 0.125 + 0.125;
            double        d4             = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * (j + 1) / b0 - 0.125 + 0.125;
            AxisAlignedBB axisAlignedBB1 = AxisAlignedBB.getBoundingBox(this.boundingBox.minX, d3, this.boundingBox.minZ, this.boundingBox.maxX, d4, this.boundingBox.maxZ);

            if (this.worldObj.isAABBInMaterial(axisAlignedBB1, Material.water))
            {
                d10 += 1.0 / b0;
            }
        }

        if (!this.worldObj.isRemote && d10 > 0.0)
        {
            WorldServer worldserver = (WorldServer) this.worldObj;
            int         k           = 1;

            if (this.rand.nextDouble() < 0.25 && this.worldObj.canLightningStrikeAt(MathHelper.floor_double(this.posX),
                                                                                    MathHelper.floor_double(this.posY) + 1,
                                                                                    MathHelper.floor_double(this.posZ)))
            {
                k = 2;
            }

            if (this.rand.nextDouble() < 0.5 && !this.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.posX),
                                                                                 MathHelper.floor_double(this.posY) + 1,
                                                                                 MathHelper.floor_double(this.posZ)))
            {
                --k;
            }

            if (this.movedOnX > 0)
            {
                --this.movedOnX;

                if (this.movedOnX <= 0)
                {
                    this.movedOnY = 0;
                    this.movedOnZ = 0;
                }
            }
            else
            {
                double bubbleY;
                double bubbleZ;
                double bubbleX;

                double cosYPosition;
                double increasedYPosition;
                double sinYPosition;

                if (this.movedOnZ > 0)
                {
                    this.movedOnZ -= k;

                    if (this.movedOnZ <= 0)
                    {
                        this.motionY -= 0.20000000298023224D;
                        this.playSound("random.splash", 0.25F,
                                       (float) (1.0D + (this.rand.nextDouble() - this.rand.nextDouble()) * 0.4D));
                        bubbleY = Math.floor(this.boundingBox.minY);
                        worldserver.func_147487_a("bubble",
                                                  this.posX,
                                                  (bubbleY + 1.0),
                                                  this.posZ,
                                                  (int) (1.0 + this.width * 20.0),
                                                  (double) this.width,
                                                  0.0,
                                                  (double) this.width,
                                                  0.20000000298023224);
                        worldserver.func_147487_a("wake",
                                                  this.posX,
                                                  (bubbleY + 1.0),
                                                  this.posZ,
                                                  (int) (1.0 + this.width * 20.0),
                                                  (double) this.width,
                                                  0.0,
                                                  (double) this.width,
                                                  0.20000000298023224);
                        this.movedOnX = MathHelper.getRandomIntegerInRange(this.rand, 10, 30);
                        isCaughtFish = true;
                    }
                    else
                    {
                        this.relativeRotation = this.relativeRotation + this.rand.nextGaussian() * 4.0;
                        bubbleY = this.relativeRotation * 0.017453292;
                        sinYPosition = Math.sin(bubbleY);
                        cosYPosition = Math.cos(bubbleY);
                        bubbleX = this.posX + (sinYPosition * this.movedOnZ * 0.1);
                        increasedYPosition = Math.floor(this.boundingBox.minY) + 1.0;
                        bubbleZ = this.posZ + (cosYPosition * this.movedOnZ * 0.1);

                        if (this.rand.nextDouble() < 0.15)
                        {
                            worldserver.func_147487_a("bubble", bubbleX, increasedYPosition - 0.10000000149011612, bubbleZ, 1, sinYPosition, 0.1D, cosYPosition, 0.0);
                        }

                        double f3 = sinYPosition * 0.04;
                        double f4 = cosYPosition * 0.04;
                        worldserver.func_147487_a("wake", bubbleX, increasedYPosition, bubbleZ, 0, f4, 0.01, (-f3), 1.0);
                        worldserver.func_147487_a("wake", bubbleX, increasedYPosition, bubbleZ, 0, (-f4), 0.01, f3, 1.0);

                    }
                }
                else if (this.movedOnY > 0)
                {
                    this.movedOnY -= k;
                    bubbleY = 0.15;

                    if (this.movedOnY < 20)
                    {
                        bubbleY = bubbleY + (double) (20 - this.movedOnY) * 0.05;
                    }
                    else if (this.movedOnY < 40)
                    {
                        bubbleY = bubbleY + (double) (40 - this.movedOnY) * 0.02;
                    }
                    else if (this.movedOnY < 60)
                    {
                        bubbleY = bubbleY + (double) (60 - this.movedOnY) * 0.01;
                    }

                    if (this.rand.nextDouble() < bubbleY)
                    {
                        sinYPosition = (double) MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F) * 0.017453292D;
                        cosYPosition = MathHelper.randomFloatClamp(this.rand, 25.0F, 60.0F);
                        bubbleX = this.posX + (Math.sin(sinYPosition) * cosYPosition * 0.1);
                        increasedYPosition = Math.floor(this.boundingBox.minY) + 1.0;
                        bubbleZ = this.posZ + (Math.cos(sinYPosition) * cosYPosition * 0.1);
                        worldserver.func_147487_a("splash",
                                                  bubbleX,
                                                  increasedYPosition,
                                                  bubbleZ,
                                                  2 + this.rand.nextInt(2),
                                                  0.10000000149011612,
                                                  0.0,
                                                  0.10000000149011612,
                                                  0.0);

                    }

                    if (this.movedOnY <= 0)
                    {
                        this.relativeRotation = MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F);
                        this.movedOnZ = MathHelper.getRandomIntegerInRange(this.rand, 20, 80);
                    }
                }
                else
                {
                    this.movedOnY = MathHelper.getRandomIntegerInRange(this.rand, 100, 900);
                    this.movedOnY -= EnchantmentHelper.func_151387_h(this.fisherman.getCitizen()) * 20 * 5;
                }
            }

            if (this.movedOnX > 0)
            {
                this.motionY -= (this.rand.nextDouble() * this.rand.nextDouble() * this.rand.nextDouble()) * 0.2;
            }
        }

        double currentDistance = d10 * 2.0D - 1.0;
        this.motionY += 0.03999999910593033 * currentDistance;

        if (d10 > 0.0)
        {
            f6 = f6 * 0.9;
            this.motionY *= 0.8;
        }

        this.motionX *= f6;
        this.motionY *= f6;
        this.motionZ *= f6;
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    /**
     * No need to write anything to NBT.
     * A hook does not need to be saved.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound unused)
    {
    }

    /**
     * If a hook gets loaded, kill it immediately.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound unused)
    {
        this.setDead();
    }

    @Override
    public boolean equals(Object o)
    {
        return !(o == null || getClass() != o.getClass() || !super.equals(o));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    /**
     * Returns a damage value by how much the fishingRod should be damaged.
     * Also spawns loot and exp and destroys the hook.
     * @return the numer of damage points to be deducted.
     */
    public int getDamage()
    {
        if (this.worldObj.isRemote)
        {
            this.setDead();
            this.fisherman.setEntityFishHook(null);
            return 0;
        }
        byte itemDamage = 0;

        if (this.movedOnX > 0)
        {
            spawnLootAndExp();
            itemDamage = 1;
        }

        if (this.inGround)
        {
            itemDamage = 0;
        }

        this.setDead();
        this.fisherman.setEntityFishHook(null);
        return itemDamage;

    }

    /**
     * Spawns a random loot from the loottable
     * and some exp orbs.
     * Should be calles when retrieving a hook.
     * todo: Perhaps streamline this and directly add the items?
     */
    private void spawnLootAndExp()
    {
        double     citizenPosX = this.fisherman.getCitizen().posX;
        double     citizenPosY = this.fisherman.getCitizen().posY;
        double     citizenPosZ = this.fisherman.getCitizen().posZ;
        EntityItem entityitem  = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, this.getFishingLoot());
        double     distanceX   = citizenPosX - this.posX;
        double     distanceY   = citizenPosY - this.posY;
        double     distanceZ   = citizenPosZ - this.posZ;

        entityitem.motionX = distanceX * 0.1;
        entityitem.motionY = distanceY * 0.1 + Math.sqrt(Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ)) * 0.08;
        entityitem.motionZ = distanceZ * 0.1;
        this.worldObj.spawnEntityInWorld(entityitem);
        this.fisherman.getCitizen().worldObj.spawnEntityInWorld(new EntityXPOrb(this.fisherman.getCitizen().worldObj,
                                                                                citizenPosX,
                                                                                citizenPosY + 0.D,
                                                                                citizenPosZ + 0.5,
                                                                                this.rand.nextInt(6) + 1));
    }

    /**
     * Determines which loot table should be used.
     *
     * The selection is somewhat random and depends on enchantments
     * and the level of the fisherman hut.
     *
     * @return an ItemStack randomly from the loot table
     */
    private ItemStack getFishingLoot()
    {
        double random                  = this.worldObj.rand.nextDouble();
        int    fishingSpeedEnchantment = EnchantmentHelper.func_151386_g(this.fisherman.getCitizen());
        int    fishingLootEnchantment  = EnchantmentHelper.func_151387_h(this.fisherman.getCitizen());
        double speedBonus              = 0.1 - fishingSpeedEnchantment * 0.025 - fishingLootEnchantment * 0.01;
        double lootBonus               = 0.05 + fishingSpeedEnchantment * 0.01 - fishingLootEnchantment * 0.01;
        //clamp_float gives the values an upper limit
        speedBonus = MathHelper.clamp_float((float) speedBonus, 0.0F, 1.0F);
        lootBonus = MathHelper.clamp_float((float) lootBonus, 0.0F, 1.0F);
        int buildingLevel = fisherman.getCitizen().getWorkBuilding().getBuildingLevel();

        if (random < speedBonus || buildingLevel == 1)
        {
            return ((WeightedRandomFishable) WeightedRandom.getRandomItem(this.rand, possibleDrops_3)).func_150708_a(this.rand);
        }
        else
        {
            random -= speedBonus;

            if (random < lootBonus || buildingLevel == 2)
            {
                return ((WeightedRandomFishable) WeightedRandom.getRandomItem(this.rand, possibleDrops_1)).func_150708_a(this.rand);
            }
            else
            {
                return ((WeightedRandomFishable) WeightedRandom.getRandomItem(this.rand, possibleDrops_2)).func_150708_a(this.rand);
            }
        }
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (fisherman != null ? fisherman.hashCode() : 0);
        return result;
    }

    public long getCreationTime()
    {
        return creationTime;
    }

    public boolean caughtFish()
    {
        return isCaughtFish;
    }

    public void setCaughtFish(boolean caughtFish)
    {
        isCaughtFish = caughtFish;
    }
}
