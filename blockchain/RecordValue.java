package blockchain;

/** Wraps the main data that is stored in a blockchain record */
abstract class RecordValue {

    @Override
    public abstract String toString();

    @Override
    public abstract boolean equals(Object obj);
}
