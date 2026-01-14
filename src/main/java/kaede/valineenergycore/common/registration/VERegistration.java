package kaede.valineenergycore.common.registration;

import kaede.valineenergycore.ValineEnergyCore;
import kaede.valineenergycore.common.block.*;
import kaede.valineenergycore.common.content.network.VECableTier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * ValineEnergy Core の全登録を管理するクラス
 */
public class VERegistration {

    // DeferredRegisterの定義
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ValineEnergyCore.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ValineEnergyCore.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ValineEnergyCore.MOD_ID);

    // ========== ブロック登録 ==========

    // VE Cable - Basic
    public static final RegistryObject<Block> VE_CABLE_BASIC = BLOCKS.register(
            "ve_cable_basic",
            () -> new BlockVECableBasic(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.0f, 3.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            )
    );

    // VE Cable - Advanced
    public static final RegistryObject<Block> VE_CABLE_ADVANCED = BLOCKS.register(
            "ve_cable_advanced",
            () -> new BlockVECableAdvanced(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.5f, 4.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            )
    );

    // VE Cable - Elite
    public static final RegistryObject<Block> VE_CABLE_ELITE = BLOCKS.register(
            "ve_cable_elite",
            () -> new BlockVECableElite(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0f, 5.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            )
    );

    // VE Cable - Ultimate
    public static final RegistryObject<Block> VE_CABLE_ULTIMATE = BLOCKS.register(
            "ve_cable_ultimate",
            () -> new BlockVECableUltimate(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            )
    );

    // ========== アイテム登録 ==========

    // VE Cable Items
    public static final RegistryObject<Item> VE_CABLE_BASIC_ITEM = ITEMS.register(
            "ve_cable_basic",
            () -> new BlockItem(VE_CABLE_BASIC.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> VE_CABLE_ADVANCED_ITEM = ITEMS.register(
            "ve_cable_advanced",
            () -> new BlockItem(VE_CABLE_ADVANCED.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> VE_CABLE_ELITE_ITEM = ITEMS.register(
            "ve_cable_elite",
            () -> new BlockItem(VE_CABLE_ELITE.get(), new Item.Properties())
    );

    public static final RegistryObject<Item> VE_CABLE_ULTIMATE_ITEM = ITEMS.register(
            "ve_cable_ultimate",
            () -> new BlockItem(VE_CABLE_ULTIMATE.get(), new Item.Properties())
    );

    // ========== BlockEntity登録 ==========

    public static final RegistryObject<BlockEntityType<BlockEntityVECable>> VE_CABLE_BE =
            BLOCK_ENTITIES.register("ve_cable", () ->
                    BlockEntityType.Builder.of(
                            (pos, state) -> {
                                // ブロックからTierを取得
                                Block block = state.getBlock();
                                VECableTier tier = VECableTier.BASIC;

                                if (block instanceof BlockVECable cable) {
                                    tier = cable.getTier();
                                }

                                return new BlockEntityVECable(pos, state, tier);
                            },
                            VE_CABLE_BASIC.get(),
                            VE_CABLE_ADVANCED.get(),
                            VE_CABLE_ELITE.get(),
                            VE_CABLE_ULTIMATE.get()
                    ).build(null)
            );

    /**
     * 登録を実行
     */
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }
}