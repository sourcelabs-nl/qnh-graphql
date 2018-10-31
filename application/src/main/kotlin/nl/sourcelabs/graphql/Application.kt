package nl.sourcelabs.graphql

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLResolver
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.contract.stubrunner.server.EnableStubRunnerServer
import org.springframework.context.support.beans
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import org.springframework.web.client.getForObject
import java.math.BigDecimal

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
            bean<MutationResolver>()
        }

        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<Application>(*args) { addInitializers(beans()) }
        }
    }
}

data class Order(val id: String, val totalPrice: BigDecimal, val items: List<OrderItem> = listOf())

data class OrderItem(val id: String, val productId: String, val status: String)

data class OrderItemCancellation(val orderId: String, val orderItemId: String, val reason: String?)

data class Product(val id: String, val title: String, val brand: String, val imageUrl: String)

class QueryResolver(private val restTemplate: RestTemplate) : GraphQLQueryResolver {
    fun orderById(id: Long) = restTemplate.getForObject<Order>("/orders/$id")
    fun ordersByCustomerNumber(customerNumber: String): List<Order> = restTemplate.exchange<List<Order>>(url = "/orders?customerNumber=$customerNumber", method = GET).body ?: listOf()
}

class OrderItemResolver(private val restTemplate: RestTemplate) : GraphQLResolver<OrderItem> {
    fun product(orderItem: OrderItem) = restTemplate.getForObject<Product>("/products/${orderItem.productId}")
}

class MutationResolver(private val restTemplate: RestTemplate) : GraphQLMutationResolver {
    fun cancelOrderItem(cancellation: OrderItemCancellation): OrderItem? {
        val order = restTemplate.getForObject<Order>("/orders/${cancellation.orderId}")
        return order?.items?.first { it.id == cancellation.orderItemId }?.copy(status = "Cancelled")
    }
}