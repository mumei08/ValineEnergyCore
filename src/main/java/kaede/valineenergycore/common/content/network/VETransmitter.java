package kaede.valineenergycore.common.content.network;

import kaede.valineenergycore.api.energy.BigEnergy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Transmitter (ケーブル) の基本実装
 */
public abstract class VETransmitter implements IVETransmitter {

    protected VENetwork network;
    protected final BlockPos position;
    protected final VECableTier tier;

    public VETransmitter(BlockPos position, VECableTier tier) {
        this.position = position;
        this.tier = tier;
    }

    @Override
    public BlockPos getPosition() {
        return position;
    }

    public abstract Level getWorld();

    @Override
    @Nullable
    public VENetwork getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(@Nullable VENetwork network) {
        this.network = network;
    }

    @Override
    public BigEnergy getCapacity() {
        return tier.getCapacity();
    }

    @Override
    public VECableTier getTier() {
        return tier;
    }

    /**
     * 隣接するTransmitterを取得
     */
    public abstract Set<VETransmitter> getAdjacentTransmitters();

    /**
     * このTransmitterが削除される時の処理
     */
    public void onRemove() {
        if (network != null) {
            network.removeTransmitter(this);

            // ネットワークを分割する可能性があるので再構築
            VENetworkRegistry.refreshNetworksAround(position);
        }
    }

    /**
     * このTransmitterが追加される時の処理
     */
    public void onPlace() {
        // 隣接するネットワークを探してマージ
        VENetworkRegistry.mergeOrCreateNetwork(this);
    }
}
