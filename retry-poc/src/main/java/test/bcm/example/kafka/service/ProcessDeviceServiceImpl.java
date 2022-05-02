package test.bcm.example.kafka.service;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.springframework.stereotype.Service;
import test.bcm.common.model.input.DeviceInput;
import test.bcm.common.model.output.DeviceOutput;
import test.bcm.example.util.ExampleUtils;

@Service
@NoArgsConstructor
@Slf4j
public class ProcessDeviceServiceImpl implements ProcessDeviceService {

    public KeyValue<String, DeviceOutput> inputToOutput(String key, DeviceInput deviceInput) {

        log.warn("--- Attempt to process input {} ---", deviceInput);

        DeviceOutput deviceOutput = DeviceOutput.builder()
                                                .id(deviceInput.getId())
                                                .description(deviceInput.getDescription())
                                                .build();

        log.info("Output: " + deviceOutput);
        return new KeyValue<>(ExampleUtils.fixRestProxyKey(key), deviceOutput);
    }

}

