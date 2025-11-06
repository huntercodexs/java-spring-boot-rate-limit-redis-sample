# java-spring-boot-rate-limit-redis-sample
Sample application to demonstration rate limit + redis management

### How to test

```shell
for i in {1..12}; do sleep 1; curl -i http://localhost:8080/fast; echo ""; done
```

Result

```text
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 54
Date: Thu, 06 Nov 2025 11:22:06 GMT

This endpoint allows only 3 requests every 30 seconds!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 54
Date: Thu, 06 Nov 2025 11:22:07 GMT

This endpoint allows only 3 requests every 30 seconds!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 54
Date: Thu, 06 Nov 2025 11:22:08 GMT

This endpoint allows only 3 requests every 30 seconds!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 54
Date: Thu, 06 Nov 2025 11:22:09 GMT

This endpoint allows only 3 requests every 30 seconds!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 54
Date: Thu, 06 Nov 2025 11:22:10 GMT

This endpoint allows only 3 requests every 30 seconds!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 54
Date: Thu, 06 Nov 2025 11:22:11 GMT

This endpoint allows only 3 requests every 30 seconds!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 54
Date: Thu, 06 Nov 2025 11:22:12 GMT

This endpoint allows only 3 requests every 30 seconds!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 54
Date: Thu, 06 Nov 2025 11:22:13 GMT

This endpoint allows only 3 requests every 30 seconds!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 54
Date: Thu, 06 Nov 2025 11:22:14 GMT

This endpoint allows only 3 requests every 30 seconds!
HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 54
Date: Thu, 06 Nov 2025 11:22:15 GMT

This endpoint allows only 3 requests every 30 seconds!
HTTP/1.1 429 
Content-Length: 35
Date: Thu, 06 Nov 2025 11:22:16 GMT

Too many requests - try again later
HTTP/1.1 429 
Content-Length: 35
Date: Thu, 06 Nov 2025 11:22:17 GMT

Too many requests - try again later
```