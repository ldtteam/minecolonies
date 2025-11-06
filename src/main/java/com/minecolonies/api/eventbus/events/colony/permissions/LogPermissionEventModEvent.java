package com.minecolonies.api.eventbus.events.colony.permissions;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.PermissionEvent;
import com.minecolonies.api.eventbus.events.colony.AbstractColonyModEvent;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Logging permission events mod event.
 */
public final class LogPermissionEventModEvent extends AbstractColonyModEvent
{
    /**
     * The player that is entering the colony.
     */
    @Nullable
    private final Entity entity;

    /**
     * The permission event instance.
     */
    private final PermissionEvent permissionEvent;

    /**
     * The list of conditions under which to stop logging, OR match.
     */
    private final List<Predicate<Entity>> avoidLoggingConditions;

    /**
     * Disables the built-in operator condition.
     */
    private boolean disablePermissionLevelCheck = false;

    /**
     * Constructs a colony-based event.
     *
     * @param colony          the colony related to the event.
     * @param entity          the player that is entering the colony.
     * @param permissionEvent the permission event instance.
     */
    public LogPermissionEventModEvent(final @NotNull IColony colony, final @Nullable Entity entity, final PermissionEvent permissionEvent)
    {
        super(colony);
        this.entity = entity;
        this.permissionEvent = permissionEvent;
        this.avoidLoggingConditions = new ArrayList<>(List.of(this::checkPermissionLevel));
    }

    /**
     * Internal check for validating the permission level, can be disabled by calling {@link LogPermissionEventModEvent#disablePermissionLevelCheck()}.
     *
     * @param entity the entity to check for.
     * @return true if the condition passes.
     */
    private boolean checkPermissionLevel(final @Nullable Entity entity)
    {
        return disablePermissionLevelCheck || entity != null && !entity.hasPermissions(IMinecoloniesAPI.getInstance()
            .getConfig()
            .getServer().permissionEventLoggingMinBypassPermLevel.get());
    }

    /**
     * Get the entity that caused the permission event. May be null, in case of things like explosions which are not caused by entities.
     */
    @Nullable
    public Entity getEntity()
    {
        return entity;
    }

    /**
     * Get the permission event instance.
     *
     * @return the permission event instance.
     */
    public PermissionEvent getPermissionEvent()
    {
        return permissionEvent;
    }

    /**
     * Whether we should show the notification for the player entering.
     *
     * @return true if so.
     */
    public boolean shouldLogPermissionEvent()
    {
        return avoidLoggingConditions.stream().anyMatch(condition -> condition.test(entity));
    }

    /**
     * Add a condition under which to avoid logging the given permission event.
     *
     * @param condition the condition check.
     */
    public void addAvoidanceCondition(final Predicate<Entity> condition)
    {
        this.avoidLoggingConditions.add(condition);
    }

    /**
     * Disable persisting the logging under the default operator condition.
     * This allows you to customize logic yourself.
     */
    public void disablePermissionLevelCheck()
    {
        disablePermissionLevelCheck = true;
    }

    @FunctionalInterface
    public interface AvoidanceCondition
    {
        void test(final @Nullable Entity entity);
    }
}
