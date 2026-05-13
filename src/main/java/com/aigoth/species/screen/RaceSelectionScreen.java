package com.aigoth.species.screen;

import com.aigoth.species.capability.PlayerSpeciesCapability;
import com.aigoth.species.network.PacketHandler;
import com.aigoth.species.network.RaceSelectionPacket;
import com.aigoth.species.race.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@ParametersAreNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class RaceSelectionScreen extends Screen {
    private final List<Race> races = new ArrayList<>();
    private int selectedIndex = 0;
    // 添加用于界面元素定位的常量
    private static final int TITLE_Y = 20;
    private static final int RACE_NAME_Y = 50;
    private static final int SHORT_DESC_Y = 75;
    private static final int NAV_BUTTON_WIDTH = 30;
    private static final int NAV_BUTTON_HEIGHT = 20;
    private static final int DONE_BUTTON_WIDTH = 100;
    private static final int DONE_BUTTON_HEIGHT = 20;
    private static final int DESCRIPTION_BOX_MARGIN = 20;
    private static final int DESCRIPTION_BOX_PADDING = 10;

    public RaceSelectionScreen() {
        super(Component.translatable("screen.species.select_race"));
        // 初始化种族列表
        initRaceList();
    }

    // 初始化种族列表的独立方法
    private void initRaceList() {
        races.add(new Human());      // 人族
        races.add(new Catkin());     // 猫族
        races.add(new Dogkin());     // 狗族
        races.add(new Foxkin());     // 狐族
        races.add(new WingedBeast()); // 羽兽族
        races.add(new Aquatic());    // 水生族
        races.add(new Slimekin());   // 粘液族
        races.add(new Elf());        // 精灵族
        races.add(new Plantkin());   // 植物族
        races.add(new Cursed());     // 受诅者
        races.add(new Aigoth());     // 人鱼猫族（彩蛋种族）
    }

    @Override
    protected void init() {
        // 确认选择按钮 - 放置在屏幕底部居中
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
            // 保存选择的种族到Capability并发送到服务器
            if (!races.isEmpty() && minecraft != null && minecraft.player != null) {
                Race selectedRace = races.get(selectedIndex);
                // 禁止选择人鱼猫族（彩蛋种族）
                if ("aigoth".equals(selectedRace.getRaceId())) {
                    return; // 不允许选择
                }
                
                // 发送网络包到服务器
                PacketHandler.sendToServer(new RaceSelectionPacket(selectedRace.getRaceId()));
                
                // 同时在客户端设置种族
                var cap = minecraft.player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
                cap.setSelectedRace(selectedRace);
            }
            if (minecraft != null) {
                minecraft.setScreen(null); // 关闭界面
            }
        }).bounds((width - DONE_BUTTON_WIDTH) / 2, height - 50, DONE_BUTTON_WIDTH, DONE_BUTTON_HEIGHT).build());

        // 种族选择按钮（左右箭头）- 放置在种族名称两侧
        int navButtonY = RACE_NAME_Y - 5;
        this.addRenderableWidget(Button.builder(Component.literal("◀"), button -> {
            selectedIndex = (selectedIndex - 1 + races.size()) % races.size();
            if (minecraft != null) {
                this.init(minecraft, width, height); // 重新初始化以更新显示
            }
        }).bounds(width / 2 - 150, navButtonY, NAV_BUTTON_WIDTH, NAV_BUTTON_HEIGHT).build());

        this.addRenderableWidget(Button.builder(Component.literal("▶"), button -> {
            selectedIndex = (selectedIndex + 1) % races.size();
            if (minecraft != null) {
                this.init(minecraft, width, height); // 重新初始化以更新显示
            }
        }).bounds(width / 2 + 120, navButtonY, NAV_BUTTON_WIDTH, NAV_BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // 绘制标题
        guiGraphics.drawCenteredString(font, title, width / 2, TITLE_Y, 0xFFFFFF);

        // 绘制当前选中种族信息
        if (!races.isEmpty()) {
            Race currentRace = races.get(selectedIndex);
            
            // 绘制种族名称 - 使用更大更显眼的字体
            guiGraphics.drawCenteredString(font, currentRace.getRaceName(), width / 2, RACE_NAME_Y, 0xFFFF00);
            
            // 绘制简略介绍
            if (!currentRace.getShortDescription().getString().isEmpty()) {
                guiGraphics.drawCenteredString(font, currentRace.getShortDescription(), width / 2, SHORT_DESC_Y, 0xFFFFFF);
            }
            
            // 计算描述框位置和尺寸
            int descriptionBoxX = DESCRIPTION_BOX_MARGIN;
            int descriptionBoxY = SHORT_DESC_Y + 30;
            int descriptionBoxWidth = width - DESCRIPTION_BOX_MARGIN * 2;
            // 调整高度，确保不会遮挡底部的完成按钮
            int descriptionBoxHeight = height - descriptionBoxY - 60; // 减少底部预留空间以增加说明框高度
            int descriptionBoxInnerWidth = descriptionBoxWidth - DESCRIPTION_BOX_PADDING * 2;
            
            // 绘制描述框背景
            guiGraphics.fill(descriptionBoxX, descriptionBoxY, 
                           descriptionBoxX + descriptionBoxWidth, descriptionBoxY + descriptionBoxHeight, 
                           0x80000000); // 半透明黑色背景
            
            // 绘制描述框边框
            guiGraphics.fill(descriptionBoxX, descriptionBoxY, 
                           descriptionBoxX + descriptionBoxWidth, descriptionBoxY + 1, 
                           0xFFAAAAAA); // 顶部边框
            guiGraphics.fill(descriptionBoxX, descriptionBoxY + descriptionBoxHeight - 1, 
                           descriptionBoxX + descriptionBoxWidth, descriptionBoxY + descriptionBoxHeight, 
                           0xFFAAAAAA); // 底部边框
            guiGraphics.fill(descriptionBoxX, descriptionBoxY, 
                           descriptionBoxX + 1, descriptionBoxY + descriptionBoxHeight, 
                           0xFFAAAAAA); // 左边框
            guiGraphics.fill(descriptionBoxX + descriptionBoxWidth - 1, descriptionBoxY, 
                           descriptionBoxX + descriptionBoxWidth, descriptionBoxY + descriptionBoxHeight, 
                           0xFFAAAAAA); // 右边框
            
            // 绘制详细说明标题
            int descriptionTitleY = descriptionBoxY + 5;
            guiGraphics.drawCenteredString(font, Component.literal("说明:"), width / 2, descriptionTitleY, 0xFFFF00);
            
            // 绘制详细说明（多行）
            Component fullDescription = currentRace.getFullDescription();
            int descriptionStartY = descriptionTitleY + 15;
            drawMultilineComponent(guiGraphics, fullDescription, 
                                 descriptionBoxX + DESCRIPTION_BOX_PADDING, 
                                 descriptionStartY, 
                                 descriptionBoxInnerWidth);
        }
    }
    
    // 自定义方法绘制多行文本组件（支持格式化文本）
    private void drawMultilineComponent(GuiGraphics guiGraphics, Component component, int x, int y, int maxWidth) {
        String text = component.getString();
        
        // 分离增益和减益效果
        List<String> benefits = new ArrayList<>();
        List<String> drawbacks = new ArrayList<>();
        
        // 按逗号分割文本
        String[] parts = text.split(",");
        for (String part : parts) {
            if (part.startsWith("+")) {
                benefits.add(part);
            } else if (part.startsWith("-")) {
                drawbacks.add(part);
            } else {
                // 对于不以+或-开头的文本，根据上下文判断
                if (benefits.isEmpty() && drawbacks.isEmpty()) {
                    benefits.add(part);
                } else if (!benefits.isEmpty()) {
                    benefits.add(part);
                } else {
                    drawbacks.add(part);
                }
            }
        }
        
        // 绘制增益效果（绿色）
        int currentY = y;
        int lineHeight = font.lineHeight + 2;
        
        // 绘制增益部分
        if (!benefits.isEmpty()) {
            String benefitsText = String.join(",", benefits);
            List<FormattedCharSequence> benefitLines = font.split(net.minecraft.network.chat.Component.literal("§a" + benefitsText), maxWidth);
            for (FormattedCharSequence line : benefitLines) {
                int lineWidth = font.width(line);
                int lineX = x + (maxWidth - lineWidth) / 2; // 居中显示
                guiGraphics.drawString(font, line, lineX, currentY, 0xFFFFFF, false);
                currentY += lineHeight;
            }
        }
        
        // 如果既有增益又有减益，添加一行空隙
        if (!benefits.isEmpty() && !drawbacks.isEmpty()) {
            currentY += lineHeight / 2;
        }
        
        // 绘制减益部分
        if (!drawbacks.isEmpty()) {
            String drawbacksText = String.join(",", drawbacks);
            List<FormattedCharSequence> drawbackLines = font.split(net.minecraft.network.chat.Component.literal("§c" + drawbacksText), maxWidth);
            for (FormattedCharSequence line : drawbackLines) {
                int lineWidth = font.width(line);
                int lineX = x + (maxWidth - lineWidth) / 2; // 居中显示
                guiGraphics.drawString(font, line, lineX, currentY, 0xFFFFFF, false);
                currentY += lineHeight;
            }
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return false; // 不暂停游戏
    }
    
    @Override
    public void onClose() {
        // 阻止通过ESC键关闭界面，强制玩家必须选择种族
        // 只有在已经选择了种族的情况下才允许关闭界面
        if (minecraft != null && minecraft.player != null) {
            var cap = minecraft.player.getData(PlayerSpeciesCapability.PLAYER_SPECIES);
            if (cap.hasSelectedRace()) {
                super.onClose();
            }
        }
    }
}