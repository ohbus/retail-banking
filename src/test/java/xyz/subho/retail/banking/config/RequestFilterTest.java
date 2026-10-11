package xyz.subho.retail.banking.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

class RequestFilterTest {

    @Test
    void propagatesDownstreamFailure() {
        RequestFilter filter = new RequestFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account/currentAccount");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> { throw new ServletException("boom"); };

        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));
    }

    @Test
    void answersPreflightWithoutCallingApplication() throws IOException, ServletException {
        RequestFilter filter = new RequestFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/account/deposit");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean called = new AtomicBoolean();
        FilterChain chain = (req, res) -> called.set(true);

        filter.doFilter(request, response, chain);

        assertTrue(!called.get());
        assertEquals(200, response.getStatus());
    }
}
