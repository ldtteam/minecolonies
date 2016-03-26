package com.minecolonies.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import java.util.List;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomFishable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityFishHook extends Entity
{
    private static final int ttl=720;
    private static final List possibleDrops_1 = Arrays.asList((new WeightedRandomFishable(new ItemStack(Items.leather_boots), 10)).func_150709_a(0.9F), new WeightedRandomFishable(new ItemStack(Items.leather), 10), new WeightedRandomFishable(new ItemStack(Items.bone), 10), new WeightedRandomFishable(new ItemStack(Items.potionitem), 10), new WeightedRandomFishable(new ItemStack(Items.string), 5), (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 2)).func_150709_a(0.9F), new WeightedRandomFishable(new ItemStack(Items.bowl), 10), new WeightedRandomFishable(new ItemStack(Items.stick), 5), new WeightedRandomFishable(new ItemStack(Items.dye, 10, 0), 1), new WeightedRandomFishable(new ItemStack(Blocks.tripwire_hook), 10), new WeightedRandomFishable(new ItemStack(Items.rotten_flesh), 10));
    private static final List possibleDrops_2 = Arrays.asList(new WeightedRandomFishable(new ItemStack(Blocks.waterlily), 1), new WeightedRandomFishable(new ItemStack(Items.name_tag), 1), new WeightedRandomFishable(new ItemStack(Items.saddle), 1), (new WeightedRandomFishable(new ItemStack(Items.bow), 1)).func_150709_a(0.25F).func_150707_a(), (new WeightedRandomFishable(new ItemStack(Items.fishing_rod), 1)).func_150709_a(0.25F).func_150707_a(), (new WeightedRandomFishable(new ItemStack(Items.book), 1)).func_150707_a());
    private static final List possibleDrops_3 = Arrays.asList(new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.COD.func_150976_a()), 60), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.SALMON.func_150976_a()), 25), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.CLOWNFISH.func_150976_a()), 2), new WeightedRandomFishable(new ItemStack(Items.fish, 1, ItemFishFood.FishType.PUFFERFISH.func_150976_a()), 13));
    private int field_146037_g;
    private int field_146048_h;
    private int field_146050_i;
    private Block field_146046_j;
    private boolean field_146051_au;
    private int field_146044_a;
    public EntityCitizen citizen;
    private int field_146049_av;
    private int field_146047_aw;
    private int field_146045_ax;
    private int field_146040_ay;
    private int field_146038_az;
    private double field_146054_aA;
    private Entity field_146043_c;
    private int field_146055_aB;
    private double field_146056_aC;
    private double field_146057_aD;
    private double field_146058_aE;
    private double field_146059_aF;
    private double field_146060_aG;
    @SideOnly(Side.CLIENT)
    private double field_146061_aH;
    @SideOnly(Side.CLIENT)
    private double field_146052_aI;
    @SideOnly(Side.CLIENT)
    private double field_146053_aJ;
    //Time at which the entity has been created
    private long creationTime;

    public EntityFishHook(World p_i1764_1_)
    {
        super(p_i1764_1_);
        this.field_146037_g = -1;
        this.field_146048_h = -1;
        this.field_146050_i = -1;
        this.setSize(0.25F, 0.25F);
        this.ignoreFrustumCheck = true;
        this.creationTime = System.nanoTime();
    }

    @SideOnly(Side.CLIENT)
    public EntityFishHook(World p_i1765_1_, double p_i1765_2_, double p_i1765_4_, double p_i1765_6_, EntityCitizen citizen)
    {
        this(p_i1765_1_);
        this.setPosition(p_i1765_2_, p_i1765_4_, p_i1765_6_);
        this.ignoreFrustumCheck = true;
        this.citizen = citizen;
        citizen.setFishEntity(this);
        this.creationTime = System.nanoTime();
    }

    public EntityFishHook(World p_i1766_1_, EntityCitizen p_i1766_2_)
    {
        super(p_i1766_1_);
        this.field_146037_g = -1;
        this.field_146048_h = -1;
        this.field_146050_i = -1;
        this.ignoreFrustumCheck = true;
        this.citizen = p_i1766_2_;
        this.citizen.setFishEntity(this);
        this.setSize(0.25F, 0.25F);
        this.setLocationAndAngles(p_i1766_2_.posX, p_i1766_2_.posY + 1.62 - (double)p_i1766_2_.yOffset, p_i1766_2_.posZ, p_i1766_2_.rotationYaw, p_i1766_2_.rotationPitch);
        this.posX -= Math.cos(this.rotationYaw / 180.0 * Math.PI) * 0.16;
        this.posY -= 0.10000000149011612;
        this.posZ -= Math.sin(this.rotationYaw / 180.0 * Math.PI) * 0.16;
        this.setPosition(this.posX, this.posY, this.posZ);
        this.yOffset = 0.0F;
        double f = 0.4;
        this.motionX = -Math.sin(this.rotationYaw / 180.0 * Math.PI) * Math.cos(this.rotationPitch / 180.0 * Math.PI) * f;
        this.motionZ = Math.cos(this.rotationYaw / 180.0 * Math.PI) * Math.cos(this.rotationPitch / 180.0 * Math.PI) * f;
        this.motionY = -Math.sin(this.rotationPitch / 180.0 * Math.PI) * f;
        this.func_146035_c(this.motionX, this.motionY, this.motionZ, 1.5, 1.0);
        this.creationTime = System.nanoTime();
    }

    protected void entityInit() {}

    private void func_146035_c(double p_146035_1_, double p_146035_3_, double p_146035_5_, double p_146035_7_, double p_146035_8_)
    {
        double f2 = MathHelper.sqrt_double(p_146035_1_ * p_146035_1_ + p_146035_3_ * p_146035_3_ + p_146035_5_ * p_146035_5_);
        p_146035_1_ /= f2;
        p_146035_3_ /= f2;
        p_146035_5_ /= f2;
        p_146035_1_ += this.rand.nextGaussian() * 0.007499999832361937 * p_146035_8_;
        p_146035_3_ += this.rand.nextGaussian() * 0.007499999832361937 * p_146035_8_;
        p_146035_5_ += this.rand.nextGaussian() * 0.007499999832361937 * p_146035_8_;
        p_146035_1_ *= p_146035_7_;
        p_146035_3_ *= p_146035_7_;
        p_146035_5_ *= p_146035_7_;
        this.motionX = p_146035_1_;
        this.motionY = p_146035_3_;
        this.motionZ = p_146035_5_;
        double f3 = Math.sqrt(p_146035_1_ * p_146035_1_ + p_146035_5_ * p_146035_5_);
        this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(p_146035_1_, p_146035_5_) * 180.0 / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(p_146035_3_, f3) * 180.0 / Math.PI);
        this.field_146049_av = 0;
    }

    //Returns time to life of the entity
    public int getTtl()
    {
        return ttl;
    }
    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight Args: distance
     */
    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double p_70112_1_)
    {
        double d1 = this.boundingBox.getAverageEdgeLength() * 4.0;
        d1 *= 64.0;
        return p_70112_1_ < d1 * d1;
    }

    /**
     * Sets the position and rotation. Only difference from the other one is no bounding on the rotation. Args: posX,
     * posY, posZ, yaw, pitch
     */
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double p_70056_1_, double p_70056_3_, double p_70056_5_, double p_70056_7_, double p_70056_8_, int p_70056_9_)
    {
        this.field_146056_aC = p_70056_1_;
        this.field_146057_aD = p_70056_3_;
        this.field_146058_aE = p_70056_5_;
        this.field_146059_aF = p_70056_7_;
        this.field_146060_aG = p_70056_8_;
        this.field_146055_aB = p_70056_9_;
        this.motionX = this.field_146061_aH;
        this.motionY = this.field_146052_aI;
        this.motionZ = this.field_146053_aJ;
    }

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double p_70016_1_, double p_70016_3_, double p_70016_5_)
    {
        this.field_146061_aH = this.motionX = p_70016_1_;
        this.field_146052_aI = this.motionY = p_70016_3_;
        this.field_146053_aJ = this.motionZ = p_70016_5_;
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate()
    {
        //On update if fish is pulling down calculate a "skill factor" from the fisherman (may include intelligence and speed)
        //So with a certain possibility the fisher will catch or miss.
        super.onUpdate();

        if (this.field_146055_aB > 0)
        {
            double d7 = this.posX + (this.field_146056_aC - this.posX) / (double)this.field_146055_aB;
            double d8 = this.posY + (this.field_146057_aD - this.posY) / (double)this.field_146055_aB;
            double d9 = this.posZ + (this.field_146058_aE - this.posZ) / (double)this.field_146055_aB;
            double d1 = MathHelper.wrapAngleTo180_double(this.field_146059_aF - (double)this.rotationYaw);
            this.rotationYaw = (float)((double)this.rotationYaw + d1 / (double)this.field_146055_aB);
            this.rotationPitch = (float)((double)this.rotationPitch + (this.field_146060_aG - (double)this.rotationPitch) / (double)this.field_146055_aB);
            --this.field_146055_aB;
            this.setPosition(d7, d8, d9);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }
        else
        {
            if (!this.worldObj.isRemote)
            {
                if(this.citizen == null)
                {
                    return;
                }

                ItemStack itemstack = this.citizen.getHeldItem();

                if (this.citizen.isDead || !this.citizen.isEntityAlive() || itemstack == null || !itemstack.getItem().equals(Items.fishing_rod) || this.getDistanceSqToEntity(this.citizen) > 1024.0D)
                {
                    this.setDead();
                    this.citizen.setFishEntity(null);
                    return;
                }

                if (this.field_146043_c != null)
                {
                    if (!this.field_146043_c.isDead)
                    {
                        this.posX = this.field_146043_c.posX;
                        this.posY = this.field_146043_c.boundingBox.minY + (double)this.field_146043_c.height * 0.8D;
                        this.posZ = this.field_146043_c.posZ;
                        return;
                    }

                    this.field_146043_c = null;
                }
            }

            if (this.field_146044_a > 0)
            {
                --this.field_146044_a;
            }

            if (this.field_146051_au)
            {
                if (this.worldObj.getBlock(this.field_146037_g, this.field_146048_h, this.field_146050_i) == this.field_146046_j)
                {
                    ++this.field_146049_av;

                    if (this.field_146049_av == 1200)
                    {
                        this.setDead();
                    }

                    return;
                }

                this.field_146051_au = false;
                this.motionX *= (this.rand.nextDouble() * 0.2);
                this.motionY *= (this.rand.nextDouble() * 0.2);
                this.motionZ *= (this.rand.nextDouble() * 0.2);
                this.field_146049_av = 0;
                this.field_146047_aw = 0;
            }
            else
            {
                ++this.field_146047_aw;
            }

            Vec3 vec31 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            Vec3 vec3 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
            MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec31, vec3);
            vec31 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
            vec3 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

            if (movingobjectposition != null)
            {
                vec3 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
            }

            Entity entity = null;
            List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0, 1.0, 1.0));
            double d0 = 0.0;
            double d2;

            for (Object aList : list)
            {
                Entity entity1 = (Entity) aList;

                if (entity1.canBeCollidedWith() && (entity1 != this.citizen || this.field_146047_aw >= 5))
                {
                    double f = 0.3;
                    AxisAlignedBB axisAlignedBB = entity1.boundingBox.expand(f, f, f);
                    MovingObjectPosition movingObjectPosition1 = axisAlignedBB.calculateIntercept(vec31, vec3);

                    if (movingObjectPosition1 != null)
                    {
                        d2 = vec31.distanceTo(movingObjectPosition1.hitVec);

                        if (d2 < d0 || Math.abs(d0) <= 0.001)

                        {
                            entity = entity1;
                            d0 = d2;
                        }
                    }
                }
            }

            if (entity != null)
            {
                movingobjectposition = new MovingObjectPosition(entity);
            }

            if (movingobjectposition != null)
            {
                if (movingobjectposition.entityHit != null)
                {
                    if (movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.citizen), 0.0F))
                    {
                        this.field_146043_c = movingobjectposition.entityHit;
                    }
                }
                else
                {
                    this.field_146051_au = true;
                }
            }

            if (!this.field_146051_au)
            {
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                double f5 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
                //MOTIONY really?
                this.rotationYaw = (float)(Math.atan2(this.motionY, this.motionZ) * 180.0 / Math.PI);

                this.rotationPitch = (float)(Math.atan2(this.motionY, f5) * 180.0 / Math.PI);
                while ((double)this.rotationPitch - (double)this.prevRotationPitch < -180.0)
                {
                    this.prevRotationPitch -= 360.0;
                }

                while ((double)this.rotationPitch - (double)this.prevRotationPitch >= 180.0)
                {
                    this.prevRotationPitch += 360.0;
                }

                while ((double)this.rotationYaw - (double)this.prevRotationYaw < -180.0)
                {
                    this.prevRotationYaw -= 360.0;
                }

                while ((double)this.rotationYaw - (double)this.prevRotationYaw >= 180.0)
                {
                    this.prevRotationYaw += 360.0;
                }

                this.rotationPitch =
                        (float) ((double)this.prevRotationPitch + ((double)this.rotationPitch - (double)this.prevRotationPitch) * 0.2D);
                this.rotationYaw =
                        (float) ((double)this.prevRotationYaw + ((double)this.rotationYaw - (double)this.prevRotationYaw) * 0.2D);
                double f6 = 0.92F;

                if (this.onGround || this.isCollidedHorizontally)
                {
                    f6 = 0.5;
                }

                byte b0 = 5;
                double d10 = 0.0;

                for (int j = 0; j < b0; ++j)
                {
                    double d3 = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * j / b0 - 0.125 + 0.125;
                    double d4 = this.boundingBox.minY + (this.boundingBox.maxY - this.boundingBox.minY) * (j + 1) / b0 - 0.125 + 0.125;
                    AxisAlignedBB axisAlignedBB1 = AxisAlignedBB.getBoundingBox(this.boundingBox.minX, d3, this.boundingBox.minZ, this.boundingBox.maxX, d4, this.boundingBox.maxZ);

                    if (this.worldObj.isAABBInMaterial(axisAlignedBB1, Material.water))
                    {
                        d10 += 1.0 / b0;
                    }
                }

                if (!this.worldObj.isRemote && d10 > 0.0)
                {
                    WorldServer worldserver = (WorldServer)this.worldObj;
                    int k = 1;

                    if (this.rand.nextDouble() < 0.25 && this.worldObj.canLightningStrikeAt(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY) + 1, MathHelper.floor_double(this.posZ)))
                    {
                        k = 2;
                    }

                    if (this.rand.nextDouble() < 0.5 && !this.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY) + 1, MathHelper.floor_double(this.posZ)))
                    {
                        --k;
                    }

                    if (this.field_146045_ax > 0)
                    {
                        --this.field_146045_ax;

                        if (this.field_146045_ax <= 0)
                        {
                            this.field_146040_ay = 0;
                            this.field_146038_az = 0;
                        }
                    }
                    else
                    {
                        double f1;
                        double f2;
                        double d5;
                        double d6;
                        double f7;
                        double d11;

                        if (this.field_146038_az > 0)
                        {
                            this.field_146038_az -= k;

                            if (this.field_146038_az <= 0)
                            {
                                this.motionY -= 0.20000000298023224D;
                                this.playSound("random.splash", 0.25F,
                                               (float) (1.0D + (this.rand.nextDouble() - this.rand.nextDouble()) * 0.4D));
                                f1 = Math.floor(this.boundingBox.minY);
                                worldserver.func_147487_a("bubble", this.posX, (f1 + 1.0), this.posZ, (int)(1.0 + this.width * 20.0), (double)this.width, 0.0, (double)this.width, 0.20000000298023224);
                                worldserver.func_147487_a("wake", this.posX, (f1 + 1.0), this.posZ, (int)(1.0 + this.width * 20.0), (double)this.width, 0.0, (double)this.width, 0.20000000298023224);
                                this.field_146045_ax = MathHelper.getRandomIntegerInRange(this.rand, 10, 30);
                                System.out.println("Fish bites here");
                                citizen.setCaughtFish(true);
                            }
                            else
                            {
                                this.field_146054_aA = this.field_146054_aA + this.rand.nextGaussian() * 4.0;
                                f1 = this.field_146054_aA * 0.017453292;
                                f7 = Math.sin(f1);
                                f2 = Math.cos(f1);
                                d11 = this.posX + (f7 * this.field_146038_az * 0.1);
                                d5 = Math.floor(this.boundingBox.minY) + 1.0;
                                d6 = this.posZ + (f2 * this.field_146038_az * 0.1);

                                if (this.rand.nextDouble() < 0.15)
                                {
                                    worldserver.func_147487_a("bubble", d11, d5 - 0.10000000149011612, d6, 1, f7, 0.1D, f2, 0.0);
                                }

                                double f3 = f7 * 0.04;
                                double f4 = f2 * 0.04;
                                worldserver.func_147487_a("wake", d11, d5, d6, 0, f4, 0.01, (-f3), 1.0);
                                worldserver.func_147487_a("wake", d11, d5, d6, 0, (-f4), 0.01, f3, 1.0);

                            }
                        }
                        else if (this.field_146040_ay > 0)
                        {
                            this.field_146040_ay -= k;
                            f1 = 0.15;

                            if (this.field_146040_ay < 20)
                            {
                                f1 = f1 + (double)(20 - this.field_146040_ay) * 0.05;
                            }
                            else if (this.field_146040_ay < 40)
                            {
                                f1 = f1 + (double)(40 - this.field_146040_ay) * 0.02;
                            }
                            else if (this.field_146040_ay < 60)
                            {
                                f1 = f1 + (double)(60 - this.field_146040_ay) * 0.01;
                            }

                            if (this.rand.nextDouble() < f1)
                            {
                                f7 = (double)MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F) * 0.017453292D;
                                f2 = MathHelper.randomFloatClamp(this.rand, 25.0F, 60.0F);
                                d11 = this.posX + (Math.sin(f7) * f2 * 0.1);
                                d5 = Math.floor(this.boundingBox.minY) + 1.0;
                                d6 = this.posZ + (Math.cos(f7) * f2 * 0.1);
                                worldserver.func_147487_a("splash", d11, d5, d6, 2 + this.rand.nextInt(2), 0.10000000149011612, 0.0, 0.10000000149011612, 0.0);

                            }

                            if (this.field_146040_ay <= 0)
                            {
                                this.field_146054_aA = MathHelper.randomFloatClamp(this.rand, 0.0F, 360.0F);
                                this.field_146038_az = MathHelper.getRandomIntegerInRange(this.rand, 20, 80);
                            }
                        }
                        else
                        {
                            this.field_146040_ay = MathHelper.getRandomIntegerInRange(this.rand, 100, 900);
                            this.field_146040_ay -= EnchantmentHelper.func_151387_h(this.citizen) * 20 * 5;
                        }
                    }

                    if (this.field_146045_ax > 0)
                    {
                        this.motionY -= (this.rand.nextDouble() * this.rand.nextDouble() * this.rand.nextDouble()) * 0.2;
                    }
                }

                d2 = d10 * 2.0D - 1.0;
                this.motionY += 0.03999999910593033 * d2;

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
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound p_70014_1_)
    {
        p_70014_1_.setShort("xTile", (short)this.field_146037_g);
        p_70014_1_.setShort("yTile", (short)this.field_146048_h);
        p_70014_1_.setShort("zTile", (short)this.field_146050_i);
        p_70014_1_.setByte("inTile", (byte)Block.getIdFromBlock(this.field_146046_j));
        p_70014_1_.setByte("shake", (byte)this.field_146044_a);
        p_70014_1_.setByte("inGround", (byte)(this.field_146051_au ? 1 : 0));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound p_70037_1_)
    {
        if(!citizen.getFishEntity().equals(this))
        {
            this.setDead();
        }

        this.field_146037_g = p_70037_1_.getShort("xTile");
        this.field_146048_h = p_70037_1_.getShort("yTile");
        this.field_146050_i = p_70037_1_.getShort("zTile");
        this.field_146046_j = Block.getBlockById(p_70037_1_.getByte("inTile") & 255);
        this.field_146044_a = p_70037_1_.getByte("shake") & 255;
        this.field_146051_au = p_70037_1_.getByte("inGround") == 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    public int func_146034_e()
    {
        if (this.worldObj.isRemote)
        {
            return 0;
        }
        else
        {
            byte b0 = 0;

            if (this.field_146043_c != null)
            {
                double d0 = this.citizen.posX - this.posX;
                double d2 = this.citizen.posY - this.posY;
                double d4 = this.citizen.posZ - this.posZ;
                double d6 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2 + d4 * d4);
                double d8 = 0.1;
                this.field_146043_c.motionX += d0 * d8;
                this.field_146043_c.motionY += d2 * d8 + (double)MathHelper.sqrt_double(d6) * 0.08;
                this.field_146043_c.motionZ += d4 * d8;
                b0 = 3;
            }
            else if (this.field_146045_ax > 0)
            {
                EntityItem entityitem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, this.func_146033_f());
                double d1 = this.citizen.posX - this.posX;
                double d3 = this.citizen.posY - this.posY;
                double d5 = this.citizen.posZ - this.posZ;
                double d7 = (double)MathHelper.sqrt_double(d1 * d1 + d3 * d3 + d5 * d5);
                double d9 = 0.1;
                entityitem.motionX = d1 * d9;
                entityitem.motionY = d3 * d9 + (double)MathHelper.sqrt_double(d7) * 0.08;
                entityitem.motionZ = d5 * d9;
                this.worldObj.spawnEntityInWorld(entityitem);
                this.citizen.worldObj.spawnEntityInWorld(new EntityXPOrb(this.citizen.worldObj, this.citizen.posX, this.citizen.posY + 0.D, this.citizen.posZ + 0.5, this.rand.nextInt(6) + 1));
                b0 = 1;
            }

            if (this.field_146051_au)
            {
                b0 = 2;
            }

            this.setDead();
            this.citizen.setFishEntity(null);
            return b0;
        }
    }

    private ItemStack func_146033_f()
    {
        double f = this.worldObj.rand.nextDouble();
        int i = EnchantmentHelper.func_151386_g(this.citizen);
        int j = EnchantmentHelper.func_151387_h(this.citizen);
        double f1 = 0.1 - i * 0.025 - j * 0.01;
        double f2 = 0.05 + i * 0.01 - j * 0.01;
        f1 = MathHelper.clamp_float((float)f1, 0.0F, 1.0F);
        f2 = MathHelper.clamp_float((float)f2, 0.0F, 1.0F);

        if (f < f1)
        {
            return ((WeightedRandomFishable)WeightedRandom.getRandomItem(this.rand, possibleDrops_1)).func_150708_a(this.rand);
        }
        else
        {
            f -= f1;

            if (f < f2)
            {
                return ((WeightedRandomFishable)WeightedRandom.getRandomItem(this.rand, possibleDrops_2)).func_150708_a(this.rand);
            }
            else
            {
                return ((WeightedRandomFishable)WeightedRandom.getRandomItem(this.rand, possibleDrops_3)).func_150708_a(this.rand);
            }
        }
    }

    /**
     * Will get destroyed next tick.
     */
    @Override
    public void setDead()
    {
        super.setDead();

        if (this.citizen != null)
        {
            this.citizen.setFishEntity(null);
        }
    }

    public long getCreationTime()
    {
        return creationTime;
    }
}