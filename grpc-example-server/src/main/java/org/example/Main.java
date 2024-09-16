package org.example;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.example.greeter.GreeterImpl;

public class Main {
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(50051)
                .addService(new GreeterImpl())
                .build();

        System.out.println("Grpc Server started...");
        server.start();
        server.awaitTermination();
    }
}