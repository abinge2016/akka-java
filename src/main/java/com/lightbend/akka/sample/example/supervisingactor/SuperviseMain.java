package com.lightbend.akka.sample.example.supervisingactor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class SuperviseMain {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("supervise");
        ActorRef supervisingActor = system.actorOf(SupervisingActor.props(), "supervising-actor");
        supervisingActor.tell("failChild",ActorRef.noSender());
    }
}
