package com.aigoth.species.race;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

// 种族基础属性类
public abstract class Race {
    protected final String raceId;
    protected final Component raceName;
    protected final Component shortDescription;
    protected final Component fullDescription;
    // 属性修饰符存储（属性名 -> 修饰符）
    protected final Map<ResourceLocation, AttributeModifier> attributeModifiers = new HashMap<>();

    public Race(String raceId, Component raceName, Component shortDescription, Component fullDescription) {
        this.raceId = raceId;
        this.raceName = raceName;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
        initAttributes();
    }
    
    // 兼容旧版本构造函数
    public Race(String raceId, Component raceName, Component description) {
        this(raceId, raceName, Component.literal(""), description);
    }

    // 初始化种族属性（子类实现）
    protected abstract void initAttributes();

    // 应用种族效果（可扩展特殊能力）
    public void applyEffects(Player player) {
        // 应用属性修饰符
        attributeModifiers.forEach((attrName, modifier) -> {
            net.minecraft.core.Holder<Attribute> attributeHolder = player.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE).get(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ATTRIBUTE, attrName)).orElse(null);
            if (attributeHolder != null) {
                AttributeInstance instance = player.getAttribute(attributeHolder);
                if (instance != null) {
                    // 检查修饰符是否已存在，如果存在则先移除再添加，避免重复添加
                    if (instance.getModifier(modifier.id()) != null) {
                        instance.removeModifier(modifier.id());
                    }
                    instance.addTransientModifier(modifier);
                }
            }
        });
    }

    // 移除种族效果
    public void removeEffects(Player player) {
        // 移除属性修饰符
        attributeModifiers.forEach((attrName, modifier) -> {
            net.minecraft.core.Holder<Attribute> attributeHolder = player.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE).get(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ATTRIBUTE, attrName)).orElse(null);
            if (attributeHolder != null) {
                AttributeInstance instance = player.getAttribute(attributeHolder);
                if (instance != null) {
                    // 移除修饰符
                    if (instance.getModifier(modifier.id()) != null) {
                        instance.removeModifier(modifier.id());
                    }
                }
            }
        });
    }

    // Getter方法
    public String getRaceId() { return raceId; }
    public Component getRaceName() { return raceName; }
    public Component getShortDescription() { return shortDescription; }
    public Component getFullDescription() { return fullDescription; }
}