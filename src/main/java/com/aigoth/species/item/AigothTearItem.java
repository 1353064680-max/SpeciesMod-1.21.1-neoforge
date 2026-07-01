package com.aigoth.species.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.capability.PlayerSpeciesImpl;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class AigothTearItem extends Item {
    
    public AigothTearItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // 客户端仅返回成功，实际逻辑在服务端执行
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        
        // 服务端执行实际逻辑
        executeAigothTearEffect(level, player, stack);
        
        return InteractionResultHolder.consume(stack);
    }
    
    private void executeAigothTearEffect(Level level, Player player, ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        
        // 1. 清除所有药水效果
        player.removeAllEffects();
        
        // 2. 施加增益效果（持续30秒 = 600 ticks）
        int duration = 600;
        
        // 生命恢复 V (amplifier = 4)
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 4, false, false, true));
        
        // 抗性提升 V (amplifier = 4)
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 4, false, false, true));
        
        // 海豚的恩惠 I (amplifier = 0)
        player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, duration, 0, false, false, true));
        
        // 潮涌能量 I (amplifier = 0)
        player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, duration, 0, false, false, true));
        
        // 抗火 I (amplifier = 0)
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 0, false, false, true));
        
        // 饱和 I (amplifier = 0)
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, duration, 0, false, false, true));
        
        // 速度 I (amplifier = 0)
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0, false, false, true));
        
        // 3. 播放音效（不死图腾触发音效）
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.TOTEM_USE, player.getSoundSource(), 1.0F, 1.0F);
        
        // 4. 生成粒子效果（快乐村民粒子，25个）
        for (int i = 0; i < 25; i++) {
            double offsetX = (Math.random() - 0.5) * 1.5;
            double offsetY = Math.random() * 2.0;
            double offsetZ = (Math.random() - 0.5) * 1.5;
            serverLevel.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                player.getX() + offsetX,
                player.getY() + offsetY,
                player.getZ() + offsetZ,
                1, 0, 0, 0, 0
            );
        }
        
        // 5. ActionBar 消息
        player.displayClientMessage(
            Component.translatable("item.species.aigoth_tear.use_message").withStyle(ChatFormatting.AQUA),
            true // true 表示发送到 action bar
        );
        
        // 6. 消耗耐久（人鱼猫族不消耗耐久）
        if (!player.getAbilities().instabuild) {
            PlayerSpeciesImpl cap = player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            boolean isAigoth = cap.hasSelectedRace() && "aigoth".equals(cap.getSelectedRace().getRaceId());
            
            if (!isAigoth) {
                // 检查耐久是否即将耗尽（在增加耐久之前判断）
                boolean willBreak = stack.getDamageValue() + 1 >= stack.getMaxDamage();
                
                // 增加耐久
                stack.setDamageValue(stack.getDamageValue() + 1);
                
                // 如果即将耗尽，转化为黯淡珍珠
                if (willBreak) {
                    transformToDismalPearl(player);
                    stack.shrink(1);
                }
            }
        }
        
        // 8. 施加物品冷却（20秒 = 400 ticks）
        player.getCooldowns().addCooldown(this, 400);
    }
    
    private void transformToDismalPearl(Player player) {
        // 给予黯淡珍珠
        ItemStack dismalPearl = new ItemStack(ModItems.DISMAL_PEARL.get(), 1);
        
        // 尝试添加到背包
        boolean added = player.getInventory().add(dismalPearl);
        
        // 如果背包满了，掉落在地上
        if (!added) {
            player.drop(dismalPearl, false);
        }
        
        // 转化消息
        player.displayClientMessage(
            Component.translatable("item.species.aigoth_tear.transform_message").withStyle(ChatFormatting.AQUA),
            true // true 表示发送到 action bar
        );
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        
        // 添加物品描述提示
        tooltip.add(Component.translatable("item.species.aigoth_tear.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.species.aigoth_tear.tooltip2").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.species.aigoth_tear.tooltip3").withStyle(ChatFormatting.BLUE));
    }
}