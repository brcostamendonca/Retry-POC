# Retry-POC

## PROBLEM

    I am currently working on a microservice using event-driven architecture, and I am facing some issues with error handling.
    I am running 2 instances of the microservice, and the topic has 3 partitions.

    When an event is received out of order, (due to missing crucial information of other entity on the state store), we are trying to retry infinitly, using the @Retryable annotation, locking the partition until we have all required information.
    However, after 5 minutes retrying a rebalance occurrs and other instance starts processing the same event, whereas the first instance is still retrying (so we have 2 instances processing the same event).

    Facing this issue, if we instead change the approach to use the kubernetes automatic instance restart to achieve infinite retries, we face a different problem.
    If we kill the first intance (stuck on retries) before it reaches 5 minutes, the second instance will start processing the event correctly.
    The first instance will reboot because it is being hosted in a kubernetes environment, that requires 2 intances.
    However, after the first instance reboots, the second is stuck on retries (and cant rebalance? it spams "Attempt to heartbeat failed since group is rebalancing") and might try to get the missing information from the first instance's state store.
    This will result in a HostInfo{host='unavailable', port=-1} in the first instance (rebooting), and an error retrieving the state store in the second instance (processing the event) on every retry, until it reaches the max retries of the state store connection specified in application properties and kills the second instance.

    The expected behaviour would be:
    Even if the second instance tries to access the first instance's state store and the first try returns state store unavailable, after some retries the state store should be available.

**What is the correct and safe way to retry infinitly?**


## CONTEXT:

    This POC contains 2 entities, Person and Device.
    The Person entity can have a link to a device, if so the device should exist in the state store (if it does not exits then we should retry until it does).

    The POC contains 4 topics with 3 partitions:
        - bcm.test.queuing.person.in
        - bcm.test.queuing.person.out
        - bcm.test.queuing.device.in
        - bcm.test.queuing.device.out

    The POC processes events from the topics "in" and writes to the "out" topics.
    The person processing simply maps the PersonInput to PersonOutput (which happen to have the same structure), and verifies if the device exists in the state store.
    The device processing maps the DeviceInput to DeviceOutput (which also have the same structure), it also counts device changes other than its creation, using the state store.



## REPLICATION INSTRUCTIONS:

    Everything should work fine if all persons' devices previously exist in the state store.
    However if we attempt to link devices that haven't been created yet we run into some problems.

    To replicate:
        1. Run 2 instances of the RetryApplication.
            Run the second instance with VM options: -Dserver.port=9201 -Dbinder.configuration.application-server=localhost:9201
            When booting each instance make sure to use diferent state store locations on application.properties spring.kafka.streams.state-dir.
        2. Send "Create Person 1" request before creating the device.
            A instance will start trying to process the event (lets assume it is the first instance) and it will retry 9 times, each with 30 seconds delay (as specified on application.properties "Retry Device Processing" section).
            After 9 retries failed that instance will die, and the second instance will rebalance and try to process the event.
        3. While the second instance retries, reboot the first instance (to simulate kubernetes environment).
            Also, delete the state store location used previously for the first instance, so it creates a clean one.


    Actual result:
        If the second instance tries to access the first instance's state store it will result in a ERROR 500 INTERNAL_SERVER_ERROR, and the first instance will obtain a HostInfo{host='unavailable', port=-1} when trying to access its state store.
        The second instance will spam "Attempt to heartbeat failed since group is rebalancing", not sure why.

    Expected result:
        Even if the second instance tries to access the first instance's state store and the first try returns state store unavailable, after some retries the state store should be available.
        Although, even if we retrieve the state store successfully, the device should not exist in the state store (as it has not been created yet), and the second instance should retry the person processing.


    If we change the retry.kafka.process-device.max-retry to 500, or any number which will cause retries for more than 5 minutes / max pool timeout, then after 5 minutes there will be a rebalance and both instances will try to process the event (at the same time!).



## USEFULL REQUESTS:

    - Create Person 1

        curl --location --request POST 'http://localhost:8082/topics/bcm.test.queuing.person.in' \
        --header 'Content-Type: application/vnd.kafka.json.v2+json' \
        --header 'Accept: application/vnd.kafka.v2+json' \
        --data-raw '{
            "records": [
                {
                    "key": "test-person-1",
                    "value": {
                        "name": "Bruno",
                        "job": "developer",
                        "deviceId": "test-device-1"            
                    }
                }
            ]
        }'    

    - Create Device 1

        curl --location --request POST 'http://localhost:8082/topics/bcm.test.queuing.device.in' \
        --header 'Content-Type: application/vnd.kafka.json.v2+json' \
        --header 'Accept: application/vnd.kafka.v2+json' \
        --data-raw '{
            "records": [
                {
                    "key": "test-device-1",
                    "value": {
                        "id": "test-device-1",
                        "description": "HP laptop i7 16gb ram"            
                    }
                }
            ]
        }'

    - Check if device exists in state store

        curl --location --request GET 'http://localhost:9200/stateStore-api/example/v1/device/test-device-1?type=device'

