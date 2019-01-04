package com.lightbend.akka.sample.example.startstopactor;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class StartStopActor1 extends AbstractActor {
    /**
     * 配置方法
     * @return
     */
    static Props props(){
        return Props.create(StartStopActor1.class,StartStopActor1::new);
    }

    /**
     * 在Actor开始之后但在它处理第一条消息之前调用
     */
    @Override
    public void preStart(){
        System.out.println("first started");
        getContext().actorOf(StartStopActor2.props(),"second");
    }

    /**
     * 在Actor停止之前调用。此后不会处理任何消息
     */
    @Override
    public void postStop(){
        System.out.println("first stopped");
    }

    /**
     * 接收消息并处理
     * @return
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .matchEquals("stop",s -> {
                getContext().stop(getSelf());
            }).build();
    }
}
