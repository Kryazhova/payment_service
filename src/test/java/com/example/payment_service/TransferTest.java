package com.example.payment_service;


import com.example.payment_service.entity.Account;

import com.example.payment_service.model.MoneyTransferRequest;
import com.example.payment_service.service.PaymentTransferService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

public class TransferTest {

    Map<Long, Account> input = new HashMap<>();
    //  Создала метод в before, потому что у меня 8 java и она не поддерживает map.of
    @Before
    public void createAccountList() {
        input.put(1L, new Account(1L, 1_000_000L));
        input.put(2L, new Account(2L, 1_000_000L));
    }


    @Test
    public void concurrentTest() throws InterruptedException, ExecutionException {

        PaymentTransferService service = new PaymentTransferService(input);

        MoneyTransferRequest first = new MoneyTransferRequest(1L, 2L, 1L);
        MoneyTransferRequest second = new MoneyTransferRequest(2L, 1L, 1L);

        List<MoneyTransferTask> taskList = new ArrayList<>();

        for (int i = 0; i < 100000; i++) {
            taskList.add(new MoneyTransferTask(service, first));
            taskList.add(new MoneyTransferTask(service, second));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(20);

        List<Future<Void>> futures = executorService.invokeAll(taskList);

        for (Future<Void> future : futures) {
            future.get();
        }


        Assert.assertEquals(Long.valueOf(1000000), input.get(1L).getBalance());
        Assert.assertEquals(Long.valueOf(1000000), input.get(2L).getBalance());
    }




    private class MoneyTransferTask implements Callable<Void> {
        private PaymentTransferService service;
        private MoneyTransferRequest moneyTransferRequest;

        public MoneyTransferTask(PaymentTransferService service, MoneyTransferRequest moneyTransferRequest) {
            this.service = service;
            this.moneyTransferRequest = moneyTransferRequest;
        }


        @Override
        public Void call() {
            service.transfer(moneyTransferRequest);
            return null;
        }
    }
}
