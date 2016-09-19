package com.minecolonies.api.permission;

/**
 * @author Isfirs
 * @since 0.3
 */
public interface IPermissionGroup
{

    String serialize();

    void deserialize(String data);

    void addKey(IPermissionKey key);

    boolean hasKey(IPermissionKey key);

    void removeKey(IPermissionKey key);
}
