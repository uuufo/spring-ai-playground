package camp.coconut.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfig {

    /**
     * Set up routing for the embedded React frontend.
     * Requests to paths starting with {@code /static} are handled as static resource requests.
     * Requests not matching static resources or {@code manifest.json} are routed to serve
     * the React application's {@code index.html}.
     */
    @Bean
    public RouterFunction<ServerResponse> reactRouter() {
        return route(GET("/{path:^(?!static|manifest\\.json).*}"), request ->
                ServerResponse.ok().bodyValue(new ClassPathResource("static/index.html")));
    }
}