package com.yirankuma.yrfb;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddPlayerPacket;
import cn.nukkit.network.protocol.MovePlayerPacket;

import java.nio.charset.StandardCharsets;

public class RideNPC extends CmdNPC {

    public RideNPC(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt.putCompound("Skin", new CompoundTag()));
        nbt
                .putByteArray("Data", this.getSkin().getSkinData())
                .putString("ModelID", this.getSkin().getSkinId())
                .putByteArray("CapeData", this.getSkin().getCapeData())
                .putString("GeometryName", this.getSkin().getGeometryName())
                .putByteArray("GeometryData", this.getSkin().getGeometryData().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void spawnTo(Player player) {
        if (!this.hasSpawned.containsKey(player.getLoaderId())) {
            this.hasSpawned.put(player.getLoaderId(), player);
            this.server.updatePlayerListData(this.getUniqueId(), this.getId(), this.getName(), this.skin, new Player[]{player});
            AddPlayerPacket pk = new AddPlayerPacket();
            pk.uuid = this.getUniqueId();
            pk.username = this.getName();
            pk.entityUniqueId = this.getId();
            pk.entityRuntimeId = this.getId();
            pk.x = (float) this.x;
            pk.y = (float) this.y;
            pk.z = (float) this.z;
            pk.speedX = (float) this.motionX;
            pk.speedY = (float) this.motionY;
            pk.speedZ = (float) this.motionZ;
            pk.yaw = (float) this.yaw;
            String[] itemthings = this.namedTag.getString("Item").split(",");
            pk.pitch = (float) this.pitch;
            this.inventory.setItemInHand(new Item(Integer.valueOf(itemthings[0])));
            pk.item = this.getInventory().getItemInHand();
            pk.metadata = this.dataProperties;
            player.dataPacket(pk);
            this.inventory.setHelmet(Item.fromString(this.namedTag.getString("Helmet")));
            this.inventory.setChestplate(Item.fromString(this.namedTag.getString("Chestplate")));
            this.inventory.setLeggings(Item.fromString(this.namedTag.getString("Leggings")));
            this.inventory.setBoots(Item.fromString(this.namedTag.getString("Boots")));
            this.inventory.sendArmorContents(player);
            this.server.removePlayerListData(this.getUniqueId(), new Player[]{player});
            MovePlayerPacket mpk = new MovePlayerPacket();
            mpk.headYaw = (float) yaw;
            player.dataPacket(mpk);
            super.spawnTo(player);
        }
    }
}
