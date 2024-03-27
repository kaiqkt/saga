package com.kaiqkt.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class ContextTest {

    @Test
    void givenAContext_whenPutAValue_shouldBePossibleToGet() {
        final Node node = mock();
        final Transaction transaction = new Transaction(node, new HashMap<>());
        final Context context = new Context(transaction);
        final HashMap<String, Object> expected = new HashMap<>();

        context.set("key", "value");
        expected.put("key", "value");

        String result = context.get("key", String.class);

        assertEquals("value", result);
        assertEquals(expected, context.getPayload());
    }

    @Test
    void givenAContext_whenValueIsNull_shouldReturnNull() {
        final Node node = mock();
        final Transaction transaction = new Transaction(node, new HashMap<>());
        final Context context = new Context(transaction);

        String result = context.get("key", String.class);

        assertNull(result);
    }
}
