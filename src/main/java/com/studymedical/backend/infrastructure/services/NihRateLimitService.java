package com.studymedical.backend.infrastructure.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NihRateLimitService {

    // 🔥 Límite global hacia NIH (no saturar - 3 requests simultáneos)
    private final Semaphore nihLimiter = new Semaphore(3);

    public <T> T executeWithLimit(String operation, java.util.concurrent.Callable<T> task) throws Exception {
        boolean acquired = false;
        try {
            // Esperar máximo 10 segundos
            acquired = nihLimiter.tryAcquire(10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new NihRateLimitException("NIH rate limit alcanzado para: " + operation);
            }
            return task.call();
        } finally {
            if (acquired) {
                nihLimiter.release();
            }
        }
    }

    public void executeWithLimitVoid(String operation, java.lang.Runnable task) {
        boolean acquired = false;
        try {
            acquired = nihLimiter.tryAcquire(10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new NihRateLimitException("NIH rate limit alcanzado para: " + operation);
            }
            task.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NihRateLimitException("Interrumpido esperando límite NIH");
        } finally {
            if (acquired) {
                nihLimiter.release();
            }
        }
    }
}
