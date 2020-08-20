package blockchain;

import java.util.concurrent.TimeUnit;

public class MessageCreator implements Runnable {

    /** The maximum length of the generated message */
    final static int MAX_MESSAGE_LENGTH = 10;

    /** The number of milliseconds this thread should wait after each message */
    final static int THREAD_SLEEP_TIME_MS = 100;

    /** The random generator to get messages */
    final RandomGenerator random = new RandomGenerator();

    /** The user responsible for creating the messages */
    final User user;

    /** The messages will be added onto this blockchain */
    final Blockchain blockchain;

    public MessageCreator(User user, Blockchain blockchain) {
        this.user = user;
        this.blockchain = blockchain;
    }

    @Override
    public void run() {
        boolean getNewMessage = true;
        String messageValue = null;
        while(blockchain.canAddNewBlock()) {
            if(getNewMessage) messageValue = random.generate(1, MAX_MESSAGE_LENGTH);
            long messageId = blockchain.getNextMessageId();
            Message message = new Message(
                    messageId, user.id, messageValue, user.getSignature(messageValue, messageId),
                    user.publicKey
            );
            boolean success = blockchain.tryAddMessage(message);

            // If we couldn't add a new message, then try again with the same message
            if(!success) {
                getNewMessage = false;
                continue;
            } else {
                getNewMessage = true;
            }

            try {
                // Sleep after adding a message so the miners have time to create a block
                TimeUnit.MILLISECONDS.sleep(THREAD_SLEEP_TIME_MS);
            }
            catch (InterruptedException e){
                // We do not expect this exception, so lets log it
                System.out.println(e.getMessage());
            }
        }
    }
}
