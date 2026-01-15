package kaede.valineenergycore.common.registration;

import kaede.valineenergycore.ValineEnergyCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * ValineEnergy Core のクリエイティブタブ登録（無限容量システム対応版）
 */
public class VECreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ValineEnergyCore.MOD_ID);

    public static final RegistryObject<CreativeModeTab> VE_TAB = CREATIVE_MODE_TABS.register(
            "valineenergycore",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.valineenergycore"))
                    .icon(() -> new ItemStack(VERegistration.VE_ENERGY_CUBE.get()))
                    .displayItems((parameters, output) -> {
                        // 無限容量ケーブル
                        output.accept(VERegistration.VE_CABLE_INFINITE_ITEM.get());

                        // エネルギーキューブ
                        output.accept(VERegistration.VE_ENERGY_CUBE_ITEM.get());

                        // 今後追加するアイテムもここに記述
                        // output.accept(VERegistration.ENERGY_TABLET.get());
                        // output.accept(VERegistration.NETWORK_READER.get());
                    })
                    .build()
    );

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}