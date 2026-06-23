package com.aigoth.species.item;

import com.aigoth.species.SpeciesMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SpeciesMod.MOD_ID);
    
    // 灵魂之石
    public static final DeferredItem<RaceStoneItem> SOUL_STONE = ITEMS.register("soul_stone", 
        () -> new RaceStoneItem(new Item.Properties().stacksTo(1), "reset"));
    
    // 人族之石
    @SuppressWarnings("unused")
    public static final DeferredItem<RaceStoneItem> HUMAN_STONE = ITEMS.register("human_stone", 
        () -> new RaceStoneItem(new Item.Properties().stacksTo(1), "human"));
    
    // 猫族之石
    @SuppressWarnings("unused")
    public static final DeferredItem<RaceStoneItem> CATKIN_STONE = ITEMS.register("catkin_stone", 
        () -> new RaceStoneItem(new Item.Properties().stacksTo(1), "catkin"));
    
    // 狗族之石
    @SuppressWarnings("unused")
    public static final DeferredItem<RaceStoneItem> DOGKIN_STONE = ITEMS.register("dogkin_stone", 
        () -> new RaceStoneItem(new Item.Properties().stacksTo(1), "dogkin"));
    
    // 狐族之石
    @SuppressWarnings("unused")
    public static final DeferredItem<RaceStoneItem> FOXKIN_STONE = ITEMS.register("foxkin_stone", 
        () -> new RaceStoneItem(new Item.Properties().stacksTo(1), "foxkin"));
    
    // 羽兽族之石
    @SuppressWarnings("unused")
    public static final DeferredItem<RaceStoneItem> WINGED_BEAST_STONE = ITEMS.register("winged_beast_stone", 
        () -> new RaceStoneItem(new Item.Properties().stacksTo(1), "wingedbeast"));
    
    // 水生族之石
    @SuppressWarnings("unused")
    public static final DeferredItem<RaceStoneItem> AQUATIC_STONE = ITEMS.register("aquatic_stone", 
        () -> new RaceStoneItem(new Item.Properties().stacksTo(1), "aquatic"));
    
    // 粘液族之石
    @SuppressWarnings("unused")
    public static final DeferredItem<RaceStoneItem> SLIMEKIN_STONE = ITEMS.register("slimekin_stone", 
        () -> new RaceStoneItem(new Item.Properties().stacksTo(1), "slimekin"));
    
    // 精灵族之石
    @SuppressWarnings("unused")
    public static final DeferredItem<RaceStoneItem> ELF_STONE = ITEMS.register("elf_stone", 
        () -> new RaceStoneItem(new Item.Properties().stacksTo(1), "elf"));
    
    // 植物族之石
    @SuppressWarnings("unused")
    public static final DeferredItem<RaceStoneItem> PLANTKIN_STONE = ITEMS.register("plantkin_stone", 
        () -> new RaceStoneItem(new Item.Properties().stacksTo(1), "plantkin"));
    
    // 受诅者之石
    @SuppressWarnings("unused")
    public static final DeferredItem<RaceStoneItem> CURSED_STONE = ITEMS.register("cursed_stone", 
        () -> new RaceStoneItem(new Item.Properties().stacksTo(1), "cursed"));
    
    // 哀歌凝泪
    public static final DeferredItem<AigothTearItem> AIGOTH_TEAR = ITEMS.register("aigoth_tear", 
        () -> new AigothTearItem(new Item.Properties().durability(5).rarity(net.minecraft.world.item.Rarity.EPIC)));
    
    // 黯淡珍珠
    public static final DeferredItem<Item> DISMAL_PEARL = ITEMS.register("dismal_pearl", 
        () -> new Item(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)));
    
    // 创建模组专属的创造模式标签页
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SpeciesMod.MOD_ID);
    
    public static final net.neoforged.neoforge.registries.DeferredHolder<CreativeModeTab, CreativeModeTab> SPECIES_TAB = CREATIVE_MODE_TABS.register("species_tab", 
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.species.species"))
            .icon(() -> SOUL_STONE.get().getDefaultInstance())
            .displayItems((parameters, output) -> ITEMS.getEntries().forEach(entry -> output.accept(entry.get())))
            .build());
    
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }
}