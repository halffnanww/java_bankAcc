import java.util.Objects;

public class BankAccount {

    public static final long NO_LIMIT = Long.MAX_VALUE;

    private final String owner;
    private final String accountNumber;
    private final long withdrawalLimitRubKopecks;

    private Balance balance;
    private boolean blocked;

    public BankAccount(String owner, String accountNumber, Balance initialBalance,
                       long withdrawalLimitRubKopecks) {
        this.owner = requireText(owner, "owner");
        this.accountNumber = requireText(accountNumber, "accountNumber");
        this.balance = Objects.requireNonNull(initialBalance, "initialBalance не может быть null");
        if (withdrawalLimitRubKopecks < 0) {
            throw new IllegalArgumentException("Лимит не может быть отрицательным");
        }
        this.withdrawalLimitRubKopecks = withdrawalLimitRubKopecks;
    }

    public BankAccount(String owner, String accountNumber, Balance initialBalance) {
        this(owner, accountNumber, initialBalance, NO_LIMIT);
    }

    public void deposit(Balance amount) {
        ensureActive();
        Objects.requireNonNull(amount, "amount не может быть null");
        if (amount.getKopecks() == 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть положительной");
        }
        Balance inAccountCurrency = amount.convertTo(balance.getCurrency());
        balance = balance.add(inAccountCurrency);
    }

    public void withdraw(Balance amount) {
        ensureActive();
        Objects.requireNonNull(amount, "amount не может быть null");
        if (amount.getKopecks() == 0) {
            throw new IllegalArgumentException("Сумма снятия должна быть положительной");
        }
        Balance inAccountCurrency = amount.convertTo(balance.getCurrency());
        checkLimit(inAccountCurrency);
        balance = balance.subtract(inAccountCurrency);
    }


    public void transferTo(BankAccount target, Balance amount) {
        Objects.requireNonNull(target, "target не может быть null");
        Objects.requireNonNull(amount, "amount не может быть null");
        if (target == this) {
            throw new IllegalArgumentException("Нельзя переводить самому себе");
        }
        if (amount.getKopecks() == 0) {
            throw new IllegalArgumentException("Сумма перевода должна быть положительной");
        }
        ensureActive();
        target.ensureActive();

        Balance inSourceCurrency = amount.convertTo(balance.getCurrency());
        checkLimit(inSourceCurrency);
        // проверяем достаточность средств, не меняя баланс
        if (inSourceCurrency.getKopecks() > balance.getKopecks()) {
            throw new IllegalStateException(
                    "Недостаточно средств на счёте " + accountNumber + ": баланс " + balance);
        }
        Balance inTargetCurrency = inSourceCurrency.convertTo(target.balance.getCurrency());

        balance = balance.subtract(inSourceCurrency);
        target.balance = target.balance.add(inTargetCurrency);
    }

    public void block() {
        blocked = true;
    }

    public void unblock() {
        blocked = false;
    }

    public Balance getBalance() {
        return balance;
    }

    public String getOwner() {
        return owner;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public long getWithdrawalLimitRubKopecks() {
        return withdrawalLimitRubKopecks;
    }

    public boolean isBlocked() {
        return blocked;
    }

    private void checkLimit(Balance amountInAccountCurrency) {
        long amountRubKopecks = amountInAccountCurrency.toRubKopecks();
        if (amountRubKopecks > withdrawalLimitRubKopecks) {
            throw new IllegalStateException(
                    "Превышен лимит вывода: максимум " + formatRub(withdrawalLimitRubKopecks));
        }
    }

    private void ensureActive() {
        if (blocked) {
            throw new IllegalStateException("Счёт " + accountNumber + " заблокирован");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null) {
            throw new NullPointerException(fieldName + " не может быть null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        return value;
    }

    private static String formatRub(long kopecks) {
        return String.format("%d.%02d ₽", kopecks / 100, kopecks % 100);
    }

    @Override
    public String toString() {
        String limit = (withdrawalLimitRubKopecks == NO_LIMIT)
                ? "без лимита"
                : formatRub(withdrawalLimitRubKopecks) + " за операцию";
        return String.format("BankAccount{%s (%s): %s, лимит: %s, %s}",
                owner, accountNumber, balance, limit,
                blocked ? "ЗАБЛОКИРОВАН" : "активен");
    }
}