package com.lightbend.akka.sample.example.startstopactor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class StartStopActorMain {
    public static void main(String[] args) {
        //创建主actor
        ActorSystem system = ActorSystem.create("startStopActor");
        //创建firstActor
        ActorRef first = system.actorOf(StartStopActor1.props(), "first");
        //向firstActor发送消息，消息内容为stop
        first.tell("stop",ActorRef.noSender());

    }
}
