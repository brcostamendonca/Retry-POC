package test.bcm.common.errorhandling.exceptions;

public class EntityMissingInStateStoreRetryableException extends RuntimeException {

    public EntityMissingInStateStoreRetryableException() {
        super();
    }

    public EntityMissingInStateStoreRetryableException(String s) {
        super(s);
    }
}
