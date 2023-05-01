package com.example;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

class Process {
    int id;
    boolean active;

    public Process(int id, boolean active) {
        this.id = id;
        this.active = active;
    }
}

class Message {
    int from;
    String type;

    public Message(int from, String type) {
        this.from = from;
        this.type = type;
    }
}

public class BullyWithLeaderAppointment {

    public static AtomicInteger messageCount;

    public static void main(String[] args) {
        List<Process> processes = new ArrayList<>();
        messageCount = new AtomicInteger(0);

        int numProcesses = 15; // Set the desired number of processes here

        // Create processes and add them to the list
        for (int i = 0; i < numProcesses; i++) {
            processes.add(new Process(i, true));
        }

        // Simulate a coordinator failure
        processes.get(numProcesses - 1).active = false;
        System.out.println("Coordinator (Process " + (numProcesses - 1) + ") has failed.");

        // Start the election process
        int newCoordinator = startElection(processes, 1);
        System.out.println("New coordinator is: Process " + newCoordinator);
        System.out.println("Total messages sent: " + messageCount.get());
    }

    public static int startElection(List<Process> processes, int initiator) {
        System.out.println("Election started by Process " + initiator);

        int maxId = -1;

        for (Process process : processes) {
            if (process.id > initiator && process.active) {
                Message electionMessage = new Message(initiator, "ELECTION");
                Message responseMessage = sendMessage(processes, process, electionMessage);

                if (responseMessage != null && "OK".equals(responseMessage.type)) {
                    if (responseMessage.from > maxId) {
                        maxId = responseMessage.from;
                    }
                }
            }
        }

        if (maxId == -1) {
            System.out.println("Process " + initiator + " becomes the new coordinator.");
            for (Process process : processes) {
                if (process.id < initiator && process.active) {
                    Message coordinatorMessage = new Message(initiator, "COORDINATOR");
                    sendMessage(processes, process, coordinatorMessage);
                }
            }
            return initiator;
        } else {
            Message appointMessage = new Message(initiator, "APPOINT");
            sendMessage(processes, processes.get(maxId), appointMessage);
            return maxId;
        }
    }

    public static Message sendMessage(List<Process> processes, Process receiver, Message message) {
        messageCount.incrementAndGet();
        if (!receiver.active) {
            return null;
        }

        if ("ELECTION".equals(message.type)) {
            System.out.println("OK message sent from process: " + receiver.id);
            return new Message(receiver.id, "OK");
        } else if ("COORDINATOR".equals(message.type)) {
            System.out.println("Process " + receiver.id + " acknowledges new coordinator: Process " + message.from);
        } else if ("APPOINT".equals(message.type)) {
            System.out.println("Process " + receiver.id + " is appointed as the new coordinator.");
            for (Process process : processes) {
                if (process.id != receiver.id && process.active) {
                    Message coordinatorMessage = new Message(receiver.id, "COORDINATOR");
                    sendMessage(processes, process, coordinatorMessage);
                }
            }
        }
        return null;
    }
}
