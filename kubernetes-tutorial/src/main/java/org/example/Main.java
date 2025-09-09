package org.example;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import jakarta.inject.Singleton;
import org.example.db.MongoClientProvider;


import java.net.URI;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        URI baseUri = URI.create("http://0.0.0.0:" + port + "/");

        ResourceConfig rc = new ResourceConfig()
                .packages(
                        "org.example.resources",
                        "org.glassfish.jersey.jackson"
                )
                .register(new AbstractBinder() {
                    @Override protected void configure() {
                        bindFactory(MongoClientProvider.class)
                                .to(com.mongodb.client.MongoClient.class)
                                .in(Singleton.class);

                        bind(org.example.daos.BottleDaoMongo.class)
                                .to(org.example.daos.BottleDaoMongo.class)
                                .in(jakarta.inject.Singleton.class);
                        bind(org.example.services.BottleService.class)
                                .to(org.example.services.BottleService.class)
                                .in(jakarta.inject.Singleton.class);

                        bind(org.example.daos.CrateDaoMongo.class)
                                .to(org.example.daos.CrateDaoMongo.class)
                                .in(jakarta.inject.Singleton.class);
                        bind(org.example.services.CrateService.class)
                                .to(org.example.services.CrateService.class)
                                .in(jakarta.inject.Singleton.class);

                        bind(org.example.daos.BeverageDaoMongo.class)
                                .to(org.example.daos.BeverageDaoMongo.class)
                                .in(jakarta.inject.Singleton.class);
                        bind(org.example.services.BeverageService.class)
                                .to(org.example.services.BeverageService.class)
                                .in(jakarta.inject.Singleton.class);
                    }
                });

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, rc, false);
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        server.start();

        System.out.printf("Beverage Service running at http://localhost:%d%n", port);
        Thread.currentThread().join();
    }
}
