package com.lightbend.akka.sample.example.supervisingactor;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.util.Optional;

public class SupervisedActor extends AbstractActor {

    static Props props(){
        return Props.create(SupervisedActor.class,SupervisedActor::new);
    }

    @Override
    public void preStart(){
        System.out.println("supervised actor started");
    }

    @Override
    public void postStop(){
        System.out.println("supervised actor stopped");
    }

    /**
     * 重新启动之前的调用
     * @param reason
     * @param message
     * @throws Exception
     */
    @Override
    public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
        super.preRestart(reason, message);
        System.out.println("supervised actor preRestart");
    }

    /**
     * 重新启动之后的滴啊用
     * @param reason
     * @throws Exception
     */
    @Override
    public void postRestart(Throwable reason) throws Exception {
        super.postRestart(reason);
        System.out.println("supervised actor postRestart");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals("fail",f -> {
            System.out.println("supervised actor fails now");
            throw new Exception("I failed!");
        }).build();
    }
}
