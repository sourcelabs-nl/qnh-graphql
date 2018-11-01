package nl.sourcelabs.graphql

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.contract.stubrunner.server.EnableStubRunnerServer
import org.springframework.context.support.beans
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.getForObject
import java.util.concurrent.CompletableFuture

@SpringBootApplication
@EnableStubRunnerServer
class Application {
    companion object {
        private val interceptor = ClientHttpRequestInterceptor { request, bytes, execution ->
            request.headers.accept = listOf(MediaType.APPLICATION_JSON)
            execution.execute(request, bytes)
        }

        private fun beans() = beans {
            bean { ref<RestTemplateBuilder>().additionalInterceptors(interceptor).rootUri(ref<Environment>()["wiremock.url"]).build() }
            bean<QueryResolver>()
            bean<OrderItemResolver>()
       }

        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Application>(*args) { addInitializers(beans()) }
        }
    }
}

data class Order(val id: String, val totalPrice: String, val items: List<OrderItem> = listOf())

data class OrderItem(val id: String, val productId: String, val status: String)

data class Product(val title: String, val imageUrl: String)

class QueryResolver(val restTemplate: RestTemplate) : GraphQLQueryResolver {
    fun orderById(id: String) = restTemplate.getForObject<Order>("/orders/$id")
    fun ordersByCustomerNumber(customerNumber: String) = restTemplate.exchange<List<Order>>("/orders?customerNumber=$customerNumber", HttpMethod.GET).body
}

class OrderItemResolver(val restTemplate: RestTemplate) : GraphQLResolver<OrderItem> {
    fun product(orderItem: OrderItem) = CompletableFuture.supplyAsync { restTemplate.getForObject<Product>("/products/${orderItem.productId}") }
}