package org.example.daos;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.example.dto.beverageDTOs.UnifiedBeverage;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;

@Singleton
public class BeverageDaoMongo {

    public static final class PagedResult<T> {
        public final List<T> items; public final long total; public final int page; public final int perPage;
        public PagedResult(List<T> items, long total, int page, int perPage) {
            this.items = items; this.total = total; this.page = page; this.perPage = perPage;
        }
    }

    private final MongoCollection<Document> col;

    @Inject
    public BeverageDaoMongo(MongoClient client) {
        String dbName = System.getenv().getOrDefault("MONGO_DB", "beverage");
        MongoDatabase db = client.getDatabase(dbName);
        this.col = db.getCollection("beverages");
    }

    public PagedResult<UnifiedBeverage> findFiltered(
            boolean inStockOnly, String name, Double minPrice, Double maxPrice, int page, int perPage) {

        List<org.bson.conversions.Bson> fs = new ArrayList<>();
        if (name != null && !name.isBlank()) {
            fs.add(regex("name", Pattern.compile(Pattern.quote(name), Pattern.CASE_INSENSITIVE)));
        }
        if (minPrice != null || maxPrice != null) {
            var or = new ArrayList<org.bson.conversions.Bson>();
            or.add(and(gteIf("pricePerBottle", minPrice), lteIf("pricePerBottle", maxPrice)));
            or.add(and(gteIf("pricePerCrate",  minPrice), lteIf("pricePerCrate",  maxPrice)));
            or.add(and(gteIf("price",          minPrice), lteIf("price",          maxPrice))); // legacy field fallback
            or.removeIf(Objects::isNull);
            fs.add(Filters.or(or));
        }
        if (inStockOnly) {
            fs.add(Filters.or(
                    gt("bottlesInStock", 0),
                    gt("totalBottlesInCrates", 0)
            ));
        }

        var filter = fs.isEmpty() ? new Document() : and(fs);
        long total = col.countDocuments(filter);

        int skip = Math.max(0, (page - 1) * perPage);
        List<Document> docs = col.find(filter)
                .sort(Sorts.ascending("name"))
                .skip(skip)
                .limit(perPage)
                .into(new ArrayList<>());

        List<UnifiedBeverage> items = new ArrayList<>();
        for (Document d : docs) items.add(toUnified(d));

        return new PagedResult<>(items, total, page, perPage);
    }

    public Optional<UnifiedBeverage> findByBottleId(int bottleId) {
        Document d = col.find(eq("bottleId", bottleId)).first();
        return Optional.ofNullable(d == null ? null : toUnified(d));
    }

    private static org.bson.conversions.Bson gteIf(String field, Double v) {
        return v == null ? null : gte(field, v);
    }
    private static org.bson.conversions.Bson lteIf(String field, Double v) {
        return v == null ? null : lte(field, v);
    }
    private static Double num(Object v) {
        if (v == null) return null;
        if (v instanceof Decimal128) return ((Decimal128)v).bigDecimalValue().doubleValue();
        if (v instanceof Number) return ((Number)v).doubleValue();
        return new BigDecimal(v.toString()).doubleValue();
    }
    private static Integer asInt(Object v) { return v == null ? null : Integer.valueOf(v.toString()); }
    private static boolean asBool(Object v) { return v != null && Boolean.parseBoolean(v.toString()); }

    private UnifiedBeverage toUnified(Document d) {
        UnifiedBeverage ub = new UnifiedBeverage();
        ub.setName(d.getString("name"));
        ub.setBottleId(d.getInteger("bottleId"));
        ub.setCrateId(d.get("crateId") == null ? null : d.getInteger("crateId"));
        ub.setVolume(num(d.get("volume")) == null ? 0.0 : num(d.get("volume")));
        Double ppb = num(d.get("pricePerBottle"));
        Double ppc = num(d.get("pricePerCrate"));
        ub.setPricePerBottle(ppb == null ? 0.0 : ppb);
        ub.setPricePerCrate(ppc);
        ub.setBottlesInStock(asInt(d.get("bottlesInStock")) == null ? 0 : asInt(d.get("bottlesInStock")));
        ub.setCratesInStock(asInt(d.get("cratesInStock")));
        ub.setTotalBottlesInCrates(asInt(d.get("totalBottlesInCrates")));
        ub.setIsAlcoholic(asBool(d.get("isAlcoholic")));
        Double vp = num(d.get("volumePercent"));
        ub.setVolumePercent(vp == null ? 0.0 : vp);
        return ub;
    }
}
