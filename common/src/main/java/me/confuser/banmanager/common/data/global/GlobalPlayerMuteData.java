package me.confuser.banmanager.common.data.global;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.ormlite.field.DatabaseField;
import me.confuser.banmanager.common.storage.mysql.ByteArray;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.UUID;

public class GlobalPlayerMuteData {

  @DatabaseField(generatedId = true)
  @Getter
  private int id;

  @DatabaseField(columnName = "uuid", canBeNull = false, index = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private byte[] uuidBytes;

  @DatabaseField(canBeNull = false, width = 16, columnDefinition = "VARCHAR(16) NOT NULL")
  @Getter
  private String name;

  @DatabaseField(canBeNull = false)
  @Getter
  private String reason;

  @DatabaseField(columnName = "actorUuid", canBeNull = false, index = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  private byte[] actorUuidBytes;

  @DatabaseField(canBeNull = false, width = 16, columnDefinition = "VARCHAR(16) NOT NULL")
  @Getter
  private String actorName;

  // Should always be database time
  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long created = System.currentTimeMillis() / 1000L;

  @DatabaseField(index = true, columnDefinition = "INT(10) NOT NULL")
  @Getter
  private long expires = 0;

  @DatabaseField(index = true)
  @Getter
  private boolean soft = false;

  private UUID uuid;
  private UUID actorUUID;


  GlobalPlayerMuteData() {

  }

  public GlobalPlayerMuteData(PlayerData player, PlayerData actor, String reason, boolean soft) {
    this.uuidBytes = player.getId();
    this.name = player.getName();
    this.reason = reason;
    this.actorUuidBytes = actor.getId();
    this.actorName = actor.getName();
    this.soft = soft;
  }

  public GlobalPlayerMuteData(PlayerData player, PlayerData actor, String reason, boolean soft, long expires) {
    this(player, actor, reason, soft);

    this.expires = expires;
  }

  // Only use for imports!
  public GlobalPlayerMuteData(PlayerData player, PlayerData actor, String reason, boolean soft, long expires, long created) {
    this(player, actor, reason, soft, expires);

    this.created = created;
  }

  public boolean hasExpired() {
    return getExpires() != 0 && getExpires() <= (System.currentTimeMillis() / 1000L);
  }

  public UUID getUUID() {
    if (uuid == null) {
      uuid = UUIDUtils.fromBytes(uuidBytes);
    }

    return uuid;
  }

  public UUID getActorUUID() {
    if (actorUUID == null) {
      actorUUID = UUIDUtils.fromBytes(actorUuidBytes);
    }

    return actorUUID;
  }

  public PlayerData getPlayer(BanManagerPlugin plugin) throws SQLException {
    return plugin.getPlayerStorage().createIfNotExists(getUUID(), getName());
  }

  public PlayerData getActor(BanManagerPlugin plugin) throws SQLException {
    return plugin.getPlayerStorage().createIfNotExists(getActorUUID(), getActorName());
  }

  public PlayerMuteData toLocal(BanManagerPlugin plugin) throws SQLException {
    return new PlayerMuteData(getPlayer(plugin), getActor(plugin), reason, false, soft, expires, created);
  }
}
