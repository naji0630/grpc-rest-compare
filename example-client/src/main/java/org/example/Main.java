package org.example;

import com.example.helloworld.GreeterGrpc;
import com.example.helloworld.HelloWorldProto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.example.RandomStringGenerator.generateRandomStringForByteSize;

public class Main {

    private static final int GRPC_PORT = 50051;
    private static final int REST_PORT = 50052;

    //control factor : (1) data size, (2) request count
    private static final int DATA_SIZE = 100;
    private static final int REQUEST_COUNT = 100000;

    public static void main(String[] args) throws IOException {
        long grpcTime = 0;
        long restTime = 0;
        String name = generateRandomStringForByteSize(DATA_SIZE);

        // prepare for gRPC
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", GRPC_PORT)
                .usePlaintext()
                .build();
        GreeterGrpc.GreeterBlockingStub stub = GreeterGrpc.newBlockingStub(channel);
        HelloWorldProto.HelloRequest grpcRequest = HelloWorldProto.HelloRequest.newBuilder().setName(name).build();

        // prepare for REST
        OkHttpClient client = new OkHttpClient();
        String url = "http://localhost:" + REST_PORT + "/hello?name=" + name;
        Request restRequest = new Request.Builder()
                .url(url)
                .build();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            grpcTime += callGrpc(stub, grpcRequest);
            restTime += callRest(client, restRequest);
        }
        channel.shutdown();
        System.out.println("Data size : " + name.getBytes(StandardCharsets.UTF_8).length);
        System.out.println("Request count : " + REQUEST_COUNT);

        System.out.println("grpc time : " + grpcTime);
        System.out.println("rest time : " + restTime);

    }

    private static long callGrpc(GreeterGrpc.GreeterBlockingStub stub, HelloWorldProto.HelloRequest grpcRequest) {
        long grpcStart = System.currentTimeMillis();
        HelloWorldProto.HelloReply grpcResponse = stub.sayHello(grpcRequest);
        System.out.println("gRPC response from server: " + grpcResponse.getMessage());
        long grpcEnd = System.currentTimeMillis();
        System.out.println("gRPC elapsed time: " + (grpcEnd - grpcStart) + " ms");
        return grpcEnd - grpcStart;
    }

    private static long callRest(OkHttpClient client, Request restRequest) throws IOException {
        long restStart = System.currentTimeMillis();
        try (Response restResponse = client.newCall(restRequest).execute()) {
            if (restResponse.isSuccessful()) {
                System.out.println("REST response from server: " + restResponse.body().string());
            } else {
                System.out.println("REST call failed with code: " + restResponse.code());
            }
        }
        long restEnd = System.currentTimeMillis();
        System.out.println("REST elapsed time: " + (restEnd - restStart) + " ms");
        return restEnd - restStart;
    }
}