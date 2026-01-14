package kaede.valineenergycore.common.content.network;

import kaede.valineenergycore.api.energy.BigEnergy;
import kaede.valineenergycore.api.energy.IVEContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import javax.annotation.Nullable;
import java.util.*;

/**
 * VEネットワークの基底クラス
 * 複数のTransmitter(ケーブル)を1つのネットワークとして管理
 */
public class VENetwork {

    // ネットワークに含まれる全てのTransmitter
    protected final Set<VETransmitter> transmitters = new HashSet<>();

    // ネットワークに接続されているAcceptor (エネルギー受容側)
    protected final Map<BlockPos, AcceptorData> acceptors = new HashMap<>();

    // ネットワークのバッファ (全ケーブルの合計容量)
    protected BigEnergy buffer = BigEnergy.ZERO;

    // ネットワークの最大容量
    protected BigEnergy capacity = BigEnergy.ZERO;

    // ネットワークが有効かどうか
    protected boolean valid = true;

    // ネットワークのワールド
    protected final Level world;

    // ネットワークのUUID
    protected final UUID networkId;

    public VENetwork(Level world) {
        this.world = world;
        this.networkId = UUID.randomUUID();
    }

    /**
     * Transmitterをネットワークに追加
     */
    public void addTransmitter(VETransmitter transmitter) {
        if (transmitters.add(transmitter)) {
            transmitter.setNetwork(this);
            recalculateCapacity();
            updateAcceptors();
        }
    }

    /**
     * Transmitterをネットワークから削除
     */
    public void removeTransmitter(VETransmitter transmitter) {
        if (transmitters.remove(transmitter)) {
            transmitter.setNetwork(null);
            recalculateCapacity();
            updateAcceptors();

            // Transmitterがなくなったらネットワークを無効化
            if (transmitters.isEmpty()) {
                invalidate();
            }
        }
    }

    /**
     * ネットワーク容量を再計算
     */
    protected void recalculateCapacity() {
        capacity = BigEnergy.ZERO;
        for (VETransmitter transmitter : transmitters) {
            capacity = capacity.add(transmitter.getCapacity());
        }
    }

    /**
     * 接続されているAcceptorを更新
     */
    protected void updateAcceptors() {
        acceptors.clear();

        for (VETransmitter transmitter : transmitters) {
            BlockPos pos = transmitter.getPosition();

            // 6方向をチェック
            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = pos.relative(direction);
                BlockEntity be = world.getBlockEntity(adjacentPos);

                if (be != null && !isTransmitter(be)) {
                    // VE Capabilityを持っているかチェック
                    IVEContainer container = getVEContainer(be, direction.getOpposite());
                    if (container != null && container.canReceive()) {
                        acceptors.put(adjacentPos, new AcceptorData(adjacentPos, container));
                    }
                }
            }
        }
    }

    /**
     * 毎tickの処理 - エネルギーを分配
     */
    public void tick() {
        if (!valid || transmitters.isEmpty()) {
            return;
        }

        // バッファにエネルギーがない場合はスキップ
        if (buffer.isZero()) {
            return;
        }

        // エネルギーを受け取れるAcceptorをフィルタリング
        List<AcceptorData> validAcceptors = new ArrayList<>();
        BigEnergy totalNeeded = BigEnergy.ZERO;

        for (AcceptorData acceptor : acceptors.values()) {
            IVEContainer container = acceptor.getContainer();
            if (container != null && container.canReceive()) {
                BigEnergy needed = container.getNeeded();
                if (!needed.isZero()) {
                    validAcceptors.add(acceptor);
                    totalNeeded = totalNeeded.add(needed);
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
     * エネルギー分配アルゴリズム
     */
    protected void distributeEnergy(List<AcceptorData> acceptors, BigEnergy totalNeeded) {
        BigEnergy available = buffer.min(totalNeeded);

        if (totalNeeded.greaterThan(available)) {
            // 需要が供給を上回る場合は比例配分
            for (AcceptorData acceptor : acceptors) {
                IVEContainer container = acceptor.getContainer();
                BigEnergy needed = container.getNeeded();

                // 比率計算: (needed / totalNeeded) * available
                double ratio = needed.doubleValue() / totalNeeded.doubleValue();
                BigEnergy toSend = available.multiply(ratio);

                BigEnergy sent = container.insert(toSend, IVEContainer.Action.EXECUTE);
                buffer = buffer.subtract(sent);
            }
        } else {
            // 供給が十分な場合は全て送る
            for (AcceptorData acceptor : acceptors) {
                IVEContainer container = acceptor.getContainer();
                BigEnergy needed = container.getNeeded();

                BigEnergy sent = container.insert(needed, IVEContainer.Action.EXECUTE);
                buffer = buffer.subtract(sent);
            }
        }
    }

    /**
     * ネットワークにエネルギーを挿入 (Emitter側から)
     */
    public BigEnergy emit(BigEnergy energy) {
        if (!valid) {
            return BigEnergy.ZERO;
        }

        BigEnergy available = capacity.subtract(buffer);
        BigEnergy toInsert = energy.min(available);

        buffer = buffer.add(toInsert);
        return toInsert;
    }

    /**
     * ネットワークを無効化
     */
    public void invalidate() {
        valid = false;
        transmitters.clear();
        acceptors.clear();
        VENetworkRegistry.unregisterNetwork(this);
    }

    /**
     * 2つのネットワークをマージ
     */
    public void merge(VENetwork other) {
        if (this == other || !other.valid) {
            return;
        }

        // 他のネットワークの全Transmitterをこちらに移動
        Set<VETransmitter> otherTransmitters = new HashSet<>(other.transmitters);
        for (VETransmitter transmitter : otherTransmitters) {
            other.removeTransmitter(transmitter);
            this.addTransmitter(transmitter);
        }

        // バッファを統合
        this.buffer = this.buffer.add(other.buffer);
        other.buffer = BigEnergy.ZERO;

        // 他のネットワークを無効化
        other.invalidate();
    }

    // ========== ユーティリティ ==========

    protected boolean isTransmitter(BlockEntity be) {
        return be instanceof IVETransmitter;
    }

    @Nullable
    protected IVEContainer getVEContainer(BlockEntity be, Direction side) {
        // Capabilityからコンテナを取得する実装
        // 実装はCapabilityシステムに依存
        return null; // TODO: Capability実装
    }

    // ========== Getter/Setter ==========

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

    /**
     * ネットワーク情報を文字列で取得 (デバッグ用)
     */
    public String getDebugInfo() {
        return String.format(
                "VENetwork[ID=%s, Transmitters=%d, Acceptors=%d, Buffer=%s, Capacity=%s]",
                networkId.toString().substring(0, 8),
                getTransmitterCount(),
                getAcceptorCount(),
                buffer,
                capacity
        );
    }
}