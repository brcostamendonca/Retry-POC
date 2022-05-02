package test.bcm.common.errorhandling.retry;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import test.bcm.common.constant.GlobalConstant;
import test.bcm.common.errorhandling.exceptions.ServerResponseException;
import test.bcm.common.errorhandling.exceptions.UnexpectedResponseException;
import test.bcm.common.kafka.client.StateStoreClient;
import test.bcm.common.kafka.template.FetchRequestTemplate;

@Slf4j
@NoArgsConstructor
@Service
public abstract class RetryOnOtherInstanceStoreImpl<T> implements RetryOnOtherInstanceStore<T> {

    private static final String STATE_STORE_SHUTDOWN = GlobalConstant.STATE_STORE_SHUTDOWN;
    private int countFailedCallTimes;

    @Override
    public T doWithRetry(StateStoreClient<T> client, FetchRequestTemplate request, String host, int port) {
        countFailedCallTimes = RetrySynchronizationManager.getContext().getRetryCount();
        log.warn("--- {} Attempt to get entity: {} in other instance {}:{} ---", countFailedCallTimes, request.getId(), host, port);
        return client.invoke(request.getId(), request.getType(), host, port);
    }

    @Override
    public T recover(InvalidStateStoreException ex, StateStoreClient<T> client, FetchRequestTemplate request, String host, int port) {
        log.error("--- Could not get entity {} in other instance {}:{} ---", request.getId(), host, port);
        log.warn(STATE_STORE_SHUTDOWN);
        System.exit(1);
        return null;
    }

    @Override
    public T recover(ServerResponseException ex, StateStoreClient<T> client, FetchRequestTemplate request, String host, int port) {
        log.error("--- Could not get entity {} in other instance {}:{} due to a server response error: {} ---", request.getId(), host, port,
                  ex.getMessage());
        log.warn(STATE_STORE_SHUTDOWN);
        System.exit(1);
        return null;
    }

    @Override
    public T recover(UnexpectedResponseException ex, StateStoreClient<T> client, FetchRequestTemplate request, String host, int port) {
        log.error("--- Could not get entity {} in other instance {}:{} due to an unexpected response: {} ---", request.getId(), host, port,
                  ex.getMessage());
        log.warn(STATE_STORE_SHUTDOWN);
        System.exit(1);
        return null;
    }

    @Override
    public T recover(Exception ex, StateStoreClient<T> client, FetchRequestTemplate request, String host, int port) {
        log.error("--- Could not get entity {} in other instance {}:{} due to an Unexpected Exception: {} ---", request.getId(), host, port,
                  ex.getMessage());
        log.warn(STATE_STORE_SHUTDOWN);
        System.exit(1);
        return null;
    }

    @Override
    public int getFailedCallTimes() {
        return countFailedCallTimes;
    }
}
