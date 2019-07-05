package org.zalando.logbook.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apiguardian.api.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.security.web.SecurityFilterChain;
import org.zalando.logbook.BodyFilter;
import org.zalando.logbook.BodyOnlyIfStatusAtLeastStrategy;
import org.zalando.logbook.ChunkingSink;
import org.zalando.logbook.Conditions;
import org.zalando.logbook.CurlHttpLogFormatter;
import org.zalando.logbook.DefaultHttpLogFormatter;
import org.zalando.logbook.DefaultHttpLogWriter;
import org.zalando.logbook.DefaultSink;
import org.zalando.logbook.DefaultStrategy;
import org.zalando.logbook.HeaderFilter;
import org.zalando.logbook.HeaderFilters;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.HttpLogWriter;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.PathFilter;
import org.zalando.logbook.PathFilters;
import org.zalando.logbook.QueryFilter;
import org.zalando.logbook.QueryFilters;
import org.zalando.logbook.RequestFilter;
import org.zalando.logbook.ResponseFilter;
import org.zalando.logbook.Sink;
import org.zalando.logbook.SplunkHttpLogFormatter;
import org.zalando.logbook.StatusAtLeastStrategy;
import org.zalando.logbook.Strategy;
import org.zalando.logbook.WithoutBodyStrategy;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;
import org.zalando.logbook.json.JsonHttpLogFormatter;
import org.zalando.logbook.servlet.LogbookFilter;
import org.zalando.logbook.servlet.SecureLogbookFilter;

import javax.servlet.Filter;
import java.util.List;
import java.util.function.Predicate;

import static javax.servlet.DispatcherType.ASYNC;
import static javax.servlet.DispatcherType.REQUEST;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.zalando.logbook.BodyFilters.defaultValue;
import static org.zalando.logbook.BodyFilters.truncate;

@API(status = STABLE)
@Configuration
@ConditionalOnClass(Logbook.class)
@EnableConfigurationProperties(LogbookProperties.class)
@AutoConfigureAfter(value = JacksonAutoConfiguration.class, name = {
        "org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration", // Spring Boot 1.x
        "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration" // Spring Boot 2.x
})
public class LogbookAutoConfiguration {

    private final LogbookProperties properties;

    @API(status = INTERNAL)
    @Autowired
    public LogbookAutoConfiguration(final LogbookProperties properties) {
        this.properties = properties;
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Logbook.class)
    public Logbook logbook(
            final Predicate<HttpRequest> condition,
            final List<HeaderFilter> headerFilters,
            final List<PathFilter> pathFilters,
            final List<QueryFilter> queryFilters,
            final List<BodyFilter> bodyFilters,
            final List<RequestFilter> requestFilters,
            final List<ResponseFilter> responseFilters,
            final Strategy strategy,
            final Sink sink) {

        return Logbook.builder()
                .condition(mergeWithExcludes(mergeWithIncludes(condition)))
                .headerFilters(headerFilters)
                .queryFilters(queryFilters)
                .pathFilters(pathFilters)
                .bodyFilters(bodyFilters)
                .requestFilters(requestFilters)
                .responseFilters(responseFilters)
                .strategy(strategy)
                .sink(sink)
                .build();
    }

    private Predicate<HttpRequest> mergeWithExcludes(final Predicate<HttpRequest> predicate) {
        return properties.getExclude().stream()
                .map(Conditions::requestTo)
                .map(Predicate::negate)
                .reduce(predicate, Predicate::and);
    }

    private Predicate<HttpRequest> mergeWithIncludes(final Predicate<HttpRequest> predicate) {
        return properties.getInclude().stream()
                .map(Conditions::requestTo)
                .reduce(Predicate::or)
                .map(predicate::and)
                .orElse(predicate);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(name = "requestCondition")
    public Predicate<HttpRequest> requestCondition() {
        return $ -> true;
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(QueryFilter.class)
    public QueryFilter queryFilter() {
        final List<String> parameters = properties.getObfuscate().getParameters();
        return parameters.isEmpty() ?
                QueryFilters.defaultValue() :
                parameters.stream()
                        .map(parameter -> QueryFilters.replaceQuery(parameter, "XXX"))
                        .reduce(QueryFilter::merge)
                        .orElseGet(QueryFilter::none);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HeaderFilter.class)
    public HeaderFilter headerFilter() {
        final List<String> headers = properties.getObfuscate().getHeaders();
        return headers.isEmpty() ?
                HeaderFilters.defaultValue() :
                headers.stream()
                        .map(header -> HeaderFilters.replaceHeaders(header::equalsIgnoreCase, "XXX"))
                        .reduce(HeaderFilter::merge)
                        .orElseGet(HeaderFilter::none);
    }
    
    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(PathFilter.class)
    public PathFilter pathFilter() {
        final List<String> paths = properties.getObfuscate().getPaths();
        return paths.isEmpty() ?
                PathFilter.none() :
                paths.stream()
                        .map(path -> PathFilters.replace(path, "XXX"))
                        .reduce(PathFilter::merge)
                        .orElseGet(PathFilter::none);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(BodyFilter.class)
    public BodyFilter bodyFilter() {
        final LogbookProperties.Write write = properties.getWrite();
        final int maxBodySize = write.getMaxBodySize();

        if (maxBodySize < 0) {
            return defaultValue();
        }

        return BodyFilter.merge(truncate(maxBodySize), defaultValue());
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(RequestFilter.class)
    public RequestFilter requestFilter() {
        return RequestFilter.none();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(ResponseFilter.class)
    public ResponseFilter responseFilter() {
        return ResponseFilter.none();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Strategy.class)
    @ConditionalOnProperty(name = "logbook.strategy", havingValue = "default", matchIfMissing = true)
    public Strategy strategy() {
        return new DefaultStrategy();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Strategy.class)
    @ConditionalOnProperty(name = "logbook.strategy", havingValue = "status-at-least")
    public Strategy statusAtLeastStrategy(@Value("${logbook.minimum-status:400}") final int status) {
        return new StatusAtLeastStrategy(status);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Strategy.class)
    @ConditionalOnProperty(name = "logbook.strategy", havingValue = "body-only-if-status-at-least")
    public Strategy bodyOnlyIfStatusAtLeastStrategy(@Value("${logbook.minimum-status:400}") final int status) {
        return new BodyOnlyIfStatusAtLeastStrategy(status);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Strategy.class)
    @ConditionalOnProperty(name = "logbook.strategy", havingValue = "without-body")
    public Strategy withoutBody() {
        return new WithoutBodyStrategy();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(Sink.class)
    public Sink sink(
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final HttpLogFormatter formatter,
            final HttpLogWriter writer) {
        return new DefaultSink(formatter, writer);
    }

    @API(status = INTERNAL)
    @Bean
    @Primary
    @ConditionalOnBean(Sink.class)
    @ConditionalOnProperty("logbook.write.chunk-size")
    public Sink chunkingSink(final Sink sink) {
        return new ChunkingSink(sink, properties.getWrite().getChunkSize());
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "http")
    public HttpLogFormatter httpFormatter() {
        return new DefaultHttpLogFormatter();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "curl")
    public HttpLogFormatter curlFormatter() {
        return new CurlHttpLogFormatter();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "splunk")
    public HttpLogFormatter splunkHttpLogFormatter() {
        return new SplunkHttpLogFormatter();
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnBean(ObjectMapper.class)
    @ConditionalOnMissingBean(HttpLogFormatter.class)
    public HttpLogFormatter jsonFormatter(
            @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final ObjectMapper mapper) {
        return new JsonHttpLogFormatter(mapper);
    }

    @API(status = INTERNAL)
    @Bean
    @ConditionalOnMissingBean(HttpLogWriter.class)
    public HttpLogWriter writer() {
        return new DefaultHttpLogWriter();
    }

    @Configuration
    @ConditionalOnClass({
            HttpClient.class,
            LogbookHttpRequestInterceptor.class,
            LogbookHttpResponseInterceptor.class
    })
    static class HttpClientAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(LogbookHttpRequestInterceptor.class)
        public LogbookHttpRequestInterceptor logbookHttpRequestInterceptor(final Logbook logbook) {
            return new LogbookHttpRequestInterceptor(logbook);
        }

        @Bean
        @ConditionalOnMissingBean(LogbookHttpResponseInterceptor.class)
        public LogbookHttpResponseInterceptor logbookHttpResponseInterceptor() {
            return new LogbookHttpResponseInterceptor();
        }

    }

    @Configuration
    @ConditionalOnClass(Filter.class)
    @ConditionalOnWebApplication
    static class ServletFilterConfiguration {

        private static final String FILTER_NAME = "logbookFilter";

        @Bean
        @ConditionalOnProperty(name = "logbook.filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        public FilterRegistrationBean logbookFilter(final Logbook logbook) {
            return newFilter(new LogbookFilter(logbook), FILTER_NAME, Ordered.LOWEST_PRECEDENCE);
        }

    }

    @Configuration
    @ConditionalOnClass(SecurityFilterChain.class)
    @ConditionalOnWebApplication
    @AutoConfigureAfter(name = {
            "org.springframework.boot.autoconfigure.security.SecurityFilterAutoConfiguration", // Spring Boot 1.x
            "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration" // Spring Boot 2.x
    })
    static class SecurityServletFilterConfiguration {

        private static final String FILTER_NAME = "secureLogbookFilter";

        @Bean
        @ConditionalOnProperty(name = "logbook.secure-filter.enabled", havingValue = "true", matchIfMissing = true)
        @ConditionalOnMissingBean(name = FILTER_NAME)
        public FilterRegistrationBean secureLogbookFilter(final Logbook logbook) {
            return newFilter(new SecureLogbookFilter(logbook), FILTER_NAME, Ordered.HIGHEST_PRECEDENCE + 1);
        }

    }

    private static FilterRegistrationBean newFilter(final Filter filter, final String filterName, final int order) {
        @SuppressWarnings("unchecked") // as of Spring Boot 2.x
        final FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setName(filterName);
        registration.setDispatcherTypes(REQUEST, ASYNC);
        registration.setOrder(order);
        return registration;
    }

}
