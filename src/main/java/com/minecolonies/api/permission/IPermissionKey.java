package com.minecolonies.api.permission;

/**
 * @author Isfirs
 * @since 0.3
 */
public interface IPermissionKey
{

    String serialize();

    void deserialize();

    boolean compare(IPermissionKey permKey);
    
    public String getRegex();
    
}
