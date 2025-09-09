package org.example.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.glassfish.hk2.api.Factory;
import jakarta.inject.Singleton;

@Singleton
public class MongoClientProvider implements Factory<MongoClient> {
    private MongoClient client;

    @Override
    public MongoClient provide() {
        if (client == null) {
            String uri = System.getenv().getOrDefault(
                    "MONGO_URI",
                    "mongodb://beverage:beveragepw@mongo-db:27017/beverage?authSource=beverage"
            );
            client = MongoClients.create(uri);
        }
        return client;
    }
    @Override
    public void dispose(MongoClient instance) {
        if (client != null) client.close();
    }
}
