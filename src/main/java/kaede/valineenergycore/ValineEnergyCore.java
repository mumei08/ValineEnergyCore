package kaede.valineenergycore;

import com.mojang.logging.LogUtils;
import kaede.valineenergycore.common.registration.VECreativeTabs;
import kaede.valineenergycore.common.registration.VERegistration;
import kaede.valineenergycore.common.config.VEConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * ValineEnergy Core のメインクラス
 * エネルギーシステムの前提MOD
 */
@Mod(ValineEnergyCore.MOD_ID)
public class ValineEnergyCore {

    public static final String MOD_ID = "valineenergycore";
    public static final String MOD_NAME = "ValineEnergy Core";
    public static final String VERSION = "1.0.0";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ValineEnergyCore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        // 登録処理
        VERegistration.register(modEventBus);
        VECreativeTabs.register(modEventBus);

        // Config登録
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, VEConfig.COMMON_SPEC);

        // イベントリスナー登録
        modEventBus.addListener(this::commonSetup);

        // Forge Event Bus (VENetworkRegistryが使用)
        // VENetworkRegistryは@Mod.EventBusSubscriberで自動登録される

        LOGGER.info("ValineEnergy Core initialized!");
        LOGGER.info("Memory-based energy system: 1MB = 10^50 VE");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("ValineEnergy Core common setup");

        event.enqueueWork(() -> {
            // Post-initialization処理
            // Capabilityの登録などをここで行う
            initializeCapabilities();
        });
    }

    private void initializeCapabilities() {
        LOGGER.info("Initializing VE Capabilities...");
        // TODO: Capability登録
    }
}