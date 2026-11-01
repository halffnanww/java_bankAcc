import java.util.Objects;

public final class Balance {

    private final long kopecks;
    private final Currency currency;

    public Balance(long kopecks, Currency currency) {
        if (kopecks < 0) {
            throw new IllegalArgumentException(
                    "Сумма не может быть отрицательной: " + kopecks);
        }
        this.kopecks = kopecks;
        this.currency = Objects.requireNonNull(currency, "currency не может быть null");
    }

    public long getKopecks() {
        return kopecks;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Balance add(Balance other) {
        Objects.requireNonNull(other, "other не может быть null");
        requireSameCurrency(other);
        return new Balance(Math.addExact(kopecks, other.kopecks), currency);
    }

    public Balance subtract(Balance other) {
        Objects.requireNonNull(other, "other не может быть null");
        requireSameCurrency(other);
        if (other.kopecks > kopecks) {
            throw new IllegalStateException(
                    "Недостаточно средств: на балансе " + this + ", списать " + other);
        }
        return new Balance(kopecks - other.kopecks, currency);
    }

    public Balance convertTo(Currency target) {
        Objects.requireNonNull(target, "target не может быть null");
        if (target == currency) {
            return this;
        }
        long rubKopecks = Math.multiplyExact(kopecks, currency.getPriceInRublesKopecks());
        long targetKopecks = rubKopecks / target.getPriceInRublesKopecks();
        return new Balance(targetKopecks, target);
    }

    public long toRubKopecks() {
        return Math.multiplyExact(kopecks, currency.getPriceInRublesKopecks()) / 100;
    }

    private void requireSameCurrency(Balance other) {
        if (currency != other.currency) {
            throw new IllegalArgumentException(
                    "Операция возможна только в одной валюте: " + currency + " и " + other.currency);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Balance other)) return false;
        return kopecks == other.kopecks && currency == other.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kopecks, currency);
    }

    @Override
    public String toString() {
        return String.format("%d.%02d %s", kopecks / 100, kopecks % 100, currency.getSymbol());
    }
}