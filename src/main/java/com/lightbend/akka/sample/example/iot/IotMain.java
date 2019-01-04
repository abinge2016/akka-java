package com.lightbend.akka.sample.example.iot;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.IOException;

public class IotMain {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("iot-system");
        try {
            ActorRef supervisor = system.actorOf(IotSupervisor.props(), "iot-supervisor");
            System.out.println("Press ENTER to exit the system");
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            system.terminate();
        }
    }
}

