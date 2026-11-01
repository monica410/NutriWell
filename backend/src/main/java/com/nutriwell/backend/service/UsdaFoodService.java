package com.nutriwell.backend.service;

import com.nutriwell.backend.dto.FoodDetailsResponse;
import com.nutriwell.backend.dto.FoodSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class UsdaFoodService {

    private final RestClient restClient;
    private final String apiKey;

    public UsdaFoodService(
            @Value("${usda.base-url}") String baseUrl,
            @Value("${usda.api-key}") String apiKey) {
        this.restClient = createClient(baseUrl);
        this.apiKey = apiKey;
    }

    private static RestClient createClient(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(20));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }

    public FoodSearchResponse searchFoods(String query, int pageSize, int pageNumber) {
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query must not be blank.");
        }
        if (pageSize < 1 || pageSize > 50 || pageNumber < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 50 and page number must be > 0.");
        }

        return fetch(() -> restClient.post()
                .uri(builder -> builder.path("/foods/search")
                        .queryParam("api_key", "{apiKey}").build(apiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "query", query.trim(),
                        "pageSize", pageSize,
                        "pageNumber", pageNumber,
                        "dataType", List.of("Foundation", "Survey (FNDDS)", "Branded")))
                .retrieve().body(FoodSearchResponse.class), false);
    }

    public FoodDetailsResponse getFood(long fdcId) {
        if (fdcId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FDC ID must be positive.");
        }

        return fetch(() -> restClient.get()
                .uri(builder -> builder.path("/food/{fdcId}")
                        .queryParam("api_key", "{apiKey}").build(fdcId, apiKey))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().body(FoodDetailsResponse.class), true);
    }

    private <T> T fetch(Supplier<T> request, boolean foodDetails) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "USDA_API_KEY is not configured.");
        }
        try {
            T response = request.get();
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "USDA returned an empty response.");
            }
            return response;
        } catch (RestClientResponseException exception) {
            if (foodDetails && exception.getStatusCode().value() == 404) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Food was not found.");
            }
            if (exception.getStatusCode().value() == 429) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "USDA request limit reached. Please try again later.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "USDA could not complete the request.");
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "USDA is temporarily unreachable.");
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "USDA returned an invalid response.");
        }
    }
}
