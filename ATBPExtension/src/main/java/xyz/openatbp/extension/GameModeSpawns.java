package xyz.openatbp.extension;

import java.awt.geom.Point2D;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class GameModeSpawns {

    public static final float MINI_GUARDIAN_ACTOR_OFFSET = 2.5f;

    public static void spawnTowersForMode(Room room, ATBPExtension parentExt) {
        String groupId = room.getGroupId();

        switch (groupId) {
            case "Tutorial":
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getTowerActorData(0, 1, groupId));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getTowerActorData(1, 4, groupId));
                break;

            case "Practice":
            case "ARAM":
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getTowerActorData(0, 1, groupId));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getTowerActorData(1, 4, groupId));

                ExtensionCommands.createActor(
                        parentExt, room, MapData.getBaseTowerActorData(0, groupId));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getBaseTowerActorData(1, groupId));
                break;

            case "PVE":
            case "PVP":
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getTowerActorData(0, 1, groupId));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getTowerActorData(0, 2, groupId));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getTowerActorData(1, 1, groupId));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getTowerActorData(1, 2, groupId));

                ExtensionCommands.createActor(
                        parentExt, room, MapData.getBaseTowerActorData(0, groupId));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getBaseTowerActorData(1, groupId));
                break;
        }
    }

    public static void spawnAltarsForMode(Room room, ATBPExtension parentExt) {
        String groupId = room.getGroupId();

        switch (groupId) {
            case "Tutorial":
            case "Practice":
            case "ARAM":
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getAltarActorData(0, room.getGroupId()));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getAltarActorData(1, room.getGroupId()));
                break;

            case "PVE":
            case "PVP":
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getAltarActorData(0, room.getGroupId()));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getAltarActorData(1, room.getGroupId()));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getAltarActorData(2, room.getGroupId()));
                break;
        }
    }

    public static void spawnHealthForMode(Room room, ATBPExtension parentExt) {
        String groupId = room.getGroupId();

        switch (groupId) {
            case "Tutorial":
            case "Practice":
            case "ARAM":
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getHealthActorData(0, room.getGroupId(), -1));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getHealthActorData(1, room.getGroupId(), -1));
                break;

            case "PVE":
            case "PVP":
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getHealthActorData(0, room.getGroupId(), 0));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getHealthActorData(0, room.getGroupId(), 1));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getHealthActorData(0, room.getGroupId(), 2));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getHealthActorData(1, room.getGroupId(), 0));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getHealthActorData(1, room.getGroupId(), 1));
                ExtensionCommands.createActor(
                        parentExt, room, MapData.getHealthActorData(1, room.getGroupId(), 2));
                break;
        }
    }

    public static Point2D getBaseLocationForMode(Room room, int team) {
        String roomGroup = room.getGroupId();
        Point2D L1_PURPLE = new Point2D.Float(MapData.L1_PURPLE_BASE[0], MapData.L1_PURPLE_BASE[1]);
        Point2D L1_BLUE = new Point2D.Float(MapData.L1_BLUE_BASE[0], MapData.L1_BLUE_BASE[1]);
        Point2D L2_PURPLE = new Point2D.Float(MapData.L2_PURPLE_BASE[0], MapData.L2_PURPLE_BASE[1]);
        Point2D L2_BLUE = new Point2D.Float(MapData.L2_BLUE_BASE[0], MapData.L2_BLUE_BASE[1]);

        if (roomGroup.equals("Practice")
                || roomGroup.equals("Tutorial")
                || roomGroup.equals("ARAM")) {
            if (team == 0) return L1_PURPLE;
            else return L1_BLUE;
        } else {
            if (team == 0) return L2_PURPLE;
            else return L2_BLUE;
        }
    }

    public static Point2D getBaseTowerLocationForMode(Room room, int team) {
        float towerX;
        float towerZ;
        Point2D towerLocation;
        if (room.getGroupId().equals("Practice") || room.getGroupId().equals("ARAM")) {
            towerX = team == 0 ? MapData.L1_PURPLE_TOWER_0[0] : MapData.L1_BLUE_TOWER_3[0];
            towerZ = team == 0 ? MapData.L1_PURPLE_TOWER_0[1] : MapData.L1_BLUE_TOWER_3[1];
        } else {
            towerX = team == 0 ? MapData.L2_PURPLE_BASE_TOWER[0] : MapData.L2_BLUE_BASE_TOWER[0];
            towerZ = team == 0 ? MapData.L2_PURPLE_BASE_TOWER[1] : MapData.L2_BLUE_BASE_TOWER[1];
        }
        towerLocation = new Point2D.Float(towerX, towerZ);
        return towerLocation;
    }

    public static void spawnGuardianForMode(ATBPExtension parentExt, Room room, int team) {
        String roomGroup = room.getGroupId();
        float x;
        float z;

        if (roomGroup.equals("Tutorial")
                || roomGroup.equals("Practice")
                || room.getGroupId().equals("ARAM")) {
            x = team == 0 ? MapData.L1_P_GUARDIAN_MODEL_X : MapData.L1_B_GUARDIAN_MODEL_X;
            z = MapData.L1_GUARDIAN_MODEL_Z;
        } else {
            x = team == 0 ? MapData.L2_GUARDIAN1_X * -1 : MapData.L2_GUARDIAN1_X;
            z = MapData.L2_GUARDIAN1_Z;
        }

        ISFSObject guardian = getGuardian(team, x, z);
        ExtensionCommands.createActor(parentExt, room, guardian);
    }

    private static ISFSObject getGuardian(int team, float x, float z) {
        String bundle = team == 0 ? "GG_Purple" : "GG_Blue";

        ISFSObject guardian = new SFSObject();
        ISFSObject guardianSpawn = new SFSObject();
        guardian.putUtfString("id", "gumball" + team);
        guardian.putUtfString("actor", bundle);
        guardianSpawn.putFloat("x", x);
        guardianSpawn.putFloat("y", 0);
        guardianSpawn.putFloat("z", z);
        guardian.putSFSObject("spawn_point", guardianSpawn);
        guardian.putFloat("rotation", 0f);
        guardian.putInt("team", team);
        return guardian;
    }
}
