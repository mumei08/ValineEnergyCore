package kaede.valineenergycore.common.content.network;

import kaede.valineenergycore.api.energy.BigEnergy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全てのVEネットワークを管理するレジストリ
 * Singletonパターンで実装
 */
@Mod.EventBusSubscriber
public class VENetworkRegistry {

    // ディメンションごとのネットワークマップ
    private static final Map<Level, Set<VENetwork>> NETWORKS_BY_DIMENSION = new ConcurrentHashMap<>();

    // UUID -> Network の高速検索用
    private static final Map<UUID, VENetwork> NETWORKS_BY_ID = new ConcurrentHashMap<>();

    // BlockPos -> Network の高速検索用
    private static final Map<BlockPos, VENetwork> NETWORKS_BY_POS = new ConcurrentHashMap<>();

    /**
     * ネットワークを登録
     */
    public static void registerNetwork(VENetwork network) {
        NETWORKS_BY_DIMENSION.computeIfAbsent(network.world, k -> new HashSet<>()).add(network);
        NETWORKS_BY_ID.put(network.getNetworkId(), network);

        // 全Transmitterの座標を登録
        for (VETransmitter transmitter : network.getTransmitters()) {
            NETWORKS_BY_POS.put(transmitter.getPosition(), network);
        }
    }

    /**
     * ネットワークを登録解除
     */
    public static void unregisterNetwork(VENetwork network) {
        Set<VENetwork> networks = NETWORKS_BY_DIMENSION.get(network.world);
        if (networks != null) {
            networks.remove(network);
        }

        NETWORKS_BY_ID.remove(network.getNetworkId());

        // 全Transmitterの座標を削除
        for (VETransmitter transmitter : network.getTransmitters()) {
            NETWORKS_BY_POS.remove(transmitter.getPosition());
        }
    }

    /**
     * 指定座標のネットワークを取得
     */
    @Nullable
    public static VENetwork getNetworkAt(BlockPos pos) {
        return NETWORKS_BY_POS.get(pos);
    }

    /**
     * UUIDからネットワークを取得
     */
    @Nullable
    public static VENetwork getNetworkById(UUID id) {
        return NETWORKS_BY_ID.get(id);
    }

    /**
     * ディメンション内の全ネットワークを取得
     */
    public static Set<VENetwork> getNetworksInDimension(Level world) {
        return NETWORKS_BY_DIMENSION.getOrDefault(world, Collections.emptySet());
    }

    /**
     * Transmitterが追加された時の処理
     * 隣接するネットワークを探してマージするか、新規ネットワークを作成
     */
    public static void mergeOrCreateNetwork(VETransmitter transmitter) {
        BlockPos pos = transmitter.getPosition();
        Set<VENetwork> adjacentNetworks = new HashSet<>();

        // 隣接する6方向をチェック
        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            VENetwork adjacentNetwork = getNetworkAt(adjacentPos);

            if (adjacentNetwork != null && adjacentNetwork.isValid()) {
                adjacentNetworks.add(adjacentNetwork);
            }
        }

        if (adjacentNetworks.isEmpty()) {
            // 隣接ネットワークがない場合は新規作成
            VENetwork newNetwork = new VENetwork(transmitter.getWorld());
            newNetwork.addTransmitter(transmitter);
            registerNetwork(newNetwork);
        } else if (adjacentNetworks.size() == 1) {
            // 1つのネットワークに追加
            VENetwork network = adjacentNetworks.iterator().next();
            network.addTransmitter(transmitter);
            NETWORKS_BY_POS.put(pos, network);
        } else {
            // 複数のネットワークをマージ
            Iterator<VENetwork> iterator = adjacentNetworks.iterator();
            VENetwork primary = iterator.next();

            // 最初のネットワークに追加
            primary.addTransmitter(transmitter);
            NETWORKS_BY_POS.put(pos, primary);

            // 残りのネットワークをマージ
            while (iterator.hasNext()) {
                VENetwork toMerge = iterator.next();
                primary.merge(toMerge);
            }
        }
    }

    /**
     * 指定座標周辺のネットワークを再構築
     * Transmitterが削除された時に呼ばれる
     */
    public static void refreshNetworksAround(BlockPos pos) {
        // 隣接する6方向をチェック
        Map<Direction, VETransmitter> adjacentTransmitters = new HashMap<>();

        for (Direction direction : Direction.values()) {
            BlockPos adjacentPos = pos.relative(direction);
            VENetwork network = getNetworkAt(adjacentPos);

            if (network != null) {
                for (VETransmitter transmitter : network.getTransmitters()) {
                    if (transmitter.getPosition().equals(adjacentPos)) {
                        adjacentTransmitters.put(direction, transmitter);
                        break;
                    }
                }
            }
        }

        if (adjacentTransmitters.isEmpty()) {
            return;
        }

        // 各Transmitterから到達可能なTransmitterをBFSで探索
        List<Set<VETransmitter>> groups = new ArrayList<>();
        Set<VETransmitter> visited = new HashSet<>();

        for (VETransmitter start : adjacentTransmitters.values()) {
            if (visited.contains(start)) {
                continue;
            }

            Set<VETransmitter> group = new HashSet<>();
            Queue<VETransmitter> queue = new LinkedList<>();
            queue.add(start);
            visited.add(start);

            while (!queue.isEmpty()) {
                VETransmitter current = queue.poll();
                group.add(current);

                for (VETransmitter neighbor : current.getAdjacentTransmitters()) {
                    if (!visited.contains(neighbor) && !neighbor.getPosition().equals(pos)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }

            groups.add(group);
        }

        // グループが1つなら何もしない（ネットワークは分割されていない）
        if (groups.size() <= 1) {
            return;
        }

        // 複数のグループがある場合は、それぞれ新しいネットワークを作成
        for (Set<VETransmitter> group : groups) {
            if (!group.isEmpty()) {
                VETransmitter first = group.iterator().next();
                VENetwork oldNetwork = first.getNetwork();

                if (oldNetwork != null) {
                    // 古いネットワークから削除
                    for (VETransmitter transmitter : group) {
                        oldNetwork.removeTransmitter(transmitter);
                    }
                }

                // 新しいネットワークを作成
                VENetwork newNetwork = new VENetwork(first.getWorld());
                for (VETransmitter transmitter : group) {
                    newNetwork.addTransmitter(transmitter);
                }
                registerNetwork(newNetwork);
            }
        }
    }

    /**
     * ワールドがアンロードされた時の処理
     */
    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Set<VENetwork> networks = NETWORKS_BY_DIMENSION.remove(level);
            if (networks != null) {
                for (VENetwork network : networks) {
                    network.invalidate();
                }
            }
        }
    }

    /**
     * サーバーティック時の処理
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 全ディメンションの全ネットワークをtick
            for (Set<VENetwork> networks : NETWORKS_BY_DIMENSION.values()) {
                for (VENetwork network : networks) {
                    if (network.isValid()) {
                        network.tick();
                    }
                }
            }
        }
    }

    /**
     * デバッグ情報を取得
     */
    public static String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== VE Network Registry ===\n");

        int totalNetworks = 0;
        int totalTransmitters = 0;
        int totalAcceptors = 0;
        BigEnergy totalBuffer = BigEnergy.ZERO;
        BigEnergy totalCapacity = BigEnergy.ZERO;

        for (Map.Entry<Level, Set<VENetwork>> entry : NETWORKS_BY_DIMENSION.entrySet()) {
            Level world = entry.getKey();
            Set<VENetwork> networks = entry.getValue();

            sb.append(String.format("\nDimension: %s\n", world.dimension().location()));
            sb.append(String.format("  Networks: %d\n", networks.size()));

            for (VENetwork network : networks) {
                totalNetworks++;
                totalTransmitters += network.getTransmitterCount();
                totalAcceptors += network.getAcceptorCount();
                totalBuffer = totalBuffer.add(network.getBuffer());
                totalCapacity = totalCapacity.add(network.getCapacity());

                sb.append(String.format("    - %s\n", network.getDebugInfo()));
            }
        }

        sb.append(String.format("\nTotal Statistics:\n"));
        sb.append(String.format("  Networks: %d\n", totalNetworks));
        sb.append(String.format("  Transmitters: %d\n", totalTransmitters));
        sb.append(String.format("  Acceptors: %d\n", totalAcceptors));
        sb.append(String.format("  Total Buffer: %s\n", totalBuffer));
        sb.append(String.format("  Total Capacity: %s\n", totalCapacity));

        return sb.toString();
    }

    /**
     * 全ネットワークをクリア（デバッグ用）
     */
    public static void clearAll() {
        for (Set<VENetwork> networks : NETWORKS_BY_DIMENSION.values()) {
            for (VENetwork network : networks) {
                network.invalidate();
            }
        }
        NETWORKS_BY_DIMENSION.clear();
        NETWORKS_BY_ID.clear();
        NETWORKS_BY_POS.clear();
    }
}
