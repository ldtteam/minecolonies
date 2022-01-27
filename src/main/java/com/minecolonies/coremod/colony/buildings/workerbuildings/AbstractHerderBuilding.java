package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

public abstract class AbstractHerderBuilding extends AbstractBuilding {
    /**
     * Constructor for a AbstractBuilding.
     *
     * @param colony Colony the building belongs to.
     * @param pos    Location of the building (it's Hut Block).
     */
    private BlockPos animalsPen;

    public static final ISettingKey<BoolSetting> BRING_ANIMALS = new SettingKey<>(BoolSetting.class, new ResourceLocation(MOD_ID, "bring_animals"));

    protected AbstractHerderBuilding(@NotNull IColony colony, BlockPos pos) {
        super(colony, pos);
    }

    @Override
    public void onUpgradeComplete(int newLevel) {
        super.onUpgradeComplete(newLevel);
        animalsPen = configAnimalsPen();
    }

    private BlockPos configAnimalsPen() {
        for (Iterator<BlockPos> iter = BlockPos.betweenClosedStream(getCorners().getA(), getCorners().getB()).iterator(); iter.hasNext();) {
            BlockPos pos = iter.next();
            if (getTileEntity().getLevel().getBlockState(pos).getBlock() instanceof  FenceGateBlock) {
                return checkFence(pos);
            }
        }
        return null;
    }

    private BlockPos checkFence(BlockPos fence) {
        World world = getTileEntity().getLevel();
        Direction towards = world.getBlockState(fence).getValue(BlockStateProperties.HORIZONTAL_FACING);
        return fence.relative(towards, 2);
    }

    public BlockPos getAnimalsPen() {
        return animalsPen;
    }

    @Override
    public void deserializeNBT(CompoundNBT compound) {
        super.deserializeNBT(compound);
        this.animalsPen = BlockPosUtil.read(compound, "pen");
    }

    @Override
    public CompoundNBT serializeNBT() {

        return animalsPen != null ? BlockPosUtil.write(super.serializeNBT(), "pen", animalsPen) : super.serializeNBT();
    }
}
