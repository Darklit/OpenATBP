package xyz.openatbp.extension.game.actors;

import java.awt.geom.Point2D;

import com.fasterxml.jackson.databind.JsonNode;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;

import xyz.openatbp.extension.ATBPExtension;
import xyz.openatbp.extension.Console;
import xyz.openatbp.extension.ExtensionCommands;
import xyz.openatbp.extension.MapData;
import xyz.openatbp.extension.game.ActorState;
import xyz.openatbp.extension.game.ActorType;

public class BaseTower extends Tower {
    private long lastHit;
    private boolean destroyed = false;
    private boolean isUnlocked = false;

    public BaseTower(ATBPExtension parentExt, Room room, String id, int team) {
        super(parentExt, room, team);
        this.currentHealth = 800;
        this.maxHealth = 800;
        this.location = getBaseTowerLocation(room, team);
        this.room = room;
        this.id = id;
        this.team = team;
        this.parentExt = parentExt;
        this.lastHit = 0;
        this.actorType = ActorType.TOWER;
        this.attackCooldown = 1000;
        this.avatar = "tower1";
        if (team == 1) this.avatar = "tower2";
        this.displayName = parentExt.getDisplayName(this.avatar);
        this.stats = this.initializeStats();
        this.xpWorth = 15;
        ExtensionCommands.createWorldFX(
                parentExt,
                room,
                this.id,
                "fx_target_ring_6",
                this.id + "_ring",
                15 * 60 * 1000,
                (float) this.location.getX(),
                (float) this.location.getY(),
                true,
                this.team,
                0f);
        ExtensionCommands.updateActorState(parentExt, room, id, ActorState.INVINCIBLE, true);
        ExtensionCommands.updateActorState(parentExt, room, this.id, ActorState.IMMUNITY, true);
    }

    private Point2D getBaseTowerLocation(Room room, int team) {
        float towerX;
        float towerZ;
        Point2D towerLocation;
        if (room.getGroupId().equals("Practice")) {
            towerX = team == 0 ? MapData.L1_PURPLE_TOWER_0[0] : MapData.L1_BLUE_TOWER_3[0];
            towerZ = team == 0 ? MapData.L1_PURPLE_TOWER_0[1] : MapData.L1_BLUE_TOWER_3[1];
        } else {
            towerX = team == 0 ? MapData.L2_PURPLE_BASE_TOWER[0] : MapData.L2_BLUE_BASE_TOWER[0];
            towerZ = team == 0 ? MapData.L2_PURPLE_BASE_TOWER[1] : MapData.L2_BLUE_BASE_TOWER[1];
        }
        towerLocation = new Point2D.Float(towerX, towerZ);
        return towerLocation;
    }

    @Override
    public boolean damaged(Actor a, int damage, JsonNode attackData) {
        if (!this.isUnlocked) return false;
        if (this.destroyed) return true;
        if (this.target == null && this.nearbyActors.isEmpty()) {
            if (a.getActorType() == ActorType.PLAYER) {
                UserActor ua = (UserActor) a;
                if (System.currentTimeMillis() - this.lastMissSoundTime >= 1500
                        && getAttackType(attackData) == AttackType.PHYSICAL) {
                    this.lastMissSoundTime = System.currentTimeMillis();
                    ExtensionCommands.playSound(
                            this.parentExt, ua.getUser(), ua.getId(), "sfx_attack_miss");
                } else if (System.currentTimeMillis() - this.lastSpellDeniedTime >= 1500) {
                    this.lastSpellDeniedTime = System.currentTimeMillis();
                    ExtensionCommands.playSound(
                            this.parentExt, ua.getUser(), ua.getId(), "sfx_tower_no_damage_taken");
                }
                ExtensionCommands.createActorFX(
                        this.parentExt,
                        this.room,
                        this.id,
                        "tower_no_damage_taken",
                        500,
                        this.id + "_noDamage",
                        true,
                        "",
                        true,
                        false,
                        this.team);
            }
            return false;
        } else if (a.getActorType() == ActorType.MINION) damage *= 0.5;
        this.changeHealth(this.getMitigatedDamage(damage, this.getAttackType(attackData), a) * -1);
        boolean notify = System.currentTimeMillis() - this.lastHit >= 1000 * 5;
        if (notify) ExtensionCommands.towerAttacked(parentExt, this.room, this.getTowerNum());
        if (notify) this.triggerNotification();
        return this.currentHealth <= 0;
    }

    @Override
    public void die(Actor a) {
        Console.debugLog(this.id + " has died! " + this.destroyed);
        this.currentHealth = 0;
        if (!this.destroyed) {
            this.destroyed = true;
            this.dead = true;
            UserActor earner = null;
            if (a.getActorType() == ActorType.PLAYER) {
                UserActor ua = (UserActor) a;
                earner = (UserActor) a;
                ua.addGameStat("towers", 1);
            }
            this.parentExt.getRoomHandler(this.room.getName()).addScore(earner, a.getTeam(), 50);
            ExtensionCommands.towerDown(parentExt, this.room, this.getTowerNum());
            ExtensionCommands.knockOutActor(parentExt, this.room, this.id, a.getId(), 100);
            ExtensionCommands.destroyActor(parentExt, this.room, this.id);
            for (User u : room.getUserList()) {
                String actorId = "tower2a";
                if (this.team == 0) actorId = "tower1a";
                ExtensionCommands.createWorldFX(
                        parentExt,
                        u,
                        String.valueOf(u.getId()),
                        actorId,
                        this.id + "_destroyed",
                        1000 * 60 * 15,
                        (float) this.location.getX(),
                        (float) this.location.getY(),
                        false,
                        this.team,
                        0f);
                ExtensionCommands.createWorldFX(
                        parentExt,
                        u,
                        String.valueOf(u.getId()),
                        "tower_destroyed_explosion",
                        this.id + "_destroyed_explosion",
                        2000,
                        (float) this.location.getX(),
                        (float) this.location.getY(),
                        false,
                        this.team,
                        0f);
                ExtensionCommands.removeFx(parentExt, u, this.id + "_ring");
                ExtensionCommands.removeFx(parentExt, u, this.id + "_target");
                if (this.target != null && this.target.getActorType() == ActorType.PLAYER)
                    ExtensionCommands.removeFx(parentExt, u, this.id + "_aggro");
            }
            for (UserActor ua : this.parentExt.getRoomHandler(this.room.getName()).getPlayers()) {
                if (ua.getTeam() == this.team) {
                    ExtensionCommands.playSound(
                            parentExt, ua.getUser(), "global", "announcer/base_tower_down");
                } else {
                    ExtensionCommands.playSound(
                            parentExt, ua.getUser(), "global", "announcer/you_destroyed_tower");
                }
            }
        }
    }

    @Override
    public int getTowerNum() { // Gets tower number for the client to process correctly
        if (this.team == 0) return 0;
        else return 3;
    }

    public void
            triggerNotification() { // Resets the hit timer so players aren't spammed by the tower
        // being
        // attacked
        this.lastHit = System.currentTimeMillis();
    }

    public void unlockBaseTower() {
        this.isUnlocked = true;
        ExtensionCommands.updateActorState(parentExt, room, id, ActorState.INVINCIBLE, false);
        ExtensionCommands.updateActorState(parentExt, room, this.id, ActorState.IMMUNITY, false);
    }

    public boolean isUnlocked() {
        return this.isUnlocked;
    }
}
