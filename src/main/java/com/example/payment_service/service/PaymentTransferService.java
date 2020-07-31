package com.example.payment_service.service;


import com.example.payment_service.entity.Account;
import com.example.payment_service.model.MoneyTransferRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class PaymentTransferService {

    private final Map<Long, Account> accounts;
    public ConcurrentHashMap<Long, ReentrantLock> threadLocks = new ConcurrentHashMap<>();

    public PaymentTransferService(Map<Long, Account> accounts) {
        this.accounts = accounts;
    }

    private ReentrantLock getLockerByAccountId(Long accountId) {
        ReentrantLock locker = threadLocks.get(accountId);
        if (locker == null) {
            locker = new ReentrantLock();
            threadLocks.putIfAbsent(accountId, locker);
        }
        return locker;
    }

    public void transfer(MoneyTransferRequest request) {

        Account fromAccount = accounts.get(request.getFrom());
        Account toAccount = accounts.get(request.getTo());

        Account first;
        Account second;

        if (fromAccount.getId() > toAccount.getId()) {
            first = fromAccount;
            second = toAccount;
        } else {
            first = toAccount;
            second = fromAccount;
        }

        ReentrantLock firstLocker = getLockerByAccountId(first.getId());
        ReentrantLock secondLocker = getLockerByAccountId(second.getId());

        firstLocker.lock();
        secondLocker.lock();
        try {
            Long amount = request.getAmount();
            if (fromAccount.getBalance() < amount) {
                throw new RuntimeException("Not enough funds");
            }

            fromAccount.setBalance(fromAccount.getBalance() - amount);
            toAccount.setBalance(toAccount.getBalance() + amount);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            secondLocker.unlock();
            firstLocker.unlock();
        }
    }
}
