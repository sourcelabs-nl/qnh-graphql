package contracts

import org.springframework.cloud.contract.spec.Contract

[
        Contract.make {
            request {
                url('/products/123')
                method GET()
                headers {
                    accept(applicationJson())
                }
            }
            response {
                status 200
                fixedDelayMilliseconds 1500
                body([
                        "id"          : "123",
                        "title"       : "15-inch MacBook Pro - Touch Bar en Touch ID 2,6‑GHz 6-core-processor 512 GB opslag",
                        "imageUrl"    : "https://store.storeimages.cdn-apple.com/4667/as-images.apple.com/is/image/AppleInc/aos/published/images/m/bp/mbp15touch/space/mbp15touch-space-select-201807_GEO_NL",
                        "brand"       : "Apple",
                        "color"       : "Space Gray",
                        "memory"      : "16 GB",
                        "cpu"         : "2,6‑GHz 6‑core Intel Core i7‑processor van de 8e generatie",
                        "disk"        : "2 TB",
                        "display_inch": "15",
                        "weight_kg"   : "1.87"
                ])
                headers {
                    contentType(applicationJson())
                }
            }
        },
        Contract.make {
            request {
                url('/products/234')
                method GET()
                headers {
                    accept(applicationJson())
                }
            }
            response {
                status 200
                fixedDelayMilliseconds 1500
                body([
                        "id"       : "234",
                        "title"    : "Magic Keyboard met numeriek toetsenblok - Engels (US)",
                        "imageUrl" : "https://store.storeimages.cdn-apple.com/4667/as-images.apple.com/is/image/AppleInc/aos/published/images/M/RM/MRMH2/MRMH2",
                        "brand"    : "Apple",
                        "color"    : "Space Gray",
                        "weight_kg": "0.39"
                ])
                headers {
                    contentType(applicationJson())
                }
            }
        }
]