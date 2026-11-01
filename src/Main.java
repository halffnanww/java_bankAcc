public class Main {
    public static void main(String[] args) {

        System.out.println("\n                    1. Создание счетов\n");

        BankAccount acc1 = new BankAccount(
                "Мирослава", "account-0001",
                new Balance(1_500_000L, Currency.RUB),
                200_000L);
        BankAccount acc2 = new BankAccount(
                "Марк", "account-0002",
                new Balance(50_000L, Currency.USD));

        System.out.println(acc1);
        System.out.println(acc2);

        System.out.println("\n                   2. Balance неизменяемый\n");
        Balance salary = new Balance(300_000L, Currency.RUB);
        Balance afterBonus = salary.add(new Balance(50_000L, Currency.RUB));
        System.out.println("salary     = " + salary);
        System.out.println("afterBonus = " + afterBonus + "  (salary не изменился)");

        System.out.println("\n                   3. Пополнение и снятие\n");
        acc1.deposit(new Balance(450_000L, Currency.RUB));
        System.out.println("После пополнения 4500 ₽: " + acc1);
        acc1.withdraw(new Balance(50_000L, Currency.RUB));
        System.out.println("После снятия 500 ₽:      " + acc1);

        System.out.println("\n                   4. Переводы\n");
        System.out.println("Перевод 100 ₽ Мирослава -> Марк:");
        acc1.transferTo(acc2, new Balance(10_000L, Currency.RUB));
        System.out.println(acc1);
        System.out.println(acc2);

        System.out.println("\nПеревод 10 $ Марк -> Мирослава (конвертируется в рубли):");
        acc2.transferTo(acc1, new Balance(1_000L, Currency.USD));
        System.out.println(acc1);
        System.out.println(acc2);

        System.out.println("\n                   5. Конвертация валют\n");
        Balance dollars = new Balance(10_000, Currency.USD);   // 100 $
        System.out.println(dollars + " = " + dollars.convertTo(Currency.RUB));
        System.out.println(dollars + " = " + dollars.convertTo(Currency.EUR));
        System.out.println(dollars + " = " + dollars.convertTo(Currency.CNY));

        System.out.println("\n                   6. Корнер-кейсы\n");
        runSafely("Отрицательный баланс",
                () -> new Balance(-1, Currency.RUB));
        runSafely("Сложение разных валют",
                () -> new Balance(100L, Currency.RUB).add(new Balance(100, Currency.USD)));
        runSafely("Снятие больше баланса",
                () -> acc1.withdraw(new Balance(100_000_000L, Currency.RUB)));
        runSafely("Снятие больше лимита",
                () -> acc1.withdraw(new Balance(500_000L, Currency.RUB)));
        runSafely("Перевод больше лимита",
                () -> acc1.transferTo(acc2, new Balance(201_000L, Currency.RUB)));
        runSafely("Перевод самому себе",
                () -> acc1.transferTo(acc1, new Balance(10L, Currency.RUB)));
        runSafely("Перевод на null-счёт",
                () -> acc1.transferTo(null, new Balance(100L, Currency.RUB)));
        runSafely("Пополнение на null",
                () -> acc1.deposit(null));

        System.out.println("\n                   7. Блокировка счёта\n");
        acc2.block();
        runSafely("Снятие с заблокированного счёта",
                () -> acc2.withdraw(new Balance(100L, Currency.USD)));
        runSafely("Перевод на заблокированный счёт",
                () -> acc1.transferTo(acc2, new Balance(100L, Currency.RUB)));
        acc2.unblock();
        System.out.println("Счёт разблокирован: " + acc2);

        System.out.println("\n                   8. equals / hashCode\n");
        Balance a = new Balance(10_000L, Currency.RUB);
        Balance b = new Balance(10_000L, Currency.RUB);
        Balance c = new Balance(10_000L, Currency.USD);
        System.out.println("a.equals(b) = " + a.equals(b) + "  (ожидалось true)");
        System.out.println("a.equals(c) = " + a.equals(c) + "  (ожидалось false)");
        System.out.println("a.hashCode() == b.hashCode() = " + (a.hashCode() == b.hashCode()));
    }

    private static void runSafely(String description, Runnable action) {
        try {
            action.run();
            System.out.println("ПРОВАЛ ТЕСТА (" +  description + "): исключение не было выброшено, хотя должно было");
        } catch (RuntimeException e) {
            System.out.println("ИСКЛЮЧЕНИЕ (" + description + "): "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}