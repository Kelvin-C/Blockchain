import blockchain.*;
import dataentities.block.record.RecordCreator;
import functionality.random.CurrencyTransferGenerator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CurrencyBlockchain blockchain = new CurrencyBlockchain(100);

        // Create the miners and the message creators
        int recordCreatorCount = 3;
        int minerCount = 15;
        ExecutorService executorService = Executors.newFixedThreadPool(minerCount + recordCreatorCount);
        for (int i = 0; i < recordCreatorCount; i++) {
            executorService.submit(new RecordCreator<>(
                    UserManager.generateNewUser(),
                    blockchain,
                    new CurrencyTransferGenerator()
            ));
        }
        for (int startingNonce = 1; startingNonce <= minerCount; startingNonce++) {
            executorService.submit(new Miner<>(UserManager.generateNewUser(), blockchain, startingNonce, minerCount));
        }

        // Wait for all the miners and message creators to finish
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        if (!blockchain.blocksAreValid()) {
            System.out.println("Blockchain is invalid");
        } else {
            System.out.println(blockchain.toString());
        }
    }
}
