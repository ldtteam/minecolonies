package com.minecolonies.api.permission;

/**
 * The interface of the permission manager.
 *
 * Its tasks are the relationship management between:
 * <ul>
 *     <li>UUID to IPermissionKey</li>
 *     <li>UUID to IPermissionGroup</li>
 *     <li>IPermissionGroup to IPermissionKey</li>
 * </ul>
 *
 * It:
 * <ul>
 *     <li>Stores</li>
 *     <li>load</li>
 *     <li>alters</li>
 * </ul>
 * permission data.
 *
 * @author Isfirs
 * @since 0.3
 */
public interface IPermissionManager
{

    /**
     * This methods loads stored data such as:
     * <ul>
     *     <li>UUID to PermissionKey relation</li>
     *     <li>UUID to PermissionGroup relation</li>
     *     <li>PermissionGroup to PermissionKey relation</li>
     * </ul>
     *
     */
    void load();

    /**
     * Perists the data of this instance.
     *
     * @see IPermissionManager#load()
     */
    void save();


}
