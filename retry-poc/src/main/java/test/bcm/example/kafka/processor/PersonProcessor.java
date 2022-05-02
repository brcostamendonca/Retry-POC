package test.bcm.example.kafka.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import test.bcm.common.model.input.PersonInput;
import test.bcm.common.model.output.PersonOutput;
import test.bcm.example.kafka.service.ProcessPersonService;
import test.bcm.example.kafka.service.ProcessPersonServiceImpl;

import java.util.function.Function;

@Service
@Slf4j
public class PersonProcessor {

    private final ProcessPersonService processPersonService;

    public PersonProcessor(ProcessPersonServiceImpl processService) {
        this.processPersonService = processService;
    }

    @Bean
    public Function<KStream<String, PersonInput>, KStream<String, PersonOutput>> processPersonEvent() {

        return input -> input
            .map(processPersonService::inputToOutput)
            .peek((key, value) -> {
                log.info("Successfully processed person with key: {} and value: {}", key, value);
            });

    }

}