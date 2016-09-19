package com.minecolonies.permissions;

import com.minecolonies.MineColonies;
import com.minecolonies.api.permission.IPermissionGroup;
import com.minecolonies.api.permission.IPermissionKey;
import com.minecolonies.api.permission.IPermissionManager;

import java.util.*;

/**
 */
public class PermissionManager implements IPermissionManager
{

    private static final String TEMPLATE_BLOCK = "/^(!{0,1})block.(place|break).([a-zA-Z]{1,}|\\*):([a-zA-Z,*]{1,})$/";

    private static final PermissionManager instance = new PermissionManager();

    public static PermissionManager getInstance()
    {
        return instance;
    }

    /*
     * ==========================================================
     */


    /**
     * The mod instance
     */
    private final MineColonies mod;

    /**
     * The list of loaded keys.
     */
    private final List<IPermissionKey> permissionKeys;

    /**
     * List of loaded groups.
     */
    private final List<IPermissionGroup> permissionGroups;

    /**
     * Map of UUIDs to groups.
     */
    private final Map<UUID, List<IPermissionGroup>> uuidGroups;

    /**
     * Map of UUIDs to keys.
     */
    private final Map<UUID, List<IPermissionKey>> uuidKeys;

    /**
     * Singleton.
     *
     * The constructor registers the events
     */
    private PermissionManager()
    {
        this.mod = MineColonies.instance;

        this.permissionKeys = new ArrayList<>();
        this.permissionGroups = new ArrayList<>();
        this.uuidGroups = new HashMap<>();
        this.uuidKeys = new HashMap<>();
    }

    public void load()
    {
        this.loadKeys();
        this.loadGroups();
        this.loadUserdata();
    }

    public void save()
    {
        this.saveUserdata();
        this.saveGroups();
        this.saveKeys();
    }

    private void loadKeys()
    {
        // TODO
    }

    private void loadGroups()
    {
        // TODO
    }

    private void loadUserdata()
    {
        // TODO
    }

    private void saveKeys()
    {
        // TODO
    }

    private void saveGroups()
    {
        // TODO
    }

    private void saveUserdata()
    {
        // TODO
    }

    private static void setup()
    {

    }

}
