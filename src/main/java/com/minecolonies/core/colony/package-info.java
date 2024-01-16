/*
 # About ColonyManager
 The Colony Manager system (as a whole) is a server- and client- side system that provides permanent residency for data
 structures and systems to manage colonies, removing dependence on Minecraft world chunks or entities being loaded.

 Colony Manager uses a hybrid type of 'proxy' Model/View pattern combined with a subscriber model, where Colony-related
 objects that exist on the server side and are mirrored as 'View' objects to interested clients.  View objects provide
 read-only access to data about the mirrored object, and methods that would mutate the object instead initiate a request
 to the server.

 Colonies have an integer ID, and contain all data for a colony.  They are mirrored to the client as ColonyView objects.

 Buildings in a colony are represented via Building subclass objects, and mirrored to clients as matching Building.View
 subclass objects.  Buildings are linked to a matching BlockHut-derived block in the world, with the ChunkCoordinate of
 the block as the ID of the building.

 Colonies track their citizens via CitizenData, which link to matching CitizenEntity entities when those entities are
 loaded.  They are mirrored to clients as CitizenData.View.  CitizenData have integer IDs, and on the client have the
 entityId of the associated entity, when it is loaded.

 Colonies maintain a set of Permissions, which are the relationship of individual players to a colony, and
 (possibly eventually) the privileges available to each relationship type.

 Tasks to be performed in a colony are centralized as WorkOrders within the WorkManager of a colony.  This allows
 tracking of tasks that involve multiple components in a central location.  For example, when a building is to be built
 or upgraded, a WorkOrderBuild is added to the WorkManager.

 Jobs are a method of linking a CitizenData to a Building, and also provides persistent storage for a given citizen in
 a given role.  Jobs also determine the render model ID for a citizen.

 Data for a citizen / worker / building / colony should be exist in the locations as follows:
 * AITask = Data necessary to perform the AI Tasks / work, but that does not need to be persistent
 * CitizenData = Data specific to a citizen that is independent of their job and building
 * Job = Data specific to a citizen's job (worker), that needs to be saved or persist between chunk unload/reload
 * Building = Data specific to a building, but not the workers in that building
 * Colony = Data relevant to the colony as a whole

 ## Classes

 ### ColonyManager
 The ColonyManager class is a singleton, implemented via only static members and methods.  It is usable from both server and client.

 On the server, the ColonyManager maintains ownership of all colonies, and provides lookup access to them by ID.

 On the client, the ColonyManager maintains the known ColonyViews and provides lookup access by ID.

 ### Colony and ColonyView classes
 Colony is the server-side representation of a colony, and maintains all data for a given a colony.

 A Colony is mirrored to clients as a ColonyView.  Both have the IColony interface as a common ancestor.

 ### Building subclasses, and Building.View subclasses
 Building classes are the server-side representation of buildings in a colony, maintaining state for a given building.

 A Building is mirrored to clients as a .View static child class of the given building; e.g, a BuildingMiner is mirrored
 as a BuildingMiner.View.

 ### CitizenData
 The server-side representation of citizen data.

 Mirrored to clients as CitizenData.View.

 ### Permissions
 Permissions management for relationship of plauyers to the colony.

 Mirrored to clients as Permissions.View.

 ### WorkManager and WorkOrder subclasses
 Manages a list of tasks for the colony, providing a central system for multiple components to coordinate.

 The WorkManager and WorkOrders are not mirrored to clients.

 ### Job subclasses
 Jobs provide a link between a CitizenData and a Building, allow for persistent storage of job-specific data for a given citizen.

 Jobs are not mirrored to clients.
*/
package com.minecolonies.core.colony;