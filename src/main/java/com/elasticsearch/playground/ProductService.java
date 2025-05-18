package com.elasticsearch.playground;


import co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggester;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import co.elastic.clients.elasticsearch._types.aggregations.*;
@Service
public class ProductService {


    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public Set<String> getSuggest(String keyword) {
        FieldSuggester fieldSuggester = FieldSuggester.of(builder -> builder.prefix(keyword).completion(
                CompletionSuggester.of(csb -> csb.field("name.completion").skipDuplicates(true).size(10))
        ));
        Suggester suggester = Suggester.of(builder -> builder.suggesters("product-suggest", fieldSuggester));

        NativeQuery query = NativeQuery.builder()
                .withSuggester(suggester)
                .withMaxResults(0)
                .withSourceFilter(FetchSourceFilter.of(b -> b.withExcludes("*"))).build();
        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);
        Set<String> values = searchHits.getSuggest().getSuggestion("product-suggest").getEntries().get(0).getOptions()
                .stream().map(Suggest.Suggestion.Entry.Option::getText).collect(Collectors.toSet());
        return values;
    }

    public void boolQuery() {
        Query occasionQuery = Query.of(builder -> builder.term(t -> t.field("occasion").value("")));
        Query colorQuery = Query.of(builder -> builder.term(t -> t.field("color").value("")));
        Query priceRangeQuery = Query.of(builder -> builder.range(RangeQuery.of(r -> r.number(NumberRangeQuery.of(nrq -> nrq.field("price").lte(50d))))));
        Query query = Query.of(q -> q.bool(b -> b.filter(occasionQuery, priceRangeQuery).should(colorQuery)));

        NativeQuery build = NativeQuery.builder().withQuery(query).build();

        SearchHits<Product> search = elasticsearchOperations.search(build, Product.class);
        List<Product> collect = search.stream().map(productSearchHit -> productSearchHit.getContent()).collect(Collectors.toList());
    }

    public void aggregation () {
        Aggregation price = Aggregation.of(builder -> builder.stats(StatsAggregation.of(sb -> sb.field("price"))));
        Aggregation color = Aggregation.of(builder -> builder.terms(TermsAggregation.of(ta -> ta.field("color"))));
        List<AggregationRange> ranges = List.of(AggregationRange.of(b -> b.from(50d)
        ), AggregationRange.of(b -> b.from(60d).to(100d)));

        Aggregation priceRanges = Aggregation.of(builder -> builder.range(RangeAggregation.of(r -> r.field("price").ranges(ranges))));

        NativeQuery query = NativeQuery.builder().withAggregation("priceA", price).withAggregation("color", color).withAggregation("priceRange", priceRanges).build();
        SearchHits<Product> search = elasticsearchOperations.search(query, Product.class);
        List<ElasticsearchAggregation> aggregations = (List<ElasticsearchAggregation>) search.getAggregations().aggregations();

        Map<String, Aggregate> map = aggregations.stream().map(ElasticsearchAggregation::aggregation).collect(Collectors.toMap(
                a -> a.getName(),
                a -> a.getAggregate()
        ));
        if (map.get("color").isSterms()) {
            map.get("color").sterms().buckets().array().stream().map(b -> b.key().stringValue() + " : " + b.docCount());
        }
    }
}
