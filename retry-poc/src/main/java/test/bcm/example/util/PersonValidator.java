package test.bcm.example.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import test.bcm.common.errorhandling.exceptions.EntityMissingInStateStoreRetryableException;
import test.bcm.common.model.input.PersonInput;
import test.bcm.common.model.output.DeviceOutput;
import test.bcm.example.constant.ExampleConstant;
import test.bcm.example.kafka.service.DeviceLocalStateStoreService;

@Slf4j
@Service
public class PersonValidator {

    DeviceLocalStateStoreService stateStoreService;

    public PersonValidator(DeviceLocalStateStoreService stateStoreService) {
        this.stateStoreService = stateStoreService;
    }

    public boolean validateIfDeviceExistsInStateStore(PersonInput personInput) {
        if (personInput.getDeviceId() == null) {
            String errorMessage = "--- Could not process PersonInput as deviceId is null ---";
            log.warn(errorMessage);
            return false;
        }

        DeviceOutput deviceOutput = stateStoreService.fetchByIdAndType(
            personInput.getDeviceId(),
            ExampleConstant.TypeEnum.DEVICE.getValue()
        );

        if (deviceOutput == null) {
            log.warn("Could not find device {} in state store. Retrying...", personInput.getDeviceId());
            throw new EntityMissingInStateStoreRetryableException(
                "Could not find device " + personInput.getDeviceId() + " in state store. Retrying...");
        }

        return true;
    }

}
