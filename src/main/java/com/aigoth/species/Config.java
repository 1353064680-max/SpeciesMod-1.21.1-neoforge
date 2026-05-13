package com.aigoth.species;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    // 添加配置项示例
    public static ModConfigSpec.BooleanValue enableRacialPotionEffectsByDefault;
    public static final ModConfigSpec SPEC;
    
    static {
        final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
        BUILDER.push("general");
        enableRacialPotionEffectsByDefault = BUILDER
            .comment("设置种族药水效果是否默认启用")
            .define("enableRacialPotionEffectsByDefault", true);
        BUILDER.pop();
        
        SPEC = BUILDER.build();
    }
}