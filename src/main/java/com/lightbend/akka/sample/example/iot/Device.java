package com.lightbend.akka.sample.example.iot;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Optional;
/**
 * @package com.lightbend.akka.sample.example.iot
 * @description 设备actor
 * @author abinge
 * @date 2019/1/4 11:37
 * @version 1.0.0
 * <p>Copyright Copyright (c) 2019/1/4</p>
 */
public class Device extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    final String groupId;
    final String deviceId;

    Optional<Double> lastTemperatureReading = Optional.empty();

    public Device(String groupId, String deviceId) {
        this.groupId = groupId;
        this.deviceId = deviceId;
    }

    public static Props props(String groupId,String deviceId){
        return Props.create(Device.class,() -> new Device(groupId,deviceId));
    }

    /**
     * 读取温度的请求对象
     */
    public static final class ReadTemperature{
        final long requestId;

        public ReadTemperature(long requestId) {
            this.requestId = requestId;
        }
    }

    /**
     * 响应读取温度的对象
     */
    public static final class ResponseTemperature {
        public final long requestId;
        public final Optional<Double> value;

        public ResponseTemperature(long requestId, Optional<Double> value) {
            this.requestId = requestId;
            this.value = value;
        }
    }

    /**
     * 温度记录变动的通知对象
     */
    public static final class RecordTemperature{
        public final long requestId;
        public final double value;

        public RecordTemperature(long requestId,double value) {
            this.requestId = requestId;
            this.value = value;
        }
    }

    /**
     * 温度记录变动被记录的响应对象
     */
    public static final class TemperatureRecorded{
        public final long requestId;

        public TemperatureRecorded(long requestId) {
            this.requestId = requestId;
        }
    }

    @Override
    public void preStart() {
        log.info("Device actor {}-{} started", groupId, deviceId);
    }

    @Override
    public void postStop() {
        log.info("Device actor {}-{} stopped", groupId, deviceId);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(DeviceManager.RequestTrackDevice.class,r -> {
                if(this.groupId.equals(r.groupId) && this.deviceId.equals(r.deviceId)){
                    getSender().tell(new DeviceManager.DeviceRegistered(),getSelf());
                }else{
                    log.warning("Ignoring TrackDevice request for {}-{}.This actor is responsible for {}-{}.",
                        r.groupId, r.deviceId, this.groupId, this.deviceId
                    );
                }
            })
            .match(ReadTemperature.class, r -> {
                getSender().tell(new ResponseTemperature(r.requestId,lastTemperatureReading),getSelf());
            })
            .match(RecordTemperature.class,r -> {
                log.info("Recorded temperature reading {} with {}", r.value, r.requestId);
                lastTemperatureReading = Optional.of(r.value);
                getSender().tell(new TemperatureRecorded(r.requestId),getSelf());
            }).build();
    }
}
