package com.minecolonies.core.colony.workorders.view;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.RotationMirror;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.ColonyUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.entity.ai.workers.util.BuildingProgressStage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * The WorkOrderView is the client-side representation of a WorkOrders. Views contain the WorkOrder's data that is relevant to a Client, in a more client-friendly form Mutable
 * operations on a View result in a message to the server to perform the operation
 */
public abstract class AbstractWorkOrderView implements IWorkOrderView
{
    /**
     * The work order its id.
     */
    private int id;

    /**
     * The priority.
     */
    private int priority;

    /**
     * Claimed by building id pos.
     */
    private BlockPos claimedBy;

    /**
     * Its description.
     */
    private String packName;

    /**
     * The name of the work order.
     */
    private String structurePath;

    /**
     * The type (defined by an enum).
     */
    private WorkOrderType workOrderType;

    /**
     * Position where its being built at.
     */
    private BlockPos location;

    /**
     * Position where its being built at.
     */
    private int rotation;

    /**
     * Position where its being built at.
     */
    private boolean isMirrored;

    /**
     * The level it's at before the upgrade.
     */
    private int currentLevel;

    /**
     * Level it's being upgraded to.
     */
    private int targetLevel;

    /**
     * The building stage this workorder is in
     */
    private BuildingProgressStage stage = null;

    /**
     * Translation key.
     */
    private String translationKey;

    /**
     * The workorder area
     */
    protected AABB box = Constants.EMPTY_AABB;

    /**
     * The blueprint of this workorders schematic
     */
    protected Blueprint blueprint = null;

    /**
     * The related colony
     */
    protected IColony colony = null;

    /**
     * The cached blueprint future
     */
    protected CompletableFuture<Blueprint> future = null;

    public AbstractWorkOrderView()
    {
    }

    @Override
    public int getID()
    {
        return id;
    }

    @Override
    public void setID(final int id)
    {

    }

    @Override
    public int getPriority()
    {
        return priority;
    }

    @Override
    public void setPriority(final int priority)
    {
        this.priority = priority;
    }

    public BlockPos getClaimedBy()
    {
        return claimedBy;
    }

    public void setClaimedBy(final BlockPos position)
    {
        this.claimedBy = position;
    }

    @Override
    public void setBlueprint(final Blueprint blueprint, final Level world)
    {
        if (blueprint != null)
        {
            this.blueprint = blueprint;
            blueprint.setRotationMirror(RotationMirror.of(BlockPosUtil.getRotationFromRotations(rotation), isMirrored ? Mirror.FRONT_BACK : Mirror.NONE), world);
        }
    }

    @Override
    public Blueprint getBlueprint()
    {
        return blueprint;
    }

    @Override
    public void clearBlueprint()
    {
        blueprint = null;
        future = null;
    }

    @Override
    public AABB getBoundingBox()
    {
        return box;
    }

    @Override
    public IColony getColony()
    {
        return colony;
    }

    @Override
    public void setColony(final IColony colony)
    {
        this.colony = colony;
    }

    @Override
    public BuildingProgressStage getStage()
    {
        return stage;
    }

    /**
     * Value getter.
     *
     * @return the value String.
     */
    @Override
    public String getStructurePack()
    {
        return packName.replaceAll("schematics/(?:decorations/)?", "");
    }

    @Override
    public void loadBlueprint(final Level world, final Consumer<Blueprint> afterLoad)
    {
        if (blueprint != null)
        {
            afterLoad.accept(blueprint);
        }
        else if (future == null || future.isDone())
        {
            future = ColonyUtils.queueBlueprintLoad(world, getStructurePack(), getStructurePath(), blueprint ->
                {
                    setBlueprint(blueprint, world);
                    afterLoad.accept(blueprint);
                },
                e -> afterLoad.accept(null));
        }
        else
        {
            afterLoad.accept(null);
        }
    }

    @Override
    public String getStructurePath()
    {
        return structurePath;
    }

    @Override
    public final String getTranslationKey()
    {
        return translationKey;
    }

    public WorkOrderType getWorkOrderType()
    {
        return workOrderType;
    }

    @Override
    public BlockPos getLocation()
    {
        return this.location;
    }

    @Override
    public int getRotation()
    {
        return rotation;
    }

    @Override
    public boolean isMirrored()
    {
        return isMirrored;
    }

    @Override
    public boolean isClaimed()
    {
        return !BlockPos.ZERO.equals(claimedBy);
    }

    @Override
    public int getCurrentLevel()
    {
        return currentLevel;
    }

    @Override
    public int getTargetLevel()
    {
        return targetLevel;
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        id = buf.readInt();
        priority = buf.readInt();
        claimedBy = buf.readBlockPos();
        packName = buf.readUtf(32767);
        structurePath = buf.readUtf(32767);
        translationKey = buf.readUtf(32767);
        workOrderType = WorkOrderType.values()[buf.readInt()];
        location = buf.readBlockPos();
        rotation = buf.readInt();
        isMirrored = buf.readBoolean();
        currentLevel = buf.readInt();
        targetLevel = buf.readInt();
        stage = BuildingProgressStage.values()[buf.readInt()];
        box = new AABB(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    @Override
    public boolean canBuildIgnoringDistance(@NotNull final BlockPos builderLocation, final int builderLevel)
    {
        //  A Build WorkOrder may be fulfilled by a Builder as long as any ONE of the following is true:
        //  - The Builder's Work AbstractBuilding is built
        //  - OR the WorkOrder is for the Builder's Work AbstractBuilding

        return (builderLevel >= targetLevel || builderLevel == 5 || (builderLocation.equals(location)));
    }
}
