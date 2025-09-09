package org.example.daos;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.Sorts;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bson.Document;
import org.example.model.Bottle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;

@Singleton
public class BottleDaoMongo {

    private final MongoCollection<Document> col;

    @Inject
    public BottleDaoMongo(MongoClient client) {
        String dbName = System.getenv().getOrDefault("MONGO_DB", "beverage");
        MongoDatabase db = client.getDatabase(dbName);
        this.col = db.getCollection("bottles"); // collection name
    }

    public List<Bottle> findAll() {
        List<Bottle> out = new ArrayList<>();
        for (Document d : col.find().sort(Sorts.ascending("id"))) out.add(toBottle(d));
        return out;
    }

    public List<Bottle> findFiltered(Double minPrice, Double maxPrice, String name) {
        List<org.bson.conversions.Bson> fs = new ArrayList<>();
        if (minPrice != null) fs.add(gte("price", minPrice));
        if (maxPrice != null) fs.add(lte("price", maxPrice));
        if (name != null && !name.isBlank())
            fs.add(regex("name", Pattern.compile(Pattern.quote(name), Pattern.CASE_INSENSITIVE)));
        var filter = fs.isEmpty() ? new Document() : and(fs);
        List<Bottle> out = new ArrayList<>();
        for (Document d : col.find(filter).sort(Sorts.ascending("name"))) out.add(toBottle(d));
        return out;
    }

    public List<Bottle> findAlcoholic() {
        return mapAll(col.find(eq("isAlcoholic", true)).sort(Sorts.ascending("name")));
    }

    public List<Bottle> findNonAlcoholic() {
        return mapAll(col.find(eq("isAlcoholic", false)).sort(Sorts.ascending("name")));
    }

    public List<Bottle> findInStock() {
        return mapAll(col.find(gt("inStock", 0)).sort(Sorts.ascending("name")));
    }

    public Optional<Bottle> findById(int id) {
        Document d = col.find(eq("id", id)).first();
        return Optional.ofNullable(d == null ? null : toBottle(d));
    }

    public Optional<Bottle> findByName(String name) {
        // case-insensitive exact match
        Document d = col.find(regex("name", "^" + Pattern.quote(name) + "$", "i")).first();
        return Optional.ofNullable(d == null ? null : toBottle(d));
    }

    public long count() {
        return col.countDocuments();
    }

    public int nextId() {
        Document d = col.find().sort(Sorts.descending("id")).first();
        return d == null ? 1 : d.getInteger("id") + 1;
    }

    public Bottle insert(Bottle b) {
        if (b.getId() == 0) b.setId(nextId());
        col.insertOne(toDoc(b));
        return b;
    }

    public boolean updateById(int id, Bottle updated) {
        var res = col.updateOne(eq("id", id), Updates.combine(
                Updates.set("name", updated.getName()),
                Updates.set("volume", updated.getVolume()),
                Updates.set("isAlcoholic", updated.getIsAlcoholic()),
                Updates.set("volumePercent", updated.getVolumePercent()),
                Updates.set("price", updated.getPrice()),
                Updates.set("supplier", updated.getSupplier()),
                Updates.set("inStock", updated.getInStock())
        ));
        return res.getMatchedCount() > 0;
    }

    public boolean deleteById(int id) {
        return col.deleteOne(eq("id", id)).getDeletedCount() > 0;
    }

    // ---------- Mapping ----------
    private Bottle toBottle(Document d) {
        return new Bottle(
                d.getInteger("id"),
                d.getString("name"),
                d.getDouble("volume"),
                d.getBoolean("isAlcoholic", false),
                d.getDouble("volumePercent") == null ? 0.0 : d.getDouble("volumePercent"),
                d.getDouble("price"),
                d.getString("supplier"),
                d.getInteger("inStock") == null ? 0 : d.getInteger("inStock")
        );
    }

    private Document toDoc(Bottle b) {
        return new Document("id", b.getId())
                .append("name", b.getName())
                .append("volume", b.getVolume())
                .append("isAlcoholic", b.getIsAlcoholic())
                .append("volumePercent", b.getVolumePercent())
                .append("price", b.getPrice())
                .append("supplier", b.getSupplier())
                .append("inStock", b.getInStock());
    }

    private List<Bottle> mapAll(Iterable<Document> docs) {
        List<Bottle> out = new ArrayList<>();
        for (Document d : docs) out.add(toBottle(d));
        return out;
    }
}
