package blockchain;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Blockchain blockchain = new Blockchain();

        int blockCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(blockCount);
        for (int minerId = 1; minerId <= blockCount; minerId++) {
            executorService.submit(new Miner(minerId, blockchain, blockCount));
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println(blockchain.toString());
    }
}
