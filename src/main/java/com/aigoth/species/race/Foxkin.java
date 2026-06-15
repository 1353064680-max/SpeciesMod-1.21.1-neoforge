package com.aigoth.species.race;

import com.aigoth.species.SpeciesMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.Vec3;

public class Foxkin extends Race {
    
    public Foxkin() {
        super("foxkin", Component.translatable("race.species.foxkin"),
              Component.translatable("race.species.foxkin.short_desc"),
              Component.translatable("race.species.foxkin.benefits"),
              Component.translatable("race.species.foxkin.drawbacks"),
              Component.translatable("skill.species.foxkin.name"),
              Component.translatable("skill.species.foxkin.description"));
    }

    @Override
    protected void initAttributes() {
        // 减少最大生命值
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.max_health"), 
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "foxkin_health_penalty"), -2.0, AttributeModifier.Operation.ADD_VALUE));

        // 增加移动速度
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.movement_speed"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "foxkin_speed_bonus"), 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

        // 增加摔落缓冲
        attributeModifiers.put(ResourceLocation.withDefaultNamespace("generic.safe_fall_distance"),
            new AttributeModifier(ResourceLocation.fromNamespaceAndPath(SpeciesMod.MOD_ID, "foxkin_fall_resistance_bonus"), 2.0, AttributeModifier.Operation.ADD_VALUE));
    }
    
    // ========== 主动技能：狐跃 ==========
    
    @Override
    public boolean hasActiveSkill() {
        return true;
    }
    
    @Override
    public int getSkillCooldownTicks() {
        return 20; // 20秒
    }
    
    @Override
    public float getFoodExhaustion() {
        return 16.0f; // 16.0 疲劳值 = 4 点饱食度（2格/2个鸡腿）
    }
    
    @Override
    public void executeSkill(ServerPlayer player) {
        // 1. 扣除饱食度（16.0 疲劳值 = 4 点饱食度 = 2格）
        player.getFoodData().addExhaustion(16.0f);
        
        // 2. 计算跳跃方向（面向准星方向）
        float yaw = player.getYRot();
        
        // 将角度转换为弧度（Minecraft Yaw: 0=南, 90=西, 180=北, 270=东）
        // 需要转换为标准数学角度并取反方向
        float yawRad = (float) Math.toRadians(-yaw);
        
        // 计算水平方向的单位向量
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);
        
        // 设置跳跃速度（向前 + 向上）
        double horizontalSpeed = 1.2;  // 水平速度（增强）
        double verticalSpeed = 0.8;    // 垂直速度（约4-5格高，增强）
        
        Vec3 jumpVelocity = new Vec3(
            forwardX * horizontalSpeed,
            verticalSpeed,
            forwardZ * horizontalSpeed
        );
        
        // 应用跳跃速度（直接设置，不累加）
        player.setDeltaMovement(jumpVelocity);
        
        // 标记玩家为"已跳跃"状态，防止被地面检测重置
        player.hurtMarked = true;
        
        // 3. 标记玩家为"狐跃状态"，用于在事件处理器中取消摔落伤害
        // 记录狐跃的到期时间（当前tick + 100 ticks = 5秒）
        long expiryTick = player.level().getGameTime() + 100;
        player.getPersistentData().putLong("foxkin_leap_expiry", expiryTick);
        
        // 4. 给予速度 II 效果，持续 5 秒（100 ticks）
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 1, false, false, false));

        // 5. 播放音效（狐狸叫声）
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.FOX_AMBIENT, player.getSoundSource(), 1.0F, 1.0F);
        
        // 6. 生成粒子效果（烟雾粒子）
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (int i = 0; i < 10; i++) {
                double offsetX = (Math.random() - 0.5) * 0.5;
                double offsetY = Math.random() * 0.5;
                double offsetZ = (Math.random() - 0.5) * 0.5;
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.CLOUD,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    1, 0, 0, 0, 0
                );
            }
        }
    }
}
