package com.apache.camel.catalog.mcp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.DefaultCamelCatalog;

@ApplicationScoped
public class CamelCatalogProducer {

    @Produces
    public CamelCatalog camelCatalog() {
        return new DefaultCamelCatalog(true);
    }
}
