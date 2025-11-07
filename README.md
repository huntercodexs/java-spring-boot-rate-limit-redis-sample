# java-spring-boot-rate-limit-redis-sample
Sample application to demonstration rate limit + redis management

### How to test

```shell
for i in {1..10}; do sleep 1; curl -i -H "X-Client-Id: client-1" http://localhost:8080/api/limitado; echo; done
```

Result

```text
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 38
Date: Fri, 07 Nov 2025 10:30:28 GMT

Requisição permitida. Limite: 3/10s.
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 38
Date: Fri, 07 Nov 2025 10:30:29 GMT

Requisição permitida. Limite: 3/10s.
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 38
Date: Fri, 07 Nov 2025 10:30:30 GMT

Requisição permitida. Limite: 3/10s.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 10:30:31 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 10:30:32 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 10:30:33 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 10:30:34 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 10:30:35 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 10:30:36 GMT

Limite de requisições excedido. Tente novamente mais tarde.
HTTP/1.1 429 
Content-Type: text/plain;charset=UTF-8
Content-Length: 61
Date: Fri, 07 Nov 2025 10:30:37 GMT

Limite de requisições excedido. Tente novamente mais tarde.
```