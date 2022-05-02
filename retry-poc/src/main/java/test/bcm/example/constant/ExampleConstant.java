package test.bcm.example.constant;

import lombok.Getter;

public class ExampleConstant {

    public static final String DEVICE_KTABLE_EVENT_STORE = "example-ktable-device-output";

    public enum TypeEnum {
        DEVICE("device");

        @Getter
        private final String value;

        TypeEnum(String value) {
            this.value = value;
        }

        public static TypeEnum fromValue(String value) {
            for (TypeEnum action : TypeEnum.values()) {
                if (action.getValue().equalsIgnoreCase(value)) {
                    return action;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }


}
