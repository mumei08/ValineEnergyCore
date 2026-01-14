package kaede.valineenergycore.api.energy;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import net.minecraft.nbt.CompoundTag;

/**
 * ValineEnergy (VE) の値を表すクラス
 * FloatingLongのBigInteger版として設計
 * より大きな数値と高精度な計算をサポート
 */
public class BigEnergy implements Comparable<BigEnergy> {

    public static final BigEnergy ZERO = new BigEnergy(BigInteger.ZERO);
    public static final BigEnergy ONE = new BigEnergy(BigInteger.ONE);

    // 内部的にBigIntegerで保持（整数部分）
    private final BigInteger value;

    // 計算用のMathContext（精度設定）
    private static final MathContext MATH_CONTEXT = new MathContext(32, RoundingMode.HALF_UP);

    private BigEnergy(BigInteger value) {
        this.value = value;
    }

    // ========== ファクトリーメソッド ==========

    public static BigEnergy create(BigInteger value) {
        if (value.equals(BigInteger.ZERO)) return ZERO;
        if (value.equals(BigInteger.ONE)) return ONE;
        return new BigEnergy(value);
    }

    public static BigEnergy create(long value) {
        return create(BigInteger.valueOf(value));
    }

    public static BigEnergy create(double value) {
        return create(BigDecimal.valueOf(value).toBigInteger());
    }

    public static BigEnergy create(String value) {
        return create(new BigInteger(value));
    }

    // ========== 算術演算 ==========

    public BigEnergy add(BigEnergy other) {
        return create(this.value.add(other.value));
    }

    public BigEnergy subtract(BigEnergy other) {
        BigInteger result = this.value.subtract(other.value);
        // 負の値にならないようにガード
        return result.compareTo(BigInteger.ZERO) < 0 ? ZERO : create(result);
    }

    public BigEnergy multiply(BigEnergy other) {
        return create(this.value.multiply(other.value));
    }

    public BigEnergy multiply(long scalar) {
        return create(this.value.multiply(BigInteger.valueOf(scalar)));
    }

    public BigEnergy multiply(double scalar) {
        BigDecimal decimal = new BigDecimal(this.value).multiply(
                BigDecimal.valueOf(scalar), MATH_CONTEXT
        );
        return create(decimal.toBigInteger());
    }

    public BigEnergy divide(BigEnergy other) {
        if (other.isZero()) return ZERO;
        return create(this.value.divide(other.value));
    }

    public BigEnergy divide(long scalar) {
        if (scalar == 0) return ZERO;
        return create(this.value.divide(BigInteger.valueOf(scalar)));
    }

    // ========== 比較演算 ==========

    @Override
    public int compareTo(BigEnergy other) {
        return this.value.compareTo(other.value);
    }

    public boolean isZero() {
        return this.value.equals(BigInteger.ZERO);
    }

    public boolean greaterThan(BigEnergy other) {
        return this.compareTo(other) > 0;
    }

    public boolean greaterOrEqual(BigEnergy other) {
        return this.compareTo(other) >= 0;
    }

    public boolean smallerThan(BigEnergy other) {
        return this.compareTo(other) < 0;
    }

    public boolean smallerOrEqual(BigEnergy other) {
        return this.compareTo(other) <= 0;
    }

    // ========== ユーティリティ ==========

    public BigEnergy min(BigEnergy other) {
        return this.smallerThan(other) ? this : other;
    }

    public BigEnergy max(BigEnergy other) {
        return this.greaterThan(other) ? this : other;
    }

    public BigEnergy copy() {
        return create(this.value);
    }

    // ========== 型変換 ==========

    public BigInteger toBigInteger() {
        return this.value;
    }

    public long longValue() {
        return this.value.longValue();
    }

    public double doubleValue() {
        return this.value.doubleValue();
    }

    // ========== NBT保存/読込 ==========

    public void writeToNBT(CompoundTag tag, String key) {
        tag.putString(key, this.value.toString());
    }

    public static BigEnergy readFromNBT(CompoundTag tag, String key) {
        if (tag.contains(key)) {
            String str = tag.getString(key);
            try {
                return create(str);
            } catch (NumberFormatException e) {
                return ZERO;
            }
        }
        return ZERO;
    }

    // ========== 文字列変換 ==========

    @Override
    public String toString() {
        return formatWithSuffix(this.value);
    }

    /**
     * 単位付きで表示（K, M, G, T, P, E, Z, Y）
     */
    private static String formatWithSuffix(BigInteger value) {
        if (value.compareTo(BigInteger.valueOf(1000)) < 0) {
            return value.toString() + " VE";
        }

        String[] suffixes = {"K", "M", "G", "T", "P", "E", "Z", "Y"};
        BigInteger divisor = BigInteger.valueOf(1000);

        for (String suffix : suffixes) {
            BigInteger divided = value.divide(divisor);
            if (divided.compareTo(BigInteger.valueOf(1000)) < 0) {
                BigDecimal decimal = new BigDecimal(value)
                        .divide(new BigDecimal(divisor), 2, RoundingMode.HALF_UP);
                return decimal.toPlainString() + " " + suffix + "VE";
            }
            divisor = divisor.multiply(BigInteger.valueOf(1000));
        }

        // これ以上大きい場合は科学的記数法
        return value.toString() + " VE";
    }

    // ========== Object overrides ==========

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BigEnergy)) return false;
        return this.value.equals(((BigEnergy) obj).value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
}