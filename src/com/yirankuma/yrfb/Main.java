package com.yirankuma.yrfb;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.NukkitRunnable;
import cn.nukkit.utils.Config;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main extends PluginBase implements Listener {

    @Override
    public void onEnable() {
        getServer().getLogger().info("YRFB插件test版本开启");
        getServer().getPluginManager().registerEvents(this, this);
        String path = getDataFolder().toPath().resolve("NPC").toString();
        String spath = getDataFolder().toPath().resolve("Skins").toString();
        File file = new File(path);
        File sfile = new File(spath);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!sfile.exists()) {
            sfile.mkdirs();
        }
        registerNPC();
    }

    private void registerNPC() {
        Entity.registerEntity(CmdNPC.class.getSimpleName(), CmdNPC.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("yrnpc")) {
            if (args.length <= 0) {
                return false;
            }
            switch (args[0]) {
                case "创建": {
                    if (sender instanceof Player) {
                        switch (args[1]) {
                            case "指令": {
                                if (args.length == 3) {
                                    String path = getDataFolder().toPath().resolve("NPC").resolve(args[2] + ".yml").toString();
                                    File file = new File(path);
                                    if (!file.exists()) {
                                        Config config = new Config(getDataFolder() + "/NPC/" + args[2] + ".yml", Config.YAML);
                                        LinkedHashMap<String, Object> dconfig = new LinkedHashMap<String, Object>();
                                        dconfig.put("名字", "CMDNPC");
                                        List cmds = new ArrayList();
                                        List xyz = new ArrayList(3);
                                        xyz.add(((Player) sender).x);
                                        xyz.add(((Player) sender).y);
                                        xyz.add(((Player) sender).z);
                                        cmds.add("me 亦染帅的一批");
                                        dconfig.put("指令", cmds);
                                        dconfig.put("大小", 1.0);
                                        dconfig.put("是否潜行", false);
                                        dconfig.put("是否旋转", false);
                                        dconfig.put("碰撞箱", "0,0,0,1,2,1");
                                        dconfig.put("旋转速度", 1.5);
                                        dconfig.put("锁定玩家", false);
                                        dconfig.put("皮肤", "默认");
                                        dconfig.put("手持物品", "264,0");
                                        dconfig.put("yaw", ((Player) sender).yaw);
                                        dconfig.put("pitch", ((Player) sender).pitch);
                                        dconfig.put("世界", (((Player) sender).getLocation().level.getName()));
                                        dconfig.put("坐标", xyz);
                                        config.setAll(dconfig);
                                        config.save();
                                        //生成NPC
                                        List xzy = config.getList("坐标");
                                        double x = (double) xzy.get(0);
                                        double y = (double) xzy.get(1);
                                        double z = (double) xzy.get(2);
                                        Level level = Server.getInstance().getLevelByName(config.getString("世界"));
                                        sender.sendMessage("配置文件创建成功!");
                                        CmdNPC npc = new CmdNPC(level.getChunk(4, 4), CmdNPC.getDefaultNBT(new Vector3(x, y, z)));
                                        npc.setNameTag(config.getString("名字"));
                                        npc.namedTag.putString("NPCKind", "cmd");
                                        npc.namedTag.putString("NPCID", args[2]);
                                        npc.namedTag.putBoolean("Invulnerable", true);
                                        npc.namedTag.putString("Item", config.getString("手持物品"));
                                        npc.namedTag.putBoolean("LockPlayer", config.getBoolean("锁定玩家"));
                                        Skin skin = new Skin();
                                        Path skinPath = getDataFolder().toPath().resolve("Skins");
                                        Path skinpath = skinPath.resolve(config.getString("皮肤")).resolve("skin.png");
                                        Path geometrypath = skinPath.resolve(config.getString("皮肤")).resolve("geometry.json");
                                        BufferedImage skindata = null;
                                        String skingeometry;
                                        try {
                                            skindata = ImageIO.read(skinpath.toFile());
                                            skingeometry = new String(Files.readAllBytes(geometrypath), StandardCharsets.UTF_8);
                                        } catch (IOException var11) {
                                            sender.sendMessage("不存在皮肤");
                                            return true;
                                        }
                                        if (skindata != null) {
                                            skin.setSkinData(skindata);
                                            skin.setGeometryData(skingeometry);
                                            skin.setGeometryName("geometry." + config.getString("皮肤"));
                                            skin.setSkinId(config.getString("皮肤"));
                                        }
                                        Float scale = (float) config.getDouble("大小");
                                        String itemthing = config.getString("手持物品");
                                        String[] itemthings = itemthing.split(",");
                                        Item handitem = new Item(Integer.valueOf(itemthings[0]), Integer.valueOf(itemthings[1]));
                                        npc.setScale(scale);
                                        npc.getInventory().setItemInHand(handitem);
                                        npc.setRotation(((Player) sender).yaw, ((Player) sender).pitch);
                                        npc.setSneaking(config.getBoolean("是否潜行"));
                                        String[] box = config.getString("碰撞箱").split(",");
                                        npc.boundingBox = new SimpleAxisAlignedBB(Integer.valueOf(box[0]), Integer.valueOf(box[1]), Integer.valueOf(box[2]), Integer.valueOf(box[3]), Integer.valueOf(box[4]), Integer.valueOf(box[5]));
                                        npc.setSkin(skin);
                                        npc.spawnToAll();
                                        return true;
                                    } else {
                                        sender.sendMessage("已经存在这个NPC");
                                        return true;
                                    }
                                }
                            }
                        }
                    } else {
                        sender.sendMessage("控制台不可以创建NPC");
                        return true;
                    }
                }
                case "重载": {
                    if (args.length == 2) {
                        String path = getDataFolder().toPath().resolve("NPC").resolve(args[1] + ".yml").toString();
                        File file = new File(path);
                        if (!file.exists()) {
                            sender.sendMessage("不存在这个NPC");
                            return true;
                        }
                        Entity[] entities = ((Player) sender).getLevel().getEntities();
                        for (int i = 0; i < entities.length; i++) {
                            if (entities[i].namedTag.contains("NPCID")) {
                                if (entities[i].namedTag.getString("NPCID").equals(args[1])) {
                                    entities[i].kill();
                                }
                            }
                        }
                        Config config = new Config();
                        config.load(getDataFolder() + "/NPC/" + args[1] + ".yml");
                        if (config.getAll().containsKey("是否旋转")) {
                            List xzy = config.getList("坐标");
                            double x = (double) xzy.get(0);
                            double y = (double) xzy.get(1);
                            double z = (double) xzy.get(2);
                            Level level = Server.getInstance().getLevelByName(config.getString("世界"));
                            CmdNPC npc = new CmdNPC(level.getChunk(4, 4), CmdNPC.getDefaultNBT(new Vector3(x, y, z)));
                            String itemthing = config.getString("手持物品");
                            npc.namedTag.putString("Item", itemthing);
                            npc.setNameTag(config.getString("名字"));
                            npc.namedTag.putString("NPCKind", "cmd");
                            npc.namedTag.putString("NPCID", args[1]);
                            npc.namedTag.putBoolean("Spin", config.getBoolean("是否旋转"));
                            npc.namedTag.putBoolean("LockPlayer", config.getBoolean("锁定玩家"));
                            for (int c = 0; c < entities.length; c++) {
                                if (entities[c].namedTag.contains("NPCID")) {
                                    if (entities[c].namedTag.getString("NPCID").equals(npc.namedTag.getString("NPCID"))) {
                                        entities[c].kill();
                                    }
                                }
                            }
                            Skin skin = new Skin();
                            Path skinPath = getDataFolder().toPath().resolve("Skins");
                            Path skinpath = skinPath.resolve(config.getString("皮肤")).resolve("skin.png");
                            Path geometrypath = skinPath.resolve(config.getString("皮肤")).resolve("geometry.json");
                            BufferedImage skindata = null;
                            String skingeometry;
                            try {
                                skindata = ImageIO.read(skinpath.toFile());
                                skingeometry = new String(Files.readAllBytes(geometrypath), StandardCharsets.UTF_8);
                            } catch (IOException var11) {
                                sender.sendMessage("不存在皮肤");
                                return false;
                            }
                            if (skindata != null) {
                                skin.setSkinData(skindata);
                                skin.setGeometryData(skingeometry);
                                skin.setGeometryName("geometry." + config.getString("皮肤"));
                                skin.setSkinId(config.getString("皮肤"));
                            }
                            Float scale = (float) config.getDouble("大小");
                            npc.setScale(scale);
                            String[] box = config.getString("碰撞箱").split(",");
                            npc.boundingBox = new SimpleAxisAlignedBB(Integer.valueOf(box[0]), Integer.valueOf(box[1]), Integer.valueOf(box[2]), Integer.valueOf(box[3]), Integer.valueOf(box[4]), Integer.valueOf(box[5]));
                            npc.setScale(scale);
                            npc.setRotation(config.getDouble("yaw"), config.getDouble("pitch"));
                            npc.setSneaking(config.getBoolean("是否潜行"));
                            npc.setSkin(skin);
                            npc.spawnToAll();
                            return true;
                        }
                    }
                }
                case "删除": {
                    if (args.length == 2) {
                        Entity[] entities = ((Player) sender).getLevel().getEntities();
                        for (int i = 0; i < entities.length; i++) {
                            if (entities[i].namedTag.contains("NPCID")) {
                                if (entities[i].namedTag.getString("NPCID").equals(args[1])) {
                                    entities[i].kill();
                                }
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return super.onCommand(sender, command, label, args);
    }

    @EventHandler
    public void cmdnpcDamaged(EntityDamageEvent event) {
        Entity cmdnpc = event.getEntity();
        if (cmdnpc instanceof CmdNPC || cmdnpc.namedTag.contains("NPCKind")) {
            if (event instanceof EntityDamageByEntityEvent) {
                String npcid = cmdnpc.namedTag.getString("NPCID");
                String path = getDataFolder().toPath().resolve("NPC").resolve(npcid + ".yml").toString();
                File file = new File(path);
                if (!file.exists()) {
                    if (((EntityDamageByEntityEvent) event).getDamager() instanceof Player) {
                        ((Player) ((EntityDamageByEntityEvent) event).getDamager()).sendMessage("这个NPC配置文件丢失");
                        event.getEntity().kill();
                    }
                    event.setCancelled();
                    return;
                }
                Config config = new Config();
                config.load(getDataFolder() + "/NPC/" + npcid + ".yml");
                List cmds = config.getList("指令");
                for (int i = 0; i < cmds.size(); i++) {
                    String cmd = cmds.get(i).toString();
                    cmd = cmd.replaceAll("@player", ((EntityDamageByEntityEvent) event).getDamager().getName());
                    Server.getInstance().dispatchCommand((CommandSender) ((EntityDamageByEntityEvent) event).getDamager(), cmd);
                }
                event.setCancelled();
            }
        }
    }

    @EventHandler//changelevel加载NPC
    public void onChangeLevel(EntityLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Entity[] entities = event.getTarget().getEntities();
            for (int i = 0; i < entities.length; i++) {
                if (entities[i].namedTag.contains("NPCKind")) {
                    String npcid = entities[i].namedTag.getString("NPCID");
                    Config config = new Config();
                    config.load(getDataFolder() + "/NPC/" + npcid + ".yml");
                    if (config.getAll().containsKey("是否旋转")) {
                        Skin skin = new Skin();
                        Path skinPath = getDataFolder().toPath().resolve("Skins");
                        Path skinpath = skinPath.resolve(config.getString("皮肤")).resolve("skin.png");
                        Path geometrypath = skinPath.resolve(config.getString("皮肤")).resolve("geometry.json");
                        BufferedImage skindata = null;
                        String skingeometry;
                        try {
                            skindata = ImageIO.read(skinpath.toFile());
                            skingeometry = new String(Files.readAllBytes(geometrypath), StandardCharsets.UTF_8);
                        } catch (IOException var11) {
                            ((Player) entity).sendMessage("不存在皮肤");
                            return;
                        }
                        if (skindata != null) {
                            skin.setSkinData(skindata);
                            skin.setGeometryData(skingeometry);
                            skin.setGeometryName("geometry." + config.getString("皮肤"));
                            skin.setSkinId(config.getString("皮肤"));
                        }
                        List xzy = config.getList("坐标");
                        double x = (double) xzy.get(0);
                        double y = (double) xzy.get(1);
                        double z = (double) xzy.get(2);
                        Level level = Server.getInstance().getLevelByName(config.getString("世界"));
                        CmdNPC npc = new CmdNPC(level.getChunk(4, 4), CmdNPC.getDefaultNBT(new Vector3(x, y, z)));
                        npc.setNameTag(config.getString("名字"));
                        String itemthing = config.getString("手持物品");
                        npc.namedTag.putString("Item", itemthing);
                        npc.namedTag.putString("NPCKind", "cmd");
                        npc.namedTag.putString("NPCID", entities[i].namedTag.getString("NPCID"));
                        npc.namedTag.putBoolean("Spin", config.getBoolean("是否旋转"));
                        npc.namedTag.putBoolean("LockPlayer", config.getBoolean("锁定玩家"));
                        Float scale = (float) config.getDouble("大小");
                        npc.setScale(scale);
                        String[] box = config.getString("碰撞箱").split(",");
                        npc.boundingBox = new SimpleAxisAlignedBB(Integer.valueOf(box[0]), Integer.valueOf(box[1]), Integer.valueOf(box[2]), Integer.valueOf(box[3]), Integer.valueOf(box[4]), Integer.valueOf(box[5]));
                        npc.setRotation(config.getDouble("yaw"), config.getDouble("pitch"));
                        npc.setSneaking(config.getBoolean("是否潜行"));
                        npc.setSkin(skin);
                        npc.setScale(scale);
                        for (int c = 0; c < entities.length; c++) {
                            if (entities[c].namedTag.contains("NPCID")) {
                                if (entities[c].namedTag.getString("NPCID").equals(npc.namedTag.getString("NPCID"))) {
                                    entities[c].kill();
                                }
                            }
                        }
                        if (npc.namedTag.getBoolean("Spin") == true) {
                            new NukkitRunnable() {
                                @Override
                                public void run() {
                                    npc.yaw = npc.yaw + config.getDouble("旋转速度");
                                }
                            }.runTaskTimer(this, 0, 1);
                        }
                        npc.spawnToAll();
                        List<String> npd = new ArrayList();
                        new NukkitRunnable() {
                            @Override
                            public void run() {
                                Entity[] players = npc.getLevel().getEntities();
                                for (int p = 0; p < players.length; p++) {
                                    double distance = (players[p].x - npc.x) + (players[p].y - npc.y) + (players[p].z - npc.z);
                                    if (players[p] instanceof Player) {
                                        npd.add(players[p].getName() + "@" + distance);
                                    }
                                }
                                npd.sort((mapping1, mapping2) -> {
                                    String[] nameMapNum1 = mapping1.split("@");
                                    String[] nameMapNum2 = mapping2.split("@");
                                    double compare = Double.parseDouble(nameMapNum1[1]) - Double.parseDouble(nameMapNum2[1]);
                                    if (compare > 0) {
                                        return 1;
                                    } else if (compare == 0) {
                                        return 0;
                                    } else {
                                        return -1;
                                    }
                                });
                                String name = npd.get(0).toString().split("@")[0];
                            }
                        }.runTaskTimer(this, 0, 1);
                    }
                }
            }
        }
    }

    @EventHandler//进服加载NPC
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Entity[] entities = player.getLevel().getEntities();
        for (int i = 0; i < entities.length; i++) {
            if (entities[i].namedTag.contains("NPCKind")) {
                String npcid = entities[i].namedTag.getString("NPCID");
                Config config = new Config();
                config.load(getDataFolder() + "/NPC/" + npcid + ".yml");
                if (config.getAll().containsKey("是否旋转")) {
                    Skin skin = new Skin();
                    Path skinPath = getDataFolder().toPath().resolve("Skins");
                    Path skinpath = skinPath.resolve(config.getString("皮肤")).resolve("skin.png");
                    Path geometrypath = skinPath.resolve(config.getString("皮肤")).resolve("geometry.json");
                    BufferedImage skindata = null;
                    String skingeometry;
                    try {
                        skindata = ImageIO.read(skinpath.toFile());
                        skingeometry = new String(Files.readAllBytes(geometrypath), StandardCharsets.UTF_8);
                    } catch (IOException var11) {
                        player.sendMessage("不存在皮肤");
                        return;
                    }
                    if (skindata != null) {
                        skin.setSkinData(skindata);
                        skin.setGeometryData(skingeometry);
                        skin.setGeometryName("geometry." + config.getString("皮肤"));
                        skin.setSkinId(config.getString("皮肤"));
                    }
                    List xzy = config.getList("坐标");
                    double x = (double) xzy.get(0);
                    double y = (double) xzy.get(1);
                    double z = (double) xzy.get(2);
                    Level level = Server.getInstance().getLevelByName(config.getString("世界"));
                    CmdNPC npc = new CmdNPC(level.getChunk(4, 4), CmdNPC.getDefaultNBT(new Vector3(x, y, z)));
                    npc.setNameTag(config.getString("名字"));
                    String itemthing = config.getString("手持物品");
                    npc.namedTag.putString("Item", itemthing);
                    npc.namedTag.putString("NPCKind", "cmd");
                    npc.namedTag.putString("NPCID", entities[i].namedTag.getString("NPCID"));
                    for (int c = 0; c < entities.length; c++) {
                        if (entities[c].namedTag.contains("NPCID")) {
                            if (entities[c].namedTag.getString("NPCID").equals(npc.namedTag.getString("NPCID"))) {
                                entities[c].kill();
                            }
                        }
                    }
                    npc.namedTag.putBoolean("Spin", config.getBoolean("是否旋转"));
                    npc.namedTag.putBoolean("LockPlayer", config.getBoolean("锁定玩家"));
                    Float scale = (float) config.getDouble("大小");
                    npc.setScale(scale);
                    String[] box = config.getString("碰撞箱").split(",");
                    npc.boundingBox = new SimpleAxisAlignedBB(Integer.valueOf(box[0]), Integer.valueOf(box[1]), Integer.valueOf(box[2]), Integer.valueOf(box[3]), Integer.valueOf(box[4]), Integer.valueOf(box[5]));
                    npc.setRotation(config.getDouble("yaw"), config.getDouble("pitch"));
                    npc.setSneaking(config.getBoolean("是否潜行"));
                    npc.setSkin(skin);
                    npc.setScale(scale);
                    if (npc.namedTag.getBoolean("Spin") == true) {
                        new NukkitRunnable() {
                            @Override
                            public void run() {
                                npc.yaw = npc.yaw + config.getDouble("旋转速度");
                            }
                        }.runTaskTimer(this, 0, 1);
                    }
                    npc.spawnToAll();
                    List<String> npd = new ArrayList();
                    new NukkitRunnable() {
                        @Override
                        public void run() {
                            Entity[] players = npc.getLevel().getEntities();
                            for (int p = 0; p < players.length; p++) {
                                double distance = (players[p].x - npc.x) + (players[p].y - npc.y) + (players[p].z - npc.z);
                                if (players[p] instanceof Player) {
                                    npd.add(players[p].getName() + "@" + distance);
                                }
                            }
                            npd.sort((mapping1, mapping2) -> {
                                String[] nameMapNum1 = mapping1.split("@");
                                String[] nameMapNum2 = mapping2.split("@");
                                double compare = Double.parseDouble(nameMapNum1[1]) - Double.parseDouble(nameMapNum2[1]);
                                if (compare > 0) {
                                    return 1;
                                } else if (compare == 0) {
                                    return 0;
                                } else {
                                    return -1;
                                }
                            });
                            String name = npd.get(0).toString().split("@")[0];
                        }
                    }.runTaskTimer(this, 0, 1);
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Entity[] entities = player.getLevel().getEntities();
        for (int i = 0; i < entities.length; i++) {
            if (entities[i].namedTag.contains("NPCKind")) {
                if (entities[i].namedTag.contains("LockPlayer")) {
                    List<String> npd = new ArrayList();
                    Entity[] players = player.getLevel().getEntities();
                    for (int p = 0; p < players.length; p++) {
                        double distance = (players[p].x - entities[i].x) + (players[p].y - entities[i].y) + (players[p].z - entities[i].z);
                        if (players[p] instanceof Player) {
                            npd.add(players[p].getName() + "@" + distance);
                        }
                    }
                    npd.sort((mapping1, mapping2) -> {
                        String[] nameMapNum1 = mapping1.split("@");
                        String[] nameMapNum2 = mapping2.split("@");
                        double compare = Double.parseDouble(nameMapNum1[1]) - Double.parseDouble(nameMapNum2[1]);
                        if (compare > 0) {
                            return 1;
                        } else if (compare == 0) {
                            return 0;
                        } else {
                            return -1;
                        }
                    });
                    String name = npd.get(0).toString().split("@")[0];
                    try {
                        Player playera = getServer().getPlayer(name);
                        if (entities[i].namedTag.getBoolean("LockPlayer") == true) {
                            if (getServer().getOnlinePlayers().containsValue(playera)) {
                                double npcx = entities[i].x - playera.x, npcy = entities[i].y - playera.y, npcz = entities[i].z - playera.z;
                                double yaw = Math.asin(npcx / Math.sqrt(npcx * npcx + npcz * npcz)) / 3.14 * 180,
                                        pitch = Math.round(Math.asin(npcy / Math.sqrt(npcx * npcx + npcz * npcz + npcy * npcy)) / 3.14 * 180);
                                if (npcz > 0) {
                                    yaw = -yaw + 180;
                                }
                                entities[i].yaw = yaw;
                                entities[i].pitch = pitch;
                            } else {
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void npcDeath(EntityDeathEvent event) {
        Entity npc = event.getEntity();
        if (npc.namedTag.contains("NPCKind")) {
            Item item = new Item(0);
            Item[] items = new Item[1];
            items[0] = item;
            event.setDrops(items);
        }
    }
}
