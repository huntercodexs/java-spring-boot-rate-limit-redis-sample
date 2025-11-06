# java-spring-boot-rate-limit-redis-sample
Sample application to demonstration rate limit + redis management

### How to test

```shell
for i in {1..10}; do sleep 1; curl -i -H "X-Client-Id: client-1" http://localhost:8080/test; echo; done
```

Result

```text
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 28
Date: Thu, 06 Nov 2025 16:59:35 GMT

Dados liberados com sucesso!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 28
Date: Thu, 06 Nov 2025 16:59:36 GMT

Dados liberados com sucesso!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 28
Date: Thu, 06 Nov 2025 16:59:37 GMT

Dados liberados com sucesso!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 28
Date: Thu, 06 Nov 2025 16:59:38 GMT

Dados liberados com sucesso!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 28'
Date: Thu, 06 Nov 2025 16:59:39 GMT

Dados liberados com sucesso!
HTTP/1.1 429 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 06 Nov 2025 16:59:40 GMT

{"error":"Too Many Requests","message":"Rate limit exceeded for client-1","timestamp":"2025-11-06T16:59:40.125740116Z","status":429}
HTTP/1.1 429 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 06 Nov 2025 16:59:41 GMT

{"error":"Too Many Requests","message":"Rate limit exceeded for client-1","timestamp":"2025-11-06T16:59:41.150841518Z","status":429}
HTTP/1.1 429 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 06 Nov 2025 16:59:42 GMT

{"error":"Too Many Requests","message":"Rate limit exceeded for client-1","timestamp":"2025-11-06T16:59:42.164288683Z","status":429}
HTTP/1.1 429 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 06 Nov 2025 16:59:43 GMT

{"error":"Too Many Requests","message":"Rate limit exceeded for client-1","timestamp":"2025-11-06T16:59:43.177822004Z","status":429}
HTTP/1.1 429 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 06 Nov 2025 16:59:44 GMT

{"error":"Too Many Requests","message":"Rate limit exceeded for client-1","timestamp":"2025-11-06T16:59:44.191404570Z","status":429}
```