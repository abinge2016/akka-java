package com.lightbend.akka.sample.example;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.testkit.javadsl.TestKit;
import com.lightbend.akka.sample.example.iot.Device;
import com.lightbend.akka.sample.example.iot.DeviceGroup;
import com.lightbend.akka.sample.example.iot.DeviceManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class IotTest {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
        TestKit probe = new TestKit(system);
        ActorRef deviceActor = system.actorOf(Device.props("group", "device"));

        deviceActor.tell(new Device.RecordTemperature(1L,20.0),probe.getRef());
        assertEquals(1L,probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);

        deviceActor.tell(new Device.ReadTemperature(2L),probe.getRef());
        Device.ResponseTemperature response = probe.expectMsgClass(Device.ResponseTemperature.class);
        assertEquals(2L,response.requestId);
        assertEquals(Optional.of(20.0),response.value);

        deviceActor.tell(new Device.RecordTemperature(3L, 55.0), probe.getRef());
        assertEquals(3L, probe.expectMsgClass(Device.TemperatureRecorded.class).requestId);

        deviceActor.tell(new Device.ReadTemperature(4L), probe.getRef());
        Device.ResponseTemperature response2 = probe.expectMsgClass(Device.ResponseTemperature.class);
        assertEquals(4L, response2.requestId);
        assertEquals(Optional.of(55.0), response2.value);
    }

    @Test
    public void testReplyToRegisterationRequests(){
        TestKit probe = new TestKit(system);
        ActorRef device = system.actorOf(Device.props("group", "device"));
        device.tell(new DeviceManager.RequestTrackDevice("group","device"),probe.getRef());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        assertEquals(device, probe.getLastSender());

    }

    @Test
    public void testIgnoreWrongRegistrationRequests(){
        TestKit probe = new TestKit(system);
        ActorRef device = system.actorOf(Device.props("group", "device"));
        device.tell(new DeviceManager.RequestTrackDevice("wrongGroup","device"),probe.getRef());
        probe.expectNoMessage();

        device.tell(new DeviceManager.RequestTrackDevice("group", "wrongDevice"), probe.getRef());
        probe.expectNoMessage();
    }

    @Test
    public void testReisterDeviceActor(){
        TestKit testKit = new TestKit(system);

        ActorRef group = system.actorOf(DeviceGroup.props("group"));
        group.tell(new DeviceManager.RequestTrackDevice("group","device1"),testKit.getRef());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);

        ActorRef device1 = testKit.getLastSender();

        group.tell(new DeviceManager.RequestTrackDevice("group","device2"),testKit.getRef());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef device2 = testKit.getLastSender();
        assertNotEquals(device1, device2);

        device1.tell(new Device.RecordTemperature(0L,1.0),testKit.getRef());
        assertEquals(0L,testKit.expectMsgClass(Device.TemperatureRecorded.class).requestId);

        device2.tell(new Device.RecordTemperature(1L,2.9),testKit.getRef());
        assertEquals(1L,testKit.expectMsgClass(Device.TemperatureRecorded.class).requestId);
    }


    @Test
    public void testIgnoreRequestsForWrongGroupId(){
        TestKit testKit = new TestKit(system);

        ActorRef group = system.actorOf(DeviceGroup.props("group"));
        group.tell(new DeviceManager.RequestTrackDevice("wrongGroup","device1"),testKit.getRef());
        testKit.expectNoMessage();
    }

    @Test
    public void testReturnSameActorForSameDeviceId() {
        TestKit probe = new TestKit(system);
        ActorRef groupActor = system.actorOf(DeviceGroup.props("group"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.getRef());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor1 = probe.getLastSender();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group", "device1"), probe.getRef());
        probe.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef deviceActor2 = probe.getLastSender();
        assertEquals(deviceActor1, deviceActor2);
    }

    @Test
    public void testListActiveDevices(){
        TestKit testKit = new TestKit(system);
        ActorRef group = system.actorOf(DeviceGroup.props("group"));

        group.tell(new DeviceManager.RequestTrackDevice("group","device1"),testKit.getRef());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);

        group.tell(new DeviceManager.RequestTrackDevice("group","device2"),testKit.getRef());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);

        group.tell(new DeviceGroup.RequestDeviceList(0L),testKit.getRef());
        DeviceGroup.ReplyDeviceList replyDeviceList = testKit.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
        assertEquals(0L,replyDeviceList.requestId);
        assertEquals(Stream.of("device1","device2").collect(Collectors.toSet()),replyDeviceList.ids);
    }

    @Test
    public void testListActiveDevicesAfterOneShutsDown(){
        TestKit testKit = new TestKit(system);
        ActorRef group = system.actorOf(DeviceGroup.props("group"));

        group.tell(new DeviceManager.RequestTrackDevice("group","device1"),testKit.getRef());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);
        ActorRef device1 = testKit.getLastSender();

        group.tell(new DeviceManager.RequestTrackDevice("group","device2"),testKit.getRef());
        testKit.expectMsgClass(DeviceManager.DeviceRegistered.class);

        group.tell(new DeviceGroup.RequestDeviceList(0L),testKit.getRef());
        DeviceGroup.ReplyDeviceList replyDeviceList = testKit.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
        assertEquals(0L,replyDeviceList.requestId);
        assertEquals(Stream.of("device1","device2").collect(Collectors.toSet()),replyDeviceList.ids);

        testKit.watch(device1);
        //从外部，任何actor都可以通过发送特殊的内置消息PoisonPill来停止，该消息指示actor停止
        device1.tell(PoisonPill.getInstance(),ActorRef.noSender());
        testKit.expectTerminated(device1);

        // using awaitAssert to retry because it might take longer for the groupActor
        // to see the Terminated, that order is undefined
        testKit.awaitAssert(() -> {
            group.tell(new DeviceGroup.RequestDeviceList(1L),testKit.getRef());
            DeviceGroup.ReplyDeviceList r = testKit.expectMsgClass(DeviceGroup.ReplyDeviceList.class);
            assertEquals(1L, r.requestId);
            assertEquals(Stream.of("device2").collect(Collectors.toSet()), r.ids);
            return null;
        });

    }
}
