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
 * ValineEnergy Core のクリエイティブタブ登録
 */
public class VECreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ValineEnergyCore.MOD_ID);

    public static final RegistryObject<CreativeModeTab> VE_TAB = CREATIVE_MODE_TABS.register(
            "valineenergycore",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.valineenergycore"))
                    .icon(() -> new ItemStack(VERegistration.VE_CABLE_BASIC.get()))
                    .displayItems((parameters, output) -> {
                        // ケーブルを追加
                        output.accept(VERegistration.VE_CABLE_BASIC_ITEM.get());
                        output.accept(VERegistration.VE_CABLE_ADVANCED_ITEM.get());
                        output.accept(VERegistration.VE_CABLE_ELITE_ITEM.get());
                        output.accept(VERegistration.VE_CABLE_ULTIMATE_ITEM.get());
                    })
                    .build()
    );

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}