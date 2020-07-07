# Enabling checkstyle support in eclipse.

Note that the SonarQube checkstyle config is too old to be used in checkstyle 8.0 or newer.

Use the following update link to install checkstyle 7.3.0:

https://sourceforge.net/projects/eclipse-cs/files/updatesite/7.3.0/

Navigate to *Windows* -> *Preference* -> *Checkstyle* -> *New*

Enter the following information:
```
Type: Project Relative Configuration
Name: Minecolonies
Location: /minecolonies/config/checkstyle/checkstyle.xml
```

Navigate to *Project* -> *Properties* -> *Checkstyle*

Select the Minecolonies configuration.
