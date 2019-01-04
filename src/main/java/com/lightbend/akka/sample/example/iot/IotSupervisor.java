package com.lightbend.akka.sample.example.iot;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * 物联网系统中的顶级actor，创建和管理设备和仪表板的组件是此actor的子组件
 */
public class IotSupervisor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(),this);

    public static Props props(){
        return Props.create(IotSupervisor.class,IotSupervisor::new);
    }

    @Override
    public void preStart() {
        log.info("IoT Application started");
    }

    @Override
    public void postStop() {
        log.info("IoT Application stopped");
    }

    /**
     * 不需要处理任何消息
     * @return
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .build();
    }
}
