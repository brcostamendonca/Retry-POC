package test.bcm.example.kafka.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import test.bcm.common.errorhandling.exceptions.EntityMissingInStateStoreRetryableException;
import test.bcm.common.model.input.PersonInput;
import test.bcm.common.model.output.PersonOutput;
import test.bcm.example.util.ExampleUtils;
import test.bcm.example.util.PersonValidator;

@Service
@Slf4j
public class ProcessPersonServiceImpl implements ProcessPersonService {

    private final PersonValidator validator;

    public ProcessPersonServiceImpl(PersonValidator validator) {
        this.validator = validator;
    }

    public KeyValue<String, PersonOutput> inputToOutput(String key, PersonInput personInput) {
        int countFailedCallTimes = RetrySynchronizationManager.getContext().getRetryCount();
        log.warn("--- {} Attempt to process input {} ---", countFailedCallTimes, personInput);

        if (validator.validateIfDeviceExistsInStateStore(personInput)) {

            PersonOutput personOutput = PersonOutput.builder()
                                                    .name(personInput.getName())
                                                    .job(personInput.getJob())
                                                    .deviceId(personInput.getDeviceId())
                                                    .build();
            log.info("Output: " + personOutput);
            return new KeyValue<>(ExampleUtils.fixRestProxyKey(key), personOutput);
        }
        return new KeyValue<>(null, null); //discard event

    }

    @Override
    public KeyValue<String, PersonOutput> recover(EntityMissingInStateStoreRetryableException e, String key, PersonInput personInput) {
        int countFailedCallTimes = RetrySynchronizationManager.getContext().getRetryCount();
        log.error("--- Could not process {} after {} retries ---", personInput, countFailedCallTimes);
        log.warn("--- The application will shutdown in order to re-balancing ---");
        System.exit(1);
        return null;
    }

}

