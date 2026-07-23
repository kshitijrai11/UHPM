package com.ultrahpm.recommendation;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

public class ReactiveServiceTest {

    @Test
    void verifyReactiveStreamWithStepVerifier() {
        // A simple test to demonstrate StepVerifier usage on a reactive stream
        Flux<String> recommendationStream = Flux.just("item1", "item2", "item3")
                .map(String::toUpperCase);

        StepVerifier.create(recommendationStream)
                .expectNext("ITEM1")
                .expectNext("ITEM2")
                .expectNext("ITEM3")
                .verifyComplete();
    }
}
