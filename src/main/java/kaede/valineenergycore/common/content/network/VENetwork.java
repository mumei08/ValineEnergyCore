package kaede.valineenergycore.common.content.network;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.common.config.VEConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.slf4j.Logger;
import java.util.*;

/**
 * VEネットワーク - Config完全対応版
 */
public class VENetwork {

    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Set<VETransmitter> transmitters = new HashSet<>();
    protected final Map<BlockPos, AcceptorData> acceptors = new HashMap<>();

    protected BigEnergy buffer = BigEnergy.ZERO;
    protected BigEnergy capacity = BigEnergy.ZERO;
    protected boolean valid = true;

    protected final Level world;
    protected final UUID networkId;

    // 更新間隔カウンター
    private int tickCounter = 0;

    // キャッシュ
    private boolean acceptorsCacheDirty = true;
    private int lastCacheUpdate = 0;

    public VENetwork(Level world) {
        this.world = world;
        this.networkId = UUID.randomUUID();

        if (VEConfig.COMMON.logNetworkOperations.get()) {
            LOGGER.info("Created VENetwork: {}", networkId);
        }
    }

    public void addTransmitter(VETransmitter transmitter) {
        // 最大Transmitter数のチェック
        int maxTransmitters = VEConfig.COMMON.maxTransmittersPerNetwork.get();
        if (maxTransmitters > 0 && transmitters.size() >= maxTransmitters) {
            LOGGER.warn("Network {} reached maximum transmitter limit: {}",
                    networkId.toString().substring(0, 8), maxTransmitters);
            return;
        }

        if (transmitters.add(transmitter)) {
            transmitter.setNetwork(this);
            recalculateCapacity();
            markAcceptorsDirty();

            if (VEConfig.COMMON.logNetworkOperations.get()) {
                LOGGER.debug("Added transmitter to network {}: {} (total: {})",
                        networkId.toString().substring(0, 8),
                        transmitter.getPosition(),
                        transmitters.size());
            }
        }
    }

    public void removeTransmitter(VETransmitter transmitter) {
        if (transmitters.remove(transmitter)) {
            transmitter.setNetwork(null);
            recalculateCapacity();
            markAcceptorsDirty();

            if (VEConfig.COMMON.logNetworkOperations.get()) {
                LOGGER.debug("Removed transmitter from network {}: {} (remaining: {})",
                        networkId.toString().substring(0, 8),
                        transmitter.getPosition(),
                        transmitters.size());
            }

            if (transmitters.isEmpty()) {
                invalidate();
            }
        }
    }

    protected void recalculateCapacity() {
        capacity = BigEnergy.ZERO;
        for (VETransmitter transmitter : transmitters) {
            capacity = capacity.add(transmitter.getCapacity());
        }
    }

    /**
     * Acceptorsを再スキャンが必要とマーク
     */
    protected void markAcceptorsDirty() {
        acceptorsCacheDirty = true;
    }

    /**
     * 接続されているAcceptorを更新 - Config対応
     */
    public void updateAcceptors() {
        acceptors.clear();

        int maxAcceptors = VEConfig.COMMON.maxAcceptorsPerNetwork.get();
        int acceptorCount = 0;

        for (VETransmitter transmitter : transmitters) {
            if (maxAcceptors > 0 && acceptorCount >= maxAcceptors) {
                if (VEConfig.COMMON.showMemoryWarnings.get()) {
                    LOGGER.warn("Network {} reached maximum acceptor limit: {}",
                            networkId.toString().substring(0, 8), maxAcceptors);
                }
                break;
            }

            BlockPos pos = transmitter.getPosition();

            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = pos.relative(direction);

                // すでに追加済みならスキップ
                if (acceptors.containsKey(adjacentPos)) {
                    continue;
                }

                BlockEntity be = world.getBlockEntity(adjacentPos);

                if (be != null && !isTransmitter(be)) {
                    LazyOptional<IEnergyStorage> energyCap = be.getCapability(
                            ForgeCapabilities.ENERGY,
                            direction.getOpposite()
                    );

                    energyCap.ifPresent(storage -> {
                        if (storage.canReceive()) {
                            acceptors.put(adjacentPos, new AcceptorData(adjacentPos, storage));
                        }
                    });

                    if (acceptors.containsKey(adjacentPos)) {
                        acceptorCount++;
                        if (maxAcceptors > 0 && acceptorCount >= maxAcceptors) {
                            return;
                        }
                    }
                }
            }
        }

        acceptorsCacheDirty = false;
        lastCacheUpdate = tickCounter;

        if (VEConfig.COMMON.logNetworkOperations.get()) {
            LOGGER.debug("Updated acceptors for network {}: {} acceptors found",
                    networkId.toString().substring(0, 8), acceptors.size());
        }
    }

    /**
     * 毎tickの処理 - Config設定を考慮
     */
    public void tick() {
        if (!valid || transmitters.isEmpty()) {
            return;
        }

        tickCounter++;

        // ネットワーク更新間隔をチェック
        int updateInterval = VEConfig.COMMON.networkUpdateInterval.get();
        if (tickCounter % updateInterval == 0 || acceptorsCacheDirty) {
            // キャッシュが有効でない、または間隔に達した場合のみ更新
            if (!VEConfig.COMMON.enableNetworkCaching.get() ||
                    acceptorsCacheDirty ||
                    tickCounter - lastCacheUpdate >= VEConfig.COMMON.cacheUpdateInterval.get()) {
                updateAcceptors();
            }
        }

        if (buffer.isZero()) {
            return;
        }

        // エネルギーロスを適用
        if (VEConfig.COMMON.enableEnergyLoss.get()) {
            applyEnergyLoss();
        }

        // エネルギーを受け取れるAcceptorをフィルタリング
        List<AcceptorData> validAcceptors = new ArrayList<>();
        BigEnergy totalNeeded = BigEnergy.ZERO;

        for (AcceptorData acceptor : acceptors.values()) {
            IEnergyStorage storage = acceptor.getStorage();
            if (storage != null && storage.canReceive()) {
                int maxReceive = storage.getMaxEnergyStored() - storage.getEnergyStored();
                if (maxReceive > 0) {
                    validAcceptors.add(acceptor);
                    totalNeeded = totalNeeded.add(convertFromForgeEnergy(maxReceive));
                }
            }
        }

        if (validAcceptors.isEmpty()) {
            return;
        }

        // エネルギーを分配
        distributeEnergy(validAcceptors, totalNeeded);
    }

    /**
     * エネルギーロスを適用
     */
    private void applyEnergyLoss() {
        double lossPercent = VEConfig.COMMON.energyLossPercentPerBlock.get();
        int transmitterCount = transmitters.size();

        // 各ブロックごとにlossPercentずつ減少
        double totalLossPercent = lossPercent * transmitterCount;

        if (totalLossPercent > 0) {
            BigEnergy loss = buffer.multiply(totalLossPercent);
            buffer = buffer.subtract(loss);

            if (VEConfig.COMMON.enableDebugLogging.get()) {
                LOGGER.debug("Energy loss in network {}: {} VE ({:.2f}%)",
                        networkId.toString().substring(0, 8), loss, totalLossPercent * 100);
            }
        }
    }

    /**
     * エネルギー分配アルゴリズム
     */
    protected void distributeEnergy(List<AcceptorData> acceptors, BigEnergy totalNeeded) {
        BigEnergy available = buffer.min(totalNeeded);

        if (totalNeeded.greaterThan(available)) {
            // 需要が供給を上回る場合は比例配分
            for (AcceptorData acceptor : acceptors) {
                IEnergyStorage storage = acceptor.getStorage();
                int maxReceive = storage.getMaxEnergyStored() - storage.getEnergyStored();
                BigEnergy needed = convertFromForgeEnergy(maxReceive);

                // 比率計算
                double ratio = needed.doubleValue() / totalNeeded.doubleValue();
                BigEnergy toSend = available.multiply(ratio);

                // Forge Energyに変換して送信
                int feSend = convertToForgeEnergy(toSend);
                int sent = storage.receiveEnergy(feSend, false);

                buffer = buffer.subtract(convertFromForgeEnergy(sent));
            }
        } else {
            // 供給が十分な場合は全て送る
            for (AcceptorData acceptor : acceptors) {
                IEnergyStorage storage = acceptor.getStorage();
                int maxReceive = storage.getMaxEnergyStored() - storage.getEnergyStored();

                int sent = storage.receiveEnergy(maxReceive, false);
                buffer = buffer.subtract(convertFromForgeEnergy(sent));
            }
        }
    }

    public BigEnergy emit(BigEnergy energy) {
        if (!valid) {
            return BigEnergy.ZERO;
        }

        BigEnergy available = capacity.subtract(buffer);
        BigEnergy toInsert = energy.min(available);

        buffer = buffer.add(toInsert);
        return toInsert;
    }

    public BigEnergy extract(BigEnergy energy) {
        if (!valid) {
            return BigEnergy.ZERO;
        }

        BigEnergy toExtract = energy.min(buffer);
        buffer = buffer.subtract(toExtract);
        return toExtract;
    }

    public void invalidate() {
        if (VEConfig.COMMON.logNetworkOperations.get()) {
            LOGGER.info("Invalidating network: {}", networkId);
        }

        valid = false;
        transmitters.clear();
        acceptors.clear();
        VENetworkRegistry.unregisterNetwork(this);
    }

    public void merge(VENetwork other) {
        if (this == other || !other.valid) {
            return;
        }

        // ネットワークマージが無効なら何もしない
        if (!VEConfig.COMMON.enableNetworkMerging.get()) {
            return;
        }

        if (VEConfig.COMMON.logNetworkOperations.get()) {
            LOGGER.info("Merging network {} into {}",
                    other.networkId.toString().substring(0, 8),
                    this.networkId.toString().substring(0, 8));
        }

        Set<VETransmitter> otherTransmitters = new HashSet<>(other.transmitters);
        for (VETransmitter transmitter : otherTransmitters) {
            other.removeTransmitter(transmitter);
            this.addTransmitter(transmitter);
        }

        this.buffer = this.buffer.add(other.buffer);
        other.buffer = BigEnergy.ZERO;

        other.invalidate();
    }

    // ========== ユーティリティ ==========

    protected boolean isTransmitter(BlockEntity be) {
        return be instanceof IVETransmitter;
    }

    private BigEnergy convertFromForgeEnergy(int fe) {
        return kaede.valineenergycore.common.capabilities.VECapabilities.convertFromForgeEnergy(fe);
    }

    private int convertToForgeEnergy(BigEnergy ve) {
        return kaede.valineenergycore.common.capabilities.VECapabilities.convertToForgeEnergy(ve);
    }

    // ========== Getter ==========

    public Set<VETransmitter> getTransmitters() {
        return Collections.unmodifiableSet(transmitters);
    }

    public Map<BlockPos, AcceptorData> getAcceptors() {
        return Collections.unmodifiableMap(acceptors);
    }

    public BigEnergy getBuffer() {
        return buffer;
    }

    public BigEnergy getCapacity() {
        return capacity;
    }

    public boolean isValid() {
        return valid;
    }

    public UUID getNetworkId() {
        return networkId;
    }

    public int getTransmitterCount() {
        return transmitters.size();
    }

    public int getAcceptorCount() {
        return acceptors.size();
    }

    public String getDebugInfo() {
        return String.format(
                "VENetwork[ID=%s, Transmitters=%d/%s, Acceptors=%d/%s, Buffer=%s, Capacity=%s, Cached=%s]",
                networkId.toString().substring(0, 8),
                getTransmitterCount(),
                VEConfig.COMMON.maxTransmittersPerNetwork.get() > 0 ?
                        String.valueOf(VEConfig.COMMON.maxTransmittersPerNetwork.get()) : "∞",
                getAcceptorCount(),
                VEConfig.COMMON.maxAcceptorsPerNetwork.get() > 0 ?
                        String.valueOf(VEConfig.COMMON.maxAcceptorsPerNetwork.get()) : "∞",
                buffer,
                capacity,
                VEConfig.COMMON.enableNetworkCaching.get()
        );
    }
}