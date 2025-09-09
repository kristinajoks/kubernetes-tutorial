package org.example.daos;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.*;

@Singleton
public class CrateDaoMongo {

    public static final class CrateRecord {
        public int id;
        public int bottleId;
        public int bottlesPerCrate;
        public double price;
        public int inStock;

        public CrateRecord() {}
        public CrateRecord(int id, int bottleId, int bottlesPerCrate, double price, int inStock) {
            this.id = id; this.bottleId = bottleId; this.bottlesPerCrate = bottlesPerCrate;
            this.price = price; this.inStock = inStock;
        }
    }

    private final MongoCollection<Document> col;

    @Inject
    public CrateDaoMongo(MongoClient client) {
        String dbName = System.getenv().getOrDefault("MONGO_DB", "beverage");
        MongoDatabase db = client.getDatabase(dbName);
        this.col = db.getCollection("crates");
    }

    public List<CrateRecord> findAllRecords() {
        List<CrateRecord> out = new ArrayList<>();
        for (Document d : col.find().sort(Sorts.ascending("id"))) out.add(toRec(d));
        return out;
    }

    public Optional<CrateRecord> findByIdRecord(int id) {
        Document d = col.find(eq("id", id)).first();
        return Optional.ofNullable(d == null ? null : toRec(d));
    }

    public Optional<CrateRecord> findByBottleIdRecord(int bottleId) {
        Document d = col.find(eq("bottleId", bottleId)).first();
        return Optional.ofNullable(d == null ? null : toRec(d));
    }

    public List<CrateRecord> findByPriceRangeRecords(Double minPrice, Double maxPrice) {
        List<CrateRecord> out = new ArrayList<>();
        List<org.bson.conversions.Bson> fs = new ArrayList<>();
        if (minPrice != null) fs.add(gte("price", minPrice));
        if (maxPrice != null) fs.add(lte("price", maxPrice));
        var filter = fs.isEmpty() ? new Document() : and(fs);
        for (Document d : col.find(filter).sort(Sorts.ascending("price"))) out.add(toRec(d));
        return out;
    }

    public long count() { return col.countDocuments(); }

    public int nextId() {
        Document d = col.find().sort(Sorts.descending("id")).first();
        return d == null ? 1 : d.getInteger("id") + 1;
    }

    public CrateRecord insertRecord(CrateRecord r) {
        if (r.id == 0) r.id = nextId();
        col.insertOne(toDoc(r));
        return r;
    }

    public boolean updateByIdRecord(int id, CrateRecord r) {
        var res = col.updateOne(eq("id", id), Updates.combine(
                Updates.set("bottleId", r.bottleId),
                Updates.set("bottlesPerCrate", r.bottlesPerCrate),
                Updates.set("price", r.price),
                Updates.set("inStock", r.inStock)
        ));
        return res.getMatchedCount() > 0;
    }

    public boolean deleteById(int id) {
        return col.deleteOne(eq("id", id)).getDeletedCount() > 0;
    }

    private CrateRecord toRec(Document d) {
        return new CrateRecord(
                d.getInteger("id"),
                d.getInteger("bottleId"),
                d.getInteger("bottlesPerCrate"),
                d.getDouble("price"),
                d.getInteger("inStock") == null ? 0 : d.getInteger("inStock")
        );
    }

    private Document toDoc(CrateRecord r) {
        return new Document("id", r.id)
                .append("bottleId", r.bottleId)
                .append("bottlesPerCrate", r.bottlesPerCrate)
                .append("price", r.price)
                .append("inStock", r.inStock);
    }
}
