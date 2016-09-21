package com.sitepoint.optional.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class Test {

    public static void main(String[] args) {
        misbehaving();
        attemptedSolution();
        alternativeSolutionNoOptional();
        alternativeNonSolutionNoOptional();
        alternativeSolutionOptionalOutputOnly();
        alternativeSolutionOptionalInputOnly();
    }

    public static void misbehaving() {
        Map<Long, Account> bank = new HashMap<>();

        Function<Long, Account> findAccount = id -> bank.get(id);
        Function<Account, Balance> extractBalance = account -> account != null ? account.getBalance() : new Balance(0., Currency.DOLLAR);
        Function<Balance, Double> toDollars = balance -> {
            if (balance == null) {
                return 0.;
            }
            switch (balance.getCurrency()) {
                case DOLLAR: return balance.getAmount();
                case POUND: return balance.getAmount() * 1.3;
                case EURO: return balance.getAmount() * 1.1;
                default: return 0.;
            }
        };

        Optional<Long> accountId1 = Optional.of(1L);
        Optional<Long> accountId2 = Optional.ofNullable(null);
        Optional<Long> accountId3 = Optional.of(3L);
        Optional<Long> accountId4 = Optional.of(4L);
        Optional<Long> accountId5 = Optional.of(5L);

        bank.put(1L, new Account(1L, new Balance(100., Currency.EURO)));
        bank.put(4L, null);
        bank.put(5L, new Account(2L, null));

        System.out.println("Account 1");  // An account properly stored with a non-null balance
        accountId1.map(findAccount).map(extractBalance).map(toDollars).ifPresent(System.out::println);  // 110.0
        accountId1.map(id -> toDollars.apply(extractBalance.apply(findAccount.apply(id)))).ifPresent(System.out::println);  // 110.0

        System.out.println("Account 2");  // id == null => Optional.empty
        accountId2.map(findAccount).map(extractBalance).map(toDollars).ifPresent(System.out::println);  // Optional.empty
        accountId2.map(id -> toDollars.apply(extractBalance.apply(findAccount.apply(id)))).ifPresent(System.out::println);  // Optional.empty

        System.out.println("Account 3");  // Id not in map => findAccount returns null
        accountId3.map(findAccount).map(extractBalance).map(toDollars).ifPresent(System.out::println);  // Optional.empty
        accountId3.map(id -> toDollars.apply(extractBalance.apply(findAccount.apply(id)))).ifPresent(System.out::println); // 0.0

        System.out.println("Account 4");  //Id associated to null in the map
        accountId4.map(findAccount).map(extractBalance).map(toDollars).ifPresent(System.out::println);  // Optional.empty
        accountId4.map(id -> toDollars.apply(extractBalance.apply(findAccount.apply(id)))).ifPresent(System.out::println);  // 0.0

        System.out.println("Account 5");  // Account's balance is null
        accountId5.map(findAccount).map(extractBalance).map(toDollars).ifPresent(System.out::println);  // Optional.empty
        accountId5.map(id -> toDollars.apply(extractBalance.apply(findAccount.apply(id)))).ifPresent(System.out::println);  // 0.0
    }

    /**
     * This is the best solution we can come up with to cope with the way `Optional` is designed.
     * We need to reengineer the functions in the Optional-map-chain so that they won't treat `null` as a special case,
     * and that they use `Optional` for both their inputs and outputs.
     */
    public static void attemptedSolution() {
        Map<Long, Account> bank = new HashMap<>();
        Function<Long, Optional<Account>> findAccountOpt = id -> Optional.ofNullable(bank.get(id));

        Function<Optional<Account>, Optional<Balance>> extractBalanceOpt = accountOpt -> {
            Optional<Balance> balanceOpt = accountOpt.map(Account::getBalance);
            return balanceOpt.isPresent()
                    ? balanceOpt
                    : Optional.of(new Balance(0., Currency.DOLLAR));
        };

        Function<Optional<Balance>, Optional<Double>> toDollarsOpt = balanceOpt -> {
            Function<Balance, Double> toDollars = balance -> {
                switch (balance.getCurrency()){
                    case DOLLAR:
                        return balance.getAmount();
                    case POUND:
                        return balance.getAmount() * 1.3;
                    case EURO:
                        return balance.getAmount() * 1.1;
                    default:
                        return 0.;
                }
            };
            Optional<Double> dollarsOpt = balanceOpt.map(toDollars);
            return dollarsOpt.isPresent()
                    ? dollarsOpt
                    : Optional.of(0.);
        };

        Optional<Long> accountId1 = Optional.of(1L);
        Optional<Long> accountId2 = Optional.ofNullable(null);
        Optional<Long> accountId3 = Optional.of(3L);
        Optional<Long> accountId4 = Optional.of(4L);
        Optional<Long> accountId5 = Optional.of(5L);

        bank.put(1L, new Account(1L, new Balance(100., Currency.EURO)));
        bank.put(4L, null);
        bank.put(5L, new Account(2L, null));

        System.out.println("Account 1");  // An account properly stored with a non-null balance
        accountId1.map(findAccountOpt).map(extractBalanceOpt).flatMap(toDollarsOpt).ifPresent(System.out::println);  // 110.0
        accountId1.flatMap(findAccountOpt.andThen(extractBalanceOpt).andThen(toDollarsOpt)).ifPresent(System.out::println);  // 110.0

        System.out.println("Account 2");  // id == null => Optional.empty
        accountId2.map(findAccountOpt).map(extractBalanceOpt).flatMap(toDollarsOpt).ifPresent(System.out::println);  // Optional.empty
        accountId2.flatMap(findAccountOpt.andThen(extractBalanceOpt).andThen(toDollarsOpt)).ifPresent(System.out::println);  // Optional.empty

        System.out.println("Account 3");  // Id not in map => findAccountOpt returns null
        accountId3.map(findAccountOpt).map(extractBalanceOpt).flatMap(toDollarsOpt).ifPresent(System.out::println);  // 0.0
        accountId3.flatMap(findAccountOpt.andThen(extractBalanceOpt).andThen(toDollarsOpt)).ifPresent(System.out::println);  // 0.0

        System.out.println("Account 4");  //Id associated to null in the map
        accountId4.map(findAccountOpt).map(extractBalanceOpt).flatMap(toDollarsOpt).ifPresent(System.out::println);  // 0.0
        accountId4.flatMap(findAccountOpt.andThen(extractBalanceOpt).andThen(toDollarsOpt)).ifPresent(System.out::println);  // 0.0

        System.out.println("Account 5");  // Account's balance is null
        accountId5.map(findAccountOpt).map(extractBalanceOpt).flatMap(toDollarsOpt).ifPresent(System.out::println);  // 0.0
        accountId5.flatMap(findAccountOpt.andThen(extractBalanceOpt).andThen(toDollarsOpt)).ifPresent(System.out::println);  // 0.0
    }

    /**
     * One might ask: isn't avoiding any handling of `null` inside the functions enough?
     * The answer is no. Even if we refrain from explicitly handling `null`, it doesn't mean it will never be handled.
     * For instance, since we don't check for `null` as part of the deal, we can have `NullPointerException` thrown by
     * the composed function, while mapping the individual functions in a chain would simply return `Optional.empty`.
     * So unfortunately this breaks associativity law, because of exceptions being thrown - even if we don't explicitly
     * add any side effects, we still get some because of `null` not being checked.
     */
    public static void alternativeSolutionNoOptional() {
        Map<Long, Account> bank = new HashMap<>();
        Function<Long, Account> findAccount = id -> bank.get(id);

        Function<Account, Balance> extractBalance = account -> account.getBalance();

        Function<Balance, Double> toDollars = balance -> {
            switch (balance.getCurrency()){
                case DOLLAR:
                    return balance.getAmount();
                case POUND:
                    return balance.getAmount() * 1.3;
                case EURO:
                    return balance.getAmount() * 1.1;
                default:
                    return 0.;
            }
        };

        Optional<Long> accountId1 = Optional.of(1L);
        Optional<Long> accountId2 = Optional.ofNullable(null);
        Optional<Long> accountId3 = Optional.of(3L);
        Optional<Long> accountId4 = Optional.of(4L);
        Optional<Long> accountId5 = Optional.of(5L);

        bank.put(1L, new Account(1L, new Balance(100., Currency.EURO)));
        bank.put(4L, null);
        bank.put(5L, new Account(2L, null));

        System.out.println("Account 1");  // An account properly stored with a non-null balance
        System.out.println(accountId1.map(findAccount).map(extractBalance).map(toDollars));  // 110.0
        System.out.println(accountId1.map(findAccount.andThen(extractBalance).andThen(toDollars)));  // 110.0

        System.out.println("Account 2");  // id == null => Optional.empty
        System.out.println(accountId2.map(findAccount).map(extractBalance).map(toDollars));  // Optional.empty
        System.out.println(accountId2.map(findAccount.andThen(extractBalance).andThen(toDollars)));  // Optional.empty

        System.out.println("Account 3");  // Id not in map => findAccount returns null
        System.out.println(accountId3.map(findAccount).map(extractBalance).map(toDollars));  // Optional.empty
//        System.out.println(accountId3.map(findAccount.andThen(extractBalance).andThen(toDollars)));  // NullPointerException

        System.out.println("Account 4");  //Id associated to null in the map
        System.out.println(accountId4.map(findAccount).map(extractBalance).map(toDollars));  // Optional.empty
//        System.out.println(accountId4.map(findAccount.andThen(extractBalance).andThen(toDollars)));  // NullPointerException

        System.out.println("Account 5");  // Account's balance is null
        System.out.println(accountId5.map(findAccount).map(extractBalance).map(toDollars));  // Optional.empty
//        System.out.println(accountId5.map(findAccount.andThen(extractBalance).andThen(toDollars)));  // NullPointerException
    }

    /**
     * We could of course build up on the previous attempt, and just check for `null` and return it.
     * This is not a meaningful solution. While, in fact, this would allow us to avoid breaking associativity and left
     * identity, it would defy altogether the very purpose of `Optional`: we could as well just remove the overhead
     * and keep dealing with `null` the way we were.
     */
    public static void alternativeNonSolutionNoOptional() {
        Map<Long, Account> bank = new HashMap<>();
        Function<Long, Account> findAccount = id -> bank.get(id);

        Function<Account, Balance> extractBalance = account -> account == null ? null : account.getBalance();

        Function<Balance, Double> toDollars = balance -> {
            if (balance == null) {
                return null;
            } else {
                switch (balance.getCurrency()) {
                    case DOLLAR:
                        return balance.getAmount();
                    case POUND:
                        return balance.getAmount() * 1.3;
                    case EURO:
                        return balance.getAmount() * 1.1;
                    default:
                        return 0.;
                }
            }
        };

        Optional<Long> accountId1 = Optional.of(1L);
        Optional<Long> accountId2 = Optional.ofNullable(null);
        Optional<Long> accountId3 = Optional.of(3L);
        Optional<Long> accountId4 = Optional.of(4L);
        Optional<Long> accountId5 = Optional.of(5L);

        bank.put(1L, new Account(1L, new Balance(100., Currency.EURO)));
        bank.put(4L, null);
        bank.put(5L, new Account(2L, null));

        System.out.println("Account 1");  // An account properly stored with a non-null balance
        System.out.println(accountId1.map(findAccount).map(extractBalance).map(toDollars));  // 110.0
        System.out.println(accountId1.map(findAccount.andThen(extractBalance).andThen(toDollars)));  // 110.0

        System.out.println("Account 2");  // id == null => Optional.empty
        System.out.println(accountId2.map(findAccount).map(extractBalance).map(toDollars));  // Optional.empty
        System.out.println(accountId2.map(findAccount.andThen(extractBalance).andThen(toDollars)));  // Optional.empty

        System.out.println("Account 3");  // Id not in map => findAccount returns null
        System.out.println(accountId3.map(findAccount).map(extractBalance).map(toDollars));  // Optional.empty
        System.out.println(accountId3.map(findAccount.andThen(extractBalance).andThen(toDollars)));  // NullPointerException

        System.out.println("Account 4");  //Id associated to null in the map
        System.out.println(accountId4.map(findAccount).map(extractBalance).map(toDollars));  // Optional.empty
        System.out.println(accountId4.map(findAccount.andThen(extractBalance).andThen(toDollars)));  // NullPointerException

        System.out.println("Account 5");  // Account's balance is null
        System.out.println(accountId5.map(findAccount).map(extractBalance).map(toDollars));  // Optional.empty
        System.out.println(accountId5.map(findAccount.andThen(extractBalance).andThen(toDollars)));  // NullPointerException
    }


    /**
     * We might try to just use `Optional` for the return types. We'll have a type mismatch when we try to compose
     * functions, but nothing we can't solve: we have two ways to compose all the functions:
     * - add `Optional::get` in between any two composed functions.
     * - add `Optional::orElse` with proper alternative parameters.
     * The first solution is equivalent to the case `alternativeSolutionNoOptional`, and will throw `NullPointerException`s.
     * The latter, instead, won't throw exceptions, but we would defer to users the choice of the appropriate default values.
     * What's worse, it would make so easy to pass parameters to `orElse` that would break associativity law again:
     * for example `new Balance(0., Currency.DOLLAR)` - see last example below.
     * (NOTE: in this case we won't technically break the associativity law as the composition includes functions not
     * used in the map chain, but still, the point is that we would fail the expected invariance between chaining and
     * composing functions).
     */
    public static void alternativeSolutionOptionalOutputOnly() {
        Map<Long, Account> bank = new HashMap<>();
        Function<Long, Optional<Account>> findAccount = id -> Optional.ofNullable(bank.get(id));

        Function<Account, Optional<Balance>> extractBalance = account -> Optional.ofNullable(account.getBalance());

        Function<Balance, Optional<Double>> toDollars = balance -> {
            switch (balance.getCurrency()){
                case DOLLAR:
                    return Optional.ofNullable(balance.getAmount());
                case POUND:
                    return Optional.ofNullable(balance.getAmount() * 1.3);
                case EURO:
                    return Optional.ofNullable(balance.getAmount() * 1.1);
                default:
                    return Optional.ofNullable(0.);
            }
        };

        Optional<Long> accountId1 = Optional.of(1L);
        Optional<Long> accountId2 = Optional.ofNullable(null);
        Optional<Long> accountId3 = Optional.of(3L);
        Optional<Long> accountId4 = Optional.of(4L);
        Optional<Long> accountId5 = Optional.of(5L);

        bank.put(1L, new Account(1L, new Balance(100., Currency.EURO)));
        bank.put(4L, null);
        bank.put(5L, new Account(2L, null));

        System.out.println("Account 1");  // An account properly stored with a non-null balance
        System.out.println(accountId1.flatMap(findAccount).flatMap(extractBalance).flatMap(toDollars));  // 110.0
        System.out.println(accountId1.flatMap(findAccount.andThen(Optional::get).andThen(extractBalance).andThen(Optional::get).andThen(toDollars)));  // 110.0
        System.out.println(accountId1.flatMap(
            findAccount
                .andThen(x -> x.orElse(new Account(0L, new Balance(0., Currency.DOLLAR))))
                .andThen(extractBalance)
                .andThen(x -> x.orElse(new Balance(0., Currency.DOLLAR)))
                .andThen(toDollars)));  // 110.0

        System.out.println("Account 2");  // id == null => Optional.empty
        System.out.println(accountId2.flatMap(findAccount).flatMap(extractBalance).flatMap(toDollars));  // Optional.empty
        System.out.println(accountId2.flatMap(
            findAccount.andThen(Optional::get)
                .andThen(extractBalance)
                .andThen(Optional::get)
                .andThen(toDollars)));  // Optional.empty
        System.out.println(accountId2.flatMap(
                findAccount
                        .andThen(x -> x.orElse(new Account(0L, null)))
                        .andThen(extractBalance)
                        .andThen(x -> x.orElse(new Balance(null, Currency.DOLLAR)))
                        .andThen(toDollars)));  // Optional.empty

        System.out.println("Account 3");  // Id not in map => findAccount returns null
        System.out.println(accountId3.flatMap(findAccount).flatMap(extractBalance).flatMap(toDollars));  // Optional.empty
//        System.out.println(accountId3.flatMap(
//            findAccount.andThen(Optional::get)
//                .andThen(extractBalance)
//                .andThen(Optional::get)
//                .andThen(toDollars)));  // NullPointerException
        System.out.println(accountId3.flatMap(
                findAccount
                        .andThen(x -> x.orElse(new Account(0L, null)))
                        .andThen(extractBalance)
                        .andThen(x -> x.orElse(new Balance(null, Currency.DOLLAR)))
                        .andThen(toDollars)));  // Optional.empty

        System.out.println("Account 4");  //Id associated to null in the map
        System.out.println(accountId4.flatMap(findAccount).flatMap(extractBalance).flatMap(toDollars));  // Optional.empty
//        System.out.println(accountId4.flatMap(
//                findAccount.andThen(Optional::get)
//                    .andThen(extractBalance)
//                    .andThen(Optional::get)
//                    .andThen(toDollars)));  // NullPointerException
        System.out.println(accountId4.flatMap(
                findAccount
                        .andThen(x -> x.orElse(new Account(0L, null)))
                        .andThen(extractBalance)
                        .andThen(x -> x.orElse(new Balance(null, Currency.DOLLAR)))
                        .andThen(toDollars)));  // Optional.empty

        System.out.println("Account 5");  // Account's balance is null
        System.out.println(accountId5.flatMap(findAccount).flatMap(extractBalance).flatMap(toDollars));  // Optional.empty
//        System.out.println(accountId5.flatMap(
//            findAccount.andThen(Optional::get)
//                .andThen(extractBalance)
//                .andThen(Optional::get)
//                .andThen(toDollars)));  // NullPointerException
        System.out.println(accountId5.flatMap(
                findAccount
                        .andThen(x -> x.orElse(new Account(0L, null)))
                        .andThen(extractBalance)
                        .andThen(x -> x.orElse(new Balance(null, Currency.DOLLAR)))
                        .andThen(toDollars)));  // Optional.empty
        System.out.println(accountId5.flatMap(
                findAccount
                        .andThen(x -> x.orElse(new Account(0L, null)))
                        .andThen(extractBalance)
                        .andThen(x -> x.orElse(new Balance(0., Currency.DOLLAR)))
                        .andThen(toDollars)));  // Optional[0.]
    }


    /**
     * Finally, we might try to use `Optional` only in the input types for the functions in the chain.
     * We'd be forced to insert Optional::ofNullable at every step of the chain and of the composition to make the types work.
     * And the final result would be: we'd break associativity law anyway.
     */
    public static void alternativeSolutionOptionalInputOnly() {
        Map<Long, Account> bank = new HashMap<>();
        Function<Long, Account> findAccount = id -> bank.get(id);

        Function<Optional<Account>, Balance> extractBalance = accountOpt -> {
            Optional<Balance> balanceOpt = accountOpt.map(Account::getBalance);
            return balanceOpt.orElse(new Balance(0., Currency.DOLLAR));
        };

        Function<Optional<Balance>, Double> toDollarsOpt = balanceOpt -> {
            Function<Balance, Double> toDollars = balance -> {
                switch (balance.getCurrency()){
                    case DOLLAR:
                        return balance.getAmount();
                    case POUND:
                        return balance.getAmount() * 1.3;
                    case EURO:
                        return balance.getAmount() * 1.1;
                    default:
                        return 0.;
                }
            };
            Optional<Double> dollarsOpt = balanceOpt.map(toDollars);
            return dollarsOpt.orElse(0.);
        };

        Optional<Long> accountId1 = Optional.of(1L);
        Optional<Long> accountId2 = Optional.ofNullable(null);
        Optional<Long> accountId3 = Optional.of(3L);
        Optional<Long> accountId4 = Optional.of(4L);
        Optional<Long> accountId5 = Optional.of(5L);

        bank.put(1L, new Account(1L, new Balance(100., Currency.EURO)));
        bank.put(4L, null);
        bank.put(5L, new Account(2L, null));

        System.out.println("Account 1");  // An account properly stored with a non-null balance
        System.out.println(accountId1.map(findAccount).map(Optional::ofNullable).map(extractBalance).map(Optional::ofNullable).map(toDollarsOpt));  // 110.0
        System.out.println(accountId1.map(findAccount.andThen(Optional::ofNullable).andThen(extractBalance).andThen(Optional::ofNullable).andThen(toDollarsOpt)));  // 110.0

        System.out.println("Account 2");  // id == null => Optional.empty
        System.out.println(accountId2.map(findAccount).map(Optional::ofNullable).map(extractBalance).map(Optional::ofNullable).map(toDollarsOpt));  // Optional.empty
        System.out.println(accountId2.map(findAccount.andThen(Optional::ofNullable).andThen(extractBalance).andThen(Optional::ofNullable).andThen(toDollarsOpt)));  // Optional.empty

        System.out.println("Account 3");  // Id not in map => findAccount returns null
        System.out.println(accountId3.map(findAccount).map(Optional::ofNullable).map(extractBalance).map(Optional::ofNullable).map(toDollarsOpt));  // Optional.empty
        System.out.println(accountId3.map(findAccount.andThen(Optional::ofNullable).andThen(extractBalance).andThen(Optional::ofNullable).andThen(toDollarsOpt)));  // 0.0

        System.out.println("Account 4");  //Id associated to null in the map
        System.out.println(accountId4.map(findAccount).map(Optional::ofNullable).map(extractBalance).map(Optional::ofNullable).map(toDollarsOpt));  // Optional.empty
        System.out.println(accountId4.map(findAccount.andThen(Optional::ofNullable).andThen(extractBalance).andThen(Optional::ofNullable).andThen(toDollarsOpt)));  // 0.0

        System.out.println("Account 5");  // Account's balance is null
        System.out.println(accountId5.map(findAccount).map(Optional::ofNullable).map(extractBalance).map(Optional::ofNullable).map(toDollarsOpt));  // 0.0
        System.out.println(accountId5.map(findAccount.andThen(Optional::ofNullable).andThen(extractBalance).andThen(Optional::ofNullable).andThen(toDollarsOpt)));  // 0.0
    }

    private static class Account {
        private Long id;
        Balance balance;

        public Account(Long id, Balance balance) {
            this.id = id;
            this.balance = balance;
        }

        public Long getId() {
            return id;
        }

        public Balance getBalance() {
            return balance;
        }
    }

    private enum Currency {
        DOLLAR, POUND, EURO
    }

    private static class Balance {
        private Double amount;
        private Currency currency;

        public Balance(Double amount, Currency currency) {
            this.amount = amount;
            this.currency = currency;
        }

        public Balance(Double amount) {
            this(amount, Currency.DOLLAR);
        }

        public Double getAmount() {
            return amount;
        }

        public Currency getCurrency() {
            return currency;
        }
    }

}
