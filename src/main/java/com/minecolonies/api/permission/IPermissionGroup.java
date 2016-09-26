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
    void addKey(String key);

    boolean hasKey(IPermissionKey key);
    boolean hasKey(String key);

    void removeKey(IPermissionKey key);
    void removeKey(String key);
}
