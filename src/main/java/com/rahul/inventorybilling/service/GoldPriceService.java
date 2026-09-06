package com.rahul.inventorybilling.service;

import com.rahul.inventorybilling.dto.GoldPriceResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Keeps one gold price in memory and refreshes it on a timer.
 *
 * Three things worth understanding here:
 *
 * 1. The API key lives on the server, never in the page. If the browser called
 *    the price provider directly, anyone could open dev tools and read your
 *    key. So the browser asks us, and we ask them.
 *
 * 2. We do not call the provider on every request. The price is fetched on a
 *    schedule into a field, and every request reads that field. A hundred
 *    people loading the dashboard costs zero extra API calls. That is a cache,
 *    and it is what keeps you inside a free tier.
 *
 * 3. If no key is configured, or the call fails, we serve the configured
 *    static price instead. The app always starts and always answers - somebody
 *    cloning this repo without a key still gets a working dashboard.
 */
@Service
public class GoldPriceService {

    private static final BigDecimal TEN = new BigDecimal("10");

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gold.price.static}")
    private BigDecimal staticPricePerGram;

    @Value("${gold.price.currency}")
    private String currency;

    @Value("${gold.api.url:}")
    private String apiUrl;

    @Value("${gold.api.key:}")
    private String apiKey;

    @Value("${gold.api.price-field:price_gram_24k}")
    private String priceField;

    // The cached answer. Every web request reads these three fields.
    private BigDecimal pricePerGram;
    private String source;
    private LocalDateTime asOf;

    /** Runs once when the app starts, so the first request is never empty. */
    @PostConstruct
    public void loadOnStartup() {
        refresh();
    }

    /**
     * Runs again every 30 minutes. fixedDelay means "wait this long after the
     * previous run finished", so a slow call can never stack up.
     */
    @Scheduled(fixedDelayString = "${gold.refresh-millis}")
    public void refresh() {
        if (apiUrl.isEmpty() || apiKey.isEmpty()) {
            usestatic("no api key configured");
            return;
        }

        try {
            // Ask the provider. The response comes back as a JSON object, which
            // Jackson hands us as a Map of field name to value.
            Map<String, Object> body = restTemplate.getForObject(apiUrl + "?access_key=" + apiKey, Map.class);

            if (body == null || body.get(priceField) == null) {
                usestatic("provider returned no " + priceField);
                return;
            }

            // Numbers arrive as Double or Integer, so go via toString rather
            // than casting - and never via double, for the usual reason.
            BigDecimal fetched = new BigDecimal(body.get(priceField).toString());
            this.pricePerGram = fetched.setScale(2, RoundingMode.HALF_UP);
            this.source = "live";
            this.asOf = LocalDateTime.now();

        } catch (Exception exception) {
            // The provider being down must never take our dashboard down.
            usestatic("provider unreachable");
        }
    }

    private void usestatic(String why) {
        this.pricePerGram = staticPricePerGram.setScale(2, RoundingMode.HALF_UP);
        this.source = "configured (" + why + ")";
        this.asOf = LocalDateTime.now();
    }

    public GoldPriceResponse getPrice() {
        return new GoldPriceResponse(
                pricePerGram,
                pricePerGram.multiply(TEN).setScale(2, RoundingMode.HALF_UP),
                currency,
                source,
                asOf);
    }
}
