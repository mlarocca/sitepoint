package com.company;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class Test {
  
  public static void main(String[] args) {
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
