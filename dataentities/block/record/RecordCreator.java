package dataentities.block.record;

import blockchain.Blockchain;
import dataentities.user.User;
import functionality.random.RandomGenerator;

import java.util.concurrent.TimeUnit;

public class RecordCreator<T extends RecordValue> implements Runnable {

    /**
     * The number of milliseconds this thread should wait after
     * each record creation so the miners have time to mine
     */
    final static int THREAD_SLEEP_TIME_MS = 10;

    /** The random generator to get messages */
    final RandomGenerator<T> recordGenerator;

    /** The user responsible for creating the messages */
    final User user;

    /** The messages will be added onto this blockchain */
    final Blockchain<T> blockchain;

    public RecordCreator(User user, Blockchain<T> blockchain, RandomGenerator<T> recordGenerator) {
        this.user = user;
        this.blockchain = blockchain;
        this.recordGenerator = recordGenerator;
    }

    @Override
    public void run() {
        while(blockchain.canAddNewBlock()) {
            T recordValue = recordGenerator.generate();
            long messageId = blockchain.getNextRecordId();
            Record<T> record = new Record<>(
                    messageId, user.id, recordValue, user.getSignature(recordValue, messageId),
                    user.publicKey
            );
            blockchain.tryAddRecord(record);

            try {
                // Sleep after adding a record so the miners have time to create a block
                TimeUnit.MILLISECONDS.sleep(THREAD_SLEEP_TIME_MS);
            }
            catch (InterruptedException e){
                // We do not expect this exception, so lets log it
                System.out.println(e.getMessage());
            }
        }
    }
}
