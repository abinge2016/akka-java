package com.lightbend.akka.sample.example.iot;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * @package com.lightbend.akka.sample.example.iot
 * @description 设备管理员
 * @author abinge
 * @date 2019/1/4 11:36
 * @version 1.0.0
 * <p>Copyright Copyright (c) 2019/1/4</p>
 */
public class DeviceManager extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props(){
        return Props.create(DeviceManager.class,DeviceManager::new);
    }


    public static final class RequestTrackDevice{
        public final String groupId;
        public final String deviceId;
        public RequestTrackDevice(String groupId, String deviceId) {
            this.groupId = groupId;
            this.deviceId = deviceId;
        }
    }

    public static final class DeviceRegistered{

    }


    @Override
    public Receive createReceive() {
        return null;
    }
}
