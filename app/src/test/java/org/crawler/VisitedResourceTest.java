package org.crawler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class VisitedResourceTest {
    @Test
    void testVisitedResource() {

        VisitedResource.register("https://www.example.com", "example.com");
        String filePath = VisitedResource.getFilePath("https://www.example.com");
        assertNotNull(filePath);
        String filePath2 = VisitedResource.getFilePath("https://www.example.example.com");
        assertNull(filePath2);
    }
}
