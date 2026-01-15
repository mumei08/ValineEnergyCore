package kaede.valineenergycore.common.registration;

import kaede.valineenergycore.ValineEnergyCore;
import kaede.valineenergycore.common.block.*;
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
 * ValineEnergy Core の全登録を管理するクラス（無限容量システム対応版）
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

    // VE Cable - Infinite (無限容量ケーブル)
    public static final RegistryObject<Block> VE_CABLE_INFINITE = BLOCKS.register(
            "ve_cable_infinite",
            () -> new BlockVECableInfinite(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .lightLevel(state -> 3) // ほのかに光る
            )
    );

    // VE Energy Cube (エネルギーキューブ)
    public static final RegistryObject<Block> VE_ENERGY_CUBE = BLOCKS.register(
            "ve_energy_cube",
            () -> new BlockVEEnergyCube(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIAMOND)
                    .strength(5.0f, 10.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 5) // より明るく光る
            )
    );

    // ========== アイテム登録 ==========

    // VE Cable Infinite Item
    public static final RegistryObject<Item> VE_CABLE_INFINITE_ITEM = ITEMS.register(
            "ve_cable_infinite",
            () -> new BlockItem(VE_CABLE_INFINITE.get(), new Item.Properties())
    );

    // VE Energy Cube Item
    public static final RegistryObject<Item> VE_ENERGY_CUBE_ITEM = ITEMS.register(
            "ve_energy_cube",
            () -> new BlockItem(VE_ENERGY_CUBE.get(), new Item.Properties())
    );

    // ========== BlockEntity登録 ==========

    // VE Cable Infinite BlockEntity
    public static final RegistryObject<BlockEntityType<BlockEntityVECableInfinite>> VE_CABLE_INFINITE_BE =
            BLOCK_ENTITIES.register("ve_cable_infinite", () ->
                    BlockEntityType.Builder.of(
                            BlockEntityVECableInfinite::new,
                            VE_CABLE_INFINITE.get()
                    ).build(null)
            );

    // VE Energy Cube BlockEntity
    public static final RegistryObject<BlockEntityType<BlockEntityVEEnergyCube>> VE_ENERGY_CUBE_BE =
            BLOCK_ENTITIES.register("ve_energy_cube", () ->
                    BlockEntityType.Builder.of(
                            BlockEntityVEEnergyCube::new,
                            VE_ENERGY_CUBE.get()
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