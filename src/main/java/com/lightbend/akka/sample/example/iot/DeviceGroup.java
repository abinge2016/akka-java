package com.lightbend.akka.sample.example.iot;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author abinge
 * @version 1.0.0
 * <p>Copyright Copyright (c) 2019</p>
 * @projectName huayu
 * @package com.lightbend.akka.sample.example.iot
 * @description 设备组actor
 * @date 2019/1/4 14:33
 */
public class DeviceGroup extends AbstractActor {

    private final LoggingAdapter  log = Logging.getLogger(getContext().getSystem(),this);

    final String groupId;

    final Map<String, ActorRef> deviceIdToActor = new HashMap<>();
    final Map<ActorRef,String> actorToDeviceId = new HashMap<>();

    public DeviceGroup(String groupId){
        this.groupId = groupId;
    }

    public static Props props(String groupId){
        return Props.create(DeviceGroup.class,() -> new DeviceGroup(groupId));
    }

    @Override
    public void preStart(){
        log.info("DeviceGroup {} started",groupId);
    }

    @Override
    public void postStop(){
        log.info("DeviceGroup {} stopped",groupId);
    }


    public static final class RequestDeviceList{
        public final long requestId;

        public RequestDeviceList(long requestId) {
            this.requestId = requestId;
        }
    }

    public static final class ReplyDeviceList{
        public final long requestId;
        public final Set<String> ids;

        public ReplyDeviceList(long requestId, Set<String> ids) {
            this.requestId = requestId;
            this.ids = ids;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(DeviceManager.RequestTrackDevice.class,this::onTrackDevice)
            .match(Terminated.class,this::onTerminated)
            .match(RequestDeviceList.class,this::onDeviceList)
            .build();
    }

    private void onTrackDevice(DeviceManager.RequestTrackDevice trackMsg){
        if(this.groupId.equals(trackMsg.groupId)){
            ActorRef device = deviceIdToActor.get(trackMsg.deviceId);
            if(null != device){
                device.forward(trackMsg,getContext());
            }else {
                log.info("creating device actor for {}",trackMsg.deviceId);
                device = getContext().getSystem().actorOf(Device.props(this.groupId,trackMsg.deviceId),
                    "device-"+trackMsg.deviceId);
                //添加观察功能，有actor观察组下所有的actor
                getContext().watch(device);
                actorToDeviceId.put(device,trackMsg.deviceId);

                deviceIdToActor.put(trackMsg.deviceId,device);
                device.forward(trackMsg,getContext());
            }
        }else {
            log.warning("Ignoring TrackDevice request for {}. This actor is responsible for {}.",
                groupId, this.groupId
            );
        }
    }

    private void onTerminated(Terminated t){
        ActorRef device = t.getActor();
        String deviceId = actorToDeviceId.get(device);
        log.info("device actor for {} has been terminated",deviceId);
        actorToDeviceId.remove(device);
        deviceIdToActor.remove(deviceId);
    }

    private void onDeviceList(RequestDeviceList r){
        getSender().tell(new ReplyDeviceList(r.requestId,deviceIdToActor.keySet()),getSelf());
    }
}
