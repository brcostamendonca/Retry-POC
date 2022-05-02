package test.bcm.common.model.input;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder = DeviceInput.DeviceInputBuilderImpl.class)
public class DeviceInput implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id;

    @JsonProperty("description")
    private String description;


    @JsonPOJOBuilder(withPrefix = "")
    static final class DeviceInputBuilderImpl extends DeviceInput.DeviceInputBuilder<DeviceInput, DeviceInputBuilderImpl> {

    }

}
