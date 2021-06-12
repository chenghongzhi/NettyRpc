package com.app.test.client;

import com.google.common.base.Stopwatch;
import com.netty.rpc.client.handler.RpcFuture;
import com.netty.rpc.client.RpcClient;
import com.netty.rpc.client.proxy.RpcService;
import com.app.test.service.HelloService;

import java.util.concurrent.TimeUnit;

/**
 * Created by luxiaoxun on 2016/3/16.
 */
public class RpcAsyncTest {
    public static void main(String[] args) throws InterruptedException {
        final RpcClient rpcClient = new RpcClient("localhost:2181");

        int threadNum = 1;
        final int requestNum = 100;
        Thread[] threads = new Thread[threadNum];

        Stopwatch stopwatch = Stopwatch.createStarted();
        //benchmark for async call
        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < requestNum; i++) {
                        try {
                            RpcService client = RpcClient.createAsyncService(HelloService.class, "2.0");
                            RpcFuture helloFuture = client.call("hello", Integer.toString(i));
                            String result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
                            if (!result.equals("Hi " + i)) {
                                System.out.println("error = " + result);
                            } else {
                                System.out.println("result = " + result);
                            }
                            try {
                                Thread.sleep(5 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        long time = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        String msg = String.format("Async call total-time-cost:%sms, req/s=%s", time, ((double) (requestNum * threadNum)) / time * 1000);
        System.out.println(msg);

        rpcClient.stop();

    }
}
