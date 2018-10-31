package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {

    request {
        url('/orders?customerNumber=1')
        method GET()
        headers {
            accept(applicationJson())
        }
    }
    response {
        status 200
        body([[
                      "id"        : 1,
                      "totalPrice": 3299.99,
                      "items"     : [[
                                             "id"       : 1,
                                             "productId": "123",
                                             "status"   : "Shipped"
                                     ]]
              ],
              [
                      "id"        : 2,
                      "totalPrice": 169.99,
                      "items"     : [[
                                             "id"       : 2,
                                             "productId": "234",
                                             "status"   : "Shipped"
                                     ]]
              ]
        ])
        headers {
            contentType(applicationJson())
        }
    }
}