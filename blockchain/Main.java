package blockchain;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Blockchain blockchain = new Blockchain();

        // Create the miners and the message creators
        int messageCreatorCount = 3;
        int blockCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(blockCount + messageCreatorCount);
        for (int minerId = 1; minerId <= blockCount; minerId++) {
            executorService.submit(new Miner(minerId, blockchain, blockCount));
        }
        for (int i = 0; i < messageCreatorCount; i++) {
            executorService.submit(new MessageCreator(UserManager.generateNewUser(), blockchain));
        }

        // Wait for all the miners and message creators to finish
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        if (!blockchain.blocksAreValid()) {
            System.out.println("Blockchain is invalid");
        } else {
            System.out.println(blockchain.toString());
        }
    }
}
