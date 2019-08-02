package com.yirankuma.yrfb;

import cn.nukkit.entity.EntityHuman;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

import java.nio.charset.StandardCharsets;

public class MobNPC extends EntityHuman {
    public MobNPC(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt.putCompound("Skin", new CompoundTag()));
        nbt
                .putByteArray("Data", this.getSkin().getSkinData())
                .putString("ModelID", this.getSkin().getSkinId())
                .putByteArray("CapeData", this.getSkin().getCapeData())
                .putString("GeometryName", this.getSkin().getGeometryName())
                .putByteArray("GeometryData", this.getSkin().getGeometryData().getBytes(StandardCharsets.UTF_8));
    }
}
