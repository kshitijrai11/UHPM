package com.ultrahpm.orderservice.saga;

/**
 * Generic interface for a single step within a SAGA transaction.
 * Each step defines an action and a compensating rollback action.
 *
 * Per Docs V2 §7: The SAGA pattern ensures eventual consistency through
 * local transactions and compensatory actions, avoiding 2PC bottlenecks.
 *
 * @param <T> The context object carrying state across saga steps
 */
public interface SagaStep<T> {

    /**
     * Execute the forward action of this step.
     * @param context The saga context
     * @return Updated context after execution
     * @throws Exception if the step fails, triggering compensation
     */
    T execute(T context) throws Exception;

    /**
     * Compensate (rollback) this step if a downstream step fails.
     * @param context The saga context at the point of failure
     * @return Updated context after compensation
     */
    T compensate(T context);

    /**
     * Human-readable name for logging and tracing.
     */
    String getName();
}
