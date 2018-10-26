package nl.sourcelabs.graphql

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.contract.stubrunner.server.EnableStubRunnerServer
import org.springframework.context.support.beans
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal

@SpringBootApplication
@EnableStubRunnerServer
class GraphQLApplication {
    companion object {
        private val interceptor = ClientHttpRequestInterceptor { request, bytes, execution ->
            request.headers.accept = listOf(MediaType.APPLICATION_JSON)
            execution.execute(request, bytes)
        }

        private fun beans() = beans {
            bean { ref<RestTemplateBuilder>().additionalInterceptors(interceptor).rootUri(ref<Environment>()["wiremock.url"]).build() }
            bean { ProductService(ref()) }
            bean { OrderService(ref()) }
            bean { OrderItemResolver(ref()) }
            bean { QueryResolver(ref()) }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<GraphQLApplication>(*args) { addInitializers(beans()) }
        }
    }
}

data class Order(val id: Long, val totalPrice: BigDecimal, val items: List<OrderItem> = listOf())
data class OrderItem(val id: Long, val productId: String)
data class Product(val id: String, val title: String, val brand: String, val imageUrl: String)

class QueryResolver(private val orderService: OrderService) : GraphQLQueryResolver {
    fun orderById(id: Long) = orderService.getOrderById(id)
    fun ordersByCustomerNumber(customerNumber: String): List<Order> = orderService.getOrdersByCustomerNumber(customerNumber) ?: listOf()
}

class OrderItemResolver(private val productService: ProductService) : GraphQLResolver<OrderItem> {
    fun productDetails(orderItem: OrderItem) = productService.getProductById(orderItem.productId)
}

class OrderService(val restTemplate: RestTemplate) {
    fun getOrderById(id: Long): Order? = restTemplate.getForObject("/orders/$id", Order::class.java)
    fun getOrdersByCustomerNumber(customerNumber: String) = restTemplate.exchange("/orders?customerNumber=$customerNumber", HttpMethod.GET, HttpEntity.EMPTY, object : ParameterizedTypeReference<List<Order>>() {}).body
}

class ProductService(val restTemplate: RestTemplate) {
    fun getProductById(productId: String): Product? = restTemplate.getForObject("/products/$productId", Product::class.java)
}
