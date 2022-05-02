package test.bcm.example.kafka.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Reducer;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;
import test.bcm.common.model.input.DeviceInput;
import test.bcm.common.model.output.DeviceOutput;
import test.bcm.example.constant.ExampleConstant;
import test.bcm.example.kafka.reducer.DeviceReducer;
import test.bcm.example.kafka.service.ProcessDeviceService;
import test.bcm.example.kafka.service.ProcessDeviceServiceImpl;
import test.bcm.example.util.CustomDeviceJsonDeserializer;

import java.util.function.Function;

@Service
@Slf4j
public class DeviceProcessor {

    private final ProcessDeviceService processDeviceService;
    private final Reducer<DeviceOutput> reducer;

    public DeviceProcessor(ProcessDeviceServiceImpl processService, DeviceReducer reducer) {
        this.processDeviceService = processService;
        this.reducer = reducer;
    }

    @Bean
    public Function<KStream<String, DeviceInput>, KStream<String, DeviceOutput>> processDeviceEvent() {

        return input -> input
            .map(processDeviceService::inputToOutput)
            .groupByKey(Grouped.with(Serdes.String(), Serdes.serdeFrom(new JsonSerializer<>(),
                                                                       new CustomDeviceJsonDeserializer())))
            .reduce(reducer, Materialized
                .<String, DeviceOutput, KeyValueStore<Bytes, byte[]>>as(ExampleConstant.DEVICE_KTABLE_EVENT_STORE)
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.serdeFrom(new JsonSerializer<>(), new CustomDeviceJsonDeserializer())))
            .toStream()
            .peek((key, value) -> {
                log.info("Successfully processed device with key: {} and value: {}", key, value);
            });

    }

}