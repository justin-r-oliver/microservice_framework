package uk.gov.justice.services.adapters.test.utils.builder;


import javax.ws.rs.core.HttpHeaders;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;

public class HeadersBuilder {
    public static HttpHeaders headersWith(String headerName, String headerValue) {
        MultivaluedMapImpl headersMap = new MultivaluedMapImpl();
        headersMap.add(headerName, headerValue);
        return new ResteasyHttpHeaders(headersMap);
    }

}
